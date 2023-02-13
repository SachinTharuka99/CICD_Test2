package com.ecms.web.api.tokenservice.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Common {

    public static String formatDatetoString(Date d) {
        String fDate = "";
        try {
            String pattern = "dd/MM/yyyy hh:mm:ss a";
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            fDate = dateFormat.format(d);
        } catch (Exception e) {
            fDate = "--";
        }
        return fDate;
    }
}
