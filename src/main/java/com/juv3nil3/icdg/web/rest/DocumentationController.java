package com.juv3nil3.icdg.web.rest;

import com.juv3nil3.icdg.domain.elasticsearch.DocumentationDocument;
import com.juv3nil3.icdg.repository.elasticsearch.DocumentationSearchRepo;
import com.juv3nil3.icdg.service.DocumentationGenerationService;
import com.juv3nil3.icdg.service.GitMetadataExtractor;
import com.juv3nil3.icdg.service.GithubTokenService;
import com.juv3nil3.icdg.service.KeycloakTokenService;
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
    private final KeycloakTokenService keycloakTokenService;
    private final GitMetadataExtractor gitMetadataExtractor;

    private static Logger log = LoggerFactory.getLogger(DocumentationController.class);

    @Autowired
    public DocumentationController(DocumentationGenerationService documentationService, DocumentationMapper documentationMapper, GithubTokenService githubTokenService, DocumentationElasticMapper documentationElasticMapper, DocumentationSearchRepo documentationSearchRepo, KeycloakTokenService keycloakTokenService, GitMetadataExtractor gitMetadataExtractor) {
        this.documentationService = documentationService;
        this.documentationMapper = documentationMapper;
        this.githubTokenService = githubTokenService;
        this.documentationElasticMapper = documentationElasticMapper;
        this.documentationSearchRepo = documentationSearchRepo;
        this.keycloakTokenService = keycloakTokenService;
        this.gitMetadataExtractor = gitMetadataExtractor;
    }

    @GetMapping("/generate")
    public Mono<DocumentationDTO> generateDocumentation(
            @RequestParam String repoName,
            @RequestParam String branchName
    ) {
        return keycloakTokenService.getAccessToken()
                .flatMap(githubTokenService::fetchGithubTokenFromKeycloak)
                .flatMap(githubToken ->
                        gitMetadataExtractor.fetchGitHubUsername(githubToken) // fetch the owner using GitHub API
                                .flatMap(owner ->
                                        documentationService.generateIfNecessary(owner, repoName, branchName, githubToken)
                                                .flatMap(documentationService::fetchFullDocumentation)
                                                .flatMap(documentation -> {
                                                    DocumentationDocument docDocument = documentationElasticMapper.toDocument(documentation);
                                                    return Mono.fromCallable(() -> documentationSearchRepo.save(docDocument))
                                                            .subscribeOn(Schedulers.boundedElastic())
                                                            .doOnSuccess(saved -> log.info("✅ Indexed Documentation in Elasticsearch: ID = {}", saved.getId()))
                                                            .thenReturn(documentation);
                                                })
                                )
                )
                .map(documentationMapper::toDto);
    }

//    @GetMapping("/graph")
//    public Mono<List<GraphEdgeDTO>> getDependencyGraph(
//            @RequestParam UUID documentationId
//    ) {
//        return documentationService.fetchFullDocumentation(documentationId)
//                .map(doc -> {
//                    List<FileData> allFiles = doc.getPackages().stream()
//                            .flatMap(pkg -> Optional.ofNullable(pkg.getFileAssociations()).orElse(List.of()).stream())
//                            .map(BranchFileAssociation::getFile)
//                            .filter(Objects::nonNull)
//                            .distinct()
//                            .toList();
//
//                    return graphService.buildDependencyGraph(allFiles).entrySet().stream()
//                            .flatMap(entry -> entry.getValue().stream().map(dep ->
//                                    new GraphEdgeDTO(entry.getKey().getFilePath(), dep.getFilePath())))
//                            .toList();
//                });
//    }






}
