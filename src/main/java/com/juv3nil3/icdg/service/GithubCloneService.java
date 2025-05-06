package com.juv3nil3.icdg.service;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class GithubCloneService {

    private static final Logger log = LoggerFactory.getLogger(GithubCloneService.class);

    public Mono<Path> cloneRepository(String owner, String repo, String branch, String token) {
        return Mono.fromCallable(() -> {
            Path tempDir = Files.createTempDirectory("repo-clone-");

            String uri = String.format("https://github.com/%s/%s.git", owner, repo);
            UsernamePasswordCredentialsProvider credentials =
                    token != null ? new UsernamePasswordCredentialsProvider(token, "") : null;

            Git cloned = Git.cloneRepository()
                    .setURI(uri)
                    .setDirectory(tempDir.toFile())
                    .setCredentialsProvider(credentials)
                    .setBranch("refs/heads/" + branch) // Ensure branch is checked out
                    .call();

            log.info("Cloned {} (branch: {}) into {}", uri, branch, tempDir);
            return tempDir;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
