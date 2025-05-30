package com.juv3nil3.icdg.repository.elasticsearch;

import com.juv3nil3.icdg.domain.elasticsearch.DocumentationDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentationSearchRepo extends ElasticsearchRepository<DocumentationDocument,String> {

    Iterable<DocumentationDocument> findByBranchMetadataId(Long branchMetadataId);


    List<DocumentationDocument> findByPackagesFilesClassesClassName(String className);

    @Query("""
        {
          "nested": {
            "path": "packages.files.classes",
            "query": {
              "match": {
                "packages.files.classes.className": "?0"
              }
            }
          }
        }
    """)
    List<DocumentationDocument> findByClassName(String className);



}
