package com.juv3nil3.icdg.domain;


import jakarta.persistence.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.util.UUID;

@Entity
public class BranchFileAssociation {

    @Id
    private UUID id;


    private String repoName;
    private String filePath;

    private UUID branchId;
    private UUID fileId;
    private UUID packageDataId;

    @org.springframework.data.annotation.Transient
    private BranchMetadata branch;

    @org.springframework.data.annotation.Transient
    private FileData file;

    @org.springframework.data.annotation.Transient
    private PackageData packageData;


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public void setBranchId(UUID branchId) {
        this.branchId = branchId;
    }

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public UUID getPackageDataId() {
        return packageDataId;
    }

    public void setPackageDataId(UUID packageDataId) {
        this.packageDataId = packageDataId;
    }

    public BranchMetadata getBranch() {
        return branch;
    }

    public void setBranch(BranchMetadata branch) {
        this.branch = branch;
    }

    public FileData getFile() {
        return file;
    }

    public void setFile(FileData file) {
        this.file = file;
    }

    public PackageData getPackageData() {
        return packageData;
    }

    public void setPackageData(PackageData packageData) {
        this.packageData = packageData;
    }
}
