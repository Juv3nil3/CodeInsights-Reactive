package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.RepositoryMetadata;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface RepositoryMetadataRepo extends R2dbcRepository<RepositoryMetadata,Long> {

    @Query("""
        MERGE INTO repository_metadata (owner, repo_name, created_at, updated_at)
        KEY (owner, repo_name)
        VALUES (:owner, :repoName, :createdAt, :updatedAt)
    """)
    Mono<Void> mergeMetadata(String owner, String repoName, LocalDateTime createdAt, LocalDateTime updatedAt);

    Mono<RepositoryMetadata> findByOwnerAndRepoName(String owner, String repoName);
}
