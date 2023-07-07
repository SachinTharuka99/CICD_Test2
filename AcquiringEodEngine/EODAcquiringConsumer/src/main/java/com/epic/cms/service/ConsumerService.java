package com.epic.cms.service;

import com.epic.cms.connector.*;
import com.epic.cms.util.Configurations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class ConsumerService {

    @Autowired
    AcqTxnUpdateConnector acqTxnUpdateConnector;

    @Autowired
    PreMerchantFeeConnector preMerchantFeeConnector;

    @Autowired
    AcquiringAdjustmentConnector acquiringAdjustmentConnector;

    @Autowired
    MerchantPaymentFileConnector merchantPaymentFileConnector;

    @Autowired
    MerchantGLSummaryFileConnector merchantGLSummaryFileConnector;

    @Autowired
    MerchantFeeConnector merchantFeeConnector;

    @Autowired
    MerchantPaymentConnector merchantPaymentConnector;

    @Autowired
    MerchantEasyPaymentRequestConnector merchantEasyPaymentRequestConnector;

    @Autowired
    MerchantCommissionCalculationConnector commissionCalculationConnector;

    @Autowired
    EodParameterResetConnector eodParameterResetConnector;

    @KafkaListener(topics = "acqTxnUpdate", groupId = "group_acqTxnUpdate")
    public void acqTxnUpdateConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start AcqTxnUpdate Process");
        acqTxnUpdateConnector.startProcess(Configurations.PROCESS_ID_ACQUIRING_TXN_UPDATE_PROCESS, uniqueID);
        System.out.println("Complete AcqTxnUpdate Process");
    }

    @KafkaListener(topics = "PreMerchantFee", groupId = "group_PreMerchantFee")
    public void PreMerchantFeeConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start PreMerchantFeeProcess");
        preMerchantFeeConnector.startProcess(Configurations.PROCESS_PRE_MERCHANT_FEE_PROCESS, uniqueID);
        System.out.println("Complete PreMerchantFee Process");
    }

    @KafkaListener(topics = "AcquiringAdjustment", groupId = "group_AcquiringAdjustment")
    public void AcquiringAdjustmentConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start AcquiringAdjustment Process");
        acquiringAdjustmentConnector.startProcess(Configurations.PROCESS_ACQUIRING_ADJUSTMENT_PROCESS, uniqueID);
        System.out.println("Complete AcquiringAdjustment Process");
    }

    @KafkaListener(topics = "MerchantPaymentFile", groupId = "group_MerchantPaymentFile")
    public void MerchantPaymentFileConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start MerchantPaymentFile Process");
        merchantPaymentFileConnector.startProcess(Configurations.PROCESS_ID_MERCHANT_PAYMENT_FILE_CREATION, uniqueID);
        System.out.println("Complete MerchantPaymentFile Process");
    }

    @KafkaListener(topics = "MerchantGLSummaryFile", groupId = "group_MerchantGLSummaryFile")
    public void MerchantGLSummaryFileConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start MerchantGLSummaryFile Process");
        merchantGLSummaryFileConnector.startProcess(Configurations.PROCESS_ID_MERCHANT_GL_FILE_CREATION, uniqueID);
        System.out.println("Complete MerchantGLSummaryFile Process");
    }

    @KafkaListener(topics = "merchantFee", groupId = "group_merchantFee")
    public void merchantFeeConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start MerchantFee Process");
        merchantFeeConnector.startProcess(Configurations.PROCESS_ID_MERCHANT_FEE, uniqueID);
        System.out.println("Complete MerchantFee Process");
    }

    @KafkaListener(topics = "merchantPayment", groupId = "group_merchantPayment")
    public void merchantPaymentConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start Merchant Payment Process");
        merchantPaymentConnector.startProcess(Configurations.PROCESS_ID_MERCHANT_PAYMENT_PROCESS, uniqueID);
        System.out.println("Complete Merchant Payment Process");
    }

    @KafkaListener(topics = "merchantEasyPaymentRequest", groupId = "group_merchantEasyPaymentRequest")
    public void merchantEasyPaymentRequestConsumer(String uniqueID) throws Exception {
        System.out.println("Start Merchant Easy Payment Request Process");
        merchantEasyPaymentRequestConnector.startProcess(Configurations.PROCESS_ID_EOD_MERCHANT_EASY_PAYMENT_REQUEST, uniqueID);
        System.out.println("Complete Merchant Easy Payment Request Process");
    }

    @KafkaListener(topics = "merchantCommissionCalculation", groupId = "group_merchantCommissionCalculation")
    public void merchantCommissionCalculation(String uniqueID) throws Exception {
        System.out.println("Start MerchantCommissionCalculation Process");
        commissionCalculationConnector.startProcess(Configurations.PROCESS_ID_COMMISSION_CALCULATION, uniqueID);
        System.out.println("Complete MerchantCommissionCalculation Process");
    }

    @KafkaListener(topics = "eodParameterReset", groupId = "group_eodParameterReset")
    public void eodParameterResetConsumer(String uniqueID) throws Exception {
        Configurations.eodUniqueId = uniqueID;
        System.out.println("Start EOD Parameter Reset Process");
        eodParameterResetConnector.startProcess(Configurations.PROCESS_ID_EOD_PARAMETER_RESET, uniqueID);
        System.out.println("Complete EOD Parameter Reset Process");
    }

}
