package com.juv3nil3.icdg.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;


@Service
public class GithubTokenService {

    private static final Logger log = LoggerFactory.getLogger(GithubTokenService.class);

    private final WebClient webClient;
    private final String githubTokenUrl;

    public GithubTokenService(WebClient.Builder webClientBuilder,
                              @Value("${keycloak.github.token-url}") String githubTokenUrl) {
        this.webClient = webClientBuilder.build();
        this.githubTokenUrl = githubTokenUrl;
    }

    public Mono<String> fetchGithubTokenFromKeycloak(String keycloakAccessToken) {
        log.info("Requesting GitHub token from Keycloak...");

        return webClient.get()
                .uri(githubTokenUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + keycloakAccessToken)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(body -> log.debug("Keycloak token exchange response: {}", body))
                .map(this::extractToken)
                .doOnNext(token -> log.info("Extracted GitHub token successfully"))
                .onErrorResume(e -> {
                    log.error("Failed to fetch GitHub token from Keycloak", e);
                    return Mono.empty();
                });
    }

    private String extractToken(String responseBody) {
        try {
            String decoded = URLDecoder.decode(responseBody, StandardCharsets.UTF_8);
            for (String param : decoded.split("&")) {
                if (param.startsWith("access_token=")) {
                    String token = param.split("=")[1];
                    log.debug("Decoded GitHub token: {}", token);
                    return token;
                }
            }
            log.warn("GitHub token not found in response body.");
        } catch (Exception e) {
            log.error("Error decoding GitHub token from response", e);
        }
        return null;
    }
}
