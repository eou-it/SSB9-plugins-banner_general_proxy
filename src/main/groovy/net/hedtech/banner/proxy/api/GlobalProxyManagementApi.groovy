/********************************************************************************
  Copyright 2020-2021 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.proxy.api

class GlobalProxyManagementApi {
    /*
    Input: Global Pidm
    Output: Relationship Options
    */
    public final static String RELATIONSHIP_OPTION_LIST = """
DECLARE
  global_syst CONSTANT gtvsyst.gtvsyst_code%TYPE := 'PROXY_GLOBAL_ACCESS';
  lv_access   VARCHAR2(01);
  global_pidm spriden.spriden_pidm%TYPE;
  rel_list VARCHAR2(32000);
  rel_enabled TWGBWMNU.TWGBWMNU_ENABLED_IND%TYPE;
  
  CURSOR C_RETPlist
    RETURN GTVRETP%ROWTYPE
  IS
    SELECT *
    FROM GTVRETP
    WHERE EXISTS
      (SELECT 1
      FROM GEBSRTP
      WHERE GEBSRTP_SYST_CODE = global_syst
      AND GEBSRTP_RETP_CODE   = GTVRETP_CODE
      )
  ORDER BY GTVRETP_DESC;
  
  CURSOR C_MenuEnabledInd (reltype_in IN GTVRETP.GTVRETP_CODE%TYPE)
  IS
    SELECT TWGBWMNU_ENABLED_IND
    FROM TWGBWMNU
    WHERE TWGBWMNU_NAME = ('PROXY_ACCESS_'
      || reltype_in);
      
BEGIN
  global_pidm := ?;
  rel_list    := '{ "relationships":[';
  FOR lv_GTVRETP_rec IN C_RETPlist
  LOOP
    -- Check to see if the user actually can access this relationship
    lv_access := bwgkpxym.f_check_proxy_relationship_sql(global_syst ,lv_GTVRETP_rec.GTVRETP_CODE ,global_pidm );
    
    -- Check to see if the relationship menu is enabled
    OPEN C_MenuEnabledInd(lv_GTVRETP_rec.GTVRETP_CODE);
    FETCH C_MenuEnabledInd INTO rel_enabled;
    IF C_MenuEnabledInd%ROWCOUNT = 1 THEN
      IF rel_enabled = 'N' THEN
        lv_access   := 'N';
      END IF;
    END IF;
    CLOSE C_MenuEnabledInd;
     
    IF lv_access = 'Y' THEN
      rel_list  := rel_list || '{"code": "' || lv_GTVRETP_rec.GTVRETP_CODE || '", ' || '"description": "' || lv_GTVRETP_rec.GTVRETP_DESC || '"},';
    END IF;
  END LOOP;
  rel_list := TRIM(TRAILING ',' FROM rel_list);
  rel_list := rel_list || ']}';
  
  ?        := rel_list;
END;
""";

    /*
    Input: Relationship Type (UNIV_FINAID, UNIV_ADMINISTRATION)
    Output: Authorized Pages
    */
    public final static String GLOBAL_PROXY_PAGES = """
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
         AND  (o.twgrmenu_url like '%/proxy/%'
               OR o.twgrmenu_url like '%/ssb/%')
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
         auth := 'Y';                                           
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

    /*
    Input: Target Pidm (Case Insensitive)
    Output: Return JSON in format: {"isValidToBeProxied": Y/N, "isValidBannerId": Y/N}
    */
    public final static String CHECK_IF_GLOBAL_PROXY_ACCESS_TARGET_IS_VALID = """
DECLARE
  p_banner_id    spriden.spriden_id%TYPE := ?;
  is_valid_to_be_proxied  VARCHAR2 (5);
  valid_banner_id_entered VARCHAR2 (5);
  return_json             VARCHAR(4000);
  lv_person_pidm spriden.spriden_pidm%TYPE;
  lv_attr_nt G_ATTRIBUTE_NT;
  lv_id  VARCHAR2(09);
  id_cnt INTEGER;
  CURSOR id_c
  IS
    SELECT spriden_id
    FROM spriden
    WHERE UPPER(spriden_id) = UPPER(p_banner_id)
    AND spriden_change_ind IS NULL;
BEGIN
  is_valid_to_be_proxied  := 'false';
  valid_banner_id_entered := 'false';
  
  -- Enable case insensitive IDs to be entered, but only if there is a single match
  id_cnt := 0;
  OPEN id_c;
  LOOP
    FETCH id_c INTO lv_id;
    EXIT
  WHEN id_c%NOTFOUND;
    id_cnt := id_cnt + 1;
  END LOOP;
  CLOSE id_c;
  IF id_cnt <> 1 THEN
    lv_id   := p_banner_id;
  END IF;
  
  --Check if ID gets valid PIDM
  lv_person_pidm            := bwgkprxy.F_GetSpridenPIDM (lv_id);
  IF (lv_person_pidm)       IS NOT NULL THEN
    valid_banner_id_entered := 'true';
    
    -- Ensure that the person is eligible to be targeted by a Global Access User
    lv_attr_nt                                := gp_gorrsql.F_Execute_Rule ('SSB_ROLES', 'SSB_ROLE_PROXYTARGET', lv_person_pidm);
    IF lv_attr_nt                             IS NOT NULL AND lv_attr_nt.COUNT > 0 THEN
      IF lv_attr_nt (lv_attr_nt.FIRST)."value" = 'TRUE' THEN
        is_valid_to_be_proxied                := 'true'; --Pidm is valid to be targeted
      END IF;
    END IF;
  END IF;
  return_json := ('{"isValidToBeProxied": "' || is_valid_to_be_proxied || '", "isValidBannerId": "' || valid_banner_id_entered || '"}');
  ? := return_json;
END;
"""

    /*
    Input: Proxy Gidm, Target Pidm, Proxy Pidm
    Output: Error Status (Y/N)
    */
    public final static String DELETE_GLOBAL_PROXY_RELATIONSHIP = """
DECLARE
  p_proxyIDM gpbprxy.gpbprxy_proxy_idm%TYPE := ?;
  p_personPIDM spriden.spriden_pidm%TYPE    := ?;
  global_pidm spriden.spriden_pidm%TYPE     := ?;
  error_status VARCHAR2(1)                  := 'N';
  CURSOR C_PageList
    RETURN GPRAUTH%ROWTYPE
  IS
    SELECT GPRAUTH.*
    FROM GPRAUTH
    WHERE GPRAUTH_PROXY_IDM = p_proxyIDM
    AND GPRAUTH_PERSON_PIDM = p_personPIDM;
  lv_GPBPRXY_rec gp_gpbprxy.gpbprxy_rec;
  lv_GPBPRXY_ref gp_gpbprxy.gpbprxy_ref;
  lv_GPRXREF_rec gp_gprxref.gprxref_rec;
  lv_GPRXREF_ref gp_gprxref.gprxref_ref;
  proxy_access_cookie OWA_COOKIE.cookie;
  proxy_access_value VARCHAR2 (255);
  lv_hold_rowid gb_common.internal_record_id_type;
BEGIN

  -- Get the proxy record and verify that it is compatible with the current user
  lv_GPBPRXY_ref := gp_gpbprxy.F_Query_One (p_proxyIDM);
  FETCH lv_GPBPRXY_ref INTO lv_GPBPRXY_rec;
  CLOSE lv_GPBPRXY_ref;
  IF lv_GPBPRXY_rec.R_PROXY_PIDM <> global_pidm THEN
    error_status                 := 'Y';
  END IF;
  
  -- GLOBAL DELETE_RELATIONSHIP e-mail
  -- Need to send letter before we actually delete
  IF error_status = 'N' THEN
    SELECT ROWID
    INTO lv_hold_rowid
    FROM gprxref
    WHERE gprxref_proxy_idm = p_proxyIDM
    AND gprxref_person_pidm = p_personPIDM;
    -- Send message with generic login URL since no action is required
    gp_gpbeltr.P_Create ( p_syst_code => 'PROXY_GLOBAL_ACCESS', p_ctyp_code => 'DELETE_RELATIONSHIP', p_ctyp_url => NULL, p_ctyp_exp_date => NULL, p_ctyp_exe_date => NULL, p_transmit_date => NULL, p_proxy_idm => p_proxyIDM, p_proxy_old_data => bwgkprxy.F_GetSpridenID(global_pidm), p_proxy_new_data => bwgkprxy.F_GetSpridenID(p_personPIDM), p_person_pidm => p_personPIDM, p_user_id => goksels.f_get_ssb_id_context, p_create_date => SYSDATE, p_create_user => goksels.f_get_ssb_id_context, p_rowid_out => lv_hold_rowid );
    gb_common.P_Commit;
    bwgkprxy.P_SendEmail(lv_hold_rowid);
    IF NVL(bwgkprxy.F_GetOption ('DELETE_HISTORY',NULL,'PROXY_GLOBAL_ACCESS'),'N') <> 'Y' THEN
      -- update to prior date instead of deleting all records
      gp_gprxref.p_update( p_proxy_idm => p_proxyIDM, p_person_pidm => p_personPIDM, p_start_date => TRUNC(SYSDATE - 1), p_stop_date => TRUNC(SYSDATE - 1), p_user_id => goksels.f_get_ssb_id_context);
      gb_common.P_Commit;
    ELSE
      -- Delete history records
      DELETE
      FROM gprhist
      WHERE gprhist_proxy_idm = p_proxyIDM
      AND gprhist_person_pidm = p_personPIDM;
      COMMIT;
      
      -- Delete authorization records
      FOR page IN C_PageList
      LOOP
        gp_gprauth.P_Delete (p_proxy_idm => p_proxyIDM, p_person_pidm => p_personPIDM, p_page_name => page.GPRAUTH_PAGE_NAME);
      END LOOP;
      gb_common.P_Commit;
      
      -- Delete relationship record
      lv_GPRXREF_ref := gp_gprxref.F_Query_One (p_proxyIDM, p_personPIDM);
      FETCH lv_GPRXREF_ref INTO lv_GPRXREF_rec;
      IF lv_GPRXREF_ref%FOUND THEN
        gp_gprxref.P_Delete (p_proxy_idm => p_proxyIDM, p_person_pidm => p_personPIDM);
        gb_common.P_Commit;
      ELSE
        error_status := 'Y';
      END IF;
      CLOSE lv_GPRXREF_ref;
    END IF;
  END IF;
  ? := error_status;
END;
"""

    /*
    Input: Global Pidm, Active Preferred Email, Last Name, First Name
    Output: Proxy Gidm
    */

    public final static String GET_GLOBAL_PROXY_GIDM() {
        """
    DECLARE
      global_syst CONSTANT gtvsyst.gtvsyst_code%TYPE := 'PROXY_GLOBAL_ACCESS';
      global_pidm spriden.spriden_pidm%TYPE := ?;
      lv_email goremal.goremal_email_address%TYPE := ?;
      lv_last spriden.spriden_last_name%TYPE := ?;
      lv_first spriden.spriden_first_name %TYPE := ?;
      p_proxyIDM gpbprxy.gpbprxy_proxy_idm%TYPE;
    FUNCTION F_GenerateIDM
      RETURN NUMBER
    IS
      lv_dummy   VARCHAR2 (1);
      lv_sabnstu VARCHAR2 (1);
      lv_proxyIDM gpbprxy.gpbprxy_proxy_idm%TYPE;
      -- Check idm candidate against spriden, sabnstu and gpbprxy
      CURSOR C_FindIDM
      IS
        SELECT 'X' FROM spriden WHERE spriden_pidm = lv_proxyIDM
      UNION
      SELECT 'X' FROM gpbprxy WHERE gpbprxy_proxy_idm = lv_proxyIDM
      UNION
      SELECT 'X' FROM DUAL WHERE 'X' = lv_sabnstu;
    BEGIN
      -- Loop through using a sequence number to generate a new PIDM.
      LOOP
        SELECT PROXY_ACCESS_IDM_SEQUENCE.NEXTVAL INTO lv_proxyIDM FROM DUAL;
        -- Exit loop if IDM does not exist
        -- Call to sabnstu is dynamic in case student is not installed
        BEGIN
          EXECUTE IMMEDIATE 'SELECT DISTINCT ''X''                 
    FROM sabnstu                
    WHERE sabnstu_aidm = :1' INTO lv_sabnstu USING lv_proxyIDM;
        EXCEPTION
        WHEN OTHERS THEN
          lv_sabnstu := 'N';
        END;
        OPEN C_FindIDM;
        FETCH C_FindIDM INTO lv_dummy;
        IF C_FindIDM%NOTFOUND THEN
          CLOSE C_FindIDM;
          EXIT;
        END IF;
        CLOSE C_FindIDM;
      END LOOP;
      RETURN lv_proxyIDM;
    END F_GenerateIDM;
    FUNCTION F_GetProxyIDM(
        p_email goremal.goremal_email_address%TYPE,
        p_last spriden.spriden_last_name%TYPE,
        p_first spriden.spriden_first_name%TYPE,
        p_pidm spriden.spriden_pidm%TYPE DEFAULT NULL)
      RETURN NUMBER
    IS
      lv_email goremal.goremal_email_address%TYPE := TRIM (LOWER (p_email));
      lv_proxyIDM gpbprxy.gpbprxy_proxy_idm%TYPE  := 0;
      lv_pinhash gpbprxy.gpbprxy_pin%TYPE;
      lv_salt gpbprxy.gpbprxy_salt%TYPE;
      lv_hold_rowid gb_common.internal_record_id_type;
      lv_GPBPRXY_rec gp_gpbprxy.gpbprxy_rec;
      lv_GPBPRXY_ref gp_gpbprxy.gpbprxy_ref;
      lv_proxy_url VARCHAR2(300);
    BEGIN
      -- Get the proxy record
      -- If PIDM is provided then query by PIDM, if not found then query by email
      -- If PIDM is not provided then query by email
      IF p_pidm        IS NOT NULL THEN
        lv_GPBPRXY_ref := gp_gpbprxy.F_Query_One_By_PIDM (p_pidm);
        FETCH lv_GPBPRXY_ref INTO lv_GPBPRXY_rec;
        IF lv_GPBPRXY_ref%NOTFOUND THEN
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
      IF lv_GPBPRXY_ref%FOUND THEN
        -- If you found the proxy record by e-mail address (PIDM lookup failed) then update the proxy PIDM and ID
        IF p_pidm IS NOT NULL AND ((lv_email <> lv_GPBPRXY_rec.R_email_address) OR (NVL(lv_GPBPRXY_rec.R_PROXY_PIDM, 0) <> p_pidm)) THEN
          BEGIN
            gp_gpbprxy.P_Update ( p_proxy_idm => lv_GPBPRXY_rec.R_PROXY_IDM, p_proxy_pidm => p_pidm, p_email_address => lv_email, p_entity_cde => bwgkprxy.F_GetSpridenEntity (p_pidm), p_id => bwgkprxy.F_GetSpridenID (p_pidm), p_user_id => goksels.f_get_ssb_id_context );
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
      lv_salt     := gspcrpt.f_get_salt(26);
      gspcrpt.p_saltedhash( lv_salt, lv_salt, lv_pinhash);
      gp_gpbprxy.P_Create ( p_proxy_idm => lv_proxyIDM, p_email_address => lv_email, p_last_name => p_last, p_first_name => p_first, p_proxy_pidm => p_pidm, p_pin => lv_pinhash, p_pin_disabled_ind => 'C', p_salt => lv_salt, p_entity_cde => bwgkprxy.F_GetSpridenEntity (p_pidm), p_id => bwgkprxy.F_GetSpridenID (p_pidm), p_email_ver_date => NULL, p_pin_exp_date => NULL, p_create_user => goksels.f_get_ssb_id_context, p_create_date => SYSDATE, p_user_id => goksels.f_get_ssb_id_context, p_opt_out_adv_date => NULL, p_rowid_out => lv_hold_rowid );
      gp_gpbeltr.P_Create ( p_syst_code => global_syst, p_ctyp_code => 'NEW_PROXY', p_ctyp_url => NULL, p_ctyp_exp_date => SYSDATE + bwgkprxy.F_GetOption ('ACTION_VALID_DAYS'), p_ctyp_exe_date => NULL, p_transmit_date => NULL, p_proxy_idm => lv_proxyIDM, p_proxy_old_data => NULL, p_proxy_new_data => NULL, p_person_pidm => global_pidm, p_user_id => goksels.f_get_ssb_id_context, p_create_date => SYSDATE, p_create_user => goksels.f_get_ssb_id_context, p_rowid_out => lv_hold_rowid );
      gb_common.P_Commit;
      RETURN lv_proxyIDM;
    END F_GetProxyIDM;
    BEGIN
      p_proxyIDM  := F_GetProxyIDM(lv_email, lv_last, lv_first, global_pidm);
      ?           := p_proxyIDM;
    END;
        """
    }

    /*
    Input: Gidm, Global Proxy Pidm, Relationship Type, Banner ID
    Output: Error Status (Y/N), Error Message (NOPERSON, NOTARGET, NORETP)
    */

    public final static String CREATE_GLOBAL_PROXY() {
        """
    DECLARE
      p_proxyIDM gpbprxy.gpbprxy_proxy_idm%TYPE := ?;
      global_pidm spriden.spriden_pidm%TYPE := ?;
      p_RETP gtvretp.gtvretp_code%TYPE := ?;
      p_banner_id spriden.spriden_id%TYPE := ?;
      lv_hold_rowid gb_common.internal_record_id_type;
      lv_person_pidm spriden.spriden_pidm%TYPE;
      lv_GPBPRXY_rec gp_gpbprxy.gpbprxy_rec;
      lv_GPBPRXY_ref gp_gpbprxy.gpbprxy_ref;
      lv_GPRXREF_rec gp_gprxref.gprxref_rec;
      lv_GPRXREF_ref gp_gprxref.gprxref_ref;
      lv_attr_nt G_ATTRIBUTE_NT;
      lv_id VARCHAR2(09);
      lv_info twgrinfo.twgrinfo_label%TYPE := 'PROXYEXISTS';
      CURSOR C_AuthorizationList ( p_RETP gprxref.gprxref_retp_code%TYPE)
      IS
        SELECT m.twgbwmnu_name AS menu_name,
          m.twgbwmnu_desc      AS menu_desc,
          o.twgrmenu_url_text  AS menu_text,
          o.twgrmenu_url       AS menu_url,
          o.twgrmenu_sequence  AS menu_seq
        FROM TWGRMENU o,
          TWGBWMNU m
        WHERE o.twgrmenu_name = m.twgbwmnu_name
        AND (o.twgrmenu_url LIKE '%/proxy/%'
        OR o.twgrmenu_url LIKE '%/ssb/%')
        AND m.twgbwmnu_source_ind =
          (SELECT NVL (MAX (n.twgbwmnu_source_ind), 'B')
          FROM twgbwmnu n
          WHERE n.twgbwmnu_name     = m.twgbwmnu_name
          AND n.twgbwmnu_source_ind = 'L'
          )
      AND o.twgrmenu_source_ind =
        (SELECT NVL (MAX (p.twgrmenu_source_ind), 'B')
        FROM twgrmenu p
        WHERE p.twgrmenu_name     = o.twgrmenu_name
        AND p.twgrmenu_source_ind = 'L'
        )
      AND o.twgrmenu_name LIKE 'PROXY_ACCESS_'
        || p_RETP
        || '%'
      AND NVL(o.twgrmenu_enabled, 'N')       = 'Y'
      AND NVL(m.twgbwmnu_enabled_ind,'N')    = 'Y'
      AND NVL(m.twgbwmnu_adm_access_ind,'N') = 'N'
      AND NVL(o.twgrmenu_enabled,'N')        = 'Y'
      ORDER BY menu_desc,
        menu_name,
        menu_seq;
      id_cnt INTEGER;
      CURSOR id_c
      IS
        SELECT spriden_id
        FROM spriden
        WHERE UPPER(spriden_id) = UPPER(p_banner_id)
        AND spriden_change_ind IS NULL;
      error_status VARCHAR2(1) := 'N';
    BEGIN
      -- Get the proxy record and verify that it jives with the curent user
      lv_GPBPRXY_ref := gp_gpbprxy.F_Query_One (p_proxyIDM);
      FETCH lv_GPBPRXY_ref INTO lv_GPBPRXY_rec;
      CLOSE lv_GPBPRXY_ref;
      IF lv_GPBPRXY_rec.R_PROXY_PIDM <> global_pidm THEN
        error_status                 := 'Y';
      END IF;
      IF error_status  <> 'Y' THEN
        lv_person_pidm := bwgkprxy.F_GetSpridenPIDM (lv_id);
        -- Enable case insensitive ID's to be entered, but only if there is a single match
        id_cnt := 0;
        OPEN id_c;
        LOOP
          FETCH id_c INTO lv_id;
          EXIT
        WHEN id_c%NOTFOUND;
          id_cnt := id_cnt + 1;
        END LOOP;
        CLOSE id_c;
        IF id_cnt <> 1 THEN
          lv_id   := p_banner_id;
        END IF;
        lv_person_pidm := bwgkprxy.F_GetSpridenPIDM (lv_id);
        -- Ensure that a valid person has been selected
        IF lv_person_pidm IS NULL THEN
          lv_info         := 'NOPERSON';
          error_status    := 'Y';
        END IF;
      END IF;
      IF error_status <> 'Y' THEN
        -- Ensure that the person is eligible to be targeted by a Global Access User
        lv_attr_nt                                := gp_gorrsql.F_Execute_Rule ('SSB_ROLES', 'SSB_ROLE_PROXYTARGET', lv_person_pidm);
        IF lv_attr_nt                             IS NOT NULL AND lv_attr_nt.COUNT > 0 THEN
          IF lv_attr_nt (lv_attr_nt.FIRST)."value" = 'TRUE' THEN
            NULL; -- Pidm is valid to be targeted
          END IF;
        ELSE
          lv_info      := 'NOTARGET';
          error_status := 'Y';
        END IF;
      END IF;
      IF error_status <> 'Y' THEN
        -- Ensure that a proxy relationship has been selected
        IF p_RETP       = 'AAA' THEN
          error_status := 'Y';
          lv_info      := 'NORETP';
        END IF;
      END IF;
      IF error_status <> 'Y' THEN
        -- Ensure that a valid relationship record exists
        lv_GPRXREF_ref := gp_gprxref.F_Query_One (p_proxyIDM, lv_person_pidm);
        FETCH lv_GPRXREF_ref INTO lv_GPRXREF_rec;
        IF lv_GPRXREF_ref%NOTFOUND THEN
          gp_gprxref.P_Create (p_proxy_idm => p_proxyIDM, p_person_pidm => lv_person_pidm, p_retp_code => p_RETP, p_proxy_desc => NULL, p_start_date => TRUNC (SYSDATE), p_stop_date => TRUNC (SYSDATE), p_create_user => goksels.f_get_ssb_id_context, p_create_date => SYSDATE, p_user_id => goksels.f_get_ssb_id_context, p_passphrase => NULL, p_rowid_out => lv_hold_rowid);
        ELSE
          gp_gprxref.P_Update (p_proxy_idm => p_proxyIDM, p_person_pidm => lv_person_pidm, p_retp_code => p_RETP, p_start_date => TRUNC (SYSDATE), p_stop_date => TRUNC (SYSDATE), p_user_id => goksels.f_get_ssb_id_context);
          SELECT ROWID
          INTO lv_hold_rowid
          FROM gprxref
          WHERE gprxref_proxy_idm = p_proxyIDM
          AND gprxref_person_pidm = lv_person_pidm;
        END IF;
        gb_common.P_Commit;
        CLOSE lv_GPRXREF_ref;
        -- GLOBAL NEW_PROXY e-mail
        -- Send generic e-mail message since no action is required
        gp_gpbeltr.P_Create ( p_syst_code => 'PROXY_GLOBAL_ACCESS', p_ctyp_code => 'NEW_PROXY', p_ctyp_url => NULL, p_ctyp_exp_date => NULL, p_ctyp_exe_date => NULL, p_transmit_date => NULL, p_proxy_idm => p_proxyIDM, p_proxy_old_data => bwgkprxy.F_GetSpridenID(global_pidm), p_proxy_new_data => p_banner_id, p_person_pidm => lv_person_pidm, p_user_id => goksels.f_get_ssb_id_context, p_create_date => SYSDATE, p_create_user => goksels.f_get_ssb_id_context, p_rowid_out => lv_hold_rowid );
        gb_common.P_Commit;
        bwgkprxy.P_SendEmail(lv_hold_rowid);
        -- Load authorization records for the selected person
        FOR auth_rec IN C_AuthorizationList (p_RETP)
        LOOP
          IF gp_gprauth.F_Exists (p_proxy_idm => p_proxyIDM, p_person_pidm => lv_person_pidm, p_page_name => auth_rec.menu_url) = 'Y' THEN
            gp_gprauth.P_Update (p_proxy_idm => p_proxyIDM, p_person_pidm => lv_person_pidm, p_page_name => auth_rec.menu_url, p_auth_ind => 'Y', p_user_id => goksels.f_get_ssb_id_context);
          ELSE
            gp_gprauth.P_Create (p_proxy_idm => p_proxyIDM, p_person_pidm => lv_person_pidm, p_page_name => auth_rec.menu_url, p_auth_ind => 'Y', p_create_user => goksels.f_get_ssb_id_context, p_create_date => SYSDATE, p_user_id => goksels.f_get_ssb_id_context, p_rowid_out => lv_hold_rowid);
          END IF;
        END LOOP;
        gb_common.P_Commit;
      END IF;
      ? := error_status;
      ? := lv_info;
    END;
        """
    }

    /*
    Input: Target Banner ID (Case Insensitive)
    Output: Returns Target PIDM or NULL if no match is found
    */
    public final static String GET_TARGET_PIDM_FROM_CASE_INSENSITIVE_ID = """
DECLARE
  p_banner_id spriden.spriden_id%TYPE := ?;
  return_value VARCHAR(08);
  lv_person_pidm spriden.spriden_pidm%TYPE;
  lv_id  VARCHAR2(09);
  id_cnt INTEGER;
  CURSOR id_c
  IS
    SELECT spriden_id
    FROM spriden
    WHERE UPPER(spriden_id) = UPPER(p_banner_id)
    AND spriden_change_ind IS NULL;
BEGIN
  -- Enable case insensitive IDs to be entered, but only if there is a single match
  id_cnt := 0;
  OPEN id_c;
  LOOP
    FETCH id_c INTO lv_id;
    EXIT
  WHEN id_c%NOTFOUND;
    id_cnt := id_cnt + 1;
  END LOOP;
  CLOSE id_c;
  IF id_cnt <> 1 THEN
    lv_id   := p_banner_id;
  END IF;
  --Check if ID gets valid PIDM
  lv_person_pidm      := bwgkprxy.F_GetSpridenPIDM (lv_id);
  IF (lv_person_pidm) IS NOT NULL THEN
    return_value      := lv_person_pidm;
  ELSE
    return_value := 'NULL';
  END IF;
  ? := return_value;
END;
"""

    /*
    Input: Global Proxy GIDM
    Output: List of Proxied Users for Global Proxy
    */
    public final static String PROXIED_USERS_LIST_FOR_GLOBAL_PROXY = """
DECLARE
  lv_attr_nt     G_ATTRIBUTE_NT;
  lv_minify_ext  varchar2(04);
  TYPE T_desc_table IS TABLE OF TWGBWMNU.TWGBWMNU_DESC%TYPE
  index by binary_integer;
  lv_used_desc   T_desc_table;
  lv_value varchar2(2000);

  CURSOR C_PersonList
      IS
           SELECT GPRXREF.*, SPRIDEN_ID ID
             FROM GPRXREF, SPRIDEN
             WHERE GPRXREF_PERSON_PIDM = SPRIDEN_PIDM
             AND SPRIDEN_CHANGE_IND IS NULL
             AND GPRXREF_PROXY_IDM = ?
                  AND EXISTS (SELECT 'Y' FROM GOBTPAC 
                  WHERE GOBTPAC_PIDM = GPRXREF_PERSON_PIDM
                  AND GOBTPAC_PIN_DISABLED_IND = 'N')
         ORDER BY F_Format_Name (GPRXREF_PERSON_PIDM, 'LFMI');

  active varchar2(3000);
  inactive varchar2(3000);
  student varchar2(3000);
  students varchar2(3000);

BEGIN

      -- Loop through potential Banner Web users
      -- Only show users if they have the Proxy Management role

     active := '"active":[';
     inactive := '"inactive":[';

      FOR person IN C_PersonList
      LOOP
         -- Execute the GORRSQL rule for proxy management
         -- Only create tab for first occurance of role
         lv_attr_nt :=
            gp_gorrsql.F_Execute_Rule ('SSB_ROLES',
                                       'SSB_ROLE_PROXYTARGET',
                                       person.GPRXREF_PERSON_PIDM);

         IF lv_attr_nt IS NOT NULL AND lv_attr_nt.COUNT > 0
         THEN
            FOR i IN lv_attr_nt.FIRST .. lv_attr_nt.LAST
            LOOP
               IF lv_attr_nt (i)."value" = 'TRUE'
               THEN
                IF lv_used_desc.EXISTS (person.GPRXREF_PERSON_PIDM)
                  THEN
                     NULL;
                 ELSE
                  lv_used_desc (person.GPRXREF_PERSON_PIDM) :=
                        person.GPRXREF_PERSON_PIDM;

                   gspcrpt.p_apply(dbms_random.string('P',12) || '::'  || to_char(sysdate,'DDMMYYYYHH24MISS') || '::' || person.GPRXREF_PERSON_PIDM, lv_value);

                      student := '{' ||
                      '"name" ' || ':' || '"' || f_format_name (person.GPRXREF_PERSON_PIDM, 'FML') || '"' ||
                      ',"id" ' || ':'  || '"' || person.ID || '"' || 
                      ',"token" ' || ':'  || '"' || lv_value || '"' || '},';
                      
                      IF TRUNC(SYSDATE) BETWEEN TRUNC(person.GPRXREF_START_DATE) AND TRUNC (person.GPRXREF_STOP_DATE)
                      THEN
                         active := active || student;
                      ELSE
                         inactive := inactive || student;
                      END IF;
                  END IF;
                 END IF;
            END LOOP;

         END IF;
      END LOOP;

     active := TRIM(TRAILING ',' FROM active );
     active := active || ']';
     
     inactive := TRIM(TRAILING ',' FROM inactive );
     inactive := inactive || ']';

     students := '{ "students":{'
                 || active || ','
                 || inactive
                 || '}}';

     ? := students;

END;
    """

}
