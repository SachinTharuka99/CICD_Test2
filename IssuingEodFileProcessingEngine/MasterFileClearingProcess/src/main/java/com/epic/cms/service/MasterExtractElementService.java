/**
 * Author :
 * Date : 2/3/2023
 * Time : 11:41 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.service;

import com.epic.cms.dao.MasterFileClearingDao;
import com.epic.cms.Exception.RejectException;
import com.epic.cms.model.bean.MasterFieldsDataBean;
import com.epic.cms.model.bean.MasterPDSBean;
import com.epic.cms.util.Configurations;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericValidatingPackager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.HashMap;

import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class MasterExtractElementService {
    public static GenericValidatingPackager ISO_MSG_PACK;
    private static MasterFieldsDataBean masterBean;

    @Autowired
    public MasterFileClearingDao masterFileClearingDao;

    /**
     * @return
     * @throws Exception
     */
    public static boolean initialize() throws Exception {
        boolean isSuccess;
        try {
            System.out.println("--initialize completed--");
            GenericValidatingPackager packager = new GenericValidatingPackager(); //create a packager
            if (Configurations.INCOMMING_IPM_FILE_ENCODING_FORMAT == 1) {
                System.out.println("--ASCII Path--" + ResourceUtils.getFile("classpath:config" + File.separator + "iso8583MasterCardASCII.xml").getAbsolutePath());
                packager.readFile(ResourceUtils.getFile("classpath:config" + File.separator + "iso8583MasterCardASCII.xml").getAbsolutePath()); // set ASCII xml configuration file to packager
            } else {
                System.out.println("--EBCIDIC Path updated--" + ResourceUtils.getFile("classpath:config" + File.separator + "iso8583MasterCardEBCIDIC.xml").getAbsolutePath());
                packager.readFile(ResourceUtils.getFile("classpath:config" + File.separator + "iso8583MasterCardEBCIDIC.xml").getAbsolutePath()); // set EBCIDIC xml configuration file to packager
            }

            ISO_MSG_PACK = packager;
            isSuccess = true;
        } catch (Exception e) {
            throw e;
        }
        return isSuccess;
    }

    /**
     * @param record
     * @param masterBean
     * @throws Exception
     */
    public void doUnpack(String record, MasterFieldsDataBean masterBean) throws Exception {
        String print = null;
        try {
            byte data[] = ISOUtil.hex2byte(record); //convert hexa string to byte array
            ISOMsg isomsg = new ISOMsg();
            isomsg.setPackager(ISO_MSG_PACK);

            //print = "Master File validating...";
            //System.out.println(print);

            isomsg.unpack(data); //unpack the fields from byte array
            setUnpackedDatatoBean(isomsg, masterBean);
            //return masterBean;
        } catch (Exception ee) {
            ee.printStackTrace();
            throw new RejectException(ee.getMessage());

        }

    }

    /**
     * @param isomsg
     * @param masterBean
     * @throws Exception
     */
    public void setUnpackedDatatoBean(ISOMsg isomsg, MasterFieldsDataBean masterBean) throws Exception {
        StringBuilder msg = new StringBuilder(); // if needed to print the transaction
        try {
            if ((isomsg.getValue(0).toString().equals(Configurations.FIRST_PRESENTMENT_MTI))) {
                for (int i = 0; i <= 128; i++) {
                    if (isomsg.hasField(i)) {
                        if (i == 55) {
                            msg.append("Element [" + i + "  ]  [ " + isomsg.getValue(i) + " ]\n");
                        } else if (i == 52) {
                            msg.append("Element [").append(i).append("  ]  [ ").append(ISOUtil.hexString((byte[]) isomsg.getValue(i))).append(" ]\n");
                        } else if (i < 10 && i >= 0) {
                            msg.append("Element [").append(i).append("   ]  [ ").append(isomsg.getValue(i).toString()).append(" ]\n");
                        } else if (i < 100 && i >= 10) {
                            msg.append("Element [").append(i).append("  ]  [ ").append(isomsg.getValue(i).toString()).append(" ]\n");
                        } else if (i < 130 && i >= 100) {
                            msg.append("Element [").append(i).append(" ]  [ ").append(isomsg.getValue(i).toString()).append(" ]\n");
                        }
                        //set value to master bean
                        switch (i) {
                            case 0:
                                masterBean.setMti(isomsg.getValue(i).toString());
                                break;
                            case 1:
                                break;
                            case 2:
                                masterBean.setPan(new StringBuffer(isomsg.getValue(i).toString()));
                                break;
                            case 3:
                                masterBean.setProcessingCode(isomsg.getValue(i).toString());
                                masterBean.setTxnTypeProcessingCode((isomsg.getValue(i).toString()).substring(0, 2));
                                break;
                            case 4:
                                masterBean.setTxnAmount(isomsg.getValue(i).toString());
                                break;
                            case 5:
                                masterBean.setReconAmount(isomsg.getValue(i).toString());
                                break;
                            case 6:
                                masterBean.setBillingAmount(isomsg.getValue(i).toString());
                                break;
                            case 7:
                                masterBean.setTransmissionTime(isomsg.getValue(i).toString());
                                break;
                            case 8:
                                masterBean.setBillingFee(isomsg.getValue(i).toString());
                                break;
                            case 9:
                                masterBean.setReconConversionRate(isomsg.getValue(i).toString());
                                break;
                            case 10:
                                masterBean.setBillingConversionRate(isomsg.getValue(i).toString());
                                break;
                            case 11:
                                masterBean.setTraceNumber(isomsg.getValue(i).toString());
                                break;
                            case 12:
                                masterBean.setLocalTransactionTime(isomsg.getValue(i).toString());
                                masterBean.setTxnDate((isomsg.getValue(i).toString()).substring(0, 6));
                                masterBean.setTxnTime((isomsg.getValue(i).toString()).substring(6, 12));
                                break;
                            case 13:
                                masterBean.setEffectiveDate(isomsg.getValue(i).toString());
                                break;
                            case 14:
                                masterBean.setExpirationDate(isomsg.getValue(i).toString());
                                break;
                            case 15:
                                masterBean.setSettlementDate(isomsg.getValue(i).toString());
                                break;
                            case 16:
                                masterBean.setConversionDate(isomsg.getValue(i).toString());
                                break;
                            case 17:
                                masterBean.setCaptureDate(isomsg.getValue(i).toString());
                                break;
                            case 18:
                                masterBean.setMerchantType(isomsg.getValue(i).toString());
                                break;
                            case 19:
                                masterBean.setAcqureCountryCode(isomsg.getValue(i).toString());
                                break;
                            case 20:
                                masterBean.setPancountryCode(isomsg.getValue(i).toString());
                                break;
                            case 21:
                                masterBean.setForwardingCountryCode(isomsg.getValue(i).toString());
                                break;
                            case 22:
                                masterBean.setPosCode(isomsg.getValue(i).toString());
                                break;
                            case 23:
                                masterBean.setCardSeqNumber(isomsg.getValue(i).toString());
                                break;
                            case 24:
                                masterBean.setFunctionCode(isomsg.getValue(i).toString());
                                break;
                            case 25:
                                masterBean.setMessageReasonCode(isomsg.getValue(i).toString());
                                break;
                            case 26:
                                masterBean.setAcceptorBusinessCode(isomsg.getValue(i).toString());
                                break;
                            case 27:
                                masterBean.setApprovalCodeLength(isomsg.getValue(i).toString());
                                break;
                            case 28:
                                masterBean.setReconDate(isomsg.getValue(i).toString());
                                break;
                            case 29:
                                masterBean.setReconIndicator(isomsg.getValue(i).toString());
                                break;
                            case 30:
                                masterBean.setOriginalAmounts(isomsg.getValue(i).toString());
                                break;
                            case 31:
                                masterBean.setAcquirerRefData(isomsg.getValue(i).toString());
                                break;
                            case 32:
                                masterBean.setAcquirerInstituteId(isomsg.getValue(i).toString());
                                break;
                            case 33:
                                masterBean.setForwadingInstituteId(isomsg.getValue(i).toString());
                                break;
                            case 34:
                                masterBean.setExtendedPan(isomsg.getValue(i).toString());
                                break;
                            case 35:
                                masterBean.setTrack2Data(isomsg.getValue(i).toString());
                                break;
                            case 36:
                                masterBean.setTrack3Data(isomsg.getValue(i).toString());
                                break;
                            case 37:
                                masterBean.setRrNumber(isomsg.getValue(i).toString());
                                break;
                            case 38:
                                masterBean.setApprovalCode(isomsg.getValue(i).toString());
                                break;
                            case 39:
                                masterBean.setActionCode(isomsg.getValue(i).toString());
                                break;
                            case 40:
                                masterBean.setServiceCode(isomsg.getValue(i).toString());
                                break;
                            case 41:
                                masterBean.setAcceptorTerminalId(isomsg.getValue(i).toString());
                                break;
                            case 42:
                                masterBean.setAcceptoerId(isomsg.getValue(i).toString());
                                break;
                            case 43:
                                masterBean.setAcceptorName(isomsg.getValue(i).toString());
                                unpackMerchantData(masterBean, isomsg.getValue(i).toString()); //In order to get merchantnName, merchantCity, merchantCountryCode.
                                break;
                            case 44:
                                masterBean.setAdditionalResponseData(isomsg.getValue(i).toString());
                                break;
                            case 45:
                                masterBean.setTrack1Data(isomsg.getValue(i).toString());
                                break;
                            case 46:
                                masterBean.setFees(isomsg.getValue(i).toString());
                                break;
                            case 47:
                                masterBean.setNationalAdditionalData(isomsg.getValue(i).toString());
                                break;
                            case 48:
                                masterBean.setPrivateAdditionalData(isomsg.getValue(i).toString());
                                unpackPDSData(masterBean, isomsg.getValue(i).toString(), i);
                                break;
                            case 49:
                                masterBean.setTxnCurrencyCode(isomsg.getValue(i).toString());
                                break;
                            case 50:
                                masterBean.setReconCurrencyCode(isomsg.getValue(i).toString());
                                break;
                            case 51:
                                masterBean.setBillingCurrencyCode(isomsg.getValue(i).toString());
                                break;
                            case 52:
                                masterBean.setPinData(isomsg.getValue(i).toString());
                                break;
                            case 53:
                                masterBean.setSecurityInformation(isomsg.getValue(i).toString());
                                break;
                            case 54:
                                masterBean.setAdditionalAmounts(isomsg.getValue(i).toString());
                                break;
                            case 55:
                                masterBean.setIccData(isomsg.getValue(i).toString());
                                break;
                            case 56:
                                masterBean.setOriginalDataElements(isomsg.getValue(i).toString());
                                break;
                            case 57:
                                masterBean.setAuthCode(isomsg.getValue(i).toString());
                                break;
                            case 58:
                                masterBean.setAuthInstituteId(isomsg.getValue(i).toString());
                                break;
                            case 59:
                                masterBean.setTransportData(isomsg.getValue(i).toString());
                                break;
                            case 60:
                                masterBean.setReservedForNational1(isomsg.getValue(i).toString());
                                break;
                            case 61:
                                masterBean.setReservedForNational2(isomsg.getValue(i).toString());
                                break;
                            case 62:
                                masterBean.setAdditionalData2(isomsg.getValue(i).toString());
                                break;
                            case 63:
                                masterBean.setTxnLifeCycleId(isomsg.getValue(i).toString());
                                break;
                            case 64:
                                masterBean.setMacField1(isomsg.getValue(i).toString());
                                break;
                            case 65:
                                masterBean.setReservedIso1(isomsg.getValue(i).toString());
                                break;
                            case 66:
                                masterBean.setOriginalFees(isomsg.getValue(i).toString());
                                break;
                            case 67:
                                masterBean.setExtendedPayment(isomsg.getValue(i).toString());
                                break;
                            case 68:
                                masterBean.setReceivingCountryCode(isomsg.getValue(i).toString());
                                break;
                            case 69:
                                masterBean.setSettlementCountryCode(isomsg.getValue(i).toString());
                                break;
                            case 70:
                                masterBean.setAuthCountryCode(isomsg.getValue(i).toString());
                                break;
                            case 71:
                                masterBean.setMessageNumber(isomsg.getValue(i).toString());
                                break;
                            case 72:
                                masterBean.setDataRecord(isomsg.getValue(i).toString());
                                break;
                            case 73:
                                masterBean.setActionDate(isomsg.getValue(i).toString());
                                break;
                            case 74:
                                masterBean.setCreditsNo(isomsg.getValue(i).toString());
                                break;
                            case 75:
                                masterBean.setCreditsReversalNo(isomsg.getValue(i).toString());
                                break;
                            case 76:
                                masterBean.setDebitsNo(isomsg.getValue(i).toString());
                                break;
                            case 77:
                                masterBean.setDebitsReversalNo(isomsg.getValue(i).toString());
                                break;
                            case 78:
                                masterBean.setTransferNo(isomsg.getValue(i).toString());
                                break;
                            case 79:
                                masterBean.setTransferReversalNo(isomsg.getValue(i).toString());
                                break;
                            case 80:
                                masterBean.setInquiriesNo(isomsg.getValue(i).toString());
                                break;
                            case 81:
                                masterBean.setAuthorizationsNo(isomsg.getValue(i).toString());
                                break;
                            case 82:
                                masterBean.setInquiriesReversalNo(isomsg.getValue(i).toString());
                                break;
                            case 83:
                                masterBean.setPaymentsNo(isomsg.getValue(i).toString());
                                break;
                            case 84:
                                masterBean.setPaymentsReversalNo(isomsg.getValue(i).toString());
                                break;
                            case 85:
                                masterBean.setFeeCollectionNo(isomsg.getValue(i).toString());
                                break;
                            case 86:
                                masterBean.setCreditsAmount(isomsg.getValue(i).toString());
                                break;
                            case 87:
                                masterBean.setCreditsReversalAmount(isomsg.getValue(i).toString());
                                break;
                            case 88:
                                masterBean.setDebitsAmount(isomsg.getValue(i).toString());
                                break;
                            case 89:
                                masterBean.setDebitsReversalAmount(isomsg.getValue(i).toString());
                                break;
                            case 90:
                                masterBean.setAuthReversalNo(isomsg.getValue(i).toString());
                                break;
                            case 91:
                                masterBean.setTxnDestinationCountryCode(isomsg.getValue(i).toString());
                                break;
                            case 92:
                                masterBean.setTxnSourceCountryCode(isomsg.getValue(i).toString());
                                break;
                            case 93:
                                masterBean.setTxnDestinationCode(isomsg.getValue(i).toString());
                                break;
                            case 94:
                                masterBean.setTxnSourceCode(isomsg.getValue(i).toString());
                                break;
                            case 95:
                                masterBean.setIssuerRefData(isomsg.getValue(i).toString());
                                break;
                            case 96:
                                masterBean.setKeyMgtData(isomsg.getValue(i).toString());
                                break;
                            case 97:
                                masterBean.setNetReconAmount(isomsg.getValue(i).toString());
                                break;
                            case 98:
                                masterBean.setPayee(isomsg.getValue(i).toString());
                                break;
                            case 99:
                                masterBean.setSettlementInstituteId(isomsg.getValue(i).toString());
                                break;
                            case 100:
                                masterBean.setReceivingInstituteId(isomsg.getValue(i).toString());
                                break;
                            case 101:
                                masterBean.setFileName(isomsg.getValue(i).toString());
                                break;
                            case 102:
                                masterBean.setAccountIdentification1(isomsg.getValue(i).toString());
                                break;
                            case 103:
                                masterBean.setAccountIdentification2(isomsg.getValue(i).toString());
                                break;
                            case 104:
                                masterBean.setTxnDescription(isomsg.getValue(i).toString());
                                break;
                            case 105:
                                masterBean.setCreditchargebackamount(isomsg.getValue(i).toString());
                                break;
                            case 106:
                                masterBean.setDebitChargebackAmount(isomsg.getValue(i).toString());
                                break;
                            case 107:
                                masterBean.setCreditChargebackNo(isomsg.getValue(i).toString());
                                break;
                            case 108:
                                masterBean.setDebitChargebackNo(isomsg.getValue(i).toString());
                                break;
                            case 109:
                                masterBean.setCreditFeeAmounts(isomsg.getValue(i).toString());
                                break;
                            case 110:
                                masterBean.setDebitFeeAmounts(isomsg.getValue(i).toString());
                                break;
                            case 111:
                                masterBean.setConversionAssessAmount(isomsg.getValue(i).toString());
                                break;
                            case 112:
                                masterBean.setReservedIso2(isomsg.getValue(i).toString());
                                break;
                            case 113:
                                masterBean.setReservedIso3(isomsg.getValue(i).toString());
                                break;
                            case 114:
                                masterBean.setReservedIso4(isomsg.getValue(i).toString());
                                break;
                            case 115:
                                masterBean.setReservedIso5(isomsg.getValue(i).toString());
                                break;
                            case 116:
                                masterBean.setReservedForNational3(isomsg.getValue(i).toString());
                                break;
                            case 117:
                                masterBean.setReservedForNational4(isomsg.getValue(i).toString());
                                break;
                            case 118:
                                masterBean.setReservedForNational5(isomsg.getValue(i).toString());
                                break;
                            case 119:
                                masterBean.setReservedForNational6(isomsg.getValue(i).toString());
                                break;
                            case 120:
                                masterBean.setReservedForNational7(isomsg.getValue(i).toString());
                                break;
                            case 121:
                                masterBean.setReservedForNational8(isomsg.getValue(i).toString());
                                break;
                            case 122:
                                masterBean.setReservedForNational9(isomsg.getValue(i).toString());
                                break;
                            case 123:
                                masterBean.setAdditionalData3(isomsg.getValue(i).toString());
                                break;
                            case 124:
                                masterBean.setAdditionalData4(isomsg.getValue(i).toString());
                                break;
                            case 125:
                                masterBean.setAdditionalData5(isomsg.getValue(i).toString());
                                break;
                            case 126:
                                masterBean.setReservedPrivate(isomsg.getValue(i).toString());
                                break;
                            case 127:
                                masterBean.setNetworkData(isomsg.getValue(i).toString());
                                break;
                            case 128:
                                masterBean.setMacField2(isomsg.getValue(i).toString());
                                break;
                            default:
                                break;
                        }

                    }

                }
            } else {
                masterBean.setMti(isomsg.getValue(0).toString());
            }
        } catch (Exception e) {
            errorLogger.error("--error--", e);
            throw e;
        }
    }

    /**
     * @param masterBean
     * @param fieldContent
     * @throws Exception
     * @author pramod_d
     */
    public static void unpackMerchantData(MasterFieldsDataBean masterBean, String fieldContent) {
        String[] masterMerchantDetails;
        masterMerchantDetails = fieldContent.split("\\\\");
        String merchantName = masterMerchantDetails[0].trim();
        String merchantCity = masterMerchantDetails[2].trim();
        String merchantCountryCode = masterMerchantDetails[3].substring(13);

        masterBean.setMerchantName(merchantName);
        masterBean.setMerchantCity(merchantCity);
        masterBean.setMerchantCountryCode(merchantCountryCode);

    }

    /**
     * @param masterBean
     * @param fieldContent
     * @param fieldId
     * @throws Exception
     */
    public void unpackPDSData(MasterFieldsDataBean masterBean, String fieldContent, int fieldId) throws Exception {

        String currencyExponentPds = "0148";
        String reversalPds = "0025";
        String reversalData = "R";

        int i = 0;
        MasterPDSBean masterPDSBean = new MasterPDSBean();
        MasterFieldsDataBean masterfieldPDSBean = new MasterFieldsDataBean();

        masterPDSBean.setTxnId(masterBean.getTxnId());
        masterPDSBean.setSessionId(masterBean.getSessionid());
        masterPDSBean.setFileId(masterBean.getFileid());
        masterPDSBean.setMti(masterBean.getMti());
        masterPDSBean.setFieldId(String.valueOf(fieldId));

        while (i < fieldContent.length()) {

            String subPDSTag = fieldContent.substring(i, (i + 4));
            i += 4;

            String subPDSLength = fieldContent.substring(i, (i + 3));
            i += 3;

            String subPDSTagValue = fieldContent.substring(i, (i + Integer.parseInt(subPDSLength)));
            i += Integer.parseInt(subPDSLength);

            masterPDSBean.setPds(subPDSTag);
            masterPDSBean.setLength(subPDSLength);
            masterPDSBean.setData(subPDSTagValue);
            masterFileClearingDao.insertMasterPDSDetailsIntoEODMASTERPDSDATA(masterPDSBean);

            // extract currency exponent list into a hashmap
            if (subPDSTag.equals(currencyExponentPds)) {
                unpackExponentData(masterBean, subPDSTagValue);
            } else if (subPDSTag.equals(reversalPds) && subPDSTag.equals(reversalData)) {

                masterfieldPDSBean.setReversalPds(subPDSTag);
                masterfieldPDSBean.setReversalData(subPDSTagValue);
            }

        }

    }

    public static void unpackExponentData(MasterFieldsDataBean masterBean, String subPDSTagValue) throws Exception {

        int i = 0;
        HashMap<String, String> exponentHashMap = new HashMap<>();
        while (i < subPDSTagValue.length()) {

            String currencyCode = subPDSTagValue.substring(i, (i + 3)); //first 3 character represent currency code
            i += 3;
            String currencyExponent = subPDSTagValue.substring(i, (i + 1));
            i += 1;

            exponentHashMap.put(currencyCode, currencyExponent);

        }
        masterBean.setCurrencyExponentList(exponentHashMap);
    }
}
