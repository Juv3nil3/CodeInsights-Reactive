package com.juv3nil3.icdg.service;


import com.juv3nil3.icdg.domain.BranchMetadata;
import com.juv3nil3.icdg.domain.Documentation;
import com.juv3nil3.icdg.domain.RepositoryMetadata;
import com.juv3nil3.icdg.repository.BranchDataRepository;
import com.juv3nil3.icdg.repository.DocumentationRepository;
import com.juv3nil3.icdg.repository.RepositoryMetadataRepo;
import com.juv3nil3.icdg.service.dto.DocumentationDTO;
import com.juv3nil3.icdg.service.mapper.DocumentationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

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
    @Autowired
    private DocumentationMapper documentationMapper;

    private static Logger log = LoggerFactory.getLogger(DocumentationGenerationService.class);

    public Mono<DocumentationDTO> generateIfNecessary(String owner, String repoName, String branchName, String token) {
        log.info("Starting documentation generation for repo: {}/{}, branch: {}", owner, repoName, branchName);

        return repositoryMetadataRepository.findByOwnerAndRepoName(owner, repoName)
                .doOnNext(repo -> log.debug("Found RepositoryMetadata: id={}, name={}", repo.getId(), repo.getRepoName()))
                .flatMap(repo ->
                        branchMetadataRepository.findByBranchNameAndRepositoryMetadataId(branchName, repo.getId())
                                .doOnNext(branch -> log.debug("Found BranchMetadata: id={}, name={}", branch.getId(), branch.getBranchName()))
                                .flatMap(branch ->
                                        documentationRepository.findByBranchMetadataId(branch.getId())
                                                .doOnNext(existingDoc -> log.debug("Found existing documentation for branchId={}", branch.getId()))
                                                .flatMap(existingDoc -> {
                                                    if (!existingDoc.getUpdatedAt().isBefore(branch.getUpdatedAt())) {
                                                        log.info("Documentation is up-to-date for branch: {}", branchName);
                                                        return Mono.just(existingDoc);
                                                    } else {
                                                        log.info("Documentation is outdated for branch: {}, regenerating...", branchName);
                                                        return regenerateDocumentation(repo, branch);
                                                    }
                                                })
                                                .switchIfEmpty(Mono.defer(() -> {
                                                    log.info("No existing documentation found for branch: {}, generating new...", branchName);
                                                    return regenerateDocumentation(repo, branch);
                                                }))
                                )
                )
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Repository or branch not found. Cloning and parsing repository: {}/{}:{}", owner, repoName, branchName);
                    return gitCloneService.cloneRepository(owner, repoName, branchName, token)
                            .flatMap(clonePath ->
                                    structureParserService.parseRepositoryStructure(clonePath, owner, repoName, branchName)
                                            .doOnSuccess(v -> log.info("Structure parsed successfully."))
                                            .then(repositoryMetadataRepository.findByOwnerAndRepoName(owner, repoName))
                                            .flatMap(repo ->
                                                    branchMetadataRepository.findByBranchNameAndRepositoryMetadataId(branchName, repo.getId())
                                                            .flatMap(branch -> regenerateDocumentation(repo, branch))
                                            )
                            );
                }))
                .doOnSuccess(doc -> log.info("Documentation generation completed for {}/{}:{}", owner, repoName, branchName))
                .doOnError(error -> log.error("Error during documentation generation for {}/{}:{} -> {}", owner, repoName, branchName, error.getMessage(), error))
                .map(documentationMapper::toDto);
    }

    private Mono<Documentation> regenerateDocumentation(RepositoryMetadata repo, BranchMetadata branch) {
        log.debug("Regenerating documentation for repoId={}, branchId={}", repo.getId(), branch.getId());

        return documentationRepository.findByBranchMetadataId(branch.getId())
                .flatMap(existingDoc -> {
                    // Update existing
                    return documentationBuilderService.buildDocumentation(repo, branch)
                            .doOnNext(doc -> log.debug("Built new doc for branchId={}", branch.getId()))
                            .flatMap(builtDoc -> {
                                builtDoc.setId(existingDoc.getId()); // Preserve ID
                                builtDoc.setCreatedAt(existingDoc.getCreatedAt()); // Preserve created time
                                builtDoc.setUpdatedAt(LocalDateTime.now());
                                return documentationRepository.save(builtDoc);
                            });
                })
                .switchIfEmpty(
                        documentationBuilderService.buildDocumentation(repo, branch)
                                .doOnNext(doc -> log.debug("Built first-time doc for branchId={}", branch.getId()))
                                .flatMap(builtDoc -> {
                                    builtDoc.setUpdatedAt(LocalDateTime.now()); // createdAt already set
                                    return documentationRepository.save(builtDoc);
                                })
                )
                .doOnNext(saved -> log.debug("📄 Saved documentation for branchId={} with ID={}", branch.getId(), saved.getId()));
    }

}
