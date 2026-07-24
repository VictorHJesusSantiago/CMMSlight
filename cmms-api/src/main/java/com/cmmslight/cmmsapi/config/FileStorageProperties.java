package com.cmmslight.cmmsapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cmms.storage")
public class FileStorageProperties {

    /** Diretorio local (disco) onde os anexos de ativos sao gravados. */
    private String baseDir = "./data/attachments";

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
}
