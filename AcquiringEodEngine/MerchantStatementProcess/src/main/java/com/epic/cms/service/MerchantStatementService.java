package com.epic.cms.service;

import com.epic.cms.model.bean.ErrorMerchantBean;
import com.epic.cms.model.bean.MerchantLocationBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.MerchantStatementRepository;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.MerchantCustomer;
import com.epic.cms.util.StatusVarList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MerchantStatementService  {


    private Map.Entry<String, MerchantLocationBean> entry;
    private static final Boolean BACKEDN_CON_REQ = Boolean.TRUE;
    private static final Boolean ONLINE_CON_REQ = Boolean.FALSE;
    public List<ErrorMerchantBean> merchantErrorList = new ArrayList<ErrorMerchantBean>();
    int successMerchantCount = 0;
    int failedMerchantCount = 0;
    public String processHeader = "MERCHANT STATEMENT PROCESS";

    String fieldDelimeter = "|";
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");

    @Autowired
    MerchantStatementRepository merchantStatementDao;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    StatusVarList statusList;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void merchantStatementService(Map.Entry<String, MerchantLocationBean> entry) throws Exception{

        List<Object[]> txnList = new ArrayList<>();
        List<Object[]> adjList = new ArrayList<>();
        List<Object[]> feeList = new ArrayList<>();
        StringBuilder sbContent = new StringBuilder();
        String MerchantID = entry.getKey();
        boolean isInsert = false;

        try {

            MerchantLocationBean merchantBean = entry.getValue();
            merchantBean = merchantStatementDao.insertMerchantStatement(merchantBean);
            try {

                txnList = merchantStatementDao.getMerchantStatementTxnList(merchantBean.getMerchantId());
                adjList = merchantStatementDao.getMerchantStatementAdjustmentList(merchantBean.getMerchantId());
                feeList = merchantStatementDao.getMerchantStatementFeesList(merchantBean.getMerchantId());

                if (txnList.size() > 0 || adjList.size() > 0 || feeList.size() > 0) {
                    isInsert = true;
                }

                if (isInsert) {
                    sbContent.append("1").append(fieldDelimeter)
                            .append(merchantBean.getMerchantDes()).append(fieldDelimeter)
                            .append(merchantBean.getAddress1().trim()).append(fieldDelimeter)
                            .append(merchantBean.getAddress2().trim()).append(fieldDelimeter)
                            .append(merchantBean.getAddress3().trim()).append(fieldDelimeter)
                            .append("").append(fieldDelimeter)
                            .append("").append(fieldDelimeter)
                            .append(merchantBean.getPostalCode().trim())
                            .append(System.lineSeparator());

                    sbContent.append("2").append(fieldDelimeter)
                            .append(merchantBean.getMerchantId()).append(fieldDelimeter)
                            .append(merchantBean.getMerchantType()).append(fieldDelimeter)
                            .append(sdf.format(merchantBean.getLastBillingDate()) + " - " + sdf.format(merchantBean.getBillingDate())).append(fieldDelimeter)
                            .append(merchantBean.getOpeningBalance()).append(fieldDelimeter)
                            .append(merchantBean.getClosingBalance()).append(fieldDelimeter)
                            .append(merchantBean.getMerchantCurrency()).append(fieldDelimeter)
                            .append(merchantBean.getNetPaymentAmount())
                            .append(System.lineSeparator());

                    for (Object[] txn : txnList) {
                        sbContent.append("3").append(fieldDelimeter)
                                .append(txn[0]).append(fieldDelimeter)
                                .append(txn[1]).append(fieldDelimeter)
                                .append(txn[2]).append(fieldDelimeter)
                                .append(txn[3]).append(fieldDelimeter)
                                .append(txn[4]).append(fieldDelimeter)
                                .append(txn[5]).append(fieldDelimeter)
                                .append(txn[6]).append(fieldDelimeter)
                                .append(txn[7])
                                .append(System.lineSeparator());
                    }

                    for (Object[] txn : adjList) {
                        sbContent.append("4").append(fieldDelimeter)
                                .append(txn[0]).append(fieldDelimeter)
                                .append(txn[1]).append(fieldDelimeter)
                                .append(txn[2]).append(fieldDelimeter)
                                .append(txn[3]).append(fieldDelimeter)
                                .append(txn[4])
                                .append(System.lineSeparator());
                    }

                    for (Object[] txn : feeList) {
                        sbContent.append("5").append(fieldDelimeter)
                                .append(txn[0]).append(fieldDelimeter)
                                .append(txn[1]).append(fieldDelimeter)
                                .append(txn[2]).append(fieldDelimeter)
                                .append(txn[3])
                                .append(System.lineSeparator());
                    }

                    sbContent.append("6").append(fieldDelimeter)
                            .append(merchantBean.getPaymentAmount()).append(fieldDelimeter)
                            .append(merchantBean.getCommissionAmount()).append(fieldDelimeter)
                            .append(merchantBean.getFeeAmount()).append(fieldDelimeter)
                            .append(merchantBean.getNetPaymentAmount()).append(fieldDelimeter)
                            .append(merchantBean.getPaymentMode()).append(fieldDelimeter)
                            .append(merchantBean.getBankName()).append(fieldDelimeter)
                            .append(merchantBean.getAccNumber())
                            .append(System.lineSeparator());

                    sbContent.append("7").append(fieldDelimeter)
                            .append(merchantBean.getMerchantEmail())
                            .append(System.lineSeparator());

                }

            } catch (Exception ex) {
                throw ex;
            }
            successMerchantCount++;
            Configurations.PROCESS_SUCCESS_COUNT++;
        }
        catch (Exception ex)
        {
            merchantErrorList.add(new ErrorMerchantBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, MerchantID, ex.getMessage(), Configurations.PROCESS_ID_MERCHANT_STATEMENT, processHeader, 0, MerchantCustomer.MERCHANTLOCATION));
            failedMerchantCount++;
            Configurations.PROCESS_FAILD_COUNT++;
            logError.error("Error Occurs, when running merchant statement process for mID " + MerchantID + " ", ex);
        }
    }
}
