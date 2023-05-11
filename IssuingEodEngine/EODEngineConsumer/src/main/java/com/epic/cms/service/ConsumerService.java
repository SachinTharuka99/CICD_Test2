package com.epic.cms.service;

import com.epic.cms.connector.*;
import com.epic.cms.util.Configurations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class ConsumerService {

    @Autowired
    InitialConnector initialConnector; //Done

    @Autowired
    CardReplaceConnector cardReplaceConnector;//Done

    @Autowired
    EodParameterResetConnector eodParameterResetConnector;//Done

    @Autowired
    AdjustmentConnector adjustmentConnector;

    @Autowired
    CardFeeConnector cardFeeConnector;

    @Autowired
    FeePostConnector feePostConnector;

    @Autowired
    AtmCashAdvanceUpdateConnector cashAdvanceUpdateConnector;

    @Autowired
    PaymentReversalsConnector paymentReversalsConnector;//Done

    @Autowired
    TxnMismatchPostConnector txnMismatchPostConnector;

    @Autowired
    IncrementLimitExpireConnector incrementLimitExpireConnector;//Done

    @Autowired
    CardPermanentBlockConnector cardPermanentBlockConnector;//Done

    @Autowired
    CardTemporaryBlockConnector cardTemporaryBlockConnector;//Done

    @Autowired
    CardLimitEnhancementConnector cardLimitEnhancementConnector;//Done

    @Autowired
    AcqTxnUpdateConnector acqTxnUpdateConnector;

    @Autowired
    ClearMinAmountAndTempBlockConnector clearMinAmountAndTempBlockConnector;

    @Autowired
    CollectionAndRecoveryConnector collectionAndRecoveryConnector;
    @Autowired
    StampDutyFeeConnector stampDutyFeeConnector;

    @Autowired
    CardExpireConnector cardExpireConnector;

    @Autowired
    OverLimitFeeConnector overLimitFeeConnector;

    @Autowired
    ChequePaymentConnector chequePaymentConnector;

    @Autowired
    CheckPaymentForMinimumAmountConnector checkPaymentForMinimumAmountConnector;

    @Autowired
    CRIBFileConnector cribFileConnector;

    @Autowired
    CashBackAlertConnector cashBackAlertConnector;

    @Autowired
    CollectionAndRecoveryAlertConnector collectionAndRecoveryAlertConnector;

    @Autowired
    TransactionUpdateConnector transactionUpdateConnector;

    @Autowired
    LoanOnCardConnector loanOnCardConnector;

    @Autowired
    ChequeReturnConnector chequeReturnConnector;

    @Autowired
    RunnableFeeConnector runnableFeeConnector;

    @Autowired
    ManualNpConnector manualNpConnector;

    @Autowired
    OnlineToBackendTxnConnector onlineToBackendTxnConnector;

    @Autowired
    CashBackConnector cashBackConnector;

    @Autowired
    KnockOffConnector knockOffConnector;

    @Autowired
    EOMInterestConnector eomInterestConnector;

    @Autowired
    MonthlyStatementConnector monthlyStatementConnector;

    @Autowired
    CardRenewConnector cardRenewConnector;

    @Autowired
    TransactionPostConnector transactionPostConnector;

    @Autowired
    RiskCalculationConnector riskCalculationConnector;

    @Autowired
    DailyInterestCalculationConnector dailyInterestCalculationConnector;

    @Autowired
    EODPaymentUpdateConnector eodPaymentUpdateConnector;

    @Autowired
    SnapShotConnector snapShotConnector;

    @Autowired
    TxnDropRequestConnector txnDropRequestConnector;

    @Autowired
    EasyPaymentConnector easyPaymentConnector;

    @Autowired
    BalanceTransferConnector balanceTransferConnector;

    @Autowired
    EOMSupplementaryCardResetConnector eomSupplementaryCardResetConnector;

    @KafkaListener(topics = "initialProcess", groupId = "group_id")
    public void initialProcessConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Initial Process");
        initialConnector.startProcess(Configurations.PROCESS_ID_INITIAL_PROCESS, uniqueID);
        System.out.println("Complete Initial Process");
    }

    @KafkaListener(topics = "txnDropRequest", groupId = "group_txnDropRequest")
    public void txnDropRequest(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Txn Drop Request Process");
        txnDropRequestConnector.startProcess(Configurations.PROCESS_TRANSACTION_DROP_REQUEST, uniqueID);
        System.out.println("Complete Txn Drop Request Process");
    }

    @KafkaListener(topics = "cardReplace", groupId = "group_cardReplace")
    public void cardReplaceConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start card Replace Process");
        cardReplaceConnector.startProcess(Configurations.PROCESS_ID_CARD_REPLACE, uniqueID);
        System.out.println("Complete card Replace Process");
    }

    @KafkaListener(topics = "eodParameterReset", groupId = "group_parameterReset")
    public void eodParameterResetConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Eod Parameter Reset Process");
        eodParameterResetConnector.startProcess(Configurations.PROCESS_ID_EOD_PARAMETER_RESET, uniqueID);
        System.out.println("Complete Eod Parameter Reset Process");
    }

    @KafkaListener(topics = "eodAtmCashAdvanceUpdate", groupId = "group_eodAtmCashAdvanceUpdate")
    public void eodAtmCashAdvanceUpdateConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Eod Parameter Reset Process");
        cashAdvanceUpdateConnector.startProcess(Configurations.PROCESS_EODCASHADVANCEUPDATE, uniqueID);
        System.out.println("Complete Eod Parameter Reset Process");
    }

    @KafkaListener(topics = "cardAdjustment", groupId = "group_cardAdjustment")
    public void adjustmentConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Adjustment Process");
        adjustmentConnector.startProcess(Configurations.ADJUSTMENT_PROCESS, uniqueID);
        System.out.println("Complete Adjustment Process");
    }

    @KafkaListener(topics = "cardFeeTest", groupId = "group_cardFeeTest")
    public void cardFeeConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Card Fee Process");
        cardFeeConnector.startProcess(Configurations.PROCESS_CARD_FEE, uniqueID);
        System.out.println("Complete Card Fee Process");
    }

    @KafkaListener(topics = "feePost", groupId = "group_feePost")
    public void feePostConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Fee Post Process");
        feePostConnector.startProcess(Configurations.PROCESS_ID_FEE_POST, uniqueID);
        System.out.println("Complete Fee Post Process");
    }

    @KafkaListener(topics = "cardTemporaryBlock", groupId = "group_cardTemporaryBlock")
    public void cardTemporaryBlockConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Card Temporary Block Process");
        cardTemporaryBlockConnector.startProcess(Configurations.PROCESS_ID_CARD_TEMPORARY_BLOCK, uniqueID);
        System.out.println("Complete Card Temporary Block Process");
    }

    @KafkaListener(topics = "cardPermanentBlock", groupId = "group_cardPermanentBlock")
    public void cardPermanentBlockConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Card Permanent Block Process");
        cardPermanentBlockConnector.startProcess(Configurations.PROCESS_ID_CARD_PERMENANT_BLOCK, uniqueID);
        System.out.println("Complete Card Permanent Block Process");
    }

    @KafkaListener(topics = "creditLimitExpire", groupId = "group_creditLimitExpire")
    public void creditLimitExpireConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Credit Limit Expire Process");
        incrementLimitExpireConnector.startProcess(Configurations.PROCESS_ID_INCREMENT_LIMIT_EXPIRE, uniqueID);
        System.out.println("Complete Credit Limit Expire Process");
    }

    @KafkaListener(topics = "txnMismatchPost", groupId = "group_txnMismatchPost")
    public void txnMismatchPostConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Txn Mismatch Post Process");
        txnMismatchPostConnector.startProcess(Configurations.PROCESS_ID_TXNMISMATCH_POST, uniqueID);
        System.out.println("Complete Txn Mismatch Post Process");
    }

    @KafkaListener(topics = "paymentReversal", groupId = "group_paymentReversal")
    public void paymentReversalConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Payment Reversal Process");
        paymentReversalsConnector.startProcess(Configurations.PAYMENT_REVERSAL_PROCESS, uniqueID);
        System.out.println("Complete Payment Reversal Process");
    }

    @KafkaListener(topics = "cardLimitEnhancement", groupId = "group_cardLimitEnhancement")
    public void cardLimitEnhancementConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Card Limit Enhancement Process");
        cardLimitEnhancementConnector.startProcess(Configurations.Iterator_Card_Limit_Enhancement, uniqueID);
        System.out.println("Complete Card Limit Enhancement Process");
    }

    @KafkaListener(topics = "acqTxnUpdate", groupId = "group_acqTxnUpdate")
    public void acqTxnUpdateConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start AcqTxnUpdate Process");
        acqTxnUpdateConnector.startProcess(Configurations.PROCESS_ID_ACQUIRING_TXN_UPDATE_PROCESS, uniqueID);
        System.out.println("Complete AcqTxnUpdate Process");
    }

    @KafkaListener(topics = "clearMinAmountAndTempBlock", groupId = "group_clearMinAmountAndTempBlock")
    public void clearMinAmountAndTempBlockConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Clear MinAmount And Temp Block Process");
        clearMinAmountAndTempBlockConnector.startProcess(Configurations.PROCESS_CLEAR_MINPAYMENTS_AND_TEMPBLOCK, uniqueID);
        System.out.println("Complete Clear MinAmount And Temp Block Process");
    }

    @KafkaListener(topics = "collectionAndRecoveryNotification", groupId = "group_collectionAndRecoveryNotification")
    public void collectionAndRecoveryConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Collection And Recovery Notification Process");
        collectionAndRecoveryConnector.startProcess(Configurations.COLLECTION_AND_RECOVERY_NOTIFICATION, uniqueID);
        System.out.println("Complete Collection And Recovery Notification Process");
    }

    @KafkaListener(topics = "stampDutyFee", groupId = "group_stampDutyFee")
    public void StampDutyFeeConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Collection And Recovery Notification Process");
        stampDutyFeeConnector.startProcess(Configurations.PROCESS_STAMP_DUTY_FEE, uniqueID);
        System.out.println("Complete Collection And Recovery Notification Process");
    }

    @KafkaListener(topics = "cardExpire", groupId = "group_cardExpire")
    public void cardExpireConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start card Expire Process");
        cardExpireConnector.startProcess(Configurations.PROCESS_CARD_EXPIRE, uniqueID);
        System.out.println("Complete card Expire Process");
    }

    @KafkaListener(topics = "overLimitFee", groupId = "group_overLimitFee")
    public void overLimitFeeConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start over Limit Fee Process");
        overLimitFeeConnector.startProcess(Configurations.PROCESS_ID_OVER_LIMIT_FEE, uniqueID);
        System.out.println("Complete over Limit Fee Process");
    }

    @KafkaListener(topics = "chequePayment", groupId = "group_chequePayment")
    public void chequePaymentConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start cheque Payment Process");
        chequePaymentConnector.startProcess(Configurations.PROCESS_ID_CHEQUEPAYMENT, uniqueID);
        System.out.println("Complete chequePayment Process");
    }

    @KafkaListener(topics = "checkPaymentForMinimumAmount", groupId = "group_checkPaymentForMinimumAmount")
    public void checkPaymentForMinimumAmountConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Check Payment For Minimum Amount Process");
        checkPaymentForMinimumAmountConnector.startProcess(Configurations.PROCESS_CHECK_PAYMENTS_FOR_MIN_AMOUNT, uniqueID);
        System.out.println("Complete Check Payment For Minimum Amount Process");
    }

    @KafkaListener(topics = "cribFile", groupId = "group_cribFile")
    public void cribFileConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start CRIB File Process");
        cribFileConnector.startProcess(Configurations.PROCESS_CRIB_FILE, uniqueID);
        System.out.println("Complete CRIB File Process");
    }

    @KafkaListener(topics = "cashBackAlert", groupId = "group_cashBackAlert")
    public void cashBackAlertConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Cash Back Alert Process");
        cashBackAlertConnector.startProcess(Configurations.PROCESS_ID_CASH_BACK_ALERT_PROCESS, uniqueID);
        System.out.println("Complete Cash Back Alert Process");
    }

    @KafkaListener(topics = "collectionAndRecoveryAlert", groupId = "group_collectionAndRecoveryAlert")
    public void collectionAndRecoveryAlertConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Collection And Recovery Alert Process");
        collectionAndRecoveryAlertConnector.startProcess(Configurations.PROCESS_ID_COLLECTION_AND_RECOVERY_ALERT_PROCESS, uniqueID);
        System.out.println("Complete Collection And Recovery Alert Process");
    }

    @KafkaListener(topics = "transactionUpdate", groupId = "group_transactionUpdate")
    public void transactionUpdateConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start EOD Transaction Update Process");
        transactionUpdateConnector.startProcess(Configurations.PROCESS_EODTRANSACTIONUPDATE, uniqueID);
        System.out.println("Complete EOD Transaction Update Process");
    }

    @KafkaListener(topics = "loanOnCard", groupId = "group_loanOnCard")
    public void loanOnCardConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Loan On Card Process");
        loanOnCardConnector.startProcess(Configurations.PROCESS_ID_LOAN_ON_CARD, uniqueID);
        System.out.println("Complete Loan On Card Process");
    }

    @KafkaListener(topics = "chequeReturn", groupId = "group_chequeReturn")
    public void chequeReturnConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Cheque Return Process");
        chequeReturnConnector.startProcess(Configurations.PROCESS_CHEQUERETURN, uniqueID);
        System.out.println("Complete Cheque Return Process");
    }

    @KafkaListener(topics = "runnableFee", groupId = "group_runnableFee")
    public void runnableFeeConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start EOD Runnable Fee Process");
        runnableFeeConnector.startProcess(Configurations.PROCESS_EOD_RUNNABLE_FEE, uniqueID);
        System.out.println("Complete EOD Runnable Fee Process");
    }

    @KafkaListener(topics = "cashBack", groupId = "group_cashBack")
    public void cashBackConsumer(String uniqueID) throws Exception {
        System.out.println("Start EOD Runnable Fee Process");
        cashBackConnector.startProcess(Configurations.PROCESS_CASHBACK, uniqueID);
        System.out.println("Complete EOD Runnable Fee Process");
    }

    @KafkaListener(topics = "knockOff", groupId = "group_knockOff")
    public void knockOffConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start EOD Runnable Fee Process");
        knockOffConnector.startProcess(Configurations.PROCESS_ID_KNOCK_OFF, uniqueID);
        System.out.println("Complete EOD Runnable Fee Process");
    }

    @KafkaListener(topics = "manualNpProcess", groupId = "group_manualNpProcess")
    public void manualNpConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Manual Np Process");
        manualNpConnector.startProcess(Configurations.PROCESS_ID_MANUAL_NP_PROCESS, uniqueID);
        System.out.println("Complete Manual Np Process");
    }

    @KafkaListener(topics = "onlineToBackendTxnProcess", groupId = "group_onlineToBackendTxnProcess")
    public void onlineToBackendTxnConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Online To Backend Txn Syn Process");
        onlineToBackendTxnConnector.startProcess(Configurations.PROCESS_ONLINETOBACKEND_TXNSYNC, uniqueID);
        System.out.println("Complete Online To Backend Txn Syn Process");
    }

    @KafkaListener(topics = "eomInterestCalculationProcess", groupId = "group_eomInterestCalculationProcess")
    public void EOMInterestCalculationConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start EOM Interest Calculation Process");
        eomInterestConnector.startProcess(Configurations.PROCESS_ID_EOM_INTEREST_CALCULATION, uniqueID);
        System.out.println("Complete EOM Interest Calculation Process");
    }

    @KafkaListener(topics = "monthlyStatement", groupId = "group_monthlyStatement")
    public void MonthlyStatement(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Monthly Statement Process");
        monthlyStatementConnector.startProcess(Configurations.PROCESS_MONTHLY_STATEMENT, uniqueID);
        System.out.println("Complete Monthly Statement Process");
    }

    @KafkaListener(topics = "dailyInterestCalculation", groupId = "group_dailyInterestCalculation")
    public void DailyInterestCalculation(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Daily Interest Calculation Process");
        dailyInterestCalculationConnector.startProcess(Configurations.PROCESS_DAILY_INTEREST_CALCULATION, uniqueID);
        System.out.println("Complete Daily Interest Calculation Process");
    }

    @KafkaListener(topics = "eodPaymentUpdate", groupId = "group_eodPaymentUpdate")
    public void EODPaymentUpdateConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start EOD Payment Update Process");
        eodPaymentUpdateConnector.startProcess(Configurations.PROCESS_EODPAYMENTUPDATE, uniqueID);
        System.out.println("Complete EOD Payment Update Process");
    }

    @KafkaListener(topics = "dailySnapShot", groupId = "group_dailySnapShot")
    public void SnapShotProcessConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Daily SnapShot Process");
        snapShotConnector.startProcess(Configurations.PROCESS_ID_SNAPSHOT, uniqueID);
        System.out.println("Complete Daily SnapShot Process");
    }

    @KafkaListener(topics = "easyPayment", groupId = "group_easyPayment")
    public void easyPayment(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Easy Payment Process");
        easyPaymentConnector.startProcess(Configurations.PROCESS_ID_EASY_PAYMENT, uniqueID);
        System.out.println("Complete Easy Payment Process");
    }

    @KafkaListener(topics = "balanceTransfer", groupId = "group_balanceTransfer")
    public void balanceTransfer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Balance Transfer Process");
        balanceTransferConnector.startProcess(Configurations.PROCESS_ID_BALANCE_TRANSFER, uniqueID);
        System.out.println("Complete Balance Transfer Process");
    }

    @KafkaListener(topics = "riskCalculation", groupId = "group_riskCalculation")
    public void RiskCalculationConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Risk Calculation Process");
        riskCalculationConnector.startProcess(Configurations.PROCESS_ID_RISK_CALCULATION_PROCESS, uniqueID);
        System.out.println("Complete Risk Calculation Process");
    }

    @KafkaListener(topics = "cardRenew", groupId = "group_cardRenew")
    public void CardRenewConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Card Renew Process");
        cardRenewConnector.startProcess(Configurations.PROCESS_CARD_RENEW, uniqueID);
        System.out.println("Complete Card Renew Process");
    }

    @KafkaListener(topics = "transactionPost", groupId = "group_transactionPost")
    public void TransactionPostConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Transaction Post Process");
        transactionPostConnector.startProcess(Configurations.PROCESS_ID_TXN_POST, uniqueID);
        System.out.println("Complete Transaction Post Process");
    }

    @KafkaListener(topics = "eomSupplementaryCardReset", groupId = "group_eomSupplementaryCardReset")
    public void EOMSupplementaryCardResetConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start EOM Supplementary Card Reset Process");
        eomSupplementaryCardResetConnector.startProcess(Configurations.PROCESS_EOM_SUP_CARD_RESET, uniqueID);
        System.out.println("Complete EOM Supplementary Card Reset Process");
    }

//    @KafkaListener(topics = "logTopic", groupId = "group_id_logs", containerFactory = "kafkaListenerContainerFactory")
//    public void LoggerConsumer(Logger message) throws Exception {
//        System.out.println("log Topic Message..........");
//    }
}
