package com.epic.cms.util;

import java.util.Date;

public class DateUtil {
    public static java.sql.Date getSqldate(Date date){
        return new java.sql.Date(date.getTime());
    }
}
