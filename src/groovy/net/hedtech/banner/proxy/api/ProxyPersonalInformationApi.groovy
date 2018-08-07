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

         ? := gp_gpbprxy.F_Query_One (to_number(?));

         END;
    """


    public final static String CHECK_PROXY_PROFILE_REQUIRED_DATA = """
    DECLARE
    lv_message VARCHAR2 (30000);

    FUNCTION f_validate_date (p_date VARCHAR2)
    RETURN VARCHAR2
    IS
    f_date    DATE;
    BEGIN
    IF p_date IS NOT NULL THEN
    BEGIN
    IF twbkwbis.f_isdate (p_date, twbklibs.date_input_fmt) THEN
    f_date := twbkwbis.f_fmtdate (p_date);
    IF trunc(( sysdate - f_date)/365) > 150 THEN -- if the birthdate makes them over 150, error
    RETURN NULL;
    ELSE
    RETURN p_date;
    END IF;
    ELSE
    RETURN NULL;
    END IF;
    EXCEPTION
    WHEN OTHERS THEN
    RETURN NULL;
    END;
    END IF;
    RETURN NULL;
    END f_validate_date;

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
    lv_message := G\$_NLS.Get ('BWGKPXYA1-0000', 'SQL', 'Required data missing');

    -- Check first name (always required)
    IF goksels.f_clean_text(p_first_name) IS NULL
    THEN
    lv_info := 'REQUIRED';
    lv_message :=
    lv_message || ' : ' || G\$_NLS.Get ('BWGKPXYA1-0001', 'SQL', 'First Name');
    END IF;

    -- Check last name (always required)
    IF goksels.f_clean_text(p_last_name) IS NULL
    THEN
    lv_info := 'REQUIRED';
    lv_message :=
    lv_message || ' : ' || G\$_NLS.Get ('BWGKPXYA1-0002', 'SQL', 'Last Name');
    END IF;

    -- Check e-mail address (always required)
    IF goksels.f_clean_text(p_email_address) IS NULL
    THEN
    lv_info := 'REQUIRED';
    lv_message :=
    lv_message || ' : ' || G\$_NLS.Get ('BWGKPXYA1-0003', 'SQL', 'E-Mail Address');
    END IF;

    -- Check name prefix (salutation)
    P_Check_If_Missing('PROFILE_NAME_PREFIX',     p_name_prefix,     G\$_NLS.Get ('BWGKPXYA1-0004', 'SQL', 'Salutation'));
    -- Check middle name
    P_Check_If_Missing('PROFILE_MI',              p_mi,              G\$_NLS.Get ('BWGKPXYA1-0005', 'SQL', 'Middle Name'));
    -- Check surname prefix
    P_Check_If_Missing('PROFILE_SURNAME_PREFIX',  p_surname_prefix,  G\$_NLS.Get ('BWGKPXYA1-0006', 'SQL', 'Surname Prefix'));
    -- Check name suffix
    P_Check_If_Missing('PROFILE_NAME_SUFFIX',     p_name_suffix,     G\$_NLS.Get ('BWGKPXYA1-0007', 'SQL', 'Name Suffix'));
    -- Check preferred first name (nickname)
    P_Check_If_Missing('PROFILE_PREF_FIRST_NAME', p_pref_first_name, G\$_NLS.Get ('BWGKPXYA1-0008', 'SQL', 'Nickname'));
    -- Check phone area code
    P_Check_If_Missing('PROFILE_PHONE_AREA',      p_phone_area,      G\$_NLS.Get ('BWGKPXYA1-0009', 'SQL', 'Phone Area Code'));
    -- Check phone number
    P_Check_If_Missing('PROFILE_PHONE_NUMBER',    p_phone_number,    G\$_NLS.Get ('BWGKPXYA1-0010', 'SQL', 'Phone Number'));
    -- Check phone extension
    P_Check_If_Missing('PROFILE_PHONE_EXT',       p_phone_ext,       G\$_NLS.Get ('BWGKPXYA1-0011', 'SQL', 'Phone Extension'));
    -- Check phone country code
    P_Check_If_Missing('PROFILE_PHONE_COUNTRY',   p_ctry_code_phone, G\$_NLS.Get ('BWGKPXYA1-0012', 'SQL', 'Phone Country Code'));
    -- Check house number
    P_Check_If_Missing('PROFILE_HOUSE_NUMBER',    p_house_number,    G\$_NLS.Get ('BWGKPXYA1-0013', 'SQL', 'House Number'));
    -- Check address line 1
    P_Check_If_Missing('PROFILE_STREET_LINE1',    p_street_line1,    G\$_NLS.Get ('BWGKPXYA1-0014', 'SQL', 'Address Line 1'));
    -- Check address line 2
    P_Check_If_Missing('PROFILE_STREET_LINE2',    p_street_line2,    G\$_NLS.Get ('BWGKPXYA1-0015', 'SQL', 'Address Line 2'));
    -- Check address line 3
    P_Check_If_Missing('PROFILE_STREET_LINE3',    p_street_line3,    G\$_NLS.Get ('BWGKPXYA1-0016', 'SQL', 'Address Line 3'));
    -- Check address line 4
    P_Check_If_Missing('PROFILE_STREET_LINE4',    p_street_line4,    G\$_NLS.Get ('BWGKPXYA1-0017', 'SQL', 'Address Line 4'));
    -- Check city
    P_Check_If_Missing('PROFILE_CITY',            p_city,            G\$_NLS.Get ('BWGKPXYA1-0018', 'SQL', 'City'));
    -- Check state
    P_Check_If_Missing('PROFILE_STAT_CODE',       p_stat_code,       G\$_NLS.Get ('BWGKPXYA1-0019', 'SQL', 'State'));
    -- Check zipcode
    P_Check_If_Missing('PROFILE_ZIP',             p_zip,             G\$_NLS.Get ('BWGKPXYA1-0020', 'SQL', 'Zipcode'));
    -- Check county
    P_Check_If_Missing('PROFILE_CNTY_CODE',       p_cnty_code,       G\$_NLS.Get ('BWGKPXYA1-0021', 'SQL', 'County'));
    -- Check nation
    P_Check_If_Missing('PROFILE_NATN_CODE',       p_natn_code,       G\$_NLS.Get ('BWGKPXYA1-0022', 'SQL', 'Nation'));
    -- Check gender
    P_Check_If_Missing('PROFILE_SEX',             p_sex,             G\$_NLS.Get ('BWGKPXYA1-0023', 'SQL', 'Gender'));
    -- Check national identifier
    P_Check_If_Missing('PROFILE_SSN',             p_ssn,             G\$_NLS.Get ('BWGKPXYA1-0024', 'SQL', 'SSN/SIN/TIN'));
    -- Check birth date
    P_Check_If_Missing('PROFILE_BIRTH_DATE',      p_birth_date,      G\$_NLS.Get ('BWGKPXYA1-0025', 'SQL', 'Birthdate'));
    -- Also check validity of date
    IF p_birth_date IS NOT NULL THEN
    IF f_validate_date(p_birth_date) IS NULL THEN
    lv_info := 'REQUIRED';
    lv_message :=
    lv_message || ' : ' || g\$_nls.get ('BWGKPXYA1-0026', 'SQL', 'Birthdate %01% has invalid date format or values.' ,p_birth_date);
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

pidm GPRXREF.GPRXREF_PERSON_PIDM%TYPE;

CURSOR refProxy is SELECT DISTINCT GPRXREF_PERSON_PIDM
FROM GPRXREF
WHERE GPRXREF_PROXY_IDM = ?;

BEGIN
OPEN refProxy;
FETCH refProxy INTO pidm;
CLOSE refProxy;


 lv_RETP := gp_gprxref.F_GetXREF_RETP (?, pidm);

   IF bwgkprxy.F_GetOption ('LOGIN_IN_HISTORY', lv_RETP) = 'Y'
      THEN
         gp_gprhist.P_Create (
            p_proxy_idm    => to_number(?),
            p_person_pidm  => pidm,
            p_page_name    => G\$_NLS.Get ('BWGKPXYA1-0055', 'SQL', 'Display authorization menu'),
            p_old_auth_ind => 'L',
            p_new_auth_ind => 'L',
            p_create_user  => goksels.f_get_ssb_id_context,
            p_create_date  => SYSDATE,
            p_user_id      => goksels.f_get_ssb_id_context,
            p_rowid_out    => lv_hold_rowid
            );

            gb_common.P_Commit;
      END IF;

END;

    """

    public final static String UPDATE_PROFILE = """
     DECLARE
      lv_GPBPRXY_rec        gp_gpbprxy.gpbprxy_rec;
      lv_GPBPRXY_ref        gp_gpbprxy.gpbprxy_ref;

      lv_opt_out_adv_date   DATE := SYSDATE;

      lv_info               twgrinfo.twgrinfo_label%TYPE := 'SAVED';

         FUNCTION f_validate_date (p_date VARCHAR2)
           RETURN VARCHAR2
         IS
           f_date    DATE;
         BEGIN
           IF p_date IS NOT NULL THEN
             BEGIN
               IF twbkwbis.f_isdate (p_date, twbklibs.date_input_fmt) THEN
                 f_date := twbkwbis.f_fmtdate (p_date);
                 IF trunc(( sysdate - f_date)/365) > 150 THEN -- if the birthdate makes them over 150, error
                   RETURN NULL;
                 ELSE
                   RETURN p_date;
                 END IF;
               ELSE
                 RETURN NULL;
               END IF;
             EXCEPTION
               WHEN OTHERS THEN
                 RETURN NULL;
             END;
           END IF;
           RETURN NULL;
         END f_validate_date;

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
          -- Get the proxy record
        lv_GPBPRXY_ref := gp_gpbprxy.F_Query_One (?);
        FETCH lv_GPBPRXY_ref INTO lv_GPBPRXY_rec;
        CLOSE lv_GPBPRXY_ref;

        gp_gpbprxy.P_Update (
          p_proxy_idm    => ?,
          p_first_name   => ?,
          p_last_name    => ?,
          p_user_id      => goksels.f_get_ssb_id_context,
          p_rowid        => lv_GPBPRXY_rec.R_INTERNAL_RECORD_ID
          );

            -- Update everything else except e-mail
                      -- but verify birthdate and null out if invalid
                          BEGIN
                             gp_gpbprxy.P_Update (
                                p_proxy_idm          => ?,
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
                                p_birth_date         => TO_DATE (f_validate_date(?), twbklibs.date_input_fmt),
                                p_ssn                => goksels.f_clean_text(?),
                                p_opt_out_adv_date   => GET_DATE(?),
                                p_user_id            => goksels.f_get_ssb_id_context,
                                p_rowid              => lv_GPBPRXY_rec.R_INTERNAL_RECORD_ID);
                          EXCEPTION
                             WHEN OTHERS THEN lv_info := 'DATA_ERROR';
                          END;

          -- Update match-n-load tables for insert/update into General Person
          bwgkprxy.P_MatchLoad (?);

          gb_common.P_Commit;

      END ;

          """
    }

