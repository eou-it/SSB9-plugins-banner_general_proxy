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
        def errorStatus

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

        //def session = RequestContextHolder?.currentRequestAttributes()?.getSession()
        //session."gidm" = gidm

        return [verify: actionVerify.equals("Y"), login: login.equals("Y"), doPin: doPin.equals("Y"), message: msg, error: error.equals("Y")]
    }


    public def savePin(def p_proxyIDM, def p_pin1, def p_pin2, def p_email, def p_pin_orig) {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        def msg
        def error
        def errorStatus

        sql.call("""
      DECLARE
      lv_pinhash       gpbprxy.gpbprxy_pin%TYPE;
      lv_salt          gpbprxy.gpbprxy_salt%TYPE;
      lv_msg           gb_common.err_type;
      lv_error         varchar2(20);
      lv_GPBPRXY_rec   gp_gpbprxy.gpbprxy_rec;
      lv_GPBPRXY_ref   gp_gpbprxy.gpbprxy_ref;
      lv_context_hash  gpbprxy.gpbprxy_pin%TYPE;
      error_status     VARCHAR2(1);

      FUNCTION F_Validate_Credentials (
         p_email     gpbprxy.gpbprxy_email_address%TYPE DEFAULT NULL,
         p_pin       gpbprxy.gpbprxy_pin%TYPE           DEFAULT NULL,
         p_proxyIDM  gpbprxy.gpbprxy_proxy_idm%TYPE     DEFAULT NULL)
         RETURN VARCHAR2
      IS
         lv_proxyIDM      gpbprxy.gpbprxy_proxy_idm%TYPE;
         lv_pinhash       gpbprxy.gpbprxy_pin%TYPE;
         lv_GPBPRXY_rec   gp_gpbprxy.gpbprxy_rec;
         lv_GPBPRXY_ref   gp_gpbprxy.gpbprxy_ref;
      BEGIN
         -- Get proxy by e-mail address
         lv_proxyIDM := bwgkpxya.F_GetProxyIDM (p_email);

         IF NVL(lv_proxyIDM,0) <> p_proxyIDM THEN
            RETURN 'N';
         ELSE
           lv_GPBPRXY_ref := gp_gpbprxy.F_Query_One (lv_proxyIDM);

           FETCH lv_GPBPRXY_ref INTO lv_GPBPRXY_rec;

           CLOSE lv_GPBPRXY_ref;

           gspcrpt.P_SaltedHash (p_pin, lv_GPBPRXY_rec.R_SALT, lv_pinhash);

           -- Check for disabled PIN
           IF NVL(lv_GPBPRXY_rec.R_PIN_DISABLED_IND,'N') IN ('Y','E') THEN
             RETURN 'N';
           -- Check for expired PIN (unless it was a 'R'eset Pin condition or a 'C'reate new pin
           ELSIF NVL (TRUNC(lv_GPBPRXY_rec.R_PIN_EXP_DATE), TRUNC(SYSDATE)) < TRUNC(SYSDATE)  AND
                 NVL(lv_GPBPRXY_rec.R_PIN_DISABLED_IND,'N') = 'N' THEN
             RETURN 'N';
           -- Compare hashed values to authenticate PIN
           ELSIF lv_pinhash <> lv_GPBPRXY_rec.R_PIN THEN
             -- update invalid logins count
             --bwgkpxya.P_Update_Invalid_Login(lv_proxyIDM, lv_GPBPRXY_rec.R_PIN_DISABLED_IND, lv_GPBPRXY_rec.R_INV_LOGIN_CNT);
             RETURN 'N';
           ELSE
             RETURN 'Y';
           END IF;
         END IF;
      END F_Validate_Credentials;
     
      BEGIN

      lv_error := NULL;
      lv_msg   := NULL;

      IF F_Validate_Credentials (${p_email}, TRIM(${p_pin_orig}), ${p_proxyIDM} ) = 'N' THEN
         lv_error := 'ERR_USER';
      ELSIF NVL (${p_pin1}, '1bogus1pin1') <> NVL (${p_pin2}, '2bogus2pin2') THEN
         lv_error := 'ERR_NOMATCH';
      ELSIF NVL(bwgkprxy.F_GetOption ('PIN_VALIDATION_VIA_GUAPPRF'),'Y') = 'Y' THEN
           gb_third_party_access_rules.p_validate_pinrules  (
             p_pidm             => 0,
             p_pin              => ${p_pin1},
             p_pin_reusechk_ind => 'N',
             error_message      => lv_msg);
           if lv_msg is not null then
             lv_error := 'ERR_GUAPPRF';
           else
           -- The PIN rules do not check for leading spaces so we have to do that here, just in case
             IF TRIM(NVL (${p_pin1}, 'x')) <>  NVL (${p_pin1}, 'x') THEN
               lv_error := 'ERR_GUAPPRF';
               lv_msg   := g\$_NLS.Get('BWGKPXYA1-0050','SQL', 'PIN values may not start with or end with a space');
             END IF;
           end if;
      ELSIF LENGTH (NVL (${p_pin1}, 'x')) < bwgkprxy.F_GetOption ('PIN_LENGTH_MINIMUM') OR
            LENGTH (NVL (${p_pin2}, 'x')) < bwgkprxy.F_GetOption ('PIN_LENGTH_MINIMUM') THEN
              lv_error := 'ERR_TOOSHORT';
      ELSIF TRIM(NVL (${p_pin1}, 'x')) <>  NVL (${p_pin1}, 'x') THEN
              lv_error := 'ERR_GUAPPRF';
              lv_msg   := g\$_NLS.Get('BWGKPXYA1-0051','SQL', 'PIN values may not start with or end with a space');
      END IF;

      IF lv_error is not null then
       -- P_PA_ResetPin (p_proxyIDM, lv_error, replace(lv_msg,'::',' '));
       error_status := 'Y';
      ELSE
       error_status := 'N';
       lv_salt := gspcrpt.F_Get_Salt (LENGTH (${p_pin1}));
       gspcrpt.P_SaltedHash (${p_pin1}, lv_salt, lv_pinhash);

        gp_gpbprxy.P_Update (
           p_proxy_idm          => ${p_proxyIDM},
           p_pin_disabled_ind   => 'N',
           p_pin_exp_date       => SYSDATE + bwgkprxy.F_GetOption ('PIN_LIFETIME_DAYS'),
           p_pin                => lv_pinhash,
           p_inv_login_cnt      => 0,
           p_salt               => lv_salt,
           p_user_id            => goksels.f_get_ssb_id_context);

        gb_common.P_Commit;      
        
       END IF;
       
          ${Sql.VARCHAR}  := lv_error;
          ${Sql.VARCHAR}  := lv_msg;
          ${Sql.VARCHAR}  := error_status;
             
      END;
            """){ errorOut, msgOut, errorStatusOut ->
            error = errorOut
            msg = msgOut
            errorStatus = errorStatusOut
        }

        println "errorStatus: " + errorStatus
        println "msg: " + msg
        println "error: " + error

        return [errorStatus: errorStatus.equals("Y"), message: msg, error: error]

    }

}
