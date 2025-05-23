package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.RepositoryMetadata;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface RepositoryMetadataRepo extends R2dbcRepository<RepositoryMetadata, UUID> {

    @Query("""
    INSERT INTO repository_metadata (owner, repo_name, created_at, updated_at)
    VALUES (:owner, :repoName, :createdAt, :updatedAt)
    ON CONFLICT (owner, repo_name) DO NOTHING
""")
    Mono<Void> insertIfNotExistsRepositoryMetadata(
            @Param("owner") String owner,
            @Param("repoName") String repoName,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );


    Mono<RepositoryMetadata> findByOwnerAndRepoName(String owner, String repoName);
}
