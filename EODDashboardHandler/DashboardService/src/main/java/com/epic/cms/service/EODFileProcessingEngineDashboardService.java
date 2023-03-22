/**
 * Author : rasintha_j
 * Date : 3/18/2023
 * Time : 6:41 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.EodInputFileBean;
import com.epic.cms.model.bean.ProcessSummeryBean;
import com.epic.cms.model.bean.StatementGenSummeryBean;
import com.epic.cms.model.entity.EODATMFILE;
import com.epic.cms.model.entity.EODMASTERFILE;
import com.epic.cms.model.entity.EODPAYMENTFILE;
import com.epic.cms.model.entity.EODVISAFILE;
import com.epic.cms.repository.*;
import com.epic.cms.util.Configurations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class EODFileProcessingEngineDashboardService {
    @Autowired
    EodAtmInputFileRepo eodAtmInputFileRepo;

    @Autowired
    EodMasterInputFileRepo eodMasterInputFileRepo;

    @Autowired
    EodVisaInputFileRepo eodVisaInputFileRepo;

    @Autowired
    EodPaymentInputFileRepo eodPaymentInputFileRepo;

    @Autowired
    FileProcessingSummeryListRepo processingSummeryListRepo;


    public List<Object> getEodInputFIleList(Long eodId) {
        List<Object> eodInputFileObjectList = new ArrayList<>();

        try {
            List<EODATMFILE> atmInputFileList = eodAtmInputFileRepo.findEODATMFILEByEODID(eodId);
            List<EODPAYMENTFILE> paymentInputFileList = eodPaymentInputFileRepo.findEODPAYMENTFILEByEODID(eodId);
            List<EODMASTERFILE> masterInputFileList = eodMasterInputFileRepo.findEODMASTERFILEByEODID(eodId);
            List<EODVISAFILE> visaInputFileList = eodVisaInputFileRepo.findEODVISAFILEByEODID(eodId);


            atmInputFileList.forEach(eod -> {
                EodInputFileBean inputFileBean = new EodInputFileBean();
                inputFileBean.setUploadTime(eod.getUPLOADTIME());
                inputFileBean.setFileType("ATM");
                inputFileBean.setFileId(eod.getFILEID());
                inputFileBean.setFileName(eod.getFILENAME());
                inputFileBean.setStatus(eod.getSTATUS());

                eodInputFileObjectList.add(inputFileBean);
            });

            paymentInputFileList.forEach(eod -> {
                EodInputFileBean inputFileBean = new EodInputFileBean();
                inputFileBean.setUploadTime(eod.getUPLOADTIME());
                inputFileBean.setFileType("PAYMENT");
                inputFileBean.setFileId(eod.getFILEID());
                inputFileBean.setFileName(eod.getFILENAME());
                inputFileBean.setStatus(eod.getSTATUS());

                eodInputFileObjectList.add(inputFileBean);
            });

            masterInputFileList.forEach(eod -> {
                EodInputFileBean inputFileBean = new EodInputFileBean();
                inputFileBean.setUploadTime(eod.getUPLOADTIME());
                inputFileBean.setFileType("VISA");
                inputFileBean.setFileId(eod.getFILEID());
                inputFileBean.setFileName(eod.getFILENAME());
                inputFileBean.setStatus(eod.getSTATUS());

                eodInputFileObjectList.add(inputFileBean);
            });

            visaInputFileList.forEach(eod -> {
                EodInputFileBean inputFileBean = new EodInputFileBean();
                inputFileBean.setUploadTime(eod.getUPLOADTIME());
                inputFileBean.setFileType("MASTER");
                inputFileBean.setFileId(eod.getFILEID());
                inputFileBean.setFileName(eod.getFILENAME());
                inputFileBean.setStatus(eod.getSTATUS());

                eodInputFileObjectList.add(inputFileBean);
            });
        } catch (Exception e) {
            throw e;
        }
        return eodInputFileObjectList;
    }

    public List<StatementGenSummeryBean> getProcessingSummeryList(Long eodID) {
        List<StatementGenSummeryBean> processingSummeryBeans = new ArrayList<>();
        try {
            processingSummeryBeans = processingSummeryListRepo.findProcessingSummeryListByEodId(eodID, Configurations.EOD_FILE_PROCESSING);
        } catch (Exception e) {
            throw e;
        }
        return processingSummeryBeans;
    }
}
