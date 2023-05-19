package com.epic.cms.util;


import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.ErrorMerchantBean;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Configurations {
    /*
     * Transaction types
     *
     */
    public static String TXN_TYPE_SIGN_ON;
    public static String TXN_TYPE_KEY_EXCHANGE;
    public static String TXN_TYPE_ECHO;
    public static String TXN_TYPE_SIGN_OFF;
    public static String TXN_TYPE_AUTHORIZATION_ADVICE;
    public static String TXN_TYPE_REDEEM;
    public static String TXN_TYPE_VOID_SALE;
    public static String TXN_TYPE_SALE;
    public static String TXN_TYPE_PRE_COMPLETION;
    public static String TXN_TYPE_CASH_ADVANCE;
    public static String TXN_TYPE_REVERSAL;
    public static String TXN_TYPE_INSTALLMENT;
    public static String TXN_TYPE_WITHDRAWAL;
    public static String TXN_TYPE_MINI_STATEMENT;
    public static String TXN_TYPE_BALANCE_INQUIRY;
    public static String TXN_TYPE_FUND_TRANSFER;
    public static String TXN_TYPE_PIN_CHANGE;
    public static String TXN_TYPE_PIN_VERIFY;
    public static String TXN_TYPE_PAYMENT;
    public static String TXN_TYPE_DEBIT_PAYMENT;
    public static String TXN_TYPE_FEE_AS_TRANSACTION;
    public static String TXN_TYPE_ADJUSTMENT_CREDIT;
    public static String TXN_TYPE_ADJUSTMENT_DEBIT;
    public static String TXN_TYPE_REVERSAL_ADVICE_CREDIT;
    public static String TXN_TYPE_BALANCE_TRANSFER;
    public static String TXN_TYPE_LOAN_ON_CARD;
    public static String TXN_TYPE_QUASI_CASH;
    public static String TXN_TYPE_FEE_INSTALLMENT;
    public static String TXN_TYPE_START_FETCH_STIP_ON_ADVICES;
    public static String TXN_TYPE_STOP_FETCH_STIP_ON_ADVICES;
    public static String TXN_TYPE_BATCH_UPLOAD;
    public static String TXN_TYPE_SETTLMENT;
    public static String TXN_TYPE_FORCE_SETTLMENT;
    public static String TXN_TYPE_AUTHORIZATION_REPEAT;
    public static String TXN_TYPE_REVERSAL_REPEAT;
    public static String TXN_TYPE_REVERSAL_ADVICE_QUASI_CASH;
    public static String TXN_TYPE_REVERSAL_INSTALLMENT;
    public static String TXN_TYPE_REFUND;
    public static String TXN_TYPE_MVISA_REFUND;
    public static String TXN_TYPE_MVISA_MERCHANT_PAYMENT;
    public static String TXN_TYPE_MVISA_ORIGINATOR;
    public static String TXN_TYPE_ISS_ON_US_MASTER;
    public static String TXN_TYPE_ISS_OFF_US_MASTER;
    public static String TXN_TYPE_CASH_ADVANCE_ISS_ON_US_MASTER;
    public static String TXN_TYPE_CASH_ADVANCE_ISS_OFF_US_MASTER;
    public static String TXN_TYPE_MONEY_SEND;
    public static String TXN_TYPE_MONEY_SEND_REVERSAL;
    public static String TXN_TYPE_MONEY_SEND_ADVICE;
    public static String TXN_TYPE_CUP_QR_PAYMENT;
    public static String TXN_TYPE_CUP_QR_REFUND;

    //Online Transaction Type here
    public static String TXN_TYPE_ONLINE_SIGN_ON;
    public static String TXN_TYPE_ONLINE_KEY_EXCHANGE;
    public static String TXN_TYPE_ONLINE_ECHO;
    public static String TXN_TYPE_ONLINE_SIGN_OFF;
    public static String TXN_TYPE_ONLINE_AUTHORIZATION_ADVICE;
    public static String TXN_TYPE_ONLINE_REDEEM;
    public static String TXN_TYPE_ONLINE_VOID_SALE;
    public static String TXN_TYPE_ONLINE_SALE;
    public static String TXN_TYPE_ONLINE_CASH_ADVANCE;
    public static String TXN_TYPE_ONLINE_REVERSAL;
    public static String TXN_TYPE_ONLINE_INSTALLMENT;
    public static String TXN_TYPE_ONLINE_WITHDRAWAL;
    public static String TXN_TYPE_ONLINE_MINI_STATEMENT;
    public static String TXN_TYPE_ONLINE_BALANCE_INQUIRY;
    public static String TXN_TYPE_ONLINE_FUND_TRANSFER;
    public static String TXN_TYPE_ONLINE_PIN_CHANGE;
    public static String TXN_TYPE_ONLINE_PIN_VERIFY;
    public static String TXN_TYPE_ONLINE_PAYMENT;
    public static String TXN_TYPE_ONLINE_DEBIT_PAYMENT;
    public static String TXN_TYPE_ONLINE_REVERSAL_INSTALLMENT;
    public static String TXN_TYPE_ONLINE_INSTALLMENT_PROCESSING_FEE;
    public static String TXN_TYPE_ONLINE_ADJUSTMENT_CREDIT;
    public static String TXN_TYPE_ONLINE_ADJUSTMENT_DEBIT;
    public static String TXN_TYPE_ONLINE_REVERSAL_ADVICE_CREDIT;
    public static String TXN_TYPE_ONLINE_BALANCE_TRANSFER;
    public static String TXN_TYPE_ONLINE_LOAN_ON_CARD;
    public static String TXN_TYPE_ONLINE_QUASI_CASH;
    public static String TXN_TYPE_ONLINE_FEE_INSTALLMENT;
    public static String TXN_TYPE_ONLINE_START_FETCH_STIP_ON_ADVICES;
    public static String TXN_TYPE_ONLINE_STOP_FETCH_STIP_ON_ADVICES;
    public static String TXN_TYPE_ONLINE_BATCH_UPLOAD;
    public static String TXN_TYPE_ONLINE_SETTLMENT;
    public static String TXN_TYPE_ONLINE_FORCE_SETTLMENT;
    public static String TXN_TYPE_ONLINE_AUTHORIZATION_REPEAT;
    public static String TXN_TYPE_ONLINE_REVERSAL_REPEAT;
    public static String TXN_TYPE_ONLINE_REVERSAL_ADVICE_QUASI_CASH;
    public static String TXN_TYPE_ONLINE_REFUND;
    public static String TXN_TYPE_ONLINE_MVISA_REFUND;
    public static String TXN_TYPE_ONLINE_MVISA_MERCHANT_PAYMENT;
    public static String TXN_TYPE_ONLINE_MVISA_ORIGINATOR;
    public static String TXN_TYPE_ONLINE_PRE_COMPLETION;
    public static String TXN_TYPE_ONLINE_OFFLINE_SALE;
    public static String TXN_TYPE_ONLINE_MVISA_ORIGINATOR_UNAVAIL_ADVICE;
    public static String TXN_TYPE_ONLINE_MONEY_SEND;
    public static String TXN_TYPE_ONLINE_MONEY_SEND_REVERSAL;
    public static String TXN_TYPE_ONLINE_MONEY_SEND_ADVICE;
    public static String TXN_TYPE_ONLINE_CUP_QR_PAYMENT;
    public static String TXN_TYPE_ONLINE_CUP_QR_REFUND;
    public static String TXN_TYPE_AFT;

    //files
    public static String STATEMENT_FILE_PATH;
    public static String AUTO_SETTLEMENT_FILE_PATH;
    public static String EOD_LETTER_FILE_PATH;
    public static String GLFILE_FILE_PATH;
    public static String MERCHANT_GLFILE_FILE_PATH;
    public static String EOD_LOGS_FILE_PATH;
    public static String EXPOSURE_FILE_PATH;
    public static String RB36_FILE_PATH;
    public static String MERCHANT_STATEMENT_PATH;
    public static String MERCHANT_CUSTOMER_STATEMENT_PATH;
    public static String MERCHANT_STATEMENT_SUMMARY_PATH;
    public static String OUTGOING_CTF_FILE_PATH;
    public static String CASHBACK_FILE_PATH;
    public static String MERCHANT_PAYMENT_FILE_PATH;
    public static String BULK_APPLICATION_FILE_PATH;
    public static String TOPAN_FILE_PATH_CSV;
    public static String OUTGOING_IPM_FILE_PATH;
    public static String MASTERCARD_ABU_FILE_PATH;
    public static String EOD_DASHBOARD_CONSOLELOG_FILE_PATH;
    public static String OUTGOING_CUP_FILE_PATH;


    public static String SERVER_RUN_PLATFORM;
    public static int PROCESS_ID_EOD_PARAMETER_RESET;
    public static boolean COMMIT_STATUS;
    public static boolean FLOW_STEP_COMPLETE_STATUS;
    public static boolean PROCESS_FLOW_STEP_COMPLETE_STATUS;
    public static boolean MAIN_EOD_STATUS;
    public static volatile int PROCESS_SUCCESS_COUNT = 0;
    public static volatile int PROCESS_FAILD_COUNT = 0;
    public static String EOD_USER;
    public static int EOD_ID;
    public static int ERROR_EOD_ID;
    public static String STARTING_EOD_STATUS;
    public static int RUNNING_PROCESS_ID = 0;
    public static int PROCESS_ID_INITIAL_PROCESS;
    public static int PROCESS_CARD_FEE;
    public static int PROCESS_ID_LOAN_ON_CARD;
    public static int PROCESS_CHEQUERETURN;
    public static int PROCESS_EOD_RUNNABLE_FEE;

    public static String PROCESS_PROGRESS = "N/A";
    public static volatile int PROCESS_TOTAL_NOOF_TRABSACTIONS = 0;
    public static volatile boolean IS_PROCESS_ERROR = false;
    public static int PROCESS_ID_CARD_REPLACE;
    public static int START_INDEX;
    public static int END_INDEX;
    public static String PATTERN_CHAR;
    public static String EOD_DONE_STATUS;
    public static String YES_STATUS;
    public static String CARD_REPLACE_ACCEPT;
    public static Date EOD_DATE;
    public static String EOD_DATE_String;
    /**
     * adjustment types
     */
    public static int LOYALTY_ADJUSTMENT_TYPE;
    public static int PAYMENT_ADJUSTMENT_TYPE;
    public static int TRANSACTION_ADJUSTMENT_TYPE;
    public static int FEE_ADJUSTMENT_TYPE;
    public static int INTEREST_ADJUSTMENT_TYPE;
    public static int CASH_ADVANCE_ADJUSTMENT_TYPE;
    public static int INSTALLMENT_ADJUSTMENT_TYPE;
    public static int GOVERNMENT_STAMP_DUTY_ADJUSTMENT_TYPE;
    public static int CASHBACK_ADJUSTMENT_TYPE;

    /**
     * fee codes
     */
    public static String FOREX_MARKUP_FEE;
    public static String JOINING_FEE;
    public static String ANNUAL_FEE;
    public static String SUPPLEMENTARY_CARD_FEE;
    public static String VOUCHER_COPY_RETRIEVAL_CHARGE_FEE;
    public static String UNSUCCESSFUL_STANDING_INSTRUCTION_CHARGE_FEE;
    public static String INSTALLMENT_PLAN_SETUP_FEE;
    public static String STANDING_INSTRUCTION_SETUP_FEE;
    public static String CARD_RENEWAL_FEE;
    public static String STATEMENT_COPY_CHARGE_FEE;
    public static String FUEL_SURCHARGE_FEE;
    public static String CHEQUE_RETURN_ON_PAYMENTS_OTHER_REASONS_FEE;
    public static String CHEQUE_RETURN_ON_PAYMENTS_INSUFFICIENT_FUNDS_FEE;
    public static String CHEQUE_RETURN_ON_PAYMENTS_STOP_FEE;
    public static String EARLY_RENEWAL_FEE;
    public static String BILLING_CYCLE_CHANGE_FEE;
    public static String PIN_RESET_FEE;
    public static String CARD_REPLACEMENT_FEE;
    public static String CASH_ADVANCE_FEE;
    public static String OVER_LIMIT_FEE;
    public static String LIMIT_ENHANCEMENT_FEE;
    public static String LATE_PAYMENT_FEE;
    public static String STAMP_DUTY_FEE;
    public static int PROCESS_ID_ACQUIRING_TXN_UPDATE_PROCESS;
    public static String CREDIT;
    public static String DEBIT;
    public static int CHANNEL_TYPE_VISA;
    public static String VISA_ASSOCIATION;
    public static int CHANNEL_TYPE_MASTER;
    public static String MASTER_ASSOCIATION;
    public static int CHANNEL_TYPE_CUP;
    public static String CUP_ASSOCIATION;
    public static String BASE_CURRENCY;
    public static int PAYMENT_REVERSAL_PROCESS;
    public static int PROCESS_EODPAYMENTUPDATE;
    public static int PROCESS_ID_TXNMISMATCH_POST;
    public static int EOD_ACQUIRING_STATUS;
    public static String EOD_PENDING_STATUS;
    public static int EOD_CONSIDER_STATUS;
    public static int PROCESS_STEP_ID;// used for process category thread
    public static int PROCESS_ID_CARD_PERMENANT_BLOCK;
    public static int NO_OF_MONTHS_FOR_PERMENANT_BLOCK;
    public static String PERM_BLOCK_REASON;
    public static String ACTIVE_STATUS;
    public static int ONLINE_ACTIVE_STATUS;
    public static int PROCESS_ID_CARD_TEMPORARY_BLOCK;
    public static int NO_OF_MONTHS_FOR_TEMPORARY_BLOCK;
    public static String TEMP_BLOCK_REASON;
    public static int PROCESS_ID_INCREMENT_LIMIT_EXPIRE;
    public static String CREDIT_INCREMENT;
    public static String CARD_CATEGORY_MAIN;
    public static String CARD_CATEGORY_ESTABLISHMENT;
    public static String CARD_CATEGORY_FD;
    public static String CARD_CATEGORY_AFFINITY;
    public static String CARD_CATEGORY_CO_BRANDED;
    public static String CASH_INCREMENT;
    public static int PROCESS_LIMIT_ENHANCEMENT;
    public static int Failed_Count_Card_Temporary_Block = 0;
    public static int Failed_Count_Card_Limit_Enhancement = 0;
    public static int Iterator_Card_Limit_Enhancement = 1;
    public static String LIMIT_INCREMENT;
    public static String LIMIT_DECREMENT;
    public static int PROCESS_CLEAR_MINPAYMENTS_AND_TEMPBLOCK;
    public static int Failed_Count_IncrementLimit = 0;
    public static int Failed_Count_FileValidate = 0;
    public static int Failed_File_Count_FileRead = 0;
    public static int Failed_FileName_Count_FileRead = 0;
    public static int ONLINE_LOG_LEVEL;
    public static int COLLECTION_AND_RECOVERY_NOTIFICATION;
    public static String TP_X_DATES_BEFORE_FIRST_DUE_DATE;
    public static String TP_X_DATES_AFTER_FIRST_DUE_DATE;
    public static String TP_X_DAYS_AFTER_THE_2ND_STATEMENT_DATE;
    public static String TP_ON_THE_2ND_STATEMENT_DATE;
    public static String TP_IMMEDIATELY_AFTER_THE_2ND_DUE_DATE;
    public static String TP_ON_THE_3RD_STATEMENT_DATE;
    public static String TP_IMMEDIATELY_AFTER_THE_3RD_DUE_DATE;
    public static String TP_ON_THE_4TH_STATEMENT_DATE;
    public static String TP_X_DAYS_AFTER_THE_4TH_STATEMENT_DATE;
    public static String TP_WITHIN_X_DAYS_OF_THE_CRIB_INFO_LETTER_REMINDER;
    public static String TP_IMMEDIATELY_AFTER_THE_4TH_DUE_DATE;
    public static String NO_STATUS;
    public static int noOfCardsForCollectionAndRecoveryNotification = 0;
    public static int failedCardsForCollectionAndRecoveryNotification = 0;
    public static boolean checkErrorForCollectionAndRecoveryNotification = false;
    public static int Failed_Count_Card_Permanent_Block = 0;
    public static int ReversedTxnCount = 0;
    public static int failedCount_TxnMisMatchProcess = 0;
    //AcqTxnUpdate Txn Counts
    public static int totalTxnCount_AcqTxnUpdateProcess = 0;
    public static int failedTxnCount_AcqTxnUpdateProcess = 0;
    public static int acqFailedMerchantCount_AcqTxnUpdateProcess = 0;
    public static int onusTxnCount_AcqTxnUpdateProcess = 0;

    public static String COMPLETE_STATUS;
    public static String INITIAL_STATUS;
    public static int PROCESS_PAYMENT_FILE_VALIDATE;

    @SuppressWarnings("unchecked")
    public static Hashtable PAYMENT_VALIDATION_HASH_TABLE;
    public static Hashtable<String, String[]> ATM_VALIDATION_HASH_TABLE;


    public static String PAYMENT_FILE_CHEQUE_INITIATE_TXN_TYPES;
    public static String PAYMENT_FILE_CHEQUE_RETURN_TXN_TYPES;
    public static String USER;

    public static int PROCESS_ID_CARDAPPLICATION_LETTER_REJECT;
    public static String APPLICATION_REJECTION_LETTER_CODE;
    public static int PROCESS_ATM_FILE_VALIDATE;
    public static int PROCESS_ATM_FILE_READ = 167;
    public static int PROCESS_PAYMENT_FILE_READ;
    public static String FAIL_STATUS;

    //File code
    public static String FILE_CODE_PAYMENT = "PAYMENT";
    public static String FILE_CODE_ATM;
    public static String FILE_CODE_MONITOR;
    public static String FILE_CODE_WALLET;
    public static String FILE_CODE_BULK_APPLICATION;
    public static String FILE_CODE_MASTERCARD_T67;
    public static String FILE_CODE_DCF;
    public static String FILE_CODE_VISA = "VISA";
    //Letter
    public static String CARD_CATEGORY_SUPPLEMENTORY;
    public static String CARD_CATEGORY_AFFINITY_SUPPLEMENTORY;
    public static String CARD_CATEGORY_CO_BRANDED_SUPPLEMENTORY;
    public static String CARD_CATEGORY_FD_SUPPLEMENTORY;
    public static String CARD_CATEGORY_CORPORATE;

    public static int PROCESS_STAMP_DUTY_FEE;

    //Country Code
    public static String COUNTRY_CODE_SRILANKA;

    //cashback
    public static int CBREDEEMDAYCOUNT;
    public static int PROCESS_CASHBACK;


    public static int PROCESS_CARD_EXPIRE;

    public static int failedCount_CardExpireProcess = 0;
    public static int PROCESS_CRIB_FILE;
    public static int PROCESS_ID_OVER_LIMIT_FEE;
    public static int failedCount_OverLimitFeeProcess = 0;
    public static int noOfCardCount_OverLimitFeeProcess = 0;
    public static int PROCESS_ID_CHEQUEPAYMENT;
    public static int PROCESS_ID_SNAPSHOT;
    public static int PROCESS_TRANSACTION_DROP_REQUEST;
    public static int SuccessCount_TxnDropRequest = 0;
    public static int FailedCount_TxnDropRequest = 0;
    public static int FailedCards_TxnDropRequest = 0;
    public static int ONLINE_REVERSE_STATUS;
    public static int ONLINE_DROP_STATUS;
    public static int ONLINE_PARTIALLY_REVERSE_STATUS;
    public static int ONLINE_TXN_INCOMPLETE_STATUS;
    public static int EOD_ISSUING_STATUS;
    public static int PROCESS_ID_LOYALTY_POINT_CALCULATION_PROCESS;
    public static int LOYALTY_ACCUMILATION_VALUE;
    public static int LOYALTY_EXPIARY_PERIOD;
    public static int LOYALTY_MINIMUM_POINT;
    public static int PROCESS_ID_CASH_BACK_ALERT_PROCESS;
    public static String CASH_BACK_SMS_CODE;
    public static String STMT_WITH_CASH_BACK_SMS_CODE;
    public static String STMT_WITHOUT_CASH_BACK_SMS_CODE;
    public static volatile int successCardNoCount_CashBackAlert = 0;
    public static volatile int failedCardNoCount_CashBackAlert = 0;
    public static int PROCESS_ID_COLLECTION_AND_RECOVERY_ALERT_PROCESS;
    public static String X_DAYS_BEFORE_1_DUE_DATE;
    public static String X_DAYS_BEFORE_1_DUE_DATE_EMAIL_CODE;
    public static String X_DAYS_BEFORE_1_DUE_DATE_SMS_CODE;
    public static String IMMEDIATE_AFTER_1_DUE_DATE;
    public static String IMMEDIATE_AFTER_1_DUE_DATE_EMAIL_CODE;
    public static String IMMEDIATE_AFTER_1_DUE_DATE_SMS_CODE;
    public static String IMMEDIATE_AFTER_2_DUE_DATE;
    public static String IMMEDIATE_AFTER_2_DUE_DATE_EMAIL_CODE;
    public static String IMMEDIATE_AFTER_2_DUE_DATE_SMS_CODE;
    public static String IMMEDIATE_AFTER_3_DUE_DATE;
    public static String IMMEDIATE_AFTER_3_DUE_DATE_EMAIL_CODE;
    public static String IMMEDIATE_AFTER_3_DUE_DATE_SMS_CODE;
    public static String EMAIL = "E-MAIL";
    public static String SMS = "SMS";

    public static String LETTER = "LETTER";
    public static String EMAIL_TEMPLATE;
    public static String SMS_TEMPLATE;
    public static int successCardNoCount_CollectionAndRecoveryAlert = 0;
    public static int failedCardNoCount_CollectionAndRecoveryAlert = 0;
    public static int EMAIL_TEMPLATE_CODE;
    public static int SMS_TEMPLATE_CODE;
    public static int PROCESS_ID_CARDAPPLICATION_LETTER_APPROVE;
    public static int ALERTUNUNREAD;
    public static String STATEMENT_DATE = "Statement Date";
    public static String BEFORE_DUE_DATE = "Before Due Date";
    public static String AUTO_SETTLEMENT_GENERATE_DATE = "Auto Settlment File Generate";
    public static String CRIB_FILE_GENERATE_DATE = "CRIB File Generate";
    public static String CASHBACK_REDEEM = "Cash Back Redeem";

    //gl accounts
    public static String TXN_TYPE_UNEARNED_INCOME;
    public static String TXN_TYPE_UNEARNED_INCOME_UPFRONT_FALSE;
    public static String TXN_TYPE_FEE_INSTALLMENT_UPFRONT_FALSE;

    public static int ANNUAL_FEE_FOR_NP_ACCOUNTS;

    //Summary of Card processing
    public static int SUMMARY_FOR_FEE_UPDATE = 0;
    public static int SUMMARY_FOR_FEE_ANNIVERSARY = 0;
    public static int SUMMARY_FOR_FEE_ANNIVERSARY_PROCESSED = 0;
    public static int SUMMARY_FOR_FEE_CASHADVANCES = 0;
    public static int SUMMARY_FOR_FEE_LATEPAYMENTS = 0;
    public static int FAILED_CARDS = 0;

    public static int PROCESS_ID_KNOCK_OFF;

    public static int PROCESS_ID_BALANCE_TRANSFER;
    public static int PROCESS_ID_EASY_PAYMENT;

    //Manual Np Process
    public static int PROCESS_ID_MANUAL_NP_PROCESS;
    public static String INTEREST_ON_THE_NP_GL;
    public static String OUTSTANDING_ON_THE_NP_GL;
    public static String KNOCKOFF_NP_INTEREST_DECLASSIFIED_GL;
    public static String KNOCKOFF_NP_OUTSTANDING_DECLASSIFIED_GL;
    public static String KNOCKOFF_ACCRUED_LATE_FEE_DECLASSIFIED_GL;
    public static String KNOCKOFF_ACCRUED_INTEREST_DECLASSIFIED_GL;
    public static String KNOCKOFF_ACCRUED_OVERLIMIT_FEE_DECLASSIFIED_GL;
    public static String KNOCKOFF_ACCRUED_OTHER_FEES_DECLASSIFIED_GL;

    //Online to Backend Txn Syn
    public static String ONLINE_LISTNER_TYPE_ATM;
    public static String ONLINE_LISTNER_TYPE_NAC;
    public static int EOD_NOT_CONSIDER_STATUS;
    public static int ADJUSTMENT_PROCESS;
    public static int PROCESS_ID_FEE_POST;

    public static int VISA_TXN_UPDATE_COUNT = 0;
    public static int MASTER_TXN_UPDATE_COUNT = 0;
    public static int FAILED_VISA_TXN_COUNT = 0;
    public static int FAILED_MASTER_TXN_COUNT = 0;
    //EOM Interest Calculation
    public static String TXN_TYPE_INTEREST_INCOME;
    public static String NP_ACCRUED_INTEREST_GL;
    public static int PROCESS_ID_EOM_INTEREST_CALCULATION;

    public static int ADJUSTMENT_SEQUENCE_NO;

    //--------------
    public static int PROCESS_INTEREST_CALCULATION;

    ///////////////////
    public static boolean IS_PROCESS_COMPLETELY_FAILED = false;
    public static boolean EOD_SHEDULER;
    public static int BATCH_SIZE;
    public static Timestamp START_TIME;
    public static Timestamp END_TIME;
    public static boolean isDevMode = false;
    public static boolean isFirstRun = true;
    public static boolean isManualMode = false;
    public static boolean isMainEOD = false;
    public static boolean isSubEOD = false;
    public static boolean isOneEODCompleted = false;
    public static boolean EOD_LIVE_MODE;
    /* EOD Scheduling HH:MM*/
    public static int MAIN_EOD_STARTTIME_H;
    public static int MAIN_EOD_STARTTIME_M;
    public static int SUB_EOD_STARTTIME_H;
    public static int SUB_EOD_STARTTIME_M;
    /* Executor Thread Pool*/
    public static int CORE_POOL_SIZE;
    public static int MAX_POOL_SIZE;
    public static int KEEP_ALIVE_TIME;

    /*card domain*/
    public static String CARD_DOMAIN_CREDIT;
    public static String CARD_DOMAIN_DEDIT;

    public static String DEACTIVE_STATUS;
    public static String CARD_INITIAL_STATUS;
    public static String CREDIT_STATUS;
    public static int ONLINE_DEACTIVE_STATUS;

    public static int ONLINE_OFUS_BIN;
    /**
     * public static String CARD_TYPE_VISA;
     * public static String CARD_TYPE_MASTER;
     * public static String CARD_TYPE_AMEX;
     * public static String CARD_TYPE_VISA_PLUS;
     * public static String CARD_TYPE_MAESTRO;
     * public static String CARD_TYPE_ATM_PR;
     * public static String CARD_TYPE_CIRRUS;
     * public static String CARD_TYPE_VISA_DEBIT;
     * public static String CARD_TYPE_LOYALTY;
     * public static String CARD_TYPE_GIFT;
     * public static String CARD_TYPE_VISA_ELECTRON;
     * public static String CARD_TYPE_DIACOVER;
     * public static String CARD_TYPE_JCB;
     * public static String CARD_TYPE_DINERS;
     * public static String CARD_TYPE_CUP;
     * public static String CARD_TYPE_CREDIT_PR;
     */

    public static boolean SUB_EOD_STATUS;
    public static int STEP_CATEGORY_COUNT;
    public static int STEP_PROCESS_COUNT;
    public static String EOD_VERSION;

    public static boolean PROCESS_SETTLEMENT_FILE;
    public static String MERCHANT_PAYMENT_FILE_PATH_SLIPS;
    public static String MERCHANT_PAYMENT_FILE_PATH_CHEQUE;
    public static String MERCHANT_PAYMENT_FILE_PATH_DIRECT;
    public static String MERCHANT_STAT_AUTOMATE_FILE_PATH;
    public static String MERCHANT_STAT_AUTOMATE_FILE_NAME;

    //Set EOD user in here MAnulaly
    public static String SERVER_IP;
    public static int SERVER_PORT;
    public static int MONITOR_PORT;
    public static int BACKLOG;
    public static String SERVER_NAME;
    public static int SERVER_LOG_LEVEL;
    public static String SETTLEMENT_FILE_PATH;
    public static String HOME_FOLDER;
    public static String PATH_CONFIG_FILE;
    public static String PATH_LOGS_INFOR;
    public static String PATH_LOGS_ERROR;
    public static Boolean FILEHEADER;
    public static Boolean BATCHHEADER;
    public static int TXNCOUNT;
    public static int FILETXNCOUNT;
    public static int BATCHCOUNT;
    public static double TXNAMOUNT;
    public static double FILETXNAMOUNT;
    public static int CATEGORY_THREAD_POOL_MIN_SIZE;
    public static int CATEGORY_THREAD_POOL_MAX_SIZE;
    public static int PROCESS_THREAD_POOL_MIN_SIZE;
    public static int PROCESS_THREAD_POOL_MAX_SIZE;
    //EOD step IDs
    public static int MAIN_EOD_STEPID;
    public static int SUB_EOD_STEPID;
    //internal_keys
    public static int INTERNAL_KEY_CHEQUE;
    public static int INTERNAL_KEY_CASH;

    // accOTBs
    public static HashMap<String, Double> AccOpeningOTBs;

    //EOD Process IDs
    public static int MAIN_EOD_PROCESS;
    public static int PROCESS_ID_PINGENERATION;
    public static int PROCESS_ID_OVER_LIMIT_INTEREST_CALCULATION;
    public static int PROCESS_ID_CREDIT_SCORE;
    public static int PROCESS_MERCHANT_TRANSACTION;
    public static int PROCESS_MERCHANT_SETTLEMENT;
    public static int PARAMETER_INITIALIZATION;
    public static int PROCESS_EMBOSS_FILEGENERATION;
    public static int PROCESS_MERCHANT_PAYMENT_CYCLE;
    public static int PROCESS_CREATE_CRIB_REQUEST;
    public static int PROCESS_EXPOSURE_FILE;
    public static int PROCESS_EOM_SUP_CARD_RESET;
    public static int PROCESS_ID_CASHBACK_FILE_GENERATION;
    public static int PROCESS_CARD_RENEW;
    public static int PROCESS_MONTHLY_STATEMENT;
    public static int PROCESS_ID_VISA_BASEII_CLEARING;
    public static int PROCESS_ID_COPY_VISA_TRANSACTION_TO_BACKEND;
    public static String MERCHANT_PAYMENT_STARTDATE;
    public static int PROCESS_ONLINETOBACKEND_TXNSYNC;
    public static int AUTO_SETTLEMENT_PROCESS;
    public static int PROCESS_VISA_SETTELEMENTDATEUPDATE;//134
    public static int PROCESS_EODTRANSACTIONUPDATE;// 5
    public static int PROCESS_EODCASHADVANCEUPDATE;// 170
    public static int PROCESS_VISA_BIN_FILE_READ_PROCESS;// 201
    public static int PROCESS_PRE_MERCHANT_FEE_PROCESS;// 212
    public static int PROCESS_ACQUIRING_ADJUSTMENT_PROCESS;// 214
    public static int PROCESS_WALLET_SETTLEMENT_FILE_READ;
    public static int PROCESS_WALLET_SETTLEMENT_FILE_UPDATE;
    public static int PROCESS_MASTER_CARD_T67_FILE_READ;

    //outgoing ctf file generation
    public static int MAXIMUM_TCRS_IN_CTF_BATCH;
    public static int PROCESS_ID_OUTGOING_CTF_FILE_GEN;
    public static String MANUAL_CASH_MCC;
    public static String VISA_ACQ_BIN;

    // outgoing statement file (UPI) generation
    public static int PROCESS_ID_OUTGOING_CUP_FILE_GEN;

    public static int PROCESS_ID_OUTGOING_IPM_FILE_GEN;
    public static long MASTER_OUT_SEQUENCE_NUMBER = 0;
    public static String GENERATED_IPM_FILE_ID;
    public static long GENERATED_IPM_AMOUNT_CHECKSUM;
    public static int GENERATED_IPM_TRANSACTION_COUNT;

    public static int PROCESS_DCF_FILE_READ;
    public static int PROCESS_DCF_FILE_VALIDATE;
    public static int PROCESS_PAYMENT_KNOCK_OFF;
    public static int PROCESS_DAILY_INTEREST_CALCULATION;
    public static int PROCESS_DAILY_STATEMENT_INTEREST_CALCULATION;
    public static int PROCESS_TRANSACTION_MISMATCH_UPDATE;

    public static int PROCESS_CHECK_PAYMENTS_FOR_MIN_AMOUNT;
    public static int PROCESS_ID_RISK_CALCULATION_PROCESS;
    public static int PROCESS_ID_MASTER_CLEARING;
    public static int PROCESS_ID_MASTER_CARD_ABU_FILE_GEN; //401
    public static int PROCESS_ID_CARDRENEW_LETTER;
    public static int PROCESS_ID_CARDREPLACE_LETTER;
    public static String STATEMENT_NOTIFY_MSG;
    public static int PROCESS_ID_GL_FILE_CREATION;
    public static int PROCESS_RB36_FILE_CREATION;

    public static int PROCESS_ID_COLLECTION_AND_RECOVERY_LETTER_PROCESS;

    public static int PROCESS_ID_BULK_APPLICATION_READ_PROCESS;
    public static int PROCESS_ID_BULK_APPLICATION_VALIDATE_PROCESS;

    public static String GL_SUMMARY_FILE_PREFIX;
    public static String MERCHANT_GL_SUMMARY_FILE_PREFIX;
    public static String OUTPUTFILE_FIELD_DELIMETER;
    public static String CASHBACK_FILE_PREFIX_F1;
    public static String MERCHANT_PAYMENT_FILE_DIRECT_PREFIX_F1;
    public static String MERCHANT_PAYMENT_FILE_SLIPS_PREFIX;
    public static String RB36_FILE_PREFIX;
    public static String AUTOSETTLEMENT_FILE_PREFIX;
    public static String CASHBACK_FILE_PREFIX_F2;
    public static String MERCHANT_PAYMENT_FILE_DIRECT_PREFIX_F2;

    //Card temorary block process
    public static int PROCESS_ID_TXN_POST;

    /*crib request parameters*/
    public static String REPORT_ID;
    public static String SUBJECT_TYPE;
    public static String RESPONSE_TYPE;
    public static String INQUIRY_REOSON_CODE;
    public static String BULK_STATUS;
    public static String STATUS;

    //EOD File Processing
    public static volatile int PROCESS_ATM_FILE_CLEARING_SUCCESS_COUNT = 0;
    public static volatile int PROCESS_ATM_FILE_CLEARING_FAILD_COUNT = 0;
    public static volatile int PROCESS_ATM_FILE_CLEARING_TOTAL_NOOF_TRABSACTIONS = 0;
    public static volatile int PROCESS_ATM_FILE_CLEARING_INVALID_COUNT = 0;

    public static volatile int PROCESS_PAYMENT_FILE_CLEARING_SUCCESS_COUNT = 0;
    public static volatile int PROCESS_PAYMENT_FILE_CLEARING_FAILD_COUNT = 0;
    public static volatile int PROCESS_PAYMENT_FILE_CLEARING_TOTAL_NOOF_TRABSACTIONS = 0;
    public static volatile int PROCESS_PAYMENT_FILE_CLEARING_INVALID_COUNT = 0;

    public static volatile int PROCESS_VISA_FILE_CLEARING_SUCCESS_COUNT = 0;
    public static volatile int PROCESS_VISA_FILE_CLEARING_FAILD_COUNT = 0;
    public static volatile int PROCESS_VISA_FILE_CLEARING_TOTAL_NOOF_TRABSACTIONS = 0;
    public static volatile int PROCESS_VISA_FILE_CLEARING_INVALID_COUNT = 0;

    public static volatile int PROCESS_MASTER_FILE_CLEARING_SUCCESS_COUNT = 0;
    public static volatile int PROCESS_MASTER_FILE_CLEARING_FAILD_COUNT = 0;
    public static volatile int PROCESS_MASTER_FILE_CLEARING_TOTAL_NOOF_TRABSACTIONS = 0;
    public static volatile int PROCESS_MASTER_FILE_CLEARING_INVALID_COUNT = 0;


    /*BASE II Clearing*/
    public static int VISA_THREAD_POOL_MAX_SIZE;
    public static int VISA_THREAD_POOL_MIN_SIZE;
    public static boolean isTxnThreadStart;
    public static int VISA_THREAD_POOL_AVERAGE_SIZE;/*BASE II Clearing*/
    public static int VISA_CURRENTLY_AVAILABLE_THREADS;
    public static int VISA_CURRENTLY_BUSY_THREADS;
    public static int VISA_QUEUE_MAX_SIZE;
    /*BASE II Clearing*/

    //EOD Log Path
    public static String PATH_LOG_WINDOWS;
    public static String PATH_LOG_LINUX;
    public static String MAIN_EOD_LOG_FOLDER;
    public static String SUB_EOD_LOG_FOLDER;
    public static String EOD_CONFIG_DATA_FOLDER;
    public static String EOD_MONITOR_LOG_FOLDER;

    //letter type
    public static String CARD_REPLACEMENT_LETTER_CODE;
    public static String SECOND_REMINDER_LETTER_CODE;
    public static String CARD_RENEWAL_LETTER_CODE;
    public static String APPLICATION_CONFIRMATION_LETTER_CODE;
    public static String FIRST_REMINDER_LETTER_CODE;
    public static String PRODUCT_CHANGE_LETTER_CODE;

    //autosettlement collection account
    public static String COLLECTION_ACCOUNT;
    //EOD Statistics File
    public static String PATH_EOD_STATISTICS_WINDOWS;
    public static String PATH_EOD_STATISTICS_LINUX;
    public static String EOD_STATISTICS_FILE;

    //exposure file
    public static String EXPOSURE_FILE_BRANCH;
    public static String EXPOSURE_FILE_PRODUCT;
    public static String EXPOSURE_FILE_FACILITY_TYPE;
    public static int VISA_QUEUE_CURRENT_SIZE;
    public static boolean ERROR_VISA_FILE;
    public static boolean REJECT_VISA_FILE;
    public static boolean UNAVAILABLE_VISA_FILE;
    public static boolean PROCESS_VISA_FILE;
    public static boolean SUCCESS_PROCESS_VISA_FILE;
    public static boolean VISA_FILE_LISTNER_STATUS;
    public static String SERVER_VERSION;
    public static String PATH_DBCONFIG;
    public static String PATH_ROOT;
    public static String PATH_BACKUP_WINDOWS;
    public static String PATH_BACKUP_LINUX;
    public static String PATH_BACKUP;
    public static String PATH_LOGS_MCC;
    public static String PATH_VISA_FILE;
    public static String PATH_VISA_FILE_WINDOWS;
    public static String PATH_VISA_FILE_LINUX;
    public static int FILE_QUEUE_MAX_SIZE;
    public static int THREAD_MAX_POOL_SIZE;
    public static int THREAD_MIN_POOL_SIZE;
    public static int THREAD_AVERAGE_POOL_SIZE;
    public static String DB_NAME;
    public static String DB_DRIVER;
    public static String DB_URL;
    public static String DB_USERNAME;
    public static String DB_PASSWORD;
    public static int DB_POOL_INIT_SIZE;
    public static int DB_POOL_MAX_SIZE;
    public static int DB_POOL_MIN_SIZE;
    public static int DB_POOL_COUNT_AVAILABLE;
    public static int DB_POOL_COUNT_BUSSY;
    public static int DB_POOL_COUNT_OPENED;
    public static int DB_CONNECTION_TIMEOUT;
    public static int DB_CONNECTION_EXPIRE_TIMEOUT;
    public static int VISA_BASEII_CLEANING_CURRENTLY_AVAILABLE_THREADS;
    public static int VISA_BASEII_CLEANING_CURRENTLY_BUSY_THREADS;
    public static int VISA_BASEII_CLEANING_QUEUE_CURRENT_SIZE;
    //public static TxnQueueVISA QUEUE_BASEII_CLEANING_VISA_TRANSACTIONS;
    public static int PROGRESS_VISA_FILE;
    public static int SIZE_VISA_FILE;
    public static String SESSION;
    public static String VALIDPERIOD;
    public static ArrayList<String> CURRENCY_CODES_VISA_LIST;
    public static ArrayList<String> COUNTRY_CODES_VISA_LIST;
    public static ArrayList<String> CATEGORY_CODES_VISA_LIST;
    public static ArrayList<String> VISA_FINANCIAL_TXN_LIST;

    public static String EOD_TXN_AUTH_ONLY_STATUS;
    public static String EOD_TXN_AUTH_ONLY_INIT;
    public static String EOD_TXN_AUTH_ONLY_POSTED;
    public static int ONLINE_TXN_EOD_COPIED_STS;
    public static int ONLINE_TXN_EOD_NOT_COPIED_STS;

    //acquiring fee codes
    public static String MERCHANT_ANNUAL_FEE;
    public static String MERCHANT_BI_MONTHLY_FEE;
    public static String MERCHANT_QUARTERLY_FEE;
    public static String MERCHANT_HALF_YEARLY_FEE;
    public static String TERMINAL_MONTHLY_RENTAL_FEE;
    public static String TERMINAL_BI_MONTHLY_RENTAL_FEE;
    public static String TERMINAL_HALF_YEARLY_RENTAL_FEE;
    public static String TERMINAL_WEEKLY_RENTAL_FEE;
    public static String TERMINAL_QUARTERLY_RENTAL_FEE;
    public static String TERMINAL_MAINTAINACE_FEE;

    public static String PATH_MASTER_FILE;
    public static String PATH_MASTER_FILE_WINDOWS;
    public static String PATH_MASTER_FILE_LINUX;

    public static String FIRST_PRESENTMENT_MTI = "1240";
    public static int INCOMMING_IPM_FILE_ENCODING_FORMAT;
    public static int DCF_ICA_FOR_AIIC;
    public static int DCF_ICA_FOR_AIIC_VISA;
    public static int OUTGOING_IPM_FILE_ENCODING_FORMAT;
    public static int OUTGOING_IPM_FILE_LAYOUT;
    public static String MASTER_ACQ_BIN;

    public static boolean PROCESS_MASTER_FILE;

    public static int READ_T67_IP0040T1_TABLE;
    public static int READ_T67_IP0075T1_TABLE;


    @SuppressWarnings("unchecked")
    public static Hashtable VISA_VALIDATION_HASH_TABLE;
    @SuppressWarnings("unchecked")
    public static Hashtable VISA_FIELDS_HASH_TABLE;
    public static boolean STATUS_SERVER_CONSOLE;
    public static int BATCH_PROCESSING_COUNT;
    /*visa bin file processing*/
    @SuppressWarnings("unchecked")
    public static Hashtable VISA_BIN_FIELDS_HASH_TABLE;
    @SuppressWarnings("unchecked")
    public static Hashtable VISA_BIN_VALIDATION_HASH_TABLE;

    public static HashMap<String, String> VISA_TXN_FIELD_TABLE;
    public static HashMap<String, String> OUTGOING_VISA_REJECT_REASON_TABLE;
    public static HashMap<String, String> OUTGOING_MASTER_REJECT_REASON_TABLE;
    public static HashMap<String, String> CURRENCY_EXPONENT_TABLE;
    public static String OUTGOING_CUP_FILE_NAME_PREFIX;
    public static String OUTGOING_CUP_FILE_NAME_SUFIX;
    public static HashMap<String, String> OUTGOING_CUP_FILE_TXN_BLOCK_FIELD_TABLE;
    public static HashMap<String, String> OUTGOING_CUP_FILE_REJECT_REASON_TABLE;
    public static String INSTITUTION_IDENTIFICATION_NUMBER;
    public static String CUP_FILE_HEADER_TXN_CODE;
    public static String CUP_FILE_TAILER_TXN_CODE;


    public static String OUTGOING_CUP_FILE_HEADER_VERSION_TAG;
    public static String OUTGOING_CUP_FILE_HEADER_VERSION_NUMBER;
    public static int PROCESS_CUP_BIN_FILE_READ;

    //GlTxn types
    public static String PERPORMING_LOAN_GL;
    public static String UNEARNED_INCOME_UPFRON_FALSE_POSITION;
    public static String FEE_INSTALLMENT_UPFRON_FALSE_POSITION;
    public static String INSTALLMENT_LOAN_GL;
    public static String UNEARNED_INCOME_GL;
    public static String FEE_RECOVER_GL;
    public static String UNEARNED_INCOME;
    public static String INSTALLMENT_FEE;
    public static String TXN_TYPE_ISS_ON_US;
    public static String TXN_TYPE_ISS_OFF_US;
    public static String TXN_TYPE_ACQ_ON_US;
    public static String TXN_TYPE_ACQ_OFF_US;
    public static String TXN_TYPE_ACQ_ON_US_EASYPAY;
    public static String TXN_TYPE_CASH_ADVANCE_ISS_ON_US;
    public static String TXN_TYPE_CASH_ADVANCE_ISS_OFF_US;
    public static String TXN_TYPE_CASH_ADVANCE_ACQ_ON_US;
    public static String TXN_TYPE_CASH_ADVANCE_ACQ_OFF_US;
    public static String TXN_TYPE_MVISA_ORI_ON_US;
    public static String TXN_TYPE_MVISA_ORI_OFF_US;
    public static String TXN_TYPE_MVISA_RECI_ON_US;
    public static String TXN_TYPE_MVISA_RECI_OFF_US;
    public static String TXN_TYPE_PAYMENT_DIRECT;
    public static String TXN_TYPE_PAYMENT_SLIPS;
    public static String TXN_TYPE_PAYMENT_CHEQUE;
    public static String TXN_TYPE_FUEL_SURCHARGE_ON_US;
    public static String TXN_TYPE_FUEL_SURCHARGE_OFF_US;
    public static String NP_ACCRUED_LATE_PAYMENT_FEE_GL;
    public static String NP_ACCRUED_OVER_LIMIT_FEE_GL;
    public static String NP_ACCRUED_OTHER_FEE_GL;
    public static String NPINTEREST_ON_NP_TO_PERFORM_GL;
    public static String NPOUTSTANDING_ON_NP_TO_PERFORM_GL;
    public static String ACCRUED_OVERLIMIT_FEES_ON_NP_TO_PERFORM_GL;
    public static String ACCRUED_LATE_PAYMENT_FEES_ON_NP_TO_PERFORM_GL;
    public static String KNOCKOFF_NPINTEREST_GL;
    public static String KNOCKOFF_NPOUTSTANDING_GL;
    public static String KNOCKOFF_ACCRUED_INTEREST_GL;
    public static String KNOCKOFF_ACCRUED_LATE_FEE_GL;
    public static String KNOCKOFF_ACCRUED_OVERLIMIT_FEE_GL;
    public static String KNOCKOFF_ACCRUED_OTHER_FEES_GL;
    public static String PROVISION_GL;
    public static String PROVISION_KNOCK_OFF_GL;

    public static int PROVISION_PERCENTAGE_NDIA_120_179;
    public static int PROVISION_PERCENTAGE_NDIA_180_239;
    public static int PROVISION_PERCENTAGE_NDIA_OVER_239;

    //CASH BACK Txn Types
    public static String TXN_TYPE_CASH_BACK;
    public static String TXN_TYPE_CASHBACK_REDEEMED;
    public static String TXN_TYPE_CASHBACK_EXRIRED;
    public static String TXN_TYPE_CASHBACK_NP;
    public static String CASH_BACK_FILE_CRDR;
    public static String CASHBACK_TXN_TYPE;

    //payment_types
    public static int STANDING_INSTRUCTION;
    public static int OVER_THE_COUNTER;

    public static int THIRD_PARTY;

    //Collection And Recovery
    public static int X_DATES_BEFORE_FIRST_DUE_DATE;
    public static int X_DATES_AFTER_FIRST_DUE_DATE;
    public static int X_DATES_AFTER_SECOND_STATEMENT;
    public static int X_DATES_AFTER_SECOND_DUE_DATE;
    public static int X_DATES_AFTER_THIRD_STATEMENT;
    public static int X_DATES_AFTER_THIRD_DUE_DATE;
    public static int X_DATES_AFTER_FOURTH_STATEMENT;
    public static int WITHIN_X_DAYS_OF_THE_CRIB_INFO_LETTER_REMINDER;
    public static int X_DATES_AFTER_FOURTH_DUE_DATE;

    public static String PATH_CUP_BIN_FILE;
    public static String PATH_CUP_BIN_FILE_WINDOWS;
    public static String PATH_CUP_BIN_FILE_LINUX;
    public static int MAX_THREAD_SIZE_CUP_BIN;
    public static String FILE_CODE_CUP_BIN_FILE;
    public static boolean PROCESS_CUP_BIN_FILE;

    //letterTemplateCodes
    public static String RENEWAL_LETTER_TEMPLATE_CODE;
    public static String APPLICATION_REJECT_TEMPLATE_CODE;
    public static String COLLECTION_AND_RECOVERY_FIRST_LETTER;
    public static String COLLECTION_AND_RECOVERY_SECOND_LETTER;

    public static String GL_BRANCH_CODE;
    public static boolean IS_WRITE_CSV;
    public static String PRINTED_CSV_FILE_CREATED_LOCATION;
    public static String EOD_DATE_FORMAT;
    public static String PRINTED_STATEMENT_RELATIVE_PATH;

    //    RB36
    public static String CASH_ACCOUNT_RB36;
    public static String CHEQUE_ACCOUNT_RB36;
    public static String CREDIT_TXN_TYPE_RB36;
    public static String DEBIT_TXN_TYPE_RB36;
    public static String BRANCH_CODE_RB36;
    public static String CLIENT_NO_RB36;
    public static String PERFORM_LOAN_RB36;
    public static String NON_PERFORM_LOAN_RB36;
    public static String PROFIT_CENTER_RB36;

    public static String CHANNEL_ID_CASHBACK;
    public static String CHANNEL_ID_AUTOSETTLEMENT;
    public static String CHANNEL_ID_PAYOUT_DIRECT;
    public static String CHANNEL_ID_PAYOUT_SLIP;
    public static String OUTPUT_FILE_PROD_CODE;
    public static String BULK_FILE_REFERENCE_CASHBACK;
    public static String BULK_FILE_REFERENCE_MERCH_PAYOUT;
    public static String BULK_TYPE_MULTI;
    public static String BULK_TYPE_SINGLE;
    public static String TRAN_TYPE_CREDIT;
    public static String UPLOADED_METHOD_SINGLE;

    //exposure file
    public static String EXPOSURE_FILE_PREFIX;

    //Acquiring
    public static String MERCHANT_FILE_NAME_PREFIX;
    public static String MERCHANT_FILE_NAME_SLIPS_PREFIX;
    public static int PROCESS_ID_COMMISSION_CALCULATION;//182
    public static int PROCESS_ID_MERCHANT_FEE;
    public static int PROCESS_ID_MERCHANT_STATEMENT;//202
    public static int PROCESS_MERCHANT_STATEMENT_FILE_CREATION;//203
    public static int PROCESS_MONTHLY_STATEMENT_FILE_CREATION;//191 - 17
    public static int PROCESS_ID_MERCHANT_CUSTOMER_STATEMENT;//204
    public static int PROCESS_MERCHANT_CUSTOMER_STATEMENT_FILE_CREATION;//205

    public static int PROCESS_ID_MERCHANT_PAYMENT_PROCESS;//209
    public static int PROCESS_ID_MERCHANT_PAYMENT_FILE_CREATION;//210

    public static int PROCESS_ID_EOD_MERCHANT_EASY_PAYMENT_REQUEST;//215
    public static int PROCESS_ID_ORIGINATOR_PUSH_TXN_UPDATE;//216
    public static int PROCESS_ID_RECIPIENT_PUSH_TXN_UPDATE;//217

    //Acq Jasper Templates
    public static String MERCHANT_REPORT_LOCATION;
    public static String MERCHANT_CUSTOMER_REPORT_LOCATION;
    public static String MERCHANT_STATEMENT_SUMMARY_LOCATION;

    public static String MERCHANT_PAY_MODE_SLIPS;
    public static String MERCHANT_PAY_MODE_DIRECT;
    public static String MERCHANT_PAY_MODE_CHEQUE;

    public static String SUSPENCE_ACC_MERCHANT_PAY_MODE_SLIPS;

    //Merchant RB36
    public static String MERCHANT_PAYABLE_GL;
    //    public static String MERCHANT_SUSPENSE_ACCOUNT_RB36_SLIPS;
    public static String MERCHANT_PAYABLE_GL_SLIPS;
    public static String MERCHANT_CASH_ACCOUNT_RB36;
    public static String MERCHANT_CHEQUE_ACCOUNT_RB36;
    public static String MERCHANT_CREDIT_TXN_TYPE_RB36;
    public static String MERCHANT_DEBIT_TXN_TYPE_RB36;
    public static String MERCHANT_BRANCH_CODE_RB36;
    public static String MERCHANT_CLIENT_NO_RB36;
    public static String MERCHANT_PERFORM_LOAN_RB36;
    public static String MERCHANT_NON_PERFORM_LOAN_RB36;
    public static String MERCHANT_PROFIT_CENTER_RB36;
    public static String MERCHANT_NARRATIVE_RB36;

    public static int PROCESS_ID_MERCHANT_GL_FILE_CREATION;//213
    public static int PROCESS_ID_ADMIN_ALERT_PROCESS;
    public static String ADMIN_STATEMENT_ALERT_EMAIL_CODE;
    public static String ADMIN_DUE_DATE_ALERT_EMAIL_CODE;
    public static String ADMIN_AUTO_SETTLEMENT_ALERT_EMAIL_CODE;
    public static String ADMIN_CRIB_FILE_ALERT_EMAIL_CODE;
    public static String ADMIN_CASHBACK_REDEEM_ALERT_EMAIL_CODE;


    //Commission Calculation
    public static String COMMISSION_TRANSACTION_TABLE = "COMMISSIONTRANSACTION";
    public static String COMMISSION_SEGMENT_TRANSACTION = "TRANSACTIONCODE";
    public static String COMMISSION_MCC_TABLE = "COMMISSIONMCC";
    public static String COMMISSION_SEGMENT_MCC = "MCCCODE";
    public static String COMMISSION_VOLUME_TABLE = "COMMISSIONVOLUME";
    public static String COMMISSION_SEGMENT_VOLUME = "VOLUMEID";
    public static String CARD_ASSOCIATION_VISA = "VISA";
    public static String CARD_ASSOCIATION_MASTER = "MASTER";
    public static String TRANSACTION_SEGMENT_TXNTYPE = "TRANSACTIONTYPE";
    public static String TRANSACTION_SEGMENT_MCC = "MCC";
    public static String COMMISSION_DEFAULT_TXN = "TDEF";
    public static String COMMISSION_DEFAULT_MCC = "MDEF";
    public static String COMMISSION_DEFAULT_VOLUME = "VDEF";
    public static String MERCHANT_STATEMENT_TOPAN_FILE_PATH;
    public static String MERCHANT_CUSTOMER_STATEMENT_TOPAN_FILE_PATH;
    public static String ACQ_ADJUSTMENT_TYPE_REVERSAL = "3";
    public static String ACQ_ADJUSTMENT_TYPE_PAYMENT = "1";
    public static String ACQ_ADJUSTMENT_TYPE_COMMISSION = "2";
    public static String ACQ_ADJUSTMENT_TYPE_REFUND = "4";
    public static String ACQ_ADJUSTMENT_TYPE_FEE = "5";

    public static String TOPAN_FILE_CSV_PREFIX;
    public static int STATEMENT_BATCH_SIZE;
    public static int MAX_THREAD_SIZE;
    public static int MAX_THREAD_SIZE_STATEMENT;
    public static int MAX_THREAD_SIZE_T67;

    public static String MVISA_RECIPIENT_BIN;
    public static int EOD_ACQISS_BOTH_STATUS;

    public static String TXN_TYPE_MVISA_RECI_LK_QR_ON_US;
    public static String TXN_TYPE_MVISA_RECI_LK_QR_OFF_US;

    public static String PAYMENT_FILE_CASH_PAYMENT_TXN_TYPES;
    public static String PAYMENT_FILE_CASH_REVERSAL_TXN_TYPES;

    public static String PAYMENT_FILE_FUND_TRANSFER_TXN_TYPES;
    public static String PAYMENT_FILE_OB_BILLS_TXN_TYPES;
    public static String PAYMENT_FILE_STD_ORDER_TXN_TYPES;

    public static String MASTERCARD_ABU_FILE_PREFIX;
    public static String MASTERCARD_ABU_FILE_POSTFIX;
    public static String MASTERCARD_ABU_FILE_EXTENSION;
    public static String MASTERCARD_ABU_ISSUER_ICA_NUMBER;

    //EOD Dashboard
    public static String LAST_INPUT_SIGNAL = "";
    public static String LAST_INPUT_SIGNAL_MESSAGE = "";
    public static boolean IS_CONNECTED_WITH_WEB = false;
    public static boolean EOD_RUN_STATUS = false;
    public static boolean IS_CONTROL_BY_WEB;
    public static boolean IS_EOD_SIMULATION_MODE = false;
    public static String EODRUNNING_SIMULATION_MODE = "0";
    public static boolean IS_EOD_NORMAL_MODE = false;
    public static String EODRUNNING_NORMAL_MODE = "0";
    public static boolean IS_EOD_MANUAL_RUN = false;
    public static String EODRUNNING_MANUAL_RUN = "0";
    public static boolean IS_EOD_CONTINUOUS_RUN = false;
    public static String EODRUNNING_CONTINIOUS_RUN = "0";
    public static boolean IS_EOD_YES_COMMAND = false;
    public static boolean IS_EOD_NO_COMMAND = false;
    public static boolean IS_EOD_EXIT_COMMAND = false;
    public static boolean IS_EOD_DATE_INPUT_ON_DASHBOARD = false;
    public static int WEB_COM_HANDLER_PORT;
    public static int clientHandlerThreadId = 1;
    public static boolean DO_YOU_WANT_RUN_NEWEOD = false;
    public static String EOD_IN_INITIAL_STATE = "0";
    public static String EOD_IN_STARTING_STATE = "1";
    public static String EOD_IN_WAITING_TO_RUN = "2";
    public static String EOD_STOP = "1";
    public static String EOD_IN_END_STATE = "3";
    public static String EOD_IN_RUNNING_STATE = "4";
    public static String YES_BUTTON_CLICKED = "2";
    public static String BUTTON_DISABLE = "0";
    public static String BUTTON_ENABLE = "1";
    public static String SUB_EOD_FINISHED = "3";
    public static String MAIN_EOD_FINISHED = "3";
    public static String SUB_EOD_RUNNING = "1";
    public static String MAIN_EOD_RUNNING = "1";
    public static String SUB_EOD_INITIAL = "0";
    public static String MAIN_EOD_INITIAL = "0";
    //public static EodRunningParameterBean eodRunningPrameterBean;


    public static int DASHBOARD_EOD_ID;
    public static String DASHBOARD_EOD_START_TIME;
    public static String DASHBOARD_EOD_END_TIME;
    public static String DASHBOARD_MAIN_EOD_STATUS;
    public static String DASHBOARD_SUB_EOD_STATUS;
    public static int DASHBOARD_EOD_INPUT_FILES_COUNT = 0;
    public static BlockingQueue<String> consoleMessageQueue = new ArrayBlockingQueue<String>(100000);

    public static String DASHBOARD_NEXT_MAIN_EOD_ID;
    public static String DASHBOARD_NEXT_MAIN_EOD_START_TIME;
    public static String DASHBOARD_NEXT_SUB_EOD_START_TIME;

    public static String DASHBOARD_EOD_PROCESS_SUMMERY;
    public static String DASHBOARD_EOD_INPUT_FILES = "";
    public static String DASHBOARD_EOD_OUTPUT_FILES;
    public static String DASHBOARD_EOD_ERROR_FILES = "";
    public static String DASHBOARD_EOD_ERROR_CARDS;
    public static String DASHBOARD_EOD_ERROR_MERCHANT;
    public static String DASHBOARD_EOD_INVALID_TRANSACTIONS = "";
    public static boolean IS_DASHBOARD_CONNECTED = false;
    public static volatile int DASHBORD_EOD_THREAD_HANDLER_COUNT = 0;
    public static boolean IS_SHOW_INDETAILS_ON_EODDASHBOARD;

    public static String TXN_TYPE_ACQ_ON_US_MASTER;
    public static String TXN_TYPE_ACQ_OFF_US_MASTER;
    public static String TXN_TYPE_ACQ_ON_US_EASYPAY_MASTER;
    public static String TXN_TYPE_CASH_ADVANCE_ACQ_ON_US_MASTER;
    public static String TXN_TYPE_CASH_ADVANCE_ACQ_OFF_US_MASTER;
    public static String TXN_TYPE_FUEL_SURCHARGE_ON_US_MASTER;
    public static String TXN_TYPE_FUEL_SURCHARGE_OFF_US_MASTER;

    public static String MASTERCARD_FORWARDING_INSTITUTION_ID_CODE;
    public static String TRANSACTION_ORIGINATOR_INSTITUTION_ID_CODE;
    public static String PROCESSING_MODE;

    public static String TXN_TYPE_ACQ_ON_US_CUP;
    public static String TXN_TYPE_ACQ_OFF_US_CUP;
    public static String TXN_TYPE_ACQ_ON_US_EASYPAY_CUP;
    public static String TXN_TYPE_CASH_ADVANCE_ACQ_ON_US_CUP;
    public static String TXN_TYPE_CASH_ADVANCE_ACQ_OFF_US_CUP;
    public static String TXN_TYPE_FUEL_SURCHARGE_ON_US_CUP;
    public static String TXN_TYPE_FUEL_SURCHARGE_OFF_US_CUP;

    public static int NO_OF_EASY_PAYMENTS = 0;
    public static int NO_OF_BALANCE_TRANSFERS = 0;
    public static int NO_OF_LOAN_ON_CARDS = 0;

    public static int FAILED_EASY_PAYMENTS = 0;
    public static int FAILED_BALANCE_TRANSFERS = 0;
    public static int FAILED_LOAN_ON_CARDS = 0;

    public static boolean PROCESS_COMPLETE_STATUS = false;
    public static boolean EOD_ENGINE_SOFT_STOP = false;
    public static String STATUS_FILE_COMP = "FCOMP";
    public static String STATUS_FILE_REJECT = "FREJT";

    public static String eodUniqueId;

    //Kafka Log Topic Config
    public static final String LOG_TOPIC = "logTopic";

    public static synchronized void countFinishedCategories() {
        STEP_CATEGORY_COUNT = STEP_CATEGORY_COUNT + 1;
    }

    public static synchronized void countFinishedProcess() {
        STEP_PROCESS_COUNT = STEP_PROCESS_COUNT + 1;
    }

    public static synchronized void addVISACurrentlyAvailableThreads() {
        VISA_CURRENTLY_AVAILABLE_THREADS++;
    }

    public static synchronized void deductVISACurrentlyAvailableThreads() {
        VISA_CURRENTLY_AVAILABLE_THREADS--;
    }

    public static synchronized void addVISACurrentlyBusyThreads() {
        VISA_CURRENTLY_BUSY_THREADS++;
    }

    public static synchronized void deductVISACurrentlyBusyThreads() {
        VISA_CURRENTLY_BUSY_THREADS--;
    }

    public static synchronized void addVISAQueueCurrentSize() {
        VISA_QUEUE_CURRENT_SIZE++;
    }

    public static synchronized void deductVISAQueueCurrentSize() {
        VISA_QUEUE_CURRENT_SIZE--;
    }

    public static synchronized String getOS_Type() throws Exception {
        String osType = "";
        String osName = "";
        osName = System.getProperty("os.name", "").toLowerCase();

        /*For WINDOWS*/
        if (osName.contains("windows")) {
            osType = "WINDOWS";
        } else /*For LINUX*/ if (osName.contains("linux")) {
            osType = "LINUX";
        } else {
            throw new Exception("Cannot identify the Operating System.");
        }
        return osType;
    }

    public static String getLine() {
        return "\r\n___________________________________________________________________________\r\n";
    }

    public static String getEndLine() {
        return "\r\n===========================================================================\r\n";
    }

    public static volatile List<ErrorCardBean> errorCardList = new ArrayList<>();
    public static volatile List<ErrorMerchantBean> merchantErrorList = new ArrayList<>();
    public static volatile boolean isInterrupted = false;

    public static String RUNNING_PROCESS_DESCRIPTION;
    public static int IssuingOrAcquiring; //0 -ISSUING, 1 - ACQUIRING, 2- Both
    public static HashMap<Integer, Object> processConnectorList = new HashMap<>();

    //dcf file read
    public static int RECORD_COUNT = 0;
    public static int BATCH_NUMBER = 0;
    public static int FAILED_FILE_NAME_COUNT = 0;

    public static volatile int PROCESS_ATM_VALIDATE_INVALID_COUNT = 0;
    //public static volatile int PROCESS_ATM_VALIDATE_SUCCESS_COUNT = 0;
    public static volatile int PROCESS_ATM_VALIDATE_FAIL_COUNT = 0;
    public static AtomicInteger PROCESS_ATM_VALIDATE_SUCCESS_COUNT = new AtomicInteger(0);

    public static String WALLET_SCHEMA_NAME = "DFCCBACKENDMVISAORTEST2";
    public static String ONLINE_DB_VIEW_NAME = "ABC_DB";

    //log file prefix
    public static final String LOG_TYPE_INFO = "INFO";
    public static final String LOG_TYPE_ERROR = "ERROR";
    public static final String LOG_FILE_PREFIX_COMMON = "common";
    public static final String LOG_FILE_PREFIX_EOD_ENGINE = "eod_engine";
    public static final String LOG_FILE_PREFIX_EOD_FILE_PROCESSING_ENGINE = "file_pro_engine";
    public static final String LOG_FILE_PREFIX_EOD_FILE_GENERATION_ENGINE = "file_gen_engine";
    public static final String INFO_LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n";
    public static final String ERROR_LOG_PATTERN = "%d [%thread] %-5level %-5logger{40} - %msg%n";

    public static final String EOD_ENGINE = "EENG";
    public static final String EOD_FILE_GENERATION = "EFGE";
    public static final String EOD_FILE_PROCESSING = "EFPE";

    //Statement Gen Base Url
    public static final String EOD_STATEMENT_GEN_BASE_URL = "http://192.168.1.122:5000/eod-engine";
}
