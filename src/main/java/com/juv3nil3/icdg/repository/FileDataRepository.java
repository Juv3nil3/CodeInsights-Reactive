package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.FileData;
import com.juv3nil3.icdg.domain.PackageData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileDataRepository extends R2dbcRepository<FileData, UUID> {

    Mono<FileData> findByRepoNameAndContentHash(String repoName, String contentHash);

    Flux<FileData> findAllByRepoName(String repoName);
    Mono<FileData> findByFilePathAndRepoName(String filePath, String repoName);
    Mono<FileData> findById(UUID fileId);

}
