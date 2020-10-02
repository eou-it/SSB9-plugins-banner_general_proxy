package net.hedtech.banner.proxy.api

class GlobalProxyManagementApi {
    public final static String RELATIONSHIP_OPTION_LIST = """
  DECLARE
     global_syst CONSTANT gtvsyst.gtvsyst_code%TYPE := 'PROXY_GLOBAL_ACCESS';
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
}
