package com.juv3nil3.icdg.web.rest;


import com.juv3nil3.icdg.service.GithubTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/github")
public class GithubController {

    private final WebClient webClient;

    private final GithubTokenService githubTokenService;

    @Autowired
    private ReactiveOAuth2AuthorizedClientService clientService;
    private static final Logger log = LoggerFactory.getLogger(GithubController.class);


    public GithubController(WebClient.Builder webClientBuilder,
                            @Value("${github.api.base-url:https://api.github.com}") String githubApiBaseUrl, GithubTokenService githubTokenService) {
        this.githubTokenService = githubTokenService;
        this.webClient = webClientBuilder.baseUrl(githubApiBaseUrl).build();
    }

    @GetMapping("/repos")
    public Mono<List<String>> getJavaRepositories() {
        return getAccessToken()
                .doOnNext(keycloakToken -> log.info("🔑 Keycloak Access Token: {}", keycloakToken))
                .flatMapMany(keycloakToken ->
                        githubTokenService.fetchGithubTokenFromKeycloak(keycloakToken)
                                .doOnNext(githubToken -> log.info("🐙 GitHub Access Token: {}", githubToken))
                                .flatMapMany(githubToken ->
                                        webClient.get()
                                                .uri("/user/repos?per_page=100")
                                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken)
                                                .retrieve()
                                                .bodyToFlux(GitHubRepo.class)
                                                .doOnNext(repo -> log.info("📦 Repo: {} (Language: {})", repo.getName(), repo.getLanguage()))
                                )
                )
                .filter(repo -> {
                    boolean isJava = "Java".equalsIgnoreCase(repo.getLanguage());
                    if (isJava) {
                        log.info("✅ Java Repo Found: {}", repo.getName());
                    }
                    return isJava;
                })
                .map(GitHubRepo::getName)
                .collectList()
                .doOnSuccess(names -> log.info("🎯 Final Java Repositories List: {}", names));
    }


    @GetMapping("/token")
    public Mono<String> token() {
        return getAccessToken()
                .doOnNext(token -> log.info("Retrieved Access Token: {}", token))
                .map(token -> "Bearer " + token);
    }






    // Minimal POJO to deserialize GitHub repo objects
    public static class GitHubRepo {
        private String name;
        private String language;

        // Getters and setters
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        public String getLanguage() {
            return language;
        }
        public void setLanguage(String language) {
            this.language = language;
        }
    }


    public Mono<String> getAccessToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(OAuth2AuthenticationToken.class)
                .flatMap(auth ->
                        clientService.loadAuthorizedClient(
                                auth.getAuthorizedClientRegistrationId(),
                                auth.getName()
                        )
                )
                .cast(OAuth2AuthorizedClient.class)
                .map(client -> client.getAccessToken().getTokenValue());
    }






}
