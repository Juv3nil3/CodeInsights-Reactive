package com.juv3nil3.icdg.repository.elasticsearch;

import com.juv3nil3.icdg.domain.elasticsearch.DocumentationDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentationSearchRepo extends ElasticsearchRepository<DocumentationDocument,String> {

    Iterable<DocumentationDocument> findByBranchMetadataId(Long branchMetadataId);
}
