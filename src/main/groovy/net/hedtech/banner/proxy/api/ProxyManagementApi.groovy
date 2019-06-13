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
}
