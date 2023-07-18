/**
 * Author : lahiru_p
 * Date : 7/11/2023
 * Time : 2:55 PM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.service;

import com.epic.cms.Exception.RejectException;
import com.epic.cms.model.bean.IP0040T1Bean;
import com.epic.cms.model.bean.MPGSAdditionalDataBean;
import com.epic.cms.model.bean.TransactionDataBean;
import com.epic.cms.repository.OutgoingIPMFileGenRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import org.jpos.iso.ISOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class MasterFieldValidator {

    public static TransactionDataBean transactionBean;
    List<String> dataElementValueList;
    static String decidedMTI;
    static String networkCode;
    static int transactionCount = 1; //transaction record message number will start from 2 since header record have message number 1
    static boolean isMPGSTxn;
    static MPGSAdditionalDataBean mpgsAdditionalData;

    @Autowired
    StatusVarList statusVarList;

    @Autowired
    OutgoingIPMFileGenRepo outgoingIPMFileGenRepo;

    public int addTxnToMasterOutgoingFieldIdentityTable(TransactionDataBean txnBean) throws Exception {
        this.transactionBean = txnBean;
        dataElementValueList = new ArrayList<>();
        mpgsAdditionalData = new MPGSAdditionalDataBean();

        //check transaction type is MPGS ipg transaction
        if (txnBean.getListenerType() != null && Integer.toString(statusVarList.getLISTENER_TYPE_IPG()).equals(txnBean.getListenerType())) {
            isMPGSTxn = true;
            mpgsAdditionalData = outgoingIPMFileGenRepo.getMPGSAdditionalData(transactionBean.getTxnId());
        } else {
            isMPGSTxn = false;
        }
        transactionCount++;
        for (int i = 0; i <= 128; i++) {
            //dynamically find the method to prepare the value
            Class<?> cls = Class.forName("com.epic.cms.service.MasterFieldValidator");
            //Object ins = cls.newInstance();
            Constructor<?> constructor = cls.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object ins = constructor.newInstance();

            Object parameterObj[] = {};
            Method method = cls.getDeclaredMethod("getDE" + i, new Class[]{}); // this will find the method name dynamically. eg: "getDE"+"10" --> getDE10() with no arguments
            String returnValue = null;
            try {
                returnValue = (String) method.invoke(ins, parameterObj); //will throw null pointer exception when casting for null values
            } catch (InvocationTargetException e) {
                throw e;
            } catch (Exception ex) {
                ex.printStackTrace(); //TODO:tempory
            }
            dataElementValueList.add(returnValue);

        }
        // retriev all PDS values list
        Map<String, String> pdsMap = new LinkedHashMap<>();
        getAllPDS(pdsMap);
        if (transactionBean.getMC_F63_DATA() != null && !transactionBean.getMC_F63_DATA().isEmpty()) {
            networkCode = transactionBean.getMC_F63_DATA().substring(0, 3);
        } else {
            //refund adjustment may not contain MC_F63_DATA value as it initiate from WEB
            networkCode = outgoingIPMFileGenRepo.getGCMSProductIDFromCardNumber(ISOUtil.padright(transactionBean.getCardno().toString(), 19, '0'));
        }
        IP0040T1Bean issuerAccountRangeDetails = outgoingIPMFileGenRepo.getIssuerAccountRangeDetails(ISOUtil.padright(transactionBean.getCardno().toString(), 19, '0'), networkCode);
        if (issuerAccountRangeDetails == null) { //try again without online network code
            issuerAccountRangeDetails = outgoingIPMFileGenRepo.getIssuerAccountRangeDetailsWithoutCardProgram(ISOUtil.padright(transactionBean.getCardno().toString(), 19, '0'));
        }
        //insert record to outgoingfieldidentity table
        int result = outgoingIPMFileGenRepo.insertToEODMasterOutgoingFieldIdentity(decidedMTI, transactionBean.getTxnId(),
                dataElementValueList, pdsMap, issuerAccountRangeDetails.getCardProgramIdentifier(), issuerAccountRangeDetails.getGCMSProductID(), issuerAccountRangeDetails.getCountryCodeAlpha(), issuerAccountRangeDetails.getRegion(), networkCode);
        return result;
    }

    private void getAllPDS(Map<String, String> pdsMap) {
        // PDS 0023—Terminal Type ***************************************************************************
        //PDS 0023 is required in non-reversal First Presentment/1240
        String pds23Value = "NA ";
        //TODO:need better logic to decide Terminal Type as follows
        /*
        ATM ATM terminal
        CT1 CAT level 1 (automated dispensing machine)
        CT2 CAT level 2 (self-service terminal)
        CT3 CAT level 3 (limited amount terminal)
        CT4 CAT level 4 (in-flight commerce terminal)
        CT6 CAT level 6 (electronic commerce transaction)
        CT7 CAT level 7 (transponders)
        CT8 Reserved for Future Use
        CT9 MPOS Acceptance Device
        MAN Manual, no terminal
        NA Terminal type data unknown or not available (The third position of this value is a
        space.)
        POI POI terminal
         */
        if (isMPGSTxn) {
            if (mpgsAdditionalData.getCardHolderActivatedTerminalLevelInd() != null && !mpgsAdditionalData.getCardHolderActivatedTerminalLevelInd().isEmpty()) {
                switch (mpgsAdditionalData.getCardHolderActivatedTerminalLevelInd()) {
                    case "0":
                        pds23Value = "NA ";
                        break;
                    case "1":
                        pds23Value = "CT1";
                        break;
                    case "2":
                        pds23Value = "CT2";
                        break;
                    case "3":
                        pds23Value = "CT3";
                        break;
                    case "4":
                        pds23Value = "CT4";
                        break;
                    case "5":
                        pds23Value = "NA ";
                        break;
                    case "6":
                        pds23Value = "CT6";
                        break;
                    case "7":
                        pds23Value = "CT7";
                        break;
                    case "8":
                        pds23Value = "CT8";
                        break;
                    case "9":
                        pds23Value = "CT9";
                        break;
                    default:
                        pds23Value = "NA ";
                        break;

                }
            }
        } else if (!transactionBean.getBackendTxnType().equalsIgnoreCase(Configurations.TXN_TYPE_REVERSAL)) {
            if (transactionBean.getBackendTxnType().equalsIgnoreCase(Configurations.TXN_TYPE_CASH_ADVANCE)) {
                pds23Value = "CT1";
            } else if (transactionBean.getBackendTxnType().equalsIgnoreCase(Configurations.TXN_TYPE_SALE)) {
                pds23Value = "POI";
            } else {
                pds23Value = "NA ";
            }
        }
        pdsMap.put("23", pds23Value);

        //PDS 0025 Message Reversal Indicator
        if (transactionBean.getBackendTxnType().equalsIgnoreCase(Configurations.TXN_TYPE_REVERSAL)) {
            try {
                String pds25Value = "R".concat(new SimpleDateFormat("yyMMdd").format(new SimpleDateFormat("yyyy-MM-dd").parse(transactionBean.getTransactionDate().substring(0, 10))));
                pdsMap.put("25", pds25Value);
            } catch (Exception ex) {
                System.out.println("Cannot parse transaction date for reversal transaction: " + transactionBean.getTxnId() + ex.getMessage());
            }
        }

        //PDS 0043—Transaction Type Identifier
        if (mpgsAdditionalData.getTransactionTypeIndicator() != null && !mpgsAdditionalData.getTransactionTypeIndicator().trim().isEmpty()) {
            String pds43Value = mpgsAdditionalData.getTransactionTypeIndicator();
            pdsMap.put("43", pds43Value);
        }

        //PDS 0052—Electronic Commerce Security Level Indicator
        // for ecommerce transactions
        if (isMPGSTxn && mpgsAdditionalData.getECI() != null) {
            String pds52Value = mpgsAdditionalData.getECI();
            pdsMap.put("52", pds52Value);
        }

        //PDS 0056—Mastercard Electronic Card Indicator
        if (isMPGSTxn && mpgsAdditionalData.getElectronicAcceptanceIndicator() != null && !mpgsAdditionalData.getElectronicAcceptanceIndicator().trim().isEmpty()) {
            String pds56Value = mpgsAdditionalData.getElectronicAcceptanceIndicator();
            pdsMap.put("56", pds56Value);
        }

        // PDS 0148—Currency Exponents ***************************************************************************
        String pds148Value = transactionBean.getTxnCurrency().concat(Configurations.CURRENCY_EXPONENT_TABLE.get(transactionBean.getTxnCurrency()));
        pdsMap.put("148", pds148Value);

        // PDS 0158—Business Activity ***************************************************************************
        String pds158Value = "   "// Subfield 1—Card Program Identifier
                .concat(" ") //Subfield 2—Business Service Arrangement Type Code
                .concat("      ") //Subfield 3—Business Service ID Code
                .concat("**"); // Subfield 4—Interchange Rate Designator (IRD)
        //TODO: Subfield 4—Interchange Rate Designator(IRD)  need to decide based on Interchange Manual Customer.pdf doc
        pdsMap.put("158", pds158Value);

        // PDS 0165 Settlement Indicator
        String pds165Value = "M";
        pdsMap.put("165", pds165Value);

        // PDS 0184—Directory Server Transaction ID
        if (isMPGSTxn && mpgsAdditionalData.getDirectoryServerTxnId() != null && !mpgsAdditionalData.getDirectoryServerTxnId().trim().isEmpty()) {
            String pds184Value = mpgsAdditionalData.getDirectoryServerTxnId();
            pdsMap.put("184", pds184Value);
        }

        //PDS 0186—Program Protocol
        if (isMPGSTxn && mpgsAdditionalData.getProgramProtocol() != null && !mpgsAdditionalData.getProgramProtocol().trim().isEmpty()) {
            String pds186Value = mpgsAdditionalData.getProgramProtocol();
            pdsMap.put("186", pds186Value);
        }

        // PDS 0207—Wallet Identifier
        if (isMPGSTxn && mpgsAdditionalData.getWalletProgramData() != null && !mpgsAdditionalData.getWalletProgramData().trim().isEmpty()) {
            String pds207Value = mpgsAdditionalData.getWalletProgramData();
            pdsMap.put("207", pds207Value);
        }

        //PDS 0777—Promotion Code
        if (isMPGSTxn && mpgsAdditionalData.getPromotionCode() != null && !mpgsAdditionalData.getPromotionCode().trim().isEmpty()) {
            String pds777Value = mpgsAdditionalData.getPromotionCode();
            pdsMap.put("777", pds777Value);
        }
    }

    public String getDE0() throws RejectException {
        try {
            return "1240";
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE0"));
        }
    }//MESSAGE TYPE INDICATOR

    public String getDE1() throws RejectException {
        try {
            return null; // bitmap auto generated by using jpos library later. no need to create it here
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE1"));
        }
    }//BIT MAP

    public String getDE2() throws RejectException {
        try {
            return transactionBean.getCardno().toString();
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE2"));
        }
    }//PAN - PRIMARY ACCOUNT NUMBER

    public String getDE3() throws RejectException {
        try {
            //subfield 1 (Cardholder Transaction Type) *******************************
            String s1 = "00"; //Purchase (Goods and Services)
            if (transactionBean.getBackendTxnType().equalsIgnoreCase(Configurations.TXN_TYPE_REFUND)) {
                s1 = "20"; //Credit (Purchase Return)
            } else if (transactionBean.getBackendTxnType().equalsIgnoreCase(Configurations.TXN_TYPE_CASH_ADVANCE)) {
                s1 = "12"; //Cash Disbursement
            } else if (transactionBean.getBackendTxnType().equalsIgnoreCase(Configurations.TXN_TYPE_WITHDRAWAL)) {
                s1 = "01"; //ATM Cash Withdrawal
            } //TODO: add other transaction type codes if needed in future

            //subfield 2 (Cardholder “From” Account Type Code)*******************************
            String s2 = "00"; //default account
            //TODO: how to detect other account types. need to add logic
            //10 Savings Account ,20 Checking Account, 30 Credit Account, 39 Commercial Credit Account, 60 Stored Value Account

            //subfield 3 (Cardholder “To” Account Type Code) *******************************
            String s3 = "00";
            return s1.concat(s2).concat(s3);
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE3"));
        }
    }//PROCESSING CODE

    public String getDE4() throws RejectException {
        try {
            //actual amount in the acquirer’s local currency excluding any fees.
            //DE 49 (Currency Code, Transaction), PDS 0148 (Currency Exponents) need to fill
            //PDS 0146 (Amounts, Transaction Fee) may populate if transaction fee available

            DecimalFormat df = new DecimalFormat("#.00");
            String result = df.format(new Double(transactionBean.getTxnAmount()));
            result = result.replace(".", "");
            return ISOUtil.zeropad(result, 12);
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE4"));
        }
    }//AMOUNT, TRANSACTION

    public String getDE5() throws RejectException {
        try {
            //TODO: how to determine issuer's reconciliation currency (THIS IS A MANDATORY FIELD)
            //DE 5 is DE 4 converted to the issuer’s reconciliation currency for the transaction.
            //The clearing system may use an adjusted conversion rate
            /*DecimalFormat df = new DecimalFormat("#.00");
            String result = df.format(new Double(transactionBean.getSettlementAmount()));
            result = result.replace(".", "");
            return ISOUtil.zeropad(result, 12);*/
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE5"));
        }
    }//AMOUNT, RECONCILIATION

    public String getDE6() throws RejectException {
        try {
            //TODO: how to determine issuer's designated cardholder billing currency (THIS IS A OPTIONAL FIELD)
            //DE 4 (Amount, Transaction) converted to the issuer’s
            //designated cardholder billing currency.
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE6"));
        }
    }//AMOUNT, CARDHOLDER BILLING

    public String getDE7() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE7"));
        }
    }//TRANSMISSION DATE AND TIME

    public String getDE8() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE8"));
        }
    }//AMOUNT, CARDHOLDER BILLING FEE

    public String getDE9() throws RejectException {
        try {
            // not required. clearing system insert the value
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE9"));
        }
    }//CONVERSION RATE,RECONCILIATION

    public String getDE10() throws RejectException {
        try {
            // not required. clearing system insert the value
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE10"));
        }
    }//CONVERSION RATE, CARDHOLDER BILLING

    public String getDE11() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE11"));
        }
    }//SYSTEM TRACE AUDIT NUMBER

    public String getDE12() throws RejectException {
        try {
            //Subfield 1—Date*****************************
            String s1 = "";
            if (transactionBean.getTxnDate() != null || !transactionBean.getTxnDate().isEmpty()) {
                s1 = transactionBean.getOnlineCreatedTime().substring(2, 4).concat(transactionBean.getTxnDate()); //YY+ assuming online txndate in MMDD format
            } else {
                throw new RejectException(getRejectMessege("DE12"));
            }
            //Subfield 2—Time******************
            //TODO: if time available need to append here
            String s2 = "000000";
            return s1.concat(s2);
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE12"));
        }
    }//DATE AND TIME, LOCAL TRANSACTION

    public String getDE13() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE13"));
        }
    }//DATE, EFFECTIVE

    public String getDE14() throws RejectException {
        try {
            //optional field. if available need to read from db
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE14"));
        }
    }//DATE, EXPIRATION

    public String getDE15() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE15"));
        }
    }//DATE, SETTLEMENT

    public String getDE16() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE16"));
        }
    }//DATE, CONVERSION

    public String getDE17() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE17"));
        }
    }//DATE, CAPTURE

    public String getDE18() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE18"));
        }
    }//MERCHANTS TYPE

    public String getDE19() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE19"));
        }
    }//ACQUIRING INSTITUTION COUNTRY CODE

    public String getDE20() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE20"));
        }
    }//PAN EXTENDED COUNTRY CODE

    public String getDE21() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE21"));
        }
    }//FORWARDING INSTITUTION COUNTRY CODE

    public String getDE22() throws RejectException {
        try {
            //Subfield 1—Terminal Data: Card Data Input Capability *****************************
            //TODO: for following all scenarios need to map. only few in current switch case
            /*
            0 Input capability unknown or unspecified.
            1 No POS terminal used (e.g. voice/ARU authorization or other transactions originated
            from a data server).
            2 Terminal supports magnetic stripe input only.
            4 Optical character reader (OCR) capability
            5 Terminal supports EMV contact chip input only.
            6 Terminal supports key entry input only.
            A Contactless Magnetic Stripe (Proximity Chip)
            B Terminal supports magnetic stripe input, and key entry input.
            C Terminal supports EMV contact chip input, magnetic stripe input, and key entry input.
            D Terminal supports EMV contact chip input and magnetic stripe input.
            E EMV contact chip and key entry capability.
            M Contactless EMV/Chip (Proximity Chip)
            V Other capability
             */
            String s1 = "0";
            if (isMPGSTxn && mpgsAdditionalData.getPointOfServiceDataCode() != null) {
                s1 = mpgsAdditionalData.getPointOfServiceDataCode().substring(0, 1);
            } else if (transactionBean.getTerminalCapability() != null && !transactionBean.getTerminalCapability().equals("")) {
                switch (transactionBean.getTerminalCapability()) {
                    case "2":
                        s1 = "2"; // Magnetic stripe read capability
                        break;
                    case "3":
                        s1 = "M"; //Chip-capable terminal
                        break;
                    case "4":
                        s1 = "A"; //Proximity-read-capable terminal ,   TODO: need to veryfy
                        break;
                    default:
                        s1 = "0";
                        break;
                }
            } else {
                s1 = "0";
            }
            //Subfield 2—Terminal Data: Cardholder Authentication Capability**************
            /*
            0 Terminal does not have PIN entry capability
            1 Terminal has PIN entry capability
            2 Electronic signature analysis capability
            3 mPOS software-based PIN entry capability
            5 Terminal has PIN entry capability but PIN pad is not currently operative
            6 Other
            9 Unknown; data unavailable
             */
            String s2 = "9";
            if (isMPGSTxn && mpgsAdditionalData.getPointOfServiceDataCode() != null) {
                s2 = mpgsAdditionalData.getPointOfServiceDataCode().substring(1, 2);
            } else if (transactionBean.getPosEntryMode() != null && !transactionBean.getPosEntryMode().equals("")) {
                String de22s01 = transactionBean.getPosEntryMode().substring(0, 2);
                String de22s02 = transactionBean.getPosEntryMode().substring(2, 3);
                if (de22s02.equals("0")) {
                    s2 = "9";
                } else if (de22s02.equals("1")) {
                    s2 = "1";
                } else if (de22s02.equals("2")) {
                    s2 = "0";
                } else if (de22s02.equals("8")) {
                    s2 = "5";
                } else if (de22s01.equals("04")) {
                    s2 = "6";
                }
            } else {
                s2 = "9";
            }

            //Subfield 3—Terminal Data: Card Capture Capability***************
            /*
            0 Terminal/operator does not have card capture capability
            1 Terminal/operator has card capture capability
            9 Unknown; data unavailable
             */
            String s3 = "1";
            if (isMPGSTxn && mpgsAdditionalData.getPointOfServiceDataCode() != null) {
                s3 = mpgsAdditionalData.getPointOfServiceDataCode().substring(2, 3);
            }

            //Subfield 4—Terminal Operating Environment*************
            /*
            0 No terminal used
            1 On card acceptor premises; attended terminal
            2 On card acceptor premises; unattended terminal
            3 Off card acceptor premises; attended
            4 Off card acceptor premises; unattended
            5 On cardholder premises; unattended
            6 Off cardholder premises; unattended
            7 Private use (Future use)
            8 Additional Terminal Operating Environments
            9 Unknown; data unavailable
             */
            String s4 = "3";
            if (isMPGSTxn && mpgsAdditionalData.getPointOfServiceDataCode() != null) {
                s4 = mpgsAdditionalData.getPointOfServiceDataCode().substring(3, 4);
            }

            //Subfield 5—Cardholder Present Data **********************
            /*
            0 Cardholder present
            1 Cardholder not present (unspecified)
            2 Cardholder not present (mail/facsimile transaction)
            3 Cardholder not present (phone order or from automated response unit [ARU])
            4 Cardholder not present (standing order/recurring transactions)
            5 Cardholder not present (electronic order [PC, Internet, mobile phone, or PDA])
            9 Unknown; data unavailable
             */
            String s5 = "0";
            if (isMPGSTxn && mpgsAdditionalData.getPointOfServiceDataCode() != null) {
                s5 = mpgsAdditionalData.getPointOfServiceDataCode().substring(4, 5);
            }

            //Subfield 6—Card Present Data **********************
            /*
            0 Card not present*
            1 Card present*
            9 Unknown; data unavailable
             */
            String s6 = "1";
            if (isMPGSTxn && mpgsAdditionalData.getPointOfServiceDataCode() != null) {
                s6 = mpgsAdditionalData.getPointOfServiceDataCode().substring(5, 6);
            }

            //Subfield 7—Card Data: Input Mode **********************
            String s7 = "0";

            /*
            0 PAN entry mode unknown
            1 PAN manual entry; no terminal used
            2 PAN auto-entry via magnetic strip: track data is not required within transaction
            6 PAN manual entry using a terminal, or through voice transaction after chip card read
            error or chip fallback transaction failure
            7 Credential on File (effective June 12, 2018)
            A PAN auto-entry via contactless magnetic stripe: track data provided unaltered within
            transaction
            B PAN auto-entry via magnetic stripe: track data provided unaltered within transaction
            (magnetic stripe entry may also be chip fallback transaction).
            C PAN auto-entry via chip (online authorized transaction)
            F PAN auto-entry via chip (offline chip-approved transaction)
            M PAN auto-entry via contactless M/Chip
            R PAN entry via electronic commerce containing Digital Secure Remote Payment (DSRP)
            cryptogram within DE 55 (Integrated Circuit Card [ICC])
            S PAN entry via electronic commerce
            T PAN auto-entry via server (issuer, acquirer, or third party vendor system)
             */
            if (isMPGSTxn && mpgsAdditionalData.getPointOfServiceDataCode() != null) {
                s7 = mpgsAdditionalData.getPointOfServiceDataCode().substring(6, 7);
            } else if (transactionBean.getPosEntryMode() != null && !transactionBean.getPosEntryMode().equals("")) {
                String de22s01 = transactionBean.getPosEntryMode().substring(0, 2);
                if (de22s01.equals("00")) {
                    s7 = "0";
                } else if (de22s01.equals("01")) {
                    s7 = "1";
                } else if (de22s01.equals("02")) {
                    s7 = "B"; // online module pos entry mode 02 replace with 90 when send to mastercard. but not in database. EOD need to consider always 02 as 90
                } else if (de22s01.equals("03") || de22s01.equals("04")) {
                    s7 = "0";
                } else if (de22s01.equals("05")) {
                    s7 = "C";
                } else if (de22s01.equals("07")) {
                    s7 = "M";
                } else if (de22s01.equals("09")) {
                    s7 = "R";
                } else if (de22s01.equals("10")) {
                    s7 = "7";
                } else if (de22s01.equals("79")) {
                    s7 = "6";
                } else if (de22s01.equals("80") || de22s01.equals("90")) {
                    s7 = "B";
                } else if (de22s01.equals("81")) {
                    s7 = "S";
                } else if (de22s01.equals("82")) {
                    s7 = "T";
                } else if (de22s01.equals("91")) {
                    s7 = "B";
                } else if (de22s01.equals("95")) {
                    s7 = "C";
                }
            } else {
                s7 = "0";
            }

            //Subfield 8—Cardholder Authentication Method **********************
            /*
            0 Not authenticated
            1 PIN
            2 Electronic signature analysis
            5 Manual signature verification
            6 Other manual verification (such as a driver’s license number)
            9 Unknown; data unavailable
            S Other systematic verification
             */
            String s8 = "9";
            if (isMPGSTxn && mpgsAdditionalData.getPointOfServiceDataCode() != null) {
                s8 = mpgsAdditionalData.getPointOfServiceDataCode().substring(7, 8);
            }

            //Subfield 9—Cardholder Authentication Entity **********************
            String s9 = "9";
            //TODO: how to determine following values
            /*
            0 Not authenticated
            1 ICC—Offline PIN
            2 Card acceptance device (CAD)
            3 Authorizing agent—Online PIN
            4 Merchant/card acceptor—signature
            5 Other
            9 Unknown; data unavailable
             */

            //Subfield 10—Card Data Output Capability **********************
            String s10 = "0";
            //TODO: how to determine following values
            /*
            0 Unknown; data unavailable
            1 None
            2 Write data to card at magnetic stripe level
            3 Write data to card at ICC chip level
            S Other
             */

            //Subfield 11—Terminal Data Output Capability **********************
            String s11 = "0";
            //TODO: how to determine following values
            /*
            0 Unknown; data unavailable
            1 None
            2 Printing capability only
            3 Display capability only
            4 Printing and display capability
             */

            //Subfield 12—PIN Capture Capability **********************
            /*
            0 No PIN capture capability
            1 Unknown; data unavailable
            2 Reserved
            3 Reserved
            4 PIN capture capability 4 characters maximum
            5 PIN capture capability 5 characters maximum
            6 PIN capture capability 6 characters maximum
            7 PIN capture capability 7 characters maximum
            8 PIN capture capability 8 characters maximum
            9 PIN capture capability 9 characters maximum
            A PIN capture capability 10 characters maximum
            B PIN capture capability 11 characters maximum
            C PIN capture capability 12 characters maximum
             */
            String s12 = "1";
            if (isMPGSTxn && mpgsAdditionalData.getPointOfServiceDataCode() != null) {
                s12 = mpgsAdditionalData.getPointOfServiceDataCode().substring(8, 9);
            } else if (transactionBean.getPosEntryMode() != null && !transactionBean.getPosEntryMode().equals("")) {
                String de22s02 = transactionBean.getPosEntryMode().substring(2, 3);
                if (de22s02.equals("0")) {
                    s12 = "1";
                } else if (de22s02.equals("2")) {
                    s12 = "0";
                }
            } else {
                s12 = "1";
            }

            return s1.concat(s2).concat(s3).concat(s4).concat(s5).concat(s6).concat(s7).concat(s8).concat(s9).concat(s10).concat(s11).concat(s12);

        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE22"));
        }
    }//POINT OF SERVICE DATA CODE

    public String getDE23() throws RejectException {
        try {
            //optional field
            if (isMPGSTxn && mpgsAdditionalData.getPanSequenceNumber() != null && !mpgsAdditionalData.getPanSequenceNumber().trim().isEmpty()) {
                return mpgsAdditionalData.getPanSequenceNumber();
            } else {
                return null;
            }
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE23"));
        }
    }//CARD SEQUENCE NUMBER

    public String getDE24() throws RejectException {
        try {
            return "200"; //first presentment
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE24"));
        }
    }//FUNCTION CODE

    public String getDE25() throws RejectException {
        try {
            //optional field
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE25"));
        }
    }//MESSAGE REASON CODE

    public String getDE26() throws RejectException {
        try {
            return transactionBean.getMcc();
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE26"));
        }
    }//CARD ACCEPTOR BUSINESS CODE - MCC

    public String getDE27() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE27"));
        }
    }//APPROVAL CODE LENGTH

    public String getDE28() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE28"));
        }
    }//DATE, RECONCILIATION

    public String getDE29() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE29"));
        }
    }//RECONCILIATION INDICATOR

    public String getDE30() throws RejectException {
        try {
            //optional field
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE30"));
        }
    }//AMOUNT, ORIGINAL

    public String getDE31() throws RejectException {
        try {
            StringBuilder ARN = new StringBuilder();

            String randomNumber = Integer.toString(getRandomInteger(1, 9)); //any numeric value

            ARN.append(randomNumber);
            // acquirer bin
            ARN.append(Configurations.MASTER_ACQ_BIN);
            // date
            ARN.append(getJulianDateString(Configurations.EOD_DATE)); //Julian Processing Date YDDD
            //film locator
            long sequenceNumber = ++Configurations.MASTER_OUT_SEQUENCE_NUMBER;
            ARN.append(ISOUtil.zeropad(Long.toString(sequenceNumber), 11));
            //check digit
            return ARN.toString() + calModulus10CheckDigit(ARN.toString());
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE31"));
        }
    }//ACQUIRER REFERENCE DATA

    public String getDE32() throws RejectException {
        try {
            //optional field
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE32"));
        }
    }//ACQUIRING INSTITUTION IDENT CODE

    public String getDE33() throws RejectException {
        try {
            //TODO: make changes in config.prop for correct value when go live
            return Configurations.MASTERCARD_FORWARDING_INSTITUTION_ID_CODE;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE33"));
        }
    }//FORWARDING INSTITUTION IDENT CODE

    public String getDE34() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE34"));
        }
    }//PAN EXTENDED

    public String getDE35() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE35"));
        }
    }//TRACK 2 DATA

    public String getDE36() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE36"));
        }
    }//TRACK 3 DATA

    public String getDE37() throws RejectException {
        try {
            return transactionBean.getRrn();
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE37"));
        }
    }//RETRIEVAL REFERENCE NUMBER

    public String getDE38() throws RejectException {
        try {
            return transactionBean.getAuthCode();
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE38"));
        }
    }//APPROVAL CODE

    public String getDE39() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE39"));
        }
    }//ACTION CODE

    public String getDE40() throws RejectException {
        try {
            return transactionBean.getServiceCode();
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE40"));
        }
    }//SERVICE CODE

    public String getDE41() throws RejectException {
        try {
            return transactionBean.getTid();
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE41"));
        }
    }//CARD ACCEPTOR TERMINAL ID

    public String getDE42() throws RejectException {
        try {
            return transactionBean.getMid();
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE42"));
        }
    }//CARD ACCEPTOR IDENTIFICATION CODE

    public String getDE43() throws RejectException {
        try {
            //Subfield 1—Card Acceptor Name **********************
            String s1 = "";
            if (transactionBean.getMerchantName() != null && !transactionBean.getMerchantName().isEmpty()) {
                if (transactionBean.getMerchantName().length() > 22) {
                    s1 = transactionBean.getMerchantName().substring(0, 22);
                } else {
                    s1 = transactionBean.getMerchantName();
                }
            } else {
                throw new RejectException(getRejectMessege("DE43"));
            }
            //Subfield 2—Card Acceptor Street Address
            //TODO: need a way to find street address of merchant (atleast 98% of transactions need the value by mastercard and validated)
            String s2 = "STREET_ADDRESS";
            if (transactionBean.getMerchantCity() != null && !transactionBean.getMerchantCity().isEmpty()) {
                if (transactionBean.getMerchantCity().length() > 48) {
                    s2 = transactionBean.getMerchantCity().substring(0, 48);
                }
            }

            //Subfield 3—Card Acceptor City
            String s3 = "CITY";
            if (transactionBean.getMerchantCity() != null && !transactionBean.getMerchantCity().isEmpty()) {
                if (transactionBean.getMerchantCity().length() > 13) {
                    s3 = transactionBean.getMerchantCity().substring(0, 13);
                }
            }

            //Subfield 4—Card Acceptor Postal (ZIP) Code
            String s4 = "ZIP CODE  ";
            if (transactionBean.getMerchantZipCode() != null && !transactionBean.getMerchantZipCode().isEmpty()) {
                s4 = ISOUtil.strpad(transactionBean.getMerchantZipCode(), 10);
            }

            //Subfield 5—Card Acceptor State, Province, or Region Code
            String s5 = "   ";

            // Subfield 6—Card Acceptor Country Code
            String s6 = "LKA";
            //TODO: need to update correct 'country alpha-3 code list' in country table. eg:- ISO 3166 ALPHA-2 is 'LK', ISO 3166-1 ALPHA-3 code is 'LKA'
            if (transactionBean.getMerchantCountryCode() != null && !transactionBean.getMerchantCountryCode().isEmpty()) {
                s6 = transactionBean.getMerchantCountryCode();
            } else {
                throw new RejectException(getRejectMessege("VF0015"));
            }
            return s1.concat("\\").concat(s2).concat("\\").concat(s3).concat("\\").concat(s4).concat(s5).concat(s6);
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE43"));
        }
    }//CARD ACCEPTOR NAME/LOCATION

    public String getDE44() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE44"));
        }
    }//ADITIONAL RESPONSE DATA

    public String getDE45() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE45"));
        }
    }//TRACK 1 DATA

    public String getDE46() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE46"));
        }
    }//AMOUNT,FEES

    public String getDE47() throws RejectException {
        try {
            // not used in mastercard
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE47"));
        }
    }//ADITIONAL DATA - NATIONAL

    public String getDE48() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE48"));
        }
    }//ADITIONAL DATA - PRIVATE

    public String getDE49() throws RejectException {
        try {
            return transactionBean.getTxnCurrency();
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE49"));
        }
    }//CURRENCY CODE, TRANSACTION

    public String getDE50() throws RejectException {
        try {
            //return transactionBean.getSettlementCurrency();
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE50"));
        }
    }//CURRENCY CODE, RECONCILIATION

    public String getDE51() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE51"));
        }
    }//CURRENCY CODE, CARDHOLDER BILLING

    public String getDE52() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE52"));
        }
    }//PIN DATA

    public String getDE53() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE53"));
        }
    }//SECURITY RELATED CONTROL INFO

    public String getDE54() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE54"));
        }
    }//ADDITIONAL AMOUNTS

    public String getDE55() throws RejectException {
        try {
            String de22s01 = null;
            if (transactionBean.getPosEntryMode() != null) {
                de22s01 = transactionBean.getPosEntryMode().substring(0, 2);
            }

            StringBuilder sb = new StringBuilder();
            if (de22s01 != null && (de22s01.equals("05") || de22s01.equals("07"))) {
                //9F26
                if (transactionBean.getEMV_9F26() != null) { //6FD2C380AE5B3800
                    sb.append("9F26").append(ISOUtil.zeropad(Integer.toHexString(transactionBean.getEMV_9F26().length() / 2), 2)).append(transactionBean.getEMV_9F26());
                }
                //9F27
                if (transactionBean.getEMV_9F27() != null) {
                    sb.append("9F27").append(ISOUtil.zeropad(Integer.toHexString(transactionBean.getEMV_9F27().length() / 2), 2)).append(transactionBean.getEMV_9F27());
                }

                //9F10
                if (transactionBean.getEMV_9F10() != null) { //0110A04001220000000000000000000000FF
                    sb.append("9F10").append(ISOUtil.zeropad(Integer.toHexString(transactionBean.getEMV_9F10().length() / 2), 2)).append(transactionBean.getEMV_9F10());
                }

                //9F37
                if (transactionBean.getEMV_9F37() != null) { //AA0B04B8
                    sb.append("9F37").append(ISOUtil.zeropad(Integer.toHexString(transactionBean.getEMV_9F37().length() / 2), 2)).append(transactionBean.getEMV_9F37());
                }
                //9F36
                if (transactionBean.getEMV_9F36() != null) { //0135
                    sb.append("9F36").append(ISOUtil.zeropad(Integer.toHexString(transactionBean.getEMV_9F36().length() / 2), 2)).append(transactionBean.getEMV_9F36());
                }
                //95
                if (transactionBean.getEMV_95() != null) { //0020008000
                    sb.append("95").append(ISOUtil.zeropad(Integer.toHexString(transactionBean.getEMV_95().length() / 2), 2)).append(transactionBean.getEMV_95());
                }
                //9A
                if (transactionBean.getEMV_9A() != null) { //2)10914
                    sb.append("9A").append(ISOUtil.zeropad(Integer.toHexString(transactionBean.getEMV_9A().length() / 2), 2)).append(transactionBean.getEMV_9A());
                }
                //9C
                if (transactionBean.getEMV_9A() != null) { //00
                    sb.append("9C").append(ISOUtil.zeropad(Integer.toHexString(transactionBean.getEMV_9C().length() / 2), 2)).append(transactionBean.getEMV_9C());
                }
                //9F02
                if (transactionBean.getEMV_9F02() != null) { //000000066850
                    sb.append("9F02").append(ISOUtil.zeropad(Integer.toHexString(transactionBean.getEMV_9F02().length() / 2), 2)).append(transactionBean.getEMV_9F02());
                }
                //5F2A
                if (transactionBean.getEMV_5F2A() != null) { //0144
                    sb.append("5F2A").append(ISOUtil.zeropad(Integer.toHexString(transactionBean.getEMV_5F2A().length() / 2), 2)).append(transactionBean.getEMV_5F2A());
                }
                //82
                if (transactionBean.getEMV_82() != null) { //1800
                    sb.append("82").append(ISOUtil.zeropad(Integer.toHexString(transactionBean.getEMV_82().length() / 2), 2)).append(transactionBean.getEMV_82());
                }
                //9F1A
                if (transactionBean.getEMV_9F1A() != null) { //0144
                    sb.append("9F1A").append(ISOUtil.zeropad(Integer.toHexString(transactionBean.getEMV_9F1A().length() / 2), 2)).append(transactionBean.getEMV_9F1A());
                }
                //9F03
                if (transactionBean.getEMV_9F03() != null) { //000000000000
                    sb.append("9F03").append(ISOUtil.zeropad(Integer.toHexString(transactionBean.getEMV_9F03().length() / 2), 2)).append(transactionBean.getEMV_9F03());
                }
                //9F34
                if (transactionBean.getEMV_9F34() != null) { //
                    sb.append("9F34").append(ISOUtil.zeropad(Integer.toHexString(transactionBean.getEMV_9F34().length() / 2), 2)).append(transactionBean.getEMV_9F34());
                }
                //9F33
                if (transactionBean.getEMV_9F33() != null) { //E00808
                    sb.append("9F33").append(ISOUtil.zeropad(Integer.toHexString(transactionBean.getEMV_9F33().length() / 2), 2)).append(transactionBean.getEMV_9F33());
                }
                //84
                if (transactionBean.getEMV_84() != null) { //
                    sb.append("84").append(ISOUtil.zeropad(Integer.toHexString(transactionBean.getEMV_84().length() / 2), 2)).append(transactionBean.getEMV_84());
                }

                return sb.toString();

            } else {
                return null;
            }

        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE55"));
        }
    }//ICC SYSTEM RELATED DATA

    public String getDE56() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE56"));
        }
    }//ORIGINAL DATA ELEMENT

    public String getDE57() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE57"));
        }
    }//AUTHORIZATION LIFE CYCLE

    public String getDE58() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE58"));
        }
    }//AUTHORIZING AGENT INSTITUTE ID

    public String getDE59() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE59"));
        }
    }//TRANSPORT DATA

    public String getDE60() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE60"));
        }
    }//RESERVED NATIONAL

    public String getDE61() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE61"));
        }
    }//RESERVED NATIONAL

    public String getDE62() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE62"));
        }
    }//ADDITIONAL DATA 2

    public String getDE63() throws RejectException {
        //TODO: MPGS transaction 6020 field 34 network reference number(9). but other fields not available
        // MPGS transaction 6021 field 08 Financial Network Code(3)
        try {

            if (isMPGSTxn) {
                if(transactionBean.getBackendTxnType().equalsIgnoreCase(Configurations.TXN_TYPE_REFUND)){
                    return null;
                }else if (mpgsAdditionalData.getBanknetNetworkCode() != null && !mpgsAdditionalData.getBanknetNetworkCode().isEmpty()
                        && mpgsAdditionalData.getBanknetReferenceNumber() != null && !mpgsAdditionalData.getBanknetReferenceNumber().isEmpty()
                        && mpgsAdditionalData.getBanknetDate() != null && !mpgsAdditionalData.getBanknetDate().isEmpty()) {
                    //trace id = Network Reference Number(9) + Network Date(4)+ two spaces
                    return " ".concat(ISOUtil.padright(mpgsAdditionalData.getBanknetNetworkCode().concat(mpgsAdditionalData.getBanknetReferenceNumber()).concat(mpgsAdditionalData.getBanknetDate()), 15, ' '));

                } else {
                    return null;
                }

            } else if (transactionBean.getMC_F63_DATA() != null && !transactionBean.getMC_F63_DATA().isEmpty() && transactionBean.getF15_SETTLE_DATE() != null && !transactionBean.getF15_SETTLE_DATE().isEmpty()) {
                //trace id = Network Reference Number(9) + Network Date(4)+ two spaces
                return " ".concat(ISOUtil.padright(transactionBean.getMC_F63_DATA().concat(transactionBean.getF15_SETTLE_DATE()), 15, ' '));

            } else {
                return null;
            }
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE63"));
        }
    }//TRANSACTION LIFE CYCLE ID

    public String getDE64() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE64"));
        }
    }//MESSAGE AUTHENTICATION CODE FIELD

    public String getDE65() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE65"));
        }
    }//RESERVED - ISO

    public String getDE66() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE66"));
        }
    }//AMOUNT, ORIGINAL FEES

    public String getDE67() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE67"));
        }
    }//EXTENDED PAYMENT DATA

    public String getDE68() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE68"));
        }
    }//RECEIVING INSTITUTION COUNTRY CODE

    public String getDE69() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE69"));
        }
    }//SETTLEMENT INSTITUTION COUNTRY CODE

    public String getDE70() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE70"));
        }
    }//AGENT INSTITUTION COUNTRY CODE

    public String getDE71() throws RejectException {
        try {
            return Integer.toString(transactionCount);
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE71"));
        }
    }//MESSAGE NUMBER

    public String getDE72() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE72"));
        }
    }//DATA RECORD

    public String getDE73() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE73"));
        }
    }//DATE ACTION

    public String getDE74() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE74"));
        }
    }//CREDITS NUMBER

    public String getDE75() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE75"));
        }
    }//CREDITS REVERSAL NUMBER

    public String getDE76() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE76"));
        }
    }//DEBITS NUMBER

    public String getDE77() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE77"));
        }
    }//DEBITS REVERSAL NUMBER

    public String getDE78() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE78"));
        }
    }//TRANSFER NUMBER

    public String getDE79() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE79"));
        }
    }//TRANSFER REVERSAL NUMBER

    public String getDE80() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE80"));
        }
    }//INQUIRIES NUMBER

    public String getDE81() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE81"));
        }
    }//AUTHORIZATION NUMBER

    public String getDE82() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE82"));
        }
    }//INQUIRIES, REVERSAL NUMBER

    public String getDE83() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE83"));
        }
    }//PAYMENTS, NUMBER

    public String getDE84() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE84"));
        }
    }//PAYMENTS, REVERSAL NUMBER

    public String getDE85() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE85"));
        }
    }//FEE COLLECTIONS, NUMBER

    public String getDE86() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE86"));
        }
    }//CREDITS, AMOUNT

    public String getDE87() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE87"));
        }
    }//CREDITS, REVERSAL AMOUNT

    public String getDE88() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE88"));
        }
    }//DEBITS, AMOUNT

    public String getDE89() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE89"));
        }
    }//DEBITS, REVERSAL AMOUNT

    public String getDE90() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE90"));
        }
    }//AUTHORIZATION, REVERSAL NUMBER

    public String getDE91() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE91"));
        }
    }//COUNTRY CODE,TDI

    public String getDE92() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE92"));
        }
    }//COUNTRY CODE,TOI

    public String getDE93() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE93"));
        }
    }//TRANSACTION DESTINATION INSTITUTION ID

    public String getDE94() throws RejectException {
        try {
            return Configurations.TRANSACTION_ORIGINATOR_INSTITUTION_ID_CODE;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE94"));
        }
    }//TRANSACTION ORIGINATOR INSTITUTION ID

    public String getDE95() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE95"));
        }
    }//CARD ISSUER REFERENCE DATA

    public String getDE96() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE96"));
        }
    }//KEY MANAGEMENT DATA

    public String getDE97() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE97"));
        }
    }//AMOUNT, NET RECONCILIATION

    public String getDE98() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE98"));
        }
    }//PAYEE

    public String getDE99() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE99"));
        }
    }//SETTLEMENT INSTITUTION IDENT CODE

    public String getDE100() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE100"));
        }
    }//RECEIVING INSTITUTION IDENT CODE

    public String getDE101() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE101"));
        }
    }//FILE NAME

    public String getDE102() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE102"));
        }
    }//ACCOUNT IDENTIFICATION 1

    public String getDE103() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE103"));
        }
    }//ACCOUNT IDENTIFICATION 2

    public String getDE104() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE104"));
        }
    }//TRANSACTION DESCRIPTION

    public String getDE105() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE105"));
        }
    }//CREDITS, CHARGEBACK AMOUNT

    public String getDE106() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE106"));
        }
    }//DEBITS, CHARGEBACK AMOUNT

    public String getDE107() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE107"));
        }
    }//CREDITS, CHARGEBACK NUMBER

    public String getDE108() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE108"));
        }
    }//DEBITS, CHARGEBACK NUMBER

    public String getDE109() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE109"));
        }
    }//CREDITS, FEE AMOUNTS

    public String getDE110() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE110"));
        }
    }//DEBITS, FEE AMOUNTS

    public String getDE111() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE111"));
        }
    }//AMOUNT, CURRENCY CONVERSION ASSESSMENT

    public String getDE112() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE112"));
        }
    }//RESERVED ISO USE

    public String getDE113() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE113"));
        }
    }//RESERVED ISO USE

    public String getDE114() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE114"));
        }
    }//RESERVED ISO USE

    public String getDE115() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE115"));
        }
    }//RESERVED ISO USE

    public String getDE116() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE116"));
        }
    }//RESERVED NATIONAL USE

    public String getDE117() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE117"));
        }
    }//RESERVED NATIONAL USE

    public String getDE118() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE118"));
        }
    }//RESERVED NATIONAL USE

    public String getDE119() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE119"));
        }
    }//RESERVED NATIONAL USE

    public String getDE120() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE120"));
        }
    }//RESERVED NATIONAL USE

    public String getDE121() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE121"));
        }
    }//RESERVED NATIONAL USE

    public String getDE122() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE122"));
        }
    }//RESERVED NATIONAL USE

    public String getDE123() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE123"));
        }
    }//ADDITIONAL DATA 3

    public String getDE124() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE124"));
        }
    }//ADDITIONAL DATA 4

    public String getDE125() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE125"));
        }
    }//ADDITIONAL DATA 5

    public String getDE126() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE126"));
        }
    }//CONVERSION DATA

    public String getDE127() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE127"));
        }
    }//NETWORK DATA

    public String getDE128() throws RejectException {
        try {
            return null;
        } catch (Exception ex) {
            throw new RejectException(getRejectMessege("DE128"));
        }
    }//MAC

    private String getRejectMessege(String fieldCode) {
        //TODO: need to implement
        return null;
    }

    public int calModulus10CheckDigit(String numberString) {
        /* STEPS:
           1) multiply odd postions by 2 and even positions by 1 (starting from right & position count from 1)
           2) if multiplied result greater than 9 then subtract 9 from result
           3) add all results and get modulus of 10
           4) if modulus 10 result is not zero then subtract modulus result from 10
           5) result is the check digit
         */

        char[] charArray = numberString.toCharArray();
        int[] number = new int[charArray.length];
        int total = 0;
        int answer = 0;
        for (int i = 0; i < charArray.length; i++) { //convert character array to number array
            number[i] = Character.getNumericValue(charArray[i]);
        }

        // 1) multiply odd postions by 2 and even positions by 1 (starting from right & position count from 1)
        for (int i = number.length - 1; i >= 0; i -= 2) {
            number[i] *= 2;
            // 2) if multiplied result greater than 9 then subtract 9 from result
            if (number[i] > 9) {
                number[i] -= 9;
            }
        }

        // 3) add all results and get modulus of 10
        for (int i = 0; i < number.length; i++) {
            total += number[i];
        }
        // 4) if modulus 10 result is not zero then subtract modulus result from 10
        if (total % 10 != 0) {
            answer = 10 - (total % 10);
        }
        return answer;
    }

    public String getJulianDateString(Date inputDate) {
        SimpleDateFormat odf = new SimpleDateFormat("yyDDD");
        String outputDate = odf.format(inputDate);
        outputDate = outputDate.substring(outputDate.length() - 4); //get date in yDDD
        return outputDate;
    }

    /* * returns random integer between minimum and maximum range */
    public int getRandomInteger(int maximum, int minimum) {
        return ((int) (Math.random() * (maximum - minimum))) + minimum;
    }
}
