package net.hedtech.banner.proxy.api

class GlobalProxyManagementApi {
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

    public final static String CHECK_IF_GLOBAL_PROXY_ACCESS_TARGET_IS_VALID = """
DECLARE
  is_valid_to_be_proxied  VARCHAR2 (5);
  valid_banner_id_entered VARCHAR2 (5);
  return_json             VARCHAR(4000);
  lv_person_pidm spriden.spriden_pidm%TYPE;
  lv_attr_nt G_ATTRIBUTE_NT;
BEGIN
  is_valid_to_be_proxied  := 'false';
  valid_banner_id_entered := 'false';
  --Check if ID gets valid PIDM
  lv_person_pidm            := bwgkprxy.F_GetSpridenPIDM (?);
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
  ?           := return_json;
END;
"""
}
