package com.juv3nil3.icdg.service;


import com.juv3nil3.icdg.domain.*;
import com.juv3nil3.icdg.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
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

    private static final Logger log = LoggerFactory.getLogger(StructureParserService.class);

    public Mono<Void> parseRepositoryStructure(Path repoPath, String owner, String repoName, String branchName) {
        return createOrUpdateRepositoryMetadata(owner, repoName)
                .flatMap(repo ->
                        createOrUpdateBranchMetadata(repo, branchName)
                                .flatMap(branch ->
                                        Flux.fromStream(Files.walk(repoPath))
                                                .filter(path -> path.toString().endsWith(".java"))
                                                .flatMap(path -> parseAndSaveFile(path, repoPath, repo.getRepoName(), repo, branch))
                                                .then()
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

    public Mono<BranchMetadata> createOrUpdateBranchMetadata(RepositoryMetadata repo, String branchName) {
        return branchMetadataRepository.findByRepositoryMetadataAndBranchName(repo, branchName)
                .switchIfEmpty(Mono.defer(() -> {
                    BranchMetadata newBranch = new BranchMetadata();
                    newBranch.setRepositoryMetadata(repo);
                    newBranch.setBranchName(branchName);
                    newBranch.setCreatedAt(LocalDateTime.now());
                    newBranch.setUpdatedAt(LocalDateTime.now());
                    return branchMetadataRepository.save(newBranch);
                }))
                .flatMap(existing -> {
                    existing.setUpdatedAt(LocalDateTime.now());
                    return branchMetadataRepository.save(existing);
                });
    }

    private Mono<Void> parseAndSaveFile(Path path, Path repoRoot, String repoName, RepositoryMetadata repo, BranchMetadata branch) {
        try {
            String content = Files.readString(path);
            String relativePath = repoRoot.relativize(path).toString();
            String contentHash = DigestUtils.sha256Hex(content);

            return fileRepository.findByRepoNameAndFilePathAndSha256(repoName, relativePath, contentHash)
                    .switchIfEmpty(
                            javaFileParserService.parseJavaFile(content)
                                    .flatMap(parsedFile -> {
                                        parsedFile.setRepoName(repoName);
                                        parsedFile.setFilePath(relativePath);
                                        parsedFile.setSha256(contentHash);
                                        return saveFileData(parsedFile);
                                    })
                    )
                    .flatMap(file -> getOrCreatePackage(extractPackageName(content), repo, branch)
                            .flatMap(pkg -> branchFileAssociationRepository.findByBranchAndFile(branch, file)
                                    .switchIfEmpty(Mono.defer(() -> {
                                        BranchFileAssociation assoc = new BranchFileAssociation();
                                        assoc.setBranch(branch);
                                        assoc.setFile(file);
                                        assoc.setPackageData(pkg);
                                        return branchFileAssociationRepository.save(assoc);
                                    }))
                                    .then()
                            )
                    );

        } catch (IOException e) {
            return Mono.error(new RuntimeException("Failed to read file: " + path, e));
        }
    }

    private Mono<FileData> saveFileData(FileData fileData) {
        log.info("Saving FileData: {}", fileData.getFilePath());

        return fileRepository.save(fileData)
                .doOnNext(savedFile -> log.info("FileData saved: {}", savedFile.getId()))
                .flatMap(savedFile -> Flux.fromIterable(fileData.getClasses())
                        .flatMap(clazz -> saveClassData(clazz, savedFile))
                        .then(Mono.just(savedFile))
                );
    }

    private Mono<ClassData> saveClassData(ClassData clazz, FileData fileData) {
        clazz.setFileData(fileData);
        log.info("Saving ClassData: {}", clazz.getName());

        return classRepository.save(clazz)
                .flatMap(savedClass -> Mono.when(
                                saveFieldData(clazz.getFields(), savedClass),
                                saveMethodData(clazz.getMethods(), savedClass)
                        ).thenReturn(savedClass)
                );
    }

    private Mono<Void> saveFieldData(List<FieldData> fields, ClassData classData) {
        return Flux.fromIterable(fields)
                .doOnNext(field -> field.setClassData(classData))
                .flatMap(fieldRepository::save)
                .then();
    }

    private Mono<Void> saveMethodData(List<MethodData> methods, ClassData classData) {
        return Flux.fromIterable(methods)
                .doOnNext(method -> method.setClassData(classData))
                .flatMap(methodRepository::save)
                .then();
    }

    private String extractPackageName(String content) {
        if (content == null || content.isBlank()) return "default";
        Matcher matcher = Pattern.compile("^\\s*package\\s+([a-zA-Z0-9_.]+)\\s*;", Pattern.MULTILINE).matcher(content);
        return matcher.find() ? matcher.group(1).trim() : "default";
    }

    private Mono<PackageData> getOrCreatePackage(String packageName, RepositoryMetadata repo, BranchMetadata branch) {
        String effectiveName = (packageName == null || packageName.isBlank()) ? "default" : packageName;

        return packageRepository.findByBranchAndPackageName(branch, effectiveName)
                .switchIfEmpty(Mono.defer(() -> {
                    String parentName = extractParentPackage(effectiveName);

                    Mono<PackageData> parentMono = (parentName != null)
                            ? getOrCreatePackage(parentName, repo, branch)
                            : Mono.empty();

                    return parentMono.defaultIfEmpty(null).flatMap(parentPkg -> {
                        PackageData newPkg = new PackageData();
                        newPkg.setPackageName(effectiveName);
                        newPkg.setRepoName(repo.getRepoName());
                        newPkg.setBranch(branch);
                        newPkg.setRepository(repo);
                        newPkg.setParentPackage(parentPkg);
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
