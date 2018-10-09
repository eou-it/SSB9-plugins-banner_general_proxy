package net.hedtech.banner.proxy.api

class PinManagementApi {

    public final static String SET_PROXY = """
DECLARE
   p_verify      gpbprxy.gpbprxy_salt%TYPE DEFAULT '!@#bogus!@#';
   lv_rowid              gb_common.internal_record_id_type;
   lv_GPBELTR_ref        gp_gpbeltr.gpbeltr_ref;
   lv_GPBELTR_rec        gp_gpbeltr.gpbeltr_rec;
   lv_GPBPRXY_rec        gp_gpbprxy.gpbprxy_rec;
   lv_GPBPRXY_ref        gp_gpbprxy.gpbprxy_ref;

   do_pin                varchar2(1) := 'N';
   msg                   varchar2(100);
   lv_loginOut           varchar2(1)  := 'N';
   lv_actionVerifyOut    varchar2(1)  := 'N';
   lv_gidm               number;
   
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
   lv_rowid := twbkbssf.f_decode_base64(?);

   lv_GPBELTR_ref := gp_gpbeltr.F_Query_By_Rowid(lv_rowid);

   FETCH lv_GPBELTR_ref INTO lv_GPBELTR_rec;

   IF lv_GPBELTR_ref%NOTFOUND THEN

      lv_loginOut  := 'Y';
      msg := 'token-error';

      -- Check for expiration date
      -- Check for action already executed
   ELSIF NVL (TRUNC(lv_GPBELTR_rec.R_CTYP_EXP_DATE), TRUNC(SYSDATE)) < TRUNC(SYSDATE)
   OR lv_GPBELTR_rec.R_CTYP_EXE_DATE IS NOT NULL
   THEN
     lv_loginOut  := 'Y';
     msg := 'tokenExpire';

   ELSIF (lv_GPBELTR_ref%FOUND)
   THEN
      lv_gidm := lv_GPBELTR_rec.R_PROXY_IDM;

      IF(F_ActionVerify (lv_GPBELTR_rec.R_PROXY_IDM, lv_GPBELTR_rec.R_CTYP_CODE, TRIM(p_verify)))
      then
         lv_actionVerifyOut := 'Y';
      else
         do_pin := 'Y';
         
         -- Update action as executed
         gp_gpbeltr.P_Update (p_ctyp_exe_date => SYSDATE, p_user_id => USER, p_rowid => lv_rowid);

         -- Update e-mail address as verified
         gp_gpbprxy.P_Update (p_proxy_idm        => lv_GPBELTR_rec.R_PROXY_IDM,
                              p_email_ver_date   => SYSDATE,
                              p_user_id          => USER);
          
         CASE lv_GPBELTR_rec.R_CTYP_CODE
            WHEN 'NEW_EMAIL'
            THEN
               do_pin := 'N';
               lv_loginOut := 'Y';
               msg := 'emailChanged';
               --P_PA_SaveEmail (lv_GPBELTR_rec.R_PROXY_IDM,
               --                lv_GPBELTR_rec.R_PROXY_OLD_DATA,
               --                lv_GPBELTR_rec.R_PROXY_NEW_DATA);
               -- Set email address to updated value
               gp_gpbprxy.P_Update (p_proxy_idm          => lv_GPBELTR_rec.R_PROXY_IDM,
                                    p_pin_disabled_ind   => 'N',
                                    p_email_address      => lv_GPBELTR_rec.R_PROXY_NEW_DATA,
                                    p_user_id            => USER);
               bwgkprxy.P_MatchLoad (lv_GPBELTR_rec.R_PROXY_IDM);
               gb_common.P_Commit;
            WHEN 'CANCEL_EMAIL'
            THEN
               do_pin := 'N';
               lv_loginOut := 'Y';
               msg := 'emailCanceled';
               --P_PA_CancelEmail (lv_GPBELTR_rec.R_PROXY_IDM,
               --                  lv_GPBELTR_rec.R_PROXY_OLD_DATA,
               --                  lv_GPBELTR_rec.R_PROXY_NEW_DATA);
               -- Verify that previous e-mail address doesn't exist
               lv_GPBPRXY_ref := gp_gpbprxy.F_Query_One_By_Email (lv_GPBELTR_rec.R_PROXY_OLD_DATA);

               FETCH lv_GPBPRXY_ref INTO lv_GPBPRXY_rec;

               IF lv_GPBPRXY_ref%FOUND
               THEN
                  --lv_info := 'EMAIL_DUPLICATE';
                  msg := 'emailDuplicate';
               ELSE
                  -- Reset email address back to previous value and enable account
                  gp_gpbprxy.P_Update (p_proxy_idm          => lv_GPBELTR_rec.R_PROXY_IDM,
                                       p_pin_disabled_ind   => 'N',
                                       p_email_address      => lv_GPBELTR_rec.R_PROXY_OLD_DATA,
                                       p_user_id            => USER);

                  -- Invalidate any outstanding actions related to changing e-mail address
                  -- The cancel e-mail action can override a new e-mail action
                  UPDATE GPBELTR
                     SET GPBELTR_CTYP_EXE_DATE = SYSDATE,
                         GPBELTR_ACTIVITY_DATE = SYSDATE
                   WHERE     GPBELTR_CTYP_CODE = 'NEW_EMAIL'
                         AND GPBELTR_CTYP_EXE_DATE IS NULL
                         AND GPBELTR_PROXY_IDM = lv_GPBELTR_rec.R_PROXY_IDM;

                  gb_common.P_Commit;
               END IF;

               CLOSE lv_GPBPRXY_ref;
         END CASE;
      end if;
   END IF;

  ?  := lv_gidm;
  ?  := lv_actionVerifyOut;
  ?  := do_pin;
  ?  := msg;
  ?  := lv_loginOut;

EXCEPTION
WHEN OTHERS 
THEN 
   ? := 'Y';

END ;
    """

    public final static String SET_PROXY_VERIFY = """
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
              lv_rowid := twbkbssf.f_decode_base64(?);

              lv_GPBELTR_ref := gp_gpbeltr.F_Query_By_Rowid(lv_rowid);

               FETCH lv_GPBELTR_ref INTO lv_GPBELTR_rec;

            IF lv_GPBELTR_ref%NOTFOUND THEN

                ?  := 'Y';
                msg := 'token-error';

            ELSIF (lv_GPBELTR_ref%FOUND AND F_ActionVerify (lv_GPBELTR_rec.R_PROXY_IDM,
                            lv_GPBELTR_rec.R_CTYP_CODE,
                            TRIM(?))) THEN

             ? := lv_GPBELTR_rec.R_PROXY_IDM;

             ?  := 'Y';

          ELSE
             do_pin := 'Y';
          END IF;

          ?  := do_pin;
          ?  := msg;

       EXCEPTION
         WHEN OTHERS THEN ? := 'Y';

        END ;
    """

    public final static String SAVE_PIN = """
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

      IF F_Validate_Credentials (?, TRIM(?), ? ) = 'N' THEN
         lv_error := 'ERR_USER';
      ELSIF NVL (?, '1bogus1pin1') <> NVL (?, '2bogus2pin2') THEN
         lv_error := 'ERR_NOMATCH';
      ELSIF NVL(bwgkprxy.F_GetOption ('PIN_VALIDATION_VIA_GUAPPRF'),'Y') = 'Y' THEN
           gb_third_party_access_rules.p_validate_pinrules  (
             p_pidm             => 0,
             p_pin              => ?,
             p_pin_reusechk_ind => 'N',
             error_message      => lv_msg);
           if lv_msg is not null then
             lv_error := 'ERR_GUAPPRF';
           else
           -- The PIN rules do not check for leading spaces so we have to do that here, just in case
             IF TRIM(NVL (?, 'x')) <>  NVL (?, 'x') THEN
               lv_error := 'ERR_GUAPPRF';
               lv_msg   := g\$_NLS.Get('BWGKPXYA1-0050','SQL', 'PIN values may not start with or end with a space');
             END IF;
           end if;
      ELSIF LENGTH (NVL (?, 'x')) < bwgkprxy.F_GetOption ('PIN_LENGTH_MINIMUM') OR
            LENGTH (NVL (?, 'x')) < bwgkprxy.F_GetOption ('PIN_LENGTH_MINIMUM') THEN
              lv_error := 'ERR_TOOSHORT';
              lv_msg := 'Minimum PIN length: ' || NVL (bwgkprxy.F_GetOption ('PIN_LENGTH_MINIMUM'), '6');
      ELSIF TRIM(NVL (?, 'x')) <>  NVL (?, 'x') THEN
              lv_error := 'ERR_GUAPPRF';
              lv_msg   := g\$_NLS.Get('BWGKPXYA1-0051','SQL', 'PIN values may not start with or end with a space');
      END IF;

      IF lv_error is not null then
       -- P_PA_ResetPin (p_proxyIDM, lv_error, replace(lv_msg,'::',' '));
       error_status := 'Y';
      ELSE
       error_status := 'N';
       lv_salt := gspcrpt.F_Get_Salt (LENGTH (?));
       gspcrpt.P_SaltedHash (?, lv_salt, lv_pinhash);

        gp_gpbprxy.P_Update (
           p_proxy_idm          => ?,
           p_pin_disabled_ind   => 'N',
           p_pin_exp_date       => SYSDATE + bwgkprxy.F_GetOption ('PIN_LIFETIME_DAYS'),
           p_pin                => lv_pinhash,
           p_inv_login_cnt      => 0,
           p_salt               => lv_salt,
           p_user_id            => goksels.f_get_ssb_id_context);

        gb_common.P_Commit;

       END IF;

          ?  := lv_error;
          ?  := replace(lv_msg,'::',' ');
          ?  := error_status;

      END;
    """
}