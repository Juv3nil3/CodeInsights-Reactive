package com.juv3nil3.icdg.service;


import com.juv3nil3.icdg.domain.*;
import com.juv3nil3.icdg.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
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

    private final ConcurrentHashMap<String, Mono<RepositoryMetadata>> repositoryLocks = new ConcurrentHashMap<>();

    public Mono<Void> parseRepositoryStructure(Path repoPath, String owner, String repoName, String branchName) {
        log.info("🚀 Starting structure parsing for {}/{} [{}]", owner, repoName, branchName);

        return Mono.fromCallable(() -> GitMetadataExtractor.getLatestCommitSha(repoPath, branchName))
                .flatMap(latestCommitSha ->
                        createOrUpdateRepositoryMetadata(owner, repoName)
                                .flatMap(repository ->
                                        branchMetadataRepository.findByBranchNameAndRepositoryMetadataId(branchName, repository.getId())
                                                .flatMap(existingBranch -> {
                                                    if (latestCommitSha.equals(existingBranch.getLatestCommitHash())) {
                                                        log.info("✅ Branch {} is up-to-date. Skipping parsing.", branchName);
                                                        return Mono.empty(); // Skipping is okay
                                                    }

                                                    log.info("🔄 Branch {} is outdated. Updating commit hash.", branchName);
                                                    return createOrUpdateBranchMetadata(repository, branchName, latestCommitSha)
                                                            .flatMap(updatedBranch -> parseAndWalkJavaFiles(repoPath, repository, updatedBranch));
                                                })
                                                .switchIfEmpty(
                                                        createOrUpdateBranchMetadata(repository, branchName, latestCommitSha)
                                                                .flatMap(newBranch -> {
                                                                    log.info("🆕 Created new branch metadata for {}", branchName);
                                                                    return parseAndWalkJavaFiles(repoPath, repository, newBranch);
                                                                })
                                                )
                                )
                )
                .doOnError(e -> log.error("❌ Error parsing repo structure", e))
                .then(); // 🔧 Ensure terminal operator is there
    }







    public Mono<RepositoryMetadata> createOrUpdateRepositoryMetadata(String owner, String repoName) {
        LocalDateTime now = LocalDateTime.now();

        return repositoryMetadataRepository.findByOwnerAndRepoName(owner, repoName)
                .flatMap(existing -> {
                    existing.setUpdatedAt(now);
                    return repositoryMetadataRepository.save(existing);
                })
                .switchIfEmpty(
                        repositoryMetadataRepository.save(new RepositoryMetadata(
                                UUID.randomUUID(), owner, repoName, null, null, now, now
                        ))
                )
                .doOnNext(repo -> log.info("✅ Saved RepositoryMetadata: id={}, name={}", repo.getId(), repo.getRepoName()));
    }





    public Mono<BranchMetadata> createOrUpdateBranchMetadata(RepositoryMetadata repo, String branchName, String latestCommitSha) {
        LocalDateTime now = LocalDateTime.now();
        UUID id = UUID.randomUUID();

        return branchMetadataRepository
                .mergeBranchMetadata(id, branchName, latestCommitSha, repo.getId(), now, now)
                .then(branchMetadataRepository.findByBranchNameAndRepositoryMetadataId(branchName, repo.getId()))
                .switchIfEmpty(
                        branchMetadataRepository.save(new BranchMetadata(id, branchName, latestCommitSha, repo.getId(), now, now))
                                .doOnNext(b -> log.info("✅ Fallback saved BranchMetadata: id={}, name={}", b.getId(), b.getBranchName()))
                )
                .doOnNext(branch -> log.info("📌 Using BranchMetadata: id={}, name={}", branch.getId(), branch.getBranchName()));
    }





    private Mono<Void> parseAndWalkJavaFiles(Path repoPath, RepositoryMetadata repo, BranchMetadata branch) {
        try {
            log.info("Walking files for repo {} branch {}", repo.getRepoName(), branch.getBranchName());
            return Flux.fromStream(Files.walk(repoPath))
                    .filter(path -> path.toString().endsWith(".java"))
                    .flatMap(path -> parseAndSaveFile(path, repoPath, repo, branch))
                    .then();
        } catch (IOException e) {
            log.error("Failed to walk repo path", e);
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
                                        parsedFile.setId(UUID.randomUUID());
                                        parsedFile.setRepoName(repoName);
                                        parsedFile.setFilePath(relativePath);
                                        parsedFile.setContentHash(contentHash);
                                        return saveFileData(parsedFile); // ↓ will propagate UUID
                                    })
                    )
                    .flatMap(file -> {
                        log.info("FileData flow continued with file ID = {}", file.getId());
                        String contentString = new String(fileBytes);

                        String extractedPackage = extractPackageName(contentString);
                        log.info("📦 Extracted package: {}", extractedPackage);

                        return getOrCreatePackage(extractedPackage, repo, branch)
                                .flatMap(pkg -> {
                                    BranchFileAssociation assoc = new BranchFileAssociation();
                                    assoc.setId(UUID.randomUUID());
                                    assoc.setRepoName(repoName);
                                    assoc.setBranchId(branch.getId());
                                    assoc.setFileId(file.getId()); // UUID propagated
                                    assoc.setPackageDataId(pkg.getId());
                                    assoc.setFilePath(relativePath);
                                    log.info("Saving association: branch={}, fileId={}, filePath={}", branch.getId(), file.getId(), relativePath);
                                    return branchFileAssociationRepository.save(assoc)
                                            .doOnNext(saved -> log.info("✅ BranchFileAssociation saved: id={}, branchId={}, fileId={}", saved.getId(), saved.getBranchId(), saved.getFileId()))
                                            .then();
                                });
                    });

        } catch (IOException e) {
            return Mono.error(new RuntimeException("Failed to read file: " + path, e));
        }
    }




    private Mono<FileData> saveFileData(FileData fileData) {
        fileData.setId(UUID.randomUUID());

        List<ClassData> classes = fileData.getClasses();

        return fileRepository.save(fileData)
                .doOnNext(saved -> log.info("✅ Saved FileData: id={}, path={}", saved.getId(), saved.getFilePath()))
                .flatMap(savedFile -> {
                    for (ClassData clazz : classes) {
                        clazz.setId(UUID.randomUUID());
                        clazz.setFileDataId(savedFile.getId());
                    }

                    return Flux.fromIterable(classes)
                            .flatMap(this::saveClassData)
                            .then(Mono.just(savedFile));
                });
    }



    private Mono<ClassData> saveClassData(ClassData clazz) {
        List<FieldData> fields = clazz.getFields() != null ? clazz.getFields() : Collections.emptyList();
        List<MethodData> methods = clazz.getMethods() != null ? clazz.getMethods() : Collections.emptyList();
        List<AnnotationData> annotations = clazz.getAnnotations() != null ? clazz.getAnnotations() : Collections.emptyList();

        for (FieldData field : fields) {
            field.setId(UUID.randomUUID());
            field.setClassDataId(clazz.getId());
        }
        for (MethodData method : methods) {
            method.setId(UUID.randomUUID());
            method.setClassDataId(clazz.getId());
        }

        return classRepository.save(clazz)
                .doOnNext(c -> log.info("✅ Saved ClassData: id={}, name={}", c.getId(), c.getName()))
                .flatMap(savedClass -> {
                    // Mono<Void> saveClassAnnotations = saveAnnotations(annotations, savedClass.getId(), null, null);
                    Mono<Void> saveFieldsMono = saveFieldData(fields);
                    Mono<Void> saveMethodsMono = saveMethodData(methods);

                    // Skip annotation save for testing
                    return Mono.when(/*saveClassAnnotations,*/ saveFieldsMono, saveMethodsMono)
                            .thenReturn(savedClass);
                });
    }

    private Mono<Void> saveFieldData(List<FieldData> fields) {
        if (fields.isEmpty()) {
            return Mono.empty();
        }

        return Flux.fromIterable(fields)
                .flatMap(field ->
                        fieldRepository.save(field)
                                .doOnNext(saved -> log.info("✅ Saved FieldData: id={}, name={}", saved.getId(), saved.getName()))
                                // .flatMap(savedField -> saveAnnotations(savedField.getAnnotations(), null, savedField.getId(), null))
                                .then() // Skip annotation save for testing
                ).then();
    }

    private Mono<Void> saveMethodData(List<MethodData> methods) {
        if (methods.isEmpty()) {
            return Mono.empty();
        }

        return Flux.fromIterable(methods)
                .flatMap(method ->
                        methodRepository.save(method)
                                .doOnNext(saved -> log.info("✅ Saved MethodData: id={}, name={}", saved.getId(), saved.getName()))
                                // .flatMap(savedMethod -> saveAnnotations(savedMethod.getAnnotations(), null, null, savedMethod.getId()))
                                .then() // Skip annotation save for testing
                ).then();
    }

    private Mono<Void> saveAnnotations(List<AnnotationData> annotations,
                                       UUID classId,
                                       UUID fieldId,
                                       UUID methodId) {
        if (annotations == null || annotations.isEmpty()) {
            return Mono.empty();
        }

        return Flux.fromIterable(annotations)
                .flatMap(annotation -> {
                    if (annotation.getId() == null) {
                        annotation.setId(UUID.randomUUID());
                    }
                    annotation.setClassDataId(classId);
                    annotation.setFieldDataId(fieldId);
                    annotation.setMethodDataId(methodId);
                    return annotationDataRepository.save(annotation)
                            .doOnNext(saved -> log.info("✅ Saved Annotation: for class={}, field={}, method={}", classId, fieldId, methodId));
                })
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
                .doOnNext(found -> log.info("🔁 Found existing package: {}", found.getPackageName()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("📦 Creating new package: {}", effectiveName);

                    String parentName = extractParentPackage(effectiveName);
                    Mono<PackageData> parentMono = (parentName != null)
                            ? getOrCreatePackage(parentName, repo, branch)
                            : Mono.justOrEmpty((PackageData) null);

                    return parentMono.flatMap(parentPkg -> {
                        PackageData newPkg = new PackageData();
                        newPkg.setId(UUID.randomUUID());
                        newPkg.setPackageName(effectiveName);
                        newPkg.setRepoName(repo.getRepoName());
                        newPkg.setBranchId(branch.getId());
                        if (parentPkg != null) {
                            newPkg.setParentPackageId(parentPkg.getId());
                        }
                        return packageRepository.save(newPkg)
                                .doOnNext(saved -> log.info("✅ Saved package: id={}, name={}", saved.getId(), saved.getPackageName()));
                    });
                }));
    }



    private String extractParentPackage(String packageName) {
        if (packageName == null || packageName.isBlank() || packageName.equals("default")) return null;
        int lastDot = packageName.lastIndexOf('.');
        return (lastDot > 0) ? packageName.substring(0, lastDot) : null;
    }


}
