/********************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.proxy.api

class FinancialAidStatusApi {

    public final static String FINANCIAL_AID_SUMMARY = """

declare
  msg                  VARCHAR2(500);
  pidm                 NUMBER := ?;
  aidy                 ROBINST.ROBINST_AIDY_CODE%TYPE := ?;
  award_dtl_rec        bwrkrhst.award_dtl_type;
  award_found          VARCHAR2(1) := 'N';
  rorsapr_rec          RORSAPR%ROWTYPE;
  lv_ReqTab            bwrktrkr.ReqRecTab;
  rtvsapr_rec          rtvsapr%ROWTYPE;
  stvterm_rec          stvterm%ROWTYPE;
  othres1_rec          bwrkrhst.othres1_type;
  othres2_rec          bwrkrhst.othres2_type;
  rnvand0_rec          bwrkrhst.rnvand0_type;
  rorhold_rec          bwrkhold.rorhold_type;
  appmsg_rec           bwrkamsg.appmsg_type;
  robinst_rec          bwrkrhst.robinst_type;
  detail_rec           bwrklhst.loan_detail_type;
  amounts_rec          bwrklhst.loan_amounts_type;
  loans_paid           NUMBER := 0;
  awards_paid          NUMBER := 0;
  hold_aidy            ROBINST.ROBINST_AIDY_CODE%TYPE;
  aidy_desc            ROBINST.ROBINST_AIDY_DESC%TYPE;
  total_budget_amt     NUMBER;
  total_offer_amt      NUMBER;
  cost_of_attendance   VARCHAR2(2000) DEFAULT NULL;
  award_package        VARCHAR2(2000) DEFAULT NULL;
  account_summary      VARCHAR2(2000) DEFAULT NULL;
  fin_aid_history      VARCHAR2(2000) DEFAULT NULL;
  fin_aid_holds        VARCHAR2(2000) DEFAULT NULL;
  unsat_req            VARCHAR2(2000) DEFAULT NULL;
  lv_finaid_json       VARCHAR2(8000);

  /* Global cursor declarations for package */
  CURSOR calc_rprawrd_offer_amt
  IS
    SELECT NVL(SUM(RPRATRM_OFFER_AMT), 0)
      FROM
          (-- Scheduled awards
           SELECT NVL(SUM(RPRATRM_OFFER_AMT), 0) RPRATRM_OFFER_AMT
             FROM rprawrd,
                  RPRATRM,
                  rorstat,
                  rfrbase,
                  rorwebr
            WHERE rprawrd_pidm      = pidm
              AND NVL(rprawrd_info_access_ind, 'Y') = 'Y'
              AND rfrbase_info_access_ind = 'Y'
              AND rprawrd_fund_code = rfrbase_fund_code
              AND RPRAWRD_AIDY_CODE = RPRATRM_AIDY_CODE
              AND RPRAWRD_PIDM      = RPRATRM_PIDM
              AND rprawrd_pidm      = rorstat_pidm
              AND rorstat_aidy_code = aidy
              AND rorwebr_aidy_code = rorstat_aidy_code
              AND NVL(rorstat_info_access_ind, rorwebr_null_infoaccess_ind) = 'Y'
              AND ((rorstat_pgrp_code IS NOT NULL AND
                    rorstat_pgrp_code IN (SELECT rtvpgrp_code
                                          FROM   rtvpgrp
                                          WHERE  rtvpgrp_info_access_ind = 'Y')
                   )
                   OR rorstat_pgrp_code IS NULL
                  )
              AND RPRAWRD_FUND_CODE = RPRATRM_FUND_CODE
              AND (  (   NVL(RFRBASE_FED_FUND_ID, '*')  = 'PELL'
                     AND RPRATRM_OFFER_AMT > 0
                     AND bwrkolib.F_CheckPellCrossover(
                            aidy,
                            pidm,
                            RPRAWRD_AIDY_CODE,
                            RPRATRM_PERIOD) = 'Y'
                     )
                  OR (   NVL(RFRBASE_FED_FUND_ID, '*') <> 'PELL'
                 AND       ( ( rpratrm_bbay_code IS NULL
                 AND           rpratrm_aidy_code = aidy
                              )
                  OR         ( rpratrm_bbay_code IS NOT NULL
                 AND           NVL(rpratrm_aidy_code_funds,
                               rpratrm_aidy_code)       = aidy
                             )
                           )
                     )
                  )
           UNION ALL
           -- Unscheduled awards
           SELECT NVL(SUM(rprawrd_offer_amt), 0)
             FROM rprawrd, rorstat, rfrbase, rorwebr
            WHERE rprawrd_aidy_code = aidy
              AND rprawrd_pidm      = pidm
              AND NVL(rprawrd_info_access_ind, 'Y') = 'Y'
              AND rfrbase_info_access_ind = 'Y'
              AND rprawrd_fund_code = rfrbase_fund_code
              AND rprawrd_pidm      = rorstat_pidm
              AND rprawrd_aidy_code = rorstat_aidy_code
              AND rprawrd_aidy_code = rorwebr_aidy_code
              AND NVL(rorstat_info_access_ind, rorwebr_null_infoaccess_ind) = 'Y'
              AND ((rorstat_pgrp_code IS NOT NULL AND
                    rorstat_pgrp_code IN (SELECT rtvpgrp_code
                                          FROM   rtvpgrp
                                          WHERE  rtvpgrp_info_access_ind = 'Y')
                   )
                         OR rorstat_pgrp_code IS NULL
                  )
              AND NOT EXISTS
                  (SELECT 'X'
                     FROM RPRATRM
                    WHERE RPRAWRD_AIDY_CODE = RPRATRM_AIDY_CODE
                      AND RPRAWRD_PIDM      = RPRATRM_PIDM
                      AND RPRAWRD_FUND_CODE = RPRATRM_FUND_CODE)
           ) A;

  FUNCTION f_get_info_access_ind (pidm NUMBER,
                                  aidy VARCHAR2)
    RETURN VARCHAR2
  IS
    CURSOR info_access_c IS
      SELECT 'Y'
        FROM RORSTAT, RORWEBR
       WHERE RORSTAT_AIDY_CODE = aidy
         AND RORSTAT_PIDM      = pidm
         AND RORSTAT_AIDY_CODE = RORWEBR_AIDY_CODE
         AND NVL(RORSTAT_INFO_ACCESS_IND, RORWEBR_NULL_INFOACCESS_IND) <> 'N';

    info_access RORSTAT.RORSTAT_INFO_ACCESS_IND%TYPE;
  BEGIN
    OPEN  info_access_c;
    FETCH info_access_c INTO info_access;
    IF info_access_c%NOTFOUND THEN
      info_access := 'N';
    END IF;
    CLOSE info_access_c;

    RETURN info_access;
  END f_get_info_access_ind;

BEGIN
  total_budget_amt := bwrkbudg.f_get_budget_total(aidy, pidm);

  <<Chk_UnSat_Trk_Req>>

     lv_ReqTab := bwrktrkr.f_get_requirements
                             ( p_aidy_code      => aidy,
                               p_pidm           => pidm,
                               p_sat_ind        => 'N'
                             ) ;

     IF lv_ReqTab.COUNT = 0 THEN
         GOTO Chk_Budg;
     END IF; 
     
     unsat_req := '"unSatReq":{"text":"UNSATISFIED_REQS"}';

  <<Chk_Budg>>
     IF  total_budget_amt IS NULL THEN
         GOTO Chk_Awd;
     END IF;

     cost_of_attendance := '"costOfAttendance":{"text":"EST_COST_OF_ATTD"' || ', "amount":' || total_budget_amt || '}';

  <<Chk_Awd>>
     OPEN bwrkrhst.GetAwdDtlC(pidm,aidy);
     FETCH bwrkrhst.GetAwdDtlC INTO award_dtl_rec;
     IF  bwrkrhst.GetAwdDtlC%FOUND THEN
         award_found := 'Y';
         CLOSE bwrkrhst.GetAwdDtlC;
         GOTO Disp_Awd;
     END IF;
     CLOSE bwrkrhst.GetAwdDtlC;
     GOTO Chk_Hold;

  <<Disp_Awd>>
     IF f_get_info_access_ind(pidm,aidy) = 'Y' THEN
        OPEN calc_rprawrd_offer_amt ;
        FETCH calc_rprawrd_offer_amt INTO total_offer_amt ;
        CLOSE calc_rprawrd_offer_amt ;
     
        award_package := '"awardPackage":'
                         || '{"text":"AWARDED_FINAID", "url":"' || 'dummy' || '", "amount":' || total_offer_amt || '}';
     END IF;

  <<Chk_Hold>>
     OPEN bwrkhold.GetFAHoldsC(pidm, aidy); -- 80301-1
     FETCH bwrkhold.GetFAHoldsC INTO rorhold_rec;
     IF  bwrkhold.GetFAHoldsC%NOTFOUND THEN
         CLOSE bwrkhold.GetFAHoldsC;
         GOTO Chk_Acct;
     END IF;
     CLOSE bwrkhold.GetFAHoldsC;
     
     fin_aid_holds := '"finAidHolds":{"text":"HOLDS_PLACED"}';

  <<Chk_Acct>>
     OPEN bwrklhst.GetLoanDtlC(pidm, aidy);
     FETCH bwrklhst.GetLoanDtlC INTO detail_rec;
     IF bwrklhst.GetLoanDtlC%NOTFOUND THEN
        OPEN bwrkrhst.GetAwdDtlC(pidm,aidy);
        FETCH bwrkrhst.GetAwdDtlC INTO award_dtl_rec;
        IF  bwrkrhst.GetAwdDtlC%NOTFOUND THEN
            CLOSE bwrkrhst.GetAwdDtlC;
            CLOSE bwrklhst.GetLoanDtlC;
            GOTO Chk_Hist;
        END IF;
        CLOSE bwrkrhst.GetAwdDtlC;
     END IF;
     CLOSE bwrklhst.GetLoanDtlC;
     OPEN bwrklhst.GetLoanDtlC(pidm,aidy);
     FETCH bwrklhst.GetLoanDtlC INTO detail_rec;
     WHILE bwrklhst.GetLoanDtlC%FOUND LOOP
           bwrklhst.p_get_disb_amts
                      ( p_pidm                =>  pidm,
                        p_loan_rec            =>  detail_rec,
                        p_net_amt_out         =>  amounts_rec.rprladb_check_amt,
                        p_net_amt_return_out  =>  amounts_rec.rprladb_check_return_amt
                      ) ;
           IF amounts_rec.rprladb_check_amt IS NOT NULL OR
              amounts_rec.rprladb_check_amt != 0 THEN
              loans_paid := loans_paid + 1;
           END IF;
     FETCH bwrklhst.GetLoanDtlC INTO detail_rec;
     END LOOP;
     CLOSE bwrklhst.GetLoanDtlC;

     IF loans_paid = 0 THEN
        OPEN bwrkrhst.GetAwdDtlC(pidm,aidy);
        FETCH bwrkrhst.GetAwdDtlC INTO award_dtl_rec;
        WHILE bwrkrhst.GetAwdDtlC%FOUND LOOP
              IF award_dtl_rec.rprawrd_paid_amt IS NOT NULL OR
                 award_dtl_rec.rprawrd_paid_amt != 0 THEN
                 awards_paid := awards_paid + 1;
              END IF;
        FETCH bwrkrhst.GetAwdDtlC INTO award_dtl_rec;
        END LOOP;
        CLOSE bwrkrhst.GetAwdDtlC;
        IF awards_paid = 0 THEN
           GOTO Chk_Hist;
        END IF;
     END IF;

     account_summary := '"accountSummary":{"text":"CREDITS_IN_ACCT_SUMMARY", "url":"' || 'dummy' || '"}';

  <<Chk_Hist>>
     OPEN bwrkrhst.GetActiveAidYearC(pidm);
     FETCH bwrkrhst.GetActiveAidYearC
      INTO robinst_rec.robinst_aidy_desc,
           robinst_rec.robinst_aidy_start_date,
           hold_aidy;
     IF bwrkrhst.GetActiveAidYearC%NOTFOUND THEN
        hold_aidy := NULL ;
     END IF ;
     CLOSE bwrkrhst.GetActiveAidYearC;
     IF  hold_aidy IS NULL THEN
         GOTO End_Summ;
     END IF;

  <<Disp_Hist>>
     fin_aid_history := '"financialAidHistory":{"text":"VIEW_FINAID_SUMMARY", "url":"' || 'dummy' || '"}';

  <<End_Summ>>
    IF cost_of_attendance IS NULL THEN
      cost_of_attendance := '"costOfAttendance":null';
    END IF;
    IF award_package IS NULL THEN
      award_package := '"awardPackage":null';
    END IF;
    IF account_summary IS NULL THEN
      account_summary := '"accountSummary":null';
    END IF;
    IF fin_aid_history IS NULL THEN
      fin_aid_history := '"financialAidHistory":null';
    END IF;
    IF fin_aid_holds IS NULL THEN
      fin_aid_holds := '"finAidHolds":null';
    END IF;
    IF unsat_req IS NULL THEN
      unsat_req := '"unSatReq":null';
    END IF;
  
    lv_finaid_json := '{' 
                      || cost_of_attendance || ','
                      || award_package      || ','
                      || account_summary    || ','
                      || fin_aid_history    || ','
                      || fin_aid_holds        || ','
                      || unsat_req
                      || '}';

    ? := lv_finaid_json;
END;

"""

}
