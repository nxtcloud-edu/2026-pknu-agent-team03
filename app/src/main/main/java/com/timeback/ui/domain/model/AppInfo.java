package com.timeback.ui.domain.model;

public class AppInfo {
    private final String packageName;
    private final String appName;
    private final AppClassification classification;

    public AppInfo(String packageName, String appName, AppClassification classification) {
        this.packageName = packageName;
        this.appName = appName;
        this.classification = classification;
    }

    public String getPackageName() { return packageName; }
    public String getAppName() { return appName; }
    public AppClassification getClassification() { return classification; }
}
