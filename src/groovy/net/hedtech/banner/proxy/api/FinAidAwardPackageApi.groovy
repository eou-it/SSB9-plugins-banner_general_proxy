/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.proxy.api

class FinAidAwardPackageApi {
    public final static String GET_NEED_CALCULATION = """
--PROCEDURE P_ShowNeedCalc(pidm  NUMBER,
--                         aidy  robinst.robinst_aidy_code%TYPE,
--                         WIDTH VARCHAR2) IS
declare
   pidm  NUMBER := ?;
   aidy  robinst.robinst_aidy_code%TYPE := ?;

   budget_amount   NUMBER := 0;
   efc             NUMBER := 0;
   init_need       NUMBER := 0;
   resource_amount NUMBER := 0;
   need            NUMBER := 0;

   lv_json VARCHAR2(32000);

   TYPE resources_rec IS RECORD(
      resource_desc rprarsc.rprarsc_resource_desc%TYPE,
      term_code     rprarsc.rprarsc_term_code%TYPE,
      est_amt       rprarsc.rprarsc_est_amt%TYPE,
      actual_amt    rprarsc.rprarsc_actual_amt%TYPE);

   resources_r resources_rec; -- 081000-1
   arsc_c      rokrefc.arsc_cur; -- 081000-1
   arsc_r      rokrefc.arsc_rec; -- 081000-1

   need_rec rnkneed.needrectype; -- 081401-2
   efc_ind  VARCHAR2(1); -- 081401-2

   CURSOR need_calc_c IS
   SELECT budget_amount,
         efc,
         budget_amount - efc init_need,
         need
   FROM  (SELECT NVL(rnvand0_budget_amount, 0) budget_amount,
                  --          FROM (SELECT NVL(bwrkbudg.f_get_budget_total(aidy, pidm), 0) BUDGET_AMOUNT,               -- 081200-1
                  NVL(CASE
                        WHEN rnvand0_efc_ind = 'I' THEN
                         rnvand0_im_efc_amt
                        ELSE
                         rnvand0_efc_amt
                      END,
                      0) efc,
                  NVL(CASE
                        WHEN rnvand0_efc_ind = 'I' THEN
                         rnvand0_im_gross_need
                        ELSE
                         rnvand0_gross_need
                      END,
                      0) need
           FROM   rnvand0
           WHERE  rnvand0_aidy_code = aidy
           AND    rnvand0_pidm = pidm) x;

   -- 081000-1
   CURSOR resources_c(pidm NUMBER,
                     aidy robinst.robinst_aidy_code%TYPE) IS
      SELECT rprarsc_resource_desc,
             rprarsc_term_code,
             rprarsc_est_amt, -- 081200-5
             rprarsc_actual_amt -- 081200-5
      FROM   rprarsc
      WHERE  rprarsc_pidm = pidm
      AND    rprarsc_info_access_ind = 'Y'
      AND    ((rprarsc_arsc_code IS NOT NULL AND
            rprarsc_info_access_ind =
            (SELECT rtvarsc_info_access_ind
                FROM   rtvarsc
                WHERE  rtvarsc_code = rprarsc_arsc_code
                AND    rtvarsc_info_access_ind = 'Y')) OR rprarsc_arsc_code IS NULL)
      AND    ((rprarsc_aidy_code = aidy AND rprarsc_term_code IS NULL) OR
            (rprarsc_term_code IS NOT NULL AND
            rprarsc_term_code IN
            (SELECT rorprds_term_code
                FROM   rorprds,
                       rortprd,
                       rorstat
                WHERE  rorstat_pidm = pidm
                AND    rorstat_aidy_code = aidy
                AND    rorprds_period = rortprd_period
                AND    rortprd_aidy_code = aidy
                AND    rortprd_aprd_code = rorstat_aprd_code)));

BEGIN
   -- 081401-2
   IF (rbk_period_budget.f_period_budget_enabled(aidy) = 'N')
   THEN
      OPEN need_calc_c;
      FETCH need_calc_c
      INTO budget_amount,
           efc,
           init_need,
           need;
      CLOSE need_calc_c;

      -- Other resources                                                         -- 081000-1
      OPEN resources_c(pidm, aidy);
      FETCH resources_c
      INTO resources_r;
      WHILE resources_c%FOUND LOOP
         resource_amount := resource_amount +
                           NVL(NVL(resources_r.actual_amt, resources_r.est_amt), 0); -- 081200-5
         FETCH resources_c
         INTO resources_r;
      END LOOP;
      CLOSE resources_c;

      -- Contracts and Exemptions                                                -- 081000-1
      rokrefc.p_get_arsc_data(p_resultset_inout => arsc_c,
                              p_aidy_code       => aidy,
                              p_pidm            => pidm);
      FETCH arsc_c
       INTO arsc_r;
      WHILE arsc_c%FOUND LOOP
        IF arsc_r.arsc_rec_info_access_ind = 'Y' THEN
          resource_amount := resource_amount + NVL(NVL(arsc_r.arsc_rec_actual_amt,
                                                       arsc_r.arsc_rec_estimated_amt),
                                                   0); -- 081200-5
        END IF;
        FETCH arsc_c
         INTO arsc_r;
      END LOOP;
      CLOSE arsc_c;

   ELSE
      efc_ind := bwrkbudg.f_get_budget_efc_ind(aidy, pidm);

      IF efc_ind IS NULL THEN
         RETURN;
      END IF;

      need_rec := rnkneed.f_get_need_data(pidm, aidy);

      resource_amount := need_rec.resource_amount;

      IF efc_ind = 'F' THEN
         budget_amount := need_rec.fm_budget_amount;
         efc           := need_rec.fm_efc_amt;
         init_need     := need_rec.fm_gross_need;
         need          := need_rec.fm_unmet_need;
      ELSIF efc_ind = 'I' THEN
         budget_amount := need_rec.im_budget_amount;
         efc           := need_rec.im_efc_amt;
         init_need     := need_rec.im_gross_need;
         need          := need_rec.im_unmet_need;
      END IF;
   END IF;

   --twbkfrmt.p_tableopen('DATADISPLAY',
   --                   cattributes  => rb_misc_parameter.f_get_data(p_parameter => 'HTML_TABLE_PARMS',
   --                                                                p_key       => 'bwrkrhst.P_ShowNeedCalc') || -- 081801-4
   --                                   'width="' || WIDTH || '" SUMMARY= "' ||
   --                                   g\$_nls.get('BWRKRHS1-0016',
   --                                              'SQL',
   --                                              'This table lists the need calculation.') || '"',
   --                   ccaption     => g\$_nls.get('BWRKRHS1-0017',
   --                                              'SQL',
   --                                              'Need Calculation'));
 /* 081401-1 Removed
      twbkfrmt.P_TableRowOpen;
      twbkfrmt.P_TableDataHeader(g\$_nls.get('BWRKRHS1-0019', 'SQL', 'Component'));
      twbkfrmt.P_TableDataHeader(g\$_nls.get('BWRKRHS1-0020', 'SQL', 'Amount'), 'right');
      twbkfrmt.P_TableRowClose;
 */
   lv_json := '{';
   --P_ShowCalcDetail(g\$_nls.get('BWRKRHS1-0018', 'SQL', 'Cost of Attendance'), budget_amount);
   lv_json := lv_json || '"attendanceCost": ' || nvl(budget_amount,0) || ',';
   --P_ShowCalcDetail(g\$_nls.get('BWRKRHS1-0019', 'SQL', 'Estimated Family Contribution'), efc);
   lv_json := lv_json || '"familyContrib": ' || nvl(efc,0) || ',';
   --P_ShowCalcDetail(g\$_nls.get('BWRKRHS1-0020', 'SQL', 'Initial Need'), init_need);
   lv_json := lv_json || '"initialNeed": ' || nvl(init_need,0) || ',';
   --P_ShowCalcDetail(g\$_nls.get('BWRKRHS1-0021', 'SQL', 'Outside Resource'), resource_amount);
   lv_json := lv_json || '"outsideResrc": ' || nvl(resource_amount,0) || ',';
   --P_ShowCalcDetail(g\$_nls.get('BWRKRHS1-0022', 'SQL', 'Need'), need);
   lv_json := lv_json || '"need": ' || nvl(need,0);
   lv_json := lv_json || '}';
-- twbkfrmt.p_tableclose;
-- htp.br;
   ? := lv_json;
end;
--END P_ShowNeedCalc;
"""

    public final static String GET_HOUSING_STATUS = """
--PROCEDURE P_ShowHousing(pidm  NUMBER,
--                       aidy  robinst.robinst_aidy_code%TYPE,
--                       WIDTH VARCHAR2) IS
declare
   pidm  NUMBER := ?;
   aidy  robinst.robinst_aidy_code%TYPE := ?;

   housing rormval.rormval_desc%TYPE;

   CURSOR housing_c IS
   SELECT rormval_desc
   FROM   rcrapp1,
          rormval
   WHERE  rcrapp1_aidy_code = aidy
   AND    rcrapp1_pidm = pidm
   AND    rcrapp1_curr_rec_ind = 'Y'
   AND    rcrapp1_aidy_code = rormval_key_1
   AND    rcrapp1_inst_hous_cde = rormval_code
   AND    rormval_column = 'RCRAPP1_INST_HOUS_CDE';

   lv_count      NUMBER;
   lv_house_json VARCHAR2(1000) := '{}';

   BEGIN
      OPEN housing_c;
      FETCH housing_c
      INTO housing;

      IF housing_c%FOUND
      THEN
--         twbkfrmt.p_tableopen('DATADISPLAY',
--                              cattributes  => rb_misc_parameter.f_get_data(p_parameter => 'HTML_TABLE_PARMS',
--                                                                           p_key       => 'bwrkrhst.P_ShowHousing') || -- 081801-4
--                                              'width="' || WIDTH || '" SUMMARY= "' ||
--                                              g\$_nls.get('BWRKRHS1-0025',
--                                                         'SQL',
--                                                         'This table lists the housing.') || '"',
--                              ccaption     => g\$_nls.get('BWRKRHS1-0026', 'SQL', 'Housing'));

--         twbkfrmt.p_tablerowopen;
--         twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0027', 'SQL', 'Status'));
--         twbkfrmt.p_tablerowclose;


         lv_house_json := '{"rows": [';
         lv_count := 0;
         WHILE housing_c%FOUND LOOP
--            twbkfrmt.p_tablerowopen;
--            twbkfrmt.p_tabledata(housing);
            lv_count := lv_count + 1;
            if lv_count > 1
            then
               lv_house_json := lv_house_json || ',';
            end if;
            lv_house_json := lv_house_json || '"' || housing || '"';
--            twbkfrmt.p_tablerowclose;
            FETCH housing_c
             INTO housing;
         END LOOP;
         lv_house_json := lv_house_json || ']}';

--         twbkfrmt.p_tableclose;
--         htp.br;
      END IF;
      CLOSE housing_c;
   ? := lv_house_json;
end;
--END P_ShowHousing;
"""

    public final static String GET_ENROLLMENT = """
--PROCEDURE P_ShowEnrollment(pidm   NUMBER,
--                          aidy   robinst.robinst_aidy_code%TYPE,
--                          WIDTH  VARCHAR2,
--                          status rorwebr.rorwebr_enrollment_status%TYPE) IS
declare
   pidm   NUMBER := ?;
   aidy   robinst.robinst_aidy_code%TYPE := ?;
   status rorwebr.rorwebr_enrollment_status%TYPE := ?;

   summer_t        rcrapp1.rcrapp1_rqst_fa_summer_this_yr%TYPE := NULL;
   fall            rcrapp1.rcrapp1_rqst_fa_fall_this_yr%TYPE := NULL;
   winter          rcrapp1.rcrapp1_rqst_fa_winter_next_yr%TYPE := NULL;
   spring          rcrapp1.rcrapp1_rqst_fa_spring_next_yr%TYPE := NULL;
   summer_n        rcrapp1.rcrapp1_rqst_fa_summer_next_yr%TYPE := NULL;
   est_enroll_pell rpbopts.rpbopts_est_enroll_pell_ind%TYPE := NULL;
   exp_enroll_stat rcrapp1.rcrapp1_exp_enroll_status%TYPE := NULL;
   default_option  rpbopts.rpbopts_default_option_ind%TYPE := NULL;

   lv_enroll_json VARCHAR2(32000);

   CURSOR period_c IS
     SELECT rcrapp1_rqst_fa_summer_this_yr,
            rcrapp1_rqst_fa_fall_this_yr,
            rcrapp1_rqst_fa_winter_next_yr,
            rcrapp1_rqst_fa_spring_next_yr,
            rcrapp1_rqst_fa_summer_next_yr,
            rcrapp1_exp_enroll_status
     FROM   rcrapp1
     WHERE  rcrapp1_aidy_code = aidy
     AND    rcrapp1_pidm = pidm
     AND    rcrapp1_curr_rec_ind = 'Y';

   CURSOR est_enroll_pell_c IS
     SELECT rpbopts_est_enroll_pell_ind,
            rpbopts_default_option_ind
     FROM   rpbopts
     WHERE  rpbopts_aidy_code = aidy;

   FUNCTION f_showenrollmentdesc(aidy   robinst.robinst_aidy_code%TYPE,
                                 period VARCHAR2,
                                 COLUMN VARCHAR2,
                                 CODE   VARCHAR2) RETURN VARCHAR2 IS

      period_desc rormval.rormval_desc%TYPE;

      CURSOR enrollment_desc_c IS
        SELECT rormval_desc
        FROM   rormval
        WHERE  rormval_key_1 = aidy
        AND    rormval_code = CODE
        AND    rormval_column = COLUMN;

      lv_enrollment_desc VARCHAR2(120);
   BEGIN
      OPEN enrollment_desc_c;
      FETCH enrollment_desc_c
        INTO period_desc;
      IF enrollment_desc_c%FOUND THEN
--         twbkfrmt.p_tablerowopen;
         IF period IS NOT NULL THEN
--           twbkfrmt.p_tabledata(period || ': ');
            lv_enrollment_desc := period || ':';
         END IF;
--         twbkfrmt.p_tabledata(period_desc);
         lv_enrollment_desc := lv_enrollment_desc || period_desc;
--         twbkfrmt.p_tablerowclose;
      END IF;
      CLOSE enrollment_desc_c;
      return lv_enrollment_desc;
   END f_showenrollmentdesc;
BEGIN
--   twbkfrmt.p_tableopen('DATADISPLAY',
--                        cattributes  => rb_misc_parameter.f_get_data(p_parameter => 'HTML_TABLE_PARMS',
--                                                                     p_key       => 'bwrkrhst.P_ShowEnrollment') || -- 081801-4
--                                        'width="' || WIDTH || '" SUMMARY= "' ||
--                                        g\$_nls.get('BWRKRHS1-0036',
--                                                   'SQL',
--                                                   'This table lists the expected enrollment status.') || '"',
--                        ccaption     => g\$_nls.get('BWRKRHS1-0037',
--                                                   'SQL',
--                                                   'Expected Enrollment'));

   IF rb_common.f_sel_robinst_aidy_end_year(aidy) >= 2015 THEN                                                   -- 8180101-1
      --P_ShowNewEnrollment                                                                                       -- 8180101-1
      --   ( pidm   =>  pidm,                                                                                     -- 8180101-1
      --     aidy   =>  aidy,                                                                                     -- 8180101-1
      --     status =>  status                                                                                    -- 8180101-1
      --   ) ;                                                                                                    -- 8180101-1
      GOTO get_out;                                                                                             -- 8180101-1
   END IF;                                                                                                       -- 8180101-1

   IF status = 'F' THEN
      lv_enroll_json := '{"fStatus": {';
--      twbkfrmt.p_tablerowopen;
--      twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0038', 'SQL', 'Status'));
--      twbkfrmt.p_tablerowclose;

      OPEN est_enroll_pell_c;
      FETCH est_enroll_pell_c
        INTO est_enroll_pell,
             default_option;
      CLOSE est_enroll_pell_c;

      IF est_enroll_pell = 'N' THEN
         -- 80201-3
--         IF default_option = '1' THEN
--           twbkfrmt.p_tabledata(g\$_nls.get('BWRKRHS1-0039', 'SQL', 'Full-Time'));
--         ELSIF default_option = '2' THEN
--           twbkfrmt.p_tabledata(g\$_nls.get('BWRKRHS1-0040', 'SQL', '3/4 Time'));
--         ELSIF default_option = '3' THEN
--           twbkfrmt.p_tabledata(g\$_nls.get('BWRKRHS1-0041', 'SQL', '1/2 Time'));
--         ELSIF default_option = '4' THEN
--           twbkfrmt.p_tabledata(g\$_nls.get('BWRKRHS1-0042', 'SQL', 'Less Than 1/2 Time'));
--         END IF;
         --
         lv_enroll_json := lv_enroll_json || '"dfltOption":"' || default_option || '"';
      ELSE
         -- est_enroll_pell = 'Y'
         OPEN period_c;
         FETCH period_c
           INTO summer_t,
                fall,
                winter,
                spring,
                summer_n,
                exp_enroll_stat;
         CLOSE period_c;
         IF exp_enroll_stat IS NOT NULL THEN
--            p_showenrollmentdesc(aidy, NULL, 'RCRAPP1_EXP_ENROLL_STATUS', exp_enroll_stat);
            lv_enroll_json := lv_enroll_json || '"status":"' ||
               f_showenrollmentdesc(aidy, NULL, 'RCRAPP1_EXP_ENROLL_STATUS', exp_enroll_stat) ||
               '"';
         ELSE
--            twbkfrmt.p_tablerowopen;
--            twbkfrmt.p_tabledata(g\$_nls.get('BWRKRHS1-0043', 'SQL', 'Unknown'));
            lv_enroll_json := lv_enroll_json || '"status":"unknown"';
--            twbkfrmt.p_tablerowclose;
         END IF;
      END IF;
      lv_enroll_json := lv_enroll_json || '}}';
     --
   ELSE
      lv_enroll_json := '{"tStatus": {"statuses": [';
            -- status = 'T'
            OPEN period_c;
            FETCH period_c
             INTO summer_t,
                  fall,
                  winter,
                  spring,
                  summer_n,
                  exp_enroll_stat;
            IF period_c%FOUND AND
                  (summer_t IS NOT NULL OR fall IS NOT NULL OR winter IS NOT NULL OR
                  spring IS NOT NULL OR summer_n IS NOT NULL) THEN

      --         twbkfrmt.p_tablerowopen;
      --         twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0044', 'SQL', 'Status'));
      --         twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0045', 'SQL', ''));
      --         twbkfrmt.p_tablerowclose;

      --         p_showenrollmentdesc(aidy,
      --                              g\$_nls.get('BWRKRHS1-0046', 'SQL', 'Summer'),
      --                              'RCRAPP1_RQST_FA_SUMMER_THIS_YR',
      --                              summer_t);
               lv_enroll_json := lv_enroll_json || '"' ||
                     f_showenrollmentdesc(aidy,
                                    'summer',
                                    'RCRAPP1_RQST_FA_SUMMER_THIS_YR',
                                    summer_t) || '",';
      --         p_showenrollmentdesc(aidy,
      --                              g\$_nls.get('BWRKRHS1-0047', 'SQL', 'Fall'),
      --                              'RCRAPP1_RQST_FA_FALL_THIS_YR',
      --                              fall);
               lv_enroll_json := lv_enroll_json || '"' ||
                     f_showenrollmentdesc(aidy,
                                    'fall',
                                    'RCRAPP1_RQST_FA_FALL_THIS_YR',
                                    fall) || '",';
      --         p_showenrollmentdesc(aidy,
      --                              g\$_nls.get('BWRKRHS1-0048', 'SQL', 'Winter'),
      --                              'RCRAPP1_RQST_FA_WINTER_NEXT_YR',
      --                              winter);
               lv_enroll_json := lv_enroll_json || '"' ||
                     f_showenrollmentdesc(aidy,
                                    'winter',
                                    'RCRAPP1_RQST_FA_WINTER_NEXT_YR',
                                    winter) || '",';
      --         p_showenrollmentdesc(aidy,
      --                              g\$_nls.get('BWRKRHS1-0049', 'SQL', 'Spring'),
      --                              'RCRAPP1_RQST_FA_SPRING_NEXT_YR',
      --                              spring);
               lv_enroll_json := lv_enroll_json || '"' ||
                     f_showenrollmentdesc(aidy,
                                    'spring',
                                    'RCRAPP1_RQST_FA_SPRING_NEXT_YR',
                                    spring) || '",';
      --         p_showenrollmentdesc(aidy,
      --                              g\$_nls.get('BWRKRHS1-0050', 'SQL', 'Summer'),
      --                              'RCRAPP1_RQST_FA_SUMMER_NEXT_YR',
      --                              summer_n);
               lv_enroll_json := lv_enroll_json || '"' ||
                     f_showenrollmentdesc(aidy,
                                    'nSummer',
                                    'RCRAPP1_RQST_FA_SUMMER_NEXT_YR',
                                    summer_n) || '"';
         --
      ELSE
--         twbkfrmt.p_tablerowopen;
--         twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0051', 'SQL', 'Status'));
--         twbkfrmt.p_tablerowclose;

--         twbkfrmt.p_tablerowopen;
--         twbkfrmt.p_tabledata(g\$_nls.get('BWRKRHS1-0052', 'SQL', 'Unknown'));
         lv_enroll_json := lv_enroll_json || '"status":"unknown"';
--         twbkfrmt.p_tablerowclose;
      END IF;

      CLOSE period_c;
      lv_enroll_json := lv_enroll_json || ']}}';
   END IF;

   << get_out >>
   ? := lv_enroll_json;
--   twbkfrmt.p_tableclose;
--   htp.br;
end;
--END P_ShowEnrollment;
"""

    public final static String GET_NEW_ENROLLMENT = """
--PROCEDURE P_ShowNewEnrollment
--           ( pidm   NUMBER,
--             aidy   robinst.robinst_aidy_code%TYPE,
--             status rorwebr.rorwebr_enrollment_status%TYPE
--           ) IS
declare
   pidm   NUMBER := ?;
   aidy   robinst.robinst_aidy_code%TYPE := ?;
   status rorwebr.rorwebr_enrollment_status%TYPE := ?;

   TYPE PeriodRecType IS RECORD                                                     -- 8180101-1
            ( period                               rorprst.rorprst_period%TYPE,    -- 8180101-1
              period_desc                          robprds.robprds_desc%TYPE,      -- 8180101-1
              xes                                  rorprst.rorprst_xes%TYPE,       -- 8180101-1
              xes_desc                             rormval.rormval_desc%TYPE       -- 8180101-1
            );                                                                     -- 8180101-1

   TYPE PeriodRecTab IS TABLE OF PeriodRecType;

   lv_year_xes         rorstat.rorstat_xes%TYPE;
   lv_year_desc        rormval.rormval_desc%TYPE;

   lv_PeriodRec        PeriodRecTab;

   lv_unknown          rormval.rormval_desc%TYPE := g\$_nls.get('BWRKRHS1-0028', 'SQL', 'Unknown');
   i                   PLS_INTEGER;

   est_enroll_pell     rpbopts.rpbopts_est_enroll_pell_ind%TYPE := NULL;
   default_option      rpbopts.rpbopts_default_option_ind%TYPE := NULL;

   lv_nenroll_json VARCHAR2(32000);

   CURSOR year_c IS
          SELECT rorstat_xes,
                 CASE   WHEN rorstat_xes IS NOT NULL THEN (SELECT rormval_desc
                                                             FROM rormval
                                                            WHERE rormval_column = 'EXP_ENROLL_STATUS'
                                                              AND rormval_code   = x.rorstat_xes
                                                          )
                       ELSE lv_unknown
                 END
            FROM rorstat x
           WHERE rorstat_aidy_code  = aidy
             AND rorstat_pidm       = pidm ;

   CURSOR period_c IS
          SELECT rorprst_period,
                 robprds_desc,
                 rorprst_xes,
                 CASE   WHEN rorprst_xes IS NOT NULL THEN (SELECT rormval_desc
                                                             FROM rormval
                                                            WHERE rormval_column = 'EXP_ENROLL_STATUS'
                                                              AND rormval_code   = x.rorprst_xes
                                                          )
                       ELSE lv_unknown
                 END
            FROM robprds,
                 rorprst x
           WHERE rorprst_period  = robprds_period
             AND rorprst_pidm    = pidm
             AND rorprst_period IN ( SELECT rortprd_period
                                       FROM rortprd,
                                            rorstat
                                      WHERE rorstat_aidy_code  = aidy
                                        AND rorstat_pidm       = pidm
                                        AND rortprd_aidy_code  = rorstat_aidy_code
                                        AND rortprd_aprd_code IN (rorstat_aprd_code,rorstat_aprd_code_pell)
                                   )
       ORDER BY robprds_seq_no ;

   CURSOR est_enroll_pell_c IS
          SELECT rpbopts_est_enroll_pell_ind,
                 rpbopts_default_option_ind
            FROM rpbopts
           WHERE rpbopts_aidy_code = aidy;

BEGIN
   IF status = 'F' THEN
--      twbkfrmt.p_tablerowopen;
--      twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0029', 'SQL', 'Status'));
--      twbkfrmt.p_tablerowclose;
      lv_nenroll_json := '{"fStatus":{';
      OPEN  est_enroll_pell_c;
      FETCH est_enroll_pell_c INTO est_enroll_pell,
                                  default_option;
      CLOSE est_enroll_pell_c;

      IF est_enroll_pell = 'N' THEN
         -- 80201-3
--         IF default_option = '1' THEN
--             twbkfrmt.p_tabledata(g\$_nls.get('BWRKRHS1-0030', 'SQL', 'Full-Time'));
--         ELSIF default_option = '2' THEN
--             twbkfrmt.p_tabledata(g\$_nls.get('BWRKRHS1-0031', 'SQL', '3/4 Time'));
--         ELSIF default_option = '3' THEN
--             twbkfrmt.p_tabledata(g\$_nls.get('BWRKRHS1-0032', 'SQL', '1/2 Time'));
--         ELSIF default_option = '4' THEN
--             twbkfrmt.p_tabledata(g\$_nls.get('BWRKRHS1-0033', 'SQL', 'Less Than 1/2 Time'));
--         END IF;
         lv_nenroll_json := lv_nenroll_json || '"status":"' || default_option || '"';
      ELSE
         -- est_enroll_pell = 'Y'
         OPEN  year_c;
         FETCH year_c INTO lv_year_xes,
                           lv_year_desc ;
         CLOSE year_c;
--         twbkfrmt.p_tablerowopen;
--         twbkfrmt.p_tabledata(lv_year_desc);
         lv_nenroll_json := lv_nenroll_json || '"status":"' || lv_year_desc || '"';
--         twbkfrmt.p_tablerowclose;
      END IF;
      lv_nenroll_json := lv_nenroll_json || '}}';
   ELSE
      -- status = 'T'
      lv_nenroll_json := '{"tStatus":{';
      OPEN  period_c;
      FETCH period_c BULK COLLECT INTO lv_PeriodRec;
      CLOSE period_c;

      IF lv_PeriodRec.COUNT = 0 THEN
--         twbkfrmt.p_tablerowopen;
--         twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0034', 'SQL', 'Status'));
--         twbkfrmt.p_tablerowclose;

--         twbkfrmt.p_tablerowopen;
--         twbkfrmt.p_tabledata(lv_unknown);
         lv_nenroll_json := lv_nenroll_json || '"statuses":["' || lv_unknown || '"]';
--         twbkfrmt.p_tablerowclose;
      ELSE
--         twbkfrmt.p_tablerowopen;
--         twbkfrmt.p_tabledataheader('');
--         twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0035', 'SQL', 'Status'));
--         twbkfrmt.p_tablerowclose;

         lv_nenroll_json := lv_nenroll_json || '"statuses":[';
         FOR i IN lv_PeriodRec.FIRST .. lv_PeriodRec.LAST
         LOOP
--            twbkfrmt.p_tablerowopen;
            if i > 1 then
               lv_nenroll_json := lv_nenroll_json || ',';
            end if;
            --twbkfrmt.p_tabledata(lv_PeriodRec(i).period_desc);
            --twbkfrmt.p_tabledata(lv_PeriodRec(i).xes_desc);
            lv_nenroll_json := lv_nenroll_json || '"' || lv_PeriodRec(i).period_desc || ':' || lv_PeriodRec(i).xes_desc || '"';
--            twbkfrmt.p_tablerowclose;
         END LOOP;
         lv_nenroll_json := lv_nenroll_json || ']';
      END IF;
      lv_nenroll_json := lv_nenroll_json || '}}';
   END IF;

   ? := lv_nenroll_json;
end;
--END P_ShowNewEnrollment;
"""

    public final static String GET_COST_OF_ATTENDANCE = """
--PROCEDURE p_showcoa(pidm  NUMBER,
--                    aidy  robinst.robinst_aidy_code%TYPE,
--                    WIDTH VARCHAR2) IS
declare
   pidm  NUMBER := ?;
   aidy  robinst.robinst_aidy_code%TYPE := ?;

   -- p_display_budget
   p_aidy_code rbracmp.rbracmp_aidy_code%TYPE;
   p_pidm      rbracmp.rbracmp_pidm%TYPE;
   p_caption   VARCHAR2(50);
   p_header    VARCHAR2(50);
   p_width     VARCHAR2(50) DEFAULT NULL;
   width_attribute VARCHAR2(50) := '';
   total           NUMBER       := 0;
   lv_BudgetTab    rbkpfrm.BudgetSummaryRecTab;


   lv_budg_json VARCHAR2(32000)  := '{}';

      -- 081401-2
   FUNCTION f_get_pbud_info_access_ind RETURN VARCHAR2 IS
         pbud_info_access_ind rorstat.rorstat_pbud_info_access_ind%TYPE;
   BEGIN
      SELECT rorstat_pbud_info_access_ind
      INTO   pbud_info_access_ind
      FROM   rorstat
      WHERE  rorstat_aidy_code = aidy
      AND    rorstat_pidm = pidm;
      RETURN pbud_info_access_ind;
   EXCEPTION
      WHEN NO_DATA_FOUND THEN
      RETURN 'N';
   END;
BEGIN
   -- 081401-01
   IF (rbk_period_budget.f_period_budget_enabled(aidy) = 'N') OR
      (f_get_pbud_info_access_ind = 'Y') THEN
--      bwrkbudg.p_display_budget(p_aidy_code => aidy,
--                               p_pidm      => pidm,
--                               p_caption   => g\$_nls.get('BWRKRHS1-0023',
--                                                         'SQL',
--                                                         'Cost of Attendance'), -- 081500-2
--                               p_header    => g\$_nls.get('BWRKRHS1-0024',
--                                                         'SQL',
--                                                         'Component'), -- 081500-2
--                               p_width     => WIDTH);
      p_aidy_code := aidy;
      p_pidm := pidm;
--      p_caption := 'Cost of Attendance';
--      p_header := 'Component';
--      p_width := WIDTH;

    --BEGIN p_display_budget
      IF rbk_period_budget.f_period_budget_enabled(p_aidy_code) = 'N' THEN
--         lv_BudgetTab := f_get_budget(p_aidy_code, p_pidm);
         lv_BudgetTab := bwrkbudg.f_get_budget(p_aidy_code, p_pidm);
      ELSE
--         lv_BudgetTab := rbkpfrm.f_get_budget(p_aidy_code, p_pidm, f_get_budget_efc_ind(p_aidy_code, p_pidm));
         lv_BudgetTab := rbkpfrm.f_get_budget(p_aidy_code, p_pidm, bwrkbudg.f_get_budget_efc_ind(p_aidy_code, p_pidm));
      END IF;

      IF lv_BudgetTab.COUNT > 0 THEN
--         IF p_width IS NOT NULL THEN
--            width_attribute := 'width="' || p_width;
--         END IF;

--         twbkfrmt.P_TableOpen('DATADISPLAY',
--                              cattributes => rb_misc_parameter.F_Get_Data(
--                                                        p_parameter => 'HTML_TABLE_PARMS',
--                                                        p_key       => 'bwrkbudg.P_DispBudg') ||                           -- 081401-1
--                                             width_attribute || '" SUMMARY= "' ||
--                                             g\$_nls.get('BWRKBUD1-0002', 'SQL', 'This table lists the cost of attendance.') || '"',
--                              ccaption    =>G\$_NLS.FormatMsg('x', 'SQL', p_caption));

         lv_budg_json := '{"budgets":[';
         FOR i IN lv_BudgetTab.FIRST .. lv_BudgetTab.LAST LOOP
--            twbkfrmt.P_TableRowOpen;
            if i > 1 then
               lv_budg_json := lv_budg_json || ',';
            end if;
--            twbkfrmt.P_TableData(lv_BudgetTab(i).description);
            lv_budg_json := lv_budg_json || '{"desc":"' || lv_BudgetTab(i).description ||'",';
--            twbkfrmt.P_TableData(to_char(lv_BudgetTab(i).amount, 'L999G999G999G999D99'), calign => 'right');                -- 081401-1
            lv_budg_json := lv_budg_json || '"amount":' || lv_BudgetTab(i).amount || '}';
--            twbkfrmt.P_TableRowClose;
            total := total + lv_BudgetTab(i).amount;
         END LOOP;
         lv_budg_json := lv_budg_json || '],';

         total := LEAST(total, 999999999999.99);

--         twbkfrmt.P_TableRowOpen;
--         twbkfrmt.P_TableDataLabel(g\$_nls.get('BWRKBUD1-0003', 'SQL', 'Total:'), ccolspan => '1');
--         twbkfrmt.P_TableData(to_char(total, 'L999G999G999G999D99'), 'right');
         lv_budg_json := lv_budg_json || '"total":' || total || '}';
--         twbkfrmt.P_TableRowClose;

--         twbkfrmt.P_TableClose;
--         htp.br;
      END IF;
    --END p_display_budget
   END IF;
   ? := lv_budg_json;
end;
--END p_showcoa;
"""

    public final static String GET_CUM_LOAN_INFO = """
--PROCEDURE P_ShowCumLoanInfo(pidm  NUMBER,
--                            aidy  robinst.robinst_aidy_code%TYPE,
--                            WIDTH VARCHAR2) IS
declare
   pidm  NUMBER := ?;
   aidy  robinst.robinst_aidy_code%TYPE := ?;
   WIDTH VARCHAR2 (30);

   agt_sub_total       rcrlds4.rcrlds4_agt_sub_total%TYPE := 0;
   agt_unsub_total     rcrlds4.rcrlds4_agt_unsub_total%TYPE := 0;
   agt_gr_plus_total   rcrlds4.rcrlds4_agt_gr_plus_total%TYPE := 0;
   agt_plus_total      rcrlds4.rcrlds4_agt_plus_total%TYPE := 0;
   perk_cumulative_amt rcrlds4.rcrlds4_perk_cumulative_amt%TYPE := 0;
   teach_loan_total    rcrlds4.rcrlds4_teach_loan_total%TYPE := 0; -- 80300-6
   proc_date           rcrlds4.rcrlds4_proc_date%TYPE := NULL;

   lv_loan_json VARCHAR2(32000);

   CURSOR loan_info_c IS
     SELECT NVL(rcrlds4_agt_sub_total, 0),
            NVL(rcrlds4_agt_unsub_total, 0),
            NVL(rcrlds4_agt_gr_plus_total, 0),
            NVL(rcrlds4_agt_plus_total, 0),
            NVL(rcrlds4_perk_cumulative_amt, 0),
            NVL(rcrlds4_teach_loan_total, 0), -- 80300-6
            rcrlds4_proc_date
     FROM   rcrlds4
     WHERE  rcrlds4_aidy_code = aidy
     AND    rcrlds4_pidm = pidm
     AND    rcrlds4_curr_rec_ind = 'Y';

BEGIN
   OPEN loan_info_c;
   FETCH loan_info_c
    INTO agt_sub_total,
         agt_unsub_total,
         agt_gr_plus_total,
         agt_plus_total,
         perk_cumulative_amt,
         teach_loan_total, -- 80300-6
         proc_date;

   lv_loan_json := '{';
   IF agt_sub_total <> 0 OR
      agt_unsub_total <> 0 OR
      agt_gr_plus_total <> 0 OR
      agt_plus_total <> 0 OR
      perk_cumulative_amt <> 0 OR
      teach_loan_total <> 0 THEN
      -- 80300-6

--      twbkfrmt.p_tableopen('DATADISPLAY',
--                           cattributes  => rb_misc_parameter.f_get_data(p_parameter => 'HTML_TABLE_PARMS',
--                                                                        p_key       => 'bwrkrhst.P_ShowCumLoanInfo') || -- 081801-4
--                                           'width="' || WIDTH || '" SUMMARY= "' ||
--                                           g\$_nls.get('BWRKRHS1-0053',
--                                                      'SQL',
--                                                      'This table lists the cumulative loan information.') || '"',
--                           ccaption     => g\$_nls.get('BWRKRHS1-0054',
--                                                      'SQL',
--                                                      'Cumulative Loan Information as of<br>') ||
--                                           to_char(proc_date, g\$_date.get_nls_date_format));

      --twbkfrmt.p_tablerowopen;
      --twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0055', 'SQL', 'Loan Type'));
      --twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0056', 'SQL', 'Amount'), 'right');
      --twbkfrmt.p_tablerowclose;

--      P_ShowCumLoanDetail(g\$_nls.get('BWRKRHS1-0057', 'SQL', 'Subsidized'),
--                          agt_sub_total);
      lv_loan_json := lv_loan_json || '"subsidized":"' || agt_sub_total || '",';
--      P_ShowCumLoanDetail(g\$_nls.get('BWRKRHS1-0058', 'SQL', 'Unsubsidized'),
--                          agt_unsub_total);
      lv_loan_json := lv_loan_json || '"unsubsidized":"' || agt_unsub_total || '",';
--      P_ShowCumLoanDetail(g\$_nls.get('BWRKRHS1-0059', 'SQL', 'Graduate PLUS'),
--                          agt_gr_plus_total);
      lv_loan_json := lv_loan_json || '"gradPlus":"' || agt_gr_plus_total || '",';
--      P_ShowCumLoanDetail(g\$_nls.get('BWRKRHS1-0060', 'SQL', 'Parent PLUS'),
--                          agt_plus_total);
      lv_loan_json := lv_loan_json || '"parentPlus":"' || agt_plus_total || '",';
--      P_ShowCumLoanDetail(g\$_nls.get('BWRKRHS1-0061', 'SQL', 'Perkins'),
--                          perk_cumulative_amt);
      lv_loan_json := lv_loan_json || '"perkins":"' || perk_cumulative_amt || '",';
--      P_ShowCumLoanDetail(g\$_nls.get('BWRKRHS1-0062',
--                                     'SQL',
--                                     'Direct Unsubsidized (TEACH)'),
--                          teach_loan_total); -- 80300-6
      lv_loan_json := lv_loan_json || '"directUnsub":"' || teach_loan_total || '"';

--      twbkfrmt.p_tableclose;
--      htp.br;
   END IF;
   lv_loan_json := lv_loan_json || '}';
   CLOSE loan_info_c;

   ? := lv_loan_json;
end;
--END P_ShowCumLoanInfo;
"""

    public final static String GET_AWARD_INFO = """
--P_ShowAwardInfo
declare
   pidm   NUMBER := ?;
   aidy   robinst.robinst_aidy_code%TYPE := ?;

   lv_award_json VARCHAR2(32000) := '{}';
   lv_period_json VARCHAR2(32000):= '{}';


   g_unscheduled VARCHAR2(50) := g\$_nls.get('BWRKRHS1-0000', 'SQL', 'Unscheduled');
   info_access      VARCHAR2(1) := 'N';

   lv_unscheduled_long VARCHAR2(100) := g\$_nls.get('BWRKRHS1-0076', 'SQL', 'year that has not been scheduled for specific term(s)');

   rorwebr_rec rorwebr%ROWTYPE ;

   CURSOR award_detail_c IS
    SELECT rpratrm_aidy_code     awrd_aidy_code,
           rpratrm_period        period,
           robprds_desc          period_desc,
           rprawrd_fund_code     fund_code,
           rfrbase_fund_title    fund_title,
           a.rtvawst_desc        status,
           a.rtvawst_offer_ind   status_offer_ind,
           a.rtvawst_accept_ind  status_accept_ind,
           a.rtvawst_decline_ind status_decline_ind,
           a.rtvawst_cancel_ind  status_cancel_ind,
           NVL(rpratrm_offer_amt,0)     amt,
           NVL(rpratrm_offer_amt,0)     period_offer_amt,
           rpratrm_accept_amt           period_accept_amt,
           rpratrm_decline_amt          period_decline_amt,
           rpratrm_cancel_amt           period_cancel_amt,
           robprds_seq_no        period_seq_no,
           rfrbase_print_seq_no  print_seq_no,
           p.rtvawst_desc        period_status,
           p.rtvawst_offer_ind   period_status_offer_ind,
           p.rtvawst_accept_ind  period_status_accept_ind
    FROM   rprawrd,
           rpratrm,
           rtvawst a, -- rprawrd awst
           robprds,
           rfrbase,
           rtvawst p  -- rpratrm awst                                                                         -- 081801-1
    WHERE  p.rtvawst_code                    = rpratrm_awst_code                                              -- 081801-1
    AND    p.rtvawst_info_access_ind         = 'Y'                                                            -- 081801-1
--  AND    rprawrd_pidm                      = pidm                                                           -- 8.23-1
    AND    rprawrd_awst_code                 = a.rtvawst_code
    AND    NVL(rprawrd_info_access_ind, 'Y') = 'Y'
    AND    rfrbase_info_access_ind           = 'Y'
    AND    rprawrd_fund_code                 = rfrbase_fund_code
    AND    a.rtvawst_info_access_ind         = 'Y'
    AND    rpratrm_pidm                      = pidm                                                           -- 8.23-1
    AND    rprawrd_aidy_code                 = rpratrm_aidy_code
    AND    rprawrd_pidm                      = rpratrm_pidm
    AND    rprawrd_fund_code                 = rpratrm_fund_code
    AND    robprds_period                    = rpratrm_period
    AND    (
             ( NVL(rfrbase_fed_fund_id, '*') = 'PELL'                                                         -- 081000-1
    AND        rpratrm_offer_amt > 0                                                                          -- 081000-1
    AND        bwrkolib.f_checkpellcrossover                                                                  -- 081000-1
                          ( aidy,                                                                             -- 081000-1
                            pidm,                                                                             -- 081000-1
                            rprawrd_aidy_code,                                                                -- 081000-1
                            rpratrm_period                                                                    -- 081000-1
                          ) = 'Y'                                                                             -- 081000-1
             )                                                                                                -- 081000-1
     OR      ( NVL(rfrbase_fed_fund_id, '*') <> 'PELL'                                                        -- 081000-1
--     AND       rprawrd_aidy_code = aidy                                                                     -- 081000-1
     AND       ( ( rpratrm_bbay_code IS NULL                                                                  -- 8.23-1
     AND           rpratrm_aidy_code = aidy                                                                   -- 8.23-1
                 )                                                                                            -- 8.23-1
      OR         ( rpratrm_bbay_code IS NOT NULL                                                              -- 8.23-1
     AND           NVL(rpratrm_aidy_code_funds,                                                               -- 8.23-1
                       rpratrm_aidy_code)       = aidy                                                        -- 8.23-1
                 )
               )
     AND       ( ( rorwebr_rec.rorwebr_term_zero_awrd_ind = 'N'
     AND           rpratrm_offer_amt > 0
                 )
      OR         ( rorwebr_rec.rorwebr_term_zero_awrd_ind = 'Y'
                 )
               )
             )
           )
    UNION ALL
    SELECT rprawrd_aidy_code,
           '~',
           g_unscheduled,
           rprawrd_fund_code,
           rfrbase_fund_title,
           rtvawst_desc,
           rtvawst_offer_ind,
           rtvawst_accept_ind,
           rtvawst_decline_ind,
           rtvawst_cancel_ind,
           NVL(rprawrd_offer_amt,0),
           NVL(rprawrd_offer_amt,0),
           rprawrd_accept_amt,
           rprawrd_decline_amt,
           rprawrd_cancel_amt,
           99999999,
           rfrbase_print_seq_no,
           rtvawst_desc,
           '~',
           '~'
    FROM   rprawrd,
           rtvawst,
           rfrbase
    WHERE  rprawrd_aidy_code = aidy
    AND    rprawrd_pidm = pidm
    AND    rprawrd_awst_code = rtvawst_code
    AND    NVL(rprawrd_info_access_ind, 'Y') = 'Y'
    AND    rfrbase_info_access_ind = 'Y'
    AND    rprawrd_fund_code = rfrbase_fund_code
    AND    rtvawst_info_access_ind = 'Y'
    AND    NOT EXISTS
           (SELECT 'X'
            FROM   rpratrm
            WHERE  rprawrd_aidy_code = rpratrm_aidy_code
            AND    rprawrd_pidm = rpratrm_pidm
            AND    rprawrd_fund_code = rpratrm_fund_code)
    AND    ((rorwebr_rec.rorwebr_fund_zero_amt_ind = 'N' AND rprawrd_offer_amt > 0) OR rorwebr_rec.rorwebr_fund_zero_amt_ind = 'Y')
    ORDER  BY print_seq_no,
              fund_code,
              period_seq_no ;

   TYPE period_award_tab_type IS TABLE OF award_detail_c%ROWTYPE INDEX BY BINARY_INTEGER ;

  TYPE fund_data_rec IS RECORD (
    fund_code rprawrd.rprawrd_fund_code%TYPE,
    fund_title rfrbase.rfrbase_fund_title%TYPE,
    periods period_award_tab_type  -- array of fund/period records, index by period_seq_no
  ) ;

  TYPE fund_data_tab_type IS TABLE OF fund_data_rec INDEX BY BINARY_INTEGER ;
  -- index by binary_integer here because we need the records to be retrievable in the
  -- same order we put them in

  TYPE list_type IS TABLE OF robprds.robprds_desc%TYPE INDEX BY BINARY_INTEGER ;

   lv_period_list list_type ;
   lv_award_data fund_data_tab_type ;

   FUNCTION f_getinfoaccess(pidm NUMBER,
                           aidy robinst.robinst_aidy_code%TYPE) RETURN VARCHAR2 IS

    lv_info_access VARCHAR2(1) := 'N';

    CURSOR getinfoaccess_c IS
      SELECT NVL(rorstat_info_access_ind, rorwebr_null_infoaccess_ind)
      FROM   rorstat,
             rorwebr
      WHERE  rorstat_pidm = pidm
      AND    rorstat_aidy_code = aidy
      AND    rorstat_aidy_code = rorwebr_aidy_code;

  BEGIN
    OPEN getinfoaccess_c;
    FETCH getinfoaccess_c
      INTO lv_info_access;
    CLOSE getinfoaccess_c;

    RETURN lv_info_access;
  END f_getinfoaccess;

  PROCEDURE P_Sel_rorwebr(aidy robinst.robinst_aidy_code%TYPE) IS

    CURSOR get_rorwebr_c( p_aidy_code rorwebr.rorwebr_aidy_code%TYPE ) IS
      SELECT *
      FROM   rorwebr
      WHERE  rorwebr_aidy_code = p_aidy_code ;

  BEGIN
    OPEN get_rorwebr_c( p_aidy_code => aidy ) ;
    FETCH get_rorwebr_c INTO rorwebr_rec ;
    CLOSE get_rorwebr_c ;

    info_access := f_getinfoaccess(pidm, aidy);
  END P_Sel_rorwebr;


FUNCTION f_no_split_period
             ( p_aidy_code              rpratrm.rpratrm_aidy_code%TYPE,
               p_pi                     BINARY_INTEGER,
               p_award_data             fund_data_tab_type
    --         ) RETURN indicator_type IS
             ) RETURN VARCHAR2 IS
    lv_return_ind                       VARCHAR2(1);

    lv_fi                               BINARY_INTEGER ;     -- fund_data index

  BEGIN

    lv_return_ind  :=  'N';

    lv_fi := p_award_data.FIRST ;
    WHILE lv_fi IS NOT NULL LOOP
        IF p_award_data(lv_fi).periods.EXISTS(p_pi) THEN -- period data exists
            IF NVL(p_award_data(lv_fi).periods(p_pi).awrd_aidy_code,p_aidy_code) <> p_aidy_code THEN
                lv_return_ind :=  'Y';
            END IF ;
        END IF ;

        lv_fi := p_award_data.NEXT(lv_fi) ;

     END LOOP ;

    RETURN lv_return_ind;

  END f_no_split_period ;


FUNCTION f_award_exists(pidm     NUMBER,
                          aidy     robinst.robinst_aidy_code%TYPE,
                          webaccpt VARCHAR2 DEFAULT 'N') RETURN BOOLEAN IS

    dummy        VARCHAR2(1);
    return_value BOOLEAN := TRUE;

    -- This cursor is used to test if they have awards. If they don't
    -- have a packing group they can't have any awards.
    CURSOR getpackgroup_c IS
      SELECT 'X'
      FROM   rorstat,
             rorwebr
      WHERE  rorstat_pidm = pidm
      AND    rorstat_aidy_code = aidy
      AND    rorstat_aidy_code = rorwebr_aidy_code
      AND    NVL(rorstat_info_access_ind, rorwebr_null_infoaccess_ind) = 'Y'
      AND    ((rorstat_pgrp_code IS NOT NULL AND
            rorstat_pgrp_code IN
            (SELECT rtvpgrp_code FROM rtvpgrp WHERE rtvpgrp_info_access_ind = 'Y')) OR
            rorstat_pgrp_code IS NULL);

  CURSOR getawddtl_c IS
    SELECT 'X'
    FROM   rprawrd,
           rpratrm,
           rtvawst,
           robprds,
           rfrbase
    WHERE  rprawrd_pidm = pidm
    AND    rprawrd_awst_code = rtvawst_code
    AND    NVL(rprawrd_info_access_ind, 'Y') = 'Y'
    AND    rfrbase_info_access_ind = 'Y'
    AND    rprawrd_fund_code = rfrbase_fund_code
    AND    rtvawst_info_access_ind = 'Y'
    AND    rprawrd_aidy_code = rpratrm_aidy_code
    AND    rprawrd_pidm = rpratrm_pidm
    AND    rprawrd_fund_code = rpratrm_fund_code
    AND    robprds_period = rpratrm_period
    AND    (
             ( NVL(rfrbase_fed_fund_id, '*') = 'PELL'                                                         -- 081000-1
    AND        rpratrm_offer_amt > 0                                                                          -- 081000-1
    AND        bwrkolib.f_checkpellcrossover                                                                  -- 081000-1
                          ( aidy,                                                                             -- 081000-1
                            pidm,                                                                             -- 081000-1
                            rprawrd_aidy_code,                                                                -- 081000-1
                            rpratrm_period                                                                    -- 081000-1
                          ) = 'Y'                                                                             -- 081000-1
             )                                                                                                -- 081000-1
     OR      ( NVL(rfrbase_fed_fund_id, '*') <> 'PELL'                                                        -- 081000-1
--     AND       rprawrd_aidy_code = aidy                                                                     -- 081000-1
     AND       ( ( rpratrm_bbay_code IS NULL                                                                  -- 8.23-1
     AND           rpratrm_aidy_code = aidy                                                                   -- 8.23-1
                 )                                                                                            -- 8.23-1
      OR         ( rpratrm_bbay_code IS NOT NULL                                                              -- 8.23-1
     AND           NVL(rpratrm_aidy_code_funds,                                                               -- 8.23-1
                       rpratrm_aidy_code)       = aidy                                                        -- 8.23-1
                 )
               )
     AND       (
                 ( rorwebr_rec.rorwebr_term_zero_awrd_ind = 'N'
     AND           rpratrm_offer_amt > 0
                 )
      OR           rorwebr_rec.rorwebr_term_zero_awrd_ind = 'Y'
               )
     AND       (
                 ( rorwebr_rec.rorwebr_fund_zero_amt_ind = 'N'
     AND           rprawrd_offer_amt > 0
                 )
      OR         rorwebr_rec.rorwebr_fund_zero_amt_ind = 'Y'
               )
             )
           )
    UNION ALL
    SELECT 'X'
    FROM   rprawrd,
           rtvawst,
           rfrbase
    WHERE  rprawrd_aidy_code = aidy
    AND    rprawrd_pidm = pidm
    AND    rprawrd_awst_code = rtvawst_code
    AND    NVL(rprawrd_info_access_ind, 'Y') = 'Y'
    AND    rfrbase_info_access_ind = 'Y'
    AND    rprawrd_fund_code = rfrbase_fund_code
    AND    rtvawst_info_access_ind = 'Y'
    AND    NOT EXISTS
     (SELECT 'X'
            FROM   rpratrm
            WHERE  rprawrd_aidy_code = rpratrm_aidy_code
            AND    rprawrd_pidm = rpratrm_pidm
            AND    rprawrd_fund_code = rpratrm_fund_code)
    AND    ((rorwebr_rec.rorwebr_fund_zero_amt_ind = 'N' AND rprawrd_offer_amt > 0) OR rorwebr_rec.rorwebr_fund_zero_amt_ind = 'Y');
    /* 081500-1
            SELECT 'X'
              FROM RPRAWRD,
                   RTVAWST,
                   RFRBASE  -- 80300-4
             WHERE RPRAWRD_PIDM      = pidm
               AND RPRAWRD_AWST_CODE = RTVAWST_CODE
               AND NVL(RPRAWRD_INFO_ACCESS_IND, 'Y') = 'Y'
               AND RPRAWRD_FUND_CODE = RFRBASE_FUND_CODE -- 80300-4
               AND RFRBASE_INFO_ACCESS_IND = 'Y'
               AND RTVAWST_INFO_ACCESS_IND = 'Y'
               AND (RPRAWRD_FUND_CODE IN
                     (SELECT RPRATRM_FUND_CODE
                        FROM RPRATRM,
                             ROBPRDS                                                -- 080900-1
                       WHERE 1=1 -- ROBPRDS_AIDY_CODE = aidy                        -- 081201-1
                         AND ROBPRDS_PERIOD    = RPRATRM_PERIOD
                         AND RPRATRM_AIDY_CODE = RPRAWRD_AIDY_CODE
                         AND RPRATRM_PIDM      = RPRAWRD_PIDM
                         AND RPRATRM_FUND_CODE = RPRAWRD_FUND_CODE
                         AND (  (   NVL(RFRBASE_FED_FUND_ID, '*')  = 'PELL'         -- 081000-1
                                AND RPRATRM_OFFER_AMT > 0                           -- 081000-1
                                AND bwrkolib.F_CheckPellCrossover(                  -- 081000-1
                                       aidy,                                        -- 081000-1
                                       pidm,                                        -- 081000-1
                                       RPRAWRD_AIDY_CODE,                           -- 081000-1
                                       RPRATRM_PERIOD) = 'Y'                        -- 081000-1
                                )                                                   -- 081000-1
                             OR (   NVL(RFRBASE_FED_FUND_ID, '*') <> 'PELL'         -- 081000-1
                                AND RPRAWRD_AIDY_CODE = aidy                        -- 081000-1
                                AND (  (term_zero_awrd = 'N' AND RPRATRM_OFFER_AMT > 0)
                                    OR  term_zero_awrd = 'Y')
                                )
                             )
                     )
                   OR NOT EXISTS -- 071401-2
                     (SELECT 'X'
                        FROM RPRATRM
                       WHERE RPRAWRD_AIDY_CODE = RPRATRM_AIDY_CODE
                         AND RPRAWRD_PIDM      = RPRATRM_PIDM
                         AND RPRAWRD_FUND_CODE = RPRATRM_FUND_CODE
                     )
                   )
               AND ( (fund_zero_amt  = 'N' AND RPRAWRD_OFFER_AMT > 0)
                   OR fund_zero_amt  = 'Y')
               AND ( (webAccpt = 'Y' AND F_AllowWebUpdate(pidm, aidy, RPRAWRD_FUND_CODE) = 'Y')
                   OR webAccpt = 'N');
    */
  BEGIN
    -- Check the packing group.If they don't have a group,they don't have awards.
    OPEN getpackgroup_c;
    FETCH getpackgroup_c
      INTO dummy;
    IF getpackgroup_c%NOTFOUND THEN
      return_value := FALSE;
    END IF;
    CLOSE getpackgroup_c;

    -- So they have a packing group, then check to see if there are awards out there.
    OPEN getawddtl_c;
    FETCH getawddtl_c
      INTO dummy;
    IF getawddtl_c%NOTFOUND THEN
      return_value := FALSE;
    END IF;
    CLOSE getawddtl_c;

    RETURN return_value;
  END f_award_exists;

PROCEDURE P_GetAwardData( p_award_data   OUT fund_data_tab_type,
                            p_period_list  OUT list_type )    IS
    lv_fund_indx BINARY_INTEGER := 0 ;
    lv_last_fund_code rprawrd.rprawrd_fund_code%TYPE ;
  BEGIN
    -- Builds a table of unique periods in a student's award package (p_period_list)
    -- Also builds an award detail data structure that looks something like this:
    /*
    award data table (indexed sequentially from 1, orderd by print_seq_no+fund_code)
      -> period award data table (indexed by robprds_seq_no)

    A data example might look like this:

    award_data(1)  -- these indexes are sequential, in the order that funds should appear (print_seq_no+fund_code)
      fund_code  => 'PELL'
      fund_title => 'Federal PELL Grant'
      periods(100) =>    -- simce this is indexed by robprds_seq_no, it can skip values based on the actual data in the tables
          award_detail_c%ROWTYPE: period      => 'FALL_2012'
                                  period_desc => 'Fall 2012'
                                  fund_code   => 'PELL'
                                  fund_title  => 'Federal PELL Grant'
                                  status      => 'ACPT'
                                  ...
      periods(101) =>
          award_detail_c%ROWTYPE: period      => 'SPRING_2013'
                                  period_desc => 'Spring 2013'
                                  fund_code   => 'PELL'
                                  fund_title  => 'Federal PELL Grant'
                                  status      => 'ACPT'
                                  ...
    award_data(2)
      fund_code  => 'STFD'
      fund_title => 'Stafford Loan'
      periods(101) =>
      periods(101) =>
          award_detail_c%ROWTYPE: period      => 'SPRING_2013'
                                  period_desc => 'Spring 2013'
                                  fund_code   => 'STFD'
                                  fund_title  => 'Stafford Loan'
                                  status      => 'ACPT'
                                  ...
    award_data(3)
      ...
    */

    FOR i IN award_detail_c LOOP
      -- only create a new fund record if the fund code has changed
      IF lv_last_fund_code != i.fund_code THEN
        lv_fund_indx := lv_fund_indx + 1 ;
      END IF ;
      lv_last_fund_code := i.fund_code ;

      -- Set members of fund_data_rec
      p_award_data( lv_fund_indx ).fund_code := i.fund_code ;
      p_award_data( lv_fund_indx ).fund_title := i.fund_title ;
      p_award_data( lv_fund_indx ).periods( i.period_seq_no ) := i ;

      -- Build the unique list of periods, indexed by period_seq_no
      p_period_list(i.period_seq_no) := i.period_desc ;
    END LOOP ;

  END P_GetAwardData ;


   -- Draws a table for period awards for the specified period
   --PROCEDURE P_Show_PA_PeriodBlock( period_seq_no robprds.robprds_seq_no%TYPE ) IS
   PROCEDURE P_Show_PA_PeriodBlock( period_seq_no robprds.robprds_seq_no%TYPE,
         lv_period_json IN OUT VARCHAR2) IS
      lv_fi BINARY_INTEGER ;
      lv_total_amt NUMBER ;
      lv_term_desc VARCHAR2(2000) ;
      lv_count NUMBER := 0;                                                                                     -- 8.23-3
   BEGIN
      IF lv_period_list(period_seq_no) = g_unscheduled THEN
         lv_term_desc := lv_unscheduled_long ;
      ELSE
      --   lv_term_desc := lv_period_list(period_seq_no) ;

         IF f_no_split_period                                                                                          -- 8.23-3
              ( p_aidy_code     =>  aidy,                                                                              -- 8.23-3
                p_pi            =>  period_seq_no,                                                                     -- 8.23-3
                p_award_data    =>  lv_award_data                                                                      -- 8.23-3
              ) = 'Y' THEN                                                                                             -- 8.23-3

            --lv_term_desc  :=  G\$_NLS.FormatMsg('x', 'SQL',                                                          -- 8.23-3
            --                                f_getinfotext('bwrkrhst.P_AwardOverviewTab', 'BBAY_DESC'),                 -- 8.23-3
            --                                lv_period_list(period_seq_no));                                            -- 8.23-3
            lv_term_desc := lv_period_list(period_seq_no);
         ELSE                                                                                                          -- 8.23-3
            --lv_term_desc  :=  G\$_NLS.FormatMsg('x', 'SQL',                                                           -- 8.23-3
            --                               f_getinfotext('bwrkrhst.P_AwardOverviewTab', 'TERM_DESC'),                  -- 8.23-3
            --                               lv_period_list(period_seq_no));                                             -- 8.23-3
            lv_term_desc := lv_period_list(period_seq_no);
         END IF;                                                                                                        -- 8.23-3
      END IF ;

      lv_period_json := lv_period_json || '{';
      --twbkfrmt.p_tableopen('DATADISPLAY',
      --                     cattributes  => rb_misc_parameter.f_get_data(p_parameter => 'HTML_TABLE_PARMS',
      --                                                                  p_key       => 'bwrkrhst.P_AcceptAwardOfferTab') || -- 080500-4
      --                                     'width="' || WIDTH || '" SUMMARY= "' ||
      --                                     g\$_nls.get('BWRKRHS1-0084',
      --                                                'SQL',
      --                                                'This table lists the award information.') || '"',
      --                     ccaption     => lv_term_desc);                                                               -- 8.23-3
   --                         ccaption     => g\$_nls.get('BWRKRHS1-0084',                                                  -- 8.23-3
   --                                                    'SQL',                                                            -- 8.23-3
   --                                                    'Financial Aid Award for the %01%',                               -- 8.23-3
   --                                                    lv_term_desc ));
      lv_period_json := lv_period_json || '"termDesc":"' || lv_term_desc || '",';
      -- First row of header
      --twbkfrmt.p_tablerowopen;
      --twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0085', 'SQL', 'Fund'));
      --twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0086', 'SQL', 'Status'));
      --twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0087', 'SQL', 'Total'));
      --twbkfrmt.p_tablerowclose;

      lv_period_json := lv_period_json || '"periodAwards": [';
      lv_fi := lv_award_data.FIRST ;
      WHILE lv_fi IS NOT NULL LOOP
         --twbkfrmt.p_tablerowopen;

         IF lv_award_data(lv_fi).periods.EXISTS( period_seq_no ) THEN
            lv_count := lv_count + 1;
            if lv_count > 1 then
               lv_period_json := lv_period_json || ',';
            end if;
            --P_PrintCell_FundTitle( p_fund_code => lv_award_data(lv_fi).fund_code,
            --                       p_fund_title => lv_award_data(lv_fi).fund_title,
            --                       p_aidy_code => aidy ) ;
            lv_period_json := lv_period_json || '{"fundTitle":"' || lv_award_data(lv_fi).fund_title || '",';

            --twbkfrmt.p_tabledata( lv_award_data(lv_fi).periods(period_seq_no).period_status ) ;
            lv_period_json := lv_period_json || '"status":"' || lv_award_data(lv_fi).periods(period_seq_no).period_status || '",';
            --twbkfrmt.p_tabledata( TO_CHAR(lv_award_data(lv_fi).periods(period_seq_no).amt, 'L999G999G999D99'),
            --                      calign => 'right' ) ;
            lv_period_json := lv_period_json || '"amount":' || nvl(lv_award_data(lv_fi).periods(period_seq_no).amt,0) || '}';
            --twbkfrmt.p_tablerowclose;

            lv_total_amt := NVL(lv_total_amt,0) + lv_award_data(lv_fi).periods(period_seq_no).amt ;
         END IF ;
         lv_fi := lv_award_data.NEXT(lv_fi) ;
      END LOOP ;
      lv_period_json := lv_period_json || '],';

      --twbkfrmt.p_tabledata(g\$_nls.get('BWRKRHS1-0088', 'SQL', 'Total'), cattributes=>'style="font-weight: bold;"');
      --htp.print('<td></td>') ;
      --twbkfrmt.p_tabledata( TO_CHAR( lv_total_amt, 'L999G999G999D99'),
      --                               calign => 'right');  -- grand total for the period
      lv_period_json := lv_period_json || '"total": ' || nvl(lv_total_amt,0) || '}';

      --twbkfrmt.p_tablerowclose;
      --twbkfrmt.p_tableclose;
   END P_Show_PA_PeriodBlock ;


   --PROCEDURE P_Show_PA_V IS
   FUNCTION P_Show_PA_V RETURN VARCHAR2 IS
      lv_pi BINARY_INTEGER ;
      lv_periods_json VARCHAR2(32000);
      lv_count NUMBER := 0;
   BEGIN
      lv_pi := lv_period_list.FIRST ;
      lv_periods_json := '{"periods":[';
      WHILE lv_pi IS NOT NULL LOOP
         lv_count := lv_count + 1;
         if lv_count > 1 then
            lv_periods_json := lv_periods_json || ',';
         end if;
         --P_Show_PA_PeriodBlock(lv_pi) ;
         P_Show_PA_PeriodBlock(lv_pi, lv_periods_json) ;
         --htp.br ;
         lv_pi := lv_period_list.NEXT(lv_pi) ;
      END LOOP ;
      lv_periods_json := lv_periods_json || ']}';

      return lv_periods_json;
   END P_Show_PA_V ;

   PROCEDURE P_Calc_AwardAidyAmtStatus( p_fi                 BINARY_INTEGER,
                                        p_out_amt        OUT NUMBER,
                                        p_out_status_img OUT VARCHAR2,
                                        lv_offer_amt     OUT NUMBER,
                                        lv_accept_amt    OUT NUMBER,
                                        lv_decline_amt   OUT NUMBER,
                                        lv_cancel_amt    OUT NUMBER) IS
      lv_pi BINARY_INTEGER ;
      lv_offered BOOLEAN := FALSE ;
      lv_accepted BOOLEAN := FALSE ;
      lv_declined BOOLEAN := FALSE ;
      lv_cancelled BOOLEAN := FALSE ;
   BEGIN
      lv_offer_amt := NULL ;
      lv_accept_amt  := NULL ;
      lv_decline_amt := NULL ;
      lv_cancel_amt  := NULL ;

      lv_pi := lv_award_data(p_fi).periods.FIRST ;
      WHILE lv_pi IS NOT NULL LOOP
         p_out_amt := NVL(p_out_amt,0) + NVL(lv_award_data(p_fi).periods(lv_pi).amt,0) ;

         -- Sum up the individual column amounts ofrd/acpt/decl/cncl
         IF lv_award_data(p_fi).periods(lv_pi).period_offer_amt IS NOT NULL THEN
            lv_offer_amt := NVL(lv_offer_amt,0) + lv_award_data(p_fi).periods(lv_pi).period_offer_amt ;
         END IF ;

         IF lv_award_data(p_fi).periods(lv_pi).period_accept_amt IS NOT NULL THEN
            lv_accept_amt := NVL(lv_accept_amt,0) + lv_award_data(p_fi).periods(lv_pi).period_accept_amt ;
         END IF ;

         IF lv_award_data(p_fi).periods(lv_pi).period_decline_amt IS NOT NULL THEN
            lv_decline_amt := NVL(lv_decline_amt,0) + lv_award_data(p_fi).periods(lv_pi).period_decline_amt ;
         END IF ;

         IF lv_award_data(p_fi).periods(lv_pi).period_cancel_amt IS NOT NULL THEN
            lv_cancel_amt := NVL(lv_cancel_amt,0) + lv_award_data(p_fi).periods(lv_pi).period_cancel_amt ;
         END IF ;

         -- Set ofrd/acpt/decl/cncl booleans so we can calculate the status image to display later
         IF lv_award_data(p_fi).periods(lv_pi).period_status_offer_ind = 'Y' OR
               (lv_award_data(p_fi).periods(lv_pi).period = '~' AND lv_award_data(p_fi).periods(lv_pi).status_offer_ind = 'Y') THEN
            lv_offered := TRUE ;
         END IF ;

         IF lv_award_data(p_fi).periods(lv_pi).period_status_accept_ind = 'Y' OR
               (lv_award_data(p_fi).periods(lv_pi).period = '~' AND lv_award_data(p_fi).periods(lv_pi).status_accept_ind = 'Y') THEN
            lv_accepted := TRUE ;
         END IF ;

         IF lv_award_data(p_fi).periods(lv_pi).status_decline_ind = 'Y' THEN
            lv_declined := TRUE ;
         END IF ;

         IF lv_award_data(p_fi).periods(lv_pi).status_cancel_ind = 'Y' THEN
            lv_cancelled := TRUE ;
         END IF ;

         lv_pi := lv_award_data(p_fi).periods.NEXT(lv_pi) ;
      END LOOP ;

      IF lv_offered = TRUE THEN
         --IF rorwebr_rec.rorwebr_offer_image IS NOT NULL THEN
            p_out_status_img := 'offer' ;
         --END IF ;
      ELSIF lv_accepted = TRUE THEN
         --IF rorwebr_rec.rorwebr_accept_image IS NOT NULL THEN
            p_out_status_img := 'accept' ;
         --END IF ;
      ELSIF lv_declined = TRUE THEN
         --IF rorwebr_rec.rorwebr_decline_image IS NOT NULL THEN
            p_out_status_img := 'decline' ;
         --END IF ;
      ELSIF lv_cancelled = TRUE THEN
         --IF rorwebr_rec.rorwebr_cancel_image IS NOT NULL THEN
            p_out_status_img := 'cancel' ;
         --END IF ;
      END IF ;
   END P_Calc_AwardAidyAmtStatus ;


   PROCEDURE P_Show_AA(lv_aidyr_json IN OUT VARCHAR2) IS
      lv_fi BINARY_INTEGER ;
      lv_amt NUMBER := 0 ;
      lv_total_amt NUMBER ;
      lv_term_desc VARCHAR2(100) ;
      lv_status_img rorwebr.rorwebr_decline_image%TYPE ;
      lv_offer_amt NUMBER ;
      lv_accept_amt  NUMBER ;
      lv_decline_amt NUMBER ;
      lv_cancel_amt  NUMBER ;
      lv_total_offer_amt NUMBER ;
      lv_total_accept_amt  NUMBER ;
      lv_total_decline_amt NUMBER ;
      lv_total_cancel_amt  NUMBER ;

      lv_count NUMBER := 0;
   BEGIN
      --twbkfrmt.p_tableopen('DATADISPLAY',
      --                     cattributes  => rb_misc_parameter.f_get_data(p_parameter => 'HTML_TABLE_PARMS',
      --                                                                  p_key       => 'bwrkrhst.P_AcceptAwardOfferTab') || -- 080500-4
      --                                     'width="' || WIDTH || '" SUMMARY= "' ||
      --                                     g\$_nls.get('BWRKRHS1-0089',
      --                                                'SQL',
      --                                                'This table lists the award information.') || '"',
      --                     ccaption     => g\$_nls.get('BWRKRHS1-0090',
      --                                                'SQL',
      --                                                'Financial Aid Award for the %01%',
      --                                                bwrkolib.f_validaidy(aidy) ));
      lv_aidyr_json := lv_aidyr_json || '{';
      -- Header
      --twbkfrmt.p_tablerowopen;
      --twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0091', 'SQL', 'Fund'));
      --twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0092', 'SQL', 'Status'));
      --IF rorwebr_rec.rorwebr_aidy_award_ind = 'D' THEN  -- then also display ofrd/acpt/decl/cncl amounts
      --  twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0093', 'SQL', 'Offered'));
      --  twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0094', 'SQL', 'Accepted'));
      --  IF rorwebr_rec.rorwebr_fund_zero_amt_ind != 'N' THEN
      --    twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0095', 'SQL', 'Declined'));
      --    twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0096', 'SQL', 'Cancelled'));
      --  END IF ;
      --END IF ;
      --twbkfrmt.p_tabledataheader(g\$_nls.get('BWRKRHS1-0097', 'SQL', 'Total'));
      --twbkfrmt.p_tablerowclose;

      lv_aidyr_json := lv_aidyr_json || '"aidAwards":[';
      lv_fi := lv_award_data.FIRST ;
      WHILE lv_fi IS NOT NULL LOOP
         --twbkfrmt.p_tablerowopen;
         lv_count := lv_count + 1;
         if lv_count > 1 then
            lv_aidyr_json := lv_aidyr_json || ',';
         end if;
         lv_aidyr_json := lv_aidyr_json || '{';
         --P_PrintCell_FundTitle( p_fund_code => lv_award_data(lv_fi).fund_code,
         --                       p_fund_title => lv_award_data(lv_fi).fund_title,
         --                       p_aidy_code => aidy ) ;
         lv_aidyr_json := lv_aidyr_json || '"fundTitle":"' || lv_award_data(lv_fi).fund_title || '"';
         P_Calc_AwardAidyAmtStatus( lv_fi, lv_amt, lv_status_img,
                                   lv_offer_amt, lv_accept_amt, lv_decline_amt,
                                   lv_cancel_amt ) ;
         --twbkfrmt.p_tabledata( lv_status_img ) ;
         lv_aidyr_json := lv_aidyr_json || ',"status":"' || lv_status_img || '"';


         IF rorwebr_rec.rorwebr_aidy_award_ind = 'D' THEN  -- then also display ofrd/acpt/decl/cncl amounts
            --twbkfrmt.p_tabledata( TO_CHAR( lv_offer_amt, 'L999G999G999D99'),
            --                      calign => 'right' ) ;
            lv_aidyr_json := lv_aidyr_json || ',"offerAmt":' || nvl(lv_offer_amt,0) || ',';
            --twbkfrmt.p_tabledata( TO_CHAR( lv_accept_amt, 'L999G999G999D99'),
            --                      calign => 'right' ) ;
            lv_aidyr_json := lv_aidyr_json || '"acceptAmt":' || nvl(lv_accept_amt,0) || '';
            IF rorwebr_rec.rorwebr_fund_zero_amt_ind != 'N' THEN
               --twbkfrmt.p_tabledata( TO_CHAR( lv_decline_amt, 'L999G999G999D99'),
               --                      calign => 'right' ) ;
               lv_aidyr_json := lv_aidyr_json || ',"declineAmt":' || nvl(lv_decline_amt,0) || ',';
               --twbkfrmt.p_tabledata( TO_CHAR( lv_cancel_amt, 'L999G999G999D99'),
               --                    calign => 'right' ) ;
               lv_aidyr_json := lv_aidyr_json || '"cancelAmt":' || nvl(lv_cancel_amt,0) || '';
            END IF ;

            lv_total_offer_amt := NVL(lv_total_offer_amt,0) + lv_offer_amt ;

            IF lv_accept_amt IS NOT NULL THEN
               lv_total_accept_amt := NVL(lv_total_accept_amt,0) + lv_accept_amt ;
            END IF ;

            IF lv_decline_amt IS NOT NULL THEN
               lv_total_decline_amt := NVL(lv_total_decline_amt,0) + lv_decline_amt ;
            END IF ;

            IF lv_cancel_amt IS NOT NULL THEN
               lv_total_cancel_amt := NVL(lv_total_cancel_amt,0) + lv_cancel_amt ;
            END IF ;

         END IF ;

         --twbkfrmt.p_tabledata( TO_CHAR( lv_amt, 'L999G999G999D99'),
         --                      calign => 'right' ) ;
         lv_aidyr_json := lv_aidyr_json || ',"amount":' || nvl(lv_amt,0);
         --twbkfrmt.p_tablerowclose;
         lv_aidyr_json := lv_aidyr_json || '}';

         lv_total_amt := NVL(lv_total_amt,0) + lv_amt ;

         lv_amt := 0 ;
         lv_fi := lv_award_data.NEXT(lv_fi) ;
      END LOOP ;
      lv_aidyr_json := lv_aidyr_json || ']';

      --twbkfrmt.p_tabledata(g\$_nls.get('BWRKRHS1-0098', 'SQL', 'Total'), cattributes=>'style="font-weight: bold;"');
      --htp.print('<td></td>') ;

      IF rorwebr_rec.rorwebr_aidy_award_ind = 'D' THEN  -- then also display ofrd/acpt/decl/cncl amounts
         --twbkfrmt.p_tabledata( TO_CHAR( lv_total_offer_amt, 'L999G999G999D99'),
         --                      calign => 'right',cattributes=>'style="font-weight: bold;"' ) ;
         lv_aidyr_json := lv_aidyr_json || ',"totalOfferAmt":' || nvl(lv_total_offer_amt,0) || ',';
         --twbkfrmt.p_tabledata( TO_CHAR( lv_total_accept_amt, 'L999G999G999D99'),
         --                      calign => 'right',cattributes=>'style="font-weight: bold;"' ) ;
         lv_aidyr_json := lv_aidyr_json || '"totalAcceptAmt":' || nvl(lv_total_accept_amt,0);
         IF rorwebr_rec.rorwebr_fund_zero_amt_ind != 'N' THEN
            --twbkfrmt.p_tabledata( TO_CHAR( lv_total_decline_amt, 'L999G999G999D99'),
            --                     calign => 'right',cattributes=>'style="font-weight: bold;"' ) ;
            lv_aidyr_json := lv_aidyr_json || ',"totalDeclineAmt":' || nvl(lv_total_decline_amt,0) || ',';
            --twbkfrmt.p_tabledata( TO_CHAR( lv_total_cancel_amt, 'L999G999G999D99'),
            --                     calign => 'right',cattributes=>'style="font-weight: bold;"' ) ;
            lv_aidyr_json := lv_aidyr_json || '"totalCancelAmt":' || nvl(lv_total_cancel_amt,0);
         END IF ;
      END IF ;

      --twbkfrmt.p_tabledata( TO_CHAR( lv_total_amt, 'L999G999G999D99'),
      --                      calign => 'right',cattributes=>'style="font-weight: bold;"');  -- grand total for the year
      lv_aidyr_json := lv_aidyr_json || ',"totalAmt":' || nvl(lv_total_amt,0);
      --twbkfrmt.p_tablerowclose;

      --twbkfrmt.p_tableclose;
      lv_aidyr_json := lv_aidyr_json || '}';
   END P_Show_AA ;

BEGIN
   --bwrkolib.P_ProcessUserDefTxt( pidm, aidy, 'FA' ) ;
   P_Sel_rorwebr(aidy);

   -- Check for no awards
   IF NOT f_award_exists(pidm, aidy) OR info_access = 'N' THEN
      lv_award_json := '{}';
      lv_period_json := '{}';
   --   RETURN;
   -- END IF;
   else

   P_GetAwardData( lv_award_data, lv_period_list ) ;

   IF rorwebr_rec.rorwebr_aidy_award_ind IN ('D','S') THEN
     -- The logic for D and S Aidyear Awards is so similar that
     -- they are both handled by the same procedure
     --P_Show_AA ;  -- Show Aidyear Awards
     lv_award_json := '{"aidYearAwards":';
     P_Show_AA(lv_award_json);
     lv_award_json := lv_award_json || '}';
   END IF ; -- IF rorwebr_rec.rorwebr_aidy_award_ind = 'N' or anything else, don't display anything

   -- determine if we need a spacer - only if one of each table is shown
   --IF rorwebr_rec.rorwebr_aidy_award_ind != 'N' AND
   --   rorwebr_rec.rorwebr_prds_award_ind != 'N' THEN
   --  htp.br ;
   --END IF ;

   --IF rorwebr_rec.rorwebr_prds_award_ind = 'H' THEN
   --  P_Show_PA_H ;  -- Show Period Awards - Horizontal
   --ELSIF rorwebr_rec.rorwebr_prds_award_ind = 'V' THEN
   --  P_Show_PA_V ;  -- Show Period Awards - Vertical
   --ELSIF rorwebr_rec.rorwebr_prds_award_ind = 'D' THEN
   --  P_Show_PA_D ;  -- Show Period Awards - Double
   --END IF ; -- IF rorwebr_rec.rorwebr_prds_award_ind = 'N' or anything else, don't display anything

   lv_period_json := P_Show_PA_V;
   end if;
   --htp.br;

   --EXCEPTION WHEN OTHERS THEN
   --  htp.p('<hr><font size="+3"><bold>' || SQLERRM || '<br><pre>' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE || '</pre></font><hr>') ;
--END P_ShowAwardInfo;
   ? := lv_award_json;
   ? := lv_period_json;
END;
--P_ShowAwardInfo
"""

    public final static String GET_RORWEBRREC = """
--PROCEDURE P_Sel_rorwebr(aidy robinst.robinst_aidy_code%TYPE) IS
declare
   pidm NUMBER := ?;
   aidy robinst.robinst_aidy_code%TYPE := ?;

   lv_rorwebr_json VARCHAR(32000) := '{}';

   rorwebr_rec rorwebr%ROWTYPE ;

   CURSOR get_rorwebr_c( p_aidy_code rorwebr.rorwebr_aidy_code%TYPE ) IS
   SELECT *
   FROM   rorwebr
   WHERE  rorwebr_aidy_code = p_aidy_code ;

   FUNCTION f_getinfoaccess(pidm NUMBER,
                           aidy robinst.robinst_aidy_code%TYPE) RETURN VARCHAR2 IS

      lv_info_access VARCHAR2(1) := 'N';

      CURSOR getinfoaccess_c IS
         SELECT NVL(rorstat_info_access_ind, rorwebr_null_infoaccess_ind)
         FROM   rorstat,
                rorwebr
         WHERE  rorstat_pidm = pidm
         AND    rorstat_aidy_code = aidy
         AND    rorstat_aidy_code = rorwebr_aidy_code;

   BEGIN
      OPEN getinfoaccess_c;
      FETCH getinfoaccess_c
        INTO lv_info_access;
      CLOSE getinfoaccess_c;

      RETURN lv_info_access;
   END f_getinfoaccess;

BEGIN
   OPEN get_rorwebr_c( p_aidy_code => aidy ) ;
   FETCH get_rorwebr_c INTO rorwebr_rec ;
   CLOSE get_rorwebr_c ;

   lv_rorwebr_json := '{';
   lv_rorwebr_json := lv_rorwebr_json || '"need_calc_ind":"' || rorwebr_rec.rorwebr_need_calc_ind || '",';
   lv_rorwebr_json := lv_rorwebr_json || '"housing_status_ind":"' || rorwebr_rec.rorwebr_housing_status_ind || '",';
   lv_rorwebr_json := lv_rorwebr_json || '"enrollment_status":"' || rorwebr_rec.rorwebr_enrollment_status || '",';
   lv_rorwebr_json := lv_rorwebr_json || '"coa_ind":"' || rorwebr_rec.rorwebr_coa_ind || '",';
   lv_rorwebr_json := lv_rorwebr_json || '"cum_loan_ind":"' || rorwebr_rec.rorwebr_cum_loan_ind || '",';
   lv_rorwebr_json := lv_rorwebr_json || '"term_zero_awrd_ind":"' || rorwebr_rec.rorwebr_term_zero_awrd_ind || '",';
   lv_rorwebr_json := lv_rorwebr_json || '"fund_zero_amt_ind":"' || rorwebr_rec.rorwebr_fund_zero_amt_ind || '",';
   lv_rorwebr_json := lv_rorwebr_json || '"info_access":"' || f_getinfoaccess(pidm, aidy) || '",';
   lv_rorwebr_json := lv_rorwebr_json || '"aidYearDesc":"' || bwrkolib.f_validaidy(aidy) || '",';
   lv_rorwebr_json := lv_rorwebr_json || '"aid_year":' ||rb_common.f_sel_robinst_aidy_end_year(aidy);
   lv_rorwebr_json := lv_rorwebr_json || '}';

   ? := lv_rorwebr_json;

end;
--END P_Sel_rorwebr;
"""
}
