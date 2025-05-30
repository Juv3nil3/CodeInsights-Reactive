package com.juv3nil3.icdg.service;

import com.juv3nil3.icdg.domain.elasticsearch.DocumentationDocument;
import com.juv3nil3.icdg.repository.elasticsearch.DocumentationSearchRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentationSearchService {

    private final DocumentationSearchRepo searchRepository;

    public DocumentationSearchService(DocumentationSearchRepo searchRepository) {
        this.searchRepository = searchRepository;
    }

    public List<DocumentationDocument> searchByClassName(String className) {
        return searchRepository.findByClassName(className);
    }
}
