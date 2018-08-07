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

 }