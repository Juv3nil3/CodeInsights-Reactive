package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.BranchMetadata;
import com.juv3nil3.icdg.domain.RepositoryMetadata;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BranchDataRepository extends R2dbcRepository<BranchMetadata,Long> {

    Mono<BranchMetadata> findByRepositoryMetadataAndBranchName(RepositoryMetadata repositoryMetadata, String branchName);
}
