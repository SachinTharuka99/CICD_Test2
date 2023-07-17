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
public class ConfigVarList {

    @Value("${TXN_TYPE_ONLINE_SIGN_ON}")
    public String TXN_TYPE_ONLINE_SIGN_ON;

    @Value("${TXN_TYPE_ONLINE_KEY_EXCHANGE}")
    public String TXN_TYPE_ONLINE_KEY_EXCHANGE;

    @Value("${TXN_TYPE_ONLINE_ECHO}")
    public String TXN_TYPE_ONLINE_ECHO;

    @Value("${TXN_TYPE_ONLINE_SIGN_OFF}")
    public String TXN_TYPE_ONLINE_SIGN_OFF;

    @Value("${TXN_TYPE_ONLINE_AUTHORIZATION_ADVICE}")
    public String TXN_TYPE_ONLINE_AUTHORIZATION_ADVICE;

    @Value("${TXN_TYPE_ONLINE_REDEEM}")
    public String TXN_TYPE_ONLINE_REDEEM;

    @Value("${TXN_TYPE_ONLINE_VOID_SALE}")
    public String TXN_TYPE_ONLINE_VOID_SALE;

    @Value("${TXN_TYPE_ONLINE_SALE}")
    public String TXN_TYPE_ONLINE_SALE;

    @Value("${TXN_TYPE_ONLINE_CASH_ADVANCE}")
    public String TXN_TYPE_ONLINE_CASH_ADVANCE;

    @Value("${TXN_TYPE_ONLINE_REVERSAL}")
    public String TXN_TYPE_ONLINE_REVERSAL;

    @Value("${TXN_TYPE_ONLINE_INSTALLMENT}")
    public String TXN_TYPE_ONLINE_INSTALLMENT;

    @Value("${TXN_TYPE_ONLINE_WITHDRAWAL}")
    public String TXN_TYPE_ONLINE_WITHDRAWAL;

    @Value("${TXN_TYPE_ONLINE_MINI_STATEMENT}")
    public String TXN_TYPE_ONLINE_MINI_STATEMENT;

    @Value("${TXN_TYPE_ONLINE_BALANCE_INQUIRY}")
    public String TXN_TYPE_ONLINE_BALANCE_INQUIRY;

    @Value("${TXN_TYPE_ONLINE_FUND_TRANSFER}")
    public String TXN_TYPE_ONLINE_FUND_TRANSFER;

    @Value("${TXN_TYPE_ONLINE_PIN_CHANGE}")
    public String TXN_TYPE_ONLINE_PIN_CHANGE;

    @Value("${TXN_TYPE_ONLINE_PIN_VERIFY}")
    public String TXN_TYPE_ONLINE_PIN_VERIFY;

    @Value("${TXN_TYPE_ONLINE_PAYMENT}")
    public String TXN_TYPE_ONLINE_PAYMENT;

    @Value("${TXN_TYPE_ONLINE_DEBIT_PAYMENT}")
    public String TXN_TYPE_ONLINE_DEBIT_PAYMENT;

    @Value("${TXN_TYPE_ONLINE_REVERSAL_INSTALLMENT}")
    public String TXN_TYPE_ONLINE_REVERSAL_INSTALLMENT;

    @Value("${TXN_TYPE_ONLINE_ADJUSTMENT_CREDIT}")
    public String TXN_TYPE_ONLINE_ADJUSTMENT_CREDIT;

    @Value("${TXN_TYPE_ONLINE_ADJUSTMENT_DEBIT}")
    public String TXN_TYPE_ONLINE_ADJUSTMENT_DEBIT;

    @Value("${TXN_TYPE_ONLINE_REVERSAL_ADVICE_CREDIT}")
    public String TXN_TYPE_ONLINE_REVERSAL_ADVICE_CREDIT;

    @Value("${TXN_TYPE_ONLINE_BALANCE_TRANSFER}")
    public String TXN_TYPE_ONLINE_BALANCE_TRANSFER;

    @Value("${TXN_TYPE_ONLINE_LOAN_ON_CARD}")
    public String TXN_TYPE_ONLINE_LOAN_ON_CARD;

    @Value("${TXN_TYPE_ONLINE_QUASI_CASH}")
    public String TXN_TYPE_ONLINE_QUASI_CASH;

    @Value("${TXN_TYPE_ONLINE_FEE_INSTALLMENT}")
    public String TXN_TYPE_ONLINE_FEE_INSTALLMENT;

    @Value("${TXN_TYPE_ONLINE_START_FETCH_STIP_ON_ADVICES}")
    public String TXN_TYPE_ONLINE_START_FETCH_STIP_ON_ADVICES;

    @Value("${TXN_TYPE_ONLINE_STOP_FETCH_STIP_ON_ADVICES}")
    public String TXN_TYPE_ONLINE_STOP_FETCH_STIP_ON_ADVICES;

    @Value("${TXN_TYPE_ONLINE_BATCH_UPLOAD}")
    public String TXN_TYPE_ONLINE_BATCH_UPLOAD;

    @Value("${TXN_TYPE_ONLINE_SETTLMENT}")
    public String TXN_TYPE_ONLINE_SETTLMENT;

    @Value("${TXN_TYPE_ONLINE_FORCE_SETTLMENT}")
    public String TXN_TYPE_ONLINE_FORCE_SETTLMENT;

    @Value("${TXN_TYPE_ONLINE_AUTHORIZATION_REPEAT}")
    public String TXN_TYPE_ONLINE_AUTHORIZATION_REPEAT;

    @Value("${TXN_TYPE_ONLINE_REVERSAL_REPEAT}")
    public String TXN_TYPE_ONLINE_REVERSAL_REPEAT;

    @Value("${TXN_TYPE_ONLINE_REVERSAL_ADVICE_QUASI_CASH}")
    public String TXN_TYPE_ONLINE_REVERSAL_ADVICE_QUASI_CASH;

    @Value("${TXN_TYPE_ONLINE_REFUND}")
    public String TXN_TYPE_ONLINE_REFUND;

    @Value("${TXN_TYPE_ONLINE_MVISA_REFUND}")
    public String TXN_TYPE_ONLINE_MVISA_REFUND;

    @Value("${TXN_TYPE_ONLINE_MVISA_MERCHANT_PAYMENT}")
    public String TXN_TYPE_ONLINE_MVISA_MERCHANT_PAYMENT;

    @Value("${TXN_TYPE_ONLINE_MVISA_ORIGINATOR}")
    public String TXN_TYPE_ONLINE_MVISA_ORIGINATOR;

    @Value("${TXN_TYPE_ONLINE_PRE_COMPLETION}")
    public String TXN_TYPE_ONLINE_PRE_COMPLETION;

    @Value("${TXN_TYPE_ONLINE_OFFLINE_SALE}")
    public String TXN_TYPE_ONLINE_OFFLINE_SALE;

    @Value("${TXN_TYPE_ONLINE_MVISA_ORIGINATOR_UNAVAIL_ADVICE}")
    public String TXN_TYPE_ONLINE_MVISA_ORIGINATOR_UNAVAIL_ADVICE;

    @Value("${TXN_TYPE_ONLINE_MONEY_SEND}")
    public String TXN_TYPE_ONLINE_MONEY_SEND;

    @Value("${TXN_TYPE_ONLINE_MONEY_SEND_REVERSAL}")
    public String TXN_TYPE_ONLINE_MONEY_SEND_REVERSAL;

    @Value("${TXN_TYPE_ONLINE_MONEY_SEND_ADVICE}")
    public String TXN_TYPE_ONLINE_MONEY_SEND_ADVICE;

    @Value("${TXN_TYPE_ONLINE_CUP_QR_PAYMENT}")
    public String TXN_TYPE_ONLINE_CUP_QR_PAYMENT;

    @Value("${TXN_TYPE_ONLINE_CUP_QR_REFUND}")
    public String TXN_TYPE_ONLINE_CUP_QR_REFUND;

    @Value("${eod_user}")
    public String eodUser;

    @Value("${initial_process}")
    public int initialProcess;

    @Value("${eod_parameter_reset_process}")
    public int eodParameterResetProcess;

    @Value("${process_id_card_replace}")
    public int cardReplaceProcess;

    @Value("${process_card_fee}")
    public int cardFeeProcess;

    @Value("${process_id_loan_on_card}")
    public int loanOnCardProcess;

    @Value("${process_cheque_return}")
    public int chequeReturnProcess;

    @Value("${process_eod_runnable_fee}")
    public int eodRunnableFeeProcess;

    @Value("${starting_index}")
    public int startIndex;

    @Value("${end_index}")
    public int endIndex;

    @Value("${mask_pattern_character}")
    public String patternChar;

    @Value("${eod_done_status}")
    public String eodDoneStatus;

    @Value("${yes_status}")
    public String yesStatus;

    @Value("${card_replace_accept}")
    public String cardReplaceAccept;

    @Value("${LOYALTY_ADJUSTMENT_TYPE}")
    public int LOYALTY_ADJUSTMENT_TYPE;

    @Value("${PAYMENT_ADJUSTMENT_TYPE}")
    public int PAYMENT_ADJUSTMENT_TYPE;

    @Value("${TRANSACTION_ADJUSTMENT_TYPE}")
    public int TRANSACTION_ADJUSTMENT_TYPE;

    @Value("${FEE_ADJUSTMENT_TYPE}")
    public int FEE_ADJUSTMENT_TYPE;

    @Value("${INTEREST_ADJUSTMENT_TYPE}")
    public int INTEREST_ADJUSTMENT_TYPE;

    @Value("${CASH_ADVANCE_ADJUSTMENT_TYPE}")
    public int CASH_ADVANCE_ADJUSTMENT_TYPE;

    @Value("${INSTALLMENT_ADJUSTMENT_TYPE}")
    public int INSTALLMENT_ADJUSTMENT_TYPE;

    @Value("${GOVERNMENT_STAMP_DUTY_ADJUSTMENT_TYPE}")
    public int GOVERNMENT_STAMP_DUTY_ADJUSTMENT_TYPE;

    @Value("${CASHBACK_ADJUSTMENT_TYPE}")
    public int CASHBACK_ADJUSTMENT_TYPE;

    @Value("${over_limit_fee}")
    public String OVER_LIMIT_FEE;

    @Value("${late_payment_fee}")
    public String LATE_PAYMENT_FEE;

    @Value("${process_id_acquiring_txn_update_process}")
    public int process_id_acquiring_txn_update_process;

    @Value("${Credit}")
    public String credit;

    @Value("${Debit}")
    public String debit;

    @Value("${channel_type_visa}")
    public int channel_type_visa;

    @Value("${VISA_ASSOCIATION}")
    public String VISA_ASSOCIATION;

    @Value("${channel_type_master}")
    public int channel_type_master;

    @Value("${MASTER_ASSOCIATION}")
    public String MASTER_ASSOCIATION;

    @Value("${channel_type_cup}")
    public int channel_type_cup;

    @Value("${CUP_ASSOCIATION}")
    public String CUP_ASSOCIATION;

    @Value("${process_payment_reversal}")
    public int process_payment_reversal;

    @Value("${process_eodpayment_update}")
    public int process_eodpayment_update;

    @Value("${process_txnmismatch_post}")
    public int process_txnmismatch_post;

    @Value("${eod_acquiring_status}")
    public int eod_acquiring_status;

    @Value("${eod_pending_status}")
    public String eod_pending_status;

    @Value("${eod_consider_status}")
    public int eod_consider_status;


    @Value("${card_permenant_block}")
    public int card_permenant_block;

    @Value("${no_of_months_for_permanent_block}")
    public int no_of_months_for_permanent_block;

    @Value("${perm_block_reason}")
    public String perm_block_reason;

    @Value("${active_status}")
    public String active_status;

    @Value("${online_active_status}")
    public int online_active_status;

    @Value("${card_temporary_block}")
    public int card_temporary_block;

    @Value("${no_of_months_for_temporary_block}")
    public int no_of_months_for_temporary_block;

    @Value("${temp_block_reason}")
    public String temp_block_reason;

    @Value("${process_id_increment_limit_expire}")
    public int process_id_increment_limit_expire;

    @Value("${credit_increment}")
    public String credit_increment;

    @Value("${card_category_main}")
    public String card_category_main;

    @Value("${card_category_establishment}")
    public String card_category_establishment;

    @Value("${card_category_fd}")
    public String card_category_fd;

    @Value("${card_category_affinity}")
    public String card_category_affinity;

    @Value("${card_category_co_branded}")
    public String card_category_co_branded;

    @Value("${cash_increment}")
    public String cash_increment;

    @Value("${process_limit_enhancement}")
    public int process_limit_enhancement;

    @Value("${limit_increment}")
    public String limit_increment;

    @Value("${limit_decrement}")
    public String limit_decrement;

    @Value("${process_clear_minpayment_and_tempblock}")
    public int process_clear_minpayment_and_tempblock;

    @Value("${ONLINE_LOG_LEVEL}")
    public int ONLINE_LOG_LEVEL;

    @Value("${process_collection_and_recovery}")
    public int process_collection_and_recovery;

    @Value("${tp_x_dates_before_first_due_date}")
    public String tp_x_dates_before_first_due_date;

    @Value("${tp_x_dates_after_first_due_date}")
    public String tp_x_dates_after_first_due_date;

    @Value("${tp_x_days_after_the_2nd_statement_date}")
    public String tp_x_days_after_the_2nd_statement_date;

    @Value("${tp_On_the_2nd_statement_date}")
    public String tp_On_the_2nd_statement_date;

    @Value("${tp_immediately_after_the_2nd_due_date}")
    public String tp_immediately_after_the_2nd_due_date;

    @Value("${tp_on_the_3rd_statement_date}")
    public String tp_on_the_3rd_statement_date;

    @Value("${tp_immediately_after_the_3rd_due_date}")
    public String tp_immediately_after_the_3rd_due_date;

    @Value("${tp_on_the_4th_statement_date}")
    public String tp_on_the_4th_statement_date;

    @Value("${tp_x_days_after_the_4th_statement_date}")
    public String tp_x_days_after_the_4th_statement_date;

    @Value("${tp_within_x_days_of_the_crib_info_letter_reminder}")
    public String tp_within_x_days_of_the_crib_info_letter_reminder;

    @Value("${tp_immediately_after_the_4th_due_date}")
    public String tp_immediately_after_the_4th_due_date;

    @Value("${no_status}")
    public String no_status;

    @Value("${complete_status}")
    public String complete_status;


    @Value("${process_payment_file_validate}")
    public int process_payment_file_validate;

    @Value("${payment_file_cheque_initiate_txn_types}")
    public String payment_file_cheque_initiate_txn_types;

    @Value("${payment_file_cheque_return_txn_types}")
    public String payment_file_cheque_return_txn_types;

    @Value("${process_atm_file_validate}")
    public int process_atm_file_validate;


    @Value("${file_code_atm}")
    public String file_code_atm;

    @Value("${fail_status}")
    public String fail_status;

    @Value("${init_status}")
    public String init_status;

    @Value("${process_letter_gen_application_reject}")
    public int process_letter_gen_application_reject;
    @Value("${process_card_expire}")
    public int process_card_expire;

    @Value("${process_crib_file}")
    public int process_crib_file;

    @Value("${process_id_overlimit_fee}")
    public int process_id_overlimit_fee;

    @Value("${process_chequepayment}")
    public int process_chequepayment;

    @Value("${snapshot_process}")
    public int snapshot_process;

    @Value("${process_transaction_drop_request}")
    public int process_transaction_drop_request;

    @Value("${loyalty_point_calculation_process}")
    public int loyalty_point_calculation_process;

    @Value("${process_cashback_alert}")
    public int process_cashback_alert;

    @Value("${process_collection_and_recovery_alert}")
    public int process_collection_and_recovery_alert;

    @Value("${process_letter_gen_application_approve}")
    public int process_letter_gen_application_approve;

    @Value("${online_reverse_status}")
    public int online_reverse_status;

    @Value("${online_drop_status}")
    public int online_drop_status;

    @Value("${online_partially_reverse_status}")
    public int online_partially_reverse_status;

    @Value("${online_txn_incomplete_status}")
    public int online_txn_incomplete_status;

    @Value("${eod_issuing_status}")
    public int eod_issuing_status;

    @Value("${cash_back_sms}")
    public String cash_back_sms;

    @Value("${stmt_with_cash_back_sms}")
    public String stmt_with_cash_back_sms;

    @Value("${stmt_without_cash_back_sms}")
    public String stmt_without_cash_back_sms;
    @Value("${application_rejection_letter}")
    public String application_rejection_letter;

    @Value("${x_days_before_1_due_date}")
    public String x_days_before_1_due_date;

    @Value("${x_days_before_1_due_date_email}")
    public String x_days_before_1_due_date_email;

    @Value("${x_days_before_1_due_date_sms}")
    public String x_days_before_1_due_date_sms;

    @Value("${immediately_after_1_due_date}")
    public String immediately_after_1_due_date;

    @Value("${immediately_after_1_due_date_email}")
    public String immediately_after_1_due_date_email;

    @Value("${immediately_after_1_due_date_sms}")
    public String immediately_after_1_due_date_sms;

    @Value("${immediately_after_2_due_date}")
    public String immediately_after_2_due_date;

    @Value("${immediately_after_2_due_date_email}")
    public String immediately_after_2_due_date_email;

    @Value("${immediately_after_2_due_date_sms}")
    public String immediately_after_2_due_date_sms;

    @Value("${immediately_after_3_due_date}")
    public String immediately_after_3_due_date;

    @Value("${immediately_after_3_due_date_email}")
    public String immediately_after_3_due_date_email;

    @Value("${immediately_after_3_due_date_sms}")
    public String immediately_after_3_due_date_sms;

    @Value("${email_template}")
    public String email_template;

    @Value("${sms_template}")
    public String sms_template;

    @Value("${email_template_code}")
    public int email_template_code;

    @Value("${sms_template_code}")
    public int sms_template_code;

    @Value("${alert_unread}")
    public int alert_unread;

    @Value("${annual_fee}")
    public String annual_fee;

    @Value("${TXN_TYPE_UNEARNED_INCOME}")
    public String TXN_TYPE_UNEARNED_INCOME;

    @Value("${TXN_TYPE_UNEARNED_INCOME_UPFRON_FALSE}")
    public String TXN_TYPE_UNEARNED_INCOME_UPFRONT_FALSE;

    @Value("${TXN_TYPE_FEE_INSTALLMENT_UPFRON_FALSE}")
    public String TXN_TYPE_FEE_INSTALLMENT_UPFRONT_FALSE;

    @Value("${cash_advance_fee}")
    public String CASH_ADVANCE_FEE;

    @Value("${annual_fee_for_NP_accounts}")
    public int ANNUAL_FEE_FOR_NP_ACCOUNTS;

    @Value("${cheque_return_on_payments_other_reasons_fee}")
    public String CHEQUE_RETURN_ON_PAYMENTS_OTHER_REASONS_FEE;

    @Value("${cheque_return_on_payments_insufficient_funds_fee}")
    public String CHEQUE_RETURN_ON_PAYMENTS_INSUFFICIENT_FUNDS_FEE;

    @Value("${cheque_return_on_payments_stop_fee}")
    public String CHEQUE_RETURN_ON_PAYMENTS_STOP_FEE;

    @Value("${no_of_months_for_permanent_block}")
    public int NO_OF_MONTHS_FOR_PERMENANT_BLOCK;
    //Letter
    @Value("${card_category_supplementory}")
    public String card_category_supplementory;

    @Value("${card_category_affinity_supplementory}")
    public String card_category_affinity_supplementory;

    @Value("${card_category_co_branded_supplementory}")
    public String card_category_co_branded_supplementory;

    @Value("${card_category_fd_supplementory}")
    public String card_category_fd_supplementory;

    @Value("${card_category_corporate}")
    public String card_category_corporate;

    @Value("${stamp_duty_fee}")
    public String stamp_duty_fee;

    //Country Code
    @Value("${country_code_srilanka}")
    public String country_code_srilanka;

    //Cash Back
    @Value("${process_cashback}")
    public int process_cashback;


    @Value("${process_knock_off}")
    public int process_knock_off;

    //Manul Np Process
    @Value("${process_manual_np}")
    public int process_manual_np;

    @Value("${NPINTEREST_ON_NP_GL}")
    public String NPINTEREST_ON_NP_GL;

    @Value("${NPOUSTANDING_ON_NP_GL}")
    public String NPOUSTANDING_ON_NP_GL;
    @Value("${KNOCKOFF_NP_INTEREST_DECLASSIFIED_GL}")
    public String KNOCKOFF_NP_INTEREST_DECLASSIFIED_GL;
    @Value("${KNOCKOFF_NP_OUTSTANDING_DECLASSIFIED_GL}")
    public String KNOCKOFF_NP_OUTSTANDING_DECLASSIFIED_GL;
    @Value("${KNOCKOFF_ACCRUED_LATE_DECLASSIFIED_GL}")
    public String KNOCKOFF_ACCRUED_LATE_FEE_DECLASSIFIED_GL;
    @Value("${KNOCKOFF_ACCRUED_INT_DECLASSIFIED_GL}")
    public String KNOCKOFF_ACCRUED_INTEREST_DECLASSIFIED_GL;
    @Value("${KNOCKOFF_ACCRUED_OVERLIM_DECLASSIFIED_GL}")
    public String KNOCKOFF_ACCRUED_OVERLIMIT_FEE_DECLASSIFIED_GL;
    @Value("${KNOCKOFF_ACCRUED_OTH_FEE_DECLASSIFIED_GL}")
    public String KNOCKOFF_ACCRUED_OTHER_FEES_DECLASSIFIED_GL;

    //Online to Backend Txn Syn
    @Value("${process_manual_np}")
    public String online_listner_type_atm;

    @Value("${online_listner_type_nac}")
    public String online_listner_type_nac;

    @Value("${eod_not_consider_status}")
    public int eod_not_consider_status;

    @Value("${ADJUSTMENT_PROCESS}")
    public int ADJUSTMENT_PROCESS;

    @Value("${process_id_balance_transfer}")
    public int process_id_balance_transfer;

    @Value("${process_id_easy_payment}")
    public int process_id_easy_payment;

    @Value("${process_interest_calculation}")
    public int process_interest_calculation;

    @Value("${batch_size}")
    public int batch_size;

    @Value("${eod_live_mode}")
    public int eod_live_mode;

    @Value("${main_eod_starttime_h}")
    public int main_eod_starttime_h;

    @Value("${main_eod_starttime_m}")
    public int main_eod_starttime_m;

    @Value("${sub_eod_starttime_h}")
    public int sub_eod_starttime_h;

    @Value("${sub_eod_starttime_m}")
    public int sub_eod_starttime_m;

    @Value("${core_pool_size}")
    public int core_pool_size;

    @Value("${card_domain_credit}")
    public String card_domain_credit;

    @Value("${card_domain_dedit}")
    public String card_domain_dedit;

    @Value("${card_initial_status}")
    public String card_initial_status;

    @Value("${credit_status}")
    public String credit_status;

    @Value("${online_deactive_status}")
    public int online_deactive_status;

    @Value("${online_ofus_bin}")
    public int online_ofus_bin;

    @Value("${merchant_file_path_slips}")
    public String merchant_file_path_slips;

    @Value("${merchant_file_path_cheque}")
    public String merchant_file_path_cheque;

    @Value("${merchant_file_path_direct}")
    public String merchant_file_path_direct;

    @Value("${merchant_stat_auto_file_path}")
    public String merchant_stat_auto_file_path;

    @Value("${merchant_stat_auto_file_name}")
    public String merchant_stat_auto_file_name;

    @Value("${internal_key_for_cheque}")
    public int internal_key_for_cheque;

    @Value("${internal_key_for_cash}")
    public int internal_key_for_cash;

    @Value("${main_eod_process}")
    public int main_eod_process;

    @Value("${process_id_pingeneration}")
    public int process_id_pingeneration;

    @Value("${process_id_eom_interest_calculation}")
    public int process_id_eom_interest_calculation;

    @Value("${process_id_overlimit_interest_calculation}")
    public int process_id_overlimit_interest_calculation;

    @Value("${process_id_credit_score}")
    public int process_id_credit_score;

    @Value("${process_merchant_transaction}")
    public int process_merchant_transaction;

    @Value("${process_merchant_settlement}")
    public int process_merchant_settlement;

    @Value("${parameter_initialization}")
    public int parameter_initialization;

    @Value("${process_emboss_filegeneration}")
    public int process_emboss_filegeneration;

    @Value("${process_merchant_payment_cycle}")
    public int process_merchant_payment_cycle;

    @Value("${process_create_crib_request}")
    public int process_create_crib_request;

    @Value("${process_exposure_file}")
    public int process_exposure_file;

    @Value("${process_sup_card_reset}")
    public int process_sup_card_reset;

    @Value("${process_id_cashback_file_generation}")
    public int process_id_cashback_file_generation;

    @Value("${process_card_renew}")
    public int process_card_renew;

    @Value("${process_monthly_statement}")
    public int process_monthly_statement;

    @Value("${process_monthly_statement_file_creation}")
    public int process_monthly_statement_file_creation;

    @Value("${process_id_visa_baseii_clearing}")
    public int process_id_visa_baseii_clearing;

    @Value("${process_id_copy_visa_transaction_to_backend}")
    public int process_id_copy_visa_transaction_to_backend;

    @Value("${merchant_payment_startdate}")
    public String merchant_payment_startdate;

    @Value("${process_onlinetobackend_txnsync}")
    public int process_onlinetobackend_txnsync;

    @Value("${auto_settlement_process}")
    public int auto_settlement_process;

    @Value("${process_visa_settlementdate_update}")
    public int process_visa_settlementdate_update;

    @Value("${process_eodtransaction_update}")
    public int process_eodtransaction_update;

    @Value("${process_eodcashadvance_update}")
    public int process_eodcashadvance_update;

    @Value("${process_id_visa_bin_file_read}")
    public int process_id_visa_bin_file_read;

    @Value("${process_id_pre_merchant_fee_process}")
    public int process_id_pre_merchant_fee_process;

    @Value("${process_acquiring_adjustment_process}")
    public int process_acquiring_adjustment_process;

    @Value("${process_wallet_settlement_file_read}")
    public int process_wallet_settlement_file_read;

    @Value("${process_wallet_settlement_file_update}")
    public int process_wallet_settlement_file_update;

    @Value("${process_master_card_t67_file_read}")
    public int process_master_card_t67_file_read;

    @Value("${maximum_tcrs_in_ctf_batch}")
    public int maximum_tcrs_in_ctf_batch;

    @Value("${process_id_outgoing_ctf_file_gen}")
    public int process_id_outgoing_ctf_file_gen;

    @Value("${manual_cash_mcc}")
    public String manual_cash_mcc;

    @Value("${visa_acq_bin}")
    public String visa_acq_bin;

    @Value("${process_id_outgoing_cup_file_gen}")
    public int process_id_outgoing_cup_file_gen;

    @Value("${process_id_outgoing_ipm_file_gen}")
    public int process_id_outgoing_ipm_file_gen;

    @Value("${process_dcf_file_read}")
    public int process_dcf_file_read;

    @Value("${process_dcf_file_validate}")
    public int process_dcf_file_validate;

    @Value("${process_check_payment_for_min_amount}")
    public int process_check_payment_for_min_amount;

    @Value("${process_id_risk_calculation_process}")
    public int process_id_risk_calculation_process;

    @Value("${process_id_master_clearing}")
    public int process_id_master_clearing;

    @Value("${process_id_master_card_abu_file_gen}")
    public int process_id_master_card_abu_file_gen;

    @Value("${process_card_renew_letter}")
    public int process_card_renew_letter;

    @Value("${card_replace_letter_process}")
    public int card_replace_letter_process;

    @Value("${STATEMENT_NOTIFY_MSG}")
    public String STATEMENT_NOTIFY_MSG;

    @Value("${process_gl_file_process}")
    public int process_gl_file_process;

    @Value("${PROCESS_RB36_FILE_CREATION}")
    public int PROCESS_RB36_FILE_CREATION;

    @Value("${process_collection_and_recovery_letter}")
    public int process_collection_and_recovery_letter;

    @Value("${process_bulk_application_read}")
    public int process_bulk_application_read;

    @Value("${process_bulk_application_validate}")
    public int process_bulk_application_validate;

    @Value("${GL_SUMMARY_FILE_PREFIX}")
    public String GL_SUMMARY_FILE_PREFIX;

    @Value("${MERCHANT_GL_SUMMARY_FILE_PREFIX}")
    public String MERCHANT_GL_SUMMARY_FILE_PREFIX;

    @Value("${OUTPUTFILE_FIELD_DELIMETER}")
    public String OUTPUTFILE_FIELD_DELIMETER;

    @Value("${CASHBACK_FILE_PREFIX_F1}")
    public String CASHBACK_FILE_PREFIX_F1;

    @Value("${MERCHANT_PAYMENT_FILE_DIRECT_PREFIX_F1}")
    public String MERCHANT_PAYMENT_FILE_DIRECT_PREFIX_F1;

    @Value("${MERCHANT_PAYMENT_FILE_SLIPS_PREFIX}")
    public String MERCHANT_PAYMENT_FILE_SLIPS_PREFIX;

    @Value("${RB36_FILE_PREFIX}")
    public String RB36_FILE_PREFIX;

    @Value("${AUTOSETTLEMENT_FILE_PREFIX}")
    public String AUTOSETTLEMENT_FILE_PREFIX;

    @Value("${CASHBACK_FILE_PREFIX_F2}")
    public String CASHBACK_FILE_PREFIX_F2;

    @Value("${MERCHANT_PAYMENT_FILE_DIRECT_PREFIX_F2}")
    public String MERCHANT_PAYMENT_FILE_DIRECT_PREFIX_F2;

    @Value("${process_txn_post}")
    public int process_txn_post;

    @Value("${process_fee_post}")
    public int PROCESS_ID_FEE_POST;

    @Value("${process_eodtransaction_update}")
    public int PROCESS_EODTRANSACTIONUPDATE;

    @Value("${report_id}")
    public String report_id;

    @Value("${subject_type}")
    public String subject_type;

    @Value("${response_type}")
    public String response_type;

    @Value("${inquiry_reoson_code}")
    public String inquiry_reoson_code;

    @Value("${bulk_status}")
    public String bulk_status;

    @Value("${status}")
    public String status;

    @Value("${card_replacement_letter}")
    public String card_replacement_letter;

    @Value("${second_reminder_letter}")
    public String second_reminder_letter;

    @Value("${card_renewal_letter}")
    public String card_renewal_letter;

    @Value("${application_confirmation_letter}")
    public String application_confirmation_letter;

    @Value("${first_reminder_letter}")
    public String first_reminder_letter;

    @Value("${product_change_letter_code}")
    public String product_change_letter_code;

    @Value("${collection_account}")
    public String collection_account;

    @Value("${exposure_file_branch}")
    public String exposure_file_branch;

    @Value("${exposure_file_product}")
    public String exposure_file_product;

    @Value("${exposure_file_facility_type}")
    public String exposure_file_facility_type;

    @Value("${eod_txn_auth_only_status}")
    public String eod_txn_auth_only_status;

    @Value("${eod_txn_auth_only_init}")
    public String eod_txn_auth_only_init;

    @Value("${eod_txn_auth_only_posted}")
    public String eod_txn_auth_only_posted;

    @Value("${online_txn_eod_copied_sts}")
    public int online_txn_eod_copied_sts;

    @Value("${online_txn_eod_not_copied_sts}")
    public int online_txn_eod_not_copied_sts;

    @Value("${merchant_annual_fee}")
    public String merchant_annual_fee;

    @Value("${merchant_bi_monthly_fee}")
    public String merchant_bi_monthly_fee;

    @Value("${merchant_quarterly_fee}")
    public String merchant_quarterly_fee;

    @Value("${merchant_half_yearly_fee}")
    public String merchant_half_yearly_fee;

    @Value("${terminal_monthly_rental_fee}")
    public String terminal_monthly_rental_fee;

    @Value("${terminal_bi_monthly_rental_fee}")
    public String terminal_bi_monthly_rental_fee;

    @Value("${terminal_half_yearly_rental_fee}")
    public String terminal_half_yearly_rental_fee;

    @Value("${terminal_weekly_rental_fee}")
    public String terminal_weekly_rental_fee;

    @Value("${terminal_quarterly_rental_fee}")
    public String terminal_quarterly_rental_fee;

    @Value("${terminal_maintainace_fee}")
    public String terminal_maintainace_fee;

    @Value("${TXN_TYPE_CASHBACK}")
    public String TXN_TYPE_CASHBACK;

    @Value("${TXN_TYPE_CASHBACK_REDEEMED}")
    public String TXN_TYPE_CASHBACK_REDEEMED;

    @Value("${TXN_TYPE_CASHBACK_EXRIRED}")
    public String TXN_TYPE_CASHBACK_EXRIRED;

    @Value("${TXN_TYPE_CASHBACK_NP}")
    public String TXN_TYPE_CASHBACK_NP;

    @Value("${TXN_TYPE_INTEREST_INCOME}")
    public String TXN_TYPE_INTEREST_INCOME;

    @Value("${CASH_BACK_FILE_CRDR}")
    public String CASH_BACK_FILE_CRDR;

    @Value("${CASHBACK_TXN_TYPE}")
    public String CASHBACK_TXN_TYPE;

    @Value("${Standing_Instruction}")
    public int Standing_Instruction;

    @Value("${Over_the_Counter}")
    public int Over_the_Counter;

    @Value("${Third_Party}")
    public int Third_Party;

    @Value("${INCOMMING_IPM_FILE_ENCODING_FORMAT}")
    public int INCOMMING_IPM_FILE_ENCODING_FORMAT;

    @Value("${ICA_AIIC}")
    public int ICA_AIIC;

    @Value("${ICA_AIIC_VISA}")
    public int ICA_AIIC_VISA;

    @Value("${OUTGOING_IPM_FILE_ENCODING_FORMAT}")
    public int OUTGOING_IPM_FILE_ENCODING_FORMAT;

    @Value("${OUTGOING_IPM_FILE_LAYOUT}")
    public int OUTGOING_IPM_FILE_LAYOUT;

    @Value("${master_acq_bin}")
    public String master_acq_bin;

    @Value("${read_t67_ip0040t1_table}")
    public int read_t67_ip0040t1_table;

    @Value("${read_t67_ip0075t1_table}")
    public int read_t67_ip0075t1_table;

    @Value("${file_name_prefix}")
    public String file_name_prefix;

    @Value("${file_name_sufix}")
    public String file_name_sufix;

    @Value("${institution_identification_number}")
    public String institution_identification_number;

    @Value("${header_txn_code}")
    public String header_txn_code;

    @Value("${tailer_txn_code}")
    public String tailer_txn_code;

    @Value("${version_tag}")
    public String version_tag;

    @Value("${version_number}")
    public String version_number;

    @Value("${process_cup_bin_file_read}")
    public int process_cup_bin_file_read;

    @Value("${NP_ACCRUED_INTEREST_GL}")
    public String NP_ACCRUED_INTEREST_GL;

    @Value("${merchant_fee_process}")
    public int merchant_fee_process;

    @Value("${process_id_merchant_payment_process}")
    public int process_id_merchant_payment_process;

    @Value("${process_id_eod_merchant_easy_payment_request}")
    public int process_id_eod_merchant_easy_payment_request;
    @Value("${commission_calculation_process}")
    public int commission_calculation_process;

    @Value("${merchant_statement_process}")
    public int PROCESS_ID_MERCHANT_STATEMENT;

    @Value("${merchant_statement_file_generation_process}")
    public int PROCESS_MERCHANT_STATEMENT_FILE_CREATION;

    @Value("${merchant_customer_statement_file_generation_process}")
    public int PROCESS_MERCHANT_CUSTOMER_STATEMENT_FILE_CREATION;

    @Value("${process_monthly_statement_file_creation}")
    public int PROCESS_MONTHLY_STATEMENT_FILE_CREATION;

    @Value("${statement_batch_size}")
    public int STATEMENT_BATCH_SIZE;

    @Value("${process_stamp_duty_fee}")
    public int process_stamp_duty_fee;

    @Value("${process_merchant_gl_file_process}")
    public int process_merchant_gl_file_process;

    @Value("${OUTPUT_FILE_PROD_CODE}")
    public String OUTPUT_FILE_PROD_CODE;

    //Acquiring
    @Value("${process_id_originator_push_txn_update}")
    public int process_id_originator_push_txn_update;

    @Value("${processing_mode}")
    public String processing_mode;

    @Value("${file_code_mastercard_t67}")
    public String FILE_CODE_MASTERCARD_T67;

    @Value("${file_code_dcf}")
    public String FILE_CODE_DCF;

}
