package com.juv3nil3.icdg.web.rest;


import com.juv3nil3.icdg.service.GithubTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/github")
public class GithubController {

    private final WebClient webClient;

    private final GithubTokenService githubTokenService;

    public GithubController(WebClient.Builder webClientBuilder,
                            @Value("${github.api.base-url:https://api.github.com}") String githubApiBaseUrl, GithubTokenService githubTokenService) {
        this.githubTokenService = githubTokenService;
        this.webClient = webClientBuilder.baseUrl(githubApiBaseUrl).build();
    }

    @GetMapping("/repos")
    public Flux<String> getJavaRepositories(@RequestHeader("Authorization") String authHeader) {
        return githubTokenService.fetchGithubTokenFromKeycloak(authHeader)
                .flatMapMany(token ->
                        webClient.get()
                                .uri("/user/repos?per_page=100") // relative path only here
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token) // add Bearer prefix
                                .retrieve()
                                .bodyToFlux(GitHubRepo.class)
                                .filter(repo -> "Java".equalsIgnoreCase(repo.getLanguage()))
                                .map(GitHubRepo::getName)
                );
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

}
