package com.juv3nil3.icdg.domain;

import com.juv3nil3.icdg.repository.BranchDataRepository;
import com.juv3nil3.icdg.repository.DocumentationRepository;
import com.juv3nil3.icdg.repository.RepositoryMetadataRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
public class DomainPersistenceTest {

    @Autowired
    private RepositoryMetadataRepo repoRepo;
    @Autowired private BranchDataRepository branchRepo;
    @Autowired private DocumentationRepository docRepo;

    @Test
    void testDomainEntityPersistence() {
        RepositoryMetadata repo = new RepositoryMetadata("juv3nil3", "icdg", , "main");
        repoRepo.save(repo);

        BranchMetadata branch = new BranchMetadata();
        branch.setBranchName("main");
        branch.setLatestCommitHash("abcd123");
        branch.setRepositoryMetadata(repo);
        branchRepo.save(branch);

        Documentation doc = new Documentation();
        doc.setExportPath("/docs/main");
        doc.setBranchMetadata(branch);
        doc.setCreatedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());
        docRepo.save(doc);

        assertThat(doc.getId()).isNotNull();
    }
}

