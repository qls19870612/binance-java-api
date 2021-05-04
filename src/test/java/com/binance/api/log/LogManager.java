package com.binance.api.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 创建人  liangsong
 * 创建时间 2021/05/04 17:55
 */
public class LogManager {
    private static final Logger logger = LoggerFactory.getLogger(LogManager.class);
    public static void println(String x) {
        logger.debug("{}", x);
    }
    public static void println(Object x) {
        String s = String.valueOf(x);
        logger.debug("{}", s);
    }
}
