/**
 * Author : sharuka_j
 * Date : 11/22/2022
 * Time : 3:05 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.service;

import com.epic.cms.dao.CardRenewDao;
import com.epic.cms.repository.CardRenewRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import com.epic.cms.model.bean.CardRenewBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class CardRenewService {
    @Autowired
    CardRenewDao cardRenewDao;

    @Autowired
    CardRenewRepo cardRenewRepo;

    @Autowired
    LogManager logManager;


    @Async("taskExecutor2")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void cardRenewProcess(CardRenewBean CRBean) {
        if (!Configurations.isInterrupted) {
            if (!Configurations.isInterrupted) {
                LinkedHashMap details = new LinkedHashMap();
                int NoOfFailCards = 0;
                int noOfEarlyRenewals = 0;
                int noOfNormalRenewals = 0;

                try {
                    //get current month and current year
                    DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy");
                    Date eod_date = Configurations.EOD_DATE;// eod date
                    String eod_date_str = dateFormat.format(eod_date).toUpperCase();//07-OCT-16
                    DateFormat dateFormatforRenew = new SimpleDateFormat("yyMM");
                    String curDateforRenew = dateFormatforRenew.format(eod_date);

                    details.put("Card Number", CommonMethods.cardNumberMask(CRBean.getCardNumber()));

                    int validityPeriod = cardRenewDao.getCardValidityPeriod(CRBean.getCardNumber());
                    if (CRBean.getEarlyRenew().equals("YES")) {
                        noOfEarlyRenewals++;
                        details.put("Early Renew Status", "YES");

                        Calendar date = Calendar.getInstance();
                        date.setTime(eod_date);
                        Format yyMM_Format = new SimpleDateFormat("yyMM");

                        date.add(Calendar.MONTH, validityPeriod);
                        String newExpireDate = yyMM_Format.format(date.getTime());

                        details.put("New Expire Date", newExpireDate);
                        details.put("ReNew Status", "Successful");
                        cardRenewDao.updateCardTable(CRBean.getCardNumber(), newExpireDate, CRBean.getIsProductChange());
                        cardRenewDao.updateCardRenewTable(CRBean.getCardNumber());
                        cardRenewDao.updateOnlineCardTable(CRBean.getCardNumber(), newExpireDate);
                    } else if (CRBean.getEarlyRenew().equals("NO")) {
                        noOfNormalRenewals++;
                        details.put("Early Renew Status", "NO");
                        String OldExpDate = CRBean.getExpirydate();
                        String OldExpYear = OldExpDate.substring(0, 2);
                        String OldExpMonth = OldExpDate.substring(2, 4);

                        Calendar date = Calendar.getInstance();
                        date.clear();
                        date.set(Calendar.MONTH, Integer.parseInt(OldExpMonth) - 1); //months start from 0-jan 1-feb ..
                        date.set(Calendar.YEAR, Integer.parseInt(OldExpYear));

                        Format yyMM_Format = new SimpleDateFormat("yyMM");

                        date.add(Calendar.MONTH, validityPeriod);
                        String newExpireDate = yyMM_Format.format(date.getTime());

                        details.put("New Expire Date", newExpireDate);
                        cardRenewDao.updateCardTable(CRBean.getCardNumber(), newExpireDate, CRBean.getIsProductChange());
                        cardRenewDao.updateCardRenewTable(CRBean.getCardNumber());
                        cardRenewDao.updateOnlineCardTable(CRBean.getCardNumber(), newExpireDate);
                        details.put("ReNew Status", "Successful");
                    }

                    /** if the card is already expired, then expiry date will calculate 4 years from current date+-
                     check two conditions, bcoz some card may tempBlock after expiry, if we consider only expiry status, those cards will missing
                     */
                    else if (Integer.parseInt(CRBean.getExpirydate()) <= Integer.parseInt(curDateforRenew) || CRBean.getCardStatus().equals("CAEX")) {

                        Calendar date = Calendar.getInstance();
                        date.setTime(eod_date);
                        Format yyMM_Format = new SimpleDateFormat("yyMM");
                        date.add(Calendar.MONTH, validityPeriod);
                        String newExpireDate = yyMM_Format.format(date.getTime());

                        details.put("New Expire Date", newExpireDate);
                        details.put("ReNew Status", "Successful");
                        cardRenewDao.updateCardTable(CRBean.getCardNumber(), newExpireDate, CRBean.getIsProductChange());
                        cardRenewDao.updateCardRenewTable(CRBean.getCardNumber());
                        cardRenewDao.updateOnlineCardTable(CRBean.getCardNumber(), newExpireDate);
                    }
                    Configurations.PROCESS_SUCCESS_COUNT++;
                    // call ProcessDetailsStriles method in logger
                    infoLogger.info(logManager.processDetailsStyles(details));
                } catch (Exception ex) {
                    details.put("ReNew Status", "Fail");
                    infoLogger.info(logManager.processDetailsStyles(details));
                    errorLogger.error("Renew Process Fails for Card " + CommonMethods.cardNumberMask(CRBean.getCardNumber()), ex);
                    NoOfFailCards++;
                    Configurations.PROCESS_FAILD_COUNT++;
                }
            }
        }
    }
}
