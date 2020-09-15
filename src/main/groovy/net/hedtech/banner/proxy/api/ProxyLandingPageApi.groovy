/********************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.proxy.api

class ProxyLandingPageApi {

    public final static String GET_GIDM_GLOBAL_ACCESS = """
  DECLARE
     pidm SPRIDEN.SPRIDEN_PIDM%TYPE;
     gidm GPBPRXY.GPBPRXY_PROXY_IDM%TYPE;
     
--
   cursor getGidm(pidm SPRIDEN.SPRIDEN_PIDM%TYPE) is
   select gpbprxy_proxy_idm
     from gpbprxy
     where gpbprxy_proxy_pidm = pidm;
   BEGIN
   pidm := ?;
   --  
   open getGidm(pidm);
   fetch getGidm into gidm;  
--   
   ? := gidm;
   -- DBMS_OUTPUT.PUT_LINE('GIDM: ' || gidm);
--
  END;
"""

    public final static String GET_STUDENT_ID_FROM_TOKEN ="""
  DECLARE
     token varchar2(2000);
     proxyId varchar2(2000);
   BEGIN
   token := ?;
   --
   gspcrpu.p_unapply (token,proxyId);
   proxyId := substr(proxyId,instr(proxyId,'::', 1, 2) + 2);
--   
   ? := proxyId;
--
  END;
"""

    public final static String STUDENT_TOKEN = """
    DECLARE
      lv_value varchar2(2000);
      id SPRIDEN.SPRIDEN_ID%TYPE;
    BEGIN
    --
    id := ?;
    gspcrpt.p_apply(dbms_random.string('P',12) || '::'  || to_char(sysdate,'DDMMYYYYHH24MISS') || '::' || id, lv_value);
    --
    ? := lv_value;
    --
    END;
    """

    public final static String STUDENT_LIST_FOR_PROXY = """

DECLARE
  lv_attr_nt     G_ATTRIBUTE_NT;
  lv_minify_ext  varchar2(04);
  TYPE T_desc_table IS TABLE OF TWGBWMNU.TWGBWMNU_DESC%TYPE
  index by binary_integer;
  lv_used_desc   T_desc_table;

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
                                       'SSB_ROLE_PROXYMGMT',
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

                      student := '{' ||
                      '"name" ' || ':' || '"' || f_format_name (person.GPRXREF_PERSON_PIDM, 'FML') || '"' ||
                      ',"id" ' || ':'  || '"' || person.ID || '"' || '},';
                      
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

    public final static String PROXY_PAGES = """
DECLARE
  --
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
         AND  (o.twgrmenu_url like '%/proxy/%')
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
           AND o.twgrmenu_name LIKE 'PROXY_ACCESS_' || p_retp || '%'
           AND NVL(o.twgrmenu_enabled, 'N')       = 'Y'
           AND NVL(m.twgbwmnu_enabled_ind,'N')    = 'Y'
           AND NVL(m.twgbwmnu_adm_access_ind,'N') = 'N'
        UNION
        SELECT m.twgbwmnu_name     AS menu_name,
               m.twgbwmnu_desc     AS menu_desc,
               o.twgrmenu_url_text AS menu_text,
               o.twgrmenu_url      AS menu_url,
               o.twgrmenu_sequence AS menu_seq
          FROM TWGRMENU o, TWGBWMNU m
         WHERE o.twgrmenu_name = m.twgbwmnu_name
         AND  (o.twgrmenu_url like '%%/ssb/%%')
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
           AND o.twgrmenu_name LIKE 'PROXY_ACCESS_' || p_retp || '%'
           AND NVL(o.twgrmenu_enabled, 'N')       = 'Y'
           AND NVL(m.twgbwmnu_enabled_ind,'N')    = 'Y'
           AND NVL(m.twgbwmnu_adm_access_ind,'N') = 'N'
           AND NVL(o.twgrmenu_enabled,'N')        = 'Y'
           AND EXISTS (SELECT 'X'
           from GVQ_PAGE_ROLE_MAPPING
                   where application_id = 'SSS'
                   and (page_url = o.twgrmenu_url
                        and role_code = 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'
                        OR (page_url = '/ssb/financialAid/**'
                        and role_code = 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'
                        and INSTR(o.twgrmenu_url,'financialAid') > 0
                        )
                        )
                OR (o.twgrmenu_url IN ('/ssb/studentProfile','/ssb/studentGrades'))
            )
      ORDER BY menu_desc, menu_name, menu_seq;

lv_RETP        gtvretp.gtvretp_code%TYPE;
pages          VARCHAR2(32000);
--
  FUNCTION GET_AID_YEAR(pidmIn NUMBER) RETURN VARCHAR IS
   CURSOR GET_DEFAULT_AID_YEAR(pidmIn SPRIDEN.SPRIDEN_PIDM%TYPE) IS
    SELECT MAX(aidy_code) 
     from RVQ_ACTIVE_AID_YEARS
     where pidm = pidmIn;
--    
     defaultAidYear VARCHAR2(4);
  BEGIN
    OPEN GET_DEFAULT_AID_YEAR(pidmIn);
    FETCH GET_DEFAULT_AID_YEAR into defaultAidYear;
    CLOSE GET_DEFAULT_AID_YEAR;
    
      RETURN NVL(defaultAidYear,'0000');
  END GET_AID_YEAR;
  
  FUNCTION checkRole(pidm number,url varchar2) RETURN BOOLEAN IS
holdRole varchar2(1);

cursor chckRole is
select 'Y' from twgrwmrl
where twgrwmrl_name = url
and exists (  select * FROM (
               select 
               CASE govrole_student_ind
               WHEN 'Y' THEN 'STUDENT'
               ELSE null
               END AS "ROLE"
               FROM GOVROLE
               WHERE govrole_pidm = pidm
               UNION
               select 
               CASE govrole_alumni_ind
               WHEN 'Y' THEN 'ALUMNI'
               ELSE null
               END AS "ROLE"
               FROM GOVROLE
               WHERE govrole_pidm = pidm
               UNION
               select 
               CASE govrole_employee_ind
               WHEN 'Y' THEN 'EMPLOYEE'
               ELSE null
               END AS "ROLE"
               FROM GOVROLE
               WHERE govrole_pidm = pidm
               UNION
               select 
               CASE govrole_faculty_ind
               WHEN 'Y' THEN 'FACULTY'
               ELSE null
               END AS "ROLE"
               FROM GOVROLE
               WHERE govrole_pidm = pidm
               UNION
               select 
               CASE govrole_finance_ind
               WHEN 'Y' THEN 'FINANCE'
               ELSE null
               END AS "ROLE"
               FROM GOVROLE
               WHERE govrole_pidm = pidm
               UNION
               select 
               CASE govrole_finaid_ind
               WHEN 'Y' THEN 'FINAID'
               ELSE null
               END AS "ROLE"
               FROM GOVROLE
               WHERE govrole_pidm = pidm
               ) where role is not null
               and role = twgrwmrl_role
               );


begin
 open chckRole;
 fetch chckRole into holdRole;
 if chckRole%FOUND then
    return true;
    else
    return false;
 end if;
END checkRole;
--
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
            AND checkRole (?, auth_rec.menu_url)
         THEN
           -- TO DO Add AidYear Calculation
            IF (INSTR(auth_rec.menu_url,'financialAid') > 0) THEN
              auth_rec.menu_url := REPLACE(auth_rec.menu_url,'#!','');
              auth_rec.menu_url := auth_rec.menu_url || '/' || GET_AID_YEAR(?);
            END IF;
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