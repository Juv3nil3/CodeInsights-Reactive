package com.juv3nil3.icdg.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
public class GithubTokenService {

    private WebClient webClient;
    private String githubTokenUrl;

    public GithubTokenService(WebClient.Builder webClientBuilder,
                                      @Value("${keycloak.github.token-url}") String githubTokenUrl) {
        this.webClient = webClientBuilder.build();
        this.githubTokenUrl = githubTokenUrl;
    }

    public Mono<String> fetchGithubTokenFromKeycloak(String keycloakAccessToken) {
        return webClient.get()
                .uri(githubTokenUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + keycloakAccessToken)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractToken)
                .onErrorResume(e -> {
                    // Log and return empty
                    return Mono.empty();
                });
    }

    private String extractToken(String responseBody) {
        try {
            String decoded = URLDecoder.decode(responseBody, StandardCharsets.UTF_8);
            for (String param : decoded.split("&")) {
                if (param.startsWith("access_token=")) {
                    return param.split("=")[1];
                }
            }
        } catch (Exception e) {
            // Log error
        }
        return null;
    }
}
