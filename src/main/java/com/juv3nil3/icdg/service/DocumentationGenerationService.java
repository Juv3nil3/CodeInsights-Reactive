package com.juv3nil3.icdg.service;


import com.juv3nil3.icdg.domain.*;
import com.juv3nil3.icdg.repository.*;
import com.juv3nil3.icdg.service.dto.DocumentationDTO;
import com.juv3nil3.icdg.service.mapper.DocumentationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    @Autowired
    private ClassDataRepository classDataRepository;
    @Autowired
    private BranchFileAssociationRepository branchFileAssociationRepository;
    @Autowired
    private FieldDataRepository fieldDataRepository;
    @Autowired
    private MethodDataRepository methodDataRepository;

    @Autowired
    private FileDataRepository fileDataRepository;

    @Autowired
    private PackageDataRepository packageDataRepository;

    private static Logger log = LoggerFactory.getLogger(DocumentationGenerationService.class);

    public Mono<UUID> generateIfNecessary(String owner, String repoName, String branchName, String token) {
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
                .map(doc -> {
                    log.info("📄 Returning documentation ID: {}", doc.getId());
                    return doc.getId();
                });

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


    public Mono<Documentation> fetchFullDocumentation(UUID documentationId) {
        return documentationRepository.findById(documentationId)
                .switchIfEmpty(Mono.error(new RuntimeException("Documentation not found")))
                .flatMap(doc -> branchMetadataRepository.findById(doc.getBranchMetadataId())
                        .flatMap(branch -> repositoryMetadataRepository.findById(branch.getRepositoryMetadataId())
                                .map(repo -> {
                                    branch.setRepositoryMetadata(repo);
                                    doc.setBranchMetadata(branch);
                                    return doc;
                                })))
                .flatMap(this::populatePackages);
    }



    private Mono<Documentation> populatePackages(Documentation documentation) {
        UUID branchId = documentation.getBranchMetadataId();
        return packageDataRepository.findAllByBranchId(branchId)
                .collectList()
                .flatMap(packages -> {
                    documentation.setPackages(packages);
                    return Flux.fromIterable(packages)
                            .flatMap(pkg -> branchFileAssociationRepository.findAllByPackageDataId(pkg.getId())
                                    .collectList()
                                    .flatMap(associations -> {
                                        pkg.setFileAssociations(associations);
                                        pkg.setBranch(documentation.getBranchMetadata());
                                        return Flux.fromIterable(associations)
                                                .flatMap(assoc -> fileDataRepository.findById(assoc.getFileId())
                                                        .flatMap(file -> {
                                                            assoc.setFile(file);
                                                            return populateFileClasses(file);
                                                        }))
                                                .then(Mono.just(pkg));
                                    }))
                            .then(Mono.just(documentation));
                });
    }

    private Mono<FileData> populateFileClasses(FileData file) {
        return classDataRepository.findAllByFileDataId(file.getId())
                .collectList()
                .flatMap(classes -> Flux.fromIterable(classes)
                        .flatMap(clazz -> {
                            clazz.setFileData(file);
                            Mono<List<MethodData>> methodsMono = methodDataRepository.findAllByClassDataId(clazz.getId()).collectList();
                            Mono<List<FieldData>> fieldsMono = fieldDataRepository.findAllByClassDataId(clazz.getId()).collectList();

                            return Mono.zip(methodsMono, fieldsMono)
                                    .map(tuple -> {
                                        clazz.setMethods(tuple.getT1());
                                        clazz.setFields(tuple.getT2());
                                        return clazz;
                                    });
                        })
                        .collectList()
                        .map(classList -> {
                            file.setClasses(classList);
                            return file;
                        }));
    }




}
