package com.juv3nil3.icdg.web.rest;


import com.juv3nil3.icdg.service.GithubTokenService;
import com.juv3nil3.icdg.service.dto.GitHubRepoPojo;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
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
                                                .bodyToFlux(GitHubRepoPojo.class)
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
                .map(GitHubRepoPojo::getName)
                .collectList()
                .doOnSuccess(names -> log.info("🎯 Final Java Repositories List: {}", names));
    }


    @GetMapping("/repos/{repo}/branches")
    public Mono<List<String>> getBranches(@PathVariable String repo) {
        return getAccessToken()
                .doOnNext(keycloakToken -> log.info("🔑 Keycloak Access Token: {}", keycloakToken))
                .flatMap(keycloakToken ->
                        githubTokenService.fetchGithubTokenFromKeycloak(keycloakToken)
                                .doOnNext(githubToken -> log.info("🐙 GitHub Access Token: {}", githubToken))
                                .flatMap(githubToken ->
                                        webClient.get()
                                                .uri("/user")
                                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken)
                                                .retrieve()
                                                .bodyToMono(GitHubUser.class)
                                                .map(GitHubUser::getLogin)
                                                .flatMap(username ->
                                                        webClient.get()
                                                                .uri("/repos/{owner}/{repo}/branches", username, repo)
                                                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken)
                                                                .retrieve()
                                                                .bodyToFlux(GitHubBranch.class)
                                                                .doOnNext(branch -> log.info("🌿 Branch: {}", branch.getName()))
                                                                .map(GitHubBranch::getName)
                                                                .collectList()
                                                )
                                )
                )
                .doOnSuccess(list -> log.info("✅ Branches fetched for repo: {}", repo));
    }


    public static class GitHubUser {
        private String login; // GitHub username

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }
    }

    public static class GitHubBranch {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @GetMapping("/token")
    public Mono<String> token() {
        return getAccessToken()
                .doOnNext(token -> log.info("Retrieved Access Token: {}", token))
                .map(token -> "Bearer " + token);
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
