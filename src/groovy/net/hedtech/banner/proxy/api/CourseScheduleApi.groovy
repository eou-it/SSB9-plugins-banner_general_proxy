package net.hedtech.banner.proxy.api

class CourseScheduleApi {

    public final static String WEEKLY_COURSE_SCHEDULE = """
--
-- P_CRSESCHD.
-- Displays the Week at a glance page.
-- ==================================================
--   PROCEDURE P_CrseSchd (
--      start_date_in   IN   VARCHAR2 DEFAULT NULL,
--      error_msg_in    IN   VARCHAR2 DEFAULT NULL,
--      error_date_in   IN   VARCHAR2 DEFAULT NULL
--   )
--   IS
declare
--
-- Cursor to read meeting records.
-- ==================================================
      CURSOR sfvstumc (
         pidm_in         NUMBER,
         start_date_in   DATE DEFAULT NULL,
         end_date_in     DATE DEFAULT NULL
      )
      IS
         SELECT SFRSTCR.SFRSTCR_TERM_CODE term_code,
                SSBSECT.SSBSECT_CRN crn,
                SSBSECT.SSBSECT_SUBJ_CODE subj_code,
                SSBSECT.SSBSECT_CRSE_NUMB crse_numb,
                SSBSECT.SSBSECT_SEQ_NUMB seq_numb,
                SSRMEET.SSRMEET_MON_DAY mon_day,
                SSRMEET.SSRMEET_TUE_DAY tue_day,
                SSRMEET.SSRMEET_WED_DAY wed_day,
                SSRMEET.SSRMEET_THU_DAY thu_day,
                SSRMEET.SSRMEET_FRI_DAY fri_day,
                SSRMEET.SSRMEET_SAT_DAY sat_day,
                SSRMEET.SSRMEET_SUN_DAY sun_day, SSRMEET.SSRMEET_START_DATE,
                SSRMEET.SSRMEET_END_DATE,
                SSBSECT.SSBSECT_PTRM_START_DATE,
                SSBSECT.SSBSECT_PTRM_END_DATE,
                SSRMEET.SSRMEET_BEGIN_TIME begin_time,
                SSRMEET.SSRMEET_END_TIME end_time,
                SSRMEET.SSRMEET_BLDG_CODE bldg_code,
                SSRMEET.SSRMEET_ROOM_CODE room_code,
                SSRMEET.SSRMEET_MTYP_CODE mtyp_code, NULL gtvmtyp_desc
           FROM SSRMEET, SSBSECT, SFRSTCR, STVRSTS, SOBTERM
          WHERE SSBSECT.SSBSECT_TERM_CODE = SFRSTCR.SFRSTCR_TERM_CODE
            AND SSBSECT.SSBSECT_CRN = SFRSTCR.SFRSTCR_CRN
            AND SFRSTCR_RSTS_CODE = STVRSTS_CODE
            AND STVRSTS_SB_PRINT_IND = 'Y'
            AND STVRSTS_WAIT_IND = 'N'
            AND STVRSTS_WITHDRAW_IND = 'N'
            AND SSRMEET.SSRMEET_TERM_CODE = SFRSTCR.SFRSTCR_TERM_CODE
            AND SSRMEET.SSRMEET_CRN = SFRSTCR.SFRSTCR_CRN
            AND (
                      (
                             SFRSTCR.SFRSTCR_ERROR_FLAG <> 'D'
                         AND SFRSTCR.SFRSTCR_ERROR_FLAG <> 'F'
                      )
                   OR SFRSTCR.SFRSTCR_ERROR_FLAG IS NULL
                )
            AND SFRSTCR.SFRSTCR_PIDM = pidm_in
            AND SFRSTCR.SFRSTCR_TERM_CODE = sobterm_term_code
            AND (
                      SSRMEET.SSRMEET_START_DATE BETWEEN start_date_in
                          AND end_date_in
                   OR SSRMEET.SSRMEET_END_DATE BETWEEN start_date_in
                          AND end_date_in
                   OR start_date_in BETWEEN SSRMEET.SSRMEET_START_DATE
                          AND SSRMEET.SSRMEET_END_DATE
                   OR end_date_in BETWEEN SSRMEET.SSRMEET_START_DATE
                          AND SSRMEET.SSRMEET_END_DATE
                )
            AND SSRMEET.SSRMEET_BEGIN_TIME IS NOT NULL
            AND SSRMEET.SSRMEET_END_TIME IS NOT NULL
            AND (
                      SSRMEET.SSRMEET_MON_DAY IS NOT NULL
                   OR SSRMEET.SSRMEET_TUE_DAY IS NOT NULL
                   OR SSRMEET.SSRMEET_WED_DAY IS NOT NULL
                   OR SSRMEET.SSRMEET_THU_DAY IS NOT NULL
                   OR SSRMEET.SSRMEET_FRI_DAY IS NOT NULL
                   OR SSRMEET.SSRMEET_SAT_DAY IS NOT NULL
                   OR SSRMEET.SSRMEET_SUN_DAY IS NOT NULL
                )
            AND sobterm_dynamic_sched_term_ind = 'Y'
          ORDER BY SSRMEET.SSRMEET_BEGIN_TIME,
                   SSRMEET.SSRMEET_END_TIME,
                   SSRMEET.SSRMEET_MON_DAY,
                   SSRMEET.SSRMEET_TUE_DAY,
                   SSRMEET.SSRMEET_WED_DAY,
                   SSRMEET.SSRMEET_THU_DAY,
                   SSRMEET.SSRMEET_FRI_DAY,
                   SSRMEET.SSRMEET_SAT_DAY,
                   SSRMEET.SSRMEET_SUN_DAY;

      CURSOR sfvstareg (
         pidm_in         NUMBER,
         term_in         VARCHAR2,
         crn_in          VARCHAR2
      )
      IS
         SELECT SFRAREG_START_DATE,
                SFRAREG_COMPLETION_DATE
           FROM SFRAREG
          WHERE SFRAREG_PIDM = pidm_in
            AND SFRAREG_CRN = crn_in
            AND SFRAREG_TERM_CODE = term_in
            AND (
                      EXISTS (SELECT 'X'
                                FROM STVRSTS B
                               WHERE B.STVRSTS_CODE = SFRAREG_RSTS_CODE
                                 AND B.STVRSTS_WITHDRAW_IND = 'N')
                   OR NOT EXISTS (SELECT 'Y'
                                    FROM STVRSTS C
                                   WHERE C.STVRSTS_CODE = SFRAREG_RSTS_CODE)
                );

      sfvstareg_rec sfvstareg%ROWTYPE;

--
-- Classes that do not have meeting times in the current
-- template.
-- ==================================================
      CURSOR nonschdc (
         pidm_in         NUMBER
      )
      IS
         SELECT DISTINCT ssbsect_subj_code subj_code,
                         ssbsect_crse_numb crse_numb,
                         ssbsect_seq_numb seq_numb, sfrstcr_crn crn,
                         sfrstcr_term_code term_code, stvschd_desc,
                         ssbsect_ptrm_code ptrm_code,
                         ssbsect_ptrm_start_date,
                         ssbsect_ptrm_end_date
           FROM ssbsect, sfrstcr, stvrsts, sobterm, stvschd
          WHERE sfrstcr_pidm = pidm_in
            AND sfrstcr_term_code = sobterm_term_code
            AND ssbsect_term_code = sfrstcr_term_code
            AND NVL(sfrstcr_error_flag,'N') <> 'F'
            AND ssbsect_crn = sfrstcr_crn
            AND (
                      NOT EXISTS (SELECT 1
                                    FROM ssrmeet
                                   WHERE ssrmeet_crn = sfrstcr_crn
                                     AND ssrmeet_term_code = sfrstcr_term_code)
                   OR EXISTS (SELECT 1
                                FROM ssrmeet
                               WHERE ssrmeet_crn = sfrstcr_crn
                                 AND ssrmeet_term_code = sfrstcr_term_code
                                 AND ssrmeet_mon_day IS NULL
                                 AND ssrmeet_tue_day IS NULL
                                 AND ssrmeet_wed_day IS NULL
                                 AND ssrmeet_thu_day IS NULL
                                 AND ssrmeet_fri_day IS NULL
                                 AND ssrmeet_sat_day IS NULL
                                 AND ssrmeet_sun_day IS NULL
                                 AND ssrmeet_begin_time IS NULL
                                 AND ssrmeet_end_time IS NULL)
                )
            AND ssbsect_schd_code = stvschd_code
            AND stvrsts_code = sfrstcr_rsts_code
            AND stvrsts_sb_print_ind = 'Y'
            AND stvrsts_wait_ind = 'N'
            AND stvrsts_withdraw_ind = 'N'
            AND sobterm_dynamic_sched_term_ind = 'Y';

      CURSOR gtvmtypc (mtyp_code_in VARCHAR2)
      IS
         SELECT gtvmtyp_desc
           FROM gtvmtyp
          WHERE gtvmtyp_code = mtyp_code_in;

--
-- Tables.
-- ==================================================
      meeting_tab              bwckweek.meeting_tab_type;
      template_tab             bwckweek.template_tab_type;
--
-- Variables/Constants.
-- ==================================================
      row_count                INTEGER;
      min_time                 VARCHAR2 (4);
      max_time                 VARCHAR2 (4);
      template_start_date      DATE;
      template_end_date        DATE;
      no_schd                  BOOLEAN                    := TRUE;
      slots_in_hour   CONSTANT INTEGER                    := 4;
      slot_length     CONSTANT INTEGER                    := 60 /
                                                                slots_in_hour;
      template_index           INTEGER;
      --
      todays_template_index    INTEGER                    := 0;
      max_template_index       INTEGER                    := 0;
      start_date               DATE;
      i_date                   DATE;
      s_date                   DATE;
      e_date                   DATE;

      start_date_in            VARCHAR2 (30) := ?;
      lv_pidm                  INTEGER  := ?;
      lv_sched_json            VARCHAR2 (32000) DEFAULT NULL;
      lv_tba_sched_json        VARCHAR2 (32000) DEFAULT NULL;
      lv_errorMsg              VARCHAR2 (30) DEFAULT NULL;
      lv_temp_fmt              VARCHAR2 (30);
   BEGIN

   -- Validate CHAR/VARCHAR2 post variables
--    twbksecr.p_chk_parms_05(start_date_in, error_msg_in, error_date_in); /*080701-1*/

-- Check for valid user.
-- ==================================================
--      IF NOT twbkwbis.f_validuser (global_pidm)
--      THEN
--         RETURN;
--      END IF;

--
-- Start the web page.
-- ==================================================
--      bwckfrmt.p_open_doc ('bwskfshd.P_CrseSchd');
--
-- Display info text.
-- ==================================================
--      twbkwbis.p_dispinfo ('bwskfshd.P_CrseSchd', 'DEFAULT');
--
-- Populate a pl/sql table with the start and end dates
-- for each template. A template is a frame of time when the
-- user's schedule remains static. A template starts on the
-- week that a class starts or the week after a class ends.
-- The template end dates can be derived as one week before
-- the start dates.
-- ==================================================
      lv_temp_fmt := twbklibs.date_input_fmt;
      twbklibs.date_input_fmt := 'MM/DD/YYYY';

      bwckweek.p_load_template (
         'S',
         lv_pidm,
         start_date_in,
         start_date,
         i_date,
         template_index,
         template_tab,
         template_start_date,
         template_end_date,
         todays_template_index,
         no_schd,
         max_template_index
      );

      twbklibs.date_input_fmt := lv_temp_fmt;

--
-- Loop through schedule records for the current template.
-- ==================================================
      row_count := 0;

      lv_sched_json := '{"rows": [';


      <<read_classes_loop>>
      FOR sfvstum_rec IN sfvstumc (
                            lv_pidm,
                            template_start_date,
                            template_end_date
                         )
      LOOP
         OPEN sfvstareg (lv_pidm,
                         sfvstum_rec.term_code,
                         sfvstum_rec.crn);
         FETCH sfvstareg INTO sfvstareg_rec;
         CLOSE sfvstareg;

         -- Determine the intersect dates (SFRAREG vs. SSRMEET)
         IF GREATEST (
               NVL (sfvstum_rec.ssrmeet_start_date, SYSDATE - 36500),
               NVL (NVL(sfvstareg_rec.sfrareg_start_date, sfvstum_rec.ssbsect_ptrm_start_date), SYSDATE - 36500)
            ) > NVL (NVL(sfvstareg_rec.sfrareg_completion_date, sfvstum_rec.ssbsect_ptrm_end_date), SYSDATE + 36500)
         THEN
            s_date := NVL(sfvstareg_rec.sfrareg_start_date, sfvstum_rec.ssbsect_ptrm_start_date);
         ELSE
            s_date :=
              GREATEST (
                 NVL (sfvstum_rec.ssrmeet_start_date, SYSDATE - 36500),
                 NVL (NVL(sfvstareg_rec.sfrareg_start_date, sfvstum_rec.ssbsect_ptrm_start_date), SYSDATE - 36500)
              );
         END IF;

         IF LEAST (
               NVL (sfvstum_rec.ssrmeet_end_date, SYSDATE + 36500),
               NVL (NVL (sfvstareg_rec.sfrareg_completion_date, sfvstum_rec.ssbsect_ptrm_end_date), SYSDATE + 36500)
            ) < NVL (NVL(sfvstareg_rec.sfrareg_start_date, sfvstum_rec.ssbsect_ptrm_start_date), SYSDATE - 36500)
         THEN
            e_date := NVL (sfvstareg_rec.sfrareg_completion_date, sfvstum_rec.ssbsect_ptrm_end_date);
         ELSE
            e_date :=
              LEAST (
                 NVL (sfvstum_rec.ssrmeet_end_date, SYSDATE + 36500),
                 NVL (NVL (sfvstareg_rec.sfrareg_completion_date, sfvstum_rec.ssbsect_ptrm_end_date), SYSDATE + 36500)
              );
         END IF;

         -- If the template dates dont fall within the intersect dates,
         -- ignore this class record
         IF NOT (
                      s_date BETWEEN template_start_date AND template_end_date
                   OR e_date BETWEEN template_start_date AND template_end_date
                   OR template_start_date BETWEEN s_date AND e_date
                   OR template_end_date BETWEEN s_date AND e_date
                )
         THEN
            GOTO end_loop;
         END IF;

         row_count := row_count + 1;

         IF row_count = 1
         THEN
            lv_sched_json := lv_sched_json || '{';
         ELSE
            lv_sched_json := lv_sched_json || ', {';
         END IF;


--
-- Keep track of the earliest and latest times for the
-- current template for purposes of determining length
-- of html table.
-- ==================================================
         min_time :=
           LEAST (
              NVL (min_time, sfvstum_rec.begin_time),
              sfvstum_rec.begin_time
           );
         max_time :=
           GREATEST (
              NVL (max_time, sfvstum_rec.end_time),
              sfvstum_rec.end_time
           );
         min_time := bwckweek.f_time_slot_round (min_time, slot_length);
         max_time := bwckweek.f_time_slot_round (max_time, slot_length);
--
-- Put the class record into an array.
-- ==================================================
         meeting_tab (row_count) := sfvstum_rec;
         -- Use the intersect dates for creating the weekly grid
         meeting_tab (row_count).ssrmeet_start_date := s_date;
         meeting_tab (row_count).ssrmeet_end_date := e_date;
         OPEN gtvmtypc (sfvstum_rec.mtyp_code);
         FETCH gtvmtypc INTO meeting_tab (row_count).gtvmtyp_desc;
         CLOSE gtvmtypc;

         lv_sched_json := lv_sched_json || '"meeting_term_code": "' || meeting_tab (row_count).term_code || '",';
         lv_sched_json := lv_sched_json || '"meeting_crn": "' || meeting_tab (row_count).crn || '",';
         lv_sched_json := lv_sched_json || '"meeting_subj_code": "' || meeting_tab (row_count).subj_code || '",';
         lv_sched_json := lv_sched_json || '"meeting_crse_numb": "' || meeting_tab (row_count).crse_numb || '",';
         lv_sched_json := lv_sched_json || '"meeting_seq_numb": "' || meeting_tab (row_count).seq_numb || '",';
         lv_sched_json := lv_sched_json || '"meeting_mon_day": "' || meeting_tab (row_count).mon_day || '",';
         lv_sched_json := lv_sched_json || '"meeting_tue_day": "' || meeting_tab (row_count).tue_day || '",';
         lv_sched_json := lv_sched_json || '"meeting_wed_day": "' || meeting_tab (row_count).wed_day || '",';
         lv_sched_json := lv_sched_json || '"meeting_thu_day": "' || meeting_tab (row_count).thu_day || '",';
         lv_sched_json := lv_sched_json || '"meeting_fri_day": "' || meeting_tab (row_count).fri_day || '",';
         lv_sched_json := lv_sched_json || '"meeting_sat_day": "' || meeting_tab (row_count).sat_day || '",';
         lv_sched_json := lv_sched_json || '"meeting_sun_day": "' || meeting_tab (row_count).sun_day || '",';
         lv_sched_json := lv_sched_json || '"meeting_ssrmeet_start_date": "' || meeting_tab (row_count).ssrmeet_start_date || '",';
         lv_sched_json := lv_sched_json || '"meeting_ssrmeet_end_date": "' || meeting_tab (row_count).ssrmeet_end_date || '",';
         lv_sched_json := lv_sched_json || '"meeting_sfrareg_start_date": "' || meeting_tab (row_count).sfrareg_start_date || '",';
         lv_sched_json := lv_sched_json || '"meeting_sfrareg_completion_date": "' || meeting_tab (row_count).sfrareg_completion_date || '",';
         lv_sched_json := lv_sched_json || '"meeting_begin_time": "' || meeting_tab (row_count).begin_time || '",';
         lv_sched_json := lv_sched_json || '"meeting_end_time": "' || meeting_tab (row_count).end_time || '",';
         lv_sched_json := lv_sched_json || '"meeting_bldg_code": "' || meeting_tab (row_count).bldg_code || '",';
         lv_sched_json := lv_sched_json || '"meeting_room_code": "' || meeting_tab (row_count).room_code || '",';
         lv_sched_json := lv_sched_json || '"meeting_mtyp_code": "' || meeting_tab (row_count).mtyp_code || '",';
         lv_sched_json := lv_sched_json || '"meeting_gtvmtyp_code": "' || meeting_tab (row_count).gtvmtyp_desc || '",';


         lv_sched_json := lv_sched_json || '}';

         <<end_loop>>
         NULL;
      END LOOP read_classes_loop;

      lv_sched_json := lv_sched_json || ']}';

--
-- Display any error messages, and the go to date input field.
-- ===========================================================
--      bwckweek.p_goto_date (
--         error_msg_in,
--         error_date_in,
--         start_date,
--         'bwskfshd.p_proc_crse_schd'
--      );
--
-- Display the Weekly schedule table
-- ==================================================
--      bwckweek.p_disp_grid (
--         meeting_tab,
--         template_tab,
--         max_template_index,
--         todays_template_index,
--         template_start_date,
--         'bwskfshd.P_CrseSchd',
--         'bwskfshd.P_CrseSchdDetl',
--         slots_in_hour,
--         slot_length,
--         min_time,
--         max_time,
--         row_count,
--         no_schd
--      );
--
-- Print classes without assigned times.
-- ==================================================
      lv_tba_sched_json := '{"rows": [';
      row_count := 0;

      <<print_nonscheduled_loop>>
      FOR crse_rec IN nonschdc (lv_pidm)
      LOOP
         OPEN sfvstareg (lv_pidm,
                crse_rec.term_code,
                crse_rec.crn);
         FETCH sfvstareg INTO sfvstareg_rec;
         CLOSE sfvstareg;

         -- If the template dates dont fall within the intersect dates,
         -- ignore this class record
         IF NOT (
                   (  template_start_date
                      BETWEEN NVL (sfvstareg_rec.sfrareg_start_date,
                                   crse_rec.ssbsect_ptrm_start_date)
                          AND NVL (sfvstareg_rec.sfrareg_completion_date,
                                   crse_rec.ssbsect_ptrm_end_date)
                   )
                OR (  template_end_date
                      BETWEEN NVL (sfvstareg_rec.sfrareg_start_date,
                                   crse_rec.ssbsect_ptrm_start_date)
                          AND NVL (sfvstareg_rec.sfrareg_completion_date,
                                   crse_rec.ssbsect_ptrm_end_date)
                   )
                OR (  NVL (sfvstareg_rec.sfrareg_start_date, crse_rec.ssbsect_ptrm_start_date)
                      BETWEEN template_start_date
                          AND template_end_date
                   )
                OR (  NVL (sfvstareg_rec.sfrareg_completion_date, crse_rec.ssbsect_ptrm_end_date)
                      BETWEEN template_start_date
                          AND template_end_date
                   )
                )
         THEN
            GOTO end_ns_loop;
         END IF;

         row_count := row_count + 1;

--         bwckweek.p_disp_unassigned (
--            crse_rec.term_code,
--            crse_rec.crn,
--            crse_rec.subj_code,
--            crse_rec.crse_numb,
--            crse_rec.seq_numb,
--           crse_rec.ptrm_code,
--            crse_rec.stvschd_desc,
--            'bwskfshd.P_CrseSchdDetl',
--            row_count
--         );
         IF row_count = 1
         THEN
            lv_tba_sched_json := lv_tba_sched_json || '{';
         ELSE
            lv_tba_sched_json := lv_tba_sched_json || ', {';
         END IF;
         lv_tba_sched_json := lv_tba_sched_json || '"crse_term_code": "' || crse_rec.term_code || '",';
         lv_tba_sched_json := lv_tba_sched_json || '"crse_crn": "' || crse_rec.crn || '",';
         lv_tba_sched_json := lv_tba_sched_json || '"crse_subj_code": "' || crse_rec.subj_code || '",';
         lv_tba_sched_json := lv_tba_sched_json || '"crse_crse_numb": "' || crse_rec.crse_numb || '",';
         lv_tba_sched_json := lv_tba_sched_json || '"crse_seq_numb": "' || crse_rec.seq_numb || '",';
         lv_tba_sched_json := lv_tba_sched_json || '"crse_ptrm_code": "' || crse_rec.ptrm_code || '",';
         lv_tba_sched_json := lv_tba_sched_json || '"crse_stvschd_desc": "' || crse_rec.stvschd_desc || '"';
         lv_tba_sched_json := lv_tba_sched_json || '}';

         <<end_ns_loop>>
         NULL;

      END LOOP print_nonscheduled_loop;
      lv_tba_sched_json := lv_tba_sched_json || ']}';


      IF row_count > 0
      THEN
         no_schd := FALSE;
      END IF;

--
-- Handle no schedule.
-- ==================================================
      IF no_schd
      THEN
--         twbkfrmt.p_printmessage (
--            G\$_NLS.Get ('BWSKFSH1-0000',
--              'SQL',
--               'You are not currently registered'
--            ),
--            'ERROR'
--         );
         lv_errorMsg := 'notRegistered';
      END IF;

--
-- Close out.
-- ==================================================
--      HTP.br;
--      twbkwbis.p_closedoc (curr_release);
--   END P_CrseSchd;

   ? := lv_sched_json;
   ? := lv_tba_sched_json;
   ? := lv_errorMsg;
END;
    """

    public final static String WEEKLY_COURSE_SCHEDULE_DETAIL = """
DECLARE
   row_count                NUMBER;
   scbcrse_row              scbcrse%ROWTYPE;
   name1                    VARCHAR2 (60);
   cpinuse                  VARCHAR2 (1);
   webctinuse               VARCHAR2 (1);
   makewebctlink            VARCHAR2 (1);
   webctlogin               VARCHAR2 (200);
   webctlink                VARCHAR2 (1000);
   tot_credit_hr            NUMBER;
   tot_bill_hr              NUMBER;
   tot_ceu                  NUMBER;
   term                     stvterm.stvterm_code%TYPE;
   hold_beg_time            VARCHAR2 (30);
   hold_end_time            VARCHAR2 (30);
   not_registered_message   VARCHAR2 (60);
   genpidm                  spriden.spriden_pidm%TYPE;
   table_opened             BOOLEAN                   := FALSE;
   hld_stvgmod_desc         STVGMOD.STVGMOD_DESC%TYPE;
   hld_stvlevl_desc         STVLEVL.STVLEVL_DESC%TYPE;
   call_path                VARCHAR2 (1);
   lv_wl_notification_ref   sb_wl_notification.wl_notification_ref;
   lv_wl_notification_rec   sb_wl_notification.wl_notification_rec;
   lv_wl_section_ctrl_ref   sb_wl_section_ctrl.wl_section_ctrl_ref;
   lv_wl_section_ctrl_rec   sb_wl_section_ctrl.wl_section_ctrl_rec;
   -- 8.4.0.2 HEOA. Two New Variables.
   lv_page_in               CONSTANT VARCHAR2(4) := 'SCHD';
   lv_int_url_txt           VARCHAR2(100) := 'Bookstore';

--  BWCKGEN globals
   tmp_hist_ind            VARCHAR2 (1)                           DEFAULT NULL;
   stvterm_rec             stvterm%ROWTYPE;
   sorrtrm_rec             sorrtrm%ROWTYPE;

-- p_disp_meeting_times
   pdm_term_in   stvterm.stvterm_code%TYPE;
   pdm_crn_in    ssbsect.ssbsect_crn%TYPE;

   meeting_times_found   BOOLEAN        := FALSE;
   instr_name            VARCHAR2 (3000);
   hold_instr_name       VARCHAR2 (3000);
   temp_instr_name       VARCHAR2 (3000);
   pdm_hold_beg_time         VARCHAR2 (30);
   pdm_hold_end_time         VARCHAR2 (30);
   primary_ind           VARCHAR2 (50);
-- p_disp_meeting_times

   CURSOR regcrsec (
      pidm_in       spriden.spriden_pidm%TYPE,
      term_in       stvterm.stvterm_code%TYPE DEFAULT NULL,
      crn_in        ssbsect.ssbsect_crn%TYPE DEFAULT NULL,
      hist_ind_in   VARCHAR2 DEFAULT NULL
   )
   IS
      SELECT *
        FROM stvschd,
             stvcamp,
             stvrsts,
             ssbsect,
             sfrstcr,
             stvterm,
             scbcrse,
             sobterm
       WHERE sfrstcr_term_code = sobterm_term_code
         AND sobterm_dynamic_sched_term_ind = 'Y'
         AND sfrstcr_pidm = pidm_in
         AND sfrstcr_term_code = NVL (term_in, sfrstcr_term_code)
         AND sfrstcr_crn = NVL (crn_in, sfrstcr_crn)
         AND sfrstcr_crn = ssbsect_crn
         AND NVL(sfrstcr_error_flag,'N') <> 'F'
         AND ssbsect_term_code = sfrstcr_term_code
         AND scbcrse_subj_code = ssbsect_subj_code
         AND scbcrse_crse_numb = ssbsect_crse_numb
         AND scbcrse_eff_term =
              (SELECT MAX (scbcrse_eff_term)
                 FROM scbcrse x
                WHERE x.scbcrse_subj_code = ssbsect_subj_code
                  AND x.scbcrse_crse_numb = ssbsect_crse_numb
                  AND x.scbcrse_eff_term <= sfrstcr_term_code)
         AND stvcamp_code = sfrstcr_camp_code
         AND stvschd_code = ssbsect_schd_code
         AND stvrsts_code = sfrstcr_rsts_code
         AND stvterm_code = sfrstcr_term_code
         AND stvrsts_sb_print_ind =
                            DECODE (hist_ind_in, 'Y', stvrsts_sb_print_ind, 'Y')
       ORDER BY ssbsect_term_code DESC,
                ssbsect_subj_code,
                ssbsect_crse_numb,
                ssbsect_seq_numb;

   CURSOR insmcrsec (
      code_in       gtvinsm.gtvinsm_code%TYPE DEFAULT NULL
   )
   IS
      SELECT *
        FROM gtvinsm
       WHERE gtvinsm_code = code_in;

   insmcrsec_rec  insmcrsec%ROWTYPE;

   CURSOR sylncrsec (
      term_in       stvterm.stvterm_code%TYPE DEFAULT NULL,
      crn_in        ssbsect.ssbsect_crn%TYPE DEFAULT NULL
   )
   IS
      SELECT *
        FROM ssrsyln
       WHERE ssrsyln_term_code = term_in
         AND ssrsyln_crn = crn_in;

   sylncrsec_rec  sylncrsec%ROWTYPE;

   CURSOR aregcrsec (
      pidm_in       spriden.spriden_pidm%TYPE,
      term_in       stvterm.stvterm_code%TYPE DEFAULT NULL,
      crn_in        ssbsect.ssbsect_crn%TYPE DEFAULT NULL
   )
   IS
      SELECT *
        FROM sfrareg a
       WHERE sfrareg_term_code = term_in
         AND sfrareg_pidm = pidm_in
         AND sfrareg_crn = crn_in
         AND sfrareg_extension_number =
                    (SELECT MAX (sfrareg_extension_number)
                       FROM sfrareg x
                      WHERE x.sfrareg_pidm = a.sfrareg_pidm
                        AND x.sfrareg_crn = a.sfrareg_crn
                        AND x.sfrareg_term_code = a.sfrareg_term_code);

   aregcrsec_rec  aregcrsec%ROWTYPE;
--  end BWCKGEN globals

   crn          VARCHAR2(6) DEFAULT NULL;
   term_in      stvterm.stvterm_code%TYPE := ?;
   lv_pidm  spriden.spriden_pidm%TYPE     := ?;
   lv_tot_credit_hr VARCHAR2(12) default null;
   lv_course_title_caption VARCHAR2(120) default null;
   lv_dowebctlogin VARCHAR2(1);
   lv_sched_count NUMBER;

   lv_course_json VARCHAR2(32000);
   lv_errorMsg VARCHAR2(60) default null;

   CURSOR tot_credit_hr_c (
      pidm_in   spriden.spriden_pidm%TYPE,
      term_in   stvterm.stvterm_code%TYPE
   )
   IS
      SELECT SUM (DECODE (stvlevl_ceu_ind, 'Y', 0, NVL (sfrstcr_credit_hr, 0)))
        FROM stvlevl, sfrstcr
       WHERE stvlevl_code = sfrstcr_levl_code
         AND sfrstcr_pidm = pidm_in
         AND sfrstcr_term_code = term_in
         AND (
                   (   sfrstcr_error_flag <> 'F'
                    OR sfrstcr_error_flag IS NULL)
                OR (
                          sfrstcr_error_flag = 'F'
                      AND sfrstcr_rmsg_cde = 'MAXI'
                   )
             );

-- BWCKFRMT stuff
   CURSOR regsc (
      term_in   stvterm.stvterm_code%TYPE,
      crn_in    ssbsect.ssbsect_crn%TYPE,
      pidm_in   spriden.spriden_pidm%TYPE
   )
   IS
      SELECT sfrstcr_grde_date, ssbsect_credit_hrs, sfrstcr_credit_hr,
             scbcrse_credit_hr_ind, sobterm_cred_web_upd_ind,
             sobterm_gmod_web_upd_ind, sobterm_levl_web_upd_ind, stvgmod_desc,
             stvlevl_desc, ssbsect_subj_code, ssbsect_crse_numb,
             ssbsect_gmod_code
        FROM sfrstcr, ssbsect, scbcrse, sobterm, stvgmod, stvlevl
       WHERE sfrstcr_term_code = term_in
         AND sfrstcr_crn = crn_in
         AND sfrstcr_pidm = pidm_in
         AND ssbsect_crn = sfrstcr_crn
         AND ssbsect_term_code = sfrstcr_term_code
         AND sfrstcr_term_code = sobterm_term_code
         AND ssbsect_subj_code = scbcrse_subj_code
         AND ssbsect_crse_numb = scbcrse_crse_numb
         AND scbcrse_eff_term =
              (SELECT MAX (scbcrse_eff_term)
                 FROM scbcrse
                WHERE scbcrse_subj_code = ssbsect_subj_code
                  AND scbcrse_crse_numb = ssbsect_crse_numb
                  AND scbcrse_eff_term <= term_in)
         AND sfrstcr_gmod_code = stvgmod_code
         AND sfrstcr_levl_code = stvlevl_code;

   regs_rec                    regsc%ROWTYPE;


   CURSOR sirasgnc (p_term stvterm.stvterm_code%TYPE,
                       p_crn ssbsect.ssbsect_crn%TYPE,
                       p_cat sirasgn.sirasgn_category%TYPE DEFAULT NULL)
      IS
--         SELECT f_format_name (sirasgn_pidm, 'FMIL') instr_name,
--         SELECT 'X' instr_name, sirasgn_pidm,
           SELECT sirasgn_pidm,
                sirasgn_primary_ind, spriden_last_name, spriden_first_name, spriden_mi,
                spriden_surname_prefix
           FROM sirasgn, spriden
          WHERE sirasgn_pidm = spriden_pidm
            AND sirasgn_term_code = p_term
            AND sirasgn_crn = p_crn
            AND ( sirasgn_category = p_cat
                  OR p_cat IS NULL )
            AND spriden_change_ind IS NULL
      ORDER BY sirasgn_primary_ind, spriden_last_name, spriden_first_name, spriden_mi;

   FUNCTION p_instructor_links (
                  p_term IN stvterm.stvterm_code%TYPE,
                  p_crn IN ssbsect.ssbsect_crn%TYPE,
                  p_ptrm_code IN ssbsect.ssbsect_ptrm_code%TYPE DEFAULT NULL,
                  p_pidm IN spriden.spriden_pidm%TYPE DEFAULT NULL,
                  p_call_path IN VARCHAR2 DEFAULT 'S') RETURN VARCHAR2
      IS
         i NUMBER := 1;
         instr_pidms bwckfrmt.instr_pidms_tabtype;
         instr_names bwckfrmt.instr_names_tabtype;
         lv_json VARCHAR2(4000);

      BEGIN
        IF     (p_ptrm_code IS NULL)
            AND (p_pidm IS NOT NULL)
         THEN
         --OLR course with instructor found in SFRAREG
            instr_names(i) := f_format_name (p_pidm, 'FMIL');
            instr_pidms(i) := p_pidm;

            FOR sirasgn_row IN sirasgnc (p_term, p_crn)
            LOOP
               IF sirasgn_row.sirasgn_pidm = p_pidm
               THEN
                  NULL;
               ELSE
                  i:= i+1;
                  instr_names(i) := f_format_name (sirasgn_row.sirasgn_pidm, 'FMIL');
                  instr_pidms(i) := sirasgn_row.sirasgn_pidm;
               END IF;
            END LOOP;

         ELSE
         -- Traditional course, or OLR with no instructor found in SFRAREG
            FOR sirasgn_row IN sirasgnc (p_term, p_crn)
            LOOP
               instr_names(i) := f_format_name (sirasgn_row.sirasgn_pidm, 'FMIL');
               instr_pidms(i) := sirasgn_row.sirasgn_pidm;
               i:= i+1;
            END LOOP;

         END IF;

         lv_json := '{"rows": [';

         IF instr_pidms.COUNT > 0
         THEN
   --         twbkfrmt.p_tabledataopen;
            FOR j IN 1..instr_pidms.COUNT
            LOOP
               IF j = 1
               THEN
                  lv_json := lv_json || '{';
               ELSE
                  lv_json := lv_json || ',{';
               END IF;

               lv_json := lv_json || '"instructor": "' || instr_names(j) || '"}';
            END LOOP;

   --         twbkfrmt.p_tabledataclose;
   --      ELSE
   --         twbkfrmt.p_tabledata (ccolspan => '1');
         END IF;
         lv_json := lv_json || ']}';
         RETURN lv_json;

   END;
-- end BWCKFRMT stuff
--

BEGIN
--   IF NOT twbkwbis.f_validuser (global_pidm)
--   THEN
--      RETURN;
--   END IF;

   IF NOT bwskflib.f_validviewterm (term_in, stvterm_rec, sorrtrm_rec)
   THEN
      NULL;
   END IF;

--   IF NVL (twbkwbis.f_getparam (global_pidm, 'STUFAC_IND'), 'STU') = 'FAC'
--   THEN
--      genpidm :=
--          TO_NUMBER (twbkwbis.f_getparam (global_pidm, 'STUPIDM'), '999999999');
--      call_path := 'F';
--      not_registered_message :=
--        g\$_nls.get ('BWCKGEN1-0009',
--           'SQL',
--           'No schedule available for selected term.');
--   ELSE
--      genpidm := global_pidm;
      genpidm := lv_pidm;
      call_path := 'S';
--      not_registered_message :=
--        g\$_nls.get ('BWCKGEN1-0010',
--           'SQL',
--           'You are not currently registered for the term.');
--   END IF;

   term := term_in;
   row_count := 0;

--   IF call_path = 'S'
--   THEN
--      bwckfrmt.p_open_doc ('bwskfshd.P_CrseSchdDetl', term);
--   END IF;

--   twbkwbis.p_dispinfo ('bwskfshd.P_CrseSchdDetl', 'DEFAULT');
   cpinuse := twbkwbis.f_fetchwtparam ('cpinuse');

   IF cpinuse = 'Y'
   THEN
      makewebctlink := 'N';
   ELSE
      webctinuse := twbkwbis.f_fetchwtparam ('WEBCTINUSE');

      IF webctinuse = 'Y'
      THEN
         makewebctlink := 'Y';
         webctlogin := twbkwbis.f_fetchwtparam ('WEBCTLOGIN');

         IF webctlogin IS NULL
         THEN
            makewebctlink := 'N';
         END IF;
      ELSE
         makewebctlink := 'N';
      END IF;
   END IF;

   -- 8.4.0.2 HEOA
   -- Get the internal URL text outside the loop.
   lv_int_url_txt := bwckbook.f_get_internal_url_txt;
   lv_course_json := lv_course_json || '{"rows":[';
   FOR regcrse IN regcrsec (genpidm, term, crn, tmp_hist_ind)
   LOOP
      row_count := regcrsec%rowcount;


      sylncrsec_rec := NULL;
      OPEN sylncrsec (term, crn);
      FETCH sylncrsec INTO sylncrsec_rec;
      CLOSE sylncrsec;

      aregcrsec_rec := NULL;
      OPEN aregcrsec (genpidm, term, crn);
      FETCH aregcrsec INTO aregcrsec_rec;
      CLOSE aregcrsec;

      IF row_count = 1
      THEN
         /*  Calculate total credit hours for Schedule By Day and Time */
         OPEN tot_credit_hr_c (genpidm, term);
         FETCH tot_credit_hr_c INTO tot_credit_hr;
         CLOSE tot_credit_hr_c;
-- Prints header:
--------

--         twbkfrmt.p_printtext (
--            g\$_nls.get ('BWCKGEN1-0011', 'SQL', 'Total Credit Hours') || ': ' ||
--               LTRIM (TO_CHAR (tot_credit_hr, '99990D990'))
--         );
--         HTP.br;
--         HTP.br;
         lv_tot_credit_hr :=  LTRIM (TO_CHAR (tot_credit_hr, '99990D990'));
         lv_course_json := lv_course_json || '{"tot_credits":"' || lv_tot_credit_hr || '",';
      ELSE
         lv_course_json := lv_course_json || ', {';
      END IF;

      FOR scbcrse IN scklibs.scbcrsec (
                        regcrse.ssbsect_subj_code,
                        regcrse.ssbsect_crse_numb,
                        term
                     )
      LOOP
         scbcrse_row := scbcrse;
      END LOOP;

      IF makewebctlink = 'N' OR
         regcrse.ssbsect_intg_cde IS NULL
      THEN
--         twbkfrmt.p_tableopen (
--            'DATADISPLAY',
--            cattributes   => 'SUMMARY="' ||
--                                g\$_nls.get ('BWCKGEN1-0012',
--                                   'SQL',
--                                   'This layout table is used to present the schedule course detail') ||
--                                '"',
--            ccaption      => bwcklibs.f_course_title (
--                                term_in,
--                                regcrse.ssbsect_crn
--                             ) ||
--                                ' - ' ||
--                                regcrse.ssbsect_subj_code ||
--                                ' ' ||
--                                regcrse.ssbsect_crse_numb ||
--                                ' - ' ||
--                               regcrse.ssbsect_seq_numb
--         );
         lv_dowebctlogin := 'Y';
      ELSE
--         twbkfrmt.p_tableopen (
--            'DATADISPLAY',
--            cattributes   => 'SUMMARY="' ||
--                                g\$_nls.get ('BWCKGEN1-0013',
--                                   'SQL',
--                                   'This layout table is used to present the schedule course detail') ||
--                                '"'
--         );
--         webctlink :=
--           bwcklibs.f_course_title (term_in, regcrse.ssbsect_crn) || ' - ' ||
--              regcrse.ssbsect_subj_code ||
--              ' ' ||
--              regcrse.ssbsect_crse_numb ||
--              ' - ' ||
--              regcrse.ssbsect_seq_numb;
--         webctlink :=
--           twbkfrmt.f_printanchor (
--              webctlogin,
--              webctlink,
--              '',
--              '',
--              bwckfrmt.f_anchor_focus (webctlogin)
--           );
--         twbkfrmt.p_tableheader (
--            twbkfrmt.f_printtext (webctlink, BYPASS_ESC=>'Y'),
--            ccolspan   => 3, cattributes=>'BYPASS_ESC=Y'
--         );
         lv_dowebctlogin := 'N';
      END IF;
      lv_course_title_caption := bwcklibs.f_course_title (
                       term_in,
                       regcrse.ssbsect_crn
                    ) ||
                       ' - ' ||
                       regcrse.ssbsect_subj_code ||
                       ' ' ||
                       regcrse.ssbsect_crse_numb ||
                       ' - ' ||
                       regcrse.ssbsect_seq_numb;
      lv_course_json := lv_course_json || '"doWebCtLink": "' || lv_dowebctlogin|| '",';
      lv_course_json := lv_course_json || '"webctlogin": "' || webctlogin || '",';
      lv_course_json := lv_course_json || '"course_title": "' || lv_course_title_caption || '",';

--      twbkfrmt.p_tablerowopen;
--      twbkfrmt.p_tabledatalabel (
--         g\$_nls.get ('BWCKGEN1-0014', 'SQL', 'Associated Term:'),
--         ccolspan   => 2
--      );
--      twbkfrmt.p_tabledata (bwcklibs.f_term_desc (term_in));
      lv_course_json := lv_course_json || '"assoc_term": "' || bwcklibs.f_term_desc (term_in) || '",';
--      twbkfrmt.p_tablerowclose;
--      twbkfrmt.p_tablerowopen;
--      twbkfrmt.p_tabledatalabel (
--         twbkfrmt.f_printtext (
--            '<ACRONYM title = "' ||
--               g\$_nls.get ('BWCKGEN1-0015', 'SQL', 'Course Reference Number') ||
--               '">' ||
--               g\$_nls.get ('BWCKGEN1-0016', 'SQL', 'CRN') ||
--               '</ACRONYM>'
--         ) ||
--            ':',
--         ccolspan   => 2
--      );
--      twbkfrmt.p_tabledata (regcrse.ssbsect_crn);
      lv_course_json := lv_course_json || '"crn": "' || regcrse.ssbsect_crn || '",';
--      twbkfrmt.p_tablerowclose;
--      twbkfrmt.p_tablerowopen;
--      twbkfrmt.p_tabledatalabel (
--         g\$_nls.get ('BWCKGEN1-0017', 'SQL', 'Status:'),
--         ccolspan   => 2
--      );
--      twbkfrmt.p_tabledata (
--         g\$_nls.get ('BWCKGEN1-0018',
--            'SQL',
--            '%01% on %02%',
--            regcrse.stvrsts_desc,
--            TO_CHAR (regcrse.sfrstcr_rsts_date, twbklibs.date_display_fmt)
--         )
--      );
      lv_course_json := lv_course_json || '"status_01": "' || regcrse.stvrsts_desc || '",';
      lv_course_json := lv_course_json || '"status_02": "' || TO_CHAR (regcrse.sfrstcr_rsts_date, twbklibs.date_display_fmt) || '",';
--      twbkfrmt.p_tablerowclose;

      -- WaitList automation enhancement begins.
      IF sfkwlat.f_display_position(term, regcrse.ssbsect_crn) = 'Y' AND
         sfkwlat.f_wl_automation_active( term, regcrse.ssbsect_crn ) = 'Y' AND
         regcrse.stvrsts_voice_type = 'L'
      THEN
--        twbkfrmt.p_tablerowopen;
--        twbkfrmt.p_tabledatalabel(
--            g\$_nls.get('BWCKGEN1-0019', 'SQL', 'Waitlist Position:'),
--            ccolspan   => 2
--        );
--        twbkfrmt.p_tabledata(
--            sfkwlat.f_get_wl_pos( p_pidm  =>  genpidm,
--                                  p_term  =>  term_in,
--                                  p_crn   =>  regcrse.ssbsect_crn
--                                )
--        );
        lv_course_json := lv_course_json || '"waitlist_pos": "' || sfkwlat.f_get_wl_pos( p_pidm  =>  genpidm,
                                  p_term  =>  term_in,
                                  p_crn   =>  regcrse.ssbsect_crn
                                ) || '",';
--        twbkfrmt.p_tablerowclose;
        lv_wl_notification_ref := sb_wl_notification.f_query_one( p_term_code  =>  term_in,
                                                                  p_crn        =>  regcrse.ssbsect_crn ,
                                                                  p_pidm       =>  genpidm );
        FETCH lv_wl_notification_ref INTO lv_wl_notification_rec;
        IF lv_wl_notification_ref%NOTFOUND THEN
          lv_wl_notification_rec.r_end_date:=NULL;
        END IF;
        CLOSE lv_wl_notification_ref;

--        twbkfrmt.p_tablerowopen;
--        twbkfrmt.p_tabledatalabel(
--            g\$_nls.get('BWCKGEN1-0020', 'SQL', 'Notification Expires:'),
--            ccolspan   => 2
--        );
--        twbkfrmt.p_tabledata(
--            TO_CHAR ( lv_wl_notification_rec.r_end_date,
--                      twbklibs.twgbwrul_rec.twgbwrul_date_fmt
--                    ) ||
--            ' ' ||
--            TO_CHAR ( lv_wl_notification_rec.r_end_date,
--                      twbklibs.twgbwrul_rec.twgbwrul_time_fmt
--                    )
--        );
        lv_course_json := lv_course_json || '"notif_expire": "' || TO_CHAR ( lv_wl_notification_rec.r_end_date,
                      twbklibs.twgbwrul_rec.twgbwrul_time_fmt
                    ) || '",';

--        twbkfrmt.p_tablerowclose;
      END IF ;
      -- Waitlist automation enhancement ends.

      IF sfkolrl.f_open_learning_course (term_in, regcrse.ssbsect_crn)
      THEN
--         twbkfrmt.p_tablerowopen;
--         twbkfrmt.p_tabledatalabel (
--            g\$_nls.get ('BWCKGEN1-0021', 'SQL', 'Class Start Date:'),
--            ccolspan   => 2
--         );
--         twbkfrmt.p_tabledata (
--            TO_CHAR (aregcrsec_rec.sfrareg_start_date, twbklibs.date_display_fmt)
--         );
         lv_course_json := lv_course_json || '"class_start": "' || TO_CHAR (aregcrsec_rec.sfrareg_start_date, twbklibs.date_display_fmt) || '",';
--         twbkfrmt.p_tablerowclose;
--         twbkfrmt.p_tablerowopen;
--         twbkfrmt.p_tabledatalabel (
--            g\$_nls.get ('BWCKGEN1-0022', 'SQL', 'Expected Completion Date:'),
--            ccolspan   => 2
--         );
--         twbkfrmt.p_tabledata (
--            TO_CHAR (
--               aregcrsec_rec.sfrareg_completion_date,
--               twbklibs.date_display_fmt
--            )
--         );
         lv_course_json := lv_course_json || '"expected_comp": "' || TO_CHAR (
               aregcrsec_rec.sfrareg_completion_date,
               twbklibs.date_display_fmt
            ) || '",';
--         twbkfrmt.p_tablerowclose;
      END IF;

--      twbkfrmt.p_tablerowopen;
--      twbkfrmt.p_tabledatalabel (
--         g\$_nls.get ('BWCKGEN1-0023', 'SQL', 'Assigned Instructor:'),
--         ccolspan   => 2
--      );

--      bwckfrmt.p_instructor_links ( -- just get the names, and email addresses
--               regcrse.sfrstcr_term_code, regcrse.sfrstcr_crn,
--               regcrse.ssbsect_ptrm_code, aregcrsec_rec.sfrareg_instructor_pidm,
--               call_path);
      lv_course_json := lv_course_json || '"instructors": ' || p_instructor_links ( -- just get the names, and email addresses
               regcrse.sfrstcr_term_code, regcrse.sfrstcr_crn,
               regcrse.ssbsect_ptrm_code, aregcrsec_rec.sfrareg_instructor_pidm,
               call_path) || ',';

--      twbkfrmt.p_tablerowclose;
--      twbkfrmt.p_tablerowopen;
--      twbkfrmt.p_tabledatalabel (
--         g\$_nls.get ('BWCKGEN1-0024', 'SQL', 'Grade Mode:'),
--         ccolspan   => 2
--      );
--      bwckfrmt.p_disp_grade_mode (term_in, regcrse.ssbsect_crn);
      OPEN regsc (term_in, regcrse.ssbsect_crn, genpidm);
      FETCH regsc INTO regs_rec;
      CLOSE regsc;
      lv_course_json := lv_course_json || '"grade_mode": "' || regs_rec.stvgmod_desc || '",';
--     twbkfrmt.p_tablerowclose;
--      twbkfrmt.p_tablerowopen;
--      twbkfrmt.p_tabledatalabel (
--         g\$_nls.get ('BWCKGEN1-0025', 'SQL', 'Credits:'),
--         ccolspan   => 2
--      );
--      bwckfrmt.p_disp_credit_hours (term_in, regcrse.ssbsect_crn);
      lv_course_json := lv_course_json || '"credits": "' || TO_CHAR (regs_rec.sfrstcr_credit_hr, '9990D990') || '",';
--      twbkfrmt.p_tablerowclose;
--      twbkfrmt.p_tablerowopen;
--      twbkfrmt.p_tabledatalabel (
--         g\$_nls.get ('BWCKGEN1-0026', 'SQL', 'Level:'),
--         ccolspan   => 2
--      );
--      bwckfrmt.p_disp_level (term_in, regcrse.ssbsect_crn);
      lv_course_json := lv_course_json || '"level": "' || regs_rec.stvlevl_desc || '",';
--      twbkfrmt.p_tablerowclose;
--      twbkfrmt.p_tablerowopen;
--      twbkfrmt.p_tabledatalabel (
--         g\$_nls.get ('BWCKGEN1-0027', 'SQL', 'Campus:'),
--         ccolspan   => 2
--      );
--      twbkfrmt.p_tabledata (regcrse.stvcamp_desc);
      lv_course_json := lv_course_json || '"campus": "' || regcrse.stvcamp_desc || '",';
--      twbkfrmt.p_tablerowclose;

      --
      -- HEOA support start
      --

      --
      -- HEOA support stop
      --
--      IF sylncrsec_rec.ssrsyln_section_url IS NOT NULL
--      THEN
--         twbkfrmt.p_tablerowopen;
--         twbkfrmt.p_tabledatalabel (
--            twbkfrmt.f_printtext (
--               g\$_nls.get ('BWCKGEN1-0028', 'SQL', 'Course ') ||
--                  '<ACRONYM title = "' ||
--                  g\$_nls.get ('BWCKGEN1-0029',
--                     'SQL',
--                     'Uniform Resource Locator') ||
--                  '">' ||
--                  g\$_nls.get ('BWCKGEN1-0030', 'SQL', 'URL') ||
--                  '</ACRONYM>' ||
--                  ':'
--            ),
--            ccolspan   => 2
--         );
--         twbkfrmt.p_tabledata (
--            twbkfrmt.f_printanchor (
--               twbkfrmt.f_encodeurl (sylncrsec_rec.ssrsyln_section_url),
--               sylncrsec_rec.ssrsyln_section_url
--            )
--         );
--         twbkfrmt.p_tablerowclose;
--      END IF;

--      twbkfrmt.p_tableclose;
--      bwckfrmt.p_disp_meeting_times (term_in, regcrse.ssbsect_crn);
      pdm_crn_in := regcrse.ssbsect_crn;
      pdm_term_in := term_in;
--    p_disp_meeting_times as inline BEGIN
      lv_course_json := lv_course_json || '"tbl_meetings": [';
      FOR ssrmeet IN bwcklibs.ssrmeetc (pdm_crn_in, pdm_term_in)
      LOOP
         IF NOT meeting_times_found
         THEN
            lv_course_json := lv_course_json || '{';
         ELSE
            lv_course_json := lv_course_json || ', {';
         END IF;
         meeting_times_found := TRUE;

--         IF bwcklibs.ssrmeetc%rowcount = 1
--         THEN
--            p_open_table;
--         END IF;

--         twbkfrmt.p_tablerowopen;
         /* type */
--         twbkfrmt.p_tabledata (ssrmeet.gtvmtyp_desc);
         lv_course_json := lv_course_json || '"type": "' || ssrmeet.gtvmtyp_desc || '",';
         /* time */
         pdm_hold_beg_time :=
           TO_CHAR (
              TO_DATE (ssrmeet.ssrmeet_begin_time, 'HH24MI'),
              twbklibs.twgbwrul_rec.twgbwrul_time_fmt
           );                                                 --  'am' or 'pm'

         IF SUBSTR (pdm_hold_beg_time, 1, 1) = '0'
         THEN
            pdm_hold_beg_time := SUBSTR (pdm_hold_beg_time, 2, 29);
         ELSE
            pdm_hold_beg_time := SUBSTR (pdm_hold_beg_time, 1, 30);
         END IF;

         pdm_hold_end_time :=
           TO_CHAR (
              TO_DATE (ssrmeet.ssrmeet_end_time, 'HH24MI'),
              twbklibs.twgbwrul_rec.twgbwrul_time_fmt
           );

         IF SUBSTR (pdm_hold_end_time, 1, 1) = '0'
         THEN
            pdm_hold_end_time := SUBSTR (pdm_hold_end_time, 2, 29);
         ELSE
            pdm_hold_end_time := SUBSTR (pdm_hold_end_time, 1, 30);
         END IF;

--         IF ssrmeet.ssrmeet_begin_time IS NULL
--         THEN
--            twbkfrmt.p_tabledata (
--               twbkfrmt.f_printtext (
--                  '<ABBR title = "' ||
--                     g\$_nls.get ('BWCKFRM1-0021', 'SQL', 'To Be Announced') ||
--                     '">' ||
--                     g\$_nls.get ('BWCKFRM1-0022', 'SQL', 'TBA') ||
--                     '</ABBR>'
--               )
--            );
--         ELSE
--            twbkfrmt.p_tabledata (pdm_hold_beg_time || ' - ' || pdm_hold_end_time);
--         END IF;
         lv_course_json := lv_course_json || '"times":"' || pdm_hold_beg_time || ' - ' || pdm_hold_end_time || '",';

         /* days */
--         twbkfrmt.p_tabledata (
--            LTRIM (
--               RTRIM (g\$_date.nls_abv_day(ssrmeet.ssrmeet_mon_day) ||
--                      g\$_date.nls_abv_day(ssrmeet.ssrmeet_tue_day) ||
--                      g\$_date.nls_abv_day(ssrmeet.ssrmeet_wed_day) ||
--                      g\$_date.nls_abv_day(ssrmeet.ssrmeet_thu_day) ||
--                      g\$_date.nls_abv_day(ssrmeet.ssrmeet_fri_day) ||
--                      g\$_date.nls_abv_day(ssrmeet.ssrmeet_sat_day) ||
--                      g\$_date.nls_abv_day(ssrmeet.ssrmeet_sun_day)
--               )
--            )
--         );
         lv_course_json := lv_course_json || '"days": "' || ssrmeet.ssrmeet_mon_day ||
                      ssrmeet.ssrmeet_tue_day ||
                      ssrmeet.ssrmeet_wed_day ||
                      ssrmeet.ssrmeet_thu_day ||
                      ssrmeet.ssrmeet_fri_day ||
                      ssrmeet.ssrmeet_sat_day ||
                      ssrmeet.ssrmeet_sun_day || '", ';

         /* where */
         lv_course_json := lv_course_json || '"where": [';
--         IF ssrmeet.ssrmeet_bldg_code IS NULL
         IF ssrmeet.ssrmeet_bldg_code IS NOT NULL
         THEN
--            twbkfrmt.p_tabledata (
--               twbkfrmt.f_printtext (
--                  '<ABBR title = "' ||
--                     g\$_nls.get ('BWCKFRM1-0023', 'SQL', 'To Be Announced') ||
--                     '">' ||
--                     g\$_nls.get ('BWCKFRM1-0024', 'SQL', 'TBA') ||
--                     '</ABBR>'
--               )
--            );
--       ELSE
            lv_sched_count := 0;
            FOR stvbldg IN stkbldg.stvbldgc (ssrmeet.ssrmeet_bldg_code)
            LOOP
               lv_sched_count := lv_sched_count + 1;
--               twbkfrmt.p_tabledata (
--                  stvbldg.stvbldg_desc || ' ' || ssrmeet.ssrmeet_room_code
--               );
               IF lv_sched_count > 1
               THEN
                  lv_course_json := lv_course_json || ',';
               END IF;
               lv_course_json := lv_course_json || '"' || stvbldg.stvbldg_desc || ' ' || ssrmeet.ssrmeet_room_code || '"';
            END LOOP;
         END IF;
         lv_course_json := lv_course_json || '],';

         /* date range */
--         twbkfrmt.p_tabledata (
--            TO_CHAR (ssrmeet.ssrmeet_start_date, twbklibs.date_display_fmt) ||
--               ' - ' ||
--               TO_CHAR (ssrmeet.ssrmeet_end_date, twbklibs.date_display_fmt)
--         );
         lv_course_json := lv_course_json || '"meet_start": "' || TO_CHAR (ssrmeet.ssrmeet_start_date, 'MM/DD/YYYY') || '",';
         lv_course_json := lv_course_json || '"meet_end": "' || TO_CHAR (ssrmeet.ssrmeet_end_date, 'MM/DD/YYYY') || '",';

         /* schedule type */
         lv_course_json := lv_course_json || '"sched_type": [';
         lv_sched_count := 0;
         FOR stvschd IN stkschd.stvschdc (ssrmeet.ssrmeet_schd_code)
         LOOP
            lv_sched_count := lv_sched_count + 1;
--            twbkfrmt.p_tabledata (stvschd.stvschd_desc);
            IF lv_sched_count > 1
            THEN
               lv_course_json := lv_course_json || ',';
            END IF;
            lv_course_json := lv_course_json || '"' || stvschd.stvschd_desc || '"';
         END LOOP;
         lv_course_json := lv_course_json || ']';


         /* instructors */
--         p_instructor_list(
--            pdm_term_in, pdm_crn_in, ssrmeet.ssrmeet_catagory, p_display_email => TRUE
--            );
--       should already have this info
--         twbkfrmt.p_tablerowclose;
         lv_course_json := lv_course_json || '}';
      END LOOP;
      lv_course_json := lv_course_json || ']';

      IF NOT meeting_times_found
      THEN
         IF sfkolrl.f_open_learning_course (pdm_term_in, pdm_crn_in)
         THEN
            instr_name := NULL;
            hold_instr_name := NULL;

            FOR sirasgnc_row IN sirasgnc (pdm_term_in, pdm_crn_in)
            LOOP
               IF NVL (sirasgnc_row.sirasgn_primary_ind, 'N') = 'Y'
               THEN
--                  primary_ind :=
--                    '(' || '<ABBR title= "' ||
--                       g\$_nls.get ('BWCKFRM1-0025', 'SQL', 'Primary') ||
--                       '">' ||
--                       g\$_nls.get ('BWCKFRM1-0026', 'SQL', 'P') ||
--                       '</ABBR>' ||
--                       ')';
                  primary_ind := 'P';
               ELSE
                  primary_ind := '';
               END IF;

               IF twbkfrmt.f_display_ssb_field ('/BWCKFRMT.p_disp_meeting_times', 'spriden_surname_prefix') = 'Y' --Masking Code for I18N
               THEN
                  hold_instr_name :=
                     (sirasgnc_row.spriden_first_name||' '||sirasgnc_row.spriden_mi||' '||sirasgnc_row.spriden_surname_prefix||' '||sirasgnc_row.spriden_last_name);
               ELSE
                  hold_instr_name :=
                     (sirasgnc_row.spriden_first_name||' '||sirasgnc_row.spriden_mi||' '||sirasgnc_row.spriden_last_name);
               END IF;
--               temp_instr_name := hold_instr_name || ' ' || primary_ind ||
--                  bwckfrmt.f_disp_instr_email_icon (sirasgnc_row.sirasgn_pidm);
               temp_instr_name := '"'||  hold_instr_name || ' ' || primary_ind || '"';

               IF instr_name IS NULL
               THEN
                  instr_name := temp_instr_name;
               ELSIF length(instr_name || ', ' || temp_instr_name) < 3001
               THEN
                  instr_name := instr_name || ', ' || temp_instr_name;
               END IF;

            END LOOP;

            IF instr_name IS NOT NULL
            THEN
--               p_open_table;
--               twbkfrmt.p_tablerowopen;
--               twbkfrmt.p_tabledatadead (ccolspan => 6);
--               twbkfrmt.p_tabledata (instr_name, cattributes=>'BYPASS_ESC=Y');
               lv_course_json := lv_course_json || '"instr_names": [' || instr_name || ']';
--               twbkfrmt.p_tablerowclose;
            END IF;
         END IF;
      END IF;

      meeting_times_found := false;

--      IF table_opened
--      THEN
--         twbkfrmt.p_tableclose;
--      END IF;

      instr_name := NULL;
--      HTP.br;
--   END p_disp_meeting_times;
--    p_disp_meeting_times as inline END

      lv_course_json := lv_course_json || '}';
   END LOOP;
   lv_course_json := lv_course_json || '],';
   IF row_count = 0
   THEN
--      twbkfrmt.p_printmessage (not_registered_message);
      lv_errorMsg := 'notRegistered';
   END IF;
   lv_course_json := lv_course_json || '"errorMsg": "'|| lv_errorMsg ||'"}';

   ? := lv_course_json;
--   dbms_output.put_line(lv_course_json);
--   bwckfrmt.p_disp_back_anchor;
--END P_DispCrseSchdDetl;
END;
"""

}
