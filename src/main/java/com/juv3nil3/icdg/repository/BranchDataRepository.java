package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.BranchMetadata;
import com.juv3nil3.icdg.domain.RepositoryMetadata;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BranchDataRepository extends R2dbcRepository<BranchMetadata,Long> {


    Flux<BranchMetadata> findAllByRepositoryMetadataId(Long repositoryMetadataId);
    Mono<BranchMetadata> findByBranchNameAndRepositoryMetadataId(String branchName, Long repositoryMetadataId);

}
