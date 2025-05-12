package com.juv3nil3.icdg.service;

import com.juv3nil3.icdg.domain.BranchMetadata;
import com.juv3nil3.icdg.domain.Documentation;
import com.juv3nil3.icdg.domain.PackageData;
import com.juv3nil3.icdg.domain.RepositoryMetadata;
import com.juv3nil3.icdg.repository.DocumentationRepository;
import com.juv3nil3.icdg.repository.PackageDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentationBuilderService {

    private final DocumentationRepository documentationRepository;
    private final PackageDataRepository packageDataRepository;

    public DocumentationBuilderService(DocumentationRepository documentationRepository, PackageDataRepository packageDataRepository) {
        this.documentationRepository = documentationRepository;
        this.packageDataRepository = packageDataRepository;
    }

    public Mono<Documentation> buildDocumentation(RepositoryMetadata repo, BranchMetadata branchMetadata) {
        return packageDataRepository
                .findByBranchIdAndParentPackageIsNull(branchMetadata.getId())
                .collectList()
                .flatMap(topLevelPackages -> {
                    Documentation documentation = new Documentation();
                    documentation.setBranchMetadata(branchMetadata);
                    documentation.setCreatedAt(LocalDateTime.now());
                    documentation.setUpdatedAt(LocalDateTime.now());
                    documentation.setPackages(topLevelPackages);

                    return documentationRepository.save(documentation);
                });
    }
}
