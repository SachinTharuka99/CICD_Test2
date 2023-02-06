package com.epic.cms.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@RefreshScope
@Configuration
@ConfigurationProperties
@Getter
@Setter
public class QueryParametersList {

    @Value("${Initial.Update.swapEodCardBalance}")
    public String InitialUpdateSwapEodCardBalance;

    @Value("${Initial.Update.insertIntoAccountBalance}")
    public String InitialUpdateInsertIntoAccountBalance;

    @Value("${Initial.Update.setResetCapsLimit}")
    public String InitialUpdateSetResetCapsLimit;

    @Value("${Initial.Update.setResetCapsLimitAccount}")
    public String InitialUpdateSetResetCapsLimitAccount;

    @Value("${Common.Select.getProcessDetails}")
    public String CommonSelectGetProcessDetails;

    @Value("${Common.Insert.insertToEodProcessSumery}")
    public String CommonInsertToEodProcessSumery;

    @Value("${Common.Update.eodProcessSummery}")
    public String CommonUpdateEodProcessSummery;

    @Value("${Common.Select.getMainCardNumber}")
    public String Common_getMainCardNumber;

    @Value("${Common.Insert.insertToEODTransaction}")
    public String Common_insertToEODTransaction;

    @Value("${Common.Insert.insertIntoEodMerchantTransaction}")
    public String Common_insertIntoEodMerchantTransaction;

    @Value("${Common.Update.updateTransactionToEDON}")
    public String Common_updateTransactionToEDON;

    @Value("${Common.Select.getNewCardNumber}")
    public String Common_getNewCardNumber;

    @Value("${Common.Update.updateCardOtb}")
    public String Common_updateCardOtb;

    @Value("${Common.Update.updateAccountOtb}")
    public String Common_updateAccountOtb;

    @Value("${Common.Update.updateCustomerOtb}")
    public String Common_updateCustomerOtb;

    @Value("${EodMain.Select.loadFilePath}")
    public String EodMainSelectLoadFilePath;

    @Value("${EodMain.Select.loadTxnTypeConfigurations}")
    public String EodMainSelectLoadTxnTypeConfigurations;

    @Value("${EodParamReset.Update.resetMerchantParameters}")
    public String EodParamResetUpdateResetMerchantParameters;

    @Value("${EodParamReset.Update.resetTerminalParameters}")
    public String EodParamResetUpdateResetTerminalParameters;

    @Value("${CardReplace.Select.getCardListToReplace}")
    public String CardReplace_getCardListToReplace;

    @Value("${CardReplace.Update.updateCardReplaceStatus}")
    public String CardReplace_updateCardReplaceStatus;

    @Value("${Adjustment.Select.getAdjustmentList}")
    public String Adjustment_getAdjustmentList;

    @Value("${Adjustment.Insert.insertToEODPayments}")
    public String Adjustment_insertToEODPayments;

    @Value("${Adjustment.Select.getCardAssociationFromCardBin}")
    public String Adjustment_getCardAssociationFromCardBin;

    @Value("${Adjustment.Insert.insertInToEODTransaction.AdTypeNotNull}")
    public String Adjustment_insertInToEODTransaction_AdTypeNotNull;

    @Value("${Adjustment.Insert.insertInToEODTransaction.AdTypeNull}")
    public String Adjustment_insertInToEODTransaction_AdTypeNull;

    @Value("${Adjustment.Update.updateAdjustmentStatus}")
    public String Adjustment_updateAdjustmentStatus;

    @Value("${Adjustment.Update.updateTransactionToEDON}")
    public String Adjustment_updateTransactionToEDON;

    @Value("${CardFee.Select.getCardFeeCountList}")
    public String CardFee_getCardFeeCountList;

    @Value("${CardFee.Select.getCardFeeCountForCard}")
    public String CardFee_getCardFeeCountForCard;

    @Value("${CardFee.Select.getNextBillingDateForCard}")
    public String CardFee_getNextBillingDateForCard;

    @Value("${CardFee.Insert.insertToEODCardFee}")
    public String CardFee_insertToEODCardFee;

    @Value("${CardFee.Update.updateCardFeeCount}")
    public String CardFee_updateCardFeeCount;

    @Value("${CardFee.Update.updateDELINQUENTACCOUNTNpDetails}")
    public String CardFee_updateDELINQUENTACCOUNTNpDetails;


    @Value("${AcqTxnUpdate.Select.getAllSettledTxnFromTxn}")
    public String AcqTxnUpdate_getAllSettledTxnFromTxn;

    @Value("${AcqTxnUpdate.Select.getForexPercentage}")
    public String AcqTxnUpdate_getForexPercentage;

    @Value("${AcqTxnUpdate.Select.getFuelSurchargeRatePercentage}")
    public String AcqTxnUpdate_getFuelSurchargeRatePercentage;

    @Value("${AcqTxnUpdate.Select.getFuelMccList}")
    public String AcqTxnUpdate_getFuelMccList;

    @Value("${AcqTxnUpdate.Select.getFinancialStatus}")
    public String AcqTxnUpdate_getFinancialStatus;

    @Value("${PaymentReversal.Select.getPaymentReversals}")
    public String PaymentReversal_getPaymentReversals;

    @Value("${PaymentReversal.Update.insertPaymentsForCashReversals}")
    public String PaymentReversal_insertPaymentsForCashReversals;

    @Value("${PaymentReversal.Update.updatePaymentsForCashReversals}")
    public String PaymentReversal_updatePaymentsForCashReversals;

    @Value("${TxnMismatchPost.Select.getInitEodTxnMismatchPostCustAcc}")
    public String TxnMismatchPost_getInitEodTxnMismatchPostCustAcc;

    @Value("${TxnMismatchPost.Select.getErrorEodTxnMismatchPostCustAcc}")
    public String TxnMismatchPost_getErrorEodTxnMismatchPostCustAcc;

    @Value("${TxnMismatchPost.Select.getInitTxnMismatch}")
    public String TxnMismatchPost_getInitTxnMismatch;

    @Value("${ATMFileClearing.Insert.RecInputRowData}")
    public String ATMFileClearingInsertRecInputRowData;

    @Value("${PaymentFileClearing.Insert.RecInputRowData}")
    public String PaymentFileClearingInsertRecInputRowData;

    @Value("${VisaFileClearing.Insert.RecInputRowData}")
    public String VisaFileClearingInsertRecInputRowData;
}
