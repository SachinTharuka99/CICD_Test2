package com.epic.cms.service;

import com.epic.cms.model.bean.EodTransactionBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.ErrorMerchantBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.AcqTxnUpdateRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class AcqTxnUpdateService {

    public List<ErrorMerchantBean> merchantErrorList = new ArrayList<ErrorMerchantBean>();
    public int configProcess = Configurations.PROCESS_ID_ACQUIRING_TXN_UPDATE_PROCESS;
    public String processHeader = "ACQUIRING_TXN_UPDATE_PROCESS";
    public List<ErrorCardBean> cardErrorList = new ArrayList<ErrorCardBean>();
    @Autowired
    LogManager logManager;
    @Autowired
    StatusVarList status;
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    AcqTxnUpdateRepo acqTxnUpdateRepo;
    HashMap<Integer, ArrayList<EodTransactionBean>> txnMap;
    ArrayList<EodTransactionBean> onusTxnList;
    ArrayList<EodTransactionBean> txnList;
    String forexPercentage = "0";
    String fuelSurchargeRate = "0";
    String backendTxnType;
    BigDecimal destinationAmount = new BigDecimal(BigInteger.ZERO);
    BigDecimal destinationAmountWithFuelSurCharge = new BigDecimal(BigInteger.ZERO);
    String maskedCardNumber;

    @Async("taskExecutor2")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processAcqTxnUpdate(Integer key, EodTransactionBean eodTransactionBean, HashMap<String, String> visaTxnFields, List<String> fuelMccList) throws Exception {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            LinkedHashMap summery = new LinkedHashMap();
            ProcessBean processBean = null;
            int issFailedTxn = 0;
            int acqFailedMerchants = 0;
            int onusTxnCount = 0;
            int totalTxnCount = 0;

            try {
                //Check for financial status yes
                if (isFinancialStatusYes(eodTransactionBean.getTxnType(), visaTxnFields)) {
                    totalTxnCount++;
                    destinationAmount = new BigDecimal(BigInteger.ZERO);
                    destinationAmountWithFuelSurCharge = new BigDecimal(BigInteger.ZERO);

                    int count = 1;

                    try {
                        maskedCardNumber = CommonMethods.cardNumberMask(eodTransactionBean.getCardNo());
                        details.put("Cardnumber", maskedCardNumber);

                        backendTxnType = eodTransactionBean.getTxnType(); //backendTxnType = this.setBackendTxnType(eodTransactionBean.getTxnType());

                        details.put("Online Txn Type", eodTransactionBean.getTxnType());
                        details.put("EOD Txn Type", backendTxnType);
                        destinationAmount = new BigDecimal(eodTransactionBean.getTxnAmount());
                        destinationAmountWithFuelSurCharge = new BigDecimal(eodTransactionBean.getTxnAmount());
                        //set crdr and calculate fuel surcharge amount for debit transactions if applicable

                        if (backendTxnType.equalsIgnoreCase(Configurations.TXN_TYPE_PAYMENT)
                                || backendTxnType.equalsIgnoreCase(Configurations.TXN_TYPE_REFUND)
                                || backendTxnType.equalsIgnoreCase(Configurations.TXN_TYPE_MVISA_REFUND)
                                || backendTxnType.equalsIgnoreCase(Configurations.TXN_TYPE_CUP_QR_REFUND)
                                || backendTxnType.equalsIgnoreCase(Configurations.TXN_TYPE_REVERSAL)) {

                            eodTransactionBean.setCrDr(Configurations.CREDIT);
                        } else {
                            eodTransactionBean.setCrDr(Configurations.DEBIT);

                            //calculate fuel surcharge amount for fuel MCCs
                            if (fuelMccList.contains(eodTransactionBean.getMcc())) {
                                //calculate surchage amount
                                BigDecimal surchargeAmount = destinationAmount.multiply(new BigDecimal(fuelSurchargeRate)).divide(BigDecimal.valueOf(100), MathContext.DECIMAL32).setScale(2, RoundingMode.DOWN);

                                eodTransactionBean.setFuelSurchargeAmount(surchargeAmount.toString());

                                //fual surcharge amount will not added to eodmerchanttransaction table txn amount. it will be in a separate column
                                destinationAmountWithFuelSurCharge = destinationAmount.add(surchargeAmount);
                            }
                        }
                        details.put("CRDR Type", eodTransactionBean.getCrDr());

                        //decide card association based on channel type(5 visa, 3 master)
                        if (eodTransactionBean.getChannelType() == Configurations.CHANNEL_TYPE_VISA) {
                            eodTransactionBean.setCardAssociation(Configurations.VISA_ASSOCIATION);

                            //if visa and ipg transaction then need to set card product as IPG - Visa. Note:- OTHER CARD PRODUCTS WILL SET AT LAST IN THIS PROCESS
                            if (eodTransactionBean.getListenerType() != null && Integer.toString(status.getLISTENER_TYPE_IPG()).equals(eodTransactionBean.getListenerType())) {
                                eodTransactionBean.setCardProduct(status.getPRODUCT_CODE_IPG_VISA());
                            }
                        } else if (eodTransactionBean.getChannelType() == Configurations.CHANNEL_TYPE_MASTER) {
                            eodTransactionBean.setCardAssociation(Configurations.MASTER_ASSOCIATION);

                            //if master and ipg transaction then need to set card product as IPG - Master, Note:- OTHER CARD PRODUCTS WILL SET AT LAST IN THIS PROCESS
                            if (eodTransactionBean.getListenerType() != null && Integer.toString(status.getLISTENER_TYPE_IPG()).equals(eodTransactionBean.getListenerType())) {
                                eodTransactionBean.setCardProduct(status.getPRODUCT_CODE_IPG_MASTER());
                            }
                        } else if (eodTransactionBean.getChannelType() == Configurations.CHANNEL_TYPE_CUP) {
                            eodTransactionBean.setCardAssociation(Configurations.CUP_ASSOCIATION);
                            eodTransactionBean.setCardProduct(status.getPRODUCT_CODE_CUP_ALL()); //set card product as cup all for commission calculations
                        }
                        if (backendTxnType.equalsIgnoreCase(Configurations.TXN_TYPE_MVISA_REFUND)) {
                            eodTransactionBean.setCardAssociation(Configurations.VISA_ASSOCIATION); // for mvisa refund transactions channel type not 5 , so decide card association from transaction type
                        } else if (backendTxnType.equalsIgnoreCase(Configurations.TXN_TYPE_CUP_QR_REFUND) || backendTxnType.equalsIgnoreCase(Configurations.TXN_TYPE_CUP_QR_PAYMENT)) {
                            eodTransactionBean.setCardAssociation(Configurations.CUP_ASSOCIATION); // for cup QR payment/refund transactions channel type not available , so decide card association from transaction type
                            eodTransactionBean.setCardProduct(status.getPRODUCT_CODE_CUP_ALL());
                        }

                        if (key == status.getONUS_STATUS()) { // onoffstatus=1 (onus cards)
                            details.put("ONUS Status", "TRUE");
                            details.put("Currency Type", eodTransactionBean.getCurrencyType());
                            onusTxnCount++;
                            try {
                                //Calculate Forex Amount for currency not equal to base currency and for debit transactions
                                if (!eodTransactionBean.getCurrencyType().equalsIgnoreCase(Configurations.BASE_CURRENCY) && eodTransactionBean.getCrDr().equals(Configurations.DEBIT)) {
                                    BigDecimal forexMarkupAmount = destinationAmountWithFuelSurCharge.multiply(new BigDecimal(forexPercentage)).divide(BigDecimal.valueOf(100), MathContext.DECIMAL32).setScale(2, RoundingMode.DOWN);
                                    String destinationAmountWithForex = destinationAmountWithFuelSurCharge.add(forexMarkupAmount).setScale(2, RoundingMode.DOWN).toString();
                                    eodTransactionBean.setForexMarkupAmount(forexMarkupAmount.toString());
                                    eodTransactionBean.setTxnAmount(destinationAmountWithForex);
                                    details.put("Forex amount", eodTransactionBean.getForexMarkupAmount());
                                }
                                count = commonRepo.insertToEODTransaction(eodTransactionBean.getCardNo(), eodTransactionBean.getAccountNo(),
                                        eodTransactionBean.getMid(), eodTransactionBean.getTid(), destinationAmountWithFuelSurCharge.setScale(2, RoundingMode.DOWN).toString(), Integer.parseInt(eodTransactionBean.getCurrencyType()),
                                        eodTransactionBean.getCrDr(), eodTransactionBean.getSettlementDate(), eodTransactionBean.getTxnDate(), backendTxnType,
                                        eodTransactionBean.getBatchNo(), eodTransactionBean.getTxnId(), eodTransactionBean.getToAccNo(), 0.0, eodTransactionBean.getTxnDescription(),
                                        eodTransactionBean.getCountryNumCode(), eodTransactionBean.getOnOffStatus(), eodTransactionBean.getPosEntryMode(), eodTransactionBean.getTraceId(),
                                        eodTransactionBean.getAuthCode(), 5, eodTransactionBean.getRequestFrom(), eodTransactionBean.getSecondPartyPan(), eodTransactionBean.getFuelSurchargeAmount(), eodTransactionBean.getMcc(), eodTransactionBean.getCardAssociation());
                                details.put("Total txn amount", destinationAmountWithFuelSurCharge.setScale(2, RoundingMode.DOWN).toString());
                                Configurations.PROCESS_SUCCESS_COUNT++;
                            } catch (Exception e) {
                                details.put("Sync to EOD Txn table", "Failed");
                                issFailedTxn++;
                                Configurations.PROCESS_FAILD_COUNT++;
                                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, eodTransactionBean.getCardNo(), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                                details.put("Process Status", "Failed");
                                infoLogger.info(processHeader + " failed for cardnumber " + CommonMethods.cardInfo(maskedCardNumber, processBean));
                                errorLogger.error(processHeader + " failed for cardnumber " + CommonMethods.cardInfo(maskedCardNumber, processBean), e);
                            }
                        }

                        if (count == 1) {
                            try {
                                //Txn amount without forex amount and without fuel surcharge
                                eodTransactionBean.setTxnAmount(destinationAmount.setScale(2, RoundingMode.DOWN).toString());
                                eodTransactionBean.setBin(eodTransactionBean.getCardNo().substring(0, 6));
                                count = 0;
                                count = commonRepo.insertIntoEodMerchantTransaction(eodTransactionBean, status.getEOD_PENDING_STATUS());//insert querry
                                if (count == 1) {
                                    details.put("Sync to Merchant Txn table", "Passed");
                                    count = 0;
                                    //Update txn table to EDON
                                    count = commonRepo.updateTransactionToEDON(eodTransactionBean.getTxnId(), eodTransactionBean.getCardNo());
                                    Configurations.PROCESS_SUCCESS_COUNT++;
                                }
                            } catch (Exception e) {
                                details.put("Sync to Merchant Txn table", "Failed");
                                acqFailedMerchants++;
                                Configurations.PROCESS_FAILD_COUNT++;
                                //merchantErrorList.add(new ErrorMerchantBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, eodTransactionBean.getMid(), e.getMessage(), configProcess, processHeader, 0, MerchantCustomerEnum.MERCHANTLOCATION));
                                details.put("Process Status", "Failed");
                                infoLogger.info(processHeader + " failed for mid " + eodTransactionBean.getMid());
                                errorLogger.error(processHeader + " failed for mid " + eodTransactionBean.getMid(), e);
                            }
                        }


                    } catch (Exception e) {

                        infoLogger.info(logManager.processStartEndStyle(processHeader + " Completely  failed"));
                        errorLogger.error(processHeader + " failed for while data selection at the initial level", e);
                    }
                    infoLogger.info(logManager.processDetailsStyles(details));
                    details.clear();
                }
                Configurations.totalTxnCount_AcqTxnUpdateProcess = totalTxnCount;
                Configurations.failedTxnCount_AcqTxnUpdateProcess = issFailedTxn;
                Configurations.acqFailedMerchantCount_AcqTxnUpdateProcess = acqFailedMerchants;
                Configurations.onusTxnCount_AcqTxnUpdateProcess = onusTxnCount;

            } catch (Exception e) {
                errorLogger.info("Failed Acq Txn Update Process for Card " + CommonMethods.cardNumberMask(eodTransactionBean.getCardNo()), e);
            }
        }
    }

    public synchronized boolean isFinancialStatusYes(String txnType, HashMap<String, String> visaTxnFields) throws Exception {
        boolean status = false;
        String financialStatus = "NO";
        try {
            financialStatus = visaTxnFields.get(txnType);
            if (financialStatus.equalsIgnoreCase(Configurations.YES_STATUS)) {
                status = true;
            }
        } catch (Exception e) {
            throw e;
        }
        return status;
    }

}
