package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.BranchMetadata;
import com.juv3nil3.icdg.domain.RepositoryMetadata;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface BranchDataRepository extends R2dbcRepository<BranchMetadata, UUID> {

    @Query("""
    MERGE INTO branch_metadata (id, branch_name, latest_commit_hash, repository_metadata_id, created_at, updated_at)
    KEY (branch_name, repository_metadata_id)
    VALUES (:id, :branchName, :latestCommitHash, :repositoryMetadataId, :createdAt, :updatedAt)
""")
    Mono<Void> mergeBranchMetadata(
            @Param("id") UUID id,
            @Param("branchName") String branchName,
            @Param("latestCommitHash") String latestCommitHash,
            @Param("repositoryMetadataId") UUID repositoryMetadataId,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );




    Flux<BranchMetadata> findAllByRepositoryMetadataId(UUID repositoryMetadataId);
    Mono<BranchMetadata> findByBranchNameAndRepositoryMetadataId(String branchName, UUID repositoryMetadataId);

}
