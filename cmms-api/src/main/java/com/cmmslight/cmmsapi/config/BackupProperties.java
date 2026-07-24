package com.cmmslight.cmmsapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cmms.backup")
public class BackupProperties {

    /** Diretorio local onde os dumps de backup do Postgres sao gravados. */
    private String directory = "./data/backups";

    /** Caminho do executavel pg_dump (deve estar instalado localmente, sem servico de cloud). */
    private String pgDumpPath = "pg_dump";

    /** Caminho do executavel psql, usado na restauracao. */
    private String psqlPath = "psql";

    private String host = "localhost";
    private String port = "5432";
    private String database = "cmmslight";
    private String username = "cmmslight";

    public String getDirectory() { return directory; }
    public void setDirectory(String directory) { this.directory = directory; }
    public String getPgDumpPath() { return pgDumpPath; }
    public void setPgDumpPath(String pgDumpPath) { this.pgDumpPath = pgDumpPath; }
    public String getPsqlPath() { return psqlPath; }
    public void setPsqlPath(String psqlPath) { this.psqlPath = psqlPath; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public String getPort() { return port; }
    public void setPort(String port) { this.port = port; }
    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
