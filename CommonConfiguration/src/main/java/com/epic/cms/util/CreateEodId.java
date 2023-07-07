package com.epic.cms.util;

import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.CreateEodIdRepo;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
@Component
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class    CreateEodId {

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    CreateEodIdRepo createEodIdRepo;

    @Autowired
    StatusVarList statusList;

    // get Current date as yymmdd format
    public String getDate(Date date) throws Exception {
        try {
            String dateString = "";

            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
            dateString = sdf.format(date);
            return dateString;
        } catch (Exception e) {
            e.printStackTrace();
            //LogFileCreator.writeErrorToLog(e);
            throw e;
        }
    }

    public Date getDateFromEODID(int eodID) throws Exception {
        Date parsedDate = null;
        String streodID = "";
        try {
            if (eodID > 10000000) {
                streodID = eodID + "";
                SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
                String eodIDsubs = streodID.substring(0, streodID.length() - 2);
                parsedDate = sdf.parse(eodIDsubs);
            }

        } catch (Exception e) {
            e.printStackTrace();
//            LogFileCreator.writeErrorToLog(e);
            throw e;
        } finally {
            return parsedDate;
        }
    }

    // insert new EodId to EOD table
    public boolean insertValuesToEODTable() throws Exception {
        try {

            boolean insertStatus = false;
            //Trying to generate new EODID.
            String eodId = generateEodId();

            //MAIN EOD START TIME
            org.joda.time.DateTime mainEodTime = new DateTime(Configurations.EOD_DATE);
            //SUB EOD START TIME
            org.joda.time.DateTime subEODTime = new DateTime(Configurations.EOD_DATE);
            Timestamp nextMainEodTimeStamp = null;
            Timestamp nextSubEodTimeStamp = null;

            if (null != eodId && !eodId.equals("")) {
                mainEodTime = mainEodTime.plusDays(1);
                subEODTime = subEODTime.plusDays(1);
                boolean holiday = createEodIdRepo.isHoliday(subEODTime.toDate());
                Date nextDate = null;
                int x = 1;
                while (holiday) {
                    nextDate = CommonMethods.getNextDateForFreq(subEODTime.toDate(), x);
                    if (createEodIdRepo.isHoliday(nextDate)) {
                        x = x + 1;
                    } else {
                        holiday = false;
                    }
                }
                if (nextDate != null) {
                    subEODTime = subEODTime.withMillis(nextDate.getTime());
                }

                mainEodTime = mainEodTime.plusDays(x).withHourOfDay(Configurations.MAIN_EOD_STARTTIME_H).withMinuteOfHour(Configurations.MAIN_EOD_STARTTIME_M);
                subEODTime = subEODTime.withHourOfDay(Configurations.SUB_EOD_STARTTIME_H).withMinuteOfHour(Configurations.SUB_EOD_STARTTIME_M);

                /**
                 * mainEodTime =
                 * mainEodTime.plusDays(2).withHourOfDay(Configurations.MAIN_EOD_STARTTIME_H).withMinuteOfHour(Configurations.MAIN_EOD_STARTTIME_M);
                 * subEODTime =
                 * subEODTime.plusDays(1).withHourOfDay(Configurations.SUB_EOD_STARTTIME_H).withMinuteOfHour(Configurations.SUB_EOD_STARTTIME_M);
                 *
                 */
                nextMainEodTimeStamp = new Timestamp(mainEodTime.getMillis());
                nextSubEodTimeStamp = new Timestamp(subEODTime.getMillis());

                insertStatus = createEodIdRepo.insertValuesToEODTable(eodId, nextMainEodTimeStamp, statusList.getINITIAL_STATUS(), statusList.getINITIAL_STATUS(), statusList.getINITIAL_STATUS());
//                if (createEodIdRepo.isNextEodIdExistsInEodRunningParameterTable(eodId)) {
//                    createEodIdRepo.updateNextEodRunningParameterTable(eodId);
//                } else {
//                    createEodIdRepo.insertValuesToEodRunningParameterTable(eodId);
//                }
                System.out.println("next Main EOD timestamp: " + nextMainEodTimeStamp);

            } else {
                System.out.println("#####################################################");
                System.out.println("Cannot create a new EOD ID. Previous ID not completed");
                System.out.println("------------------------------------------");
                /**
                 * TODO create new EOD with sequential EODID for EROR state.
                 */
                String stEODId = Configurations.ERROR_EOD_ID + "";
                String subs = stEODId.substring(stEODId.length() - 2, stEODId.length());
                int subInteger = Integer.parseInt(subs);
                if (subInteger <= 99) {
                    subs = stEODId.substring(0, stEODId.length() - 2);
                    stEODId = String.format("%02d", ++subInteger);

                    eodId = subs + stEODId;
                }
                //Updating current EOD id status with ERIP
                createEodIdRepo.updateEodEndStatus(Configurations.ERROR_EOD_ID, statusList.getERROR_INPR_STATUS());
                System.out.println("Successfully updated current EOD ID with ERIP status");
                System.out.println("#####################################################");
                nextMainEodTimeStamp = new Timestamp(mainEodTime.getMillis());
                nextSubEodTimeStamp = new Timestamp(subEODTime.getMillis());
                insertStatus = createEodIdRepo.insertValuesToEODTable(eodId, nextMainEodTimeStamp, statusList.getERROR_STATUS(), statusList.getINITIAL_STATUS(), statusList.getINITIAL_STATUS());
//                if (createEodIdRepo.isNextEodIdExistsInEodRunningParameterTable(eodId)) {
//                    createEodIdRepo.updateNextEodRunningParameterTable(eodId);
//                } else {
//                    createEodIdRepo.insertValuesToEodRunningParameterTable(eodId);
//                }
                System.out.println("next Main EOD timestamp: " + nextMainEodTimeStamp);
                // Configurations.EOD_ID = Integer.parseInt(eodId);
            }
            Configurations.EOD_ID = Integer.parseInt(eodId);
            Configurations.ERROR_EOD_ID = Configurations.EOD_ID;
            //set the starting eod status
            Configurations.STARTING_EOD_STATUS = createEodIdRepo.getEodStatusByEodID(Configurations.ERROR_EOD_ID);

            return insertStatus;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // generating eodId process
    public String generateEodId() throws Exception {
        try {

            String eodId = "";
            String lastEodId = "";
            // check if all eod status values are equal to the COMP
            if (isStatusCOMP()) {
                {
                    // generate process of eodId
                    lastEodId = getLastEodIdByCurrentDate();
                    Date dateOfLastCompletedEOD = getDateFromEODID(Integer.parseInt(lastEodId));
                    DateTime newDateTime = new DateTime(dateOfLastCompletedEOD);
                    newDateTime = newDateTime.plusDays(1);
                    eodId = getDate(newDateTime.toDate()) + "00";

                }

            } else {
                System.out.println("All status are not completed");
            }
            return eodId;

        } catch (Exception e) {
            //LogFileCreator.writeErrorToLog(e);
            throw e;
        }
    }

    // check all status values
    public boolean isStatusCOMP() throws Exception {
        try {
            boolean status = false;

            status = createEodIdRepo.isStatusComp();

            return status;
        } catch (Exception e) {
            e.printStackTrace();
            //LogFileCreator.writeErrorToLog(e);
            throw e;
        }
    }

    // get last eodid executed for current date
    public String getLastEodIdByCurrentDate() throws Exception {
        try {

            String eodId = "";
            eodId = createEodIdRepo.getEodIdByLastCompletedEODID();

            return eodId;
        } catch (Exception e) {
            e.printStackTrace();
            //LogFileCreator.writeErrorToLog(e);
            throw e;
        }
    }

    public int getCurrentEodId(String status, String ErrorStatus) throws Exception {
        int eodid = createEodIdRepo.getCurrentEodId(status, ErrorStatus);
        System.out.println("Get Current EOD Id _"+eodid );
        return eodid;
    }
}
