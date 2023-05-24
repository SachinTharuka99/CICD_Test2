/**
 * Author :
 * Date : 4/8/2023
 * Time : 1:35 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.common;

import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.CreateEodId;
import com.epic.cms.util.StatusVarList;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;

public abstract class AbstractEOD {

    @Autowired
    CommonRepo commonRepo;
    @Autowired
    StatusVarList status;

    public static int currentDay = 20;
    public static int currentMonth = 4;
    public static int currentYear = 2016;

    public static LocalDate locDate = new DateTime().withDate(currentYear, currentMonth, currentDay).toLocalDate();

    public void startProcess(int eodId, int categoryId) {
        try {
            System.out.println("#######################");
            //WebComHandler.showOnWeb("#######################");
            CreateEodId createEodDate = new CreateEodId();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

            if (eodId == -1) {//file processing request
                Configurations.EOD_ID = commonRepo.getCurrentEodId(status.getINITIAL_STATUS(), status.getERROR_STATUS());
                System.out.println("After Eod Id : " + Configurations.EOD_ID);
                if (Configurations.EOD_ID == 0) {
                    System.out.println("---------------------->> No available EOD ID to start....");
                    //WebComHandler.showOnWeb("No available EOD ID to start....");
                    int eodINPR = commonRepo.getCurrentEodId(status.getINPROGRESS_STATUS(), status.getINPROGRESS_STATUS());
                    if (eodINPR != 0) {
                        System.out.println("Currently EOD ID: [" + eodINPR + "] is in progress..");
                        //WebComHandler.showOnWeb("Currently EOD ID: [" + eodINPR + "] is in progress..");
                    } else {
                        //enterFirstEODId();
                    }
                }
            } else {
                Configurations.EOD_ID = eodId;
            }
            if (Configurations.EOD_ID != 0) {
                try {
                    /**
                     * Set Starting EOD ID
                     */
                    Configurations.ERROR_EOD_ID = Configurations.EOD_ID;
                    Configurations.STARTING_EOD_STATUS = commonRepo.getEodStatusByEodID(Configurations.EOD_ID);
                    setStartingEODID();
//                    setStartTimeForEOD();
                } catch (NullPointerException ex) {
                    System.out.println("---------------##----->> No available EOD ID to start....");
                    //WebComHandler.showOnWeb(" No available EOD ID to start....");
                    return;
                }
                System.out.println("EOD process started....");

                //EOD date
                Configurations.EOD_DATE = createEodDate.getDateFromEODID(Configurations.EOD_ID);
                Configurations.EOD_DATE_String = sdf.format(createEodDate.getDateFromEODID(Configurations.EOD_ID));

                System.out.println("EOD ID: " + Configurations.ERROR_EOD_ID);
                System.out.println("EOD Date: " + Configurations.EOD_DATE);

                this.runEOD(eodId, categoryId);
            }
        } catch (Exception ex) {

        }
    }

    protected abstract void runEOD(int eodId, int categoryId) throws Exception;

    private void enterFirstEODId() throws Exception {

    }

    void setStartingEODID() throws Exception {

        if (Configurations.STARTING_EOD_STATUS.equals(status.getERROR_STATUS())) {
            String stEODId = Configurations.EOD_ID + "";
            String subs = stEODId.substring(0, stEODId.length() - 2);
            stEODId = subs + "00";

            Configurations.EOD_ID = Integer.parseInt(stEODId);
        }
    }
}
