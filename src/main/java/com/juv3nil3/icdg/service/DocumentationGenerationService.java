package com.juv3nil3.icdg.service;


import com.juv3nil3.icdg.domain.BranchMetadata;
import com.juv3nil3.icdg.domain.Documentation;
import com.juv3nil3.icdg.domain.RepositoryMetadata;
import com.juv3nil3.icdg.repository.BranchDataRepository;
import com.juv3nil3.icdg.repository.DocumentationRepository;
import com.juv3nil3.icdg.repository.RepositoryMetadataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DocumentationGenerationService {

    @Autowired
    private RepositoryMetadataRepo repositoryMetadataRepository;
    @Autowired
    private BranchDataRepository branchMetadataRepository;
    @Autowired
    private  GithubCloneService gitCloneService;
    @Autowired
    private  StructureParserService structureParserService;
    @Autowired
    private  DocumentationBuilderService documentationBuilderService;
    @Autowired
    private DocumentationRepository documentationRepository;

    public Mono<Documentation> generateIfNecessary(String owner, String repoName, String branchName, String token) {
        return repositoryMetadataRepository.findByOwnerAndRepoName(owner, repoName)
                .flatMap(repo ->
                        branchMetadataRepository.findByRepositoryMetadataAndBranchName(repo, branchName)
                                .flatMap(branch ->
                                        documentationRepository.findByBranch(branch)
                                                .flatMap(existingDoc -> {
                                                    if (!existingDoc.getUpdatedAt().isBefore(branch.getUpdatedAt())) {
                                                        // Up-to-date
                                                        return Mono.just(existingDoc);
                                                    } else {
                                                        // Outdated
                                                        return regenerateDocumentation(repo, branch);
                                                    }
                                                })
                                                .switchIfEmpty(regenerateDocumentation(repo, branch))
                                )
                )
                .switchIfEmpty(
                        // If repo/branch don't exist, clone -> parse -> regenerate docs
                        gitCloneService.cloneRepository(owner, repoName, branchName,token)
                                .flatMap(clonePath -> structureParserService.parseRepositoryStructure(clonePath, owner, repoName, branchName)
                                        .then(repositoryMetadataRepository.findByOwnerAndRepoName(owner, repoName))
                                        .flatMap(repo -> branchMetadataRepository.findByRepositoryMetadataAndBranchName(repo, branchName))
                                        .flatMap(branch -> regenerateDocumentation(repo, branch))
                                )
                );
    }

    private Mono<Documentation> regenerateDocumentation(RepositoryMetadata repo, BranchMetadata branch) {
        return documentationRepository.findByBranch(branch)
                .defaultIfEmpty(new Documentation())
                .flatMap(existingDoc -> documentationBuilderService.buildDocumentation(repo, branch)
                        .flatMap(generatedDoc -> {
                            generatedDoc.setId(existingDoc.getId()); // retain ID if exists
                            generatedDoc.setBranch(branch);
                            generatedDoc.setCreatedAt(existingDoc.getCreatedAt() != null ? existingDoc.getCreatedAt() : LocalDateTime.now());
                            generatedDoc.setUpdatedAt(LocalDateTime.now());
                            return documentationRepository.save(generatedDoc);
                        })
                );
    }
}
