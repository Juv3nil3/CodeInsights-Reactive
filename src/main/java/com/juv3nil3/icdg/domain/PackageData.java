package com.juv3nil3.icdg.domain;

import com.juv3nil3.icdg.domain.elasticsearch.ClassDataDocument;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class PackageData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String packageName; // Fully qualified package name

    private String repoName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_package_id")
    private PackageData parentPackage; // Reference to the parent package (null for top-level)

    @OneToMany(mappedBy = "parentPackage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PackageData> subPackages = new ArrayList<>(); // Sub-packages of this package

    @OneToMany(mappedBy = "packageData")
    private List<BranchFileAssociation> fileAssociations;

    @ManyToOne(fetch = FetchType.LAZY)
    private BranchMetadata branch;


    public PackageData() {}

    public PackageData(String packageName) {
        this.packageName = packageName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public List<BranchFileAssociation> getFileAssociations() {
        return fileAssociations;
    }

    public void setFileAssociations(List<BranchFileAssociation> fileAssociations) {
        this.fileAssociations = fileAssociations;
    }

    public BranchMetadata getBranch() {
        return branch;
    }

    public void setBranch(BranchMetadata branch) {
        this.branch = branch;
    }

    public PackageData getParentPackage() {
        return parentPackage;
    }

    public void setParentPackage(PackageData parentPackage) {
        this.parentPackage = parentPackage;
    }

    public List<PackageData> getSubPackages() {
        return subPackages;
    }

    public void setSubPackages(List<PackageData> subPackages) {
        this.subPackages = subPackages;
    }
}

