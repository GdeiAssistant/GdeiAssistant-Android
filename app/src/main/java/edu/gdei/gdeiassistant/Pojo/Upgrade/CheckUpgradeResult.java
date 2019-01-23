package edu.gdei.gdeiassistant.Pojo.Upgrade;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckUpgradeResult implements Serializable {

    private String downloadURL;

    private String versionInfo;

    private String versionCodeName;

    private String fileSize;

    private Integer versionCode;

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public String getVersionInfo() {
        return versionInfo;
    }

    public void setVersionInfo(String versionInfo) {
        this.versionInfo = versionInfo;
    }

    public String getVersionCodeName() {
        return versionCodeName;
    }

    public void setVersionCodeName(String versionCodeName) {
        this.versionCodeName = versionCodeName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Integer versionCode) {
        this.versionCode = versionCode;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }
}
