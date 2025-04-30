package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.Documentation;
import com.juv3nil3.icdg.domain.RepositoryMetadata;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface DocumentationRepository extends R2dbcRepository<Documentation, Long> {

    Optional<Documentation> findByRepositoryMetadata(@Param("repositoryMetadata") RepositoryMetadata repositoryMetadata);

    @Query("SELECT d FROM Documentation d LEFT JOIN FETCH d.packages p WHERE d.repositoryMetadata = :repoMetadata")
    Optional<Documentation> findDocumentationWithPackages(@Param("repoMetadata") RepositoryMetadata metadata);
}
