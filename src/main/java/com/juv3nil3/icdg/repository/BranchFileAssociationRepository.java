package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.BranchFileAssociation;
import com.juv3nil3.icdg.domain.BranchMetadata;
import com.juv3nil3.icdg.domain.FileData;
import com.juv3nil3.icdg.domain.PackageData;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface BranchFileAssociationRepository extends R2dbcRepository<BranchFileAssociation, UUID> {

    Mono<BranchFileAssociation> findByBranchIdAndFileId(UUID branchId, UUID fileId);

    Flux<BranchFileAssociation> findAllByBranchAndPackageData(BranchMetadata branch, PackageData packageData);

    Flux<BranchFileAssociation> findAllByBranchId(UUID branchId);
    Flux<BranchFileAssociation> findAllByFileId(UUID fileId);
    Flux<BranchFileAssociation> findAllByPackageDataId(UUID packageDataId);


}
