package com.juv3nil3.icdg.domain;


import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

@Entity
public class BranchFileAssociation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private BranchMetadata branch;

    @ManyToOne(fetch = FetchType.LAZY)
    private FileData file;

    @ManyToOne(fetch = FetchType.LAZY)
    private PackageData packageData;
}
