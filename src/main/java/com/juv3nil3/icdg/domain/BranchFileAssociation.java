package com.juv3nil3.icdg.domain;


import jakarta.persistence.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

@Entity
public class BranchFileAssociation {

    @Id
    private Long id;

    private String repoName;
    private String filePath;

    private Long branchId;
    private Long fileId;
    private Long packageDataId;

    @org.springframework.data.annotation.Transient
    private BranchMetadata branch;

    @org.springframework.data.annotation.Transient
    private FileData file;

    @org.springframework.data.annotation.Transient
    private PackageData packageData;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getPackageDataId() {
        return packageDataId;
    }

    public void setPackageDataId(Long packageDataId) {
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
