/**
 * Author :
 * Date : 10/26/2022
 * Time : 9:27 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.util;

public class DatabaseStatus {
    public static final String TYPE_VISA_FILE = "VISA";
    public static final String TYPE_MASTER_FILE = "MASTER";
    public static final String TYPE_SWITCH_FILE = "SWITCH";
    public static final String TYPE_CORE_BANK_FILE = "COREBANK";
    public static final String TYPE_ONUS_FILE = "ONUS";

    public static final String STATUS_FILE_INIT = "INIT";
    public static final String STATUS_FILE_INIT2 = "FINIT";
    public static final String STATUS_FILE_READ = "FREAD";
    public static final String STATUS_FILE_PROS = "FPROS";
    public static final String STATUS_FILE_VALD = "FVALD";
    public static final String STATUS_FILE_REPT = "FREPT";
    public static final String STATUS_FILE_COMP = "FCOMP";
    public static final String STATUS_FILE_ERROR = "FEROR";
    public static final String STATUS_FILE_REJECT = "FREJT";

    public static final String STATUS_SESSION_INIT = "SINIT";
    public static final String STATUS_SESSION_PHASE_00 = "SPH00";
    public static final String STATUS_SESSION_PHASE_01 = "SPH01";
    public static final String STATUS_SESSION_PHASE_02 = "SPH02";
    public static final String STATUS_SESSION_PHASE_03 = "SPH03";
    public static final String STATUS_SESSION_COMP = "SCOMP";

    public static final String STATUS_PHASE_INIT = "PINIT";
    public static final String STATUS_PHASE_WORKING_PROGRESS = "PPROG";
    public static final String STATUS_PHASE_COMP = "PCOMP";
    public static final String STATUS_PHASE_ERROR = "PEROR";

    public static final String STATUS_MATCH_PROCESS_INIT = "MINIT";
    public static final String STATUS_MATCH_PROCESS_MANUAL = "MMANU";
    public static final String STATUS_MATCH_PROCESS_AUTO = "MAUTO";

    public static final String STATUS_TXN_INIT = "TINIT";
    public static final String STATUS_TXN_MATCH_CB = "TMACB";
    public static final String STATUS_TXN_MATCH_ONUS = "TMOUS";
    public static final String STATUS_TXN_MATCH_VISA = "TMVSA";

    public static final String STATUS_MANUAL_TXN_INIT = "MAINIT";
    public static final String STATUS_MANUAL_TXN_MATCH_PENDING = "MAPEND";
    public static final String STATUS_MANUAL_TXN_MATCH_ACCEPT = "MAACPT";
    public static final String STATUS_MANUAL_TXN_MATCH_REJECT = "MAREJT";

    public static final String STATUS_PHASE_TYPE_CBCB = "CBCB";
    public static final String STATUS_PHASE_TYPE_VISAVISA = "VISAVISA";
    public static final String STATUS_PHASE_TYPE_MASTERMASTER = "MASTMAST";
    public static final String STATUS_PHASE_TYPE_ONUSONUS = "ONUSONUS";
    public static final String STATUS_PHASE_TYPE_CBVISA = "CBVISA";
    public static final String STATUS_PHASE_TYPE_CBSW = "CBSW";
    public static final String STATUS_PHASE_TYPE_CBVISATOLAM = "CBVISATOLAM";
    public static final String STATUS_PHASE_TYPE_CBONUS = "CBONUS";
    public static final String STATUS_PHASE_TYPE_CBVISA_TOLARANCE_DAYS = "CBVISATOLDT";
    public static final String STATUS_PHASE_TYPE_CBONUS_TOLARANCE_DAYS = "CBONUSTOLDT";

    public static final String VISA_FINANCIAL_STATUS_YES = "YES";
}
