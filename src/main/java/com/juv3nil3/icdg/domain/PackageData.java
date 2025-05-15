package com.juv3nil3.icdg.domain;

import com.juv3nil3.icdg.domain.elasticsearch.ClassDataDocument;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
public class PackageData {

    @Id
    private Long id;

    @Column(nullable = false)
    private String packageName;

    private String repoName;

    private Long parentPackageId;
    private Long branchId;

    @org.springframework.data.annotation.Transient
    private PackageData parentPackage;

    @org.springframework.data.annotation.Transient
    private List<PackageData> subPackages = new ArrayList<>();

    @org.springframework.data.annotation.Transient
    private List<BranchFileAssociation> fileAssociations = new ArrayList<>();

    @org.springframework.data.annotation.Transient
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

    public Long getParentPackageId() {
        return parentPackageId;
    }

    public void setParentPackageId(Long parentPackageId) {
        this.parentPackageId = parentPackageId;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
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
}

