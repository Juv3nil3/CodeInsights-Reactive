package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.BranchFileAssociation;
import com.juv3nil3.icdg.domain.BranchMetadata;
import com.juv3nil3.icdg.domain.FileData;
import com.juv3nil3.icdg.domain.PackageData;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BranchFileAssociationRepository extends R2dbcRepository<BranchFileAssociation,Long> {

    Mono<BranchFileAssociation> findByBranchIdAndFileId(Long branchId, Long fileId);

    Flux<BranchFileAssociation> findAllByBranchAndPackageData(BranchMetadata branch, PackageData packageData);

    Flux<BranchFileAssociation> findAllByBranchId(Long branchId);
    Flux<BranchFileAssociation> findAllByFileId(Long fileId);
    Flux<BranchFileAssociation> findAllByPackageDataId(Long packageDataId);


}
