package com.epic.cms.service;

import com.epic.cms.model.bean.CollectionAndRecoveryBean;
import com.epic.cms.model.bean.DelinquentAccountBean;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CollectionAndRecoveryRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.concurrent.BlockingQueue;

@Service
public class CollectionAndRecoveryService {
    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    LogManager logManager;
    @Autowired
    CollectionAndRecoveryRepo collectionAndRecoveryRepo;
    @Autowired
    StatusVarList statusList;
    @Autowired
    CommonRepo commonRepo;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processX_DATES_BEFORE_FIRST_DUE_DATE(CollectionAndRecoveryBean collectionAndRecoveryBean, ProcessBean processBean, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            try {
                boolean status = false;
                Configurations.noOfCardsForCollectionAndRecoveryNotification++; //noOfCards++;

                /**Check weather card is already exist in triggerCard table*/
                status = collectionAndRecoveryRepo.CheckForTriggerPoint(collectionAndRecoveryBean.getCardNo());

                details.put("Card number", CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()));
                details.put("Trigger task", "SMS/Email");
                details.put("Next trigger", "X_DATES_AFTER_FIRST_DUE_DATE");

                /**SMS/Email record*/
                /**Insert the card details to TriggerCards table*/
                collectionAndRecoveryBean.setLastTriger(Configurations.TP_X_DATES_BEFORE_FIRST_DUE_DATE);
                collectionAndRecoveryRepo.addCardToTriggerCards(collectionAndRecoveryBean);
                //Configurations.PROCESS_SUCCESS_COUNT++;
                successCount.add(1);
                details.put("Process Status", "Passed");
            } catch (Exception e) {
                failCount.add(1);
                //Configurations.PROCESS_FAILD_COUNT++;
                details.put("Process Status", "Failed");
                Configurations.failedCardsForCollectionAndRecoveryNotification++;
                logError.error("Collection and recovery process failed for card number " + CommonMethods.cardInfo(CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()), processBean), e);
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(collectionAndRecoveryBean.getCardNo()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                Configurations.checkErrorForCollectionAndRecoveryNotification = true;
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processX_DATES_AFTER_FIRST_DUE_DATE(CollectionAndRecoveryBean collectionAndRecoveryBean, ProcessBean processBean, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            try {
                Configurations.noOfCardsForCollectionAndRecoveryNotification++; //noOfCards++;
                details.put("Card number", CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()));
                details.put("Trigger task", "SMS/Email");
                details.put("Next trigger", "ON_THE_2ND_STATEMENT_DATE");

                /**SMS/Email record*/
                /**Insert the card details to TriggerCards table*/
                collectionAndRecoveryBean.setLastTriger(Configurations.TP_X_DATES_AFTER_FIRST_DUE_DATE);
                collectionAndRecoveryRepo.updateTriggerCards(collectionAndRecoveryBean);
                //Configurations.PROCESS_SUCCESS_COUNT++;
                successCount.add(1);
                details.put("Process Status", "Passed");

            } catch (Exception e) {
                failCount.add(1);
                //Configurations.PROCESS_FAILD_COUNT++;
                details.put("Process Status", "Failed");
                Configurations.failedCardsForCollectionAndRecoveryNotification++;
                logError.error("Collection and recovery process failed for card number " + CommonMethods.cardInfo(CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()), processBean), e);
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(collectionAndRecoveryBean.getCardNo()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                Configurations.checkErrorForCollectionAndRecoveryNotification = true;
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processON_THE_2ND_STATEMENT_DATE(CollectionAndRecoveryBean collectionAndRecoveryBean, ProcessBean processBean, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            try {
                Configurations.noOfCardsForCollectionAndRecoveryNotification++; //noOfCards++;

                details.put("Card number", CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()));
                details.put("Trigger task", "Message on the statment");
                details.put("Next trigger", "TP_ON_THE_2ND_STATEMENT_DATE");

                /**Message on the statment*/
                collectionAndRecoveryBean.setLastTriger(Configurations.TP_ON_THE_2ND_STATEMENT_DATE);
                collectionAndRecoveryRepo.updateTriggerCards(collectionAndRecoveryBean);
                //Configurations.PROCESS_SUCCESS_COUNT++;
                details.put("Process Status", "Passed");
                successCount.add(1);
            } catch (Exception e) {
                failCount.add(1);
                //Configurations.PROCESS_FAILD_COUNT++;
                details.put("Process Status", "Failed");
                Configurations.failedCardsForCollectionAndRecoveryNotification++;
                logError.error("Collection and recovery process failed for card number " + CommonMethods.cardInfo(CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()), processBean), e);
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(collectionAndRecoveryBean.getCardNo()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                Configurations.checkErrorForCollectionAndRecoveryNotification = true;
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processX_DATES_AFTER_SECOND_STATEMENT(CollectionAndRecoveryBean collectionAndRecoveryBean, ProcessBean processBean, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            try {
                Configurations.noOfCardsForCollectionAndRecoveryNotification++; //noOfCards++;

                details.put("Card number", CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()));
                details.put("Trigger task", "Call center record to the DELINQUENTACCOUNT table");
                details.put("Next trigger", "X_DATES_AFTER_SECOND_DUE_DATE");

                /**SMS/Email record*/
                collectionAndRecoveryBean.setLastTriger(Configurations.TP_X_DAYS_AFTER_THE_2ND_STATEMENT_DATE);
                collectionAndRecoveryRepo.updateTriggerCards(collectionAndRecoveryBean);
                //Configurations.PROCESS_SUCCESS_COUNT++;
                details.put("Process Status", "Passed");
                successCount.add(1);
            } catch (Exception e) {
                failCount.add(1);
                //Configurations.PROCESS_FAILD_COUNT++;
                details.put("Process Status", "Failed");
                Configurations.failedCardsForCollectionAndRecoveryNotification++;
                logError.error("Collection and recovery process failed for card number " + CommonMethods.cardInfo(CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()), processBean), e);
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(collectionAndRecoveryBean.getCardNo()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                Configurations.checkErrorForCollectionAndRecoveryNotification = true;
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processIMMEDIATELY_AFTER_THE_2ND_DUE_DATE(CollectionAndRecoveryBean collectionAndRecoveryBean, ProcessBean processBean, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            try {
                Configurations.noOfCardsForCollectionAndRecoveryNotification++; //noOfCards++;

                details.put("Card number", CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()));
                details.put("Trigger task", "send the letter");
                details.put("Next trigger", "ON_THE_3RD_STATEMENT_DATE");

                //TODO send the letter record
                DelinquentAccountBean delinquentAccountBean = new DelinquentAccountBean();
                delinquentAccountBean = commonRepo.setDelinquentAccountDetails(collectionAndRecoveryBean.getCardNo());
                /**Add to letter details table*/
                int count = collectionAndRecoveryRepo.addDetailsToCardLetterNotifyTable(collectionAndRecoveryBean.getCardNo(), delinquentAccountBean.getNameInFull(),
                        delinquentAccountBean.getAccNo(), delinquentAccountBean.getContactNo(), delinquentAccountBean.getEmail(), delinquentAccountBean.getAddress(), collectionAndRecoveryBean.getDueAmount(),
                        collectionAndRecoveryBean.getDueDate(), Configurations.TP_IMMEDIATELY_AFTER_THE_2ND_DUE_DATE);

                /**SMS/Email record*/
                collectionAndRecoveryBean.setLastTriger(Configurations.TP_IMMEDIATELY_AFTER_THE_2ND_DUE_DATE);
                collectionAndRecoveryRepo.updateTriggerCards(collectionAndRecoveryBean);
               // Configurations.PROCESS_SUCCESS_COUNT++;
                details.put("Process Status", "Passed");
                successCount.add(1);
            } catch (Exception e) {
                failCount.add(1);
                //Configurations.PROCESS_FAILD_COUNT++;
                details.put("Process Status", "Failed");
                Configurations.failedCardsForCollectionAndRecoveryNotification++;
                logError.error("Collection and recovery process failed for card number " + CommonMethods.cardInfo(CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()), processBean), e);
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(collectionAndRecoveryBean.getCardNo()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                Configurations.checkErrorForCollectionAndRecoveryNotification = true;
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processON_THE_3RD_STATEMENT_DATE(CollectionAndRecoveryBean collectionAndRecoveryBean, ProcessBean processBean, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            try {
                Configurations.noOfCardsForCollectionAndRecoveryNotification++; //noOfCards++;

                details.put("Card number", CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()));
                details.put("Trigger task", "Temporary blocking msg to the statment");
                details.put("Next trigger", "IMMEDIATELY_AFTER_THE_3RD_DUE_DATE");

                /**SMS/Email record*/
                collectionAndRecoveryBean.setLastTriger(Configurations.TP_ON_THE_3RD_STATEMENT_DATE);
                collectionAndRecoveryRepo.updateTriggerCards(collectionAndRecoveryBean);
               // Configurations.PROCESS_SUCCESS_COUNT++;
                details.put("Process Status", "Passed");
                successCount.add(1);
            } catch (Exception e) {
                failCount.add(1);
                //Configurations.PROCESS_FAILD_COUNT++;
                details.put("Process Status", "Failed");
                Configurations.failedCardsForCollectionAndRecoveryNotification++;
                logError.error("Collection and recovery process failed for card number " + CommonMethods.cardInfo(CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()), processBean), e);
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(collectionAndRecoveryBean.getCardNo()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                Configurations.checkErrorForCollectionAndRecoveryNotification = true;
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processIMMEDIATELY_AFTER_THE_3RD_DUE_DATE(CollectionAndRecoveryBean collectionAndRecoveryBean, ProcessBean processBean, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            try {
                Configurations.noOfCardsForCollectionAndRecoveryNotification++; //noOfCards++;

                details.put("Card number", CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()));
                details.put("Trigger task", "Temporary blocking msg to the statement");
                details.put("Next trigger", "ON_THE_4TH_STATEMENT_DATE");

                /**SMS/Email record*/
                /**Insert the card details to TriggerCards table*/
                collectionAndRecoveryBean.setLastTriger(Configurations.TP_IMMEDIATELY_AFTER_THE_3RD_DUE_DATE);
                collectionAndRecoveryRepo.updateTriggerCards(collectionAndRecoveryBean);
                successCount.add(1);
                //Configurations.PROCESS_FAILD_COUNT++;
                details.put("Process Status", "Passed");

            } catch (Exception e) {
                failCount.add(1);
                //Configurations.PROCESS_FAILD_COUNT++;
                details.put("Process Status", "Failed");
                Configurations.failedCardsForCollectionAndRecoveryNotification++;
                logError.error("Collection and recovery process failed for card number " + CommonMethods.cardInfo(CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()), processBean), e);
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(collectionAndRecoveryBean.getCardNo()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                Configurations.checkErrorForCollectionAndRecoveryNotification = true;
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processON_THE_4TH_STATEMENT_DATE(CollectionAndRecoveryBean collectionAndRecoveryBean, ProcessBean processBean, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            try {
                Configurations.noOfCardsForCollectionAndRecoveryNotification++; //noOfCards++;

                details.put("Card number", CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()));
                details.put("Trigger task", "Temporary blocking msg to the statment");
                details.put("Next trigger", "X_DAYS_AFTER_THE_4TH_STATEMENT_DATE");

                /**Statement msg about temporary blocked*/
                collectionAndRecoveryBean.setLastTriger(Configurations.TP_ON_THE_4TH_STATEMENT_DATE);
                collectionAndRecoveryRepo.updateTriggerCards(collectionAndRecoveryBean);
               // Configurations.PROCESS_SUCCESS_COUNT++;
                successCount.add(1);
                details.put("Process Status", "Passed");

            } catch (Exception e) {
                failCount.add(1);
                //Configurations.PROCESS_FAILD_COUNT++;
                details.put("Process Status", "Failed");
                Configurations.failedCardsForCollectionAndRecoveryNotification++;
                logError.error("Collection and recovery process failed for card number " + CommonMethods.cardInfo(CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()), processBean), e);
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(collectionAndRecoveryBean.getCardNo()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                Configurations.checkErrorForCollectionAndRecoveryNotification = true;
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processX_DAYS_AFTER_THE_4TH_STATEMENT_DATE(CollectionAndRecoveryBean collectionAndRecoveryBean, ProcessBean processBean, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            try {
                Configurations.noOfCardsForCollectionAndRecoveryNotification++; //noOfCards++;

                details.put("Card number", CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()));
                details.put("Trigger task", "letter inform about the CRIB");
                details.put("Next trigger", "WITHIN_X_DAYS_OF_THE_CRIB_INFO_LETTER_REMINDER");

                /**To DO letter inform about the CRIB*/
                DelinquentAccountBean delinquentAccountBean = new DelinquentAccountBean();
                delinquentAccountBean = commonRepo.setDelinquentAccountDetails(collectionAndRecoveryBean.getCardNo());

                /**Add to letter details table*/
                int count = collectionAndRecoveryRepo.addDetailsToCardLetterNotifyTable(collectionAndRecoveryBean.getCardNo(), delinquentAccountBean.getNameInFull(),
                        delinquentAccountBean.getAccNo(), delinquentAccountBean.getContactNo(), delinquentAccountBean.getEmail(), delinquentAccountBean.getAddress(), collectionAndRecoveryBean.getDueAmount(),
                        collectionAndRecoveryBean.getDueDate(), Configurations.TP_X_DAYS_AFTER_THE_4TH_STATEMENT_DATE);

                collectionAndRecoveryBean.setLastTriger(Configurations.TP_X_DAYS_AFTER_THE_4TH_STATEMENT_DATE);
                collectionAndRecoveryRepo.updateTriggerCards(collectionAndRecoveryBean);
               // Configurations.PROCESS_SUCCESS_COUNT++;
                successCount.add(1);
                details.put("Process Status", "Passed");

            } catch (Exception e) {
                failCount.add(1);
                //Configurations.PROCESS_FAILD_COUNT++;
                details.put("Process Status", "Failed");
                Configurations.failedCardsForCollectionAndRecoveryNotification++;
                logError.error("Collection and recovery process failed for card number " + CommonMethods.cardInfo(CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()), processBean), e);
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(collectionAndRecoveryBean.getCardNo()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                Configurations.checkErrorForCollectionAndRecoveryNotification = true;
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processWITHIN_X_DAYS_OF_THE_CRIB_INFO_LETTER_REMINDER(CollectionAndRecoveryBean collectionAndRecoveryBean, ProcessBean processBean, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            try {
                Configurations.noOfCardsForCollectionAndRecoveryNotification++; //noOfCards++;

                details.put("Card number", CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()));
                details.put("Trigger task", "Call center record");
                details.put("Next trigger", "IMMEDIATELY_AFTER_THE_4TH_DUE_DATE");

                //TODO Call center record
                collectionAndRecoveryBean.setLastTriger(Configurations.TP_WITHIN_X_DAYS_OF_THE_CRIB_INFO_LETTER_REMINDER);
                collectionAndRecoveryRepo.updateTriggerCards(collectionAndRecoveryBean);
                //Configurations.PROCESS_SUCCESS_COUNT++;
                successCount.add(1);
                details.put("Process Status", "Passed");

            } catch (Exception e) {
                failCount.add(1);
                //Configurations.PROCESS_FAILD_COUNT++;
                details.put("Process Status", "Failed");
                Configurations.failedCardsForCollectionAndRecoveryNotification++;
                logError.error("Collection and recovery process failed for card number " + CommonMethods.cardInfo(CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()), processBean), e);
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(collectionAndRecoveryBean.getCardNo()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                Configurations.checkErrorForCollectionAndRecoveryNotification = true;
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processIMMEDIATELY_AFTER_THE_4TH_DUE_DATE(CollectionAndRecoveryBean collectionAndRecoveryBean, ProcessBean processBean, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            try {
                Configurations.noOfCardsForCollectionAndRecoveryNotification++;

                details.put("Card number", CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()));
                details.put("Trigger task", "Account to non performing account");
                details.put("Account status", "Non performing account");

                /**TODO account---> NP account
                 Permenant block*/
                collectionAndRecoveryBean.setLastTriger(Configurations.TP_IMMEDIATELY_AFTER_THE_4TH_DUE_DATE);
                collectionAndRecoveryRepo.updateTriggerCards(collectionAndRecoveryBean);
                //Configurations.PROCESS_SUCCESS_COUNT++;
                successCount.add(1);
                details.put("Process Status", "Passed");

            } catch (Exception e) {
                failCount.add(1);
                //Configurations.PROCESS_FAILD_COUNT++;
                details.put("Process Status", "Failed");
                Configurations.failedCardsForCollectionAndRecoveryNotification++;
                logError.error("Collection and recovery process failed for card number " + CommonMethods.cardInfo(CommonMethods.cardNumberMask(collectionAndRecoveryBean.getCardNo()), processBean), e);
                Configurations.errorCardList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(collectionAndRecoveryBean.getCardNo()), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, CardAccount.CARD));
                Configurations.checkErrorForCollectionAndRecoveryNotification = true;
            } finally {
                logInfo.info(logManager.logDetails(details));
            }
        }
    }
}
