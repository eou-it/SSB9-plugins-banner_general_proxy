/********************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.proxy.api

class FinancialAidStatusApi {

    public final static String FINANCIAL_AID_SUMMARY = """

--  procedure P_DispSumm (aidy_in in ROBINST.ROBINST_AIDY_CODE%TYPE default null,
--                        calling_proc_name varchar2 default null);
declare

--  curr_release VARCHAR2(10) := '8.23';
  msg                  VARCHAR2(500);
  pidm                 NUMBER := ?;
  aidy                 ROBINST.ROBINST_AIDY_CODE%TYPE := ?;
  award_dtl_rec        bwrkrhst.award_dtl_type;
  award_found          VARCHAR2(1) := 'N';
  rorsapr_rec          RORSAPR%ROWTYPE;
  lv_ReqTab            bwrktrkr.ReqRecTab;                                                                            -- 80500-1
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
  finAidHolds          VARCHAR2(2000) DEFAULT NULL;
  unSatReq             VARCHAR2(2000) DEFAULT NULL;
  lv_finaid_json       VARCHAR2(8000);

  /* Global cursor declarations for package */
  CURSOR calc_rprawrd_offer_amt
  IS
    -- 080800-1
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
              AND rfrbase_info_access_ind = 'Y'                                                     -- 80500-1
              AND rprawrd_fund_code = rfrbase_fund_code
              AND RPRAWRD_AIDY_CODE = RPRATRM_AIDY_CODE
              AND RPRAWRD_PIDM      = RPRATRM_PIDM
              AND rprawrd_pidm      = rorstat_pidm                                                  -- 081000-1
              AND rorstat_aidy_code = aidy                                                          -- 081000-1
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
              AND (  (   NVL(RFRBASE_FED_FUND_ID, '*')  = 'PELL'                                    -- 081000-1
                     AND RPRATRM_OFFER_AMT > 0                                                      -- 081000-1
                     AND bwrkolib.F_CheckPellCrossover(                                             -- 081000-1
                            aidy,                                                                   -- 081000-1
                            pidm,                                                                   -- 081000-1
                            RPRAWRD_AIDY_CODE,                                                      -- 081000-1
                            RPRATRM_PERIOD) = 'Y'                                                   -- 081000-1
                     )                                                                              -- 081000-1
                  OR (   NVL(RFRBASE_FED_FUND_ID, '*') <> 'PELL'                                    -- 081000-1
--               AND       rprawrd_aidy_code = aidy                                                 -- 081000-1
                 AND       ( ( rpratrm_bbay_code IS NULL                                            -- 8.23-1
                 AND           rpratrm_aidy_code = aidy                                             -- 8.23-1
                              )                                                                     -- 8.23-1
                  OR         ( rpratrm_bbay_code IS NOT NULL                                        -- 8.23-1
                 AND           NVL(rpratrm_aidy_code_funds,                                         -- 8.23-1
                               rpratrm_aidy_code)       = aidy                                      -- 8.23-1
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
              AND rfrbase_info_access_ind = 'Y'                                                     -- 80500-1
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
-- 80200-1
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
--
  /* Any forward declarations needed for the subprograms */
  /* Fully defined subprograms specified in package */
-------------------------------------------------------------------------------
  -- 80300-1
--  FUNCTION F_GetActiveLink(LINK VARCHAR2, text VARCHAR2) RETURN VARCHAR2 IS
--  BEGIN
--    IF twbkwbis.F_ValidLink(LINK) THEN
--      RETURN twbkfrmt.F_PrintAnchor(twbkfrmt.f_encodeurl(LINK), text);
--    ELSE
--      RETURN text;
--    END IF;
--  END;

-------------------------------------------------------------------------------

--  PROCEDURE P_DispSumm (aidy_in IN ROBINST.ROBINST_AIDY_CODE%TYPE DEFAULT NULL,
--                        calling_proc_name VARCHAR2 DEFAULT NULL) IS
--    award_dtl_rec        bwrkrhst.award_dtl_type;
--    award_found          VARCHAR2(1) := 'N';
--    rorsapr_rec          RORSAPR%ROWTYPE;
--    lv_ReqTab            bwrktrkr.ReqRecTab;                                                                            -- 80500-1
--    rtvsapr_rec          rtvsapr%ROWTYPE;
--    stvterm_rec          stvterm%ROWTYPE;
--    othres1_rec          bwrkrhst.othres1_type;
--    othres2_rec          bwrkrhst.othres2_type;
--    rnvand0_rec          bwrkrhst.rnvand0_type;
--    rorhold_rec          bwrkhold.rorhold_type;
--    appmsg_rec           bwrkamsg.appmsg_type;
--    robinst_rec          bwrkrhst.robinst_type;
--    detail_rec           bwrklhst.loan_detail_type;
--    amounts_rec          bwrklhst.loan_amounts_type;
--    loans_paid           NUMBER := 0;
--    awards_paid          NUMBER := 0;
--    hold_aidy            ROBINST.ROBINST_AIDY_CODE%TYPE;
--    aidy_desc            ROBINST.ROBINST_AIDY_DESC%TYPE;
--    total_budget_amt     NUMBER;
--    total_offer_amt      NUMBER;

  BEGIN
--     IF NOT twbkwbis.F_ValidUser(pidm) THEN
--        RETURN;
--     END IF;
  --
    -- 082101-1
--    twbksecr.p_chk_parms_05(aidy_in, calling_proc_name);


--     IF  aidy_in IS NOT NULL THEN
--         twbkwbis.P_SetParam(pidm,'AIDY',aidy_in);
--         aidy := aidy_in;
--     ELSE
--         aidy := twbkwbis.F_GetParam(pidm,'AIDY');
--     END IF;
  --
--     aidy_desc := bwrkolib.F_ValidAidy(aidy);
--     IF  aidy_desc IS NULL THEN
--         bwrkolib.P_SelDefAidy(aidy,'bwrksumm.P_DispSumm');
--         RETURN;
--     END IF;
  --
--     bwckfrmt.p_open_doc ('bwrksumm.P_DispSumm',
--                           header_text_in => aidy_desc);
  --
--     bwrkolib.P_ProcessUserDefTxt(pidm, aidy, 'ST');                                                                    -- 081401-1

     total_budget_amt := bwrkbudg.f_get_budget_total(aidy, pidm);                                                       -- 081200-1

--     IF total_budget_amt IS NOT NULL THEN                                                                               -- 081401-1
--       bwrkbudg.p_display_budget_status(aidy, pidm, 'bwrksumm.P_DispSumm');                                             -- 081401-1
--     END IF;                                                                                                            -- 081401-1

  -- 080501-1
--     twbkwbis.P_DispInfo('bwrksumm.P_DispSumm','DEFAULT');
  --
 <<Chk_UnSat_Trk_Req>>

     lv_ReqTab := bwrktrkr.f_get_requirements                                                                           -- 80500-1
                             ( p_aidy_code      => aidy,                                                                -- 80500-1
                               p_pidm           => pidm,                                                                -- 80500-1
                               p_sat_ind        => 'N'                                                                  -- 80500-1
                             ) ;                                                                                        -- 80500-1
                                                                                                                       -- 80500-1
     IF lv_ReqTab.COUNT = 0 THEN                                                                                        -- 80500-1
         GOTO Chk_Budg;                                                                                                 -- 80500-1
     END IF; 
     
     
          unSatReq := '"unSatReq":['
                       || '{"text":' || '"' ||
                                 G\$_NLS.Get('BWRKSUM1-0001', 'SQL', 'You have unsatisfied ') ||
                                 G\$_NLS.Get('BWRKSUM1-0002', 'SQL','student requirements') ||
                                 G\$_NLS.Get('BWRKSUM1-0003', 'SQL', ' for this aid year.')
                                  || '"' || '}]';                                                                                                           -- 80500-1

----
--  <<UnSat_Req_Exist>>
--     twbkfrmt.P_TableOpen('PLAIN',
--         cattributes   => 'SUMMARY= "' ||
--                             g\$_nls.get ('BWRKSUM1-0000',
--                                'SQL',
--                                'This layout table displays the link to student requirements page.'
--                             ) ||
--                             '"');
--        twbkfrmt.P_TableRowOpen;
--        -- 80300-1
--        twbkfrmt.P_TableData(G\$_NLS.Get('BWRKSUM1-0001', 'SQL', 'You have unsatisfied ') ||
----                             F_GetActiveLink('bwrktrkr.P_DispTrkReq?aidy_in=' || twbkfrmt.f_encode(aidy) || '&calling_proc_name=' || 'bwrksumm.P_DispSumm',
--                             F_GetActiveLink('bwrkelig.P_DisplayTabs?aidy_in=' || twbkfrmt.f_encode(aidy)
--                                             || '&tab_type=' || 'ER'|| '&calling_proc_name=' || 'bwrksumm.P_DispSumm',
--                                             G\$_NLS.Get('BWRKSUM1-0002', 'SQL','student requirements')) ||
--                             G\$_NLS.Get('BWRKSUM1-0003', 'SQL', ' for this aid year.'));
--        twbkfrmt.P_TableRowClose;
--     twbkfrmt.P_TableClose;

  <<Chk_Budg>>
     IF  total_budget_amt IS NULL THEN
         GOTO Chk_Awd;
     END IF;
  --
--     twbkfrmt.P_TableOpen('PLAIN',
--         cattributes   => 'SUMMARY= "' ||
--                             g\$_nls.get ('BWRKSUM1-0004',
--                                'SQL',
--                                'This layout table displays the link to cost of attendance page.'
--                             ) ||
--                             '"');
--     twbkfrmt.P_TableRowOpen;
--
--     IF (twbkwbis.F_ValidLink('bwrkbudg.P_DispBudg')) THEN
--
--            twbkfrmt.P_tableData(G\$_NLS.Get('BWRKSUM1-0005', 'SQL',
--                                'Your estimated ') ||
--            twbkfrmt.F_PrintAnchor(twbkfrmt.f_encodeurl(
--                                 'bwrkbudg.P_DispBudg?aidy_in='
--                              || twbkfrmt.f_encode(aidy)
--                       || '&calling_proc_name=' || 'bwrksumm.P_DispSumm')
--                              , G\$_NLS.Get('BWRKSUM1-0006', 'SQL',
--                                'cost of attendance'))
--                    || G\$_NLS.Get('BWRKSUM1-0007', 'SQL',' is ')
--                    || to_char(total_budget_amt,'L999G999G999G999G999G999G999G999G999G999G999D99')
--                    || '.' );
--
--     ELSE
--            twbkfrmt.P_tableData(G\$_NLS.Get('BWRKSUM1-0008', 'SQL',
--                              'Your estimated cost of attendance is')
--
--
--                    || to_char(total_budget_amt,'L999G999G999G999G999G999G999G999G999G999G999D99')                      -- 081401-1
--                    || '.' );
--     END IF;
--
--     twbkfrmt.P_TableRowClose;
--     twbkfrmt.P_TableClose;
     cost_of_attendance := '"costOfAttendance":['
                       || '{"text":"' || G\$_NLS.Get('BWRKSUM1-0008', 'SQL', 'Your estimated cost of attendance is ') || '"'
                       ||  ', "amount": ' || total_budget_amt || '}]';
  --
  <<Chk_Awd>>
     OPEN bwrkrhst.GetAwdDtlC(pidm,aidy);
     FETCH bwrkrhst.GetAwdDtlC INTO award_dtl_rec;
     IF  bwrkrhst.GetAwdDtlC%FOUND THEN
         award_found := 'Y';
         CLOSE bwrkrhst.GetAwdDtlC;
         GOTO Disp_Awd;
     END IF;
     CLOSE bwrkrhst.GetAwdDtlC;
  --
--  <<Chk_OthRes>>
--     OPEN bwrkrhst.GetOthResOneC(pidm,aidy);
--     FETCH bwrkrhst.GetOthResOneC INTO othres1_rec;
--     IF  bwrkrhst.GetOthResOneC%FOUND THEN
--         CLOSE bwrkrhst.GetOthResOneC;
--         GOTO Disp_OthRes;
--     END IF;
--     CLOSE bwrkrhst.GetOthResOneC;
--  --
--     OPEN bwrkrhst.GetOthResTwoC(pidm,aidy);
--     FETCH bwrkrhst.GetOthResTwoC INTO othres2_rec;
--     IF  bwrkrhst.GetOthResTwoC%FOUND THEN
--         CLOSE bwrkrhst.GetOthResTwoC;
--         GOTO Disp_OthRes;
--     END IF;
--     CLOSE bwrkrhst.GetOthResTwoC;
--  --
--  <<Chk_RORSTAT>>
--     OPEN bwrkrhst.GetTotAmtC(pidm,aidy);
--     FETCH bwrkrhst.GetTotAmtC INTO rnvand0_rec;
--     IF  bwrkrhst.GetTotAmtC%FOUND
--     AND rnvand0_rec.tot_resource_amt <> 0
--     AND rnvand0_rec.tot_resource_amt IS NOT NULL THEN
--         CLOSE bwrkrhst.GetTotAmtC;
--         GOTO Disp_OthRes;
--     END IF;
--     CLOSE bwrkrhst.GetTotAmtC;
     GOTO Chk_SAP;
  --
  <<Disp_Awd>>
----5301-1
----   total_offer_amt := F_CALC_TOTAL_OFFER(pidm,aidy);
--     OPEN calc_rprawrd_offer_amt ;
--     FETCH calc_rprawrd_offer_amt INTO total_offer_amt ;
--     CLOSE calc_rprawrd_offer_amt ;
----
--     IF f_get_info_access_ind(pidm,aidy) = 'Y' THEN -- 80200-1
--       twbkfrmt.P_TableOpen('PLAIN',
--           cattributes   => 'SUMMARY= "' ||
--                               g\$_nls.get ('BWRKSUM1-0009',
--                                  'SQL',
--                                  'This layout table displays the link to award package page.'
--                               ) ||
--                               '"');
--          twbkfrmt.P_TableRowOpen;
--        -- 80300-1
--          twbkfrmt.P_TableData(G\$_NLS.Get('BWRKSUM1-0010', 'SQL', 'You have been ') ||
--                               F_GetActiveLink('bwrkrhst.P_DispAwdAidYear?aidy_in=' || twbkfrmt.f_encode(aidy) || '&calling_proc_name=' || 'bwrksumm.P_DispSumm',
--                                               G\$_NLS.Get('BWRKSUM1-0011', 'SQL', 'awarded')) ||
----                             G\$_NLS.Get('BWRKSUM1-0012', 'SQL', ' financial aid which totals ') || to_char(total_offer_amt, 'L999G999D99') || '.');  -- 81801-1
--                               G\$_NLS.Get('BWRKSUM1-0012', 'SQL', ' financial aid which totals ') || to_char(total_offer_amt, 'L999G999G999G999G999G999G999G999G999G999G999D99') || '.');  -- 81801-1
--          twbkfrmt.P_TableRowClose;
--       twbkfrmt.P_TableClose;
--     END IF;
  IF f_get_info_access_ind(pidm,aidy) = 'Y' THEN
       OPEN calc_rprawrd_offer_amt ;
       FETCH calc_rprawrd_offer_amt INTO total_offer_amt ;
       CLOSE calc_rprawrd_offer_amt ;
     
       award_package := '"awardPackage":['
                        || '{"text":"' || G\$_NLS.Get('BWRKSUM1-0010', 'SQL', 'You have been ') || '"},'
                        || '{"text":"' || G\$_NLS.Get('BWRKSUM1-0011', 'SQL', 'awarded ') || '", "url":"' || 'dummy' || '"},'
                        || '{"text":"' || G\$_NLS.Get('BWRKSUM1-0012', 'SQL', 'financial aid which totals ')
                        ||  '", "amount":' || total_offer_amt || '}'
                        || ']';
  END IF;
  --
--  <<Disp_OthRes>>
--  IF award_found = 'N' THEN
--    -- 80300-1
--    IF twbkwbis.F_ValidLink('bwrkrhst.P_DispAwdAidYear') THEN
--      twbkfrmt.P_TableOpen('PLAIN',
--           cattributes   => 'SUMMARY= "' ||
--                               g\$_nls.get ('BWRKSUM1-0013',
--                                  'SQL',
--                                  'This layout table displays the link to outside resources page.'
--                               ) ||
--                               '"');
--         twbkfrmt.P_TableRowOpen;
--            twbkfrmt.P_tableData(G\$_NLS.Get('BWRKSUM1-0014', 'SQL',
--                                   'You have ') ||
--                           twbkfrmt.F_PrintAnchor (twbkfrmt.f_encodeurl(
--                                         'bwrkrhst.P_DispAwdAidYear?aidy_in='
--                                        ||twbkfrmt.f_encode(aidy)
--                                        || '&calling_proc_name=' ||
--                                           'bwrksumm.P_DispSumm')
--                                        ,  G\$_NLS.Get('BWRKSUM1-0015', 'SQL',
--                                         'outside resources'))
--                           || '.');
--         twbkfrmt.P_TableRowClose;
--      twbkfrmt.P_TableClose;
--    END IF;
--  END IF;
  --
  <<Chk_SAP>>
--     OPEN bwrksaph.GetFASAPC(pidm);
--     FETCH bwrksaph.GetFASAPC INTO rorsapr_rec;
--     IF  bwrksaph.GetFASAPC%NOTFOUND THEN
--         CLOSE bwrksaph.GetFASAPC;
--         GOTO Chk_Hold;
--     END IF;
--     CLOSE bwrksaph.GetFASAPC;
--  --
--     OPEN rtksapr.rtvsaprC(rorsapr_rec.rorsapr_sapr_code);
--     FETCH rtksapr.rtvsaprC INTO rtvsapr_rec;
--     IF rtksapr.rtvsaprC%NOTFOUND THEN
--        rtvsapr_rec.rtvsapr_desc := NULL ;
--     END IF;
--     CLOSE rtksapr.rtvsaprC;
--     OPEN stkterm.stvtermC(rorsapr_rec.rorsapr_term_code);
--     FETCH stkterm.stvtermC INTO stvterm_rec;
--     IF stkterm.stvtermC%NOTFOUND THEN
--        stvterm_rec.stvterm_desc := NULL ;
--     END IF ;
--     CLOSE stkterm.stvtermC;
--
--     IF twbkwbis.F_ValidLink('bwrksaph.P_DispSAP') THEN                                                                 -- 80500-1
--         twbkfrmt.P_TableOpen('PLAIN',
--             cattributes   => 'SUMMARY= "' ||
--                                 g\$_nls.get ('BWRKSUM1-0016',
--                                    'SQL',
--                                    'This layout table displays the links to academic transcript and academic progress pages.'
--                                 ) ||
--                                 '"');
--            twbkfrmt.P_TableRowOpen;
--            IF twbkwbis.F_ValidLink('bwskotrn.P_ViewTermTran') THEN
--               twbkfrmt.P_tableData(G\$_NLS.Get('BWRKSUM1-0017', 'SQL',
--                                   'Based on your ')
--                             || twbkfrmt.F_PrintAnchor(twbkfrmt.f_encodeurl(
--                               'bwskotrn.P_ViewTermTran'),
--                                G\$_NLS.Get('BWRKSUM1-0018', 'SQL',
--                               'academic transcript'))
--                             || ','||G\$_NLS.Get('BWRKSUM1-0019', 'SQL',
--                                ' the status of your ') ||
--                             -- 80300-1
--                             F_GetActiveLink('bwrkelig.P_DisplayTabs?aidy_in=' || twbkfrmt.f_encode(aidy)
--                                              || '&tab_type=' || 'EA'|| '&calling_proc_name=' || 'bwrksumm.P_DispSumm',
--                                              G\$_NLS.Get('BWRKSUM1-0020', 'SQL', 'academic progress'))
--                             || G\$_NLS.Get('BWRKSUM1-0021', 'SQL',' is ')
--                             || rtvsapr_rec.rtvsapr_desc
--                             || G\$_NLS.Get('BWRKSUM1-0022', 'SQL',' as of ')
--                             || stvterm_rec.stvterm_desc || '.' );
--            ELSE
--               twbkfrmt.P_tableData(G\$_NLS.Get('BWRKSUM1-0023', 'SQL',
--                          'Based on your academic transcript, the status of your ') ||
--                             -- 80300-1
--                             F_GetActiveLink('bwrkelig.P_DisplayTabs?aidy_in=' || twbkfrmt.f_encode(aidy)
--                                              || '&tab_type=' || 'EA'|| '&calling_proc_name=' || 'bwrksumm.P_DispSumm',
--                                              G\$_NLS.Get('BWRKSUM1-0024', 'SQL', 'academic progress'))
--                             || G\$_NLS.Get('BWRKSUM1-0025', 'SQL',' is ')
--                             || rtvsapr_rec.rtvsapr_desc
--                             || G\$_NLS.Get('BWRKSUM1-0026', 'SQL',' as of ')
--                             || stvterm_rec.stvterm_desc || '.' );
--            END IF;
--            twbkfrmt.P_TableRowClose;
--         twbkfrmt.P_TableClose;
--     END IF;                                                                                                            -- 80500-1
--  --
  <<Chk_Hold>>
     OPEN bwrkhold.GetFAHoldsC(pidm, aidy); -- 80301-1
    FETCH bwrkhold.GetFAHoldsC INTO rorhold_rec;
    IF  bwrkhold.GetFAHoldsC%NOTFOUND THEN
         CLOSE bwrkhold.GetFAHoldsC;
         GOTO Chk_Msgs;
     END IF;
     CLOSE bwrkhold.GetFAHoldsC;
     
               finAidHolds := '"finAidHolds":['
                       || '{"text":' || '"' || G\$_NLS.Get('BWRKSUM1-0028', 'SQL','Holds') ||
                                 G\$_NLS.Get('BWRKSUM1-0029', 'SQL', ' have been placed on your record which will prevent your application for financial aid from being processed.')
                                  || '"' || '}]';
                                  
----
--     IF twbkwbis.F_ValidLink('bwrkhold.P_DispHold') THEN                                                                -- 80500-1
--         twbkfrmt.P_TableOpen('PLAIN',
--             cattributes   => 'SUMMARY= "' ||
--                                 g\$_nls.get ('BWRKSUM1-0027',
--                                    'SQL',
--                                    'This layout table displays the link to holds page.'
--                                 ) ||
--                                 '"');
--            twbkfrmt.P_TableRowOpen;
--            -- 80300-1
--            twbkfrmt.P_tableData(F_GetActiveLink('bwrkelig.P_DisplayTabs?aidy_in=' || twbkfrmt.f_encode(aidy)
--                                              || '&tab_type=' || 'EH'|| '&calling_proc_name=' || 'bwrksumm.P_DispSumm',
--                                               G\$_NLS.Get('BWRKSUM1-0028', 'SQL','Holds')) ||
--                                 G\$_NLS.Get('BWRKSUM1-0029', 'SQL', ' have been placed on your record which will prevent your application for financial aid from being processed.'));
--            twbkfrmt.P_TableRowClose;
--         twbkfrmt.P_TableClose;
--     END IF;                                                                                                            -- 80500-1
--  --
  <<Chk_Msgs>>
--     OPEN bwrkamsg.GetAppMsgC(pidm,aidy);
--     FETCH bwrkamsg.GetAppMsgC INTO appmsg_rec;
--     IF  bwrkamsg.GetAppMsgC%NOTFOUND THEN
--         CLOSE bwrkamsg.GetAppMsgC;
--         GOTO Chk_Acct;
--     END IF;
--     CLOSE bwrkamsg.GetAppMsgC;  --5500-2
--  --
--     -- 80300-1
--     IF twbkwbis.F_ValidLink('bwrkamsg.P_FAAppMsg') THEN
--       twbkfrmt.P_TableOpen('PLAIN',
--           cattributes   => 'SUMMARY= "' ||
--                               g\$_nls.get ('BWRKSUM1-0030',
--                                  'SQL',
--                                  'This layout table displays the link to active messages page.'
--                               ) ||
--                               '"');
--          twbkfrmt.P_TableRowOpen;
--             twbkfrmt.P_tableData(G\$_NLS.Get('BWRKSUM1-0031', 'SQL',
--                                 'You have active ') ||
--                            twbkfrmt.F_PrintAnchor(twbkfrmt.f_encodeurl(
--                                          'bwrkamsg.P_FAAppMsg?aidy_in='
--                                        || twbkfrmt.f_encode(aidy)
--                                        || '&calling_proc_name=' ||
--                                          'bwrksumm.P_DispSumm')
--                                        ,  G\$_NLS.Get('BWRKSUM1-0032', 'SQL',
--                                          'messages'))
--                            || '.');
--          twbkfrmt.P_TableRowClose;
--       twbkfrmt.P_TableClose;
--     END IF;
  --
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
--           OPEN bwrklhst.GetAmtsC(pidm);                                                                              -- 8.23-1
--           FETCH bwrklhst.GetAmtsC INTO amounts_rec;                                                                  -- 8.23-1
           bwrklhst.p_get_disb_amts                                                                                     -- 8.23-1
                      ( p_pidm                =>  pidm,                                                                 -- 8.23-1
                        p_loan_rec            =>  detail_rec,                                                           -- 8.23-1
                        p_net_amt_out         =>  amounts_rec.rprladb_check_amt,                                        -- 8.23-1
                        p_net_amt_return_out  =>  amounts_rec.rprladb_check_return_amt                                  -- 8.23-1
                      ) ;                                                                                               -- 8.23-1
           IF amounts_rec.rprladb_check_amt IS NOT NULL OR
              amounts_rec.rprladb_check_amt != 0 THEN
              loans_paid := loans_paid + 1;
           END IF;
--           CLOSE bwrklhst.GetAmtsC;                                                                                   -- 8.23-1
     FETCH bwrklhst.GetLoanDtlC INTO detail_rec;
     END LOOP;
     CLOSE bwrklhst.GetLoanDtlC;
  --
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
  --
--    twbkfrmt.P_TableOpen('PLAIN',
--         cattributes   => 'SUMMARY= "' ||
--                             g\$_nls.get ('BWRKSUM1-0033',
--                                'SQL',
--                                'This layout table displays the link to account summary page.'
--                             ) ||
--                             '"');
--       twbkfrmt.P_TableRowOpen;
--       IF twbkwbis.F_ValidLink('bwskoacc.P_ViewAcct') THEN
--          twbkfrmt.P_tableData(G\$_NLS.Get('BWRKSUM1-0034', 'SQL',
--            'You have financial aid credits which appear within your ')
--                        || twbkfrmt.F_PrintAnchor(twbkfrmt.f_encodeurl(
--                          'bwskoacc.P_ViewAcct'),
--                         G\$_NLS.Get('BWRKSUM1-0035', 'SQL','account summary'))
--                        || '.');
--       ELSE
--          twbkfrmt.P_tableData(G\$_NLS.Get('BWRKSUM1-0036', 'SQL',
--                'You have financial aid credits which appear within your account summary')
--                        || '.');
--       END IF;
--       twbkfrmt.P_TableRowClose;
--    twbkfrmt.P_TableClose;

     account_summary := '"accountSummary":['
                        || '{"text":"' || G\$_NLS.Get('BWRKSUM1-0034', 'SQL', 'You have financial aid credits which appear within your ') || '"},'
                        || '{"text":"' || G\$_NLS.Get('BWRKSUM1-0035', 'SQL','account summary') || '", "url":"' || 'dummy' || '"},'
                        || '{"text":"' || '."}'
                        || ']'; 
  --
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
  --
  <<Disp_Hist>>
--    -- 80300-1 if link is NOT active do NOT display this message at all.
--    IF twbkwbis.F_ValidLink('bwrkrhst.P_DispAwdHst') THEN
--      twbkfrmt.P_TableOpen('PLAIN',
--           cattributes   => 'SUMMARY= "' ||
--                               g\$_nls.get ('BWRKSUM1-0037',
--                                  'SQL',
--                                  'This layout table displays the link to financial aid history.'
--                               ) ||
--                               '"');
--         twbkfrmt.P_TableRowOpen;
--            -- 80200-1
--            twbkfrmt.P_tableData(G\$_NLS.Get('BWRKSUM1-0038', 'SQL', 'View your ') ||
--                           twbkfrmt.F_PrintAnchor(twbkfrmt.f_encodeurl(
--                            'bwrkrhst.P_DispAwdHst'),
--                           G\$_NLS.Get('BWRKSUM1-0039', 'SQL',
--                            'financial aid history')) ||
--                           G\$_NLS.Get('BWRKSUM1-0040', 'SQL', '.'));
--         twbkfrmt.P_TableRowClose;
--      twbkfrmt.P_TableClose;
--    END IF;

     fin_aid_history := '"financialAidHistory":['
                        || '{"text":"' || G\$_NLS.Get('BWRKSUM1-0038', 'SQL', 'View your ') || '"},'
                        || '{"text":"' || G\$_NLS.Get('BWRKSUM1-0039', 'SQL', 'financial aid history') || '", "url":"' || 'dummy' || '"},'
                        || '{"text":"' || G\$_NLS.Get('BWRKSUM1-0040', 'SQL', '.') || '"}'
                        || ']'; 
  --
  <<End_Summ>>
--      htp.br;
--      twbkfrmt.p_printanchor (
--         curl          => twbkfrmt.f_encodeurl (
--                             twbkwbis.f_cgibin ||
--                                'bwrkolib.P_SelDefAidy?aidy=' ||
--                              twbkfrmt.f_encode(aidy) ||
--                                 '&' ||
--                              'calling_proc_name=' ||
--                              'bwrksumm.P_DispSumm'),
--         ctext         => g\$_nls.get ('BWRKSUM1-0041',
--                             'SQL',
--                             'Select Another Aid Year'
--                          ),
--         cattributes   => ' class = "whitespacelink"'
--      );
--     twbkwbis.P_CloseDoc(curr_release);

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
  IF finAidHolds IS NULL THEN
      finAidHolds := '"finAidHolds":null';
  END IF;
  IF unSatReq IS NULL THEN
      unSatReq := '"unSatReq":null';
  END IF;
  
  lv_finaid_json := '{' 
                    || cost_of_attendance || ','
                    || award_package      || ','
                    || account_summary    || ','
                    || fin_aid_history    || ','
                    || finAidHolds        || ','
                    || unSatReq
                    || '}';
  
  ? := lv_finaid_json;
  END P_DispSumm;

"""

}
