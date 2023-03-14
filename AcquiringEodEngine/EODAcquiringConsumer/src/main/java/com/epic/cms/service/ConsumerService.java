package com.epic.cms.service;

import com.epic.cms.connector.AcqTxnUpdateConnector;
import com.epic.cms.connector.MerchantEasyPaymentRequestConnector;
import com.epic.cms.connector.MerchantCommissionCalculationConnector;
import com.epic.cms.connector.MerchantFeeConnector;
import com.epic.cms.connector.*;
import com.epic.cms.connector.MerchantPaymentConnector;
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

    @KafkaListener(topics = "acqTxnUpdate", groupId = "group_acqTxnUpdate")
    public void acqTxnUpdateConsumer(String msg) throws Exception {
        System.out.println("Start AcqTxnUpdate Process");
        //acqTxnUpdateConnector.startProcess();
        System.out.println("Complete AcqTxnUpdate Process");
    }

    @KafkaListener(topics = "PreMerchantFee", groupId = "group_PreMerchantFee")
    public void PreMerchantFeeConsumer(String msg) throws Exception {
        System.out.println("Start PreMerchantFeeProcess");
        //preMerchantFeeConnector.startProcess();
        System.out.println("Complete PreMerchantFee Process");
    }

    @KafkaListener(topics = "AcquiringAdjustment", groupId = "group_AcquiringAdjustment")
    public void AcquiringAdjustmentConsumer(String msg) throws Exception {
        System.out.println("Start AcquiringAdjustment Process");
        //acquiringAdjustmentConnector.startProcess();
        System.out.println("Complete AcquiringAdjustment Process");
    }

    @KafkaListener(topics = "MerchantPaymentFile", groupId = "group_MerchantPaymentFile")
    public void MerchantPaymentFileConsumer(String msg) throws Exception {
        System.out.println("Start MerchantPaymentFile Process");
        //merchantPaymentFileConnector.startProcess();
        System.out.println("Complete MerchantPaymentFile Process");
    }

    @KafkaListener(topics = "MerchantGLSummaryFile", groupId = "group_MerchantGLSummaryFile")
    public void MerchantGLSummaryFileConsumer(String msg) throws Exception {
        System.out.println("Start MerchantGLSummaryFile Process");
        //merchantGLSummaryFileConnector.startProcess();
        System.out.println("Complete MerchantGLSummaryFile Process");
    }

    @KafkaListener(topics = "merchantFee", groupId = "group_merchantFee")
    public void merchantFeeConsumer(String msg) throws Exception {
        System.out.println("Start MerchantFee Process");
        //merchantFeeConnector.startProcess();
        System.out.println("Complete MerchantFee Process");
    }

    @KafkaListener(topics = "merchantPayment", groupId = "group_merchantPayment")
    public void merchantPaymentConsumer(String msg) throws Exception {
        System.out.println("Start Merchant Payment Process");
        //merchantPaymentConnector.startProcess();
        System.out.println("Complete Merchant Payment Process");
    }

    @KafkaListener(topics = "merchantEasyPaymentRequest", groupId = "group_merchantEasyPaymentRequest")
    public void merchantEasyPaymentRequestConsumer(String msg) throws Exception {
        System.out.println("Start Merchant Easy Payment Request Process");
        //merchantEasyPaymentRequestConnector.startProcess();
        System.out.println("Complete Merchant Easy Payment Request Process");
    }

    @KafkaListener(topics = "merchantCommissionCalculation", groupId = "group_merchantCommissionCalculation")
    public void merchantCommissionCalculation(String msg) throws Exception {
        System.out.println("Start MerchantCommissionCalculation Process");
        //commissionCalculationConnector.startProcess();
        System.out.println("Complete MerchantCommissionCalculation Process");
    }

}
