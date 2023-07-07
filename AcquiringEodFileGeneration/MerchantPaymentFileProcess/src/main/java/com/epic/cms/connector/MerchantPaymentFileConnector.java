/**
 * Author : sharuka_j
 * Date : 2/2/2023
 * Time : 9:33 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.FileGenProcessBuilder;
import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.dao.MerchantPaymentFileDao;
import com.epic.cms.model.bean.*;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.MerchantPaymentFileService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 1- Select list of merchant Customers & Locations according to their payment
 * file status on corresponding next payment date.
 * 2- Get Cumulative value of
 * the transactions where paymentmaintenance status = 0 in eodmerchantpayment
 * according to the list.
 * 3- If cumulative value is CR(credit) it should be
 * inserted to the payment file either it don't insert.
 * 4- finally update the
 * paymentmaintenance status = 1 in eodmerchantpayment for the CR records only.
 * DR resulted records will not be updated.
 * 5- then update next payment date on
 * merchant location and customer by perspective billing cycles.
 *
 * @author shehan_m
 * @date Mar 28, 2018
 */

@Service
public class MerchantPaymentFileConnector extends FileGenProcessBuilder {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    ArrayList<String> merchantCustomerList = new ArrayList<>();
    ArrayList<String> merchantLocationList = new ArrayList<>();

    String sequence = null;
    int seq = 0;
    HashMap<String, String> currencyList = new HashMap<String, String>();
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    MerchantPaymentFileService merchantPaymentFileService;
    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;
    @Autowired
    MerchantPaymentFileDao merchantPaymentFileDao;
    @Autowired
    LogManager logManager;

    private HashMap<String, HashMap<Integer, HashMap<String, ArrayList<MerchantPaymentCycleBean>>>> totalMerchantListOnPaymod;

    @Override
    public void concreteProcess() throws Exception {
        try {
            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_MERCHANT_PAYMENT_FILE_CREATION);
            if (processBean != null) {
                Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_MERCHANT_PAYMENT_FILE_CREATION;
                CommonMethods.eodDashboardProgressParametersReset();

                try {
                    //getCurrencyList
                    currencyList = merchantPaymentFileDao.getCurrencyList();

                    //Select merchantcustomers/merchants who eligible for payment file
                    totalMerchantListOnPaymod = merchantPaymentFileDao.getMerchantsForPayment();

                    if (!totalMerchantListOnPaymod.isEmpty() && totalMerchantListOnPaymod.size() > 0) {

                        SimpleDateFormat sd = new SimpleDateFormat("yyyy");
                        String year = sd.format(new Date());
                        String today1 = String.valueOf(year) + Integer.toString(Configurations.EOD_ID).substring(2, 6);

                        String eodSeq = Integer.toString(Configurations.ERROR_EOD_ID).substring(6);
                        seq = Integer.parseInt(eodSeq) + 1;
                        sequence = String.format("%03d", seq);

                        //first file name - direct payment
                        String fileNameF1 = Configurations.MERCHANT_PAYMENT_FILE_DIRECT_PREFIX_F1 + today1 + "." + sequence;
                        //second file name - direct payment
                        String fileNameF2 = Configurations.MERCHANT_PAYMENT_FILE_DIRECT_PREFIX_F2 + today1 + "." + sequence;

                        Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = totalMerchantListOnPaymod.size();

                        for (Map.Entry<String, HashMap<Integer, HashMap<String, ArrayList<MerchantPaymentCycleBean>>>> entrySet : totalMerchantListOnPaymod.entrySet()) {
                            merchantPaymentFileService.paymentFile(entrySet,fileNameF1,fileNameF2);

                        }


                        while (!(taskExecutor.getActiveCount() == 0)) {
                            Thread.sleep(1000);
                        }

                        Set<String> set1 = new HashSet<>(merchantCustomerList);
                        merchantCustomerList.clear();
                        merchantCustomerList.addAll(set1);

                        Set<String> set2 = new HashSet<>(merchantLocationList);
                        merchantLocationList.clear();
                        merchantLocationList.addAll(set2);

                        merchantPaymentFileDao.updateMerchantLocationNextPaymentDate(merchantLocationList);//update nextpayment date on merchant location according to their billing cycle
                        merchantPaymentFileDao.updateMerchantCustomerNextPaymentDate(merchantCustomerList);//update nextpayment date on merchant customer according to their billing cycle

                    }
                } catch (Exception e) {
                    logError.error("Exception in Merchant Payment File Generation ", e);
                }
            }
        } catch (Exception e) {
            try {
                logError.error("Exception in Merchant Payment File Generation ", e);
                if (processBean.getCriticalStatus() == 1) {
                    Configurations.COMMIT_STATUS = false;
                    Configurations.FLOW_STEP_COMPLETE_STATUS = false;
                    Configurations.PROCESS_FLOW_STEP_COMPLETE_STATUS = false;
                    Configurations.MAIN_EOD_STATUS = false;
                }
            } catch (Exception e2) {
                logError.error("Exception in Merchant Payment File Generation ", e);
            }
        }

    }

    @Override
    public void addSummaries() {
        summery.put("Process Name", "Merchant Payment File");
        summery.put("Started Date ", Configurations.EOD_DATE.toString());
        summery.put("Total No of Effected File ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
        summery.put("File Success Count ", Configurations.PROCESS_SUCCESS_COUNT);
        summery.put("File Failed Count ", Configurations.PROCESS_FAILD_COUNT);
    }


}


