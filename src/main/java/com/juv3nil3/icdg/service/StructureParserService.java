package com.juv3nil3.icdg.service;


import com.juv3nil3.icdg.domain.BranchMetadata;
import com.juv3nil3.icdg.domain.RepositoryMetadata;
import com.juv3nil3.icdg.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

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
    private final JavaFileParserService javaFileParserService;

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

            return javaFileParserService.parseJavaFile(content)
                    .flatMap(fileData -> {
                        fileData.setRepoName(repoName);
                        fileData.setFilePath(relativePath);

                        // Set package data
                        String packageName = extractPackageName(content);
                        return getOrCreatePackage(repo, packageName)
                                .flatMap(pkg -> {
                                    fileData.setPackageData(pkg);
                                    return fileRepository.save(fileData)
                                            .flatMap(savedFile -> saveClassTree(savedFile, fileData.getClasses()));
                                });
                    });

        } catch (IOException e) {
            return Mono.error(new RuntimeException("Failed to read file: " + path, e));
        }
    }
}
