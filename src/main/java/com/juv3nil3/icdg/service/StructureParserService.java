package com.juv3nil3.icdg.service;


import com.juv3nil3.icdg.domain.*;
import com.juv3nil3.icdg.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
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
public class StructureParserService {

    private final RepositoryMetadataRepo repositoryMetadataRepository;
    private final BranchDataRepository branchMetadataRepository;
    private final PackageDataRepository packageRepository;
    private final FileDataRepository fileRepository;
    private final ClassDataRepository classRepository;
    private final FieldDataRepository fieldRepository;
    private final MethodDataRepository methodRepository;
    private final JavaFileParser javaFileParserService;

    private static final Logger log = LoggerFactory.getLogger(StructureParserService.class);

    public Mono<Void> parseRepositoryStructure(Path repoPath, String owner, String repoName, String branchName) {
        RepositoryMetadata repo = new RepositoryMetadata(owner, repoName, LocalDateTime.now(), LocalDateTime.now());

        return repositoryMetadataRepository.save(repo)
                .flatMap(savedRepo -> {
                    BranchMetadata branch = new BranchMetadata(branchName, savedRepo, LocalDateTime.now(), LocalDateTime.now());
                    return branchMetadataRepository.save(branch)
                            .flatMap(savedBranch -> Flux.fromStream(Files.walk(repoPath))
                                    .filter(path -> path.toString().endsWith(".java"))
                                    .flatMap(path -> parseAndSaveFile(path, repoPath, repoName, savedRepo, savedBranch))
                                    .then());
                });
    }

    private Mono<Void> parseAndSaveFile(Path path, Path repoRoot, String repoName, RepositoryMetadata repo, BranchMetadata branch) {
        try {
            String content = Files.readString(path);
            String relativePath = repoRoot.relativize(path).toString();
            String contentHash = DigestUtils.sha256Hex(content); // Apache commons or custom hashing

            // Check for duplicate file content
            return fileRepository.findByRepoNameAndFilePathAndSha256(repoName, relativePath, contentHash)
                    .flatMap(existingFile -> {
                        // Even if reused, associate it with correct PackageData in this branch context
                        return getOrCreatePackage(repo, extractPackageName(content))
                                .flatMap(pkg -> {
                                    existingFile.setPackageData(pkg);
                                    return fileRepository.save(existingFile)
                                            .then();
                                });
                    })
                    .switchIfEmpty(
                            javaFileParserService.parseJavaFile(content)
                                    .flatMap(parsedFile -> {
                                        parsedFile.setRepoName(repoName);
                                        parsedFile.setFilePath(relativePath);
                                        parsedFile.setSha256(contentHash); // You must add this field in FileData

                                        return getOrCreatePackage(repo, extractPackageName(content))
                                                .flatMap(pkg -> {
                                                    parsedFile.setPackageData(pkg);
                                                    return saveFileData(parsedFile).then();
                                                });
                                    })
                    ).then();

        } catch (IOException e) {
            return Mono.error(new RuntimeException("Failed to read file: " + path, e));
        }
    }

    private Mono<FileData> saveFileData(FileData fileData) {
        log.info("Saving FileData: {}", fileData.getFilePath());

        return fileRepository.save(fileData)
                .doOnNext(savedFile -> log.info("FileData saved: {}", savedFile.getId()))
                .flatMap(savedFile ->
                        Flux.fromIterable(fileData.getClasses())
                                .flatMap(clazz -> saveClassData(clazz, savedFile))
                                .then(Mono.just(savedFile))
                );
    }

    private Mono<ClassData> saveClassData(ClassData clazz, FileData fileData) {
        clazz.setFileData(fileData);
        log.info("Saving ClassData: {}", clazz.getName());

        return classRepository.save(clazz)
                .doOnNext(savedClass -> {
                    if (savedClass.getId() == null) {
                        log.error("ClassData ID is null for class: {}", clazz.getName());
                    }
                })
                .flatMap(savedClass ->
                        Mono.when(
                                saveFieldData(clazz.getFields(), savedClass),
                                saveMethodData(clazz.getMethods(), savedClass)
                        ).thenReturn(savedClass)
                );
    }

    private Mono<Void> saveFieldData(List<FieldData> fields, ClassData classData) {
        return Flux.fromIterable(fields)
                .doOnNext(field -> field.setClassData(classData))
                .flatMap(field -> {
                    log.debug("Saving FieldData: {} for ClassData ID: {}", field.getName(), classData.getId());
                    return fieldRepository.save(field);
                })
                .then();
    }

    private Mono<Void> saveMethodData(List<MethodData> methods, ClassData classData) {
        return Flux.fromIterable(methods)
                .doOnNext(method -> method.setClassData(classData))
                .flatMap(method -> {
                    log.debug("Saving MethodData: {} for ClassData ID: {}", method.getName(), classData.getId());
                    return methodRepository.save(method);
                })
                .then();
    }

    private String extractPackageName(String content) {
        if (content == null || content.isBlank()) {
            return "default";
        }

        // Matches lines like: package com.example.project;
        Pattern packagePattern = Pattern.compile("^\\s*package\\s+([a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*)\\s*;\\s*$", Pattern.MULTILINE);
        Matcher matcher = packagePattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1).trim(); // Extract and trim the package name
        }

        return "default"; // Fallback if no package found
    }


    private Mono<PackageData> getOrCreatePackage(String packageName, RepositoryMetadata repo, BranchMetadata branch) {
        String effectivePackageName = (packageName == null || packageName.isBlank()) ? "default" : packageName;

        return packageRepository.findByBranchAndPackageName(branch, effectivePackageName)
                .switchIfEmpty(Mono.defer(() -> {
                    String parentName = extractParentPackage(effectivePackageName);

                    Mono<PackageData> parentMono = (parentName != null)
                            ? getOrCreatePackage(parentName, repo, branch)
                            : Mono.empty();

                    return parentMono
                            .defaultIfEmpty(null)
                            .flatMap(parentPackage -> {
                                PackageData newPackage = new PackageData();
                                newPackage.setPackageName(effectivePackageName);
                                newPackage.setRepoName(repo.getRepoName());
                                newPackage.setRepository(repo);
                                newPackage.setBranch(branch);
                                newPackage.setParentPackage(parentPackage);
                                return packageRepository.save(newPackage);
                            });
                }));
    }
    private String extractParentPackage(String packageName) {
        if (packageName == null || packageName.isBlank()) return null;

        int lastDot = packageName.lastIndexOf('.');
        if (lastDot > 0) {
            return packageName.substring(0, lastDot);
        }
        return null;
    }

}
