package com.juv3nil3.icdg.service;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitMetadataExtractor {

    public static String getLatestCommitSha(Path repoPath, String branchName) throws IOException {
        try (Repository repository = new FileRepositoryBuilder()
                .setGitDir(repoPath.resolve(".git").toFile())
                .build()) {
            ObjectId branchObjectId = repository.resolve("refs/heads/" + branchName);
            return branchObjectId != null ? branchObjectId.getName() : null;
        }
    }

    public static RevCommit getLatestCommit(Path repoPath, String branchName) throws IOException, GitAPIException {
        try (Git git = Git.open(repoPath.toFile())) {
            Iterable<RevCommit> commits = git.log().add(git.getRepository().resolve(branchName)).setMaxCount(1).call();
            return commits.iterator().hasNext() ? commits.iterator().next() : null;
        }
    }

    public static List<String> getAllBranchNames(Path repoPath) throws IOException, GitAPIException {
        try (Git git = Git.open(repoPath.toFile())) {
            return git.branchList().call().stream()
                    .map(ref -> ref.getName().replace("refs/heads/", ""))
                    .collect(Collectors.toList());
        }
    }

    public static Date getCommitDate(RevCommit commit) {
        return commit.getAuthorIdent().getWhen();
    }

    public static String getCommitAuthor(RevCommit commit) {
        return commit.getAuthorIdent().getName();
    }

    public static String getCommitMessage(RevCommit commit) {
        return commit.getFullMessage();
    }
}
