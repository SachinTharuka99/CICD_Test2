package com.epic.cms.service;

import com.epic.cms.connector.*;
import com.epic.cms.repository.CashBackAlertRepo;
import com.epic.cms.util.Configurations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

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
    public void initialProcessConsumer(String message) throws Exception {
        Configurations.eodUniqueId = message;
        System.out.println("Start Initial Process");
        initialConnector.startProcess();
        System.out.println("Complete Initial Process");
    }

    @KafkaListener(topics = "txnDropRequest", groupId = "group_txnDropRequest")
    public void txnDropRequest(String message) throws Exception {
        System.out.println("Start Txn Drop Request Process");
        txnDropRequestConnector.startProcess();
        System.out.println("Complete Txn Drop Request Process");
    }

    @KafkaListener(topics = "cardReplace", groupId = "group_cardReplace")
    public void cardReplaceConsumer(String message) throws Exception {
        System.out.println("Start card Replace Process");
        cardReplaceConnector.startProcess();
        System.out.println("Complete card Replace Process");
    }

    @KafkaListener(topics = "eodParameterReset", groupId = "group_parameterReset")
    public void eodParameterResetConsumer(String message) throws Exception {
        System.out.println("Start Eod Parameter Reset Process");
        eodParameterResetConnector.startProcess();
        System.out.println("Complete Eod Parameter Reset Process");
    }

    @KafkaListener(topics = "eodAtmCashAdvanceUpdate", groupId = "group_eodAtmCashAdvanceUpdate")
    public void eodAtmCashAdvanceUpdateConsumer(String message) throws Exception {
        System.out.println("Start Eod Parameter Reset Process");
        cashAdvanceUpdateConnector.startProcess();
        System.out.println("Complete Eod Parameter Reset Process");
    }

    @KafkaListener(topics = "cardAdjustment", groupId = "group_cardAdjustment")
    public void adjustmentConsumer(String msg) throws Exception {
        System.out.println("Start Adjustment Process");
        adjustmentConnector.startProcess();
        System.out.println("Complete Adjustment Process");
    }

    @KafkaListener(topics = "cardFeeTest", groupId = "group_cardFeeTest")
    public void cardFeeConsumer(String msg) throws Exception {
        System.out.println("Start Card Fee Process");
        cardFeeConnector.startProcess();
        System.out.println("Complete Card Fee Process");
    }

    @KafkaListener(topics = "feePost", groupId = "group_feePost")
    public void feePostConsumer(String msg) throws Exception {
        System.out.println("Start Fee Post Process");
        feePostConnector.startProcess();
        System.out.println("Complete Fee Post Process");
    }

    @KafkaListener(topics = "cardTemporaryBlock", groupId = "group_cardTemporaryBlock")
    public void cardTemporaryBlockConsumer(String msg) throws Exception {
        System.out.println("Start Card Temporary Block Process");
        cardTemporaryBlockConnector.startProcess();
        System.out.println("Complete Card Temporary Block Process");
    }

    @KafkaListener(topics = "cardPermanentBlock", groupId = "group_cardPermanentBlock")
    public void cardPermanentBlockConsumer(String msg) throws Exception {
        System.out.println("Start Card Permanent Block Process");
        cardPermanentBlockConnector.startProcess();
        System.out.println("Complete Card Permanent Block Process");
    }

    @KafkaListener(topics = "creditLimitExpire", groupId = "group_creditLimitExpire")
    public void creditLimitExpireConsumer(String msg) throws Exception {
        System.out.println("Start Credit Limit Expire Process");
        incrementLimitExpireConnector.startProcess();
        System.out.println("Complete Credit Limit Expire Process");
    }

    @KafkaListener(topics = "txnMismatchPost", groupId = "group_txnMismatchPost")
    public void txnMismatchPostConsumer(String msg) throws Exception {
        System.out.println("Start Txn Mismatch Post Process");
        txnMismatchPostConnector.startProcess();
        System.out.println("Complete Txn Mismatch Post Process");
    }

    @KafkaListener(topics = "paymentReversal", groupId = "group_paymentReversal")
    public void paymentReversalConsumer(String msg) throws Exception {
        System.out.println("Start Payment Reversal Process");
        paymentReversalsConnector.startProcess();
        System.out.println("Complete Payment Reversal Process");
    }

    @KafkaListener(topics = "cardLimitEnhancement", groupId = "group_cardLimitEnhancement")
    public void cardLimitEnhancementConsumer(String msg) throws Exception {
        System.out.println("Start Card Limit Enhancement Process");
        cardLimitEnhancementConnector.startProcess();
        System.out.println("Complete Card Limit Enhancement Process");
    }

    @KafkaListener(topics = "acqTxnUpdate", groupId = "group_acqTxnUpdate")
    public void acqTxnUpdateConsumer(String msg) throws Exception {
        System.out.println("Start AcqTxnUpdate Process");
        acqTxnUpdateConnector.startProcess();
        System.out.println("Complete AcqTxnUpdate Process");
    }

    @KafkaListener(topics = "clearMinAmountAndTempBlock", groupId = "group_clearMinAmountAndTempBlock")
    public void clearMinAmountAndTempBlockConsumer(String msg) throws Exception {
        System.out.println("Start Clear MinAmount And Temp Block Process");
        clearMinAmountAndTempBlockConnector.startProcess();
        System.out.println("Complete Clear MinAmount And Temp Block Process");
    }

    @KafkaListener(topics = "collectionAndRecoveryNotification", groupId = "group_collectionAndRecoveryNotification")
    public void collectionAndRecoveryConsumer(String msg) throws Exception {
        System.out.println("Start Collection And Recovery Notification Process");
        collectionAndRecoveryConnector.startProcess();
        System.out.println("Complete Collection And Recovery Notification Process");
    }

    @KafkaListener(topics = "stampDutyFee", groupId = "group_stampDutyFee")
    public void StampDutyFeeConsumer(String msg) throws Exception {
        System.out.println("Start Collection And Recovery Notification Process");
        stampDutyFeeConnector.startProcess();
        System.out.println("Complete Collection And Recovery Notification Process");
    }

    @KafkaListener(topics = "cardExpire", groupId = "group_cardExpire")
    public void cardExpireConsumer(String msg) throws Exception {
        System.out.println("Start card Expire Process");
        cardExpireConnector.startProcess();
        System.out.println("Complete card Expire Process");
    }

    @KafkaListener(topics = "overLimitFee", groupId = "group_overLimitFee")
    public void overLimitFeeConsumer(String msg) throws Exception {
        System.out.println("Start over Limit Fee Process");
        overLimitFeeConnector.startProcess();
        System.out.println("Complete over Limit Fee Process");
    }

    @KafkaListener(topics = "chequePayment", groupId = "group_chequePayment")
    public void chequePaymentConsumer(String msg) throws Exception {
        System.out.println("Start cheque Payment Process");
        chequePaymentConnector.startProcess();
        System.out.println("Complete chequePayment Process");
    }

    @KafkaListener(topics = "checkPaymentForMinimumAmount", groupId = "group_checkPaymentForMinimumAmount")
    public void checkPaymentForMinimumAmountConsumer(String msg) throws Exception {
        System.out.println("Start Check Payment For Minimum Amount Process");
        checkPaymentForMinimumAmountConnector.startProcess();
        System.out.println("Complete Check Payment For Minimum Amount Process");
    }

    @KafkaListener(topics = "cribFile", groupId = "group_cribFile")
    public void cribFileConsumer(String msg) throws Exception {
        System.out.println("Start CRIB File Process");
        cribFileConnector.startProcess();
        System.out.println("Complete CRIB File Process");
    }

    @KafkaListener(topics = "cashBackAlert", groupId = "group_cashBackAlert")
    public void cashBackAlertConsumer(String msg) throws Exception {
        System.out.println("Start Cash Back Alert Process");
        cashBackAlertConnector.startProcess();
        System.out.println("Complete Cash Back Alert Process");
    }

    @KafkaListener(topics = "collectionAndRecoveryAlert", groupId = "group_collectionAndRecoveryAlert")
    public void collectionAndRecoveryAlertConsumer(String msg) throws Exception {
        System.out.println("Start Collection And Recovery Alert Process");
        collectionAndRecoveryAlertConnector.startProcess();
        System.out.println("Complete Collection And Recovery Alert Process");
    }

    @KafkaListener(topics = "transactionUpdate", groupId = "group_transactionUpdate")
    public void transactionUpdateConsumer(String msg) throws Exception {
        System.out.println("Start EOD Transaction Update Process");
        transactionUpdateConnector.startProcess();
        System.out.println("Complete EOD Transaction Update Process");
    }

    @KafkaListener(topics = "loanOnCard", groupId = "group_loanOnCard")
    public void loanOnCardConsumer(String msg) throws Exception {
        System.out.println("Start Loan On Card Process");
        loanOnCardConnector.startProcess();
        System.out.println("Complete Loan On Card Process");
    }

    @KafkaListener(topics = "chequeReturn", groupId = "group_chequeReturn")
    public void chequeReturnConsumer(String msg) throws Exception {
        System.out.println("Start Cheque Return Process");
        chequeReturnConnector.startProcess();
        System.out.println("Complete Cheque Return Process");
    }

    @KafkaListener(topics = "runnableFee", groupId = "group_runnableFee")
    public void runnableFeeConsumer(String msg) throws Exception {
        System.out.println("Start EOD Runnable Fee Process");
        runnableFeeConnector.startProcess();
        System.out.println("Complete EOD Runnable Fee Process");
    }

    @KafkaListener(topics = "cashBack", groupId = "group_cashBack")
    public void cashBackConsumer(String msg) throws Exception {
        System.out.println("Start EOD Runnable Fee Process");
        cashBackConnector.startProcess();
        System.out.println("Complete EOD Runnable Fee Process");
    }

    @KafkaListener(topics = "knockOff", groupId = "group_knockOff")
    public void knockOffConsumer(String msg) throws Exception {
        System.out.println("Start EOD Runnable Fee Process");
        knockOffConnector.startProcess();
        System.out.println("Complete EOD Runnable Fee Process");
    }

    @KafkaListener(topics = "manualNpProcess", groupId = "group_manualNpProcess")
    public void manualNpConsumer(String msg) throws Exception {
        Configurations.eodUniqueId = msg;
        System.out.println("Start Manual Np Process");
        manualNpConnector.startProcess();
        System.out.println("Complete Manual Np Process");
    }

    @KafkaListener(topics = "onlineToBackendTxnProcess", groupId = "group_onlineToBackendTxnProcess")
    public void onlineToBackendTxnConsumer(String msg) throws Exception {
        System.out.println("Start Online To Backend Txn Syn Process");
        onlineToBackendTxnConnector.startProcess();
        System.out.println("Complete Online To Backend Txn Syn Process");
    }

  @KafkaListener(topics = "eomInterestCalculationProcess", groupId = "group_eomInterestCalculationProcess")
    public void EOMInterestCalculationConsumer(String msg) throws Exception {
        System.out.println("Start EOM Interest Calculation Process");
        eomInterestConnector.startProcess();
        System.out.println("Complete EOM Interest Calculation Process");
    }

    @KafkaListener(topics = "monthlyStatement", groupId = "group_monthlyStatement")
    public void MonthlyStatement(String msg) throws Exception {
        System.out.println("Start Monthly Statement Process");
        monthlyStatementConnector.startProcess();
        System.out.println("Complete Monthly Statement Process");
    }

    @KafkaListener(topics = "dailyInterestCalculation", groupId = "group_dailyInterestCalculation")
    public void DailyInterestCalculation(String msg) throws Exception {
        System.out.println("Start Daily Interest Calculation Process");
        dailyInterestCalculationConnector.concreteProcess();
        System.out.println("Complete Daily Interest Calculation Process");
    }

    @KafkaListener(topics = "eodPaymentUpdate", groupId = "group_eodPaymentUpdate")
    public void EODPaymentUpdateConsumer(String message) throws Exception {
        System.out.println("Start EOD Payment Update Process");
        eodPaymentUpdateConnector.startProcess();
        System.out.println("Complete EOD Payment Update Process");
    }

    @KafkaListener(topics = "dailySnapShot", groupId = "group_dailySnapShot")
    public void SnapShotProcessConsumer(String message) throws Exception {
        System.out.println("Start Daily SnapShot Process");
        snapShotConnector.startProcess();
        System.out.println("Complete Daily SnapShot Process");
    }

    @KafkaListener(topics = "easyPayment", groupId = "group_easyPayment")
    public void easyPayment(String msg) throws Exception {
        System.out.println("Start Easy Payment Process");
        easyPaymentConnector.startProcess();
        System.out.println("Complete Easy Payment Process");
    }

    @KafkaListener(topics = "balanceTransfer", groupId = "group_balanceTransfer")
    public void balanceTransfer(String msg) throws Exception {
        System.out.println("Start Balance Transfer Process");
        balanceTransferConnector.startProcess();
        System.out.println("Complete Balance Transfer Process");
    }

    @KafkaListener(topics = "riskCalculation", groupId = "group_riskCalculation")
    public void RiskCalculationConsumer(String msg) throws Exception {
        System.out.println("Start Risk Calculation Process");
        riskCalculationConnector.startProcess();
        System.out.println("Complete Risk Calculation Process");
    }

    @KafkaListener(topics = "cardRenew", groupId = "group_cardRenew")
    public void CardRenewConsumer(String msg) throws Exception {
        System.out.println("Start Card Renew Process");
        cardRenewConnector.startProcess();
        System.out.println("Complete Card Renew Process");
    }

    @KafkaListener(topics = "transactionPost", groupId = "group_transactionPost")
    public void TransactionPostConsumer(String msg) throws Exception {
        System.out.println("Start Transaction Post Process");
        transactionPostConnector.startProcess();
        System.out.println("Complete Transaction Post Process");
    }

    @KafkaListener(topics = "eomSupplementaryCardReset", groupId = "group_eomSupplementaryCardReset")
    public void EOMSupplementaryCardResetConsumer(String msg) throws Exception {
        System.out.println("Start EOM Supplementary Card Reset Process");
        eomSupplementaryCardResetConnector.startProcess();
        System.out.println("Complete EOM Supplementary Card Reset Process");
    }

}
