package com.juv3nil3.icdg.service;

import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch.core.search.ScoreMode;
import com.juv3nil3.icdg.domain.elasticsearch.DocumentationDocument;
import com.juv3nil3.icdg.repository.elasticsearch.DocumentationSearchRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentationSearchService {

    private final DocumentationSearchRepo searchRepository;
    private static Logger log = LoggerFactory.getLogger(DocumentationSearchService.class);

    private final ElasticsearchTemplate elasticsearchTemplate;

    public DocumentationSearchService(DocumentationSearchRepo searchRepository, ElasticsearchTemplate elasticsearchTemplate) {
        this.searchRepository = searchRepository;
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    public List<DocumentationDocument> searchByClassName(String className) {
        log.info("Searching for class name: {}", className);

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .nested(n1 -> n1
                                .path("packages")
                                .query(q1 -> q1
                                        .nested(n2 -> n2
                                                .path("packages.files")
                                                .query(q2 -> q2
                                                        .nested(n3 -> n3
                                                                .path("packages.files.classes")
                                                                .query(q3 -> q3
                                                                        .match(m -> m
                                                                                .field("packages.files.classes.className")
                                                                                .query(className)
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .build();

        SearchHits<DocumentationDocument> searchHits =
                elasticsearchTemplate.search(searchQuery, DocumentationDocument.class);

        return searchHits.stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }

}
