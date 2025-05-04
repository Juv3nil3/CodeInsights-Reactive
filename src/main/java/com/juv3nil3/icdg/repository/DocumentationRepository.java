package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.BranchMetadata;
import com.juv3nil3.icdg.domain.Documentation;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface DocumentationRepository extends R2dbcRepository<Documentation, Long> {

    Optional<Documentation> findByBranchMetadata(BranchMetadata branchMetadata);

    @Query("SELECT d FROM Documentation d LEFT JOIN FETCH d.packages p WHERE d.branchMetadata = :branchMetadata")
    Optional<Documentation> findDocumentationWithPackages(@Param("branchMetadata") BranchMetadata metadata);
}
