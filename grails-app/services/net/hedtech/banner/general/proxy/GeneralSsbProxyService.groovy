package net.hedtech.banner.general.proxy

import groovy.sql.Sql
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.context.ServletContextHolder as SC
import org.springframework.web.context.request.RequestContextHolder

class GeneralSsbProxyService {
    private final Logger log = Logger.getLogger(getClass())
    def sessionFactory                     // injected by Spring
    def dataSource                         // injected by Spring
    def grailsApplication                  // injected by Spring

    /**
     * This methods defines the p_token authentication as passed to the proxy
     * 1. Valid Token - redirect to the new pin
     * 2. Valid Toke after Pin was established- login action
     * 3. Error (wrong parameter or altered token) - login screen
     */
    public def setProxy(def token) {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        def login
        def gidm
        def actionVerify
        def doPin
        def msg
        def error

        sql.call("""
       DECLARE
              p_verify      gpbprxy.gpbprxy_salt%TYPE DEFAULT '!@#bogus!@#';
              lv_rowid              gb_common.internal_record_id_type;
              lv_GPBELTR_ref        gp_gpbeltr.gpbeltr_ref;
              lv_GPBELTR_rec        gp_gpbeltr.gpbeltr_rec;
              lv_GPBPRXY_rec        gp_gpbprxy.gpbprxy_rec;
              lv_GPBPRXY_ref        gp_gpbprxy.gpbprxy_ref;

              do_pin                varchar2(1);
              msg                   varchar2(100);

FUNCTION F_ActionVerify (p_proxyIDM    gpbprxy.gpbprxy_proxy_idm%TYPE,
                         p_CTYP        gtvctyp.gtvctyp_code%TYPE,
                         p_verify      gpbprxy.gpbprxy_salt%TYPE)
   RETURN BOOLEAN
IS
   lv_ind   gtvotyp.gtvotyp_option_default%TYPE;

   CURSOR C_VerifySalt
   IS
      SELECT 'Y'
        FROM GPBPRXY
       WHERE GPBPRXY_SALT = p_verify AND GPBPRXY_PROXY_IDM = p_proxyIDM;
BEGIN
   IF NVL (bwgkprxy.F_GetOption ('VERIFY_' || p_CTYP || '_ACTION'), 'N') = 'N'
   THEN
      RETURN FALSE;
   END IF;

   OPEN C_VerifySalt;

   FETCH C_VerifySalt INTO lv_ind;

   IF C_VerifySalt%FOUND
   THEN
      CLOSE C_VerifySalt;
      RETURN FALSE;
   END IF;

   CLOSE C_VerifySalt;

   RETURN TRUE;
END F_ActionVerify;

       BEGIN
              lv_rowid := twbkbssf.f_decode_base64(${token});
              
              lv_GPBELTR_ref := gp_gpbeltr.F_Query_By_Rowid(lv_rowid);
              
               FETCH lv_GPBELTR_ref INTO lv_GPBELTR_rec;

            IF lv_GPBELTR_ref%NOTFOUND THEN
            
                ${Sql.VARCHAR}  := 'Y';
                msg := 'token-error';

            ELSIF (lv_GPBELTR_ref%FOUND AND F_ActionVerify (lv_GPBELTR_rec.R_PROXY_IDM,
                            lv_GPBELTR_rec.R_CTYP_CODE,
                            TRIM(p_verify))) THEN
                            
             ${Sql.VARCHAR} := lv_GPBELTR_rec.R_PROXY_IDM;
                            
             ${Sql.VARCHAR}  := 'Y'; 

          ELSE             
             do_pin := 'Y';             
          END IF;
          
          ${Sql.VARCHAR}  := do_pin;
          ${Sql.VARCHAR}  := msg;

       EXCEPTION
         WHEN OTHERS THEN ${Sql.VARCHAR} := 'Y';

        END ;
            """) { loginOut, gidmOut, actionVerifyOut, pinOut, msgOut, errorOut ->
            login = loginOut
            gidm = gidmOut
            actionVerify = actionVerifyOut
            doPin = pinOut
            msg = msgOut
            error = errorOut
        }


        println "GIDM: " + gidm
        println "ActionVerify: " + actionVerify
        println "LoginOUT: " + login
        println "Do Pin: " + doPin
        println "Message: " + msg
        println "Error: " + error

        RequestContextHolder.currentRequestAttributes().getSession()["gidm"] = gidm

        return [verify: actionVerify.equals("Y"), login: login.equals("Y"), doPin: doPin.equals("Y"), message: msg, error: error.equals("Y")]
    }



    public def setProxyVerify(def token, def verify) {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        def login
        def gidm
        def actionVerify
        def doPin
        def msg
        def error

        sql.call("""
       DECLARE
              p_verify      gpbprxy.gpbprxy_salt%TYPE DEFAULT '!@#bogus!@#';
              lv_rowid              gb_common.internal_record_id_type;
              lv_GPBELTR_ref        gp_gpbeltr.gpbeltr_ref;
              lv_GPBELTR_rec        gp_gpbeltr.gpbeltr_rec;
              lv_GPBPRXY_rec        gp_gpbprxy.gpbprxy_rec;
              lv_GPBPRXY_ref        gp_gpbprxy.gpbprxy_ref;

              do_pin                varchar2(1);
              msg                   varchar2(100);

FUNCTION F_ActionVerify (p_proxyIDM    gpbprxy.gpbprxy_proxy_idm%TYPE,
                         p_CTYP        gtvctyp.gtvctyp_code%TYPE,
                         p_verify      gpbprxy.gpbprxy_salt%TYPE)
   RETURN BOOLEAN
IS
   lv_ind   gtvotyp.gtvotyp_option_default%TYPE;

   CURSOR C_VerifySalt
   IS
      SELECT 'Y'
        FROM GPBPRXY
       WHERE GPBPRXY_SALT = p_verify AND GPBPRXY_PROXY_IDM = p_proxyIDM;
BEGIN
   IF NVL (bwgkprxy.F_GetOption ('VERIFY_' || p_CTYP || '_ACTION'), 'N') = 'N'
   THEN
      RETURN FALSE;
   END IF;

   OPEN C_VerifySalt;

   FETCH C_VerifySalt INTO lv_ind;

   IF C_VerifySalt%FOUND
   THEN
      CLOSE C_VerifySalt;
      RETURN FALSE;
   END IF;

   CLOSE C_VerifySalt;

   RETURN TRUE;
END F_ActionVerify;

       BEGIN
              lv_rowid := twbkbssf.f_decode_base64(${token});
              
              lv_GPBELTR_ref := gp_gpbeltr.F_Query_By_Rowid(lv_rowid);
              
               FETCH lv_GPBELTR_ref INTO lv_GPBELTR_rec;

            IF lv_GPBELTR_ref%NOTFOUND THEN
            
                ${Sql.VARCHAR}  := 'Y';
                msg := 'token-error';

            ELSIF (lv_GPBELTR_ref%FOUND AND F_ActionVerify (lv_GPBELTR_rec.R_PROXY_IDM,
                            lv_GPBELTR_rec.R_CTYP_CODE,
                            TRIM(${verify}))) THEN
                            
             ${Sql.VARCHAR} := lv_GPBELTR_rec.R_PROXY_IDM;
                            
             ${Sql.VARCHAR}  := 'Y'; 

          ELSE             
             do_pin := 'Y';             
          END IF;
          
          ${Sql.VARCHAR}  := do_pin;
          ${Sql.VARCHAR}  := msg;

       EXCEPTION
         WHEN OTHERS THEN ${Sql.VARCHAR} := 'Y';

        END ;
            """) { loginOut, gidmOut, actionVerifyOut, pinOut, msgOut, errorOut ->
            login = loginOut
            gidm = gidmOut
            actionVerify = actionVerifyOut
            doPin = pinOut
            msg = msgOut
            error = errorOut
        }


        println "GIDM: " + gidm
        println "ActionVerify: " + actionVerify
        println "LoginOUT: " + login
        println "Do Pin: " + doPin
        println "Message: " + msg
        println "Error: " + error

        RequestContextHolder.currentRequestAttributes().getSession()["gidm"] = gidm

        return [verify: actionVerify.equals("Y"), login: login.equals("Y"), doPin: doPin.equals("Y"), message: msg, error: error.equals("Y")]
    }


}
