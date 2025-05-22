package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.BranchMetadata;
import com.juv3nil3.icdg.domain.PackageData;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
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
}
