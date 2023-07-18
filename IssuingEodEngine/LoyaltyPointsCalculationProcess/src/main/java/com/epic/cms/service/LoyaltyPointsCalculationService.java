package com.epic.cms.service;

import com.epic.cms.model.bean.LoyaltyBean;
import com.epic.cms.repository.LoyaltyPointsCalculationRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.BlockingQueue;


@Service
public class LoyaltyPointsCalculationService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    StatusVarList statusList;
    @Autowired
    LogManager logManager;
    @Autowired
    LoyaltyPointsCalculationRepo loyaltyPointsCalculationRepo;

    @Async("ThreadPool_100")
    public void calculateLoyaltyPoints(LoyaltyBean loyaltyBean, BlockingQueue<Integer> successCount, BlockingQueue<Integer> failCount) {
        int noOfAccounts = 0;
        int failedAccounts = 0;
        double accumilationPointVal, redeemLoyalty, expiredLoyalty, purchases, closingLoyalty, adjustLoyalty, thisMonthClosing, availableLoyalty;
        BigDecimal earnLoyaltyPoints;
        try {
            String maskedCardNumber = CommonMethods.cardNumberMask(loyaltyBean.getCardNo());
            LinkedHashMap cardDetails = new LinkedHashMap();

            try {
                cardDetails.put("Card Number", maskedCardNumber);
                ArrayList<Integer> requestID = new ArrayList<>();

                //get last stmt closing loyalty points
                closingLoyalty = loyaltyPointsCalculationRepo.getLastStmtClosingLoyalty(loyaltyBean.getCardNo(), loyaltyBean.getStatementId());
                cardDetails.put("Opening Loyalty points", closingLoyalty);

                //Get this month purchases
                purchases = loyaltyPointsCalculationRepo.getThisMonthPurchases(loyaltyBean.getAccNo(), loyaltyBean.getStmtEndEodID(), loyaltyBean.getStmtStartEodID());
                loyaltyBean.setPurchase(purchases);
                cardDetails.put("Total Purchases", loyaltyBean.getPurchase());

                //Calculate earn loyalty points
                if (Configurations.LOYALTY_ACCUMILATION_VALUE != 0) {
                    earnLoyaltyPoints = new BigDecimal(purchases / Configurations.LOYALTY_ACCUMILATION_VALUE);
                } else {
                    earnLoyaltyPoints = new BigDecimal(0);
                }

                loyaltyBean.setEarnLoyaltyPoints(earnLoyaltyPoints.intValue());
                cardDetails.put("Earned Loyalty points", loyaltyBean.getEarnLoyaltyPoints());

                //get this month redeem loyalty
                redeemLoyalty = loyaltyPointsCalculationRepo.getThisMonthRedeem(loyaltyBean.getCardNo(), loyaltyBean.getStmtStartDate(), loyaltyBean.getStmtEndDate(), requestID);
                //Expire redeem loyalty
                if (redeemLoyalty > 0) {
                    this.expireRedeemLoyalty(redeemLoyalty, loyaltyBean.getCardNo());
                }
                loyaltyBean.setRedeemLoyaltyPoints(redeemLoyalty);
                cardDetails.put("Redeemed Loyalty points", redeemLoyalty);

                //Expire Loyalty
                expiredLoyalty = this.getToExpiredLoyalty(loyaltyBean.getCardNo(), Configurations.LOYALTY_EXPIARY_PERIOD);
                cardDetails.put("Expired Loyalty points", expiredLoyalty);

                //Get Adjust Loyalties
                adjustLoyalty = loyaltyPointsCalculationRepo.getAdjustLoyalty(loyaltyBean.getAccNo(), loyaltyBean.getStmtStartDate(), loyaltyBean.getStmtEndDate());
                loyaltyBean.setAdjustLoyaltyPoints(adjustLoyalty);
                cardDetails.put("Adjusted Loyalty points", adjustLoyalty);

                //Get this month Closing balance
                thisMonthClosing = closingLoyalty + earnLoyaltyPoints.intValue() + adjustLoyalty - redeemLoyalty - expiredLoyalty;
                loyaltyBean.setClosingLoyaltyPoints(thisMonthClosing);
                cardDetails.put("Closing Loyalty points", loyaltyBean.getClosingLoyaltyPoints());

                //Get available Loyalty
                availableLoyalty = thisMonthClosing - Configurations.LOYALTY_MINIMUM_POINT;
                availableLoyalty = availableLoyalty > 0 ? availableLoyalty : 0;

                loyaltyBean.setAvailableLoyaltyPoints(availableLoyalty);
                cardDetails.put("Available Loyalty points", availableLoyalty);

                //update the billing statement details
                loyaltyPointsCalculationRepo.updateBillingStatment(loyaltyBean);

                if (!requestID.isEmpty()) {
                    loyaltyPointsCalculationRepo.updateLoyaltyRedeemRequest(requestID, Configurations.EOD_DONE_STATUS);
                }
                cardDetails.put("Process status", "Passed");
                Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception e) {
                failedAccounts++;
                Configurations.PROCESS_FAILD_COUNT++;
                //cardErrorList.add(new ErrorCardBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, new StringBuffer(loyaltyBean.getAccNo()), e.getMessage(), configProcess, processHeader, 0, CardAccount.ACCOUNT));
                cardDetails.put("Process Status", "Failed");
                // infoLogger.info("LOYALTY_PROCESS failed for card number " + CommonMethods.cardInfo(maskedCardNumber, processBean));
                //errorLogger.error("LOYALTY_PROCESS failed for card number " + CommonMethods.cardInfo(maskedCardNumber, processBean), e);
            }
            logInfo.info(logManager.logDetails(cardDetails));
        } catch (Exception e) {
            throw e;
        }
    }

    private double getToExpiredLoyalty(StringBuffer cardNo, int loyaltyExpiaryPeriod) {
        double expiryLoyalty = 0;

        return expiryLoyalty;
    }

    private void expireRedeemLoyalty(double redeemLoyalty, StringBuffer cardNo) {

    }

}
