package com.epic.cms.util;

import com.epic.cms.repository.CommonRepo;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateEodId {

    // get Current date as yymmdd format
    public String getDate(Date date) throws Exception {
        try {
            String dateString = "";

            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
            dateString = sdf.format(date);
            return dateString;
        } catch (Exception e) {
            e.printStackTrace();
            //LogFileCreator.writeErrorToLog(e);
            throw e;
        }
    }

    public Date getDateFromEODID(int eodID) throws Exception {
        Date parsedDate = null;
        String streodID = "";
        try {
            if (eodID > 10000000) {
                streodID = eodID + "";
                SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
                String eodIDsubs = streodID.substring(0, streodID.length() - 2);
                parsedDate = sdf.parse(eodIDsubs);
            }

        } catch (Exception e) {
            e.printStackTrace();
//            LogFileCreator.writeErrorToLog(e);
            throw e;
        } finally {
            return parsedDate;
        }
    }
}
