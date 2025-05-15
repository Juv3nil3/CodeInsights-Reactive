package com.juv3nil3.icdg.service;


import com.juv3nil3.icdg.domain.*;
import com.juv3nil3.icdg.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class StructureParserService {

    private final RepositoryMetadataRepo repositoryMetadataRepository;
    private final BranchDataRepository branchMetadataRepository;
    private final PackageDataRepository packageRepository;
    private final FileDataRepository fileRepository;
    private final ClassDataRepository classRepository;
    private final FieldDataRepository fieldRepository;
    private final MethodDataRepository methodRepository;
    private final JavaFileParser javaFileParserService;
    private final BranchFileAssociationRepository branchFileAssociationRepository;
    private final AnnotationDataRepository annotationDataRepository;

    @Autowired
    public StructureParserService(RepositoryMetadataRepo repositoryMetadataRepository, BranchDataRepository branchMetadataRepository, PackageDataRepository packageRepository, FileDataRepository fileRepository, ClassDataRepository classRepository, FieldDataRepository fieldRepository, MethodDataRepository methodRepository, JavaFileParser javaFileParserService, BranchFileAssociationRepository branchFileAssociationRepository, AnnotationDataRepository annotationDataRepository) {
        this.repositoryMetadataRepository = repositoryMetadataRepository;
        this.branchMetadataRepository = branchMetadataRepository;
        this.packageRepository = packageRepository;
        this.fileRepository = fileRepository;
        this.classRepository = classRepository;
        this.fieldRepository = fieldRepository;
        this.methodRepository = methodRepository;
        this.javaFileParserService = javaFileParserService;
        this.branchFileAssociationRepository = branchFileAssociationRepository;
        this.annotationDataRepository = annotationDataRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(StructureParserService.class);

    public Mono<Void> parseRepositoryStructure(Path repoPath, String owner, String repoName, String branchName) {
        return Mono.fromCallable(() -> GitMetadataExtractor.getLatestCommitSha(repoPath, branchName))
                .flatMap(latestCommitSha ->
                        createOrUpdateRepositoryMetadata(owner, repoName)
                                .flatMap(repo ->
                                        branchMetadataRepository.findByBranchNameAndRepositoryMetadataId(branchName, repo.getId())
                                                .flatMap(existingBranch -> {
                                                    if (latestCommitSha.equals(existingBranch.getLatestCommitHash())) {
                                                        return Mono.empty(); // Already up-to-date
                                                    }
                                                    return updateBranchMetadata(existingBranch, latestCommitSha)
                                                            .flatMap(branch -> parseAndWalkJavaFiles(repoPath, repo, branch));
                                                })
                                                .switchIfEmpty(
                                                        createOrUpdateBranchMetadata(repo, branchName, latestCommitSha)
                                                                .flatMap(branch -> parseAndWalkJavaFiles(repoPath, repo, branch))
                                                )
                                )
                );
    }



    public Mono<RepositoryMetadata> createOrUpdateRepositoryMetadata(String owner, String repoName) {
        return repositoryMetadataRepository.findByOwnerAndRepoName(owner, repoName)
                .switchIfEmpty(Mono.defer(() -> {
                    RepositoryMetadata newRepo = new RepositoryMetadata();
                    newRepo.setOwner(owner);
                    newRepo.setRepoName(repoName);
                    newRepo.setCreatedAt(LocalDateTime.now());
                    newRepo.setUpdatedAt(LocalDateTime.now());
                    return repositoryMetadataRepository.save(newRepo);
                }))
                .flatMap(existing -> {
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repositoryMetadataRepository.save(existing);
                });
    }


    public Mono<BranchMetadata> createOrUpdateBranchMetadata(RepositoryMetadata repo, String branchName, String latestCommitSha) {
        return branchMetadataRepository.findByBranchNameAndRepositoryMetadataId(branchName, repo.getId())
                .switchIfEmpty(Mono.defer(() -> {
                    BranchMetadata newBranch = new BranchMetadata();
                    newBranch.setRepositoryMetadataId(repo.getId());
                    newBranch.setBranchName(branchName);
                    newBranch.setLatestCommitHash(latestCommitSha);
                    newBranch.setCreatedAt(LocalDateTime.now());
                    newBranch.setUpdatedAt(LocalDateTime.now());
                    return branchMetadataRepository.save(newBranch);
                }))
                .flatMap(existing -> {
                    existing.setUpdatedAt(LocalDateTime.now());
                    existing.setLatestCommitHash(latestCommitSha);
                    return branchMetadataRepository.save(existing);
                });
    }

    public Mono<BranchMetadata> updateBranchMetadata(BranchMetadata branch, String latestCommitSha) {
        branch.setUpdatedAt(LocalDateTime.now());
        branch.setLatestCommitHash(latestCommitSha);
        return branchMetadataRepository.save(branch);
    }

    private Mono<Void> parseAndWalkJavaFiles(Path repoPath, RepositoryMetadata repo, BranchMetadata branch) {
        try {
            return Flux.fromStream(Files.walk(repoPath))
                    .filter(path -> path.toString().endsWith(".java"))
                    .flatMap(path -> parseAndSaveFile(path, repoPath, repo, branch))
                    .then();
        } catch (IOException e) {
            return Mono.error(new RuntimeException("Failed to walk repo path", e));
        }
    }




    private Mono<Void> parseAndSaveFile(Path path, Path repoRoot,
                                        RepositoryMetadata repo, BranchMetadata branch) {
        try {
            InputStream fileStream = Files.newInputStream(path);
            byte[] fileBytes = fileStream.readAllBytes();
            fileStream.close();

            String contentHash = DigestUtils.sha256Hex(fileBytes);
            String relativePath = repoRoot.relativize(path).toString();
            String repoName = repo.getRepoName();

            return fileRepository.findByRepoNameAndContentHash(repoName, contentHash)
                    .flatMap(existingFile -> {
                        if (!existingFile.getFilePath().equals(relativePath)) {
                            return branchFileAssociationRepository.findByBranchIdAndFileId(branch.getId(), existingFile.getId())
                                    .flatMap(branchFileAssociationRepository::delete)
                                    .then(Mono.defer(() -> {
                                        existingFile.setFilePath(relativePath);
                                        return fileRepository.save(existingFile);
                                    }));
                        }
                        return Mono.just(existingFile);
                    })
                    .switchIfEmpty(
                            javaFileParserService.parseJavaFile(new ByteArrayInputStream(fileBytes))
                                    .flatMap(parsedFile -> {
                                        parsedFile.setRepoName(repoName);
                                        parsedFile.setFilePath(relativePath);
                                        parsedFile.setContentHash(contentHash);
                                        return saveFileData(parsedFile);
                                    })
                    )
                    .flatMap(file -> {
                        String contentString = new String(fileBytes);
                        return getOrCreatePackage(extractPackageName(contentString), repo, branch)
                                .flatMap(pkg -> {
                                    BranchFileAssociation assoc = new BranchFileAssociation();
                                    assoc.setRepoName(repoName);
                                    assoc.setBranchId(branch.getId());
                                    assoc.setFileId(file.getId());
                                    assoc.setPackageDataId(pkg.getId());
                                    assoc.setFilePath(relativePath);
                                    return branchFileAssociationRepository.save(assoc).then();
                                });
                    });

        } catch (IOException e) {
            return Mono.error(new RuntimeException("Failed to read file: " + path, e));
        }
    }



    private Mono<FileData> saveFileData(FileData fileData) {
        return fileRepository.save(fileData)
                .flatMap(savedFile ->
                        Flux.fromIterable(fileData.getClasses())
                                .flatMap(clazz -> saveClassData(clazz, savedFile))
                                .then(Mono.just(savedFile))  // Return savedFile after processing classes
                );
    }


    private Mono<ClassData> saveClassData(ClassData clazz, FileData fileData) {
        clazz.setFileDataId(fileData.getId());
        return classRepository.save(clazz)
                .flatMap(savedClass ->
                        Mono.when(
                                saveAnnotations(clazz.getAnnotations(), savedClass.getId(), null, null),
                                saveFieldData(clazz.getFields(), savedClass),
                                saveMethodData(clazz.getMethods(), savedClass)
                        ).thenReturn(savedClass)
                );
    }

    private Mono<Void> saveFieldData(List<FieldData> fields, ClassData classData) {
        return Flux.fromIterable(fields)
                .flatMap(field -> {
                    field.setClassDataId(classData.getId());
                    return fieldRepository.save(field)
                            .flatMap(savedField ->
                                    saveAnnotations(field.getAnnotations(), null, savedField.getId(), null)
                            );
                })
                .then();
    }

    private Mono<Void> saveMethodData(List<MethodData> methods, ClassData classData) {
        return Flux.fromIterable(methods)
                .flatMap(method -> {
                    method.setClassDataId(classData.getId());
                    return methodRepository.save(method)
                            .flatMap(savedMethod ->
                                    saveAnnotations(method.getAnnotations(), null, null, savedMethod.getId())
                            );
                })
                .then();
    }

    private Mono<Void> saveAnnotations(List<AnnotationData> annotations,
                                       Long classId,
                                       Long fieldId,
                                       Long methodId) {
        if (annotations == null || annotations.isEmpty()) {
            return Mono.empty();
        }

        return Flux.fromIterable(annotations)
                .doOnNext(annotation -> {
                    annotation.setClassDataId(classId);
                    annotation.setFieldDataId(fieldId);
                    annotation.setMethodDataId(methodId);
                })
                .flatMap(annotationDataRepository::save)
                .then();
    }


    private String extractPackageName(String content) {
        if (content == null || content.isBlank()) return "default";
        Matcher matcher = Pattern.compile("^\\s*package\\s+([a-zA-Z0-9_.]+)\\s*;", Pattern.MULTILINE).matcher(content);
        return matcher.find() ? matcher.group(1).trim() : "default";
    }

    private Mono<PackageData> getOrCreatePackage(String packageName, RepositoryMetadata repo, BranchMetadata branch) {
        String effectiveName = (packageName == null || packageName.isBlank()) ? "default" : packageName;

        return packageRepository.findByBranchIdAndPackageName(branch.getId(), effectiveName)
                .switchIfEmpty(Mono.defer(() -> {
                    String parentName = extractParentPackage(effectiveName);

                    Mono<PackageData> parentMono = (parentName != null)
                            ? getOrCreatePackage(parentName, repo, branch)
                            : Mono.empty();

                    return parentMono.flatMap(parentPkg -> {
                        PackageData newPkg = new PackageData();
                        newPkg.setPackageName(effectiveName);
                        newPkg.setRepoName(repo.getRepoName());
                        newPkg.setBranchId(branch.getId());
                        if (parentPkg != null) newPkg.setParentPackageId(parentPkg.getId()); // Set parent package
                        return packageRepository.save(newPkg);
                    });
                }));
    }

    private String extractParentPackage(String packageName) {
        if (packageName == null || packageName.isBlank()) return null;
        int lastDot = packageName.lastIndexOf('.');
        return (lastDot > 0) ? packageName.substring(0, lastDot) : null;
    }

}
