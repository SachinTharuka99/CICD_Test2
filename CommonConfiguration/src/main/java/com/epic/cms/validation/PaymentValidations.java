package com.epic.cms.validation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class PaymentValidations {

    public static boolean isNumeric(String value) throws Exception {
        boolean valied = true;
        try {
            if (!value.trim().matches("[0-9]*")) {
                valied = false;
            }
        } catch (Exception e) {
            valied = false;
        }
        return valied;
    }

    public static boolean isDouble(String value) throws Exception {
        boolean valied = true;
        try {
            if (!value.trim().matches("[0-9]{1,38}(\\.[0-9]*)?")) {
                valied = false;
            }
        } catch (Exception e) {
            valied = false;
        }
        return valied;
    }

    public static boolean isValidDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    public static boolean isString(String value) {
        boolean valied = true;
        try {
            if (!value.matches("[a-zA-Z]*")) {
                valied = false;
            }
        } catch (Exception e) {
            valied = false;
        }
        return valied;
    }
    public static boolean isStringWithSpace(String value) {
        boolean valied = true;
        try {
            if (!value.matches("[a-zA-Z\\s]*")) {
                valied = false;
            }
        } catch (Exception e) {
            valied = false;
        }
        return valied;
    }

    public static boolean isAlphanumeric(String value) {
        boolean valied = true;
        try {
            if (!value.matches("^[a-zA-Z0-9]*$")) {
                valied = false;
            }
        } catch (Exception e) {
            valied = false;
        }
        return valied;
    }

    public static boolean isValidDateTime(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy h:mm:ss a");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    public static boolean isValidDateTimeHHMM(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSSSSSSSS a");
        try {
            dateFormat.parse(inDate.trim());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean isValidPaymentFileDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    public static String fileNameValidation(ArrayList<String> nameFields, String fileName) throws Exception {
        String isValid = "";
        String[] fields = fileName.split("[_.]");

        if (!fields[0].trim().equals(nameFields.get(0))) {
            isValid = "Invalid File Prefix";
        } else if (!isValidPaymentFileDate(fields[1].trim())) {
            isValid = "Invalid File Date Prefix";
        } else if (!isNumeric(fields[2].trim())) {
            isValid = "Invalid File Sequence";
        } else if (!fields[3].trim().equals(nameFields.get(1))) {
            isValid = "Invalid File Postfix";
        } else if (!fields[4].trim().equals(nameFields.get(2))) {
            isValid = "Invalid File Extension";
        }

        return isValid;
    }
}
