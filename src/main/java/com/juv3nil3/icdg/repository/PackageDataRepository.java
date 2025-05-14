package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.BranchMetadata;
import com.juv3nil3.icdg.domain.PackageData;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface PackageDataRepository extends R2dbcRepository<PackageData, Long> {

    Flux<PackageData> findByBranchIdAndParentPackageIsNull(Long branchId);

    Flux<PackageData> findAllByParentPackageId(Long parentPackageId);
    Flux<PackageData> findAllByBranchId(Long branchId);

    Mono<PackageData> findByBranchIdAndPackageName(Long id, String effectiveName);
}
