package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.BranchMetadata;
import com.juv3nil3.icdg.domain.Documentation;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface DocumentationRepository extends R2dbcRepository<Documentation, UUID> {


    Mono<Documentation> findByBranchMetadataId(UUID branchMetadataId);

    Mono<Documentation> findById(UUID id);

}
