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
public class StatusVarList {

    @Value("${status_yes}")
    public String STATUS_YES;

    @Value("${status_no}")
    public String STATUS_NO;

    @Value("${file_not_exist}")
    public String FILE_NOT_EXIST;/*file not found*/

    @Value("${file_validation_error}")
    public String FILE_VALIDATION_ERROR;/*file validation error*/

    @Value("${database_error}")
    public String DATABASE_ERROR;/*Database error*/

    @Value("${initial_status}")
    public String INITIAL_STATUS;

    @Value("${eod_inprogress_status}")
    public String INPROGRESS_STATUS;

    @Value("${eod_complete_status}")
    public String SUCCES_STATUS;

    @Value("${eod_error_status}")
    public String ERROR_STATUS;

    @Value("${eod_error_inpr_status}")
    public String ERROR_INPR_STATUS;

    @Value("${comission_combination_min}")
    public String COMISSION_COMBINATION_MIN;

    @Value("${comission_combination_max}")
    public String COMISSION_COMBINATION_MAX;

    @Value("${comission_combination_add}")
    public String COMISSION_COMBINATION_ADD;

    @Value("${yes_status_1}")
    public int YES_STATUS_1;

    @Value("${no_status_0}")
    public int NO_STATUS_0;

    @Value("${active_status}")
    public String ACTIVE_STATUS;

    @Value("${deactive_status}")
    public String DEACTIVE_STATUS;

    @Value("${application_confirmed_status}")
    public String APPLICATION_CONFIRMED_STATUS;

    @Value("${application_verified_status}")
    public String APPLICATION_VERIFIED_STATUS;

    @Value("${application_credit_score_completed_status}")
    public String APPLICATION_CREDIT_SCORE_COMPLETED_STATUS;

    @Value("${application_verification_onhold_status}")
    public String APPLICATION_VERIFICATION_ONHOLD_STATUS;

    @Value("${application_checking_completed_status}")
    public String APPLICATION_CHECKING_COMPLETED_STATUS;

//    @Value("${expired_status}")
//    public  String Expired_STATUS;

//    @Value("${inactive_status}")
//    public  String INACTIVE_STATUS;

    @Value("${backend_card_inactive_status}")
    public String CARD_INIT;

//    @Value("${file_not_exist}")
//    public  String CARD_RENEWAL_CONFIRM;

    @Value("${txn_type_cash_advance}")
    public String TXN_TYPE_CASH_ADVANCE;

    @Value("${txn_type_sale}")
    public String TXN_TYPE_SALE;

    @Value("${txn_type_easy_payment}")
    public String TXN_TYPE_EASY_PAYMENT;

    @Value("${fee_type_late_fee}")
    public String FEE_TYPE_LATE_FEE;

    @Value("${fee_type_cash_advance_fee}")
    public String FEE_TYPE_CASH_ADVANCE_FEE;

    @Value("${cash_payment}")
    public String CASH_PAYMENT;

    @Value("${cheque_payment}")
    public String CHEQUE_PAYMENT;

    @Value("${cash_deposit_type}")
    public String CASH_DEPOSIT_TYPE;

    @Value("${fund_transfer_type}")
    public String FUND_TRANSFER_TYPE;

    @Value("${ob_bills_type}")
    public String OB_BILLS_TYPE;

    @Value("${st_order_type}")
    public String STD_ORDER_TYPE;

    //Reversals
    @Value("${cash_reversal_type}")
    public String CASH_REVERSAL_TYPE;

    @Value("${cheque_reversal_type}")
    public String CHEQUE_REVERSAL_TYPE;
    //Cheque online
    @Value("${cheque_return_own_status_online}")
    public String CHEQUE_RETURN_OWN_ONLINE_TYPE;

    @Value("${cheque_initiate_own_status_online}")
    public String CHEQUE_OWN_INITIATE_ONLINE_TYPE;

    @Value("${cheque_return_status_online}")
    public String CHEQUE_RETURN_ONLINE_TYPE;

    @Value("${cheque_return_b_status_online}")
    public String CHEQUE_RETURN_B_ONLINE_TYPE;

    @Value("${cheque_initiate_status_online}")
    public String CHEQUE_INITATE_ONLINE_TYPE;

    @Value("${cheque_initiate_b_status_online}")
    public String CHEQUE_INITATE_B_ONLINE_TYPE;

    //Cheque backend
    @Value("${cheque_return_status}")
    public String CHEQUE_RETURN_STATUS;

    @Value("${cheque_initiate_status}")
    public String CHEQUE_INITIATE_STATUS;

    @Value("${cheque_realized_status}")
    public String CHEQUE_REALIZED_STATUS;

    @Value("${cash_reversal_status}")
    public String CASH_REVERSAL_STATUS;

    @Value("${eod_done_status}")
    public String EOD_DONE_STATUS;

    @Value("${billing_done_status}")
    public String BILLING_DONE_STATUS;

    @Value("${merchant_loc_billing_done_status}")
    public String MERCHANT_LOC_BILLING_DONE_STATUS;

    @Value("${merchant_cus_billing_done_status}")
    public String MERCHANT_CUS_BILLING_DONE_STATUS;

    @Value("${eod_return_cheque}")
    public String EOD_RETURN_CHEQUE;  /*cheque is return after EOD process*/

    @Value("${billing_return_cheque}")
    public String BILLING_RETURN_CHEQUE;  /*cheque is return after Billing process*/

    /*Card Status*/

    @Value("${replace}")
    public String REPLACE;


    @Value("${backend_card_block_status}")
    public String CARD_BLOCK_STATUS;

    @Value("${backend_card_replaced_status}")
    public String CARD_REPLACED_STATUS;

    @Value("${backend_card_closed_status}")
    public String CARD_CLOSED_STATUS;

    @Value("${backend_card_expired_status}")
    public String CARD_EXPIRED_STATUS;

    @Value("${backend_card_permanently_blocked}")
    public String CARD_PERMANENT_BLOCKED_STATUS;

    @Value("${backend_card_active_status}")
    public String CARD_ACTIVE_STATUS;

    @Value("${backend_card_virtual_active_status}")
    public String CARD_VIRTUAL_ACTIVE_STATUS;

    @Value("${card_product_change_status}")
    public  String CARD_PRODUCT_CHANGE_STATUS;

    @Value("${online_card_expired_status}")
    public int ONLINE_CARD_EXPIRED_STATUS;

    @Value("${online_card_closed_status}")
    public int ONLINE_CARD_CLOSED_STATUS;

    @Value("${online_card_replaced_status}")
    public int ONLINE_CARD_REPLACED_STATUS;

    @Value("${online_card_inactive_status}")
    public int ONLINE_CARD_INACTIVE_STATUS;

    @Value("${online_card_permanently_blocked_status}")
    public int ONLINE_CARD_PERMANENTLY_BLOCKED_STATUS;

    @Value("${online_card_temporarily_blocked_status}")
    public int ONLINE_CARD_TEMPORARILY_BLOCKED_STATUS;

    @Value("${online_customer_closed_status}")
    public int ONLINE_CUSTOMER_CLOSED_STATUS;

    @Value("${online_account_closed_status}")
    public int ONLINE_ACCOUNT_CLOSED_STATUS;

    @Value("${online_card_active_status}")
    public int ONLINE_CARD_ACTIVE_STATUS;

    @Value("${online_card_virtual_active_status}")
    public int ONLINE_CARD_VIRTUAL_ACTIVE_STATUS;

    @Value("${online_card_deactive_status}")
    public int ONLINE_CARD_DEACTIVE_STATUS;

    /*card limit enhancement status*/

    @Value("${credit_limit_enhancement_request_approved}")
    public String CREDIT_LIMIT_ENHANCEMENT_REQUEST_CONFIRMED;

    @Value("${credit_limit_enhancement_expired}")
    public String CREDIT_LIMIT_ENHANCEMENT_EXPIRED;

    @Value("${credit_limit_enhancement_inprogress}")
    public String CREDIT_LIMIT_ENHANCEMENT_INPROGRESS;

    @Value("${credit_limit_enhancement_request_initiate}")
    public String CREDIT_LIMIT_ENHANCEMENT_REQUEST_INITIATE;

    @Value("${credit_limit_enhancement_request_reject}")
    public String CREDIT_LIMIT_ENHANCEMENT_REQUEST_REJECT;

    @Value("${credit_limit_enhancement_active}")
    public String CREDIT_LIMIT_ENHANCEMENT_ACTIVE;

    /*Card renewal status*/
    @Value("${card_renewal_initiate}")
    public String CARD_RENEWAL_INITIATE;

    @Value("${card_renewal_accepted}")
    public String CARD_RENEWAL_ACCEPTED;

    @Value("${card_renewal_rejected}")
    public String CARD_RENEWAL_REJECTED;

    @Value("${card_renewal_complete}")
    public String CARD_RENEWAL_COMPLETE;


    @Value("${EOD_PENDING_STATUS}")
    public String EOD_PENDING_STATUS;

    @Value("${backend_card_temporarily_blocked}")
    public String CARD_TEMPORARY_BLOCK_Status;

    public int ONLINE_CARD_TEMPORARY_BLOCK;
    public int ONLINE_CARD_PERMENANT_BLOCK;

    public int ONHOLD_STATUS;
    public int RUNNING_STATUS;
    public int COMPLETED_STATUS;

    //Risk Classes
    public String RISK_CLASS_ONE;
    public String RISK_CLASS_ZERO;
    public String RISK_CLASS_TWO;
    public String RISK_CLASS_THREE;
    public String RISK_CLASS_FOUR;
    public String RISK_CLASS_FIVE;
    public String RISK_CLASS_SIX;
    public String RISK_CLASS_SEVEN;
    public String RISK_CLASS_EIGHT;

    @Value("${risk_class_nine}")
    public String RISK_CLASS_NINE;

    //Manual Adjustment Status
    @Value("${manual_adjestment_approve}")
    public String MANUAL_ADJUSTMENT_ACCEPT;

    //Cheque
    //Delinquent Status
    @Value("${to_active_status}")
    public String TO_ACTIVE_STATUS;
    @Value("${to_resolve_status}")
    public String TO_RESOLVE_STATUS;
    @Value("${manual_np_status}")
    public String ONLY_MANUAL_NP_STATUS;

    @Value("${to_performing_to_non_performing}")
    public String TO_PERFORMING_TO_NON_PERFORMING_STATUS;

    @Value("${to_performing_to_performing}")
    public String TO_PERFORMING_TO_PERFORMING_STATUS;

    @Value("${to_non_performing_to_performing}")
    public String TO_NON_PERFORMING_TO_PERFORMING_STATUS;

    @Value("${to_non_performing_to_non_performing}")
    public String TO_NON_PERFORMING_TO_NON_PERFORMING_STATUS;

    @Value("${card_request_accepted}")
    public String REQUEST_ACCEPTED;
    //Common
    @Value("${common_request_accepted}")
    public String COMMON_REQUEST_ACCEPTED;

    @Value("${common_request_rejected}")
    public String COMMON_REQUEST_REJECT;

    @Value("${common_completed}")
    public String COMMON_COMPLETED;

    @Value("${common_request_initiate}")
    public String COMMON_REQUEST_INITIATE;

    @Value("${active_status}")
    public String ACCOUNT_ACTIVE_STATUS;

    @Value("${performing_status}")
    public String ACCOUNT_PERFORMING_STATUS;

    @Value("${non_performing_status}")
    public String ACCOUNT_NON_PERFORMING_STATUS;

    @Value("${account_closed_status}")
    public String ACCOUNT_CLOSED_STATUS;

    @Value("${BULK_APP_REVIEW}")
    public String BULK_APPLICATION_REVISION;

    //online_init_status
    @Value("${online_init_status}")
    public int ONLINE_INIT_STATUS;

    @Value("${online_eoddone_status}")
    public int ONLINE_EODDONE_STATUS;

    @Value("${online_active_status}")
    public int ONLINE_ACTIVE_STATUS;

    @Value("${online_deactive_status}")
    public int ONLINE_DEACTIVE_STATUS;

    @Value("${online_pintryexceed_status}")
    public int ONLINE_PINTRYEXCEED_STATUS;


    //TXN RESPONSE CODE
    @Value("${response_code_00}")
    public  String RESPONSE_CODE_00;

    @Value("${txn_settled_status}")
    public  String TXN_SETTLLED_STATUS;

    @Value("${onus_status}")
    public  int ONUS_STATUS;

    @Value("${requestfrom_listner}")
    public  int REQUESTFROM_LISTNER;

    public  int REQUESTFROM_CHANNEL;
    public  int LISTENER_TYPE_ATM;
    public  int LISTENER_TYPE_NAC;

    @Value("${listener_type_ipg}")
    public  int LISTENER_TYPE_IPG;
    public  int CHANNEL_ID_VISA;
    public  int CHANNEL_ID_MASTER;

    @Value("${txn_complete_status}")
    public  String TXN_COMPLETE_STATUS;


    public  String TXN_RECIPIENT_REFUND_STATUS;


    //Merchant status
    @Value("${MERCHANT_DELETE_STATUS}")
    public String MERCHANT_DELETE_STATUS;

    @Value("${MERCHANT_CANCEL_STATUS}")
    public String MERCHANT_CANCEL_STATUS;

    @Value("${MERCHANT_DEACTIVE_STATUS}")
    public String MERCHANT_DEACTIVE_STATUS;

    @Value("${MERCHANT_CUSTOMER_DELETE_STATUS}")
    public String MERCHANT_CUSTOMER_DELETE_STATUS;

    @Value("${MERCHANT_CUSTOMER_CANCEL_STATUS}")
    public String MERCHANT_CUSTOMER_CANCEL_STATUS;

    //Terminal Status
    @Value("${TERMINAL_DELETE_STATUS}")
    public String TERMINAL_DELETE_STATUS;

    //aquiring commission cal status
    @Value("${transaction_wise}")
    public String COMMISSION_TRANSACTION_WISE;

    @Value("${volume_wise}")
    public String COMMISSION_VOLUME_WISE;

    @Value("${mcc_wise}")
    public String COMMISSION_MCC_WISE;

    @Value("${commission_complete_status}")
    public String COMMISSION_COMPLETE_STATUS;

    @Value("${commission_status}")
    public String COMMISSION_STATUS;

    @Value("${payment_status}")
    public String PAYMENT_STATUS;

    @Value("${product_code_visa_all}")
    public String PRODUCT_CODE_VISA_ALL;

    @Value("${product_code_master_all}")
    public String PRODUCT_CODE_MASTER_ALL;

    @Value("${product_code_qr_all}")
    public String PRODUCT_CODE_QR_ALL;

    //PaymentFileValidation
    @Value("${product_code_ipg_visa}")
    public String PRODUCT_CODE_IPG_VISA;

    @Value("${product_code_ipg_master}")
    public String PRODUCT_CODE_IPG_MASTER;

    @Value("${product_code_cup_all}")
    public String PRODUCT_CODE_CUP_ALL;

    @Value("${fee_promotion_profile_expire}")
    public String FEE_PROMOTION_PROFILE_EXPIRE;
}
