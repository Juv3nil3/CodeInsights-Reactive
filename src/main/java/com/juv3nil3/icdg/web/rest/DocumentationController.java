package com.juv3nil3.icdg.web.rest;

import com.juv3nil3.icdg.domain.elasticsearch.DocumentationDocument;
import com.juv3nil3.icdg.repository.elasticsearch.DocumentationSearchRepo;
import com.juv3nil3.icdg.service.DocumentationGenerationService;
import com.juv3nil3.icdg.service.GithubTokenService;
import com.juv3nil3.icdg.service.dto.DocumentationDTO;
import com.juv3nil3.icdg.service.mapper.DocumentationElasticMapper;
import com.juv3nil3.icdg.service.mapper.DocumentationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/documentation")
public class DocumentationController {

    private final DocumentationGenerationService documentationService;
    private final DocumentationMapper documentationMapper;
    private final GithubTokenService githubTokenService;
    private final DocumentationElasticMapper documentationElasticMapper;
    private final DocumentationSearchRepo documentationSearchRepo;

    private static Logger log = LoggerFactory.getLogger(DocumentationController.class);

    @Autowired
    public DocumentationController(DocumentationGenerationService documentationService, DocumentationMapper documentationMapper, GithubTokenService githubTokenService, DocumentationElasticMapper documentationElasticMapper, DocumentationSearchRepo documentationSearchRepo) {
        this.documentationService = documentationService;
        this.documentationMapper = documentationMapper;
        this.githubTokenService = githubTokenService;
        this.documentationElasticMapper = documentationElasticMapper;
        this.documentationSearchRepo = documentationSearchRepo;
    }

    @GetMapping("/generate")
    public Mono<DocumentationDTO> generateDocumentation(
            @RequestParam String owner,
            @RequestParam String repoName,
            @RequestParam String branchName,
            @RequestHeader(name = "Authorization") String authorizationHeader
    ) {
        return githubTokenService.fetchGithubTokenFromKeycloak(authorizationHeader)
                .flatMap(token ->
                        documentationService.generateIfNecessary(owner, repoName, branchName, token) // returns Mono<UUID>
                )
                .flatMap(documentationService::fetchFullDocumentation) // returns Mono<Documentation>
                .flatMap(documentation -> {
                    DocumentationDocument docDocument = documentationElasticMapper.toDocument(documentation);
                    return Mono.fromCallable(() -> documentationSearchRepo.save(docDocument)) // save to Elasticsearch
                            .subscribeOn(Schedulers.boundedElastic())
                            .doOnSuccess(saved -> log.info("✅ Indexed Documentation in Elasticsearch: ID = {}", saved.getId()))
                            .thenReturn(documentation); // pass original Documentation down the chain
                })
                .map(documentationMapper::toDto);
    }



}
