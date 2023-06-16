/**
 * Author :
 * Date : 2/3/2023
 * Time : 3:49 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.FileBean;
import com.epic.cms.model.bean.VisaTC56ComposingDataBean;
import com.epic.cms.model.bean.VisaTC56CurrencyEntryBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.VisaBaseIIFileClearingRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.QueryParametersList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service
public class VisaBaseIIFileClearingService {
    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    LogManager logManager;
    @Autowired
    JobLauncher jobLauncher;
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    private VisaBaseIIFileClearingRepo visaBaseIIFileClearingRepo;
    @Autowired
    private QueryParametersList queryParametersList;
    @Autowired
    @Qualifier("file_read_job")
    private Job visaFileReadJob;
    //TC 56 Currency records compose related variables
    private String fileBaseCurrency; // base currency code of currency update file
    private BigDecimal eodBaseCurrencyBuyingRate; // eg: how much USD amount needed to buy 1 LKR (USD - file base currency, LKR - EOD base currency)
    private BigDecimal eodBaseCurrencySellingRate; //eg: how much USD amount needed to sell 1 LKR (USD - file base currency, LKR - EOD base currency)

    /**
     * read file
     *
     * @param fileBean
     * @return
     * @throws Exception
     */
    public boolean readFile(FileBean fileBean) throws Exception {
        boolean fileReadStatus = false;
        System.out.println("Class Name:VisaBaseIIFileClearingService,File ID:" + fileBean.getFileId() + ",Current Thread:" + Thread.currentThread().getName());
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startAt", System.currentTimeMillis())
                    .addString("fileId", fileBean.getFileId())
                    .addString("fileName", fileBean.getFileName())
                    .addString("filePath", fileBean.getFilePath())
                    .addString("insertQuery", queryParametersList.getVisaFileClearingInsertRecInputRowData())
                    .addString("tableName", "EODVISAFILE").toJobParameters();
            JobExecution execution = jobLauncher.run(visaFileReadJob, jobParameters);
            final ExitStatus status = execution.getExitStatus();
            if (ExitStatus.COMPLETED.getExitCode().equals(status.getExitCode())) {
                fileReadStatus = true;
            } else {
                final List<Throwable> exceptions = execution
                        .getAllFailureExceptions();
                for (final Throwable throwable : exceptions) {
                    logError.error(throwable.getMessage(), throwable);
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
        return fileReadStatus;
    }

    /**
     * compose currency update records
     *
     * @param fileId
     * @throws Exception
     */
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void composeCurrencyUpdateRecords(String fileId) throws Exception {
        String txnIDToCompose, tcr = "", firstCurrencyEntry = "", secondCurrencyEntry = "", thirdCurrencyEntry = "", fourthCurrencyEntry = "", fifthCurrencyEntry = "",
                sixthCurrencyEntry = "", seventhCurrencyEntry = "", eighthCurrencyEntry = "", ninthCurrencyEntry = "", tenthcurrencyEntry = "", eleventhCurrencyEntry = "";
        List<VisaTC56CurrencyEntryBean> currencyUpdateBeanList;
        try {
            //get all transaction id list from RECVISAFIELDIDENTITY table with given file id
            ArrayList<String> txnIDList = visaBaseIIFileClearingRepo.getVisaTxnIDListForTC56(fileId);
            currencyUpdateBeanList = new ArrayList<>();
            int rcount = txnIDList.size();
            System.out.println("--rcount--" + rcount);

            for (int j = 0; j < rcount; j++) {
                // get the Transaction ID.
                txnIDToCompose = txnIDList.get(j);
                // Initiate The Visa composing bean.
                VisaTC56ComposingDataBean visaComposingDataBean = null;
                // Get data for TCR 0 and TCR 1.
                for (int t = 0; t < 2; t++) {

                    try {
                        visaComposingDataBean = visaBaseIIFileClearingRepo.getVisaComposingDataForTC56(txnIDToCompose, String.valueOf(t), fileId);

                        if (0 == t) { //TCR 0 record data
                            firstCurrencyEntry = visaComposingDataBean.getField6();
                            secondCurrencyEntry = visaComposingDataBean.getField7();
                            thirdCurrencyEntry = visaComposingDataBean.getField8();
                            fourthCurrencyEntry = visaComposingDataBean.getField9();
                            fifthCurrencyEntry = visaComposingDataBean.getField10();

                        } else { //TCR 1 record data
                            sixthCurrencyEntry = visaComposingDataBean.getField4();
                            seventhCurrencyEntry = visaComposingDataBean.getField5();
                            eighthCurrencyEntry = visaComposingDataBean.getField6();
                            ninthCurrencyEntry = visaComposingDataBean.getField7();
                            tenthcurrencyEntry = visaComposingDataBean.getField8();
                            eleventhCurrencyEntry = visaComposingDataBean.getField9();
                        }
                    } catch (Exception e) {
                        logError.error(e.getMessage());
                    }
                }
                //store all 11 currency entries contained in TCR0 and TCR 1 records
                VisaTC56CurrencyEntryBean splitCurrencyEntryBean = splitCurrencyEntry(firstCurrencyEntry);
                if (splitCurrencyEntryBean != null) {
                    currencyUpdateBeanList.add(splitCurrencyEntryBean);
                }
                splitCurrencyEntryBean = splitCurrencyEntry(secondCurrencyEntry);
                if (splitCurrencyEntryBean != null) {
                    currencyUpdateBeanList.add(splitCurrencyEntryBean);
                }
                splitCurrencyEntryBean = splitCurrencyEntry(thirdCurrencyEntry);
                if (splitCurrencyEntryBean != null) {
                    currencyUpdateBeanList.add(splitCurrencyEntryBean);
                }
                splitCurrencyEntryBean = splitCurrencyEntry(fourthCurrencyEntry);
                if (splitCurrencyEntryBean != null) {
                    currencyUpdateBeanList.add(splitCurrencyEntryBean);
                }
                splitCurrencyEntryBean = splitCurrencyEntry(fifthCurrencyEntry);
                if (splitCurrencyEntryBean != null) {
                    currencyUpdateBeanList.add(splitCurrencyEntryBean);
                }
                splitCurrencyEntryBean = splitCurrencyEntry(sixthCurrencyEntry);
                if (splitCurrencyEntryBean != null) {
                    currencyUpdateBeanList.add(splitCurrencyEntryBean);
                }
                splitCurrencyEntryBean = splitCurrencyEntry(seventhCurrencyEntry);
                if (splitCurrencyEntryBean != null) {
                    currencyUpdateBeanList.add(splitCurrencyEntryBean);
                }
                splitCurrencyEntryBean = splitCurrencyEntry(eighthCurrencyEntry);
                if (splitCurrencyEntryBean != null) {
                    currencyUpdateBeanList.add(splitCurrencyEntryBean);
                }
                splitCurrencyEntryBean = splitCurrencyEntry(ninthCurrencyEntry);
                if (splitCurrencyEntryBean != null) {
                    currencyUpdateBeanList.add(splitCurrencyEntryBean);
                }
                splitCurrencyEntryBean = splitCurrencyEntry(tenthcurrencyEntry);
                if (splitCurrencyEntryBean != null) {
                    currencyUpdateBeanList.add(splitCurrencyEntryBean);
                }
                splitCurrencyEntryBean = splitCurrencyEntry(eleventhCurrencyEntry);
                if (splitCurrencyEntryBean != null) {
                    currencyUpdateBeanList.add(splitCurrencyEntryBean);
                }
            }
            // Insert the composed data into recvisatransaction table.
            visaBaseIIFileClearingRepo.insertVisaTC56ComposedData(currencyUpdateBeanList, fileBaseCurrency, eodBaseCurrencyBuyingRate, eodBaseCurrencySellingRate);
            // Update record status to composed
            visaBaseIIFileClearingRepo.updateTC56RecordsAsComposed(fileId);
        } catch (Exception ex) {
            throw ex;
        }
    }

    private VisaTC56CurrencyEntryBean splitCurrencyEntry(String currencyEntry) {
        try {
            if (currencyEntry == null && currencyEntry.isEmpty()) {
                return null;
            } else {
                String actionCode = currencyEntry.substring(0, 1);
                if (actionCode.equals("A")) {
                    VisaTC56CurrencyEntryBean currencyEntryBean = new VisaTC56CurrencyEntryBean();
                    currencyEntryBean.setActionCode("A");
                    currencyEntryBean.setCounterCurrencyCode(currencyEntry.substring(1, 4));
                    currencyEntryBean.setBaseCurrencyCode(currencyEntry.substring(4, 7));
                    currencyEntryBean.setEffectiveDate(currencyEntry.substring(7, 11));
                    currencyEntryBean.setBuyScaleFactor(Integer.parseInt(currencyEntry.substring(11, 13)));
                    currencyEntryBean.setBuyRate(getRateFromScaleFactor(currencyEntry.substring(13, 19), currencyEntryBean.getBuyScaleFactor()));
                    currencyEntryBean.setSellScaleFactor(Integer.parseInt(currencyEntry.substring(19, 21)));
                    currencyEntryBean.setSellRate(getRateFromScaleFactor(currencyEntry.substring(21, 27), currencyEntryBean.getSellScaleFactor()));

                    if (currencyEntryBean.getCounterCurrencyCode().equals(Configurations.BASE_CURRENCY)) {
                        fileBaseCurrency = currencyEntry.substring(4, 7);
                        eodBaseCurrencyBuyingRate = currencyEntryBean.getBuyRate();
                        eodBaseCurrencySellingRate = currencyEntryBean.getSellRate();
                    }
                    return currencyEntryBean;
                } else {
                    return null;
                }
            }
        } catch (Exception ex) {
            logError.error(ex.getMessage());
            return null;
        }
    }

    private BigDecimal getRateFromScaleFactor(String rateInStr, int scaleFactor) {
        return new BigDecimal(rateInStr).scaleByPowerOfTen(-scaleFactor);
    }

}
