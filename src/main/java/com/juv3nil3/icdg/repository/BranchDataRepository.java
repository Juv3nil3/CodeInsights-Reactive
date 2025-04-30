package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.BranchMetadata;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchDataRepository extends R2dbcRepository<BranchMetadata,Long> {
}
