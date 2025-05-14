package com.juv3nil3.icdg.service;

import com.juv3nil3.icdg.domain.BranchMetadata;
import com.juv3nil3.icdg.domain.Documentation;
import com.juv3nil3.icdg.domain.PackageData;
import com.juv3nil3.icdg.domain.RepositoryMetadata;
import com.juv3nil3.icdg.repository.BranchFileAssociationRepository;
import com.juv3nil3.icdg.repository.DocumentationRepository;
import com.juv3nil3.icdg.repository.PackageDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentationBuilderService {

    private final DocumentationRepository documentationRepository;
    private final PackageDataRepository packageDataRepository;
    private final BranchFileAssociationRepository branchFileAssociationRepository;

    @Autowired
    public DocumentationBuilderService(DocumentationRepository documentationRepository, PackageDataRepository packageDataRepository, BranchFileAssociationRepository branchFileAssociationRepository) {
        this.documentationRepository = documentationRepository;
        this.packageDataRepository = packageDataRepository;
        this.branchFileAssociationRepository = branchFileAssociationRepository;
    }

    public Mono<Documentation> buildDocumentation(RepositoryMetadata repo, BranchMetadata branchMetadata) {
        Documentation documentation = new Documentation();
        documentation.setBranchMetadataId(branchMetadata.getId());
        documentation.setCreatedAt(LocalDateTime.now());
        documentation.setUpdatedAt(LocalDateTime.now());

        return packageDataRepository.findAllByBranchId(branchMetadata.getId())
                .collectList()
                .flatMap(packages -> {
                    documentation.setPackages(packages);

                    // OPTIONAL: hydrate nested data for downstream uses
                    return Flux.fromIterable(packages)
                            .flatMap(pkg ->
                                    branchFileAssociationRepository.findAllByPackageDataId(pkg.getId())
                                            .collectList()
                                            .doOnNext(pkg::setFileAssociations)
                            )
                            .then(Mono.just(documentation));
                })
                .flatMap(documentationRepository::save);
    }

}
