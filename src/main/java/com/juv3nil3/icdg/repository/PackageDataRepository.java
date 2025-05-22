package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.BranchMetadata;
import com.juv3nil3.icdg.domain.PackageData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public interface PackageDataRepository extends R2dbcRepository<PackageData, UUID> {

    Flux<PackageData> findByBranchIdAndParentPackageIsNull(UUID branchId);

    Flux<PackageData> findAllByParentPackageId(UUID parentPackageId);
    Flux<PackageData> findAllByBranchId(UUID branchId);

    Mono<PackageData> findByBranchIdAndPackageName(UUID id, String effectiveName);

    // 1. Find root package (no parent)
    Mono<PackageData> findByPackageNameAndParentPackageIdIsNullAndBranchId(String name, UUID branchMetadataId);

    // 2. Find child package by name, parent ID, and branch ID
    Mono<PackageData> findByPackageNameAndParentPackageIdAndBranchId(String name, UUID parentId, UUID branchMetadataId);


    @Query("""
    INSERT INTO package_data (id, package_name, repo_name, parent_package_id, branch_id)
    VALUES (:id, :packageName, :repoName, :parentPackageId, :branchId)
    ON CONFLICT (package_name, parent_package_id, branch_id) DO NOTHING
""")
    Mono<Void> insertIfNotExists(
            @Param("id") UUID id,
            @Param("packageName") String packageName,
            @Param("repoName") String repoName,
            @Param("parentPackageId") UUID parentPackageId,
            @Param("branchId") UUID branchId
    );
}
