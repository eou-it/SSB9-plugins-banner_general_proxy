package net.hedtech.banner.proxy.api

class ProxyLandingPageApi {

    public final static String STUDENT_LIST_FOR_PROXY = """

DECLARE
  lv_attr_nt     G_ATTRIBUTE_NT;
  lv_minify_ext  varchar2(04);

  CURSOR C_PersonList
      IS
           SELECT GPRXREF.*, SPRIDEN_ID ID
             FROM GPRXREF, SPRIDEN
             WHERE GPRXREF_PERSON_PIDM = SPRIDEN_PIDM
             AND SPRIDEN_CHANGE_IND IS NULL
             AND GPRXREF_PROXY_IDM = ?
                  AND TRUNC (SYSDATE) BETWEEN TRUNC (GPRXREF_START_DATE)
                                          AND TRUNC (GPRXREF_STOP_DATE)
                  AND EXISTS (SELECT 'Y' FROM GOBTPAC 
                  WHERE GOBTPAC_PIDM = GPRXREF_PERSON_PIDM
                  AND GOBTPAC_PIN_DISABLED_IND = 'N')
         ORDER BY F_Format_Name (GPRXREF_PERSON_PIDM, 'LFMI');

 students varchar2(3000);

BEGIN

      -- Loop through potential Banner Web users
      -- Only show users if they have the Proxy Management role

  students := '{ "students":[';

      FOR person IN C_PersonList
      LOOP
         -- Execute the GORRSQL rule for proxy management
         -- Only create tab for first occurance of role
         lv_attr_nt :=
            gp_gorrsql.F_Execute_Rule ('SSB_ROLES',
                                       'SSB_ROLE_PROXYMGMT',
                                       person.GPRXREF_PERSON_PIDM);

         IF lv_attr_nt IS NOT NULL AND lv_attr_nt.COUNT > 0
         THEN
            FOR i IN lv_attr_nt.FIRST .. lv_attr_nt.LAST
            LOOP
               IF lv_attr_nt (i)."value" = 'TRUE'
               THEN

                      students := students || '{' ||
                      '"name" ' || ':' || '"' || f_format_name (person.GPRXREF_PERSON_PIDM, 'FML') || '"' ||
                      ',"id" ' || ':'  || '"' || person.ID || '"' || '},';
                  END IF;
            END LOOP;

         END IF;
      END LOOP;

     students := TRIM(TRAILING ',' FROM students );

     students := students || ']}';

     ? := students;

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

BEGIN
--
pages:= '{ "pages":[';
--
lv_RETP := gp_gprxref.F_GetXREF_RETP (?, ?);
--
FOR auth_rec IN C_AuthorizationList (lv_RETP)
--
      LOOP
         -- Only show page link if it is authorized and is valid for the person
         IF bwgkprxy.F_GetAuthInd (?,
                                   ?,
                                   auth_rec.menu_url) = 'Y'
            --AND twbkwbis.F_ValidLink (auth_rec.menu_url)
         THEN
            pages := pages || '{' ||
                      '"url" ' || ':' || '"' || auth_rec.menu_url || '"' ||
                      ',"desc" ' || ':'  || '"' || auth_rec.menu_text || '"' || '},';
      END IF;
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