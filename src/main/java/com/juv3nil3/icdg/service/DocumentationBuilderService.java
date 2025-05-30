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
import java.util.UUID;

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
        String repoName = repo.getRepoName() != null ? repo.getRepoName() : "";
        String branchName = branchMetadata.getBranchName() != null ? branchMetadata.getBranchName() : "";
        documentation.setDocumentationName(repoName + branchName);
        documentation.setBranchMetadataId(branchMetadata.getId());
        documentation.setCreatedAt(LocalDateTime.now());
        documentation.setUpdatedAt(LocalDateTime.now());
        documentation.setBranchMetadata(branchMetadata); // transient only

        return packageDataRepository.findAllByBranchId(branchMetadata.getId())
                .collectList()
                .flatMap(packages -> {
                    documentation.setPackages(packages);

                    return Mono.when(
                            Flux.fromIterable(packages)
                                    .flatMap(pkg ->
                                            branchFileAssociationRepository.findAllByPackageDataId(pkg.getId())
                                                    .collectList()
                                                    .doOnNext(pkg::setFileAssociations)
                                    )
                    ).thenReturn(documentation); // correct Mono return
                });
    }


}
