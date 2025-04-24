package com.juv3nil3.icdg.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class BranchMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String branchName;
    private String latestCommitHash;

    @ManyToOne
    @JoinColumn(name = "repository_metadata_id")
    private RepositoryMetadata repositoryMetadata;

    @OneToOne(mappedBy = "branchMetadata", cascade = CascadeType.ALL)
    private Documentation documentation;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

