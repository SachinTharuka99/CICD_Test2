package com.epic.cms.service;

import com.epic.cms.dao.FeePostDao;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.OtbBean;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;

import static com.epic.cms.util.LogManager.infoLogger;
import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class FeePostService {

    @Autowired
    public LogManager logManager;

    @Autowired
    public FeePostDao feePostDao;

    @Autowired
    public StatusVarList status;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void proceedFeePost(OtbBean bean) {
        if (!Configurations.isInterrupted) {
            System.out.println("called proceedFeePost");
            LinkedHashMap detail = new LinkedHashMap();

            List<OtbBean> feeList;
            boolean feeAdditionSuccess = false;
            try {
                feeList = feePostDao.getFeeAmount(bean.getAccountnumber());
                int iterator = 1;

                cards:
                for (OtbBean cardBean : feeList) {
                    try {
                        /**
                         * Update backend CARD table OTBCREDIT and
                         * TEMPCREDITAMOUNT by fee amount for the particular
                         * Card Number
                         */
                        feePostDao.updateCardOtb(cardBean);
                        /**
                         * Update backend EODCARDBALANCE table EODCLOSINGBAL and
                         * FINANCIALCHARGES by fee amount for the particular
                         * Card Number
                         */
                        feePostDao.updateEODCARDBALANCEByFee(cardBean);
                        /**
                         * Update online ECMS_ONLINE_CARD table OTBCREDIT and
                         * TEMPCREDITAMOUNT by fee amount for the particular
                         * Card Number
                         */
                        feePostDao.updateOnlineCardOtb(cardBean);

                        bean.setOtbcredit(bean.getOtbcredit() + cardBean.getOtbcredit());

                        if (feeList.size() == iterator) {
                            /**
                             * Update backend CARDACCOUNT table OTBCREDIT by fee
                             * amount for the particular Account Number
                             */
                            feePostDao.updateAccountOtb(bean);
                            /**
                             * Update backend EODCARDFEE table STATUS as EDON
                             * for the particular Account Number
                             */
                            feePostDao.updateEODCARDFEE(bean.getAccountnumber());
                            /**
                             * Update backend EOMINTEREST table STATUS as EDON
                             * for the particular Account Number
                             */
                            feePostDao.updateEOMINTEREST(bean.getAccountnumber());
                            /**
                             * Update online ECMS_ONLINE_ACCOUNT table OTBCREDIT
                             * by fee amount for the particular Account Number
                             */
                            feePostDao.updateOnlineAccountOtb(bean);
                            /**
                             * Update backend CARDCUSTOMER table OTBCREDIT by
                             * fee amount for the particular Customer ID
                             */
                            feePostDao.updateCustomerOtb(bean);
                            /**
                             * Update online ECMS_ONLINE_CUSTOMER table
                             * OTBCREDIT by fee amount for the particular
                             * Customer ID
                             */
                            feePostDao.updateOnlineCustomerOtb(bean);

                            for (OtbBean otbBean : feeList) {
                                detail.put("Customer ID", bean.getCustomerid());
                                detail.put("Account Number", bean.getAccountnumber());
                                detail.put("Card Number", CommonMethods.cardNumberMask(otbBean.getCardnumber()));
                                detail.put("Fee Amount", otbBean.getOtbcredit());
                                infoLogger.info(logManager.processDetailsStyles(detail));
                                detail.clear();
                            }

                        }
                        feeAdditionSuccess = true;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        ex.printStackTrace();
                        errorLogger.error("Fee post process failed for account " + bean.getAccountnumber(), ex);
                        Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, bean.getCardnumber(), ex.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.ACCOUNT));
                        break cards;
                    }
                    iterator++;
                }
                if (feeAdditionSuccess) {
                    Configurations.PROCESS_SUCCESS_COUNT++;
                } else {
                    Configurations.PROCESS_FAILD_COUNT++;
                }
                feeAdditionSuccess = false;
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLogger.error("Error Occured: ", ex);
            }
        }
    }

}
