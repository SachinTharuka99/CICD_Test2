/**
 * Author : rasintha_j
 * Date : 1/31/2023
 * Time : 10:54 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.service.MerchantPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MerchantPaymentConnector extends ProcessBuilder {
    @Autowired
    MerchantPaymentService merchantPaymentService;

    @Override
    public void concreteProcess() throws Exception {
        merchantPaymentService.startMerchantPayment();
    }

    @Override
    public void addSummaries() {

    }
}
