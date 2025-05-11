package com.juv3nil3.icdg.service;


import com.juv3nil3.icdg.domain.BranchMetadata;
import com.juv3nil3.icdg.domain.Documentation;
import com.juv3nil3.icdg.domain.RepositoryMetadata;
import com.juv3nil3.icdg.repository.BranchDataRepository;
import com.juv3nil3.icdg.repository.DocumentationRepository;
import com.juv3nil3.icdg.repository.RepositoryMetadataRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DocumentationGenerationService {

    private RepositoryMetadataRepo repositoryMetadataRepository;
    private BranchDataRepository branchMetadataRepository;
    private  GithubCloneService gitCloneService;
    private  StructureParserService structureParserService;
    private  DocumentationBuilderService documentationBuilderService;
    private DocumentationRepository documentationRepository;

    public Mono<Documentation> generateIfNecessary(String owner, String repoName, String branchName) {
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
                        gitCloneService.cloneRepository(owner, repoName, branchName)
                                .flatMap(clonePath -> structureParserService.parseRepositoryStructure(clonePath, owner, repoName, branchName)
                                        .then(repositoryMetadataRepository.findByOwnerAndRepoName(owner, repoName))
                                        .flatMap(repo -> branchMetadataRepository.findByRepositoryMetadataAndBranchName(repo, branchName))
                                        .flatMap(branch -> regenerateDocumentation(repo, branch))
                                )
                );
    }

    private Mono<Documentation> regenerateDocumentation(RepositoryMetadata repo, BranchMetadata branch) {
        return documentationBuilderService.buildDocumentation(repo, branch)
                .flatMap(generatedDoc -> {
                    generatedDoc.setBranch(branch);
                    generatedDoc.setCommitSha(branch.getLatestCommit());
                    return documentationRepository.save(generatedDoc);
                });
    }
}
