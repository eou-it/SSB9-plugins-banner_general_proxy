/********************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.proxy.api

class ProxyPersonalInformationApi {

    public final static String PROXY_PROFILE_UI_RULES = """

    DECLARE
    show_p_mi              VARCHAR2(1);
    show_p_surname_prefix   VARCHAR2(1);
    show_p_name_prefix        VARCHAR2(1);
    show_p_name_suffix        VARCHAR2(1);
    show_p_pref_first_name    VARCHAR2(1);
    p_email_address      VARCHAR2(1);
    show_p_phone_area         VARCHAR2(1);
    show_p_phone_number       VARCHAR2(1);
    show_p_phone_ext          VARCHAR2(1);
    show_p_ctry_code_phone    VARCHAR2(1);
    show_p_house_number       VARCHAR2(1);
    show_p_street_line1       VARCHAR2(1);
    show_p_street_line2       VARCHAR2(1);
    show_p_street_line3       VARCHAR2(1);
    show_p_street_line4       VARCHAR2(1);
    show_p_city               VARCHAR2(1);
    show_p_stat_code          VARCHAR2(1);
    show_p_zip                VARCHAR2(1);
    show_p_cnty_code          VARCHAR2(1);
    show_p_natn_code          VARCHAR2(1);
    show_p_sex                VARCHAR2(1);
    show_p_birth_date         VARCHAR2(1);
    show_p_ssn                VARCHAR2(1);
    show_p_opt_out_adv_date VARCHAR2(1);

    FUNCTION F_Required (p_OTYP gtvotyp.gtvotyp_code%TYPE)
    RETURN VARCHAR2
    IS
    lv_option_ind   gtvotyp.gtvotyp_option_default%TYPE;
    lv_max_ind      gtvotyp.gtvotyp_option_default%TYPE;

    CURSOR C_RETPlist
    IS
    SELECT DISTINCT GPRXREF_RETP_CODE
    FROM GPRXREF
    WHERE GPRXREF_PROXY_IDM = ?
    AND TRUNC (SYSDATE) BETWEEN TRUNC (GPRXREF_START_DATE)
    AND TRUNC (GPRXREF_STOP_DATE);
    BEGIN
    lv_max_ind := 'N';

    FOR lv_code IN C_RETPlist
    LOOP
    lv_option_ind :=
    NVL (bwgkprxy.F_GetOption (p_OTYP, lv_code.GPRXREF_RETP_CODE),'N');

    IF lv_option_ind > lv_max_ind
    THEN
    lv_max_ind := lv_option_ind;
    END IF;
    END LOOP;

    RETURN lv_max_ind;
    END F_Required;

    BEGIN

    show_p_name_prefix := F_Required ('PROFILE_NAME_PREFIX');

    show_p_mi := F_Required ('PROFILE_MI');

    show_p_surname_prefix := F_Required ('PROFILE_SURNAME_PREFIX');

    show_p_name_suffix := F_Required ('PROFILE_NAME_SUFFIX');

    show_p_pref_first_name := F_Required ('PROFILE_PREF_FIRST_NAME');

    show_p_phone_area := F_Required ('PROFILE_PHONE_AREA');

    show_p_phone_number := F_Required ('PROFILE_PHONE_NUMBER');

    show_p_phone_ext := F_Required ('PROFILE_PHONE_EXT');

    show_p_ctry_code_phone := F_Required ('PROFILE_PHONE_COUNTRY');

    show_p_house_number := F_Required ('PROFILE_HOUSE_NUMBER');

    show_p_street_line1 := F_Required ('PROFILE_STREET_LINE1');

    show_p_street_line2 := F_Required ('PROFILE_STREET_LINE2');

    show_p_street_line3 := F_Required ('PROFILE_STREET_LINE3');

    show_p_street_line4 := F_Required ('PROFILE_STREET_LINE4');

    show_p_city := F_Required ('PROFILE_CITY');

    show_p_stat_code := F_Required ('PROFILE_STAT_CODE');

    show_p_zip := F_Required ('PROFILE_ZIP');

    show_p_cnty_code := F_Required ('PROFILE_CNTY_CODE');

    show_p_natn_code := F_Required ('PROFILE_NATN_CODE');

    show_p_sex := F_Required ('PROFILE_SEX');

    show_p_birth_date := F_Required ('PROFILE_BIRTH_DATE');

    show_p_ssn := F_Required ('PROFILE_SSN');

    show_p_opt_out_adv_date := F_Required ('PROFILE_OPT_OUT_ADV_IND');

    ? := show_p_name_prefix;
    ? := show_p_mi;
    ? := show_p_surname_prefix;
    ? := show_p_name_suffix;
    ? := show_p_pref_first_name;
    ? := show_p_phone_area;
    ? := show_p_phone_number;
    ? := show_p_phone_ext;
    ? := show_p_ctry_code_phone;
    ? := show_p_house_number;
    ? := show_p_street_line1;
    ? := show_p_street_line2;
    ? := show_p_street_line3;
    ? := show_p_street_line4;
    ? := show_p_city;
    ? := show_p_stat_code;
    ? := show_p_zip;
    ? := show_p_cnty_code;
    ? := show_p_natn_code;
    ? := show_p_sex;
    ? := show_p_birth_date;
    ? := show_p_ssn;
    ? := show_p_opt_out_adv_date;
    END;

    """


    public final static String PROXY_PERSONAL_INFORMATION  = """
       DECLARE
        lv_GPBPRXY_rec gp_gpbprxy.gpbprxy_rec;
        lv_GPBPRXY_ref gp_gpbprxy.gpbprxy_ref;

        BEGIN
        
        dbms_session.set_nls('NLS_DATE_FORMAT',''''||'DD-MON-RRRR'||'''');
        dbms_session.set_nls('NLS_CALENDAR',''''||'GREGORIAN'||'''');

         ? := gp_gpbprxy.F_Query_One (to_number(?));

         END;
    """


    public final static String CHECK_PROXY_PROFILE_REQUIRED_DATA = """
    DECLARE
    lv_message VARCHAR2 (30000);


    FUNCTION f_find_missing_data(
            p_proxy_idm          gpbprxy.gpbprxy_proxy_idm%TYPE,
    p_first_name         gpbprxy.gpbprxy_first_name%TYPE DEFAULT NULL,
    p_mi                 gpbprxy.gpbprxy_mi%TYPE DEFAULT NULL,
    p_last_name          gpbprxy.gpbprxy_last_name%TYPE DEFAULT NULL,
    p_surname_prefix     gpbprxy.gpbprxy_surname_prefix%TYPE DEFAULT NULL,
    p_name_prefix        gpbprxy.gpbprxy_name_prefix%TYPE DEFAULT NULL,
    p_name_suffix        gpbprxy.gpbprxy_name_suffix%TYPE DEFAULT NULL,
    p_pref_first_name    gpbprxy.gpbprxy_pref_first_name%TYPE DEFAULT NULL,
    p_email_address      gpbprxy.gpbprxy_email_address%TYPE DEFAULT NULL,
    p_phone_area         gpbprxy.gpbprxy_phone_area%TYPE DEFAULT NULL,
    p_phone_number       gpbprxy.gpbprxy_phone_number%TYPE DEFAULT NULL,
    p_phone_ext          gpbprxy.gpbprxy_phone_ext%TYPE DEFAULT NULL,
    p_ctry_code_phone    gpbprxy.gpbprxy_ctry_code_phone%TYPE DEFAULT NULL,
    p_house_number       gpbprxy.gpbprxy_house_number%TYPE DEFAULT NULL,
    p_street_line1       gpbprxy.gpbprxy_street_line1%TYPE DEFAULT NULL,
    p_street_line2       gpbprxy.gpbprxy_street_line2%TYPE DEFAULT NULL,
    p_street_line3       gpbprxy.gpbprxy_street_line3%TYPE DEFAULT NULL,
    p_street_line4       gpbprxy.gpbprxy_street_line4%TYPE DEFAULT NULL,
    p_city               gpbprxy.gpbprxy_city%TYPE DEFAULT NULL,
    p_stat_code          gpbprxy.gpbprxy_stat_code%TYPE DEFAULT NULL,
    p_zip                gpbprxy.gpbprxy_zip%TYPE DEFAULT NULL,
    p_cnty_code          gpbprxy.gpbprxy_cnty_code%TYPE DEFAULT NULL,
    p_natn_code          gpbprxy.gpbprxy_natn_code%TYPE DEFAULT NULL,
    p_sex                gpbprxy.gpbprxy_sex%TYPE DEFAULT NULL,
    p_birth_date         VARCHAR2 DEFAULT NULL,
    p_ssn                gpbprxy.gpbprxy_ssn%TYPE DEFAULT NULL)
    RETURN VARCHAR2
    IS
    lv_message            VARCHAR2 (30000);
    lv_info               twgrinfo.twgrinfo_label%TYPE := 'SAVED';
    lv_req_ind            gtvotyp.gtvotyp_option_default%TYPE;
    -- Determine whether to display the profile element
    -- N means no display or no rules where found or no relationship records exist yet
    -- V means element is visible but not required
    -- Y means element is visible and required
    FUNCTION F_Required (p_OTYP gtvotyp.gtvotyp_code%TYPE)
    RETURN VARCHAR2
    IS
    lv_option_ind   gtvotyp.gtvotyp_option_default%TYPE;
    lv_max_ind      gtvotyp.gtvotyp_option_default%TYPE;

    CURSOR C_RETPlist
    IS
    SELECT DISTINCT GPRXREF_RETP_CODE
    FROM GPRXREF
    WHERE GPRXREF_PROXY_IDM = p_proxy_idm
    AND TRUNC (SYSDATE) BETWEEN TRUNC (GPRXREF_START_DATE)
    AND TRUNC (GPRXREF_STOP_DATE);
    BEGIN
    lv_max_ind := 'N';

    FOR lv_code IN C_RETPlist
    LOOP
    lv_option_ind :=
    NVL (bwgkprxy.F_GetOption (p_OTYP, lv_code.GPRXREF_RETP_CODE),'N');

    IF lv_option_ind > lv_max_ind
    THEN
    lv_max_ind := lv_option_ind;
    END IF;
    END LOOP;

    RETURN lv_max_ind;
    END F_Required;
    --
    --
    --
    PROCEDURE P_Check_If_Missing (p_parm VARCHAR2, p_data VARCHAR2, p_msg VARCHAR2)
    IS
    BEGIN
    lv_req_ind := F_Required (p_parm);

    IF lv_req_ind = 'Y' AND goksels.f_clean_text(p_data) IS NULL
    THEN
    lv_info := 'REQUIRED';
    lv_message :=
    lv_message || ' : ' || p_msg;
    END IF;
    END P_Check_If_Missing;
    --
    --
    --
    BEGIN
    lv_message := 'required_data_missing';

    -- Check first name (always required)
    IF goksels.f_clean_text(p_first_name) IS NULL
    THEN
    lv_info := 'REQUIRED';
    lv_message :=
    lv_message || ' : ' || 'p_first_name';
    END IF;

    -- Check last name (always required)
    IF goksels.f_clean_text(p_last_name) IS NULL
    THEN
    lv_info := 'REQUIRED';
    lv_message :=
    lv_message || ' : ' || 'p_last_name';
    END IF;

    -- Check e-mail address (always required)
    IF goksels.f_clean_text(p_email_address) IS NULL
    THEN
    lv_info := 'REQUIRED';
    lv_message :=
    lv_message || ' : ' || 'p_email_address';
    END IF;

    -- Check name prefix (salutation)
    P_Check_If_Missing('PROFILE_NAME_PREFIX',     p_name_prefix,     'p_name_prefix');
    -- Check middle name
    P_Check_If_Missing('PROFILE_MI',              p_mi,              'p_mi');
    -- Check surname prefix
    P_Check_If_Missing('PROFILE_SURNAME_PREFIX',  p_surname_prefix,  'p_surname_prefix');
    -- Check name suffix
    P_Check_If_Missing('PROFILE_NAME_SUFFIX',     p_name_suffix,     'p_name_suffix');
    -- Check preferred first name (nickname)
    P_Check_If_Missing('PROFILE_PREF_FIRST_NAME', p_pref_first_name, 'p_pref_first_name');
    -- Check phone area code
    P_Check_If_Missing('PROFILE_PHONE_AREA',      p_phone_area,      'p_phone_area');
    -- Check phone number
    P_Check_If_Missing('PROFILE_PHONE_NUMBER',    p_phone_number,    'p_phone_number');
    -- Check phone extension
    P_Check_If_Missing('PROFILE_PHONE_EXT',       p_phone_ext,       'p_phone_ext');
    -- Check phone country code
    P_Check_If_Missing('PROFILE_PHONE_COUNTRY',   p_ctry_code_phone, 'p_ctry_code_phone');
    -- Check house number
    P_Check_If_Missing('PROFILE_HOUSE_NUMBER',    p_house_number,    'p_house_number');
    -- Check address line 1
    P_Check_If_Missing('PROFILE_STREET_LINE1',    p_street_line1,    'p_street_line1');
    -- Check address line 2
    P_Check_If_Missing('PROFILE_STREET_LINE2',    p_street_line2,    'p_street_line2');
    -- Check address line 3
    P_Check_If_Missing('PROFILE_STREET_LINE3',    p_street_line3,    'p_street_line3');
    -- Check address line 4
    P_Check_If_Missing('PROFILE_STREET_LINE4',    p_street_line4,    'p_street_line4');
    -- Check city
    P_Check_If_Missing('PROFILE_CITY',            p_city,            'p_city');
    -- Check state
    P_Check_If_Missing('PROFILE_STAT_CODE',       p_stat_code,       'p_stat_code');
    -- Check zipcode
    P_Check_If_Missing('PROFILE_ZIP',             p_zip,             'p_zip');
    -- Check county
    P_Check_If_Missing('PROFILE_CNTY_CODE',       p_cnty_code,       'p_cnty_code');
    -- Check nation
    P_Check_If_Missing('PROFILE_NATN_CODE',       p_natn_code,       'p_natn_code');
    -- Check gender
    P_Check_If_Missing('PROFILE_SEX',             p_sex,             'p_sex');
    -- Check national identifier
    P_Check_If_Missing('PROFILE_SSN',             p_ssn,             'p_ssn');
    -- Check birth date
    P_Check_If_Missing('PROFILE_BIRTH_DATE',      p_birth_date,      'p_birth_date');
    -- Also check validity of date
    IF p_birth_date IS NOT NULL THEN
    IF p_birth_date IS NULL THEN
    lv_info := 'REQUIRED';
    lv_message :=
    lv_message || ' : ' || 'p_birth_date_format_error';
    END IF;
    END IF;

    IF lv_info <> 'REQUIRED'
    THEN
    lv_message := NULL;
    END IF;

    RETURN lv_message;

    END f_find_missing_data;

    BEGIN

    lv_message := f_find_missing_data(
    p_proxy_idm        => ?,
    p_first_name       => ?,
    p_mi               => ?,
    p_last_name        => ?,
    p_surname_prefix   => ?,
    p_name_prefix      => ?,
    p_name_suffix      => ?,
    p_pref_first_name  => ?,
    p_email_address    => ?,
    p_phone_area       => ?,
    p_phone_number     => ?,
    p_phone_ext        => ?,
    p_ctry_code_phone  => ?,
    p_house_number     => ?,
    p_street_line1     => ?,
    p_street_line2     => ?,
    p_street_line3     => ?,
    p_street_line4     => ?,
    p_city             => ?,
    p_stat_code        => ?,
    p_zip              => ?,
    p_cnty_code        => ?,
    p_natn_code        => ?,
    p_sex              => ?,
    p_birth_date       => ?,
    p_ssn              => ?);

    ? := lv_message;

    END;
    """


    public final static String STORE_LOGIN_IN_HISTORY = """
    DECLARE
    lv_hold_rowid  gb_common.internal_record_id_type;
    lv_RETP        gtvretp.gtvretp_code%TYPE;
    pidm           GPRXREF.GPRXREF_PERSON_PIDM%TYPE;
    --  
    CURSOR refProxy is SELECT DISTINCT GPRXREF_PERSON_PIDM
    FROM GPRXREF
    WHERE GPRXREF_PROXY_IDM = ?;
    --
    BEGIN
    OPEN refProxy;
    FETCH refProxy INTO pidm;
    CLOSE refProxy;
    --
    
   IF pidm IS NOT NULL THEN
    lv_RETP := gp_gprxref.F_GetXREF_RETP (?, pidm);

    IF bwgkprxy.F_GetOption ('LOGIN_IN_HISTORY', lv_RETP) = 'Y'
      THEN

         gp_gprhist.P_Create (
            p_proxy_idm    => to_number(?),
            p_person_pidm  => pidm,
            p_page_name    => ?,
            p_old_auth_ind => 'L',
            p_new_auth_ind => 'L',
            p_create_user  => goksels.f_get_ssb_id_context,
            p_create_date  => SYSDATE,
            p_user_id      => goksels.f_get_ssb_id_context,
            p_rowid_out    => lv_hold_rowid
            );
      --
            gb_common.P_Commit;
      END IF;
    END IF;
    END;
    """

    public final static String STORE_PAGE_ACCESS_IN_HISTORY = """

 DECLARE
    lv_hold_rowid  gb_common.internal_record_id_type;
    lv_RETP        gtvretp.gtvretp_code%TYPE;
    pidm           GPRXREF.GPRXREF_PERSON_PIDM%TYPE;
    --  
       
  BEGIN
  
   pidm := ?;
   
   IF pidm IS NOT NULL THEN
  
   lv_RETP := gp_gprxref.F_GetXREF_RETP (pidm, ?);
   
     IF bwgkprxy.F_GetOption ('PAGE_DISPLAY_IN_HISTORY', lv_RETP) = 'Y'
      THEN
         gp_gprhist.P_Create (
            p_proxy_idm    => ?,
            p_person_pidm  => pidm,
            p_page_name    => ?,
            p_old_auth_ind => 'V',
            p_new_auth_ind => 'V',
            p_create_user  => goksels.f_get_ssb_id_context,
            p_create_date  => SYSDATE,
            p_user_id      => goksels.f_get_ssb_id_context,
            p_rowid_out    => lv_hold_rowid
            );
    --
            gb_common.P_Commit;
      END IF;
    --
     END IF;
    END;
    """

    public final static String UPDATE_PROFILE = """
     DECLARE
      lv_GPBPRXY_rec        gp_gpbprxy.gpbprxy_rec;
      lv_GPBPRXY_ref        gp_gpbprxy.gpbprxy_ref;

      lv_opt_out_adv_date   DATE := SYSDATE;

      lv_info               twgrinfo.twgrinfo_label%TYPE;
      
      lv_hold_rowid         gb_common.internal_record_id_type;
      lv_message            VARCHAR2 (30000);
      
      lv_email1             gpbprxy.gpbprxy_email_address%TYPE;
      lv_email2             gpbprxy.gpbprxy_email_address%TYPE;
      
      lv_temp_fmt              VARCHAR2 (30);
      
      hold_proxy_idm        gpbprxy.gpbprxy_proxy_idm%TYPE;
      
      error_status     VARCHAR2(1) := 'N';
      email_change     VARCHAR2(1) := 'N';



         FUNCTION GET_DATE(p_ind VARCHAR2) RETURN DATE
         IS
         BEGIN
           CASE
             WHEN p_ind = 'N'
           THEN
             RETURN NULL;
           ELSE
             RETURN SYSDATE;
           END CASE;
         END GET_DATE;

     BEGIN
        dbms_session.set_nls('NLS_CALENDAR',''''||'GREGORIAN'||'''');
        lv_temp_fmt := twbklibs.date_input_fmt;
        twbklibs.date_input_fmt := 'MM/DD/YYYY';
      
     hold_proxy_idm := ?;
     
          -- Get the proxy record
        lv_GPBPRXY_ref := gp_gpbprxy.F_Query_One (hold_proxy_idm);
        FETCH lv_GPBPRXY_ref INTO lv_GPBPRXY_rec;
        CLOSE lv_GPBPRXY_ref;

        gp_gpbprxy.P_Update (
          p_proxy_idm    => hold_proxy_idm,
          p_first_name   => ?,
          p_last_name    => ?,
          p_user_id      => goksels.f_get_ssb_id_context,
          p_rowid        => lv_GPBPRXY_rec.R_INTERNAL_RECORD_ID
          );

            -- Update everything else except e-mail
                      -- but verify birthdate and null out if invalid
                          BEGIN
                             gp_gpbprxy.P_Update (
                                p_proxy_idm          => hold_proxy_idm,
                                p_mi                 => goksels.f_clean_text(?),
                                p_surname_prefix     => goksels.f_clean_text(?),
                                p_name_prefix        => goksels.f_clean_text(?),
                                p_name_suffix        => goksels.f_clean_text(?),
                                p_pref_first_name    => goksels.f_clean_text(?),
                                p_phone_area         => goksels.f_clean_text(?),
                                p_phone_number       => goksels.f_clean_text(?),
                                p_phone_ext          => goksels.f_clean_text(?),
                                p_ctry_code_phone    => goksels.f_clean_text(?),
                                p_house_number       => goksels.f_clean_text(?),
                                p_street_line1       => goksels.f_clean_text(?),
                                p_street_line2       => goksels.f_clean_text(?),
                                p_street_line3       => goksels.f_clean_text(?),
                                p_street_line4       => goksels.f_clean_text(?),
                                p_city               => goksels.f_clean_text(?),
                                p_stat_code          => goksels.f_clean_text(?),
                                p_zip                => goksels.f_clean_text(?),
                                p_cnty_code          => goksels.f_clean_text(?),
                                p_natn_code          => goksels.f_clean_text(?),
                                p_sex                => goksels.f_clean_text(?),
                                p_birth_date         => TO_DATE (?, twbklibs.date_input_fmt),
                                p_ssn                => goksels.f_clean_text(?),
                                p_opt_out_adv_date   => GET_DATE(?),
                                p_user_id            => goksels.f_get_ssb_id_context,
                                p_rowid              => lv_GPBPRXY_rec.R_INTERNAL_RECORD_ID);
                          EXCEPTION
                             WHEN OTHERS THEN lv_info := 'DATA_ERROR';
                             error_status := 'Y';
                          END;
                          
           twbklibs.date_input_fmt := lv_temp_fmt;
                          
           lv_message :=
           lv_message || ? || ' ' || lv_GPBPRXY_rec.R_FIRST_NAME || ' ' || lv_GPBPRXY_rec.R_LAST_NAME || ' ' || '<P>';
                          
         gp_gpbeltr.P_Create (
         p_syst_code        => 'PROXY',
         p_ctyp_code        => 'PROFILE_CHANGE_CLR',
         p_ctyp_url         => bwgkprxy.F_getProxyURL('PROFILE_CHANGE') || twbkbssf.F_Encode (lv_hold_rowid),
         p_ctyp_exp_date    => NULL,
         p_ctyp_exe_date    => NULL,
         p_transmit_date    => NULL,
         p_proxy_idm        => hold_proxy_idm,
         p_proxy_old_data   => NULL,
         p_proxy_new_data   => NULL,
         p_person_pidm      => bwgkprxy.F_Get_PIDM_For_IDM(hold_proxy_idm),
         p_user_id          => goksels.f_get_ssb_id_context,
         p_create_date      => SYSDATE,
         p_create_user      => goksels.f_get_ssb_id_context,
         p_rowid_out        => lv_hold_rowid);

         gb_common.P_Commit;

         bwgkprxy.P_SendEmail (lv_hold_rowid, lv_message);

          -- Update match-n-load tables for insert/update into General Person
          bwgkprxy.P_MatchLoad (hold_proxy_idm);

          gb_common.P_Commit;
          
      -- Email address change requested by proxy
      
      lv_email1 := TRIM(LOWER (lv_GPBPRXY_rec.R_EMAIL_ADDRESS));
      lv_email2 := TRIM(LOWER (?));
      IF goksels.f_clean_text(lv_email2) IS NOT NULL AND lv_email1 <> lv_email2
      THEN
         -- Fetch a proxy record based on new email address
         -- If you find existing record then don't make the change
         lv_GPBPRXY_ref := gp_gpbprxy.F_Query_One_By_Email (lv_email2);

         FETCH lv_GPBPRXY_ref INTO lv_GPBPRXY_rec;

         IF lv_GPBPRXY_ref%FOUND
         THEN
            lv_info := 'EMAIL_DUPLICATE';
            error_status := 'Y';
            CLOSE lv_GPBPRXY_ref;
        ELSE
            lv_info := 'NEW_EMAIL';
            email_change := 'Y';
            -- Send first message using existing e-mail address with CANCEL_EMAIL action
            gp_gpbeltr.P_Create (
               p_syst_code        => 'PROXY',
               p_ctyp_code        => 'CANCEL_EMAIL_NOA',
               p_ctyp_url         => NULL,
               p_ctyp_exp_date    => SYSDATE
                                    + bwgkprxy.F_GetOption (
                                         'ACTION_VALID_DAYS'),
               p_ctyp_exe_date    => NULL,
               p_transmit_date    => NULL,
               p_proxy_idm        => hold_proxy_idm,
               p_proxy_old_data   => lv_email1,
               p_proxy_new_data   => lv_email2,
               p_person_pidm      => bwgkprxy.F_Get_PIDM_For_IDM(hold_proxy_idm),
               p_user_id          => goksels.f_get_ssb_id_context,
               p_create_date      => SYSDATE,
               p_create_user      => goksels.f_get_ssb_id_context,
               p_rowid_out        => lv_hold_rowid);

            gp_gpbeltr.P_Update (
               p_ctyp_url   => bwgkprxy.F_getProxyURL('CANCEL_EMAIL') || twbkbssf.F_Encode (lv_hold_rowid),
               p_user_id    => goksels.f_get_ssb_id_context,
               p_rowid      => lv_hold_rowid);

            gb_common.P_Commit;
            bwgkprxy.P_SendEmail (lv_hold_rowid);
            
            
             gp_gpbeltr.P_Create (
      p_syst_code      => 'PROXY',
      p_ctyp_code      => 'NEW_PROXY_ACCESS_CODE',
      p_ctyp_url       => NULL,
      p_ctyp_exp_date  => SYSDATE + bwgkprxy.F_GetOption ('ACTION_VALID_DAYS'),
      p_ctyp_exe_date  => NULL,
      p_transmit_date  => NULL,
      p_proxy_idm      => hold_proxy_idm,
      p_proxy_old_data => NULL,
      p_proxy_new_data => NULL,
      p_person_pidm    => NULL,
      p_user_id        => goksels.f_get_ssb_id_context,
      p_create_date    => SYSDATE,
      p_create_user    => goksels.f_get_ssb_id_context,
      p_rowid_out      => lv_hold_rowid
      );
      
      gb_common.P_Commit;
      bwgkprxy.P_SendEmail (lv_hold_rowid);

            -- Send second message using updated e-mail address with NEW_EMAIL action
            -- Expire the PIN with 'e-mail change pending' indicator
            gp_gpbprxy.P_Update (p_proxy_idm          => hold_proxy_idm,
                                 p_email_address      => lv_email2,
                                 p_pin_disabled_ind   => 'E',
                                 p_inv_login_cnt      => 0,
                                 p_email_ver_date     => NULL,
                                 p_user_id            => goksels.f_get_ssb_id_context);

            gp_gpbeltr.P_Create (
               p_syst_code        => 'PROXY',
               p_ctyp_code        => 'NEW_EMAIL_NOA',
               p_ctyp_url         => NULL,
               p_ctyp_exp_date    => SYSDATE
                                    + bwgkprxy.F_GetOption (
                                         'ACTION_VALID_DAYS'),
               p_ctyp_exe_date    => NULL,
               p_transmit_date    => NULL,
               p_proxy_idm        => hold_proxy_idm,
               p_proxy_old_data   => lv_email1,
               p_proxy_new_data   => lv_email2,
               p_person_pidm      => bwgkprxy.F_Get_PIDM_For_IDM(hold_proxy_idm),
               p_user_id          => goksels.f_get_ssb_id_context,
               p_create_date      => SYSDATE,
               p_create_user      => goksels.f_get_ssb_id_context,
               p_rowid_out        => lv_hold_rowid);

            gp_gpbeltr.P_Update (
               p_ctyp_url   => bwgkprxy.F_getProxyURL('NEW_EMAIL') || twbkbssf.F_Encode (lv_hold_rowid),
               p_user_id    => goksels.f_get_ssb_id_context,
               p_rowid      => lv_hold_rowid);

            gb_common.P_Commit;
            bwgkprxy.P_SendEmail (lv_hold_rowid);
            
                       
             gp_gpbeltr.P_Create (
      p_syst_code      => 'PROXY',
      p_ctyp_code      => 'NEW_PROXY_ACCESS_CODE',
      p_ctyp_url       => NULL,
      p_ctyp_exp_date  => SYSDATE + bwgkprxy.F_GetOption ('ACTION_VALID_DAYS'),
      p_ctyp_exe_date  => NULL,
      p_transmit_date  => NULL,
      p_proxy_idm      => hold_proxy_idm,
      p_proxy_old_data => NULL,
      p_proxy_new_data => NULL,
      p_person_pidm    => NULL,
      p_user_id        => goksels.f_get_ssb_id_context,
      p_create_date    => SYSDATE,
      p_create_user    => goksels.f_get_ssb_id_context,
      p_rowid_out      => lv_hold_rowid
      );
      
      gb_common.P_Commit;
      bwgkprxy.P_SendEmail (lv_hold_rowid);
            
             CLOSE lv_GPBPRXY_ref;
             error_status := 'N';
         END IF;
      END IF;    
         ? := lv_info;
         ? := error_status;
         ? := email_change;
      END ;

          """
    }

