package com.epic.cms.service;

import com.epic.cms.model.bean.LetterGenerationParameterValuesBean;
import com.epic.cms.model.bean.LetterGenerationReferanceTableDetailsBean;
import com.epic.cms.repository.LetterRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class LetterService {

    @Autowired
    LetterRepo letterRepo;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    public String[] genaration(String tempID, String appID, StringBuffer cardNumber, String cardProduct, String sequenceNo) throws Exception {
        String RefID = "";
        String path = "";
        String[] fileNameAndPath = new String[2];

        try {

            LetterGenerationReferanceTableDetailsBean tbleBean = new LetterGenerationReferanceTableDetailsBean();
            LetterGenerationParameterValuesBean valueBean = new LetterGenerationParameterValuesBean();
            HashMap<String, String> parameterValues = new HashMap<String, String>();

            String curDateLong = new SimpleDateFormat("dd-MMM-yy").format(new Date()).toUpperCase();//07-OCT-16
            String curDateShort = new SimpleDateFormat("yyMM").format(new Date());

            ArrayList<String> parameterList = letterRepo.getParametersInLetterTemplate(tempID, cardProduct);
            if (!appID.equals("0")) {
                tbleBean = letterRepo.getLetterFieldDetails(letterRepo.getCardTypebyApplicationID(appID), tempID);
                RefID = tempID + "_" + curDateShort + "_" + appID.substring(appID.length() - 4) + "_" + CommonMethods.validate(sequenceNo, 5, '0');
            } else if (!cardNumber.equals("0")) {
                tbleBean = letterRepo.getLetterFieldDetails(letterRepo.getCardTypebyCardNumber(cardNumber), tempID);
                RefID = tempID + "_" + curDateShort + "_" + cardNumber.substring(cardNumber.length() - 4, cardNumber.length()) + "_" + CommonMethods.validate(sequenceNo, 5, '0');
            }

//            craeting path
//            if (Configurations.SERVER_RUN_PLATFORM.equals("WINDOWS")) {
//                path = Configurations.EOD_LETTER_FILE_PATH+tempID+File.separator+curDateLong+File.separator;
//
//                File file = new File(path);
//                if (!file.exists()) {
//                    if (file.mkdirs()) {
//                        System.out.println("Directory is created!");
//                    } else {
//                        System.out.println("Failed to create directory!");
//                    }
//                }
//            }else{

            path = Configurations.EOD_LETTER_FILE_PATH + tempID + File.separator + curDateLong + File.separator;

            File file = new File(path);
            if (!file.exists()) {
                if (file.mkdirs()) {
                    System.out.println("Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
            }
//        }

            path = path + RefID + ".pdf";

            fileNameAndPath[0] = path;
            fileNameAndPath[1] = RefID + ".pdf";

            if (tempID.equals(Configurations.APPLICATION_REJECTION_LETTER_CODE)) {

                for (int i = 0; i < parameterList.size(); i++) {

                    if (parameterList.get(i).equals("NAME")) {
                        parameterValues.put("NAME", letterRepo.getParameterValueForAppLetters(tbleBean.getNAME(), appID));
                    } else if (parameterList.get(i).equals("ADDRESS1")) {
                        parameterValues.put("ADDRESS1", letterRepo.getParameterValueForAppLetters(tbleBean.getADDRESS1(), appID));
                    } else if (parameterList.get(i).equals("ADDRESS2")) {
                        parameterValues.put("ADDRESS2", letterRepo.getParameterValueForAppLetters(tbleBean.getADDRESS2(), appID));
                    } else if (parameterList.get(i).equals("ADDRESS3")) {
                        parameterValues.put("ADDRESS3", letterRepo.getParameterValueForAppLetters(tbleBean.getADDRESS3(), appID));
                    } else if (parameterList.get(i).equals("CARDNO")) {
                        parameterValues.put("CARDNO", CommonMethods.cardNumberMask(new StringBuffer(letterRepo.getParameterValueForAppLetters(tbleBean.getCARDNO(), appID))));
                    } else if (parameterList.get(i).equals("CREDITLIMIT")) {
                        parameterValues.put("CREDITLIMIT", letterRepo.getParameterValueForAppLetters(tbleBean.getCREDITLIMIT(), appID));
                    } else if (parameterList.get(i).equals("TITLE")) {
                        parameterValues.put("TITLE", letterRepo.getParameterValueForAppLetters(tbleBean.getTITLE(), appID));
                    } else if (parameterList.get(i).equals("BODY")) {
                        parameterValues.put("BODY", letterRepo.getParameterValueForAppLetters(tbleBean.getBODY(), appID));
                    } else if (parameterList.get(i).equals("TEXT")) {
                        parameterValues.put("TEXT", letterRepo.getParameterValueForAppLetters(tbleBean.getTEXT(), appID));
                    } else if (parameterList.get(i).equals("BRANCH")) {
                        parameterValues.put("BRANCH", letterRepo.getParameterValueForAppLetters(tbleBean.getBRANCH(), appID));
                    } else if (parameterList.get(i).equals("AMOUNT")) {
                        parameterValues.put("AMOUNT", letterRepo.getParameterValueForAppLetters(tbleBean.getAMOUNT(), appID));
                    } else if (parameterList.get(i).equals("TODAY")) {
                        Date date = new Date();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                        parameterValues.put("TODAY", dateFormat.format(date));
                    } else if (parameterList.get(i).equals("LETTERREF")) {
                        parameterValues.put("LETTERREF", RefID);

                    } else if (parameterList.get(i).equals("FULLPAYMENT")) {
                        parameterValues.put("FULLPAYMENT", letterRepo.getParameterValueForAppLetters(tbleBean.getFULLPAYMENT(), appID));
                    } else if (parameterList.get(i).equals("MINPAYEMENT")) {
                        parameterValues.put("MINPAYEMENT", letterRepo.getParameterValueForAppLetters(tbleBean.getMINPAYEMENT(), appID));
                    } else if (parameterList.get(i).equals("OLDCARDNO")) {
                        parameterValues.put("OLDCARDNO", CommonMethods.cardNumberMask(new StringBuffer(letterRepo.getParameterValueForCardLetters(tbleBean.getOLDCARDNO(), cardNumber))));
                    } else if (parameterList.get(i).equals("CARDPRODUCT")) {
                        parameterValues.put("CARDPRODUCT", letterRepo.getParameterValueForAppLetters(tbleBean.getCARDPRODUCT(), appID));
                    }
                }
            } else {
                for (int i = 0; i < parameterList.size(); i++) {

                    if (parameterList.get(i).equals("NAME")) {
                        parameterValues.put("NAME", letterRepo.getParameterValueForCardLetters(tbleBean.getNAME(), cardNumber));
                    } else if (parameterList.get(i).equals("ADDRESS1")) {
                        parameterValues.put("ADDRESS1", letterRepo.getParameterValueForCardLetters(tbleBean.getADDRESS1(), cardNumber));
                    } else if (parameterList.get(i).equals("ADDRESS2")) {
                        parameterValues.put("ADDRESS2", letterRepo.getParameterValueForCardLetters(tbleBean.getADDRESS2(), cardNumber));
                    } else if (parameterList.get(i).equals("ADDRESS3")) {
                        parameterValues.put("ADDRESS3", letterRepo.getParameterValueForCardLetters(tbleBean.getADDRESS3(), cardNumber));
                    } else if (parameterList.get(i).equals("CARDNO")) {
                        parameterValues.put("CARDNO", CommonMethods.cardNumberMask(new StringBuffer(letterRepo.getParameterValueForCardLetters(tbleBean.getCARDNO(), cardNumber))));
                    } else if (parameterList.get(i).equals("CREDITLIMIT")) {
                        parameterValues.put("CREDITLIMIT", letterRepo.getParameterValueForCardLetters(tbleBean.getCREDITLIMIT(), cardNumber));
                    } else if (parameterList.get(i).equals("TITLE")) {
                        parameterValues.put("TITLE", letterRepo.getParameterValueForCardLetters(tbleBean.getTITLE(), cardNumber));
                    } else if (parameterList.get(i).equals("BODY")) {
                        parameterValues.put("BODY", letterRepo.getParameterValueForCardLetters(tbleBean.getBODY(), cardNumber));
                    } else if (parameterList.get(i).equals("TEXT")) {
                        parameterValues.put("TEXT", letterRepo.getParameterValueForCardLetters(tbleBean.getTEXT(), cardNumber));
                    } else if (parameterList.get(i).equals("BRANCH")) {
                        parameterValues.put("BRANCH", letterRepo.getParameterValueForCardLetters(tbleBean.getBRANCH(), cardNumber));
                    } else if (parameterList.get(i).equals("AMOUNT")) {
                        parameterValues.put("AMOUNT", letterRepo.getParameterValueForCardLetters(tbleBean.getAMOUNT(), cardNumber));
                    } else if (parameterList.get(i).equals("TODAY")) {
                        Date date = new Date();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                        parameterValues.put("TODAY", dateFormat.format(date));
                    } else if (parameterList.get(i).equals("LETTERREF")) {
                        parameterValues.put("LETTERREF", RefID);
                    } else if (parameterList.get(i).equals("FULLPAYMENT")) {
                        parameterValues.put("FULLPAYMENT", letterRepo.getParameterValueForCardLetters(tbleBean.getFULLPAYMENT(), cardNumber));
                    } else if (parameterList.get(i).equals("MINPAYEMENT")) {
                        parameterValues.put("MINPAYEMENT", letterRepo.getParameterValueForCardLetters(tbleBean.getMINPAYEMENT(), cardNumber));
                    } else if (parameterList.get(i).equals("OLDCARDNO")) {
                        parameterValues.put("OLDCARDNO", CommonMethods.cardNumberMask(new StringBuffer(letterRepo.getParameterValueForCardLetters(tbleBean.getOLDCARDNO(), cardNumber))));
                    } else if (parameterList.get(i).equals("CARDPRODUCT")) {
                        parameterValues.put("CARDPRODUCT", letterRepo.getParameterValueForCardLetters(tbleBean.getCARDPRODUCT(), cardNumber));
                    }
                }
            }

            String body = letterRepo.getTemplateBody(new String[]{tempID, cardProduct});
            System.out.println(body);
            for (Map.Entry<String, String> entry : parameterValues.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value == null) {
                    value = " ";
                }
                body = body.replace("|" + key + "|", value);
            }

            Document document = new Document(PageSize.LETTER);
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();
            HTMLWorker htmlWorker = new HTMLWorker(document);
            htmlWorker.parse(new StringReader(body));
            document.close();
            System.out.println("Done");

        } catch (Exception e) {
            throw e;
        }

        return fileNameAndPath;
    }
}
