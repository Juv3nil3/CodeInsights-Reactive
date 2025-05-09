package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.FileData;
import com.juv3nil3.icdg.domain.PackageData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface FileDataRepository extends R2dbcRepository<FileData,Long> {

    @Query("SELECT f FROM FileData f LEFT JOIN FETCH f.classes c WHERE f.packageData IN :packages")
    List<FileData> findFilesWithClasses(@Param("packages") List<PackageData> packages);

    Mono<FileData> findByRepoNameAndContentHash(String repoName, String contentHash);
}
