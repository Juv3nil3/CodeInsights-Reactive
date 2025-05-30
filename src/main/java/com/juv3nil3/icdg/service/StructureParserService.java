package com.juv3nil3.icdg.service;


import com.juv3nil3.icdg.domain.*;
import com.juv3nil3.icdg.repository.*;
import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import jakarta.persistence.NonUniqueResultException;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
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
    private static final String ROOT_PACKAGE = "com.example.MailAuditPro";

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

        return repositoryMetadataRepository
                .insertIfNotExistsRepositoryMetadata(owner, repoName, now, now)
                .then(repositoryMetadataRepository.findByOwnerAndRepoName(owner, repoName))
                .doOnNext(repo -> log.info("✅ Loaded RepositoryMetadata: id={}, name={}", repo.getId(), repo.getRepoName()));
    }






    public Mono<BranchMetadata> createOrUpdateBranchMetadata(RepositoryMetadata repo, String branchName, String latestCommitSha) {
        LocalDateTime now = LocalDateTime.now();

        return branchMetadataRepository
                .insertIfNotExistsBranchMetadata(branchName, latestCommitSha, repo.getId(), now, now)
                .then(branchMetadataRepository.findByBranchNameAndRepositoryMetadataId(branchName, repo.getId()))
                .doOnNext(branch -> log.info("📌 Using BranchMetadata: id={}, name={}", branch.getId(), branch.getBranchName()));
    }






    private Mono<Void> parseAndWalkJavaFiles(Path repoPath, RepositoryMetadata repo, BranchMetadata branch) {
        try {
            log.info("Walking files for repo {} branch {}", repo.getRepoName(), branch.getBranchName());
            return Flux.fromStream(Files.walk(repoPath))
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> {
                        Path relative = repoPath.relativize(path);
                        return relative.toString().contains("src/main/");
                    })
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
                                        parsedFile.setFileName(relativePath);
                                        parsedFile.setRepoName(repoName);
                                        parsedFile.setFilePath(relativePath);
                                        parsedFile.setContentHash(contentHash);
                                        parsedFile.setImportsJson(parsedFile.toJson(parsedFile.getImports()));
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

        List<ClassData> classes = fileData.getClasses();

        return fileRepository.save(fileData)
                .doOnNext(saved -> log.info("✅ Saved FileData: id={}, path={}", saved.getId(), saved.getFilePath()))
                .flatMap(savedFile -> {
                    for (ClassData clazz : classes) {
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

        return classRepository.save(clazz)
                .doOnNext(c -> log.info("✅ Saved ClassData: id={}, name={}", c.getId(), c.getName()))
                .flatMap(savedClass -> {
                    // 🔁 Assign class ID after it is generated
                    for (FieldData field : fields) {
                        field.setClassDataId(savedClass.getId());
                    }
                    for (MethodData method : methods) {
                        method.setClassDataId(savedClass.getId());
                    }

                    // Mono<Void> saveClassAnnotations = saveAnnotations(annotations, savedClass.getId(), null, null);
                    Mono<Void> saveFieldsMono = saveFieldData(fields);
                    Mono<Void> saveMethodsMono = saveMethodData(methods);

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


    private static final int ROOT_PACKAGE_DEPTH = 3; // e.g. com.example.MailAuditPro

    private static final String ROOT_PARENT_NAME = "ROOT";
    //private static final UUID ROOT_PARENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");


    private Mono<PackageData> getOrCreatePackage(String fullPackageName, RepositoryMetadata repo, BranchMetadata branch) {
        String[] segments = fullPackageName.split("\\.");

        if (segments.length < ROOT_PACKAGE_DEPTH) {
            log.warn("Package {} has fewer segments than expected root depth: {}", fullPackageName, ROOT_PACKAGE_DEPTH);
            return Mono.empty();
        }

        String rootPackageName = String.join(".", Arrays.copyOfRange(segments, 0, ROOT_PACKAGE_DEPTH));
        String[] subPackages = Arrays.copyOfRange(segments, ROOT_PACKAGE_DEPTH, segments.length);

        return ensureRootParentPackage(branch, repo.getRepoName())
                .flatMap(rootParent -> ensureRootPackage(rootPackageName, branch, repo.getRepoName(), rootParent)
                        .flatMap(rootPackage -> {
                            Mono<PackageData> current = Mono.just(rootPackage);
                            StringBuilder fullNameBuilder = new StringBuilder(rootPackageName);

                            for (String part : subPackages) {
                                fullNameBuilder.append(".").append(part);
                                String currentName = fullNameBuilder.toString();

                                current = current.flatMap(parent -> {
                                    UUID parentId = parent.getId();
                                    UUID branchId = branch.getId();

                                    return packageRepository.findByPackageNameAndParentPackageIdAndBranchId(currentName, parentId, branchId)
                                            .switchIfEmpty(
                                                    Mono.defer(() -> {
                                                        UUID newId = UUID.randomUUID();
                                                        return packageRepository.insertIfNotExists(
                                                                        currentName, repo.getRepoName(), parentId, branchId)
                                                                .then(packageRepository.findByPackageNameAndParentPackageIdAndBranchId(currentName, parentId, branchId));
                                                    })
                                            )
                                            .doOnNext(pkg -> log.info("📦 Ensured child package: {}", currentName));
                                });
                            }

                            return current;
                        }));
    }



    private Mono<PackageData> ensureRootParentPackage(BranchMetadata branch, String repoName) {
        UUID rootParentId = UUID.nameUUIDFromBytes((repoName + "-root").getBytes(StandardCharsets.UTF_8));
        String rootParentName = ROOT_PARENT_NAME + "-" + repoName;
        return packageRepository.insertRootPackage(
                        rootParentId,
                        ROOT_PARENT_NAME,
                        repoName,
                        branch.getId()
                )
                .onErrorResume(e -> {
                    // ignore duplicate key error if any slips through (just log)
                    if (e instanceof DuplicateKeyException) {
                        log.info("Root package already exists: {}", rootParentId);
                        return Mono.empty();
                    }
                    return Mono.error(e);
                })
                .then(packageRepository.findById(rootParentId))
                .doOnNext(pkg -> log.info("📦 Ensured ROOT package: {}", pkg.getId()));
    }





    private Mono<PackageData> ensureRootPackage(String rootPackageName, BranchMetadata branch, String repoName, PackageData rootParent) {
        UUID parentId = rootParent.getId();
        UUID branchId = branch.getId();

        return packageRepository.findByPackageNameAndParentPackageIdAndBranchId(rootPackageName, parentId, branchId)
                .switchIfEmpty(
                        Mono.defer(() -> {
                            UUID newId = UUID.randomUUID();
                            return packageRepository.insertIfNotExists(rootPackageName, repoName, parentId, branchId)
                                    .then(packageRepository.findByPackageNameAndParentPackageIdAndBranchId(rootPackageName, parentId, branchId));
                        })
                )
                .doOnNext(pkg -> log.info("📦 Ensured root package: {}", rootPackageName));
    }


    private String extractParentPackage(String packageName) {
        if (packageName == null || packageName.isBlank() || packageName.equals("default")) return null;
        int lastDot = packageName.lastIndexOf('.');
        return (lastDot > 0) ? packageName.substring(0, lastDot) : null;
    }


}
