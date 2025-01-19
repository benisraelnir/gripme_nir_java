package com.grip.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Default Configuration
 *
 * Do NOT change the values here for risk of accidentally committing them.
 * Override them using application.properties, application.yml, or environment variables.
 */
@Component
@ConfigurationProperties(prefix = "grip")
public class GripSettings {
    
    private String host = "localhost";
    private int port = 6419;
    private boolean debug = false;
    private boolean debugGrip = false;
    private String cacheDirectory = "cache-{version}";
    private boolean autorefresh = true;
    private boolean quiet = false;

    /**
     * Note: For security concerns, please don't save your GitHub password in your
     * configuration files. Use a personal access token instead:
     * https://github.com/settings/tokens/new?scopes=
     */
    private String username = null;
    private String password = null;

    // Custom GitHub API
    private String apiUrl = null;

    // Custom styles
    private List<String> styleUrls = new ArrayList<>();

    // Getters and Setters
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebugGrip() {
        return debugGrip;
    }

    public void setDebugGrip(boolean debugGrip) {
        this.debugGrip = debugGrip;
    }

    public String getCacheDirectory() {
        return cacheDirectory;
    }

    public void setCacheDirectory(String cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    public boolean isAutorefresh() {
        return autorefresh;
    }

    public void setAutorefresh(boolean autorefresh) {
        this.autorefresh = autorefresh;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public List<String> getStyleUrls() {
        return styleUrls;
    }

    public void setStyleUrls(List<String> styleUrls) {
        this.styleUrls = styleUrls;
    }
}