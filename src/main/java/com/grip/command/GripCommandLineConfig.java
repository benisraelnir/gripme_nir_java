package com.grip.command;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;

/**
 * Configuration properties for Grip command-line interface.
 * Maps command-line arguments to configuration properties.
 */
@Configuration
@ConfigurationProperties(prefix = "grip")
public class GripCommandLineConfig {
    @Value("${application.version:4.6.2}")
    private String version;
    private String path;
    private String address;
    private boolean forceUtf8 = true;
    private boolean patchSvg = true;
    private boolean userContent;
    private String context;
    private String user;
    private char[] password;
    private boolean wide;
    private boolean clear;
    private boolean export;
    private boolean noInline;
    private boolean browser;
    private String apiUrl;
    private String title;
    private boolean norefresh;
    private boolean quiet;
    private String theme = "light";

    // Getters and setters
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isUserContent() {
        return userContent;
    }

    public void setUserContent(boolean userContent) {
        this.userContent = userContent;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public char[] getPassword() {
        return password != null ? password.clone() : null;
    }

    public void setPassword(char[] password) {
        if (this.password != null) {
            Arrays.fill(this.password, '\0');
        }
        this.password = password != null ? password.clone() : null;
    }

    public void clearPassword() {
        if (password != null) {
            Arrays.fill(password, '\0');
            password = null;
        }
    }

    public boolean isWide() {
        return wide;
    }

    public void setWide(boolean wide) {
        this.wide = wide;
    }

    public boolean isClear() {
        return clear;
    }

    public void setClear(boolean clear) {
        this.clear = clear;
    }

    public boolean isExport() {
        return export;
    }

    public void setExport(boolean export) {
        this.export = export;
    }

    public boolean isNoInline() {
        return noInline;
    }

    public void setNoInline(boolean noInline) {
        this.noInline = noInline;
    }

    public boolean isBrowser() {
        return browser;
    }

    public void setBrowser(boolean browser) {
        this.browser = browser;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isNorefresh() {
        return norefresh;
    }

    public void setNorefresh(boolean norefresh) {
        this.norefresh = norefresh;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        if (!isValidTheme(theme)) {
            throw new IllegalArgumentException("Invalid theme option. Valid options are 'light' and 'dark'");
        }
        this.theme = theme;
    }

    private boolean isValidTheme(String theme) {
        return "light".equals(theme) || "dark".equals(theme);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isForceUtf8() {
        return forceUtf8;
    }

    public void setForceUtf8(boolean forceUtf8) {
        this.forceUtf8 = forceUtf8;
    }

    public boolean isPatchSvg() {
        return patchSvg;
    }

    public void setPatchSvg(boolean patchSvg) {
        this.patchSvg = patchSvg;
    }
}