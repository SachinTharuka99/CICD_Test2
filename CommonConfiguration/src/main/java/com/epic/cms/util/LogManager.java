package com.epic.cms.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LogManager {
    final String topic = Configurations.LOG_TOPIC;

    @Autowired
    public KafkaTemplate<String, String> kafkaTemplate;

    /**
     * start and end style
     *
     * @param name
     * @return
     */
    public String processStartEndStyle(String name) {
        //String curDate = new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(Configurations.EOD_DATE);

        //String temp = "[" + curDate + "]" + "  " + name + System.lineSeparator();
        String temp = name + System.lineSeparator();

        try {
            kafkaTemplate.send(topic, temp);
        } catch (Exception e) {
            System.out.println("Kafka log_topic error");
        }

        return temp;
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
        //style = Configurations.EOD_DATE_String + style + System.lineSeparator();
        style = style + System.lineSeparator();

        try {
            kafkaTemplate.send(topic, style);
        } catch (Exception e) {
            System.out.println("Kafka log_topic error");
        }

        return style;
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

//        try {
//            kafkaTemplate.send(topic, description);
//        } catch (Exception e) {
//            System.out.println("Kafka log_topic error");
//        }

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
                description = description;
                description = summeryDesign + System.lineSeparator() + description + System.lineSeparator() + summeryDesign;
            } else {
                description = "--No Summery Data To View--" + System.lineSeparator();
            }
            // remove the final new line
        }

        try {
            kafkaTemplate.send(topic, description);
        } catch (Exception e) {
            System.out.println("Kafka log_topic error");
        }

        return description;
    }

    public String logHeader(String msg) {
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
        //formattedMsg = Configurations.EOD_DATE_String + formattedMsg + System.lineSeparator();
        formattedMsg = formattedMsg + System.lineSeparator();

        try {
            kafkaTemplate.send(topic, formattedMsg);
        } catch (Exception e) {
            System.out.println("Kafka log_topic error");
        }
        return formattedMsg;
    }

    public String logStartEnd(String msg) {
        //String curDate = new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(Configurations.EOD_DATE);

        //String formattedMsg = "[" + curDate + "]" + "  " + msg + System.lineSeparator();
        String formattedMsg = msg + System.lineSeparator();

        try {
            kafkaTemplate.send(topic, formattedMsg);
        } catch (Exception e) {
            System.out.println("Kafka log_topic error");
        }

        return formattedMsg;
    }

    public String logDetails(Map<String, Object> detailsMap) {
        String description = "";
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

//            try {
//                kafkaTemplate.send(topic, description);
//            } catch (Exception e) {
//                System.out.println("Kafka log_topic error");
//            }
        }
        return description;
    }

    public String logSummery(Map<String, Object> detailsMap) {
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
                description = description;
                description = summeryDesign + System.lineSeparator() + description + System.lineSeparator() + summeryDesign;
            } else {
                description = "--No Summery Data To View--" + System.lineSeparator();
            }
        }
        return description;
    }

    public void logDashboardInfo(String msg) {
        try {
            kafkaTemplate.send(topic, msg);
        } catch (Exception e) {
            System.out.println("Kafka log_topic error");
        }
    }
}
