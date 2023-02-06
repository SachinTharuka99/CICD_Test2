package com.epic.cms.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Component
public class LogManager {

    public static Logger infoLogger = getInfoLogger();
    public static Logger errorLogger = getErrorLogger();

    public static Logger getInfoLogger() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
//        String path = Configurations.EOD_LOGS_FILE_PATH;
//        String subDirectory = new SimpleDateFormat("dd-MMM-yy").format(Configurations.EOD_DATE);

        PatternLayoutEncoder ple = new PatternLayoutEncoder();

        //define log pattern
        ple.setPattern(" %d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n");
        ple.setContext(lc);
        ple.start();

        //console appender
        ConsoleAppender infoConsoleAppender = new ConsoleAppender();
        infoConsoleAppender.setContext(lc);
        infoConsoleAppender.setName("console");
        infoConsoleAppender.setEncoder(ple);
        infoConsoleAppender.start();

        //file appender
        FileAppender<ILoggingEvent> infoFileAppender = new FileAppender<ILoggingEvent>();
//        infoFileAppender.setFile(path + subDirectory + "/eod_info.log");
        infoFileAppender.setFile("C:/eod_logs/" + getFormattedEodDate1() + "/eod_info.log");
        infoFileAppender.setEncoder(ple);
        infoFileAppender.setContext(lc);
        infoFileAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger("InfoLog");
        logger.addAppender(infoConsoleAppender);
        logger.addAppender(infoFileAppender);
        logger.setLevel(Level.INFO);
        logger.setAdditive(false); /* set to true if root should log too */

        return logger;
    }

    public static Logger getErrorLogger() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
//        String path = Configurations.EOD_LOGS_FILE_PATH;
//        String subDirectory = new SimpleDateFormat("dd-MMM-yy").format(Configurations.EOD_DATE);

        PatternLayoutEncoder ple = new PatternLayoutEncoder();

        //define log pattern
        ple.setPattern("%d [%thread] %-5level %-5logger{40} - %msg%n");
        ple.setContext(lc);
        ple.start();

        //console appender
        ConsoleAppender errorConsoleAppender = new ConsoleAppender();
        errorConsoleAppender.setContext(lc);
        errorConsoleAppender.setName("console");
        errorConsoleAppender.setEncoder(ple);
        errorConsoleAppender.start();

        //file appender
        FileAppender<ILoggingEvent> errorFileAppender = new FileAppender<ILoggingEvent>();
//        errorFileAppender.setFile(path + subDirectory + "/eod_info.log");
        errorFileAppender.setFile("C:/eod_logs/" + getFormattedEodDate1() + "/eod_error.log");
        errorFileAppender.setEncoder(ple);
        errorFileAppender.setContext(lc);
        errorFileAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger("ErrorLog");
        logger.addAppender(errorConsoleAppender);
        logger.addAppender(errorFileAppender);
        logger.setLevel(Level.INFO);
        logger.setAdditive(false); /* set to true if root should log too */

        return logger;
    }

    //header style
    public String processHeaderStyle(String name) {
//        String eodDate = Configurations.EOD_DATE.toString();
        String eodDate = "22081600";
        String symbol = "~";
        int fixed_length = 100;
        int processName_lenght = name.length();
        int symbolic_length = fixed_length - processName_lenght;
        String style = "";
        if (symbolic_length % 2 == 0) {
            for (int i = 0; i < symbolic_length; i++) {
                if (i == (symbolic_length / 2) + 1) {
                    style = style + "[" + name + "]";
                } else {
                    style = style + symbol;
                }
            }
        } else {
            for (int i = 0; i < symbolic_length; i++) {
                if (i == ((symbolic_length - 1) / 2) + 1) {
                    style = style + "[" + name + "]";
                } else {
                    style = style + symbol;
                }
            }
        }
        style = eodDate + style + System.lineSeparator();

        return style;
    }

    //start and end style
    public static String processStartEndStyle(String name) {
//        String curDate = new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(Configurations.EOD_DATE);
        String curDate = getFormattedEodDate2();

        String temp = "[" + curDate + "]" + "  " + name + System.lineSeparator();
        return temp;
    }

    //detail style
    public static synchronized String processDetailsStyles(Map<String, Object> detailsMap) {
        String description = null;
        if (detailsMap.size() > 0) {
            int maxLength = 0;
            description = "      ";// 6 white spaces
            for (String key : detailsMap.keySet()) {
                if (key.length() > maxLength) {
                    maxLength = key.length();
                }
            }

            for (String key : detailsMap.keySet()) {
                Object tempvalue = detailsMap.get(key);
                String value = null;
                if (tempvalue == null) {
                    value = "-";
                } else {
                    value = tempvalue.toString();
                }

                int space = maxLength - key.length() + 3;
                for (int i = 0; i < space; i++) {
                    key = key + " ";
                }
                description = description + key + "- " + value + System.lineSeparator() + "      ";//6 white spaces
            }
            description = description + "-------------------------------------" + System.lineSeparator();

        }
        return description;
    }

    //summery style
    public String processSummeryStyles(Map<String, Object> detailsMap) {
        int maxLengthKey = 0;
        int maxLengthValue = 0;
        int maxLength = 0;
        int count = 0;
        int spacesAfterLine = 0;
        String description = null;

        if (detailsMap.size() > 0) {
            description = "*  ";// 6 white spaces
            for (String key : detailsMap.keySet()) {
                if (key.length() > maxLengthKey) {
                    maxLengthKey = key.length();
                }

                if (detailsMap.get(key).toString().length() > maxLengthValue) {
                    maxLengthValue = detailsMap.get(key).toString().length();
                }
            }

            for (String key : detailsMap.keySet()) {
                count++;
                String value = detailsMap.get(key).toString();

                int space = maxLengthKey - key.length() + 2;
                for (int i = 0; i < space; i++) {
                    key = key + " ";
                }

                if (count == 1) {
                    maxLength = (key + "- ").length() + maxLengthValue;// 6 is the number of white spaces in description

                }

                spacesAfterLine = maxLengthValue - value.length();
                String afterLineDesgn = "";
                for (int i = 0; i <= spacesAfterLine + 2; i++) {
                    afterLineDesgn = afterLineDesgn + " ";
                }
                afterLineDesgn = afterLineDesgn + "*";
                if (!key.equals("")) {
                    description = description + key + "- " + value + afterLineDesgn + System.lineSeparator() + "*  ";//6 white spaces
                } else {
                    description = description + key + " " + value + afterLineDesgn + System.lineSeparator() + "*  ";//6 white spaces
                }
            }

            String summeryDesign = "*******";//7 stars;
            for (int i = 0; i < maxLength; i++) {
                summeryDesign = summeryDesign + "*";
            }
            if (description.length() - 5 > 0) {
                description = description.substring(0, description.length());
                description = summeryDesign + System.lineSeparator() + description + System.lineSeparator() + summeryDesign;
            } else {
                description = "--No Summery Data To View--" + System.lineSeparator();
            }
            // remove the final new line
        }
        return description;
    }

    private static String getFormattedEodDate2() {
        String curDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
            Date eodDate = sdf.parse("220820");
            curDate = new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(eodDate);
        } catch (Exception ex) {

        }
        return curDate;
    }

    private static String getFormattedEodDate1() {
        String curDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
            Date eodDate = sdf.parse("220820");
            curDate = new SimpleDateFormat("dd-MMM-yy").format(eodDate);
        } catch (Exception ex) {

        }
        return curDate;
    }


    public String ProcessStartEndStyle(String atm_file_validate_process_failed) {
        String curDate = new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(Configurations.EOD_DATE);

        String temp = "[" + curDate + "]" + "  " + atm_file_validate_process_failed + System.lineSeparator();
        return temp;
    }

    //format of headline
    // EOD_LOGS_FILE_PATH_WINDOWS
    public String ProcessHeaderStyle(String name) {
        String eoddate = Configurations.EOD_DATE.toString();
        String symbol = "~";
        int fixed_length = 100;
        int processName_lenght = name.length();
        int symbolic_length = fixed_length - processName_lenght;
        String style = "";
        if (symbolic_length % 2 == 0) {
            for (int i = 0; i < symbolic_length; i++) {
                if (i == (symbolic_length / 2) + 1) {
                    style = style + "[" + name + "]";
                } else {
                    style = style + symbol;
                }
            }
        } else {
            for (int i = 0; i < symbolic_length; i++) {
                if (i == ((symbolic_length - 1) / 2) + 1) {
                    style = style + "[" + name + "]";
                } else {
                    style = style + symbol;
                }
            }
        }
        style = eoddate + style + System.lineSeparator();

        return style;
    }
}
