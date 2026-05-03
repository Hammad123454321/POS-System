package com.poslink.sample.setting.model;

/**
 * Created by Leon.F on 2018/3/13.
 */

public class BaseLogSetting {
    public static final String ENABLE = "ON";
    public static final String DISABLE = "OFF";
    private String logSwitch;
    private String logLevel;
    private String logFilePath = "./";
    private String logFileName;
    private String logDays = "";

    public String getLogSwitch() {
        return logSwitch;
    }

    public void setLogSwitch(String logSwitch) {
        this.logSwitch = logSwitch;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }

    public String getLogDays() {
        return logDays;
    }

    public void setLogDays(String logDays) {
        this.logDays = logDays;
    }
}
