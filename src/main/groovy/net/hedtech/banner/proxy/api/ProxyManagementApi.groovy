/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.proxy.api

class ProxyManagementApi {

    public final static String PROXY_LIST = """
    DECLARE

    proxies varchar2(3000);
    student varchar2(3000);

    listOfProxies varchar2(3000);

    lv_GPBPRXY_rec gp_gpbprxy.gpbprxy_rec;
    lv_GPBPRXY_ref gp_gpbprxy.gpbprxy_ref;
    lv_GPRXREF_rec gp_gprxref.gprxref_rec;
    lv_GPRXREF_ref gp_gprxref.gprxref_ref;

    CURSOR C_ProxyList
    RETURN GPRXREF%ROWTYPE
            IS
    SELECT GPRXREF.*
    FROM GPRXREF, GPBPRXY
    WHERE GPRXREF_PROXY_IDM = GPBPRXY_PROXY_IDM
    AND GPRXREF_PERSON_PIDM = ?
    ORDER BY GPBPRXY_LAST_NAME, GPBPRXY_FIRST_NAME;
    BEGIN

    proxies := '"proxies":[';

    FOR proxy IN C_ProxyList LOOP

    lv_GPBPRXY_ref := gp_gpbprxy.F_Query_One (proxy.GPRXREF_PROXY_IDM);

    FETCH lv_GPBPRXY_ref INTO lv_GPBPRXY_rec;

    student := '{' ||
    '"gidm" ' || ':' || '"' || lv_GPBPRXY_rec.R_PROXY_IDM || '"' ||
    ',"firstName" ' || ':' || '"' || lv_GPBPRXY_rec.R_FIRST_NAME || '"' ||
    ',"lastName" ' || ':' || '"' || lv_GPBPRXY_rec.R_LAST_NAME || '"' ||
    ',"email" ' || ':' || '"' || lv_GPBPRXY_rec.R_EMAIL_ADDRESS || '"' ||
    '},';

    proxies := proxies || student;

    END LOOP;

    proxies := TRIM(TRAILING ',' FROM proxies );
    proxies := proxies || ']';

    listOfProxies := '{' || proxies || '}';

    ? := listOfProxies;

    --dbms_output.put_line(listOfProxies);

    END;
"""

    public final static String CREATE_PROXY = """
DECLARE
   lv_proxyIDM    gpbprxy.gpbprxy_proxy_idm%TYPE;
   lv_hold_rowid  gb_common.internal_record_id_type;
   lv_GPRXREF_rec gp_gprxref.gprxref_rec;
   lv_GPRXREF_ref gp_gprxref.gprxref_ref;
   lv_info        twgrinfo.twgrinfo_label%TYPE := 'PROXYEXISTS';
   error_status   VARCHAR2(1) := 'N';
   lv_proxy_url   twgbparm.twgbparm_param_value%TYPE;
   global_pidm    spriden.spriden_pidm%TYPE;
   p_email         goremal.goremal_email_address%TYPE;
   p_email_verify goremal.goremal_email_address%TYPE;
   p_last          spriden.spriden_last_name%TYPE;
   p_first         spriden.spriden_first_name%TYPE;
   lv_at_loc      INTEGER := 0;
   lv_dot_loc     INTEGER := 0;
   lv_double      INTEGER := 0;
--
--   
     -- to apply mep code to Banner9 url for proxy e-mail communication
   FUNCTION F_ApplyMepCodeToProxyURL (endpoint IN VARCHAR2) RETURN VARCHAR2 IS
   BEGIN
    IF g\$_vpdi_security.G\$_IS_MIF_ENABLED THEN
           IF INSTR(endpoint,'?') != 0 THEN
         RETURN REPLACE(endpoint,'?','?mepCode='|| g\$_vpdi_security.G\$_VPDI_GET_INST_CODE_FNC || '&');
    ELSE
         RETURN (endpoint || '?mepCode=' || g\$_vpdi_security.G\$_VPDI_GET_INST_CODE_FNC);
    END IF;
    ELSE
        RETURN endpoint;
    END IF;
   END F_ApplyMepCodeToProxyURL;
-- 
--
   -- Get the URL for accessing Banner 9 Proxy
   FUNCTION F_getProxyURL(
      p_action          VARCHAR2 DEFAULT NULL)
      RETURN VARCHAR2
   IS
      CURSOR gurocfg_c (
         p_appid_in gurocfg.gurocfg_gubappl_app_id%TYPE,
         p_config_in gurocfg.gurocfg_name%TYPE)
      IS
       SELECT gurocfg_value
         FROM gurocfg
        WHERE gurocfg_gubappl_app_id = p_appid_in
          AND gurocfg_name      =  p_config_in
          AND gurocfg_type      =  'string';

      lv_use_ban9 VARCHAR2(1);
      lv_key VARCHAR2(1000);
      lv_endpoint VARCHAR2(60);
      lv_proxy_url VARCHAR2(1000);
   BEGIN
--
         OPEN gurocfg_c('GENERAL_SS', 'GENERALLOCATION');
         FETCH gurocfg_c INTO lv_proxy_url;
         
         IF gurocfg_c%NOTFOUND
         THEN
            raise_application_error(-20103, 'Could not build URL for proxy e-mail communication');
         END IF;
         CLOSE gurocfg_c;

         lv_key := 'proxyAccessURL.' || p_action;
         OPEN gurocfg_c('BAN9_PROXY', lv_key);
         FETCH gurocfg_c INTO lv_endpoint;

         -- process mep context
         -- it adds the mepCode parameter if system is under mep context
         lv_endpoint := F_ApplyMepCodeToProxyURL(lv_endpoint);
         IF gurocfg_c%NOTFOUND
         THEN
            raise_application_error(-20103, 'Could not build URL for proxy e-mail communication');
         END IF;
         CLOSE gurocfg_c;
      
      RETURN lv_proxy_url || lv_endpoint;
   END F_getProxyURL;
--
--  
   FUNCTION F_GenerateIDM RETURN NUMBER IS
   lv_dummy    VARCHAR2 (1);
   lv_sabnstu  VARCHAR2 (1);
   lv_proxyIDM gpbprxy.gpbprxy_proxy_idm%TYPE;

   -- Check idm candidate against spriden, sabnstu and gpbprxy
   CURSOR C_FindIDM
   IS
      SELECT 'X'
        FROM spriden
       WHERE spriden_pidm = lv_proxyIDM
      UNION
      SELECT 'X'
        FROM gpbprxy
       WHERE gpbprxy_proxy_idm = lv_proxyIDM
      UNION
      SELECT 'X'
        FROM DUAL
       WHERE 'X' = lv_sabnstu;
BEGIN
   -- Loop through using a sequence number to generate a new PIDM.
   LOOP
      SELECT PROXY_ACCESS_IDM_SEQUENCE.NEXTVAL INTO lv_proxyIDM FROM DUAL;

      -- Exit loop if IDM does not exist
      -- Call to sabnstu is dynamic in case student is not installed
      BEGIN
        EXECUTE IMMEDIATE
          'SELECT DISTINCT ''X''
             FROM sabnstu
            WHERE sabnstu_aidm = :1'
         INTO lv_sabnstu
        USING lv_proxyIDM;
      EXCEPTION
        WHEN OTHERS THEN
          lv_sabnstu := 'N';
      END;

      OPEN  C_FindIDM;
      FETCH C_FindIDM INTO lv_dummy;
      IF C_FindIDM%NOTFOUND
      THEN
         CLOSE C_FindIDM;
         EXIT;
      END IF;

      CLOSE C_FindIDM;
   END LOOP;

   RETURN lv_proxyIDM;
END F_GenerateIDM;
--
--   
 FUNCTION F_GetProxyIDM (p_email goremal.goremal_email_address%TYPE,
                        p_last  spriden.spriden_last_name%TYPE,
                        p_first spriden.spriden_first_name%TYPE,
                        p_pidm  spriden.spriden_pidm%TYPE DEFAULT NULL)
   RETURN NUMBER
IS
   lv_email       goremal.goremal_email_address%TYPE := TRIM (LOWER (p_email));
   lv_proxyIDM    gpbprxy.gpbprxy_proxy_idm%TYPE := 0;
   lv_pinhash     gpbprxy.gpbprxy_pin%TYPE;
   lv_salt        gpbprxy.gpbprxy_salt%TYPE;
   lv_hold_rowid  gb_common.internal_record_id_type;
   lv_GPBPRXY_rec gp_gpbprxy.gpbprxy_rec;
   lv_GPBPRXY_ref gp_gpbprxy.gpbprxy_ref;
   lv_proxy_url   VARCHAR2(300);
   BEGIN
   -- Get the proxy record
   -- If PIDM is provided then query by PIDM, if not found then query by email
   -- If PIDM is not provided then query by email
   IF p_pidm IS NOT NULL
   THEN
      lv_GPBPRXY_ref := gp_gpbprxy.F_Query_One_By_PIDM (p_pidm);
      FETCH lv_GPBPRXY_ref INTO lv_GPBPRXY_rec;
      IF lv_GPBPRXY_ref%NOTFOUND
      THEN
         CLOSE lv_GPBPRXY_ref;
         lv_GPBPRXY_ref := gp_gpbprxy.F_Query_One_By_Email (lv_email);
         FETCH lv_GPBPRXY_ref INTO lv_GPBPRXY_rec;
      END IF;
   ELSE
      lv_GPBPRXY_ref := gp_gpbprxy.F_Query_One_By_Email (lv_email);
      FETCH lv_GPBPRXY_ref INTO lv_GPBPRXY_rec;
   END IF;

   -- Return proxy IDM if record exists
   -- If you were passed a PIDM (usually via Secure Proxy Access) then update the proxy record
   IF lv_GPBPRXY_ref%FOUND
   THEN
      -- If you found the proxy record by e-mail address (PIDM lookup failed) then update the proxy PIDM and ID
      IF p_pidm IS NOT NULL AND
       ((lv_email <> lv_GPBPRXY_rec.R_email_address) OR (NVL(lv_GPBPRXY_rec.R_PROXY_PIDM, 0) <> p_pidm))
      THEN
        BEGIN
          gp_gpbprxy.P_Update (
             p_proxy_idm     => lv_GPBPRXY_rec.R_PROXY_IDM,
             p_proxy_pidm    => p_pidm,
             p_email_address => lv_email,
             p_entity_cde    => bwgkprxy.F_GetSpridenEntity (p_pidm),
             p_id            => bwgkprxy.F_GetSpridenID (p_pidm),
             p_user_id       => goksels.f_get_ssb_id_context
             );
        EXCEPTION
          WHEN OTHERS THEN      
            RETURN NULL;
         END;
         gb_common.P_Commit;
         bwgkprxy.P_MatchLoad (lv_GPBPRXY_rec.R_PROXY_IDM);
      END IF;
      CLOSE lv_GPBPRXY_ref;
      RETURN lv_GPBPRXY_rec.R_PROXY_IDM;
   END IF;
   CLOSE lv_GPBPRXY_ref;

   -- Otherwise insert new proxy record (storing PIDM if it was passed)
   -- Initialize salt to be used to encrypt PIN and to validate an action URL
   lv_proxyIDM := F_GenerateIDM;
   lv_salt := gspcrpt.f_get_salt(26);
   gspcrpt.p_saltedhash( lv_salt, lv_salt, lv_pinhash);

   gp_gpbprxy.P_Create (
      p_proxy_idm        => lv_proxyIDM,
      p_email_address    => lv_email,
      p_last_name        => p_last,
      p_first_name       => p_first,
      p_proxy_pidm       => p_pidm,
      p_pin              => lv_pinhash,
      p_pin_disabled_ind => 'C',
      p_salt             => lv_salt,
      p_entity_cde       => bwgkprxy.F_GetSpridenEntity (p_pidm),
      p_id               => bwgkprxy.F_GetSpridenID (p_pidm),
      p_email_ver_date   => NULL,
      p_pin_exp_date     => NULL,
      p_create_user      => goksels.f_get_ssb_id_context,
      p_create_date      => SYSDATE,
      p_user_id          => goksels.f_get_ssb_id_context,
      p_opt_out_adv_date => NULL,
      p_rowid_out        => lv_hold_rowid
      );

   gp_gpbeltr.P_Create (
      p_syst_code      => 'PROXY',
      p_ctyp_code      => 'NEW_PROXY',
      p_ctyp_url       => NULL,
      p_ctyp_exp_date  => SYSDATE + bwgkprxy.F_GetOption ('ACTION_VALID_DAYS'),
      p_ctyp_exe_date  => NULL,
      p_transmit_date  => NULL,
      p_proxy_idm      => lv_proxyIDM,
      p_proxy_old_data => NULL,
      p_proxy_new_data => NULL,
      p_person_pidm    => global_pidm,
      p_user_id        => goksels.f_get_ssb_id_context,
      p_create_date    => SYSDATE,
      p_create_user    => goksels.f_get_ssb_id_context,
      p_rowid_out      => lv_hold_rowid
      );


   gp_gpbeltr.P_Update (
      p_ctyp_code      => 'NEW_PROXY_NOA',
      p_ctyp_url => F_getProxyURL('NEW_PROXY') || twbkbssf.F_Encode (lv_hold_rowid),
      p_user_id  => goksels.f_get_ssb_id_context,
      p_rowid    => lv_hold_rowid
      );

      gb_common.P_Commit;
 
      bwgkprxy.P_SendEmail(lv_hold_rowid);

     gp_gpbeltr.P_Create (
      p_syst_code      => 'PROXY',
      p_ctyp_code      => 'NEW_PROXY_ACCESS_CODE',
      p_ctyp_url       => NULL,
      p_ctyp_exp_date  => SYSDATE + bwgkprxy.F_GetOption ('ACTION_VALID_DAYS'),
      p_ctyp_exe_date  => NULL,
      p_transmit_date  => NULL,
      p_proxy_idm      => lv_proxyIDM,
      p_proxy_old_data => NULL,
      p_proxy_new_data => NULL,
      p_person_pidm    => global_pidm,
      p_user_id        => goksels.f_get_ssb_id_context,
      p_create_date    => SYSDATE,
      p_create_user    => goksels.f_get_ssb_id_context,
      p_rowid_out      => lv_hold_rowid
      );


   gb_common.P_Commit;
   bwgkprxy.P_SendEmail(lv_hold_rowid);

   RETURN lv_proxyIDM;
END F_GetProxyIDM;
--
--------------------------------MAIN CODE----------------------------
--   
begin
--
 global_pidm := ?;
 p_email := ?;
 p_email_verify := ?;
 p_last := ?;
 p_first := ?;
--
   -- Insure that the e-mail and verify e-mail address are the same
   IF NVL(p_email,chr(01)) <> NVL(p_email_verify,chr(02)) THEN
      lv_info := 'NOEMAILMATCH';
      error_status := 'Y';
   END IF;
   
     -- Check for Bad e-mail address
   lv_double  := nvl(instr(p_email,'@@'),0) + nvl(instr(p_email,'..'),0);
   IF substr(p_email,1,1) IN ('@','.') OR substr(p_email,-1,1) IN ('@','.') THEN
      lv_double := lv_double + 1;
   END IF;
   lv_at_loc  := nvl(instr(p_email,'@'),0);
   lv_dot_loc := nvl(instr(substr(p_email,lv_at_loc + 1), '.'),0);
   IF (lv_at_loc < 2) OR (lv_dot_loc < 2) OR (lv_double > 0) THEN
      lv_info := 'BADEMAIL';
      error_status := 'Y';     
   END IF;
   
     IF (goksels.f_clean_text(p_email) IS NULL) 
       OR (goksels.f_clean_text(p_last) IS NULL) 
       OR (goksels.f_clean_text(p_first) IS NULL) THEN
      lv_info := 'REQUIRED';
      error_status := 'Y';  
     END IF;
--
IF error_status != 'Y' THEN
 lv_proxyIDM := F_GetProxyIDM (goksels.f_clean_text(p_email), /*p_email*/
                              goksels.f_clean_text(p_last), /*p_last*/
                              goksels.f_clean_text(p_first) /*p_first*/
                              );
--
   lv_GPRXREF_ref := gp_gprxref.F_Query_One (lv_proxyIDM, global_pidm);
   FETCH lv_GPRXREF_ref INTO lv_GPRXREF_rec;

   -- Insert new XREF record if no relationship record found
   -- Use yesterday as stop date so that new proxies are locked until relationship is defined
   IF lv_GPRXREF_ref%NOTFOUND
   THEN
      lv_info := 'PROXYADDED';
      error_status := 'N';
      gp_gprxref.P_Create (
         p_proxy_idm   => lv_proxyIDM,
         p_person_pidm => global_pidm,
         p_retp_code   => 'AAA',
         p_proxy_desc  => NULL,
         p_start_date  => TRUNC(SYSDATE),
         p_stop_date   => TRUNC(SYSDATE - 1),
         p_create_user => goksels.f_get_ssb_id_context,
         p_create_date => SYSDATE,
         p_user_id     => goksels.f_get_ssb_id_context,
         p_passphrase  => NULL,
         p_rowid_out   => lv_hold_rowid
         );

      -- Send generic e-mail message since no action is required
      -- No need to send NEW_RELATIONSHIP here since the UPDATE_REALTIONSHIP
      --   will now send on intial set-up as well as changes in relationships
      --   especially since this was never related to a specific relationship
      --   Users should uncheck the NEW_RELATIONSHIP to prevent a generic initial

      gp_gpbeltr.P_Create (
         p_syst_code      => 'PROXY',
         p_ctyp_code      => 'NEW_RELATIONSHIP',
         p_ctyp_url       => F_getProxyURL('NEW_RELATIONSHIP'),
         p_ctyp_exp_date  => NULL,
         p_ctyp_exe_date  => NULL,
         p_transmit_date  => NULL,
         p_proxy_idm      => lv_proxyIDM,
         p_proxy_old_data => NULL,
         p_proxy_new_data => NULL,
         p_person_pidm    => global_pidm,
         p_user_id        => goksels.f_get_ssb_id_context,
         p_create_date    => SYSDATE,
         p_create_user    => goksels.f_get_ssb_id_context,
         p_rowid_out      => lv_hold_rowid
         );

      gb_common.P_Commit;
      bwgkprxy.P_SendEmail(lv_hold_rowid);
   ELSE
      lv_info := 'EMAILINUSE';
      error_status := 'Y';
   END IF;
   CLOSE lv_GPRXREF_ref;
END IF;
   
   ? := lv_info;
   ? := error_status;
   ? := lv_proxyIDM;
   END;
"""

    public final static String PROXY_PROFILE  = """
       DECLARE
         lv_GPRXREF_rec gp_gprxref.gprxref_rec;
         lv_GPRXREF_ref gp_gprxref.gprxref_ref;
--
       BEGIN
--        
        dbms_session.set_nls('NLS_DATE_FORMAT',''''||'DD-MON-RRRR'||'''');
        dbms_session.set_nls('NLS_CALENDAR',''''||'GREGORIAN'||'''');
        ? := gp_gprxref.F_Query_One (?,?);
--
        END;
    """

    public final static String PROXY_PROFILE_UI_RULES = """
    DECLARE
    show_p_passphrase       VARCHAR2(1);
    show_p_reset_pin        VARCHAR2(1);
    r_retp_code             GTVRETP.GTVRETP_CODE%TYPE;

    BEGIN
--    
    r_retp_code := ?;
--
    IF bwgkprxy.F_GetOption ('ENABLE_PASSPHRASE', r_retp_code) = 'Y' THEN
      show_p_passphrase := 'Y';
    END IF;
    
    IF bwgkprxy.F_GetOption ('ENABLE_RESET_PIN', r_retp_code) = 'Y' THEN
      show_p_reset_pin := 'Y';
    END IF;
 
    ? := NVL(show_p_passphrase,'N');
    ? := NVL(show_p_reset_pin,'N');
    --
    END;

    """

    public final static String DELETE_PROXY = """
   DECLARE
     p_proxyIDM  gpbprxy.gpbprxy_proxy_idm%TYPE;
     p_personPIDM spriden.spriden_pidm%TYPE;
     lv_hold_rowid  gb_common.internal_record_id_type;
     error_status   VARCHAR2(1) := 'N';
     lv_info        twgrinfo.twgrinfo_label%TYPE;
     lv_count         NUMBER;
     
 -- to apply mep code to Banner9 url for proxy e-mail communication

   FUNCTION F_ApplyMepCodeToProxyURL (endpoint IN VARCHAR2) RETURN VARCHAR2 IS
   BEGIN
    IF g\$_vpdi_security.G\$_IS_MIF_ENABLED THEN
           IF INSTR(endpoint,'?') != 0 THEN
         RETURN REPLACE(endpoint,'?','?mepCode='|| g\$_vpdi_security.G\$_VPDI_GET_INST_CODE_FNC || '&');
    ELSE
         RETURN (endpoint || '?mepCode=' || g\$_vpdi_security.G\$_VPDI_GET_INST_CODE_FNC);
    END IF;
    ELSE
        RETURN endpoint;
    END IF;
   END F_ApplyMepCodeToProxyURL;
   
     
  -- Get the URL for accessing Banner 9 Proxy
   FUNCTION F_getProxyURL(
      p_action          VARCHAR2 DEFAULT NULL)
      RETURN VARCHAR2
   IS
      CURSOR gurocfg_c (
         p_appid_in gurocfg.gurocfg_gubappl_app_id%TYPE,
         p_config_in gurocfg.gurocfg_name%TYPE)
      IS
       SELECT gurocfg_value
         FROM gurocfg
        WHERE gurocfg_gubappl_app_id = p_appid_in
          AND gurocfg_name      =  p_config_in
          AND gurocfg_type      =  'string';

      lv_use_ban9 VARCHAR2(1);
      lv_key VARCHAR2(1000);
      lv_endpoint VARCHAR2(60);
      lv_proxy_url VARCHAR2(1000);
   BEGIN
--
         OPEN gurocfg_c('GENERAL_SS', 'GENERALLOCATION');
         FETCH gurocfg_c INTO lv_proxy_url;
         
         IF gurocfg_c%NOTFOUND
         THEN
            raise_application_error(-20103, 'Could not build URL for proxy e-mail communication');
         END IF;
         CLOSE gurocfg_c;

         lv_key := 'proxyAccessURL.' || p_action;
         OPEN gurocfg_c('BAN9_PROXY', lv_key);
         FETCH gurocfg_c INTO lv_endpoint;

         -- process mep context
         -- it adds the mepCode parameter if system is under mep context
         lv_endpoint := F_ApplyMepCodeToProxyURL(lv_endpoint);
         IF gurocfg_c%NOTFOUND
         THEN
            raise_application_error(-20103, 'Could not build URL for proxy e-mail communication');
         END IF;
         CLOSE gurocfg_c;
      
      RETURN lv_proxy_url || lv_endpoint;
   END F_getProxyURL;

     
     FUNCTION F_Delete_Relationship (p_proxyIDM gpbprxy.gpbprxy_proxy_idm%TYPE, p_personPIDM spriden.spriden_pidm%TYPE)
  RETURN VARCHAR2
IS
   lv_delete      VARCHAR2(01);
   lv_days        NUMBER;
   CURSOR current_views_c
   IS
     SELECT 'Y'
       FROM gprhist
      WHERE gprhist_new_auth_ind = 'V'
        AND gprhist_proxy_idm    = p_proxyIDM
        AND gprhist_person_pidm  = p_personPIDM
        AND TRUNC(gprhist_activity_date) > trunc(SYSDATE) - lv_days;
BEGIN
-- if deletes are not allowed then return N
   IF NVL(gorsrin.f_getoption('ENABLE_DELETE_RELATIONSHIP', NULL, 'PROXY'),'N') <> 'Y' THEN
     RETURN 'N';
   END IF;
-- get the number of days that must be past since the last view
   lv_days := NVL(gorsrin.f_getoption('ENABLE_DELETE_AFTER_DAYS', NULL, 'PROXY'),999999);
-- now check to see if there have been any recent views
   lv_delete := 'N';
   OPEN current_views_c;
   FETCH current_views_c INTO lv_delete;
   CLOSE current_views_c;
   IF NVL(lv_delete,'N') = 'Y' THEN
     RETURN 'N';
   ELSE
     RETURN 'Y';
   END IF;

END F_Delete_Relationship;

    BEGIN
--
    p_personPIDM := ?;
    p_proxyIDM := ?;
    
-- See if we can delete a proxy relationship
      IF F_Delete_Relationship(p_proxyIDM, p_personPIDM) = 'N' THEN
        lv_info := 'PROXYNOTDELETED';
        error_status := 'Y';
        goto final_step;
       END IF;

   -- Need to send letter before we actually delete

      -- Send message with generic login URL since no action is required
      gp_gpbeltr.P_Create (
         p_syst_code      => 'PROXY',
         p_ctyp_code      => 'DELETE_RELATIONSHIP',
         p_ctyp_url       => F_getProxyURL('DELETE_RELATIONSHIP'),  -- NULL,
         p_ctyp_exp_date  => NULL,
         p_ctyp_exe_date  => NULL,
         p_transmit_date  => NULL,
         p_proxy_idm      => p_proxyIDM,
         p_proxy_old_data => NULL,
         p_proxy_new_data => NULL,
         p_person_pidm    => p_personPIDM,
         p_user_id        => goksels.f_get_ssb_id_context,
         p_create_date    => SYSDATE,
         p_create_user    => goksels.f_get_ssb_id_context,
         p_rowid_out      => lv_hold_rowid
         );
      gb_common.P_Commit;
      bwgkprxy.P_SendEmail(lv_hold_rowid);

      DELETE
        FROM gprhist
       WHERE gprhist_proxy_idm   = p_proxyIDM
         AND gprhist_person_pidm = p_personPIDM;
      DELETE
        FROM gprauth
       WHERE gprauth_proxy_idm   = p_proxyIDM
         AND gprauth_person_pidm = p_personPIDM;
      DELETE
        FROM gpbeltr
       WHERE gpbeltr_proxy_idm   = p_proxyIDM
         AND gpbeltr_person_pidm = p_personPIDM;
      DELETE
        FROM genbpsh
       WHERE genbpsh_gidm        = p_proxyIDM
         AND genbpsh_person_pidm = p_personPIDM;
      DELETE
        FROM gprxref
       WHERE gprxref_proxy_idm   = p_proxyIDM
         AND gprxref_person_pidm = p_personPIDM;
   -- if there are no relationships left, then delete gpbprxy and geniden rows as well
      SELECT COUNT(*)
        INTO lv_COUNT
        FROM gprxref
       WHERE gprxref_proxy_idm   = p_proxyIDM;
      IF lv_count = 0 THEN
        DELETE
          FROM gpbprxy
         WHERE gpbprxy_proxy_idm = p_proxyIDM;
        DELETE
          FROM geniden
         WHERE geniden_gidm = p_proxyIDM;
      END IF;
      COMMIT;
      
      <<final_step>>
        lv_info := 'DELETED';
        error_status := 'N';
        ? := lv_info;
        ? := error_status;
   END;
"""

    public final static String PROXY_START_STOP_DATES = """
   DECLARE
     start_date gprxref.gprxref_start_date%TYPE := TRUNC(SYSDATE);
     stop_date  gprxref.gprxref_stop_date%TYPE := TRUNC(SYSDATE + bwgkprxy.F_GetOption ('ACCESS_WINDOW_DAYS', ?));
     
    BEGIN
        ? := start_date;
        ? := stop_date;
   END;
"""

    public final static String UPDATE_PROXY = """
DECLARE
   lv_hold_rowid  gb_common.internal_record_id_type;
   lv_GPRXREF_rec gp_gprxref.gprxref_rec;
   lv_GPRXREF_ref gp_gprxref.gprxref_ref;
   lv_RETP        gtvretp.gtvretp_code%TYPE;
   p_RETP         gtvretp.gtvretp_code%TYPE;
   p_desc         GPRXREF.GPRXREF_PROXY_DESC%TYPE;
   p_proxyIDM     gpbprxy.gpbprxy_proxy_idm%TYPE;
   global_pidm    spriden.spriden_pidm%TYPE;
   lv_start_date  gprxref.gprxref_start_date%TYPE ;
   lv_stop_date   gprxref.gprxref_stop_date%TYPE ;
   error_status   VARCHAR2(1) := 'N';
   lv_info        twgrinfo.twgrinfo_label%TYPE;
   p_start_date   gprxref.gprxref_start_date%TYPE;
   p_stop_date    gprxref.gprxref_stop_date%TYPE;
   p_phrase       GPRXREF.GPRXREF_PASSPHRASE%TYPE; 
   
   lv_temp_fmt              VARCHAR2 (30);
   
BEGIN
        lv_info := 'UPDATED';
        dbms_session.set_nls('NLS_CALENDAR',''''||'GREGORIAN'||'''');
        lv_temp_fmt := twbklibs.date_input_fmt;
        twbklibs.date_input_fmt := 'MM/DD/YYYY';
        
  global_pidm := ?;
  p_proxyIDM := ?;
  p_RETP := ?;
  p_desc := ?;
  p_start_date := TO_DATE (?, twbklibs.date_input_fmt);
  p_stop_date := TO_DATE (?, twbklibs.date_input_fmt);
  p_phrase := ?;
--
 lv_GPRXREF_ref := gp_gprxref.F_Query_One (p_proxyIDM, global_pidm);
--
   FETCH lv_GPRXREF_ref INTO lv_GPRXREF_rec;
--   
   IF lv_GPRXREF_ref%FOUND THEN
--   
-- RETP_CODE UPDATE
   IF lv_GPRXREF_rec.R_RETP_CODE != p_RETP then    
-- Get previous relationship type
      lv_RETP := gp_gprxref.F_GetXREF_RETP(p_proxyIDM, global_pidm);
      
      lv_start_date  := NVL(lv_start_date,TRUNC(SYSDATE));
      lv_stop_date := NVL(lv_stop_date,TRUNC(SYSDATE + bwgkprxy.F_GetOption ('ACCESS_WINDOW_DAYS', p_RETP)));
--
   gp_gprxref.P_Update (
      p_proxy_idm   => p_proxyIDM,
      p_person_pidm => global_pidm,
      p_retp_code   => p_RETP,
      --p_start_date  => p_start_date,
      --p_stop_date   => p_stop_date,
      p_user_id     => goksels.f_get_ssb_id_context
      );
--      
   gb_common.P_Commit;
   bwgkprxy.P_MatchLoad (p_proxyIDM);

-- Send message with generic login URL since no action is required
      gp_gpbeltr.P_Create (
         p_syst_code      => 'PROXY',
         p_ctyp_code      => 'UPDATE_RELATIONSHIP',
         p_ctyp_url       => bwgkprxy.F_getProxyURL('UPDATE_RELATIONSHIP'),
         p_ctyp_exp_date  => NULL,
         p_ctyp_exe_date  => NULL,
         p_transmit_date  => NULL,
         p_proxy_idm      => p_proxyIDM,
         p_proxy_old_data => lv_RETP,
         p_proxy_new_data => p_RETP,
         p_person_pidm    => global_pidm,
         p_user_id        => goksels.f_get_ssb_id_context,
         p_create_date    => SYSDATE,
         p_create_user    => goksels.f_get_ssb_id_context,
         p_rowid_out      => lv_hold_rowid
         );

      gb_common.P_Commit;
      bwgkprxy.P_SendEmail(lv_hold_rowid);
      error_status := 'N';
--      
   END IF;
--
--    UPDATE PASSPHRASE 
--
     IF NVL(lv_GPRXREF_rec.R_PASSPHRASE,'X') != p_phrase THEN
      gp_gprxref.P_Update (
      p_proxy_idm   => p_proxyIDM,
      p_person_pidm => global_pidm,
      p_passphrase  => p_phrase,
      p_user_id     => goksels.f_get_ssb_id_context
      );
       gb_common.P_Commit;
       error_status := 'N';
     END IF;
-- 
--    UPDATE DESCRIPTION    
    IF NVL(lv_GPRXREF_rec.R_PROXY_DESC,'X')  != p_desc THEN
     
      gp_gprxref.P_Update (
      p_proxy_idm   => p_proxyIDM,
      p_person_pidm => global_pidm,
      p_proxy_desc  => p_desc,
      p_user_id     => goksels.f_get_ssb_id_context
      );
      
      gb_common.P_Commit;
      
      error_status := 'N';
      
     END IF;
     
     -- UPDATE DATES
     
     IF (p_start_date > p_stop_date) THEN
      lv_info := 'DATESCOMPAREERROR';
      error_status := 'Y';
     END IF;
     
     IF lv_GPRXREF_rec.R_START_DATE  != p_start_date THEN
       gp_gprxref.P_Update (
         p_proxy_idm   => p_proxyIDM,
         p_person_pidm => global_pidm,
         p_start_date  => p_start_date,
         p_user_id     => goksels.f_get_ssb_id_context
         );
          gb_common.P_Commit;
          bwgkprxy.P_MatchLoad (p_proxyIDM);
      END IF;
      
      IF lv_GPRXREF_rec.R_STOP_DATE  != p_stop_date THEN
       gp_gprxref.P_Update (
         p_proxy_idm   => p_proxyIDM,
         p_person_pidm => global_pidm,
         p_stop_date  => p_stop_date,
         p_user_id     => goksels.f_get_ssb_id_context
         );
          gb_common.P_Commit;
          bwgkprxy.P_MatchLoad (p_proxyIDM);
      END IF;
     -- END UPDATE DATES
   END IF;
       
CLOSE lv_GPRXREF_ref;

        --lv_info := 'UPDATED';
        --error_status := 'N';
        ? := lv_info;
        ? := error_status;  
END;
"""


    public final static String PROXY_PAGES = """
DECLARE
  CURSOR C_AuthorizationList (
      p_RETP gprxref.gprxref_retp_code%TYPE)
   IS
        SELECT m.twgbwmnu_name     AS menu_name,
               m.twgbwmnu_desc     AS menu_desc,
               o.twgrmenu_url_text AS menu_text,
               o.twgrmenu_url      AS menu_url,
               o.twgrmenu_sequence AS menu_seq
          FROM TWGRMENU o, TWGBWMNU m
         WHERE o.twgrmenu_name = m.twgbwmnu_name
         AND  o.twgrmenu_url like '%/proxy/%'
           AND m.twgbwmnu_source_ind =
              (SELECT NVL (MAX (n.twgbwmnu_source_ind), 'B')
                 FROM twgbwmnu n
                WHERE n.twgbwmnu_name = m.twgbwmnu_name
                  AND n.twgbwmnu_source_ind = 'L')
           AND o.twgrmenu_source_ind =
              (SELECT NVL (MAX (p.twgrmenu_source_ind), 'B')
                 FROM twgrmenu p
                WHERE p.twgrmenu_name = o.twgrmenu_name
                  AND p.twgrmenu_source_ind = 'L')
           AND o.twgrmenu_name LIKE 'PROXY_ACCESS_' || p_RETP || '%'
           AND NVL(o.twgrmenu_enabled, 'N')       = 'Y'
           AND NVL(m.twgbwmnu_enabled_ind,'N')    = 'Y'
           AND NVL(m.twgbwmnu_adm_access_ind,'N') = 'N'
           AND NVL(o.twgrmenu_enabled,'N')        = 'Y'
      ORDER BY menu_desc, menu_name, menu_seq;

lv_RETP        gtvretp.gtvretp_code%TYPE;
pages          VARCHAR2(32000);
auth           VARCHAR2(1);

BEGIN
lv_RETP := ?;
--
pages:= '{ "pages":[';
--
FOR auth_rec IN C_AuthorizationList (lv_RETP)
--
      LOOP
      IF bwgkprxy.F_GetAuthInd (?,?, auth_rec.menu_url) = 'Y' THEN
         auth := 'Y';
      ELSE
         auth := 'N';
      END IF;
                                                                      
            pages := pages || '{' ||
                      '"url" ' || ':' || '"' || auth_rec.menu_url || '"' ||
                      ',"desc" ' || ':'  || '"' || auth_rec.menu_text || '"' || 
                      ',"auth" ' || ':'  || '"' || auth || '"' ||
                      '},';
      END LOOP;
--
     pages := TRIM(TRAILING ',' FROM pages );
--
     pages := pages || ']}';

     ? := pages;
--
END;
    """

    public final static String MANAGE_AUTHORIZATION = """
  DECLARE
  --
  p_proxyIDM gpbprxy.gpbprxy_proxy_idm%TYPE;
  p_page     gprauth.gprauth_page_name%TYPE;
  p_checked  VARCHAR2(10);
  
   lv_auth_ind   gprauth.gprauth_auth_ind%TYPE;
   lv_checked    VARCHAR2(32) ;
   lv_hold_rowid gb_common.internal_record_id_type;
   global_pidm spriden.spriden_pidm%TYPE;
  --
  BEGIN
  p_proxyIDM := ?;
  global_pidm := ?;
  p_page := ?;
  p_checked := ?;
  
  lv_checked := UPPER(p_checked);
  
   -- Get current value of authorization indicator (Y or N)
   -- If no existing auth record then N will be returned
   -- A page that has been disabled will also return N
   lv_auth_ind := bwgkprxy.F_GetAuthInd (p_proxyIDM, global_pidm, p_page);

   -- Double check that the page is valid for this person before enabling it for proxy access
   IF lv_checked = 'TRUE' AND lv_auth_ind = 'N'
   THEN
      IF gp_gprauth.F_Exists (
            p_proxy_idm   => p_proxyIDM,
            p_person_pidm => global_pidm,
            p_page_name   => p_page
            ) = 'Y'
      THEN
         gp_gprauth.P_Update (
            p_proxy_idm   => p_proxyIDM,
            p_person_pidm => global_pidm,
            p_page_name   => p_page,
            p_auth_ind    => 'Y',
            p_user_id     => goksels.f_get_ssb_id_context
            );
      ELSE
         gp_gprauth.P_Create (
            p_proxy_idm   => p_proxyIDM,
            p_person_pidm => global_pidm,
            p_page_name   => p_page,
            p_auth_ind    => 'Y',
            p_create_user => goksels.f_get_ssb_id_context,
            p_create_date => SYSDATE,
            p_user_id     => goksels.f_get_ssb_id_context,
            p_rowid_out   => lv_hold_rowid
            );
      END IF;
      gb_common.P_Commit;
   END IF;

   IF lv_checked = 'FALSE' AND lv_auth_ind = 'Y'
   THEN
      IF gp_gprauth.F_Exists (
            p_proxy_idm   => p_proxyIDM,
            p_person_pidm => global_pidm,
            p_page_name   => p_page
            ) = 'Y'
      THEN
         gp_gprauth.P_Update (
            p_proxy_idm   => p_proxyIDM,
            p_person_pidm => global_pidm,
            p_page_name   => p_page,
            p_auth_ind    => 'N',
            p_user_id     => goksels.f_get_ssb_id_context
            );
      ELSE
         gp_gprauth.P_Create (
            p_proxy_idm   => p_proxyIDM,
            p_person_pidm => global_pidm,
            p_page_name   => p_page,
            p_auth_ind    => 'N',
            p_create_user => goksels.f_get_ssb_id_context,
            p_create_date => SYSDATE,
            p_user_id     => goksels.f_get_ssb_id_context,
            p_rowid_out   => lv_hold_rowid
            );
      END IF;
      gb_common.P_Commit;
   END IF;
  
  END;
"""

    public final static String RELATIONSHIP_OPTION_LIST = """
  DECLARE
     global_syst CONSTANT gtvsyst.gtvsyst_code%TYPE := 'PROXY';
     lv_access   VARCHAR2(01);
     global_pidm spriden.spriden_pidm%TYPE;
     rel_list    VARCHAR2(32000);
     
     CURSOR C_RETPlist
         RETURN GTVRETP%ROWTYPE
     IS
         SELECT *
            FROM GTVRETP
         WHERE EXISTS (SELECT 1 FROM GEBSRTP WHERE GEBSRTP_SYST_CODE = global_syst AND GEBSRTP_RETP_CODE = GTVRETP_CODE)
         ORDER BY GTVRETP_DESC;

  BEGIN
     global_pidm := ?;
     rel_list:= '{ "relationships":[';
      
     FOR lv_GTVRETP_rec IN C_RETPlist
     LOOP
        -- Check to see if the user actually can access this relationship
            lv_access := bwgkpxym.f_check_proxy_relationship_sql(global_syst
                                                             ,lv_GTVRETP_rec.GTVRETP_CODE
                                                             ,global_pidm
                                                             );
        IF lv_access = 'Y' THEN
           rel_list := rel_list || '{"code": "' || lv_GTVRETP_rec.GTVRETP_CODE || '", ' ||
                                    '"description": "' || lv_GTVRETP_rec.GTVRETP_DESC ||
                                    '"},';
        END IF;
     END LOOP;

     rel_list := TRIM(TRAILING ',' FROM rel_list);
     rel_list := rel_list || ']}';
     
     ? := rel_list;
  END;
"""

}
