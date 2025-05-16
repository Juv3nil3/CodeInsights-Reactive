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

@Repository
public interface BranchDataRepository extends R2dbcRepository<BranchMetadata,Long> {

    @Query("""
    MERGE INTO branch_metadata (branch_name, latest_commit_hash, repository_metadata_id, created_at, updated_at)
    KEY (branch_name, repository_metadata_id)
    VALUES (:branchName, :latestCommitHash, :repositoryMetadataId, :createdAt, :updatedAt)
""")
    Mono<Void> mergeBranchMetadata(
            @Param("branchName") String branchName,
            @Param("latestCommitHash") String latestCommitHash,
            @Param("repositoryMetadataId") Long repositoryMetadataId,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );



    Flux<BranchMetadata> findAllByRepositoryMetadataId(Long repositoryMetadataId);
    Mono<BranchMetadata> findByBranchNameAndRepositoryMetadataId(String branchName, Long repositoryMetadataId);

}
