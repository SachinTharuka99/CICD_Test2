package com.epic.cms.service;

import com.epic.cms.repository.CRIBFileRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

import static com.epic.cms.util.LogManager.infoLogger;
import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class CRIBFileService {

    @Autowired
    LogManager logManager;

    @Autowired
    CRIBFileRepo cribFileRepo;

    private int[] cardCount;
    LinkedHashMap summery = new LinkedHashMap();

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void startCribFileProcess() throws Exception {
        if (!Configurations.isInterrupted) {
            try {
                //check EOD date is month end date (if not need to exit from process)
                boolean isEndofMonthDate = false;

                //get current day
                SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy");
                Date eodDate = Configurations.EOD_DATE;
                String eodDateString = dateFormat.format(eodDate);

                //get last date of particular month
                Calendar c = Calendar.getInstance();
                c.setTime(eodDate);
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH)); //get last date of month
                String lastDateOfMonth = dateFormat.format(c.getTime()); //get formatted last date

                isEndofMonthDate = lastDateOfMonth.equals(eodDateString);

                if (isEndofMonthDate) {//current date is a end of month date

                    //call to stored procedure. fetch all data and insert to intermediate table
                    try {
                        cardCount = cribFileRepo.callStoredProcedureCribFileGeneration();
                    } catch (Exception ex) {
                        errorLogger.error("CRIB file process exception for stored procedure", ex);
                        throw ex;
                    }
                } else {
                    //current date is not a end of month date
                }

            } catch (Exception e) {
                Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
                errorLogger.error("Failed crib file process:", e);
            } finally {
                addSummaries();
                infoLogger.info(logManager.processSummeryStyles(summery));
            }
        }
    }

    public void addSummaries() {
        if (cardCount != null) {
            summery.put("Number of Main Card Customers in CRIB File ", cardCount[0]);
            summery.put("Number of Supplimentry Card Customers in CRIB File ", cardCount[1]);
            summery.put("Number of Corporate Card Customers in CRIB File ", cardCount[2]);
            summery.put("Number of FD Card Customers in CRIB File ", cardCount[3]);
        } else {
            summery.put("Number of Main Card Customers in CRIB File ", 0);
            summery.put("Number of Supplimentry Card Customers in CRIB File ", 0);
            summery.put("Number of Corporate Card Customers in CRIB File ", 0);
            summery.put("Number of FD Card Customers in CRIB File ", 0);
        }
    }
}
