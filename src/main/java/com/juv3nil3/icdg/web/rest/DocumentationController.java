package com.juv3nil3.icdg.web.rest;

import com.juv3nil3.icdg.service.DocumentationGenerationService;
import com.juv3nil3.icdg.service.GithubTokenService;
import com.juv3nil3.icdg.service.dto.DocumentationDTO;
import com.juv3nil3.icdg.service.mapper.DocumentationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/documentation")
public class DocumentationController {

    private final DocumentationGenerationService documentationService;
    private final DocumentationMapper documentationMapper;
    private final GithubTokenService githubTokenService;

    @Autowired
    public DocumentationController(DocumentationGenerationService documentationService, DocumentationMapper documentationMapper, GithubTokenService githubTokenService) {
        this.documentationService = documentationService;
        this.documentationMapper = documentationMapper;
        this.githubTokenService = githubTokenService;
    }

    @GetMapping(value = "/{owner}/{repoName}/{branchName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<DocumentationDTO> generateDocumentation(
            @PathVariable String owner,
            @PathVariable String repoName,
            @PathVariable String branchName,
            @RequestHeader(name = "Authorization") String authorizationHeader
    ) {
        return githubTokenService.fetchGithubTokenFromKeycloak(authorizationHeader)
                .flatMap(token ->
                        documentationService.generateIfNecessary(owner, repoName, branchName, token)
                );
    }

}
