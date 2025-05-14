package com.juv3nil3.icdg.domain;


import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

@Entity
public class BranchFileAssociation {

    @Id
    private Long id;

    private String repoName;
    private String filePath;

    private Long branchId;
    private Long fileId;
    private Long packageDataId;

    @Transient
    private BranchMetadata branch;

    @Transient
    private FileData file;

    @Transient
    private PackageData packageData;


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
