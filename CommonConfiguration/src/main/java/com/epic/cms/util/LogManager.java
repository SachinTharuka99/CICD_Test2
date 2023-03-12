package com.epic.cms.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Map;

@Component
@DependsOn("ConfigurationService")
public class LogManager {

    public static Logger infoLogger = null, infoLoggerCOM = null, infoLoggerEFPE = null, infoLoggerEFGE = null;
    public static Logger errorLogger = null, errorLoggerCOM = null, errorLoggerEFPE = null, errorLoggerEFGE = null;
    public static String logTypeInfo = Configurations.LOG_TYPE_INFO;
    public static String logTypeError = Configurations.LOG_TYPE_ERROR;

    @PostConstruct
    public static void init() {
        //info loggers
        infoLoggerCOM = getLogger(logTypeInfo, "common_info", Configurations.LOG_FILE_PREFIX_COMMON);
        infoLogger = getLogger(logTypeInfo, "engine_info", Configurations.LOG_FILE_PREFIX_EOD_ENGINE);
        infoLoggerEFPE = getLogger(logTypeInfo, "file_pro_engine_info", Configurations.LOG_FILE_PREFIX_EOD_FILE_PROCESSING_ENGINE);
        infoLoggerEFGE = getLogger(logTypeInfo, "file_gen_engine_info", Configurations.LOG_FILE_PREFIX_EOD_FILE_GENERATION_ENGINE);
        //error loggers
        errorLoggerCOM = getLogger(logTypeError, "common_error", Configurations.LOG_FILE_PREFIX_COMMON);
        errorLogger = getLogger(logTypeError, "engine_error", Configurations.LOG_FILE_PREFIX_EOD_ENGINE);
        errorLoggerEFPE = getLogger(logTypeError, "file_pro_engine_error", Configurations.LOG_FILE_PREFIX_EOD_FILE_PROCESSING_ENGINE);
        errorLoggerEFGE = getLogger(logTypeError, "file_gen_engine_error", Configurations.LOG_FILE_PREFIX_EOD_FILE_GENERATION_ENGINE);
    }

    /**
     * Config Logger Instances
     *
     * @param logType
     * @param loggerName
     * @param fileNamePrefix
     * @return
     */
    public static Logger getLogger(String logType, String loggerName, String fileNamePrefix) {
        String fileNamePostfix = null;
        String logPattern = null;
        if (logType.equals(Configurations.LOG_TYPE_INFO)) {
            fileNamePostfix = "_info.log";
            logPattern = Configurations.INFO_LOG_PATTERN;
        } else {
            fileNamePostfix = "_error.log";
            logPattern = Configurations.ERROR_LOG_PATTERN;
        }

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        String path = Configurations.EOD_LOGS_FILE_PATH;
        String subDirectory = new SimpleDateFormat("dd-MMM-yy").format(Configurations.EOD_DATE);

        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern(logPattern);
        ple.setContext(lc);
        ple.start();

        //console appender
        ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setEncoder(ple);
        consoleAppender.setContext(lc);
        consoleAppender.start();

        //file appender
        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setFile(path + subDirectory + "/" + fileNamePrefix + fileNamePostfix);
        fileAppender.setEncoder(ple);
        fileAppender.setContext(lc);
        fileAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
        logger.detachAndStopAllAppenders();
        logger.addAppender(consoleAppender);
        logger.addAppender(fileAppender);
        logger.setLevel(Level.INFO);
        logger.setAdditive(false); /* set to true if root should log too */

        return logger;
    }

    /**
     * header style
     *
     * @param name
     * @return
     */
    public String processHeaderStyle(String name) {
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
        style = Configurations.EOD_DATE_String + style + System.lineSeparator();

        return style;
    }

    /**
     * start and end style
     *
     * @param name
     * @return
     */
    public String processStartEndStyle(String name) {
        String curDate = new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(Configurations.EOD_DATE);

        String temp = "[" + curDate + "]" + "  " + name + System.lineSeparator();
        return temp;
    }

    /**
     * detail style
     *
     * @param detailsMap
     * @return
     */
    public String processDetailsStyles(Map<String, Object> detailsMap) {
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

    /**
     * summery style
     *
     * @param detailsMap
     * @return
     */
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

    public static void logProcessHeader(String msg, Logger logger) {
        String symbol = "~";
        int fixed_length = 100;
        int processName_lenght = msg.length();
        int symbolic_length = fixed_length - processName_lenght;
        String formattedMsg = "";
        if (symbolic_length % 2 == 0) {
            for (int i = 0; i < symbolic_length; i++) {
                if (i == (symbolic_length / 2) + 1) {
                    formattedMsg = formattedMsg + "[" + msg + "]";
                } else {
                    formattedMsg = formattedMsg + symbol;
                }
            }
        } else {
            for (int i = 0; i < symbolic_length; i++) {
                if (i == ((symbolic_length - 1) / 2) + 1) {
                    formattedMsg = formattedMsg + "[" + msg + "]";
                } else {
                    formattedMsg = formattedMsg + symbol;
                }
            }
        }
        formattedMsg = Configurations.EOD_DATE_String + formattedMsg + System.lineSeparator();

        //write into a log
        logger.info(formattedMsg);
        //pass into a kafka topic
    }

    public static void logProcessStartEnd(String msg, Logger logger) {
        String curDate = new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(Configurations.EOD_DATE);

        String formattedMsg = "[" + curDate + "]" + "  " + msg + System.lineSeparator();
        //write into a log
        logger.info(formattedMsg);
        //pass into a kafka topic
    }

    public static void logProcessDetails(Map<String, Object> detailsMap, Logger logger) {
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
        //write into a log
        logger.info(description);
        //pass into a kafka topic
    }

    public static void logProcessSummery(Map<String, Object> detailsMap, Logger logger) {
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
        //write into a log
        logger.info(description);
        //pass into a kafka topic
    }

    public static void logProcessError(String msg, Throwable e, Logger logger) {
        logger.error(msg, e);
    }
}
