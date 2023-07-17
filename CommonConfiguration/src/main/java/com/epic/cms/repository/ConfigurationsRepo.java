package com.epic.cms.repository;

import com.epic.cms.dao.ConfigurationsDao;
import com.epic.cms.model.bean.CommonFilePathBean;
import com.epic.cms.model.bean.TransactionTypeBean;
import com.epic.cms.model.rowmapper.CommonFilePathRowMapper;
import com.epic.cms.model.rowmapper.TransactionTypeRowMapper;
import com.epic.cms.util.ConfigVarList;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.CreateEodId;
import com.epic.cms.util.QueryParametersList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Repository
public class ConfigurationsRepo implements ConfigurationsDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    ConfigVarList configVarList;

    @Autowired
    CreateEodId createEodId;

    public SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    @Override
    @Transactional("backendDb")
    public void loadTxnTypeConfigurations() throws Exception {
        HashMap<String, String> txnTypesMap = new HashMap<String, String>();
        List<TransactionTypeBean> transactionTypeBeanList = null;
        try {
            transactionTypeBeanList = backendJdbcTemplate.query(queryParametersList.getEodMainSelectLoadTxnTypeConfigurations(), new TransactionTypeRowMapper());

            for (TransactionTypeBean transactionTypeBean : transactionTypeBeanList) {
                txnTypesMap.put(transactionTypeBean.getOnlineTxnType(), transactionTypeBean.getBackendTxnType());
            }

            Configurations.TXN_TYPE_SIGN_ON = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_SIGN_ON);
            Configurations.TXN_TYPE_KEY_EXCHANGE = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_KEY_EXCHANGE);
            Configurations.TXN_TYPE_ECHO = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_ECHO);
            Configurations.TXN_TYPE_SIGN_OFF = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_SIGN_OFF);
            Configurations.TXN_TYPE_AUTHORIZATION_ADVICE = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_AUTHORIZATION_ADVICE);
            Configurations.TXN_TYPE_REDEEM = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_REDEEM);
            Configurations.TXN_TYPE_VOID_SALE = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_VOID_SALE);
            Configurations.TXN_TYPE_SALE = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_SALE);
            Configurations.TXN_TYPE_CASH_ADVANCE = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_CASH_ADVANCE);
            Configurations.TXN_TYPE_REVERSAL = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_REVERSAL);
            Configurations.TXN_TYPE_INSTALLMENT = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_INSTALLMENT);
            Configurations.TXN_TYPE_WITHDRAWAL = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_WITHDRAWAL);
            Configurations.TXN_TYPE_MINI_STATEMENT = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_MINI_STATEMENT);
            Configurations.TXN_TYPE_BALANCE_INQUIRY = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_BALANCE_INQUIRY);
            Configurations.TXN_TYPE_FUND_TRANSFER = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_FUND_TRANSFER);
            Configurations.TXN_TYPE_PIN_CHANGE = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_PIN_CHANGE);
            Configurations.TXN_TYPE_PIN_VERIFY = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_PIN_VERIFY);
            Configurations.TXN_TYPE_PAYMENT = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_PAYMENT);
            Configurations.TXN_TYPE_DEBIT_PAYMENT = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_DEBIT_PAYMENT);
            Configurations.TXN_TYPE_REVERSAL_INSTALLMENT = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_REVERSAL_INSTALLMENT);
            Configurations.TXN_TYPE_ADJUSTMENT_CREDIT = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_ADJUSTMENT_CREDIT);
            Configurations.TXN_TYPE_ADJUSTMENT_DEBIT = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_ADJUSTMENT_DEBIT);
            Configurations.TXN_TYPE_REVERSAL_ADVICE_CREDIT = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_ADJUSTMENT_DEBIT);
            Configurations.TXN_TYPE_BALANCE_TRANSFER = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_BALANCE_TRANSFER);
            Configurations.TXN_TYPE_LOAN_ON_CARD = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_LOAN_ON_CARD);
            Configurations.TXN_TYPE_QUASI_CASH = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_QUASI_CASH);
            Configurations.TXN_TYPE_FEE_INSTALLMENT = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_FEE_INSTALLMENT);
            Configurations.TXN_TYPE_START_FETCH_STIP_ON_ADVICES = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_START_FETCH_STIP_ON_ADVICES);
            Configurations.TXN_TYPE_STOP_FETCH_STIP_ON_ADVICES = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_STOP_FETCH_STIP_ON_ADVICES);
            Configurations.TXN_TYPE_BATCH_UPLOAD = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_BATCH_UPLOAD);
            Configurations.TXN_TYPE_SETTLMENT = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_SETTLMENT);
            Configurations.TXN_TYPE_FORCE_SETTLMENT = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_FORCE_SETTLMENT);
            Configurations.TXN_TYPE_AUTHORIZATION_REPEAT = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_AUTHORIZATION_REPEAT);
            Configurations.TXN_TYPE_REVERSAL_REPEAT = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_REVERSAL_REPEAT);
            Configurations.TXN_TYPE_REVERSAL_ADVICE_QUASI_CASH = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_REVERSAL_ADVICE_QUASI_CASH);
            Configurations.TXN_TYPE_REFUND = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_REFUND);
            Configurations.TXN_TYPE_MVISA_REFUND = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_MVISA_REFUND);
            Configurations.TXN_TYPE_MVISA_MERCHANT_PAYMENT = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_MVISA_MERCHANT_PAYMENT);
            Configurations.TXN_TYPE_MVISA_ORIGINATOR = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_MVISA_ORIGINATOR);
            Configurations.TXN_TYPE_MONEY_SEND = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_MONEY_SEND);
            Configurations.TXN_TYPE_MONEY_SEND_REVERSAL = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_MONEY_SEND_REVERSAL);
            Configurations.TXN_TYPE_MONEY_SEND_ADVICE = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_MONEY_SEND_ADVICE);
            Configurations.TXN_TYPE_PRE_COMPLETION = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_PRE_COMPLETION);
            Configurations.TXN_TYPE_CUP_QR_PAYMENT = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_CUP_QR_PAYMENT);
            Configurations.TXN_TYPE_CUP_QR_REFUND = txnTypesMap.get(Configurations.TXN_TYPE_ONLINE_CUP_QR_REFUND);
            Configurations.TXN_TYPE_AFT = txnTypesMap.get(Configurations.TXN_TYPE_AFT);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional("backendDb")
    public void loadFilePath() throws Exception {
        List<CommonFilePathBean> commonFilePathBeanList = null;
        try {
            commonFilePathBeanList = backendJdbcTemplate.query(queryParametersList.getEodMainSelectLoadFilePath(), new CommonFilePathRowMapper(), Configurations.SERVER_RUN_PLATFORM);
            for (CommonFilePathBean commonFilePathBean : commonFilePathBeanList) {
                Configurations.STATEMENT_FILE_PATH = commonFilePathBean.getStatement();
                Configurations.AUTO_SETTLEMENT_FILE_PATH = commonFilePathBean.getAutoSettlement();
                Configurations.EOD_LETTER_FILE_PATH = commonFilePathBean.getLetters();
                Configurations.GLFILE_FILE_PATH = commonFilePathBean.getGlFile();
                Configurations.MERCHANT_GLFILE_FILE_PATH = commonFilePathBean.getMerchantGlFile();
                Configurations.EOD_LOGS_FILE_PATH = commonFilePathBean.getEodFile();
                Configurations.EXPOSURE_FILE_PATH = commonFilePathBean.getExposureFile();
                Configurations.RB36_FILE_PATH = commonFilePathBean.getRb36();
                Configurations.MERCHANT_STATEMENT_PATH = commonFilePathBean.getMerchantStatementFile();
                Configurations.MERCHANT_CUSTOMER_STATEMENT_PATH = commonFilePathBean.getMerchantCustomerStatementFile();
                Configurations.MERCHANT_STATEMENT_SUMMARY_PATH = commonFilePathBean.getMerchantStatementSummeryFile();
                Configurations.OUTGOING_CTF_FILE_PATH = commonFilePathBean.getOutgoingFile();
                Configurations.CASHBACK_FILE_PATH = commonFilePathBean.getCashBack();
                Configurations.MERCHANT_PAYMENT_FILE_PATH = commonFilePathBean.getMerchantPaymentFile();
                Configurations.BULK_APPLICATION_FILE_PATH = commonFilePathBean.getBulkApplication();
                Configurations.TOPAN_FILE_PATH_CSV = commonFilePathBean.getPrintedStatementReport();
                Configurations.OUTGOING_IPM_FILE_PATH = commonFilePathBean.getOutgoingIpmFile();
                Configurations.MASTERCARD_ABU_FILE_PATH = commonFilePathBean.getMasterCardAbuFile();
                Configurations.EOD_DASHBOARD_CONSOLELOG_FILE_PATH = commonFilePathBean.getEodDashboardConsoleLog();
                Configurations.OUTGOING_CUP_FILE_PATH = commonFilePathBean.getOutgoingCupStatementFile();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void setConfigurations() throws Exception {
        try {
            Configurations.SERVER_RUN_PLATFORM = Configurations.getOS_Type();
            Configurations.STARTING_EOD_STATUS = "INIT";
            /**Configurations.EOD_ID = createEodId.getCurrentEodId("INIT", "EROR"); // Current EOD Id
            Configurations.ERROR_EOD_ID = Configurations.EOD_ID;
            Configurations.EOD_DATE = getDateFromEODID(Configurations.EOD_ID);
            Configurations.EOD_DATE_String = sdf.format(Configurations.EOD_DATE);*/
            System.out.println("TEST Status"+Configurations.STARTING_EOD_STATUS + "eod id "+Configurations.EOD_ID);
            Configurations.TXN_TYPE_ONLINE_SIGN_ON = configVarList.getTXN_TYPE_ONLINE_SIGN_ON();
            Configurations.TXN_TYPE_ONLINE_KEY_EXCHANGE = configVarList.getTXN_TYPE_ONLINE_KEY_EXCHANGE();
            Configurations.TXN_TYPE_ONLINE_ECHO = configVarList.getTXN_TYPE_ONLINE_ECHO();
            Configurations.TXN_TYPE_ONLINE_SIGN_OFF = configVarList.getTXN_TYPE_ONLINE_SIGN_OFF();
            Configurations.TXN_TYPE_ONLINE_AUTHORIZATION_ADVICE = configVarList.getTXN_TYPE_ONLINE_AUTHORIZATION_ADVICE();
            Configurations.TXN_TYPE_ONLINE_REDEEM = configVarList.getTXN_TYPE_ONLINE_REDEEM();
            Configurations.TXN_TYPE_ONLINE_VOID_SALE = configVarList.getTXN_TYPE_ONLINE_VOID_SALE();
            Configurations.TXN_TYPE_ONLINE_SALE = configVarList.getTXN_TYPE_ONLINE_SALE();
            Configurations.TXN_TYPE_ONLINE_CASH_ADVANCE = configVarList.getTXN_TYPE_ONLINE_CASH_ADVANCE();
            Configurations.TXN_TYPE_ONLINE_REVERSAL = configVarList.getTXN_TYPE_ONLINE_REVERSAL();
            Configurations.TXN_TYPE_ONLINE_INSTALLMENT = configVarList.getTXN_TYPE_ONLINE_INSTALLMENT();
            Configurations.TXN_TYPE_ONLINE_WITHDRAWAL = configVarList.getTXN_TYPE_ONLINE_WITHDRAWAL();
            Configurations.TXN_TYPE_ONLINE_MINI_STATEMENT = configVarList.getTXN_TYPE_ONLINE_MINI_STATEMENT();
            Configurations.TXN_TYPE_ONLINE_BALANCE_INQUIRY = configVarList.getTXN_TYPE_ONLINE_BALANCE_INQUIRY();
            Configurations.TXN_TYPE_ONLINE_FUND_TRANSFER = configVarList.getTXN_TYPE_ONLINE_FUND_TRANSFER();
            Configurations.TXN_TYPE_ONLINE_PIN_CHANGE = configVarList.getTXN_TYPE_ONLINE_PIN_CHANGE();
            Configurations.TXN_TYPE_ONLINE_PIN_VERIFY = configVarList.getTXN_TYPE_ONLINE_PIN_VERIFY();
            Configurations.TXN_TYPE_ONLINE_PAYMENT = configVarList.getTXN_TYPE_ONLINE_PAYMENT();
            Configurations.TXN_TYPE_ONLINE_DEBIT_PAYMENT = configVarList.getTXN_TYPE_ONLINE_DEBIT_PAYMENT();
            Configurations.TXN_TYPE_ONLINE_REVERSAL_INSTALLMENT = configVarList.getTXN_TYPE_ONLINE_REVERSAL_INSTALLMENT();
            Configurations.TXN_TYPE_ONLINE_ADJUSTMENT_CREDIT = configVarList.getTXN_TYPE_ONLINE_ADJUSTMENT_CREDIT();
            Configurations.TXN_TYPE_ONLINE_ADJUSTMENT_DEBIT = configVarList.getTXN_TYPE_ONLINE_ADJUSTMENT_DEBIT();
            Configurations.TXN_TYPE_ONLINE_REVERSAL_ADVICE_CREDIT = configVarList.getTXN_TYPE_ONLINE_REVERSAL_ADVICE_CREDIT();
            Configurations.TXN_TYPE_ONLINE_BALANCE_TRANSFER = configVarList.getTXN_TYPE_ONLINE_BALANCE_TRANSFER();
            Configurations.TXN_TYPE_ONLINE_LOAN_ON_CARD = configVarList.getTXN_TYPE_ONLINE_LOAN_ON_CARD();
            Configurations.TXN_TYPE_ONLINE_QUASI_CASH = configVarList.getTXN_TYPE_ONLINE_QUASI_CASH();
            Configurations.TXN_TYPE_ONLINE_FEE_INSTALLMENT = configVarList.getTXN_TYPE_ONLINE_FEE_INSTALLMENT();
            Configurations.TXN_TYPE_ONLINE_START_FETCH_STIP_ON_ADVICES = configVarList.getTXN_TYPE_ONLINE_START_FETCH_STIP_ON_ADVICES();
            Configurations.TXN_TYPE_ONLINE_STOP_FETCH_STIP_ON_ADVICES = configVarList.getTXN_TYPE_ONLINE_STOP_FETCH_STIP_ON_ADVICES();
            Configurations.TXN_TYPE_ONLINE_BATCH_UPLOAD = configVarList.getTXN_TYPE_ONLINE_BATCH_UPLOAD();
            Configurations.TXN_TYPE_ONLINE_SETTLMENT = configVarList.getTXN_TYPE_ONLINE_SETTLMENT();
            Configurations.TXN_TYPE_ONLINE_FORCE_SETTLMENT = configVarList.getTXN_TYPE_ONLINE_FORCE_SETTLMENT();
            Configurations.TXN_TYPE_ONLINE_AUTHORIZATION_REPEAT = configVarList.getTXN_TYPE_ONLINE_AUTHORIZATION_REPEAT();
            Configurations.TXN_TYPE_ONLINE_REVERSAL_REPEAT = configVarList.getTXN_TYPE_ONLINE_REVERSAL_REPEAT();
            Configurations.TXN_TYPE_ONLINE_REVERSAL_ADVICE_QUASI_CASH = configVarList.getTXN_TYPE_ONLINE_REVERSAL_ADVICE_QUASI_CASH();
            Configurations.TXN_TYPE_ONLINE_REFUND = configVarList.getTXN_TYPE_ONLINE_REFUND();
            Configurations.TXN_TYPE_ONLINE_MVISA_REFUND = configVarList.getTXN_TYPE_ONLINE_MVISA_REFUND();
            Configurations.TXN_TYPE_ONLINE_MVISA_MERCHANT_PAYMENT = configVarList.getTXN_TYPE_ONLINE_MVISA_MERCHANT_PAYMENT();
            Configurations.TXN_TYPE_ONLINE_MVISA_ORIGINATOR = configVarList.getTXN_TYPE_ONLINE_MVISA_ORIGINATOR();
            Configurations.TXN_TYPE_ONLINE_PRE_COMPLETION = configVarList.getTXN_TYPE_ONLINE_PRE_COMPLETION();
            Configurations.TXN_TYPE_ONLINE_OFFLINE_SALE = configVarList.getTXN_TYPE_ONLINE_OFFLINE_SALE();
            Configurations.TXN_TYPE_ONLINE_MVISA_ORIGINATOR_UNAVAIL_ADVICE = configVarList.getTXN_TYPE_ONLINE_MVISA_ORIGINATOR_UNAVAIL_ADVICE();
            Configurations.TXN_TYPE_ONLINE_MONEY_SEND = configVarList.getTXN_TYPE_ONLINE_MONEY_SEND();
            Configurations.TXN_TYPE_ONLINE_MONEY_SEND_REVERSAL = configVarList.getTXN_TYPE_ONLINE_MONEY_SEND_REVERSAL();
            Configurations.TXN_TYPE_ONLINE_MONEY_SEND_ADVICE = configVarList.getTXN_TYPE_ONLINE_MONEY_SEND_ADVICE();
            Configurations.TXN_TYPE_ONLINE_CUP_QR_PAYMENT = configVarList.getTXN_TYPE_ONLINE_CUP_QR_PAYMENT();
            Configurations.TXN_TYPE_ONLINE_CUP_QR_REFUND = configVarList.getTXN_TYPE_ONLINE_CUP_QR_REFUND();

            Configurations.PROCESS_ID_INITIAL_PROCESS = configVarList.getInitialProcess();
            Configurations.PROCESS_ID_EOD_PARAMETER_RESET = configVarList.getEodParameterResetProcess();
            Configurations.EOD_USER = configVarList.getEodUser();
            Configurations.PROCESS_ID_CARD_REPLACE = configVarList.getCardReplaceProcess();
            Configurations.START_INDEX = configVarList.getStartIndex();
            Configurations.END_INDEX = configVarList.getEndIndex();
            Configurations.PATTERN_CHAR = configVarList.getPatternChar();
            Configurations.EOD_DONE_STATUS = configVarList.getEodDoneStatus();
            Configurations.YES_STATUS = configVarList.getYesStatus();
            Configurations.CARD_REPLACE_ACCEPT = configVarList.getCardReplaceAccept();
            Configurations.PROCESS_CARD_FEE = configVarList.getCardFeeProcess();
            Configurations.PROCESS_ID_LOAN_ON_CARD = configVarList.getLoanOnCardProcess();
            Configurations.PROCESS_CHEQUERETURN = configVarList.getChequeReturnProcess();
            Configurations.PROCESS_EOD_RUNNABLE_FEE = configVarList.getEodRunnableFeeProcess();
            Configurations.PROCESS_ID_KNOCK_OFF = configVarList.getProcess_knock_off();
            Configurations.PROCESS_STAMP_DUTY_FEE = configVarList.getProcess_stamp_duty_fee();

            //adjustment types
            Configurations.LOYALTY_ADJUSTMENT_TYPE = configVarList.getLOYALTY_ADJUSTMENT_TYPE();
            Configurations.PAYMENT_ADJUSTMENT_TYPE = configVarList.getPAYMENT_ADJUSTMENT_TYPE();
            Configurations.TRANSACTION_ADJUSTMENT_TYPE = configVarList.getTRANSACTION_ADJUSTMENT_TYPE();
            Configurations.FEE_ADJUSTMENT_TYPE = configVarList.getFEE_ADJUSTMENT_TYPE();
            Configurations.INTEREST_ADJUSTMENT_TYPE = configVarList.getINTEREST_ADJUSTMENT_TYPE();
            Configurations.CASH_ADVANCE_ADJUSTMENT_TYPE = configVarList.getCASH_ADVANCE_ADJUSTMENT_TYPE();
            Configurations.INSTALLMENT_ADJUSTMENT_TYPE = configVarList.getINSTALLMENT_ADJUSTMENT_TYPE();
            Configurations.GOVERNMENT_STAMP_DUTY_ADJUSTMENT_TYPE = configVarList.getGOVERNMENT_STAMP_DUTY_ADJUSTMENT_TYPE();
            Configurations.CASHBACK_ADJUSTMENT_TYPE = configVarList.getCASHBACK_ADJUSTMENT_TYPE();

            //fee codes
            Configurations.OVER_LIMIT_FEE = configVarList.getOVER_LIMIT_FEE();
            Configurations.LATE_PAYMENT_FEE = configVarList.getLATE_PAYMENT_FEE();
            Configurations.PROCESS_ID_ACQUIRING_TXN_UPDATE_PROCESS = configVarList.getProcess_id_acquiring_txn_update_process();
            Configurations.CREDIT = configVarList.getCredit();
            Configurations.DEBIT = configVarList.getDebit();
            Configurations.CHANNEL_TYPE_VISA = configVarList.getChannel_type_visa();
            Configurations.VISA_ASSOCIATION = configVarList.getVISA_ASSOCIATION();
            Configurations.CHANNEL_TYPE_MASTER = configVarList.getChannel_type_master();
            Configurations.MASTER_ASSOCIATION = configVarList.getMASTER_ASSOCIATION();
            Configurations.CHANNEL_TYPE_CUP = configVarList.getChannel_type_cup();
            Configurations.CUP_ASSOCIATION = configVarList.getCUP_ASSOCIATION();
            Configurations.PAYMENT_REVERSAL_PROCESS = configVarList.getProcess_payment_reversal();
            Configurations.PROCESS_EODPAYMENTUPDATE = configVarList.getProcess_eodpayment_update();
            Configurations.PROCESS_ID_TXNMISMATCH_POST = configVarList.getProcess_txnmismatch_post();
            Configurations.EOD_ACQUIRING_STATUS = configVarList.getEod_acquiring_status();
            Configurations.EOD_PENDING_STATUS = configVarList.getEod_pending_status();
            Configurations.EOD_CONSIDER_STATUS = configVarList.getEod_consider_status();

            //CreditLimit
            Configurations.PROCESS_ID_CARD_PERMENANT_BLOCK = configVarList.getCard_permenant_block();
            Configurations.NO_OF_MONTHS_FOR_PERMENANT_BLOCK = configVarList.getNo_of_months_for_permanent_block();
            Configurations.PERM_BLOCK_REASON = configVarList.getPerm_block_reason();
            Configurations.ACTIVE_STATUS = configVarList.getActive_status();
            Configurations.ONLINE_ACTIVE_STATUS = configVarList.getOnline_active_status();
            Configurations.PROCESS_ID_CARD_TEMPORARY_BLOCK = configVarList.getCard_temporary_block();
            Configurations.NO_OF_MONTHS_FOR_TEMPORARY_BLOCK = configVarList.getNo_of_months_for_temporary_block();
            Configurations.TEMP_BLOCK_REASON = configVarList.getTemp_block_reason();
            Configurations.PROCESS_ID_INCREMENT_LIMIT_EXPIRE = configVarList.getProcess_id_increment_limit_expire();
            Configurations.CREDIT_INCREMENT = configVarList.getCredit_increment();
            Configurations.CARD_CATEGORY_MAIN = configVarList.getCard_category_main();
            Configurations.CARD_CATEGORY_ESTABLISHMENT = configVarList.getCard_category_establishment();
            Configurations.CARD_CATEGORY_FD = configVarList.getCard_category_fd();
            Configurations.CARD_CATEGORY_AFFINITY = configVarList.getCard_category_affinity();
            Configurations.CARD_CATEGORY_CO_BRANDED = configVarList.getCard_category_co_branded();
            Configurations.CASH_INCREMENT = configVarList.getCash_increment();

            Configurations.PROCESS_LIMIT_ENHANCEMENT = configVarList.getProcess_limit_enhancement();
            Configurations.LIMIT_INCREMENT = configVarList.getLimit_increment();
            Configurations.LIMIT_DECREMENT = configVarList.getLimit_decrement();
            Configurations.PROCESS_CLEAR_MINPAYMENTS_AND_TEMPBLOCK = configVarList.getProcess_clear_minpayment_and_tempblock();
            Configurations.ONLINE_LOG_LEVEL = configVarList.getONLINE_LOG_LEVEL();
            Configurations.COLLECTION_AND_RECOVERY_NOTIFICATION = configVarList.getProcess_collection_and_recovery();

            Configurations.TP_X_DATES_BEFORE_FIRST_DUE_DATE = configVarList.getTp_x_dates_before_first_due_date();
            Configurations.TP_X_DATES_AFTER_FIRST_DUE_DATE = configVarList.getTp_x_dates_after_first_due_date();
            Configurations.TP_X_DAYS_AFTER_THE_2ND_STATEMENT_DATE = configVarList.getTp_x_days_after_the_2nd_statement_date();
            Configurations.TP_ON_THE_2ND_STATEMENT_DATE = configVarList.getTp_On_the_2nd_statement_date();
            Configurations.TP_IMMEDIATELY_AFTER_THE_2ND_DUE_DATE = configVarList.getTp_immediately_after_the_2nd_due_date();
            Configurations.TP_ON_THE_3RD_STATEMENT_DATE = configVarList.getTp_on_the_3rd_statement_date();
            Configurations.TP_IMMEDIATELY_AFTER_THE_3RD_DUE_DATE = configVarList.getTp_immediately_after_the_3rd_due_date();
            Configurations.TP_ON_THE_4TH_STATEMENT_DATE = configVarList.getTp_on_the_4th_statement_date();
            Configurations.TP_X_DAYS_AFTER_THE_4TH_STATEMENT_DATE = configVarList.getTp_x_days_after_the_4th_statement_date();
            Configurations.TP_WITHIN_X_DAYS_OF_THE_CRIB_INFO_LETTER_REMINDER = configVarList.getTp_within_x_days_of_the_crib_info_letter_reminder();
            Configurations.TP_IMMEDIATELY_AFTER_THE_4TH_DUE_DATE = configVarList.getTp_immediately_after_the_4th_due_date();

            Configurations.NO_STATUS = configVarList.getNo_status();

            //paymentFile Validate
            Configurations.PROCESS_PAYMENT_FILE_VALIDATE = configVarList.getProcess_payment_file_validate();
            Configurations.PAYMENT_FILE_CHEQUE_INITIATE_TXN_TYPES = configVarList.getPayment_file_cheque_initiate_txn_types();
            Configurations.PAYMENT_FILE_CHEQUE_RETURN_TXN_TYPES = configVarList.getPayment_file_cheque_return_txn_types();
            Configurations.COMPLETE_STATUS = configVarList.getComplete_status();
            Configurations.INITIAL_STATUS = configVarList.getInit_status();

            Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_REJECT = configVarList.getProcess_letter_gen_application_reject();
            Configurations.APPLICATION_REJECTION_LETTER_CODE = configVarList.getApplication_rejection_letter();

            //ATMtFile Validate
            Configurations.PROCESS_ATM_FILE_VALIDATE = configVarList.getProcess_atm_file_validate();

            //File Code
            Configurations.FILE_CODE_ATM = configVarList.getFile_code_atm();
            Configurations.FAIL_STATUS = configVarList.getFail_status();
            Configurations.FILE_CODE_MASTERCARD_T67 = configVarList.getFILE_CODE_MASTERCARD_T67();
            Configurations.FILE_CODE_DCF = configVarList.getFILE_CODE_DCF();

            //Letter
            Configurations.CARD_CATEGORY_SUPPLEMENTORY = configVarList.getCard_category_supplementory();
            Configurations.CARD_CATEGORY_AFFINITY_SUPPLEMENTORY = configVarList.getCard_category_affinity_supplementory();
            Configurations.CARD_CATEGORY_CO_BRANDED_SUPPLEMENTORY = configVarList.getCard_category_co_branded_supplementory();
            Configurations.CARD_CATEGORY_FD_SUPPLEMENTORY = configVarList.getCard_category_fd_supplementory();
            Configurations.CARD_CATEGORY_CORPORATE = configVarList.getCard_category_corporate();

            Configurations.STAMP_DUTY_FEE = configVarList.getStamp_duty_fee();

            //Country Code
            Configurations.COUNTRY_CODE_SRILANKA = configVarList.getCountry_code_srilanka();

            //Cash Back
            Configurations.PROCESS_CASHBACK = configVarList.getProcess_cashback();

            Configurations.PROCESS_CARD_EXPIRE = configVarList.getProcess_card_expire();
            Configurations.PROCESS_CRIB_FILE = configVarList.getProcess_crib_file();
            Configurations.PROCESS_ID_OVER_LIMIT_FEE = configVarList.getProcess_id_overlimit_fee();
            Configurations.PROCESS_ID_CHEQUEPAYMENT = configVarList.getProcess_chequepayment();
            Configurations.PROCESS_ID_SNAPSHOT = configVarList.getSnapshot_process();
            Configurations.PROCESS_TRANSACTION_DROP_REQUEST = configVarList.getProcess_transaction_drop_request();
            Configurations.PROCESS_ID_LOYALTY_POINT_CALCULATION_PROCESS = configVarList.getLoyalty_point_calculation_process();
            Configurations.PROCESS_ID_CASH_BACK_ALERT_PROCESS = configVarList.getProcess_cashback_alert();
            Configurations.PROCESS_ID_COLLECTION_AND_RECOVERY_ALERT_PROCESS = configVarList.getProcess_collection_and_recovery_alert();
            Configurations.PROCESS_ID_CARDAPPLICATION_LETTER_APPROVE = configVarList.getProcess_letter_gen_application_approve();

            Configurations.ONLINE_REVERSE_STATUS = configVarList.getOnline_reverse_status();
            Configurations.ONLINE_DROP_STATUS = configVarList.getOnline_drop_status();
            Configurations.ONLINE_PARTIALLY_REVERSE_STATUS = configVarList.getOnline_partially_reverse_status();
            Configurations.ONLINE_TXN_INCOMPLETE_STATUS = configVarList.getOnline_txn_incomplete_status();
            Configurations.CASH_BACK_SMS_CODE = configVarList.getCash_back_sms();
            Configurations.STMT_WITH_CASH_BACK_SMS_CODE = configVarList.getStmt_with_cash_back_sms();
            Configurations.STMT_WITHOUT_CASH_BACK_SMS_CODE = configVarList.getStmt_without_cash_back_sms();
            Configurations.X_DAYS_BEFORE_1_DUE_DATE = configVarList.getX_days_before_1_due_date();
            Configurations.X_DAYS_BEFORE_1_DUE_DATE_EMAIL_CODE = configVarList.getX_days_before_1_due_date_email();
            Configurations.X_DAYS_BEFORE_1_DUE_DATE_SMS_CODE = configVarList.getX_days_before_1_due_date_sms();
            Configurations.IMMEDIATE_AFTER_1_DUE_DATE_EMAIL_CODE = configVarList.getImmediately_after_1_due_date_email();
            Configurations.IMMEDIATE_AFTER_1_DUE_DATE_SMS_CODE = configVarList.getImmediately_after_1_due_date_sms();
            Configurations.IMMEDIATE_AFTER_1_DUE_DATE = configVarList.getImmediately_after_1_due_date();
            Configurations.IMMEDIATE_AFTER_2_DUE_DATE = configVarList.getImmediately_after_2_due_date();
            Configurations.IMMEDIATE_AFTER_2_DUE_DATE_EMAIL_CODE = configVarList.getImmediately_after_2_due_date_email();
            Configurations.IMMEDIATE_AFTER_2_DUE_DATE_SMS_CODE = configVarList.getImmediately_after_2_due_date_sms();
            Configurations.IMMEDIATE_AFTER_3_DUE_DATE = configVarList.getImmediately_after_3_due_date();
            Configurations.IMMEDIATE_AFTER_3_DUE_DATE_EMAIL_CODE = configVarList.getImmediately_after_3_due_date_email();
            Configurations.IMMEDIATE_AFTER_3_DUE_DATE_SMS_CODE = configVarList.getImmediately_after_3_due_date_sms();
            Configurations.EMAIL_TEMPLATE = configVarList.getEmail_template();
            Configurations.SMS_TEMPLATE = configVarList.getSms_template();
            Configurations.EMAIL_TEMPLATE_CODE = configVarList.getEmail_template_code();
            Configurations.SMS_TEMPLATE_CODE = configVarList.getSms_template_code();
            Configurations.ALERTUNUNREAD = configVarList.getAlert_unread();

            Configurations.EOD_ISSUING_STATUS = configVarList.getEod_issuing_status();
            Configurations.ANNUAL_FEE = configVarList.getAnnual_fee();


            Configurations.CASH_ADVANCE_FEE = configVarList.getCASH_ADVANCE_FEE();
            Configurations.CHEQUE_RETURN_ON_PAYMENTS_OTHER_REASONS_FEE = configVarList.getCHEQUE_RETURN_ON_PAYMENTS_OTHER_REASONS_FEE();
            Configurations.CHEQUE_RETURN_ON_PAYMENTS_INSUFFICIENT_FUNDS_FEE = configVarList.getCHEQUE_RETURN_ON_PAYMENTS_INSUFFICIENT_FUNDS_FEE();
            Configurations.CHEQUE_RETURN_ON_PAYMENTS_STOP_FEE = configVarList.getCHEQUE_RETURN_ON_PAYMENTS_STOP_FEE();

            //gl accounts
            Configurations.TXN_TYPE_UNEARNED_INCOME = configVarList.getTXN_TYPE_UNEARNED_INCOME();
            Configurations.TXN_TYPE_UNEARNED_INCOME_UPFRONT_FALSE = configVarList.getTXN_TYPE_UNEARNED_INCOME_UPFRONT_FALSE();
            Configurations.TXN_TYPE_FEE_INSTALLMENT_UPFRONT_FALSE = configVarList.getTXN_TYPE_FEE_INSTALLMENT_UPFRONT_FALSE();

            Configurations.ANNUAL_FEE_FOR_NP_ACCOUNTS = configVarList.getANNUAL_FEE_FOR_NP_ACCOUNTS();

            Configurations.NO_OF_MONTHS_FOR_PERMENANT_BLOCK = configVarList.getNO_OF_MONTHS_FOR_PERMENANT_BLOCK();
            //Manual Np Process
            Configurations.PROCESS_ID_MANUAL_NP_PROCESS = configVarList.getProcess_manual_np();
            Configurations.INTEREST_ON_THE_NP_GL = configVarList.getNPINTEREST_ON_NP_GL();
            Configurations.OUTSTANDING_ON_THE_NP_GL = configVarList.getNPOUSTANDING_ON_NP_GL();
            Configurations.KNOCKOFF_NP_INTEREST_DECLASSIFIED_GL = configVarList.getKNOCKOFF_NP_INTEREST_DECLASSIFIED_GL();
            Configurations.KNOCKOFF_NP_OUTSTANDING_DECLASSIFIED_GL = configVarList.getKNOCKOFF_NP_OUTSTANDING_DECLASSIFIED_GL();
            Configurations.KNOCKOFF_ACCRUED_LATE_FEE_DECLASSIFIED_GL = configVarList.getKNOCKOFF_ACCRUED_LATE_FEE_DECLASSIFIED_GL();
            Configurations.KNOCKOFF_ACCRUED_INTEREST_DECLASSIFIED_GL = configVarList.getKNOCKOFF_ACCRUED_INTEREST_DECLASSIFIED_GL();
            Configurations.KNOCKOFF_ACCRUED_OVERLIMIT_FEE_DECLASSIFIED_GL = configVarList.getKNOCKOFF_ACCRUED_OVERLIMIT_FEE_DECLASSIFIED_GL();
            Configurations.KNOCKOFF_ACCRUED_OTHER_FEES_DECLASSIFIED_GL = configVarList.getKNOCKOFF_ACCRUED_OTHER_FEES_DECLASSIFIED_GL();

            //Online to Backend Txn Syn
            Configurations.ONLINE_LISTNER_TYPE_ATM = configVarList.getOnline_listner_type_atm();
            Configurations.ONLINE_LISTNER_TYPE_NAC = configVarList.getOnline_listner_type_nac();
            Configurations.EOD_NOT_CONSIDER_STATUS = configVarList.getEod_not_consider_status();
            Configurations.ADJUSTMENT_PROCESS = configVarList.getADJUSTMENT_PROCESS();
            Configurations.PROCESS_ID_FEE_POST = configVarList.getPROCESS_ID_FEE_POST();

            //EOM Interest Calculation Process
            Configurations.TXN_TYPE_INTEREST_INCOME = configVarList.getTXN_TYPE_INTEREST_INCOME();
            Configurations.NP_ACCRUED_INTEREST_GL = configVarList.getNP_ACCRUED_INTEREST_GL();
            Configurations.PROCESS_ID_EOM_INTEREST_CALCULATION = configVarList.getProcess_id_eom_interest_calculation();

            Configurations.PROCESS_ID_BALANCE_TRANSFER = configVarList.getProcess_id_balance_transfer();
            Configurations.PROCESS_ID_EASY_PAYMENT = configVarList.getProcess_id_easy_payment();
            Configurations.PROCESS_INTEREST_CALCULATION = configVarList.getProcess_interest_calculation();

            //Configurations.BATCH_SIZE = configVarList.getBatchSize();
            Configurations.EOD_LIVE_MODE = configVarList.getEod_live_mode() == 1 ? true : false;

            Configurations.MAIN_EOD_STARTTIME_H = configVarList.getMain_eod_starttime_h();
            Configurations.MAIN_EOD_STARTTIME_M = configVarList.getMain_eod_starttime_m();
            Configurations.SUB_EOD_STARTTIME_H = configVarList.getSub_eod_starttime_h();
            Configurations.SUB_EOD_STARTTIME_M = configVarList.getSub_eod_starttime_m();

            Configurations.CARD_DOMAIN_CREDIT = configVarList.getCard_domain_credit();
            Configurations.CARD_DOMAIN_DEDIT = configVarList.getCard_domain_dedit();
            Configurations.CARD_INITIAL_STATUS = configVarList.getCard_initial_status();
            Configurations.CREDIT_STATUS = configVarList.getCredit_status();
            Configurations.ONLINE_DEACTIVE_STATUS = configVarList.getOnline_active_status();
            Configurations.ONLINE_OFUS_BIN = configVarList.getOnline_ofus_bin();

            Configurations.MERCHANT_PAYMENT_FILE_PATH_SLIPS = configVarList.getMerchant_file_path_slips();
            Configurations.MERCHANT_PAYMENT_FILE_PATH_CHEQUE = configVarList.getMerchant_file_path_cheque();
            Configurations.MERCHANT_PAYMENT_FILE_PATH_DIRECT = configVarList.getMerchant_file_path_direct();
            Configurations.MERCHANT_STAT_AUTOMATE_FILE_PATH = configVarList.getMerchant_stat_auto_file_path();
            Configurations.MERCHANT_STAT_AUTOMATE_FILE_NAME = configVarList.getMerchant_stat_auto_file_name();

            Configurations.INTERNAL_KEY_CHEQUE = configVarList.getInternal_key_for_cheque();
            Configurations.INTERNAL_KEY_CASH = configVarList.getInternal_key_for_cash();

            Configurations.MAIN_EOD_PROCESS = configVarList.getMain_eod_process();

            Configurations.PROCESS_ID_PINGENERATION = configVarList.getProcess_id_pingeneration();
            Configurations.PROCESS_ID_EOM_INTEREST_CALCULATION = configVarList.getProcess_id_eom_interest_calculation();
            Configurations.PROCESS_ID_OVER_LIMIT_INTEREST_CALCULATION = configVarList.getProcess_id_overlimit_interest_calculation();
            Configurations.PROCESS_ID_CREDIT_SCORE = configVarList.getProcess_id_credit_score();
            Configurations.PROCESS_MERCHANT_TRANSACTION = configVarList.getProcess_merchant_transaction();
            Configurations.PROCESS_MERCHANT_SETTLEMENT = configVarList.getProcess_merchant_settlement();
            Configurations.PARAMETER_INITIALIZATION = configVarList.getParameter_initialization();
            Configurations.PROCESS_EMBOSS_FILEGENERATION = configVarList.getProcess_emboss_filegeneration();
            Configurations.PROCESS_MERCHANT_PAYMENT_CYCLE = configVarList.getProcess_merchant_payment_cycle();
            Configurations.PROCESS_CREATE_CRIB_REQUEST = configVarList.getProcess_create_crib_request();
            Configurations.PROCESS_EXPOSURE_FILE = configVarList.getProcess_exposure_file();
            Configurations.PROCESS_EOM_SUP_CARD_RESET = configVarList.getProcess_sup_card_reset();
            Configurations.PROCESS_ID_CASHBACK_FILE_GENERATION = configVarList.getProcess_id_cashback_file_generation();
            Configurations.PROCESS_CARD_RENEW = configVarList.getProcess_card_renew();
            Configurations.PROCESS_MONTHLY_STATEMENT = configVarList.getProcess_monthly_statement();
            Configurations.PROCESS_MONTHLY_STATEMENT_FILE_CREATION = configVarList.getProcess_monthly_statement_file_creation();
            Configurations.PROCESS_ID_VISA_BASEII_CLEARING = configVarList.getProcess_id_visa_baseii_clearing();
            Configurations.PROCESS_ID_COPY_VISA_TRANSACTION_TO_BACKEND = configVarList.getProcess_id_copy_visa_transaction_to_backend();
            Configurations.MERCHANT_PAYMENT_STARTDATE = configVarList.getMerchant_payment_startdate();
            Configurations.PROCESS_ONLINETOBACKEND_TXNSYNC = configVarList.getProcess_onlinetobackend_txnsync();
            Configurations.AUTO_SETTLEMENT_PROCESS = configVarList.getAuto_settlement_process();
            Configurations.PROCESS_VISA_SETTELEMENTDATEUPDATE = configVarList.getProcess_visa_settlementdate_update();
            Configurations.PROCESS_EODTRANSACTIONUPDATE = configVarList.getProcess_eodtransaction_update();
            Configurations.PROCESS_EODCASHADVANCEUPDATE = configVarList.getProcess_eodcashadvance_update();
            Configurations.PROCESS_VISA_BIN_FILE_READ_PROCESS = configVarList.getProcess_id_visa_bin_file_read();
            Configurations.PROCESS_PRE_MERCHANT_FEE_PROCESS = configVarList.getProcess_id_pre_merchant_fee_process();
            Configurations.PROCESS_ACQUIRING_ADJUSTMENT_PROCESS = configVarList.getProcess_acquiring_adjustment_process();
            Configurations.PROCESS_WALLET_SETTLEMENT_FILE_READ = configVarList.getProcess_wallet_settlement_file_read();
            Configurations.PROCESS_WALLET_SETTLEMENT_FILE_UPDATE = configVarList.getProcess_wallet_settlement_file_update();
            Configurations.PROCESS_MASTER_CARD_T67_FILE_READ = configVarList.getProcess_master_card_t67_file_read();
            Configurations.MAXIMUM_TCRS_IN_CTF_BATCH = configVarList.getMaximum_tcrs_in_ctf_batch();
            Configurations.PROCESS_ID_OUTGOING_CTF_FILE_GEN = configVarList.getProcess_id_outgoing_ctf_file_gen();
            Configurations.MANUAL_CASH_MCC = configVarList.getManual_cash_mcc();
            Configurations.VISA_ACQ_BIN = configVarList.getVisa_acq_bin();
            Configurations.PROCESS_ID_OUTGOING_CUP_FILE_GEN = configVarList.getProcess_id_outgoing_cup_file_gen();
            Configurations.PROCESS_ID_OUTGOING_IPM_FILE_GEN = configVarList.getProcess_id_outgoing_ipm_file_gen();
            Configurations.PROCESS_DCF_FILE_READ = configVarList.getProcess_dcf_file_read();
            Configurations.PROCESS_DCF_FILE_VALIDATE = configVarList.getProcess_dcf_file_validate();
            Configurations.PROCESS_CHECK_PAYMENTS_FOR_MIN_AMOUNT = configVarList.getProcess_check_payment_for_min_amount();
            Configurations.PROCESS_ID_RISK_CALCULATION_PROCESS = configVarList.getProcess_id_risk_calculation_process();
            Configurations.PROCESS_ID_MASTER_CLEARING = configVarList.getProcess_id_master_clearing();
            Configurations.PROCESS_ID_MASTER_CARD_ABU_FILE_GEN = configVarList.getProcess_id_master_card_abu_file_gen();
            Configurations.PROCESS_ID_CARDRENEW_LETTER = configVarList.getProcess_card_renew_letter();
            Configurations.PROCESS_ID_CARDREPLACE_LETTER = configVarList.getCard_replace_letter_process();
            Configurations.STATEMENT_NOTIFY_MSG = configVarList.getSTATEMENT_NOTIFY_MSG();
            Configurations.PROCESS_ID_GL_FILE_CREATION = configVarList.getProcess_gl_file_process();
            Configurations.PROCESS_RB36_FILE_CREATION = configVarList.getPROCESS_RB36_FILE_CREATION();
            Configurations.PROCESS_ID_COLLECTION_AND_RECOVERY_LETTER_PROCESS = configVarList.getProcess_collection_and_recovery_letter();
            Configurations.PROCESS_ID_BULK_APPLICATION_READ_PROCESS = configVarList.getProcess_bulk_application_read();
            Configurations.PROCESS_ID_BULK_APPLICATION_VALIDATE_PROCESS = configVarList.getProcess_bulk_application_validate();
            Configurations.GL_SUMMARY_FILE_PREFIX = configVarList.getGL_SUMMARY_FILE_PREFIX();
            Configurations.MERCHANT_GL_SUMMARY_FILE_PREFIX = configVarList.getMERCHANT_GL_SUMMARY_FILE_PREFIX();
            Configurations.OUTPUTFILE_FIELD_DELIMETER = configVarList.getOUTPUTFILE_FIELD_DELIMETER();
            Configurations.CASHBACK_FILE_PREFIX_F1 = configVarList.getCASHBACK_FILE_PREFIX_F1();
            Configurations.MERCHANT_PAYMENT_FILE_DIRECT_PREFIX_F1 = configVarList.getMERCHANT_PAYMENT_FILE_DIRECT_PREFIX_F1();
            Configurations.MERCHANT_PAYMENT_FILE_SLIPS_PREFIX = configVarList.getMERCHANT_PAYMENT_FILE_SLIPS_PREFIX();
            Configurations.RB36_FILE_PREFIX = configVarList.getRB36_FILE_PREFIX();
            Configurations.AUTOSETTLEMENT_FILE_PREFIX = configVarList.getAUTOSETTLEMENT_FILE_PREFIX();
            Configurations.CASHBACK_FILE_PREFIX_F2 = configVarList.getCASHBACK_FILE_PREFIX_F2();
            Configurations.MERCHANT_PAYMENT_FILE_DIRECT_PREFIX_F2 = configVarList.getMERCHANT_PAYMENT_FILE_DIRECT_PREFIX_F2();
            Configurations.PROCESS_ID_TXN_POST = configVarList.getProcess_txn_post();
            Configurations.REPORT_ID = configVarList.getReport_id();
            Configurations.SUBJECT_TYPE = configVarList.getSubject_type();
            Configurations.RESPONSE_TYPE = configVarList.getResponse_type();
            Configurations.INQUIRY_REOSON_CODE = configVarList.getInquiry_reoson_code();
            Configurations.BULK_STATUS = configVarList.getBulk_status();
            Configurations.STATUS = configVarList.getStatus();
            Configurations.CARD_REPLACEMENT_LETTER_CODE = configVarList.getCard_replacement_letter();
            Configurations.SECOND_REMINDER_LETTER_CODE = configVarList.getSecond_reminder_letter();
            Configurations.CARD_RENEWAL_LETTER_CODE = configVarList.getCard_renewal_letter();
            Configurations.APPLICATION_CONFIRMATION_LETTER_CODE = configVarList.getApplication_confirmation_letter();
            Configurations.FIRST_REMINDER_LETTER_CODE = configVarList.getFirst_reminder_letter();
            Configurations.PRODUCT_CHANGE_LETTER_CODE = configVarList.getProduct_change_letter_code();
            Configurations.COLLECTION_ACCOUNT = configVarList.getCollection_account();
            Configurations.EXPOSURE_FILE_BRANCH = configVarList.getExposure_file_branch();
            Configurations.EXPOSURE_FILE_PRODUCT = configVarList.getExposure_file_product();
            Configurations.EXPOSURE_FILE_FACILITY_TYPE = configVarList.getExposure_file_facility_type();

            Configurations.EOD_TXN_AUTH_ONLY_STATUS = configVarList.getEod_txn_auth_only_status();
            Configurations.EOD_TXN_AUTH_ONLY_INIT = configVarList.getEod_txn_auth_only_init();
            Configurations.EOD_TXN_AUTH_ONLY_POSTED = configVarList.getEod_txn_auth_only_posted();
            Configurations.ONLINE_TXN_EOD_COPIED_STS = configVarList.getOnline_txn_eod_copied_sts();
            Configurations.ONLINE_TXN_EOD_NOT_COPIED_STS = configVarList.getOnline_txn_eod_not_copied_sts();

            Configurations.MERCHANT_ANNUAL_FEE = configVarList.getMerchant_annual_fee();
            Configurations.MERCHANT_BI_MONTHLY_FEE = configVarList.getMerchant_bi_monthly_fee();
            Configurations.MERCHANT_QUARTERLY_FEE = configVarList.getMerchant_quarterly_fee();
            Configurations.MERCHANT_HALF_YEARLY_FEE = configVarList.getMerchant_half_yearly_fee();
            Configurations.TERMINAL_MONTHLY_RENTAL_FEE = configVarList.getTerminal_monthly_rental_fee();
            Configurations.TERMINAL_BI_MONTHLY_RENTAL_FEE = configVarList.getTerminal_bi_monthly_rental_fee();
            Configurations.TERMINAL_HALF_YEARLY_RENTAL_FEE = configVarList.getTerminal_half_yearly_rental_fee();
            Configurations.TERMINAL_WEEKLY_RENTAL_FEE = configVarList.getTerminal_weekly_rental_fee();
            Configurations.TERMINAL_QUARTERLY_RENTAL_FEE = configVarList.getTerminal_quarterly_rental_fee();
            Configurations.TERMINAL_MAINTAINACE_FEE = configVarList.getTerminal_maintainace_fee();

            Configurations.TXN_TYPE_CASH_BACK = configVarList.getTXN_TYPE_CASHBACK();
            Configurations.TXN_TYPE_CASHBACK_REDEEMED = configVarList.getTXN_TYPE_CASHBACK_REDEEMED();
            Configurations.TXN_TYPE_CASHBACK_EXRIRED = configVarList.getTXN_TYPE_CASHBACK_EXRIRED();
            Configurations.TXN_TYPE_CASHBACK_NP = configVarList.getTXN_TYPE_CASHBACK_NP();
            Configurations.TXN_TYPE_INTEREST_INCOME = configVarList.getTXN_TYPE_INTEREST_INCOME();
            Configurations.CASH_BACK_FILE_CRDR = configVarList.getCASH_BACK_FILE_CRDR();
            Configurations.CASHBACK_TXN_TYPE = configVarList.getCASHBACK_TXN_TYPE();

            Configurations.STANDING_INSTRUCTION = configVarList.getStanding_Instruction();
            Configurations.OVER_THE_COUNTER = configVarList.getOver_the_Counter();
            Configurations.THIRD_PARTY = configVarList.getThird_Party();

            Configurations.INCOMMING_IPM_FILE_ENCODING_FORMAT = configVarList.getINCOMMING_IPM_FILE_ENCODING_FORMAT();
            Configurations.DCF_ICA_FOR_AIIC = configVarList.getICA_AIIC();
            Configurations.DCF_ICA_FOR_AIIC_VISA = configVarList.getICA_AIIC_VISA();
            Configurations.OUTGOING_IPM_FILE_ENCODING_FORMAT = configVarList.getOUTGOING_IPM_FILE_ENCODING_FORMAT();
            Configurations.OUTGOING_IPM_FILE_LAYOUT = configVarList.getOUTGOING_IPM_FILE_LAYOUT();
            Configurations.MASTER_ACQ_BIN = configVarList.getMaster_acq_bin();
            Configurations.READ_T67_IP0040T1_TABLE = configVarList.getRead_t67_ip0040t1_table();
            Configurations.READ_T67_IP0075T1_TABLE = configVarList.getRead_t67_ip0075t1_table();
            Configurations.OUTGOING_CUP_FILE_NAME_PREFIX = configVarList.getFile_name_prefix();
            Configurations.OUTGOING_CUP_FILE_NAME_SUFIX = configVarList.getFile_name_sufix();
            Configurations.INSTITUTION_IDENTIFICATION_NUMBER = configVarList.getInstitution_identification_number();
            Configurations.CUP_FILE_HEADER_TXN_CODE = configVarList.getHeader_txn_code();
            Configurations.CUP_FILE_TAILER_TXN_CODE = configVarList.getTailer_txn_code();
            Configurations.OUTGOING_CUP_FILE_HEADER_VERSION_TAG = configVarList.getVersion_tag();
            Configurations.OUTGOING_CUP_FILE_HEADER_VERSION_NUMBER = configVarList.getVersion_number();
            Configurations.PROCESS_CUP_BIN_FILE_READ = configVarList.getProcess_cup_bin_file_read();

            Configurations.PROCESS_ID_MERCHANT_FEE = configVarList.getMerchant_fee_process();
            Configurations.PROCESS_ID_MERCHANT_PAYMENT_PROCESS = configVarList.getProcess_id_merchant_payment_process();
            Configurations.PROCESS_ID_EOD_MERCHANT_EASY_PAYMENT_REQUEST = configVarList.getProcess_id_eod_merchant_easy_payment_request();
            //acquiring
            Configurations.PROCESS_ID_COMMISSION_CALCULATION = configVarList.getCommission_calculation_process();
            Configurations.PROCESS_ID_MERCHANT_STATEMENT = configVarList.getPROCESS_ID_MERCHANT_STATEMENT();
            Configurations.PROCESS_MERCHANT_STATEMENT_FILE_CREATION = configVarList.getPROCESS_MERCHANT_STATEMENT_FILE_CREATION();
            Configurations.PROCESS_MERCHANT_CUSTOMER_STATEMENT_FILE_CREATION = configVarList.getPROCESS_MERCHANT_CUSTOMER_STATEMENT_FILE_CREATION();
            Configurations.STATEMENT_BATCH_SIZE = configVarList.getSTATEMENT_BATCH_SIZE();
            Configurations.PROCESS_ID_MERCHANT_GL_FILE_CREATION = configVarList.getProcess_merchant_gl_file_process();
            Configurations.OUTPUT_FILE_PROD_CODE = configVarList.getOUTPUT_FILE_PROD_CODE();

            //Acquring
            Configurations.PROCESS_ID_ORIGINATOR_PUSH_TXN_UPDATE = configVarList.getProcess_id_originator_push_txn_update();

            //RecipientPushTxnProcessID
            Configurations.PROCESS_ID_RECIPIENT_PUSH_TXN_UPDATE = configVarList.getProcess_id_recipient_push_txn_update();
            Configurations.PROCESS_ID_MERCHANT_GL_FILE_CREATION = configVarList.getProcess_merchant_gl_file_process();
            Configurations.OUTPUT_FILE_PROD_CODE = configVarList.getOUTPUT_FILE_PROD_CODE();

            //Acquring
            Configurations.PROCESS_ID_ORIGINATOR_PUSH_TXN_UPDATE = configVarList.getProcess_id_originator_push_txn_update();
            Configurations.PROCESSING_MODE = configVarList.getProcessing_mode();

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional("backendDb")
    public void loadBaseCurrency() throws Exception {
        String query = "SELECT BASECURRENCY FROM COMMONPARAMETER";

        try {
            Configurations.BASE_CURRENCY = backendJdbcTemplate.queryForObject(query, String.class);
        } catch (Exception e) {
            throw e;
        }
    }

    public Date getDateFromEODID(int eodId) {
        Date parsedDate = null;
        String streodID = "";
        try {
            if (eodId > 10000000) {
                streodID = eodId + "";
                SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
                String eodIDsubs = streodID.substring(0, streodID.length() - 2);
                parsedDate = sdf.parse(eodIDsubs);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            return parsedDate;
        }
    }

}
