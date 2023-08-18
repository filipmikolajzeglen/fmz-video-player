package com.filipmikolajzeglen.logger;

enum LoggerLevel {
    INFO("\u001B[36m"),
    ERROR("\u001B[31m"),
    RUNNING("\u001B[0m"),
    WARNING("\u001B[33m");

    private final String colorCode;

    LoggerLevel(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getColorCode() {
        return colorCode;
    }
}
