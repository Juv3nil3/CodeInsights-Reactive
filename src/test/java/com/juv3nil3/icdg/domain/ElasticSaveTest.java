package com.juv3nil3.icdg.domain;

import com.juv3nil3.icdg.config.TestSecurityConfiguration;
import com.juv3nil3.icdg.domain.elasticsearch.ClassDataDocument;
import com.juv3nil3.icdg.domain.elasticsearch.DocumentationDocument;
import com.juv3nil3.icdg.domain.elasticsearch.FileDataDocument;
import com.juv3nil3.icdg.domain.elasticsearch.PackageDataDocument;
import com.juv3nil3.icdg.repository.DocumentationRepository;
import com.juv3nil3.icdg.repository.elasticsearch.DocumentationSearchRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("testdev")
@Import(TestSecurityConfiguration.class)
public class ElasticSaveTest {

    @Autowired
    private DocumentationRepository docRepo; // Reactive repository

    @Autowired
    private DocumentationSearchRepo elasticRepo; // Spring Data Elasticsearch (likely not reactive)

    @Test
    void testSaveToElasticsearch() {
        // Fetch a documentation record from the DB (reactive)
        Documentation doc = docRepo.findAll().blockFirst(); // blocking only for test

        assertThat(doc).isNotNull();

        // Map to Elasticsearch Document model
        DocumentationDocument docDoc = fromEntity(doc);

        // Save to Elasticsearch (blocking for test)
        elasticRepo.save(docDoc);

        // Verify the document was saved
        Optional<DocumentationDocument> fromElastic = elasticRepo.findById(docDoc.getId());
        assertThat(fromElastic).isPresent();
    }

    // Manual mapper remains unchanged
    public static DocumentationDocument fromEntity(Documentation doc) {
        List<PackageDataDocument> packageDocs = doc.getPackages().stream().map(pkg -> {
            PackageDataDocument pkgDoc = new PackageDataDocument();
            pkgDoc.setPackageName(pkg.getPackageName());

            List<FileDataDocument> fileDocs = pkg.getFiles().stream().map(file -> {
                FileDataDocument fileDoc = new FileDataDocument();
                fileDoc.setFilePath(file.getFilePath());

                List<ClassDataDocument> classDocs = file.getClasses().stream().map(cls -> {
                    ClassDataDocument clsDoc = new ClassDataDocument();
                    clsDoc.setClassName(cls.getName());
                    clsDoc.setComment(cls.getComment());
                    return clsDoc;
                }).collect(Collectors.toList());

                fileDoc.setClasses(classDocs);
                return fileDoc;
            }).collect(Collectors.toList());

            pkgDoc.setFiles(fileDocs);
            return pkgDoc;
        }).collect(Collectors.toList());

        DocumentationDocument docDoc = new DocumentationDocument();
        docDoc.setId(doc.getId().toString());
        docDoc.setExportPath(doc.getExportPath());
        docDoc.setCreatedAt(doc.getCreatedAt());
        docDoc.setUpdatedAt(doc.getUpdatedAt());
        docDoc.setBranchMetadataId(doc.getBranchMetadata().getId());
        docDoc.setPackages(packageDocs);

        return docDoc;
    }
}

