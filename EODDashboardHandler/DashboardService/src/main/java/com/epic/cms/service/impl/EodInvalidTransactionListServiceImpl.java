/**
 * Author : rasintha_j
 * Date : 2/23/2023
 * Time : 1:25 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service.impl;

import com.epic.cms.model.bean.EodInvalidTransactionBean;
import com.epic.cms.model.entity.RECATMFILEINVALID;
import com.epic.cms.model.entity.RECPAYMENTFILEINVALID;
import com.epic.cms.repository.RecAtmFileInvalidRepo;
import com.epic.cms.repository.RecPaymentFileInvalidRepo;
import com.epic.cms.service.EodInvalidTransactionListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.epic.cms.util.LogManager.dashboardErrorLogger;


@Service
public class EodInvalidTransactionListServiceImpl implements EodInvalidTransactionListService {

    @Autowired
    RecAtmFileInvalidRepo atmFileInvalidRepo;

    @Autowired
    RecPaymentFileInvalidRepo paymentFileInvalidRepo;

    @Override
    public List<Object> getEodInvalidTransactionList(Long eodId) {
        List<Object> invalidTransactionBeanList = new ArrayList<>();

        try {
            List<RECATMFILEINVALID> recAtmFileInvalidList = atmFileInvalidRepo.findRecAtmFileInvalidByEodId(eodId);
            List<RECPAYMENTFILEINVALID> recPaymentFileInvalidList = paymentFileInvalidRepo.findRecPaymentFileInvalidByEodId(eodId);

            recAtmFileInvalidList.forEach(eod -> {
                EodInvalidTransactionBean eodInvalidTransactionBean = new EodInvalidTransactionBean();
                eodInvalidTransactionBean.setEodId(eod.getEODID());
                eodInvalidTransactionBean.setFileId(eod.getFILEID());
                eodInvalidTransactionBean.setFileType("ATM");
                eodInvalidTransactionBean.setLineNumber(eod.getLINENUMBER());
                eodInvalidTransactionBean.setErrorRemark(eod.getERRORDESC());

                invalidTransactionBeanList.add(eodInvalidTransactionBean);
            });

            recPaymentFileInvalidList.forEach(eod -> {
                EodInvalidTransactionBean eodInvalidTransactionBean = new EodInvalidTransactionBean();
                eodInvalidTransactionBean.setEodId(eod.getEODID());
                eodInvalidTransactionBean.setFileId(eod.getFILEID());
                eodInvalidTransactionBean.setFileType("PAYMENT");
                eodInvalidTransactionBean.setLineNumber(eod.getLINENUMBER());
                eodInvalidTransactionBean.setErrorRemark(eod.getERRORDESC());

                invalidTransactionBeanList.add(eodInvalidTransactionBean);
            });
        } catch (Exception e) {
            dashboardErrorLogger.error("Get Eod Invalid Transaction List Error", e);
            throw e;
        }
        return invalidTransactionBeanList;
    }
}
