package com.filipmikolajzeglen.fmzvideoplayer.logger;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
enum LoggerLevel {
    INFO("\u001B[36m"),
    ERROR("\u001B[31m"),
    RUNNING("\u001B[0m"),
    WARNING("\u001B[33m");

    private final String colorCode;
}

