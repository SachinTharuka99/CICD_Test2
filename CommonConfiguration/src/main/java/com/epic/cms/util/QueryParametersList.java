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

    @Value("${CardExpire.Select.getCardExpireList}")
    public String CardExpire_getCardExpireList;

    @Value("${CardExpire.Update.setCardStatusToExpire}")
    public String CardExpire_setCardStatusToExpire;

    @Value("${CardExpire.Update.setOnlineCardStatusToExpire}")
    public String CardExpire_setOnlineCardStatusToExpire;

    @Value("${CardExpire.Insert.insertToCardBlock}")
    public String CardExpire_insertToCardBlock;




    @Value("${CardInterestCalculation.Select.getLatestStatementAccountList}")
    public String CardInterestCalculation_getLatestStatementAccountList;

    @Value("${CardInterestCalculation.Select.getIntProf}")
    public String CardInterestCalculation_getIntProf;

    @Value("${CardInterestCalculation.Select.getTxnOrPaymentDetailByAccount}")
    public String CardInterestCalculation_getTxnOrPaymentDetailByAccount;

    @Value("${CardInterestCalculation.Select.updateEodInterest_Select}")
    public String CardInterestCalculation_updateEodInterest_Select;

    @Value("${CardInterestCalculation.Update.updateEodInterest_Update}")
    public String CardInterestCalculation_updateEodInterest_Update;

    @Value("${CardInterestCalculation.Insert.updateEodInterest_Insert}")
    public String CardInterestCalculation_updateEodInterest_Insert;




    @Value("${CardLimitEnhancement.Select.getInitLimitEnhanceCustAcc}")
    public String CardLimitEnhancement_getInitLimitEnhanceCustAcc;

    @Value("${CardLimitEnhancement.Select.getErrorLimitEnhanceCustAcc}")
    public String CardLimitEnhancement_getErrorLimitEnhanceCustAcc;

    @Value("${CardLimitEnhancement.Select.getLimitEnhanceReqConCardList}")
    public String CardLimitEnhancement_getLimitEnhanceReqConCardList;

    @Value("${CardLimitEnhancement.Update.updateCardCreditLimit}")
    public String CardLimitEnhancement_updateCardCreditLimit;

    @Value("${CardLimitEnhancement.Update.updateOnlineCardCreditLimit}")
    public String CardLimitEnhancement_updateOnlineCardCreditLimit;

    @Value("${CardLimitEnhancement.Update.updateAccountCreditLimit}")
    public String CardLimitEnhancement_updateAccountCreditLimit;

    @Value("${CardLimitEnhancement.Update.updateOnlineAccountCreditLimit}")
    public String CardLimitEnhancement_updateOnlineAccountCreditLimit;

    @Value("${CardLimitEnhancement.Update.updateCustomerCreditLimit}")
    public String CardLimitEnhancement_updateCustomerCreditLimit;

    @Value("${CardLimitEnhancement.Update.updateOnlineCustomerCreditLimit}")
    public String CardLimitEnhancement_updateOnlineCustomerCreditLimit;

    @Value("${CardLimitEnhancement.Update.updateCardCashLimit}")
    public String CardLimitEnhancement_updateCardCashLimit;

    @Value("${CardLimitEnhancement.Update.updateOnlineCardCashLimit}")
    public String CardLimitEnhancement_updateOnlineCardCashLimit;

    @Value("${CardLimitEnhancement.Update.updateAccountCashLimit}")
    public String CardLimitEnhancement_updateAccountCashLimit;

    @Value("${CardLimitEnhancement.Update.updateOnlineAccountCashLimit}")
    public String CardLimitEnhancement_updateOnlineAccountCashLimit;

    @Value("${CardLimitEnhancement.Update.updateCustomerCashLimit}")
    public String CardLimitEnhancement_updateCustomerCashLimit;

    @Value("${CardLimitEnhancement.Update.updateOnlineCustomerCashLimit}")
    public String CardLimitEnhancement_updateOnlineCustomerCashLimit;

    @Value("${CardLimitEnhancement.Update.updateTempLimitIncrementTable}")
    public String CardLimitEnhancement_updateTempLimitIncrementTable;



    @Value("${CardRenew.Select.getCardValidityPeriod}")
    public String CardRenew_getCardValidityPeriod;

    @Value("${CardRenew.Update.updateCardTable}")
    public String CardRenew_updateCardTable;

    @Value("${CardRenew.Update.updateCardTable_2}")
    public String CardRenew_updateCardTable_2;

    @Value("${CardRenew.Update.updateCardRenewTable}")
    public String CardRenew_updateCardRenewTable;

    @Value("${CardRenew.Update.updateOnlineCardTable}")
    public String CardRenew_updateOnlineCardTable;

    @Value("${CardRenew.Select.getApprovedCardList}")
    public String CardRenew_getApprovedCardList;

    @Value("${CardRenew.Select.isProcessCompletlyFail}")
    public String CardRenew_isProcessCompletlyFail;



    @Value("${CashBackAlert.Select.getConfirmedAccountToAlert}")
    public String CashBackAlert_getConfirmedAccountToAlert;

    @Value("${CashBackAlert.Update.updateCashBackAlertGenStatus}")
    public String CashBackAlert_updateCashBackAlertGenStatus;

    @Value("${CashBackAlert.Update.updateBillingStatementAlertGenStatus}")
    public String CashBackAlert_updateBillingStatementAlertGenStatus;



    @Value("${CashBack.Select.getProcessDetails}")
    public String CashBack_getProcessDetails;

    @Value("${CashBack.Select.loadInitialConfigurationsForCashback}")
    public String CashBack_loadInitialConfigurationsForCashback;

    @Value("${CashBack.Select.getEligibleAccountsForCashback}")
    public String CashBack_getEligibleAccountsForCashback;

    @Value("${CashBack.Select.getEligibleAccountsForCashback_appender1}")
    public String CashBack_getEligibleAccountsForCashback_appender1;

    @Value("${CashBack.Select.getEligibleAccountsForCashback_appender2}")
    public String CashBack_getEligibleAccountsForCashback_appender2;

    @Value("${CashBack.Select.getCashbackAmount}")
    public String CashBack_getCashbackAmount;

    @Value("${CashBack.Select.getCashbackAmount_appender1}")
    public String CashBack_getCashbackAmount_appender1;

    @Value("${CashBack.Select.getCashbackAmount_appender2}")
    public String CashBack_getCashbackAmount_appender2;

    @Value("${CashBack.Select.getCashbackAmount_appender3}")
    public String CashBack_getCashbackAmount_appender3;

    @Value("${CashBack.Select.getCashbackAdjustmentAmount}")
    public String CashBack_getCashbackAdjustmentAmount;

    @Value("${CashBack.Insert.addNewCashBack_Insert}")
    public String CashBack_addNewCashBack_Insert;

    @Value("${CashBack.Update.addNewCashBack_Update}")
    public String CashBack_addNewCashBack_Update;

    @Value("${CashBack.Update.updateCashbackAdjustmentStatus}")
    public String CashBack_updateCashbackAdjustmentStatus;

    @Value("${CashBack.Update.updateCashbackStartDate}")
    public String CashBack_updateCashbackStartDate;

    @Value("${CashBack.Select.getRedeemRequestAmount}")
    public String CashBack_getRedeemRequestAmount;

    @Value("${CashBack.Select.redeemCashbacks_Select}")
    public String CashBack_redeemCashbacks_Select;

    @Value("${CashBack.Update.redeemCashbacks_Update}")
    public String CashBack_redeemCashbacks_Update;

    @Value("${CashBack.Insert.redeemCashbacks_Insert}")
    public String CashBack_redeemCashbacks_Insert;

    @Value("${CashBack.Update.redeemCashbacks_Update_2}")
    public String CashBack_redeemCashbacks_Update_2;

    @Value("${CashBack.Update.updateEodStatusInCashbackRequest}")
    public String CashBack_updateEodStatusInCashbackRequest;

    @Value("${CashBack.Select.getRedeemableAmount_Select_1}")
    public String CashBack_getRedeemableAmount_Select_1;

    @Value("${CashBack.Select.getRedeemableAmount_Select_2}")
    public String CashBack_getRedeemableAmount_Select_2;

    @Value("${CashBack.Select.getRedeemableAmount_Select_3}")
    public String CashBack_getRedeemableAmount_Select_3;

    @Value("${CashBack.Update.updateNextCBRedeemDate_Update_1}")
    public String CashBack_updateNextCBRedeemDate_Update_1;

    @Value("${CashBack.Update.updateNextCBRedeemDate_Update_2}")
    public String CashBack_updateNextCBRedeemDate_Update_2;

    @Value("${CashBack.Update.updateNextCBRedeemDate_Update_3}")
    public String CashBack_updateNextCBRedeemDate_Update_3;

    @Value("${CashBack.Select.getCashbackAmountToBeExpireForAccount}")
    public String CashBack_getCashbackAmountToBeExpireForAccount;

    @Value("${CashBack.Update.expireNonPerformingCashbacks_Update_1}")
    public String CashBack_expireNonPerformingCashbacks_Update_1;

    @Value("${CashBack.Insert.expireNonPerformingCashbacks_Insert}")
    public String CashBack_expireNonPerformingCashbacks_Insert;

    @Value("${CashBack.Update.expireNonPerformingCashbacks_Update_2}")
    public String CashBack_expireNonPerformingCashbacks_Update_2;

    @Value("${CashBack.Update.expireCardCloseCashbacks_Update_1}")
    public String CashBack_expireCardCloseCashbacks_Update_1;

    @Value("${CashBack.Insert.expireCardCloseCashbacks_Insert}")
    public String CashBack_expireCardCloseCashbacks_Insert;

    @Value("${CashBack.Update.expireCardCloseCashbacks_Update_2}")
    public String CashBack_expireCardCloseCashbacks_Update_2;

    @Value("${CashBack.Select.expireCashbacks_Select}")
    public String CashBack_expireCashbacks_Select;

    @Value("${CashBack.Update.expireCashbacks_Update}")
    public String CashBack_expireCashbacks_Update;

    @Value("${CashBack.Insert.expireCashbacks_Insert}")
    public String CashBack_expireCashbacks_Insert;

    @Value("${CashBack.Update.updateTotalCBAmount}")
    public String CashBack_updateTotalCBAmount;



    @Value("${CheckPaymentForMinimumAmount.Select.getStatementCardList}")
    public String CheckPaymentForMinimumAmount_getStatementCardList;

    @Value("${CheckPaymentForMinimumAmount.Select.getAccountNoOnCard}")
    public String CheckPaymentForMinimumAmount_getAccountNoOnCard;

    @Value("${CheckPaymentForMinimumAmount.Select.insertToMinPayTable_Select_allPayments}")
    public String CheckPaymentForMinimumAmount_insertToMinPayTable_Select_allPayments;

    @Value("${CheckPaymentForMinimumAmount.Select.insertToMinPayTable_Select_allPamentsSameDay}")
    public String CheckPaymentForMinimumAmount_insertToMinPayTable_Select_allPaymentsallPamentsSameDay;

    @Value("${CheckPaymentForMinimumAmount.Insert.insertToMinPayTable_Insert}")
    public String CheckPaymentForMinimumAmount_insertToMinPayTable_Insert;

    @Value("${CheckPaymentForMinimumAmount.Update.insertToMinPayTable_Update}")
    public String CheckPaymentForMinimumAmount_insertToMinPayTable_Update;

    @Value("${CheckPaymentForMinimumAmount.Select.getPaymentAmount}")
    public String CheckPaymentForMinimumAmount_getPaymentAmount;

    @Value("${CheckPaymentForMinimumAmount.Select.getTotalPaymentExceptDueDate}")
    public String CheckPaymentForMinimumAmount_getTotalPaymentExceptDueDate;



    @Value("${ChequePayment.Select.getChequePaymentsBackup}")
    public String ChequePayment_getChequePaymentsBackup;

    @Value("${ChequePayment.Select.getDelinquencyStatus}")
    public String ChequePayment_getDelinquencyStatus;

    @Value("${ChequePayment.Select.getStatementCount}")
    public String ChequePayment_getStatementCount;

    @Value("${ChequePayment.Insert.insertChequePayments}")
    public String ChequePayment_insertChequePayments;

    @Value("${ChequePayment.Update.updateChequePayment}")
    public String ChequePayment_updateChequePayment;


    @Value("${ChequeReturn.Select.getChequeReturns}")
    public String ChequeReturn_getChequeReturns;

    @Value("${ChequeReturn.Update.updateChequeReturns}")
    public String ChequeReturn_updateChequeReturns;

    @Value("${ChequeReturn.Update.updateChequeReturnsForEODPayment}")
    public String ChequeReturn_updateChequeReturnsForEODPayment;

    @Value("${ChequeReturn.Select.getNewCardNumber}")
    public String ChequeReturn_getNewCardNumber;

    @Value("${ChequeReturn.Select.returnChequePaymentDetails}")
    public String ChequeReturn_returnChequePaymentDetails;

    @Value("${ChequeReturn.Select.returnChequePaymentDetails_OrderBy}")
    public String ChequeReturn_returnChequePaymentDetails_OrderBy;

    @Value("${ChequeReturn.Select.getCardAccountCustomer}")
    public String ChequeReturn_getCardAccountCustomer;

    @Value("${ChequeReturn.Select.getChequeKnockOffBean}")
    public String ChequeReturn_getChequeKnockOffBean;

    @Value("${ChequeReturn.Select.getEOMPendingKnockOffList}")
    public String ChequeReturn_getEOMPendingKnockOffList;

    @Value("${ChequeReturn.Update.updateEOMCARDBalanceKnockOn}")
    public String ChequeReturn_updateEOMCARDBalanceKnockOn;

    @Value("${ChequeReturn.Update.updateCustomerOtb}")
    public String ChequeReturn_updateCustomerOtb;

    @Value("${ChequeReturn.Update.updateAccountOtb}")
    public String ChequeReturn_updateAccountOtb;

    @Value("${ChequeReturn.Update.updateCardOtb}")
    public String ChequeReturn_updateCardOtb;

    @Value("${ChequeReturn.Update.updateOnlineCustomerOtb}")
    public String ChequeReturn_updateOnlineCustomerOtb;

    @Value("${ChequeReturn.Update.updateOnlineAccountOtb}")
    public String ChequeReturn_updateOnlineAccountOtb;

    @Value("${ChequeReturn.Update.updateOnlineCardOtb}")
    public String ChequeReturn_updateOnlineCardOtb;

    @Value("${ChequeReturn.Update.updateEODCARDBalanceKnockOn}")
    public String ChequeReturn_updateEODCARDBalanceKnockOn;

    @Value("${ChequeReturn.Select.getIntProf}")
    public String ChequeReturn_getIntProf;

    @Value("${ChequeReturn.Select.getTxnIdForLastChequeByAccount}")
    public String ChequeReturn_getTxnIdForLastChequeByAccount;

    @Value("${ChequeReturn.Select.getTxnIdForLastCheque}")
    public String ChequeReturn_getTxnIdForLastCheque;

    @Value("${ChequeReturn.Select.checkDuplicateChequeReturnEntry}")
    public String ChequeReturn_checkDuplicateChequeReturnEntry;

    @Value("${ChequeReturn.Insert.insertReturnChequeToEODTransaction}")
    public String ChequeReturn_insertReturnChequeToEODTransaction;

    @Value("${ChequeReturn.Update.addCardFeeCount_Update}")
    public String ChequeReturn_addCardFeeCount_Update;

    @Value("${ChequeReturn.Insert.addCardFeeCount_Insert}")
    public String ChequeReturn_addCardFeeCount_Insert;

    @Value("${ChequeReturn.Select.checkFeeExistForCard}")
    public String ChequeReturn_checkFeeExistForCard;

    @Value("${ChequeReturn.Select.getFeeCode}")
    public String ChequeReturn_getFeeCode;

    @Value("${ChequeReturn.Update.updatePaymentStatus}")
    public String ChequeReturn_updatePaymentStatus;

    @Value("${ChequeReturn.Update.updateTransactionEODStatus}")
    public String ChequeReturn_updateTransactionEODStatus;

    @Value("${ChequeReturn.Update.updateChequeStatusForEODTxn}")
    public String ChequeReturn_updateChequeStatusForEODTxn;

    @Value("${ChequeReturn.Update.updateChequePaymentStatus}")
    public String ChequeReturn_updateChequePaymentStatus;

    @Value("${ChequeReturn.Select.getAccountNoOnCard}")
    public String ChequeReturn_getAccountNoOnCard;

    @Value("${ChequeReturn.Select.getPaymentAmountBetweenDueDate}")
    public String ChequeReturn_getPaymentAmountBetweenDueDate;

    @Value("${ChequeReturn.Select.getEodInterestForCard}")
    public String ChequeReturn_getEodInterestForCard;

    @Value("${ChequeReturn.Update.updateEodInterestForCard}")
    public String ChequeReturn_updateEodInterestForCard;

    @Value("${ChequeReturn.Select.getFeeCodeIfThereExists}")
    public String ChequeReturn_getFeeCodeIfThereExists;

    @Value("${ChequeReturn.Update.restoreMinimumPayment}")
    public String ChequeReturn_restoreMinimumPayment;

    @Value("${ChequeReturn.Select.getCardBlockOldCardStatus}")
    public String ChequeReturn_getCardBlockOldCardStatus;

    @Value("${ChequeReturn.Update.updateCardStatus}")
    public String ChequeReturn_updateCardStatus;

    @Value("${ChequeReturn.Select.getRiskClassOnNdia}")
    public String ChequeReturn_getRiskClassOnNdia;

    @Value("${ChequeReturn.Update.updateDelinquencyStatus}")
    public String ChequeReturn_updateDelinquencyStatus;

    @Value("${ChequeReturn.Select.insertToMinPayTableOld_allPayments_Select}")
    public String ChequeReturn_insertToMinPayTableOld_allPayments_Select;

    @Value("${ChequeReturn.Select.insertToMinPayTableOld_allPaymentsSameDay_Select}")
    public String ChequeReturn_insertToMinPayTableOld_allPaymentsSameDay_Select;

    @Value("${ChequeReturn.Insert.insertToMinPayTableOld_Insert}")
    public String ChequeReturn_insertToMinPayTableOld_Insert;

    @Value("${ChequeReturn.Update.insertToMinPayTableOld_Update}")
    public String ChequeReturn_insertToMinPayTableOld_Update;



    @Value("${ClearMinAmountAndTempBlock.Select.getAllCards}")
    public String ClearMinAmountAndTempBlock_getAllCards;

    @Value("${ClearMinAmountAndTempBlock.Select.removeFromMinPayTable_allMinPayments}")
    public String ClearMinAmountAndTempBlock_removeFromMinPayTable_allMinPayments;

    @Value("${ClearMinAmountAndTempBlock.Insert.removeFromMinPayTable_insertToBackuptable}")
    public String ClearMinAmountAndTempBlock_removeFromMinPayTable_insertToBackuptable;

    @Value("${ClearMinAmountAndTempBlock.Delete.removeFromMinPayTable_removeFromTriggerCard}")
    public String ClearMinAmountAndTempBlock_removeFromMinPayTable_removeFromTriggerCard;

    @Value("${ClearMinAmountAndTempBlock.Insert.removeFromMinPayTable_Update}")
    public String ClearMinAmountAndTempBlock_removeFromMinPayTable_Update;

    @Value("${ClearMinAmountAndTempBlock.Update.updateCardBlock}")
    public String ClearMinAmountAndTempBlock_updateCardBlock;

    @Value("${ClearMinAmountAndTempBlock.Select.getMinimumPaymentExistStatementDate}")
    public String ClearMinAmountAndTempBlock_getMinimumPaymentExistStatementDate;


    @Value("${CollectionAndRecoveryAlert.Select.getConfirmedCardToAlert}")
    public String CollectionAndRecoveryAlert_getConfirmedCardToAlert;

    @Value("${CollectionAndRecoveryAlert.Select.getConfirmedCardToAlert_Appender1}")
    public String CollectionAndRecoveryAlert_getConfirmedCardToAlert_Appender1;

    @Value("${CollectionAndRecoveryAlert.Select.getConfirmedCardToAlert_Appender2}")
    public String CollectionAndRecoveryAlert_getConfirmedCardToAlert_Appender2;

    @Value("${CollectionAndRecoveryAlert.Update.updateAlertGenStatus}")
    public String CollectionAndRecoveryAlert_updateAlertGenStatus;


    @Value("${CollectionAndRecovery.Select.getNoOfDaysOnTriggerPoint}")
    public String CollectionAndRecovery_getNoOfDaysOnTriggerPoint;

    @Value("${CollectionAndRecovery.Select.getCardListForCollectionAndRecoveryOnDueDate_Select1}")
    public String CollectionAndRecovery_getCardListForCollectionAndRecoveryOnDueDate_Select1;

    @Value("${CollectionAndRecovery.Select.getCardListForCollectionAndRecoveryOnDueDate_Select2}")
    public String CollectionAndRecovery_getCardListForCollectionAndRecoveryOnDueDate_Select2;

    @Value("${CollectionAndRecovery.Select.getCardListForCollectionAndRecoveryOnStatmentDate_Select1}")
    public String CollectionAndRecovery_getCardListForCollectionAndRecoveryOnStatmentDate_Select1;

    @Value("${CollectionAndRecovery.Select.getCardListForCollectionAndRecoveryOnStatmentDate_Select2}")
    public String CollectionAndRecovery_getCardListForCollectionAndRecoveryOnStatmentDate_Select2;

    @Value("${CollectionAndRecovery.Select.CheckForTriggerPoint}")
    public String CollectionAndRecovery_CheckForTriggerPoint;

    @Value("${CollectionAndRecovery.Insert.addCardToTriggerCards}")
    public String CollectionAndRecovery_addCardToTriggerCards;

    @Value("${CollectionAndRecovery.Update.updateTriggerCards}")
    public String CollectionAndRecovery_updateTriggerCards;

    @Value("${CollectionAndRecovery.Insert.addDetailsToCardLetterNotifyTable}")
    public String CollectionAndRecovery_addDetailsToCardLetterNotifyTable;


    @Value("${SnapShot.Select.checkEodComplete}")
    public String SnapShot_checkEodComplete;


    @Value("${RunnableFee.Select.getAllActiveCards}")
    public String RunnableFee_getAllActiveCards;

    @Value("${RunnableFee.Select.findCashAdvances}")
    public String RunnableFee_findCashAdvances;

    @Value("${RunnableFee.Update.updateNextAnniversaryDate}")
    public String RunnableFee_updateNextAnniversaryDate;

    @Value("${RunnableFee.Select.checkFeeExistForCard}")
    public String RunnableFee_checkFeeExistForCard;

    @Value("${RunnableFee.Update.addCardFeeCount_Update}")
    public String RunnableFee_addCardFeeCount_Update;

    @Value("${RunnableFee.Insert.addCardFeeCount_Insert}")
    public String RunnableFee_addCardFeeCount_Insert;

    @Value("${RunnableFee.Select.getFeeCode}")
    public String RunnableFee_getFeeCode;

    @Value("${RunnableFee.Select.getLastStatementSummaryInfor}")
    public String RunnableFee_getLastStatementSummaryInfor;

    @Value("${RunnableFee.Select.getNextBillingDateForCard}")
    public String RunnableFee_getNextBillingDateForCard;

    @Value("${RunnableFee.Select.getCardFeeProfileForCard}")
    public String RunnableFee_getCardFeeProfileForCard;

    @Value("${RunnableFee.Insert.insertToEODcardFee}")
    public String RunnableFee_insertToEODcardFee;

    @Value("${RunnableFee.Update.updateCardFeeCount}")
    public String RunnableFee_updateCardFeeCount;

    @Value("${RunnableFee.Select.checkDuplicateCashAdvances}")
    public String RunnableFee_checkDuplicateCashAdvances;

    @Value("${RunnableFee.Select.getAccountNoOnCard}")
    public String RunnableFee_getAccountNoOnCard;

    @Value("${RunnableFee.Select.getTotalPayment}")
    public String RunnableFee_getTotalPayment;


    @Value("${EOMInterest.Select.getEomCardList}")
    public String EOMInterest_getEomCardList;

    @Value("${EOMInterest.Select.getEomCardList_Appender1}")
    public String EOMInterest_getEomCardList_Appender1;

    @Value("${EOMInterest.Select.getEomCardList_Appender2}")
    public String EOMInterest_getEomCardList_Appender2;

    @Value("${EOMInterest.Select.CheckForCardIncrementStatus}")
    public String EOMInterest_CheckForCardIncrementStatus;

    @Value("${EOMInterest.Delete.clearEomInterest}")
    public String EOMInterest_clearEomInterest;

    @Value("${EOMInterest.Select.getLastTwoBillingDatesOnAccount}")
    public String EOMInterest_getLastTwoBillingDatesOnAccount;

    @Value("${EOMInterest.Insert.getEOMInterest_Appender1}")
    public String EOMInterest_getEOMInterest_Appender1;

    @Value("${EOMInterest.Insert.getEOMInterest_Appender2}")
    public String EOMInterest_getEOMInterest_Appender2;

    @Value("${EOMInterest.Insert.getEOMInterest_Appender3}")
    public String EOMInterest_getEOMInterest_Appender3;

    @Value("${EOMInterest.Insert.getEOMInterest_Appender4}")
    public String EOMInterest_getEOMInterest_Appender4;

    @Value("${EOMInterest.Insert.getEOMInterest_Appender5}")
    public String EOMInterest_getEOMInterest_Appender5;

    @Value("${EOMInterest.Insert.getEOMInterest_Appender6}")
    public String EOMInterest_getEOMInterest_Appender6;

    @Value("${EOMInterest.Insert.getEOMInterest_Appender7}")
    public String EOMInterest_getEOMInterest_Appender7;

    @Value("${EOMInterest.Insert.getEOMInterest_Appender8}")
    public String EOMInterest_getEOMInterest_Appender8;

    @Value("${EOMInterest.Select.getEOMInterest_Select1}")
    public String EOMInterest_getEOMInterest_Select1;

    @Value("${EOMInterest.Select.getEOMInterest_Select2}")
    public String EOMInterest_getEOMInterest_Select2;

    @Value("${EOMInterest.Select.getEOMInterest_Select3}")
    public String EOMInterest_getEOMInterest_Select3;

    @Value("${EOMInterest.Select.getEOMInterest_Select4}")
    public String EOMInterest_getEOMInterest_Select4;

    @Value("${EOMInterest.Delete.getEOMInterest_Delete}")
    public String EOMInterest_getEOMInterest_Delete;

    @Value("${EOMInterest.Insert.insertIntoTempTxnDetails}")
    public String EOMInterest_insertIntoTempTxnDetails;

    @Value("${EOMInterest.Update.updateDELINQUENTACCOUNTnpdetails}")
    public String EOMInterest_updateDELINQUENTACCOUNTnpdetails;

    @Value("${EOMInterest.Insert.insertIntoEomInterest}")
    public String EOMInterest_insertIntoEomInterest;

    @Value("${EOMInterest.Insert.insertIntoEodGLAccount}")
    public String EOMInterest_insertIntoEodGLAccount;


    @Value("${EOMSupplementaryCardReset.Select.getEligibleAccounts}")
    public String EOMSupplementaryCardReset_getEligibleAccounts;

    @Value("${EOMSupplementaryCardReset.Select.getEligibleAccounts_Appender1}")
    public String EOMSupplementaryCardReset_getEligibleAccounts_Appender1;

    @Value("${EOMSupplementaryCardReset.Select.getEligibleAccounts_Appender2}")
    public String EOMSupplementaryCardReset_getEligibleAccounts_Appender2;

    @Value("${EOMSupplementaryCardReset.Select.getAllTheCardsForAccount}")
    public String EOMSupplementaryCardReset_getAllTheCardsForAccount;

    @Value("${EOMSupplementaryCardReset.Select.getCardBalances}")
    public String EOMSupplementaryCardReset_getCardBalances;

    @Value("${EOMSupplementaryCardReset.Select.UpdateEOMCardBalance_Select}")
    public String EOMSupplementaryCardReset_UpdateEOMCardBalance_Select;

    @Value("${EOMSupplementaryCardReset.Update.UpdateEOMCardBalance_Update}")
    public String EOMSupplementaryCardReset_UpdateEOMCardBalance_Update;

    @Value("${EOMSupplementaryCardReset.Insert.UpdateEOMCardBalance_Insert}")
    public String EOMSupplementaryCardReset_UpdateEOMCardBalance_Insert;

    @Value("${EOMSupplementaryCardReset.Select.getCardTempBalances}")
    public String EOMSupplementaryCardReset_getCardTempBalances;

    @Value("${EOMSupplementaryCardReset.Update.resetEodCardBallance}")
    public String EOMSupplementaryCardReset_resetEodCardBallance;

    @Value("${EOMSupplementaryCardReset.Select.getEOMCardBalanceFromSupplementary}")
    public String EOMSupplementaryCardReset_getEOMCardBalanceFromSupplementary;

    @Value("${EOMSupplementaryCardReset.Update.resetEOMCardBalance}")
    public String EOMSupplementaryCardReset_resetEOMCardBalance;

    @Value("${EOMSupplementaryCardReset.Select.calculateMainCardForwardPayments}")
    public String EOMSupplementaryCardReset_calculateMainCardForwardPayments;

    @Value("${EOMSupplementaryCardReset.Select.calculateSupCardForwardPayments}")
    public String EOMSupplementaryCardReset_calculateSupCardForwardPayments;

    @Value("${EOMSupplementaryCardReset.Update.updateMainCardBal}")
    public String EOMSupplementaryCardReset_updateMainCardBal;

    @Value("${EOMSupplementaryCardReset.Update.updateMainCardBalOnline}")
    public String EOMSupplementaryCardReset_updateMainCardBalOnline;

    @Value("${EOMSupplementaryCardReset.Update.updateSupplementaryEODPaymentsStatus}")
    public String EOMSupplementaryCardReset_updateSupplementaryEODPaymentsStatus;

    @Value("${EOMSupplementaryCardReset.Select.insertNewEntryToEodPayment_Select}")
    public String EOMSupplementaryCardReset_insertNewEntryToEodPayment_Select;

    @Value("${EOMSupplementaryCardReset.Insert.insertNewEntryToEodPayment_Insert}")
    public String EOMSupplementaryCardReset_insertNewEntryToEodPayment_Insert;

    @Value("${EOMSupplementaryCardReset.Update.updateEodCardBallance}")
    public String EOMSupplementaryCardReset_updateEodCardBallance;

    @Value("${EOMSupplementaryCardReset.Select.calculateOTBsAfterResetting}")
    public String EOMSupplementaryCardReset_calculateOTBsAfterResetting;

    @Value("${EOMSupplementaryCardReset.Update.resetSuplimentryBalanceInBackendCardTable}")
    public String EOMSupplementaryCardReset_resetSuplimentryBalanceInBackendCardTable;

    @Value("${EOMSupplementaryCardReset.Update.resetSuplimentryBalanceInOnlineCardTable}")
    public String EOMSupplementaryCardReset_resetSuplimentryBalanceInOnlineCardTable;

    @Value("${EOMSupplementaryCardReset.Update.updatePreviousEODErrorCardDetails}")
    public String EOMSupplementaryCardReset_updatePreviousEODErrorCardDetails;

    @Value("${EOMSupplementaryCardReset.Update.updateEodProcessSummery}")
    public String EOMSupplementaryCardReset_updateEodProcessSummery;

    @Value("${EOMSupplementaryCardReset.Select.getCreditCashLimit}")
    public String EOMSupplementaryCardReset_getCreditCashLimit;

    @Value("${FeePost.Select.getInitEodFeePostCustAcc}")
    public String FeePost_getInitEodFeePostCustAcc;

    @Value("${FeePost.Select.getErrorEodFeePostCustAcc}")
    public String FeePost_getErrorEodFeePostCustAcc;

    @Value("${FeePost.Select.getFeeAmount}")
    public String FeePost_getFeeAmount;

    @Value("${FeePost.Update.updateCardOtb}")
    public String FeePost_updateCardOtb;

    @Value("${FeePost.Update.updateEODCARDBALANCEByFee}")
    public String FeePost_updateEODCARDBALANCEByFee;

    @Value("${FeePost.Update.updateOnlineCardOtb}")
    public String FeePost_updateOnlineCardOtb;

    @Value("${FeePost.Update.updateAccountOtb}")
    public String FeePost_updateAccountOtb;

    @Value("${FeePost.Update.updateEODCARDFEE}")
    public String FeePost_updateEODCARDFEE;

    @Value("${FeePost.Update.updateEOMINTEREST}")
    public String FeePost_updateEOMINTEREST;

    @Value("${FeePost.Update.updateOnlineAccountOtb}")
    public String FeePost_updateOnlineAccountOtb;

    @Value("${FeePost.Update.updateCustomerOtb}")
    public String FeePost_updateCustomerOtb;

    @Value("${FeePost.Update.updateOnlineCustomerOtb}")
    public String FeePost_updateOnlineCustomerOtb;

    @Value("${FeePost.Update.expireFeePromotionProfile}")
    public String FeePost_expireFeePromotionProfile;


    @Value("${IncrementLimitExpire.Select.getLimitExpiredCardList}")
    public String IncrementLimitExpire_getLimitExpiredCardList;

    @Value("${IncrementLimitExpire.Update.expireCreditLimit_Appender1}")
    public String IncrementLimitExpire_expireCreditLimit_Appender1;

    @Value("${IncrementLimitExpire.Update.expireCreditLimit_Appender2}")
    public String IncrementLimitExpire_expireCreditLimit_Appender2;

    @Value("${IncrementLimitExpire.Update.limitExpireOnAccount_Appender1}")
    public String IncrementLimitExpire_limitExpireOnAccount_Appender1;

    @Value("${IncrementLimitExpire.Update.limitExpireOnAccount_Appender2}")
    public String IncrementLimitExpire_limitExpireOnAccount_Appender2;

    @Value("${IncrementLimitExpire.Update.limitExpireOnCustomer_Appender1}")
    public String IncrementLimitExpire_limitExpireOnCustomer_Appender1;

    @Value("${IncrementLimitExpire.Update.limitExpireOnCustomer_Appender2}")
    public String IncrementLimitExpire_limitExpireOnCustomer_Appender2;

    @Value("${IncrementLimitExpire.Update.expireCashLimit_Appender1}")
    public String IncrementLimitExpire_expireCashLimit_Appender1;

    @Value("${IncrementLimitExpire.Update.expireCashLimit_Appender2}")
    public String IncrementLimitExpire_expireCashLimit_Appender2;

    @Value("${IncrementLimitExpire.Update.cashLimitExpireOnAccount_Appender1}")
    public String IncrementLimitExpire_cashLimitExpireOnAccount_Appender1;

    @Value("${IncrementLimitExpire.Update.cashLimitExpireOnAccount_Appender2}")
    public String IncrementLimitExpire_cashLimitExpireOnAccount_Appender2;

    @Value("${IncrementLimitExpire.Update.cashLimitExpireOnCustomer_Appender1}")
    public String IncrementLimitExpire_cashLimitExpireOnCustomer_Appender1;

    @Value("${IncrementLimitExpire.Update.cashLimitExpireOnCustomer_Appender2}")
    public String IncrementLimitExpire_cashLimitExpireOnCustomer_Appender2;

    @Value("${IncrementLimitExpire.Update.updateTempLimitIncrementTable_Appender1}")
    public String IncrementLimitExpire_updateTempLimitIncrementTable_Appender1;

    @Value("${IncrementLimitExpire.Update.updateTempLimitIncrementTable_Appender2}")
    public String IncrementLimitExpire_updateTempLimitIncrementTable_Appender2;

    @Value("${IncrementLimitExpire.Update.expireOnlineCreditLimit_Appender1}")
    public String IncrementLimitExpire_expireOnlineCreditLimit_Appender1;

    @Value("${IncrementLimitExpire.Update.expireOnlineCreditLimit_Appender2}")
    public String IncrementLimitExpire_expireOnlineCreditLimit_Appender2;

    @Value("${IncrementLimitExpire.Update.limitOnlineExpireOnAccount_Appender1}")
    public String IncrementLimitExpire_limitOnlineExpireOnAccount_Appender1;

    @Value("${IncrementLimitExpire.Update.limitOnlineExpireOnAccount_Appender2}")
    public String IncrementLimitExpire_limitOnlineExpireOnAccount_Appender2;

    @Value("${IncrementLimitExpire.Update.limitOnlineExpireOnCustomer_Appender1}")
    public String IncrementLimitExpire_limitOnlineExpireOnCustomer_Appender1;

    @Value("${IncrementLimitExpire.Update.limitOnlineExpireOnCustomer_Appender2}")
    public String IncrementLimitExpire_limitOnlineExpireOnCustomer_Appender2;

    @Value("${IncrementLimitExpire.Update.expireOnlineCashLimit_Appender1}")
    public String IncrementLimitExpire_expireOnlineCashLimit_Appender1;

    @Value("${IncrementLimitExpire.Update.expireOnlineCashLimit_Appender2}")
    public String IncrementLimitExpire_expireOnlineCashLimit_Appender2;

    @Value("${IncrementLimitExpire.Update.cashLimitOnlineExpireOnAccount_Appender1}")
    public String IncrementLimitExpire_cashLimitOnlineExpireOnAccount_Appender1;

    @Value("${IncrementLimitExpire.Update.cashLimitOnlineExpireOnAccount_Appender2}")
    public String IncrementLimitExpire_cashLimitOnlineExpireOnAccount_Appender2;

    @Value("${IncrementLimitExpire.Update.cashLimitOnlineExpireOnCustomer_Appender1}")
    public String IncrementLimitExpire_cashLimitOnlineExpireOnCustomer_Appender1;

    @Value("${IncrementLimitExpire.Update.cashLimitOnlineExpireOnCustomer_Appender2}")
    public String IncrementLimitExpire_cashLimitOnlineExpireOnCustomer_Appender2;



    @Value("${InstallmentPayment.Select.getManualNpRequestDetails}")
    public String InstallmentPayment_getManualNpRequestDetails;

    @Value("${InstallmentPayment.Select.getDelinquentAccounts}")
    public String InstallmentPayment_getDelinquentAccounts;

    @Value("${InstallmentPayment.Select.getDelinquentAccounts_Appender1}")
    public String InstallmentPayment_getDelinquentAccounts_Appender1;

    @Value("${InstallmentPayment.Select.getDelinquentAccounts_Appender2}")
    public String InstallmentPayment_getDelinquentAccounts_Appender2;

    @Value("${InstallmentPayment.Select.checkForPayment}")
    public String InstallmentPayment_checkForPayment;

    @Value("${InstallmentPayment.Select.getRiskClassOnNdia}")
    public String InstallmentPayment_getRiskClassOnNdia;

    @Value("${InstallmentPayment.Select.getNPRiskClass}")
    public String InstallmentPayment_getNPRiskClass;

    @Value("${InstallmentPayment.Select.getNDIAOnRiskClass}")
    public String InstallmentPayment_getNDIAOnRiskClass;

    @Value("${InstallmentPayment.Select.checkLeastMinimumPayment}")
    public String InstallmentPayment_checkLeastMinimumPayment;

    @Value("${InstallmentPayment.Insert.insertInToEODTransactionOnlyVisaFalse}")
    public String InstallmentPayment_insertInToEODTransactionOnlyVisaFalse;

    @Value("${InstallmentPayment.Insert.insertInToEODTransactionWithoutGL}")
    public String InstallmentPayment_insertInToEODTransactionWithoutGL;

    @Value("${InstallmentPayment.Update.updateFeeToEDONInTransactionTable}")
    public String InstallmentPayment_updateFeeToEDONInTransactionTable;

    @Value("${InstallmentPayment.Select.getEodtxnDescription}")
    public String InstallmentPayment_getEodtxnDescription;

    @Value("${InstallmentPayment.Select.getEasyPaymentDetails}")
    public String InstallmentPayment_getEasyPaymentDetails;



    @Value("${KnockOff.Select.getInitKnockOffCustAcc}")
    public String KnockOff_getInitKnockOffCustAcc;

    @Value("${KnockOff.Select.getErrorKnockOffCustAcc}")
    public String KnockOff_getErrorKnockOffCustAcc;

    @Value("${KnockOff.Select.getKnockOffCardList}")
    public String KnockOff_getKnockOffCardList;

    @Value("${KnockOff.Select.getMainCard}")
    public String KnockOff_getMainCard;

    @Value("${KnockOff.Select.getPaymentList}")
    public String KnockOff_getPaymentList;

    @Value("${KnockOff.Select.getEomKnockOffAmount}")
    public String KnockOff_getEomKnockOffAmount;

    @Value("${KnockOff.Select.getEodKnockOffAmount}")
    public String KnockOff_getEodKnockOffAmount;

    @Value("${KnockOff.Update.updateEodPayment}")
    public String KnockOff_updateEodPayment;

    @Value("${KnockOff.Update.updateCardOtb}")
    public String KnockOff_updateCardOtb;

    @Value("${KnockOff.Update.updateEodClosingBalance}")
    public String KnockOff_updateEodClosingBalance;

    @Value("${KnockOff.Update.updateEOMCARDBALANCE}")
    public String KnockOff_updateEOMCARDBALANCE;

    @Value("${KnockOff.Update.updateEODCARDBALANCE}")
    public String KnockOff_updateEODCARDBALANCE;

    @Value("${KnockOff.Update.updateCardComp}")
    public String KnockOff_updateCardComp;

    @Value("${KnockOff.Update.updateAccountOtb}")
    public String KnockOff_updateAccountOtb;

    @Value("${KnockOff.Update.updateCustomerOtb}")
    public String KnockOff_updateCustomerOtb;

    @Value("${KnockOff.Update.OnlineupdateCardOtb}")
    public String KnockOff_OnlineupdateCardOtb;

    @Value("${KnockOff.Update.OnlineupdateAccountOtb}")
    public String KnockOff_OnlineupdateAccountOtb;

    @Value("${KnockOff.Update.OnlineupdateCustomerOtb}")
    public String KnockOff_OnlineupdateCustomerOtb;


    @Value("${LoyaltyPointsCalculation.Select.getTodayBillingCardSet}")
    public String LoyaltyPointsCalculation_getTodayBillingCardSet;

    @Value("${LoyaltyPointsCalculation.Select.getLoyaltyConfigurations}")
    public String LoyaltyPointsCalculation_getLoyaltyConfigurations;

    @Value("${LoyaltyPointsCalculation.Select.getLastStmtClosingLoyalty}")
    public String LoyaltyPointsCalculation_getLastStmtClosingLoyalty;

    @Value("${LoyaltyPointsCalculation.Select.getThisMonthRedeem}")
    public String LoyaltyPointsCalculation_getThisMonthRedeem;

    @Value("${LoyaltyPointsCalculation.Select.getAdjustLoyalty}")
    public String LoyaltyPointsCalculation_getAdjustLoyalty;

    @Value("${LoyaltyPointsCalculation.Update.updateBillingStatment}")
    public String LoyaltyPointsCalculation_updateBillingStatment;

    @Value("${LoyaltyPointsCalculation.Update.updateLoyaltyRedeemRequest}")
    public String LoyaltyPointsCalculation_updateLoyaltyRedeemRequest;


    @Value("${ManualNpRepo.Select.getProcessDetails}")
    public String ManualNpRepo_updateLoyaltyRedeemRequest;

    @Value("${ManualNpRepo.Select.getManualNpRequestDetails}")
    public String ManualNpRepo_getManualNpRequestDetails;

    @Value("${ManualNpRepo.Update.updateNpStatusCardAccount}")
    public String ManualNpRepo_updateNpStatusCardAccount;

    @Value("${ManualNpRepo.Insert.insertIntoDelinquentHistory}")
    public String ManualNpRepo_insertIntoDelinquentHistory;

    @Value("${ManualNpRepo.Select.getNPDetailsFromLastBillingStatement_Appender1}")
    public String ManualNpRepo_insertIntoDelinquentHistory_Appender1;

    @Value("${ManualNpRepo.Select.getNPDetailsFromLastBillingStatement_Appender2}")
    public String ManualNpRepo_insertIntoDelinquentHistory_Appender2;

    @Value("${ManualNpRepo.Select.getNPDetailsFromLastBillingStatement_Appender3}")
    public String ManualNpRepo_insertIntoDelinquentHistory_Appender3;

    @Value("${ManualNpRepo.Select.setDelinquentAccountDetails}")
    public String ManualNpRepo_setDelinquentAccountDetails;

    @Value("${ManualNpRepo.Select.setDelinquentAccountDetails_Appender1}")
    public String ManualNpRepo_setDelinquentAccountDetails_Appender1;

    @Value("${ManualNpRepo.Select.setDelinquentAccountDetails_Appender2}")
    public String ManualNpRepo_setDelinquentAccountDetails_Appender2;

    @Value("${ManualNpRepo.Select.setDelinquentAccountDetails_Appender3}")
    public String ManualNpRepo_setDelinquentAccountDetails_Appender3;

    @Value("${ManualNpRepo.Select.getTotalPaymentSinceLastDue}")
    public String ManualNpRepo_getTotalPaymentSinceLastDue;

    @Value("${ManualNpRepo.Select.getRiskclassOnNdia}")
    public String ManualNpRepo_getRiskclassOnNdia;

    @Value("${ManualNpRepo.Insert.insertIntoEodGLAccount}")
    public String ManualNpRepo_insertIntoEodGLAccount;

    @Value("${ManualNpRepo.Select.addDetailsForManualNPToDelinquentAccountTable_Select}")
    public String ManualNpRepo_addDetailsForManualNPToDelinquentAccountTable_Select;

    @Value("${ManualNpRepo.Update.addDetailsForManualNPToDelinquentAccountTable_Update}")
    public String ManualNpRepo_addDetailsForManualNPToDelinquentAccountTable_Update;

    @Value("${ManualNpRepo.Insert.addDetailsForManualNPToDelinquentAccountTable_Insert}")
    public String ManualNpRepo_addDetailsForManualNPToDelinquentAccountTable_Insert;

    @Value("${ManualNpRepo.Update.updateManualNPtoComplete}")
    public String ManualNpRepo_updateManualNPtoComplete;

    @Value("${ManualNpRepo.Select.getNPRiskClass}")
    public String ManualNpRepo_getNPRiskClass;

    @Value("${ManualNpRepo.Select.getNDIAOnRiskClass}")
    public String ManualNpRepo_getNDIAOnRiskClass;

    @Value("${ManualNpRepo.Select.getNPDetailsForNpGl}")
    public String ManualNpRepo_getNPDetailsForNpGl;

    @Value("${ManualNpRepo.Update.updateDelinquentAccountForManualNP}")
    public String ManualNpRepo_updateDelinquentAccountForManualNP;

    @Value("${ManualNpRepo.Update.updateAccountStatus}")
    public String ManualNpRepo_updateAccountStatus;

    @Value("${ManualNpRepo.Update.updateOnlineAccountStatus}")
    public String ManualNpRepo_updateOnlineAccountStatus;


    @Value("${MonthlyStatement.Select.getCardAccountListForBilling}")
    public String MonthlyStatement_getCardAccountListForBilling;

    @Value("${MonthlyStatement.Select.getCardAccountListForBilling_Appender1}")
    public String MonthlyStatement_getCardAccountListForBilling_Appender1;

    @Value("${MonthlyStatement.Select.getCardAccountListForBilling_Appender2}")
    public String MonthlyStatement_getCardAccountListForBilling_Appender2;

    @Value("${MonthlyStatement.Select.getCardAccountListForBilling_Appender3}")
    public String MonthlyStatement_getCardAccountListForBilling_Appender3;

    @Value("${MonthlyStatement.Select.checkReplaceStatus}")
    public String MonthlyStatement_checkReplaceStatus;

    @Value("${MonthlyStatement.Select.CheckBillingCycleChangeRequest}")
    public String MonthlyStatement_CheckBillingCycleChangeRequest;

    @Value("${MonthlyStatement.Select.CheckBillingCycleChangeRequest_Appender1}")
    public String MonthlyStatement_CheckBillingCycleChangeRequest_Appender1;

    @Value("${MonthlyStatement.Select.getAllOldCards}")
    public String MonthlyStatement_getAllOldCards;

    @Value("${MonthlyStatement.Select.updateCloseCardFlag}")
    public String MonthlyStatement_updateCloseCardFlag;

    @Value("${MonthlyStatement.Update.updateBillingCycleRequestBCCP}")
    public String MonthlyStatement_updateBillingCycleRequestBCCP;

    @Value("${MonthlyStatement.Select.calculateDueDate}")
    public String MonthlyStatement_calculateDueDate;

    @Value("${MonthlyStatement.Select.isHoliday}")
    public String MonthlyStatement_isHoliday;

    @Value("${MonthlyStatement.Select.getThisStatementStartandEndEodId}")
    public String MonthlyStatement_getThisStatementStartandEndEodId;

    @Value("${MonthlyStatement.Select.getCardTranactionSummeryBean}")
    public String MonthlyStatement_getCardTranactionSummeryBean;

    @Value("${MonthlyStatement.Update.updateStatementIDByAccNoInEODTxn}")
    public String MonthlyStatement_updateStatementIDByAccNoInEODTxn;

    @Value("${MonthlyStatement.Select.getLastStatementDetails_Select1}")
    public String MonthlyStatement_getLastStatementDetails_Select1;

    @Value("${MonthlyStatement.Select.getLastStatementDetails_Select2}")
    public String MonthlyStatement_getLastStatementDetails_Select2;

    @Value("${MonthlyStatement.Insert.insertBillingStatement}")
    public String MonthlyStatement_insertBillingStatement;

    @Value("${MonthlyStatement.Select.getTotalStampDuty}")
    public String MonthlyStatement_getTotalStampDuty;

    @Value("${MonthlyStatement.Select.getBucketIdAndNODIA}")
    public String MonthlyStatement_getBucketIdAndNODIA;

    @Value("${MonthlyStatement.Update.updateNextBillingDate_Update1}")
    public String MonthlyStatement_getBucketIdAndNODIA_Update1;

    @Value("${MonthlyStatement.Update.updateNextBillingDate_Update2}")
    public String MonthlyStatement_getBucketIdAndNODIA_Update2;

    @Value("${MonthlyStatement.Select.calculateMinPayment}")
    public String MonthlyStatement_calculateMinPayment;

    @Value("${MonthlyStatement.Select.checkMinPaymentDueCount}")
    public String MonthlyStatement_checkMinPaymentDueCount;

    @Value("${MonthlyStatement.Insert.insertBillingLastStatementSummry}")
    public String MonthlyStatement_insertBillingLastStatementSummry;

    @Value("${MonthlyStatement.Update.updateBillingLastStatementSummry}")
    public String MonthlyStatement_updateBillingLastStatementSummry;

    @Value("${MonthlyStatement.Select.checkChequeReturns_Select1}")
    public String MonthlyStatement_checkChequeReturns_Select1;

    @Value("${MonthlyStatement.Select.checkChequeReturns_Select2}")
    public String MonthlyStatement_checkChequeReturns_Select2;


    @Value("${OverLimitFee.Select.getOverLimitAcc}")
    public String OverLimitFee_getOverLimitAcc;

    @Value("${OverLimitFee.Select.getMainCardOpeningBalance}")
    public String OverLimitFee_getMainCardOpeningBalance;



    @Value("${RiskCalculation.Select.getDelinquentAccounts}")
    public String RiskCalculation_getDelinquentAccounts;

    @Value("${RiskCalculation.Select.isManualNp}")
    public String RiskCalculation_isManualNp;

    @Value("${RiskCalculation.Select.checkForPayment}")
    public String RiskCalculation_checkForPayment;

    @Value("${RiskCalculation.Select.getRiskclassOnNdia}")
    public String RiskCalculation_getRiskclassOnNdia;

    @Value("${RiskCalculation.Select.getNPRiskClass}")
    public String RiskCalculation_getNPRiskClass;

    @Value("${RiskCalculation.Select.getNDIAOnRiskClass}")
    public String RiskCalculation_getNDIAOnRiskClass;

    @Value("${RiskCalculation.Select.getNPDetailsFromLastBillingStatement_Select1}")
    public String RiskCalculation_getNPDetailsFromLastBillingStatement_Select1;

    @Value("${RiskCalculation.Select.getNPDetailsFromLastBillingStatement_Select2}")
    public String RiskCalculation_getNPDetailsFromLastBillingStatement_Select2;

    @Value("${RiskCalculation.Select.getNPDetailsFromLastBillingStatement_Select3}")
    public String RiskCalculation_getNPDetailsFromLastBillingStatement_Select3;

    @Value("${RiskCalculation.Insert.insertIntoEodGLAccount}")
    public String RiskCalculation_insertIntoEodGLAccount;

    @Value("${RiskCalculation.Update.updateNpStatusCardAccount}")
    public String RiskCalculation_updateNpStatusCardAccount;

    @Value("${RiskCalculation.Select.getTotalPaymentSinceLastDue}")
    public String RiskCalculation_getTotalPaymentSinceLastDue;

    @Value("${RiskCalculation.Select.addDetailsToDelinquentAccountTable_Select}")
    public String RiskCalculation_getTotalPaymentSinceLastDue_Select;

    @Value("${RiskCalculation.Update.addDetailsToDelinquentAccountTable_Update}")
    public String RiskCalculation_getTotalPaymentSinceLastDue_Update;

    @Value("${RiskCalculation.Insert.addDetailsToDelinquentAccountTable_Insert}")
    public String RiskCalculation_getTotalPaymentSinceLastDue_Insert;

    @Value("${RiskCalculation.Insert.insertIntoDelinquentHistory}")
    public String RiskCalculation_insertIntoDelinquentHistory;

    @Value("${RiskCalculation.Select.getRiskCalculationCardList}")
    public String RiskCalculation_getRiskCalculationCardList;

    @Value("${RiskCalculation.Update.updateProvisionInDELINQUENTACCOUNT}")
    public String RiskCalculation_updateProvisionInDELINQUENTACCOUNT;

    @Value("${RiskCalculation.Select.getLastStatementDate}")
    public String RiskCalculation_getLastStatementDate;

    @Value("${RiskCalculation.Select.getDueDateList}")
    public String RiskCalculation_getDueDateList;

    @Value("${RiskCalculation.Update.updateOnlineAccountStatus}")
    public String RiskCalculation_updateOnlineAccountStatus;

    @Value("${RiskCalculation.Update.updateAccountStatus}")
    public String RiskCalculation_updateAccountStatus;

    @Value("${RiskCalculation.Select.checkLeastMinimumPayment}")
    public String RiskCalculation_checkLeastMinimumPayment;

    @Value("${RiskCalculation.Insert.insertIntoEodGLAccountBigDecimal}")
    public String RiskCalculation_insertIntoEodGLAccountBigDecimal;

    @Value("${RiskCalculation.Select.setDelinquentAccountDetails_Select1}")
    public String RiskCalculation_setDelinquentAccountDetails_Select1;

    @Value("${RiskCalculation.Select.setDelinquentAccountDetails_Select2}")
    public String RiskCalculation_setDelinquentAccountDetails_Select2;

    @Value("${RiskCalculation.Select.setDelinquentAccountDetails_Select3}")
    public String RiskCalculation_setDelinquentAccountDetails_Select3;

    @Value("${RiskCalculation.Select.setDelinquentAccountDetails_Select4}")
    public String RiskCalculation_setDelinquentAccountDetails_Select4;

    @Value("${RiskCalculation.Select.getMinPaymentFromBilling}")
    public String RiskCalculation_getMinPaymentFromBilling;

    @Value("${RiskCalculation.Select.getDelinquentAccountDetailsAsList}")
    public String RiskCalculation_getDelinquentAccountDetailsAsList;

    @Value("${RiskCalculation.Update.updateAllDELINQUENTACCOUNTnpdetails}")
    public String RiskCalculation_updateAllDELINQUENTACCOUNTnpdetails;

    @Value("${RiskCalculation.Update.updateAllDELINQUENTACCOUNTnpdetails2}")
    public String RiskCalculation_updateAllDELINQUENTACCOUNTnpdetails2;



    @Value("${StampDutyFee.Select.getErrorStatementAccountList}")
    public String RiskCalculation_getErrorStatementAccountList;

    @Value("${StampDutyFee.Select.getOldCardNumbers}")
    public String RiskCalculation_getOldCardNumbers;

    @Value("${StampDutyFee.Select.getTotalForeignTxns_Select1}")
    public String RiskCalculation_getTotalForeignTxns_Select1;

    @Value("${StampDutyFee.Select.getTotalForeignTxns_Select2}")
    public String RiskCalculation_getTotalForeignTxns_Select2;

    @Value("${StampDutyFee.Insert.insertToEODcardFee_Insert1}")
    public String RiskCalculation_insertToEODcardFee_Insert1;

    @Value("${StampDutyFee.Insert.insertToEODcardFee_Insert2}")
    public String RiskCalculation_insertToEODcardFee_Insert2;

    @Value("${StampDutyFee.Select.getStartEodId}")
    public String RiskCalculation_getStartEodId;

    @Value("${StampDutyFee.Select.getStatementCardList}")
    public String RiskCalculation_getStatementCardList;

    @Value("${StampDutyFee.Select.checkFeeExixtForCard}")
    public String RiskCalculation_checkFeeExixtForCard;



    @Value("${TransactionPost.Select.getInitEodTxnPostCustAcc}")
    public String TransactionPost_getInitEodTxnPostCustAcc;

    @Value("${TransactionPost.Select.getErrorEodTxnPostCustAcc}")
    public String TransactionPost_getErrorEodTxnPostCustAcc;

    @Value("${TransactionPost.Select.getTxnAmount}")
    public String TransactionPost_getTxnAmount;

    @Value("${TransactionPost.Update.updateCardTemp}")
    public String TransactionPost_updateCardTemp;

    @Value("${TransactionPost.Update.updateCardOtbCredit}")
    public String TransactionPost_updateCardOtbCredit;

    @Value("${TransactionPost.Update.updateAccountOtbCredit}")
    public String TransactionPost_updateAccountOtbCredit;

    @Value("${TransactionPost.Update.updateCustomerOtbCredit}")
    public String TransactionPost_updateCustomerOtbCredit;

    @Value("${TransactionPost.Update.updateCardByPostedTransactions}")
    public String TransactionPost_updateCardByPostedTransactions;

    @Value("${TransactionPost.Update.updateEODCARDBALANCEByTxn}")
    public String TransactionPost_updateEODCARDBALANCEByTxn;

    @Value("${TransactionPost.Update.updateEODTRANSACTION}")
    public String TransactionPost_updateEODTRANSACTION;

    @Value("${TransactionPost.Update.updateAccountOtb}")
    public String TransactionPost_updateAccountOtb;

    @Value("${TransactionPost.Update.updateCustomerOtb}")
    public String TransactionPost_updateCustomerOtb;

    @Value("${TransactionPost.Select.getNewCardNumber}")
    public String TransactionPost_getNewCardNumber;



    @Value("${TxnDropRequest.Select.getTransactionValidityPeriod}")
    public String TxnDropRequest_getTransactionValidityPeriod;

    @Value("${TxnDropRequest.Select.getDropTransactionList}")
    public String TxnDropRequest_getDropTransactionList;

    @Value("${TxnDropRequest.Select.getTransactionReverseStatus}")
    public String TxnDropRequest_getTransactionReverseStatus;

    @Value("${TxnDropRequest.Insert.addTxnDropRequest}")
    public String TxnDropRequest_addTxnDropRequest;



    @Value("${EODEngineProducer.Select.getEODStatusFromEODID}")
    public String EODEngineProducer_getEODStatusFromEODID;

    @Value("${EODEngineProducer.Select.checkUploadedFileStatus}")
    public String EODEngineProducer_checkUploadedFileStatus;

    @Value("${EODEngineProducer.Select.getProcessListByCategoryId}")
    public String EODEngineProducer_getProcessListByCategoryId;

    @Value("${EODEngineProducer.Insert.insertToEODProcessCount}")
    public String EODEngineProducer_insertToEODProcessCount;

    @Value("${EODEngineProducer.Select.getCompletedProcessCount}")
    public String EODEngineProducer_getCompletedProcessCount;

    @Value("${EODEngineProducer.Delete.clearEodProcessCountTable}")
    public String EODEngineProducer_clearEodProcessCountTable;

    @Value("${EODEngineProducer.Update.updatePreviousEODErrorCardDetails}")
    public String EODEngineProducer_updatePreviousEODErrorCardDetails;

    @Value("${EODEngineProducer.Update.updatePreviousEODErrorMerchantDetails}")
    public String EODEngineProducer_updatePreviousEODErrorMerchantDetails;

    @Value("${EODEngineProducer.Update.updateEodProcessProgress}")
    public String EODEngineProducer_updateEodProcessProgress;

    @Value("${EODEngineProducer.Select.getErrorProcessIdList}")
    public String EODEngineProducer_getErrorProcessIdList;

    @Value("${EODEngineProducer.Update.updateProcessProgressForErrorProcess}")
    public String EODEngineProducer_updateProcessProgressForErrorProcess;

    @Value("${EODEngineProducer.Update.updateEodProcessStateCount}")
    public String EODEngineProducer_updateEodProcessStateCount;

    @Value("${EODEngineProducer.Update.updateEodStatus}")
    public String EODEngineProducer_updateEodStatus;

    @Value("${EODEngineProducer.Select.hasErrorforLastEOD}")
    public String EODEngineProducer_hasErrorforLastEOD;

    @Value("${EODEngineProducer.Update.updateEodEndStatus}")
    public String EODEngineProducer_updateEodEndStatus;

    @Value("${EODEngineProducer.Select.getNextRunningEodId}")
    public String EODEngineProducer_getNextRunningEodId;



    @Value("${AutoSettlement.Select.updateAutoSettlementWithPayments}")
    public String AutoSettlement_updateAutoSettlementWithPayments;

    @Value("${AutoSettlement.Update.updateRemainingAmount}")
    public String AutoSettlement_updateRemainingAmount;

    @Value("${AutoSettlement.Select.getNewCardNumber}")
    public String AutoSettlement_getNewCardNumber;

    @Value("${AutoSettlement.Select.getUnsuccessfullStandingInstructionFeeEligibleCards}")
    public String AutoSettlement_getUnsuccessfullStandingInstructionFeeEligibleCards;

    @Value("${AutoSettlement.Update.addCardFeeCount_Update}")
    public String AutoSettlement_addCardFeeCount_Update;

    @Value("${AutoSettlement.Insert.addCardFeeCount_Insert}")
    public String AutoSettlement_addCardFeeCount_Insert;

    @Value("${AutoSettlement.Select.getFeeCode}")
    public String AutoSettlement_getFeeCode;

    @Value("${AutoSettlement.Select.checkFeeExistForCard}")
    public String AutoSettlement_checkFeeExistForCard;

    @Value("${AutoSettlement.Select.generatePartialAutoSettlementFile}")
    public String AutoSettlement_generatePartialAutoSettlementFile;

    @Value("${AutoSettlement.Update.updateAutoSettlementTable_Update1}")
    public String AutoSettlement_updateAutoSettlementTable_Update1;

    @Value("${AutoSettlement.Update.updateAutoSettlementTable_Update2}")
    public String AutoSettlement_updateAutoSettlementTable_Update2;

    @Value("${AutoSettlement.Select.generateAutoSettlementFile}")
    public String AutoSettlement_generateAutoSettlementFile;

    @Value("${AutoSettlement.Select.getPaymentAmount}")
    public String AutoSettlement_getPaymentAmount;



    @Value("${CardApplicationConfirmationLetter.Select.getConfirmedCardToGenerateLetters}")
    public String CardApplicationConfirmationLetter_getConfirmedCardToGenerateLetters;

    @Value("${CardApplicationConfirmationLetter.Update.updateLettergenStatus}")
    public String CardApplicationConfirmationLetter_updateLettergenStatus;



    @Value("${CardApplicationRejectLetter.Select.getRejectApplictionIDsToGenerateLetters_Select1}")
    public String CardApplicationRejectLetter_getRejectApplictionIDsToGenerateLetters_Select1;

    @Value("${CardApplicationRejectLetter.Select.getRejectApplictionIDsToGenerateLetters_Select2}")
    public String CardApplicationRejectLetter_getRejectApplictionIDsToGenerateLetters_Select2;

    @Value("${CardApplicationRejectLetter.Update.updateLettergenStatus}")
    public String CardApplicationRejectLetter_updateLettergenStatus;

    @Value("${CardApplicationRejectLetter.Select.getCardNo}")
    public String CardApplicationRejectLetter_getCardNo;


    @Value("${CardRenewLetter.Select.getRenewalCardsToGenerateLetters}")
    public String CardRenewLetter_getRenewalCardsToGenerateLetters;

    @Value("${CardRenewLetter.Update.updateLettergenStatusInCardRenew}")
    public String CardRenewLetter_updateLettergenStatusInCardRenew;



    @Value("${CardReplaceLetter.Select.getReplacedToGenerateLetters}")
    public String CardReplaceLetter_getReplacedToGenerateLetters;

    @Value("${CardReplaceLetter.Update.updateLettergenStatusInCardReplace}")
    public String CardReplaceLetter_updateLettergenStatusInCardReplace;

    @Value("${CardReplaceLetter.Select.getProductChangedCardsToGenerateLetters}")
    public String CardReplaceLetter_getProductChangedCardsToGenerateLetters;

    @Value("${CardReplaceLetter.Select.getCardProductCardTypeForProductChangeCards}")
    public String CardReplaceLetter_getCardProductCardTypeForProductChangeCards;

    @Value("${CardReplaceLetter.Update.updateLettergenStatusInProductChange}")
    public String CardReplaceLetter_updateLettergenStatusInProductChange;



    @Value("${CashBackFileGen.Select.getCahsBackRedeemList}")
    public String CashBackFileGen_getCahsBackRedeemList;

    @Value("${CashBackFileGen.Select.getCashBackDebitAccount}")
    public String CashBackFileGen_getCashBackDebitAccount;

    @Value("${CashBackFileGen.Update.updateCashBackRedeemExp}")
    public String CashBackFileGen_updateCashBackRedeemExp;


    @Value("${CollectionAndRecoveryLetter.Select.getFirstReminderEligibleCards}")
    public String CollectionAndRecoveryLetter_updateCashBackRedeemExp;

    @Value("${CollectionAndRecoveryLetter.Select.getSecondReminderEligibleCards}")
    public String CollectionAndRecoveryLetter_getSecondReminderEligibleCards;

    @Value("${CollectionAndRecoveryLetter.Select.getTriggerEligibleStatus}")
    public String CollectionAndRecoveryLetter_getTriggerEligibleStatus;

    @Value("${CollectionAndRecoveryLetter.Update.updateTriggerCards}")
    public String CollectionAndRecoveryLetter_updateTriggerCards;

    @Value("${CollectionAndRecoveryLetter.Select.getAccountNoOnCard}")
    public String CollectionAndRecoveryLetter_getAccountNoOnCard;

    @Value("${CollectionAndRecoveryLetter.Insert.insertIntoDelinquentHistory}")
    public String CollectionAndRecoveryLetter_insertIntoDelinquentHistory;


    @Value("${ExposureFile.Select.getExposureFileDetails_Select1}")
    public String ExposureFile_getExposureFileDetails_Select1;

    @Value("${ExposureFile.Select.getExposureFileDetails_Select2}")
    public String ExposureFile_getExposureFileDetails_Select2;


    @Value("${CommonFileGenProcess.Select.getCardProductCardType}")
    public String CommonFileGenProcess_getCardProductCardType;

    @Value("${CommonFileGenProcess.Insert.InsertIntoDownloadTable}")
    public String CommonFileGenProcess_InsertIntoDownloadTable;

    @Value("${CommonFileGenProcess.Select.getCardProductCardTypeByApplicationId}")
    public String CommonFileGenProcess_getCardProductCardTypeByApplicationId;

    @Value("${CommonFileGenProcess.Select.getGLAccData}")
    public String CommonFileGenProcess_getGLAccData;

    @Value("${CommonFileGenProcess.Select.getGLTxnTypes}")
    public String CommonFileGenProcess_getGLTxnTypes;

    @Value("${CommonFileGenProcess.Select.getCRDRFromGlTxn}")
    public String CommonFileGenProcess_getCRDRFromGlTxn;

    @Value("${CommonFileGenProcess.Select.isHoliday}")
    public String CommonFileGenProcess_isHoliday;


    @Value("${LetterProcess.Select.getParametersInLetterTemplate}")
    public String LetterProcess_getParametersInLetterTemplate;

    @Value("${LetterProcess.Select.getLetterFieldDetails}")
    public String LetterProcess_getLetterFieldDetails;

    @Value("${LetterProcess.Select.getTemplateBody}")
    public String LetterProcess_getTemplateBody;

    @Value("${LetterProcess.Select.getCardTypebyApplicationID}")
    public String LetterProcess_getCardTypebyApplicationID;

    @Value("${LetterProcess.Select.getCardTypebyCardNumber}")
    public String LetterProcess_getCardTypebyCardNumber;



    @Value("${GLSummaryFile.Select.getCashbackDataToEODGL}")
    public String GLSummaryFile_getCashbackDataToEODGL;

    @Value("${GLSummaryFile.Insert.insertIntoEodGLAccount}")
    public String GLSummaryFile_insertIntoEodGLAccount;

    @Value("${GLSummaryFile.Update.updateCashback}")
    public String GLSummaryFile_updateCashback;

    @Value("${GLSummaryFile.Select.getCashbackExpAndRedeemDataToEODGL}")
    public String GLSummaryFile_getCashbackExpAndRedeemDataToEODGL;

    @Value("${GLSummaryFile.Update.updateCashbackExpAndRedeem}")
    public String GLSummaryFile_updateCashbackExpAndRedeem;

    @Value("${GLSummaryFile.Update.updateAdjusment}")
    public String GLSummaryFile_updateAdjusment;

    @Value("${GLSummaryFile.Update.updateFeeTable}")
    public String GLSummaryFile_updateFeeTable;

    @Value("${GLSummaryFile.Select.getAdjustmentDataToEODGL}")
    public String GLSummaryFile_getAdjustmentDataToEODGL;

    @Value("${GLSummaryFile.Select.getFeeDataToEODGL}")
    public String GLSummaryFile_getFeeDataToEODGL;

    @Value("${GLSummaryFile.Select.getEODTxnDataToGL}")
    public String GLSummaryFile_getEODTxnDataToGL;

    @Value("${GLSummaryFile.Update.updateEODTxn}")
    public String GLSummaryFile_updateEODTxn;

    @Value("${GLSummaryFile.Update.updateEodGLAccount}")
    public String GLSummaryFile_updateEodGLAccount;

    @Value("${GLSummaryFile.Select.getDataFromEODGl}")
    public String GLSummaryFile_getDataFromEODGl;

    @Value("${GLSummaryFile.Select.getGLTypesData}")
    public String GLSummaryFile_getGLTypesData;



    @Value("${RB36FileGeneration.Select.getNPCard}")
    public String RB36FileGeneration_getNPCard;

    @Value("${RB36FileGeneration.Select.getPaymentDataFromEODGl}")
    public String RB36FileGeneration_getPaymentDataFromEODGl;

    @Value("${RB36FileGeneration.Select.getGLAccData}")
    public String RB36FileGeneration_getGLAccData;

    @Value("${RB36FileGeneration.Update.updateEodGLAccount}")
    public String RB36FileGeneration_updateEodGLAccount;

    @Value("${RB36FileGeneration.Select.isHoliday}")
    public String RB36FileGeneration_isHoliday;

}
