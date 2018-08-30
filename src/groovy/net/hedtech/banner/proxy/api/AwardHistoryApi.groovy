package net.hedtech.banner.proxy.api

class AwardHistoryApi {

    public final static String AWARD_HISTORY = """
    DECLARE
        lv_award_json                      CLOB DEFAULT NULL;
        lv_response_json                   CLOB DEFAULT NULL;
        lv_award_message_json              CLOB DEFAULT NULL;
        lv_award_total_json                CLOB DEFAULT NULL;
        lv_resources_calc_total_json       CLOB DEFAULT NULL;
        lv_aid_year_json                   VARCHAR2 (32000) DEFAULT NULL;
        msg_no_hist                        VARCHAR2(5000);
        msg_no_aid_year                    VARCHAR2(5000);
        lv_response_json_final             CLOB DEFAULT NULL;

 hist_exist VARCHAR2(1);
 info_access      VARCHAR2(1) := 'N';

  rorwebr_rec rorwebr%ROWTYPE ;     

  total_total_amt   NUMBER;                             -- 8180100-5
  total_offer_amt   NUMBER;                             -- 8180100-5
  total_accept_amt  NUMBER;                             -- 8180100-5 previously used for Amount Total, now used for Total Total
  total_decline_amt NUMBER;                             -- 8180100-5
  total_cancel_amt  NUMBER;                             -- 8180100-5
  total_paid_amt    NUMBER; 
  
  aidy             robinst.robinst_aidy_code%TYPE ;     -- 081801-1
  aidy_desc        robinst.robinst_aidy_desc%TYPE ;     -- 081801-1
    
  TYPE robinst_type IS RECORD(
  robinst_aidy_desc       robinst.robinst_aidy_desc%TYPE,
  robinst_aidy_start_date robinst.robinst_aidy_start_date%TYPE,
  rprawrd_aidy_code       rprawrd.rprawrd_aidy_code%TYPE);
    
     TYPE packgroup_type IS RECORD(
    rorstat_info_access_ind rorstat.rorstat_info_access_ind%TYPE);
    
    TYPE award_dtl_type IS RECORD(
    rprawrd_aidy_code       rprawrd.rprawrd_aidy_code%TYPE,
    rprawrd_fund_code       rprawrd.rprawrd_fund_code%TYPE,
    rprawrd_awst_code       rprawrd.rprawrd_awst_code%TYPE,
    rprawrd_accept_amt      rprawrd.rprawrd_accept_amt%TYPE,
    rprawrd_paid_amt        rprawrd.rprawrd_paid_amt%TYPE,
    rprawrd_offer_amt       rprawrd.rprawrd_offer_amt%TYPE,
    rprawrd_decline_amt     rprawrd.rprawrd_decline_amt%TYPE,                              -- 8180100-2
    rprawrd_cancel_amt      rprawrd.rprawrd_cancel_amt%TYPE,                               -- 8180100-2
    robinst_aidy_desc       robinst.robinst_aidy_desc%TYPE,
    rfrbase_fund_title      rfrbase.rfrbase_fund_title%TYPE,
    rtvawst_desc            rtvawst.rtvawst_desc%TYPE,
    rprawrd_info_access_ind rprawrd.rprawrd_info_access_ind%TYPE,
    rtvawst_info_access_ind rtvawst.rtvawst_info_access_ind%TYPE);
    
  award_dtl_rec    award_dtl_type;
  stvterm_rec      stvterm%ROWTYPE;
  robinst_rec      robinst_type;
  pidm             NUMBER := 200;
  packgroup_rec    packgroup_type;
  
  m_resource_max       CONSTANT NUMBER := 999999999.99; -- 081200-2
  m_total_resource_max CONSTANT NUMBER := 999999999999.99; -- 081200-2
    
    CURSOR GetActiveAidyearC(pidm NUMBER) RETURN robinst_type IS
    SELECT DISTINCT robinst_aidy_desc,
                    robinst_aidy_start_date,
                    rprawrd_aidy_code
    FROM   robinst,
           rprawrd
    WHERE  robinst_info_access_ind = 'Y'
    AND    rprawrd_pidm = pidm
    AND    rprawrd_aidy_code = robinst_aidy_code
    ORDER  BY robinst_aidy_start_date DESC;
    
    CURSOR GetAwdDtlC(pidm NUMBER,
                    aidy VARCHAR2) RETURN award_dtl_type IS
    SELECT rprawrd_aidy_code,
           rprawrd_fund_code,
           rprawrd_awst_code,
           rprawrd_accept_amt,
           rprawrd_paid_amt,
           rprawrd_offer_amt,
           rprawrd_decline_amt,                     -- 8180100-5
           rprawrd_cancel_amt,                      -- 8180100-5
           robinst_aidy_desc,
           rfrbase_fund_title,
           rtvawst_desc,
           NVL(rprawrd_info_access_ind, 'Y'),
           rtvawst_info_access_ind
    FROM   rprawrd,
           robinst,
           rfrbase,
           rtvawst,
           rorwebr
    WHERE  rfrbase_info_access_ind = 'Y'
    AND    rprawrd_fund_code = rfrbase_fund_code
    AND    rprawrd_awst_code = rtvawst_code
    AND    rprawrd_aidy_code = robinst_aidy_code
    AND    rorwebr_aidy_code = robinst_aidy_code
    AND    rprawrd_aidy_code = aidy
    AND    rprawrd_pidm = pidm
    AND    NVL(rprawrd_info_access_ind, 'Y') = 'Y'
    AND    ((rorwebr_fund_zero_amt_ind = 'N' AND rprawrd_offer_amt > 0) OR
             rorwebr_fund_zero_amt_ind = 'Y')
    AND    rtvawst_info_access_ind = 'Y'
    ORDER  BY rfrbase_print_seq_no, -- 081801-3
              rprawrd_fund_code;
    
    CURSOR GetPackGroupC(pidm NUMBER,
                       aidy VARCHAR2) RETURN packgroup_type IS
    SELECT NVL(rorstat_info_access_ind, rorwebr_null_infoaccess_ind)
    FROM   rorstat,
           rorwebr
    WHERE  rorstat_pidm = pidm
    AND    rorstat_aidy_code = aidy
    AND    rorstat_aidy_code = rorwebr_aidy_code
    AND    NVL(rorstat_info_access_ind, rorwebr_null_infoaccess_ind) = 'Y'
    AND    ((rorstat_pgrp_code IS NOT NULL AND
          rorstat_pgrp_code IN
          (SELECT rtvpgrp_code FROM rtvpgrp WHERE rtvpgrp_info_access_ind = 'Y')) OR
          rorstat_pgrp_code IS NULL);
          
FUNCTION f_getinfoaccess(pidm NUMBER,
                           aidy robinst.robinst_aidy_code%TYPE) RETURN VARCHAR2 IS

    lv_info_access VARCHAR2(1) := 'N';

    CURSOR getinfoaccess_c IS
      SELECT NVL(rorstat_info_access_ind, rorwebr_null_infoaccess_ind)
      FROM   rorstat,
             rorwebr
      WHERE  rorstat_pidm = pidm
      AND    rorstat_aidy_code = aidy
      AND    rorstat_aidy_code = rorwebr_aidy_code;

  BEGIN
    OPEN getinfoaccess_c;
    FETCH getinfoaccess_c
      INTO lv_info_access;
    CLOSE getinfoaccess_c;

    RETURN lv_info_access;
  END f_getinfoaccess;
  
  FUNCTION F_CheckMaxAmount(amt NUMBER) RETURN VARCHAR2 IS
  BEGIN
    RETURN to_char(LEAST(NVL(amt, 0), m_resource_max), 'L999G999G999D99');
  END F_CheckMaxAmount;


  FUNCTION F_CheckTotalMaxAmount(amt NUMBER) RETURN VARCHAR2 IS
  BEGIN
    RETURN to_char(LEAST(NVL(amt, 0), m_total_resource_max), 'L999G999G999G999D99');
  END F_CheckTotalMaxAmount;
  
    FUNCTION f_gettermdesc(term stvterm.stvterm_code%TYPE) RETURN VARCHAR2 IS
  BEGIN
    OPEN stkterm.stvtermc(term);
    FETCH stkterm.stvtermc
      INTO stvterm_rec;
    IF stkterm.stvtermc%FOUND THEN
      CLOSE stkterm.stvtermc;
      RETURN stvterm_rec.stvterm_desc;
    ELSE
      CLOSE stkterm.stvtermc;
      RETURN(g\$_nls.get('BWRKRHS1-0099', 'SQL', 'Not Applicable'));
    END IF;
  END f_gettermdesc;

    
  FUNCTION GET_RESOURCES(pidm NUMBER,
                       aidy robinst.robinst_aidy_code%TYPE) RETURN VARCHAR2 IS


    lv_resource_json                      VARCHAR2 (32000) DEFAULT NULL;
    LV_RESOURCES_TOTAL_JSON               VARCHAR2 (32000) DEFAULT NULL;

    total_est_amt    rnkneed.total_amount_type := 0; -- 081200-2
    total_actual_amt rnkneed.total_amount_type := 0; -- 081200-2
    total_cal_amt    rnkneed.total_amount_type := 0; -- 081200-5
    resource_exists  BOOLEAN := FALSE;
    
    TYPE resources_rec IS RECORD(
    resource_desc rprarsc.rprarsc_resource_desc%TYPE,
    term_code     rprarsc.rprarsc_term_code%TYPE,
    est_amt       rprarsc.rprarsc_est_amt%TYPE,
    actual_amt    rprarsc.rprarsc_actual_amt%TYPE);

    resources_r resources_rec; -- 081000-1
    arsc_c      rokrefc.arsc_cur;
    arsc_r      rokrefc.arsc_rec;
    --pidm NUMBER := 30196;
    --aidy robinst.robinst_aidy_code%TYPE := '1314';
    
    
     CURSOR resources_c(pidm NUMBER,
                     aidy robinst.robinst_aidy_code%TYPE) IS
    SELECT rprarsc_resource_desc,
           rprarsc_term_code,
           rprarsc_est_amt, -- 081200-5
           rprarsc_actual_amt -- 081200-5
    FROM   rprarsc
    WHERE  rprarsc_pidm = pidm
    AND    rprarsc_info_access_ind = 'Y'
    AND    ((rprarsc_arsc_code IS NOT NULL AND
          rprarsc_info_access_ind =
          (SELECT rtvarsc_info_access_ind
              FROM   rtvarsc
              WHERE  rtvarsc_code = rprarsc_arsc_code
              AND    rtvarsc_info_access_ind = 'Y')) OR rprarsc_arsc_code IS NULL)
    AND    ((rprarsc_aidy_code = aidy AND rprarsc_term_code IS NULL) OR
          (rprarsc_term_code IS NOT NULL AND
          rprarsc_term_code IN
          (SELECT rorprds_term_code
              FROM   rorprds,
                     rortprd,
                     rorstat
              WHERE  rorstat_pidm = pidm
              AND    rorstat_aidy_code = aidy
              AND    rorprds_period = rortprd_period
              AND    rortprd_aidy_code = aidy
              AND    rortprd_aprd_code = rorstat_aprd_code)));


  BEGIN
    -- Other resources                                                                   -- 081000-1
    OPEN resources_c(pidm, aidy);
    FETCH resources_c
      INTO resources_r;

    IF resources_c%FOUND THEN
      resource_exists := TRUE;
      
      lv_resource_json := '"resources": [';

      WHILE resources_c%FOUND LOOP

       
        lv_resource_json := lv_resource_json || '{' ||
                      '"resource_desc" ' || ':' || '"' || resources_r.resource_desc || '"' ||
                      ',"term_code" ' || ':'  || '"' || f_gettermdesc(resources_r.term_code) || '"' ||
                      ',"est_amt" ' || ':'  || '"' || F_CheckMaxAmount(resources_r.est_amt) || '"' ||
                      ',"actual_amt" ' || ':'  || '"' || F_CheckMaxAmount(resources_r.actual_amt) || '"' || '},'; 
                      

        total_est_amt    := total_est_amt + NVL(resources_r.est_amt, 0); -- 081200-2
        total_actual_amt := total_actual_amt + NVL(resources_r.actual_amt, 0); -- 081200-2
        total_cal_amt    := total_cal_amt +
                            NVL(NVL(resources_r.actual_amt, resources_r.est_amt), 0); -- 081200-5
                            

        FETCH resources_c
          INTO resources_r;
      END LOOP;
      
       lv_resource_json := TRIM(TRAILING ',' FROM lv_resource_json ) ; --|| ']';
      
       --dbms_output.put_line(lv_resource_json);

    END IF;
    CLOSE resources_c;
 
    -- Totals
    IF resource_exists THEN
    
    lv_resources_total_json := ', {';
        
        lv_resources_total_json := lv_resources_total_json || '"resource_desc" ' || ':'  || '"RESOURCE_TOTAL"';
        lv_resources_total_json := lv_resources_total_json || ',"term_code" ' || ':'  || '""';
        lv_resources_total_json := lv_resources_total_json || ',"est_amt" ' || ':'  || '"' ||  TRIM(to_char(total_est_amt, 'L999G999G999D99')) || '"';
        lv_resources_total_json := lv_resources_total_json || ',"actual_amt" ' || ':'  || '"' || TRIM(to_char(total_actual_amt, 'L999G999G999D99')) || '"' || '}';
        
        
        --lv_resources_total_json := lv_resource_json || lv_resources_total_json || '}]';
        
        IF total_est_amt <> 0 THEN
          lv_resources_calc_total_json := ', {';
          lv_resources_total_json := lv_resources_total_json || lv_resources_calc_total_json || '"resource_desc" ' || ':'  || '"CALCULATED_RESOURCE"';
          lv_resources_total_json := lv_resources_total_json || ',"term_code" ' || ':'  || '"'||'Total '|| TRIM(to_char(total_cal_amt, 'L999G999G999D99')) || '"';
          lv_resources_total_json := lv_resources_total_json || ',"est_amt" ' || ':'  || '""';
          lv_resources_total_json := lv_resources_total_json || ',"actual_amt" ' || ':'  || '""' || '}';
        END IF;
        
        lv_resources_total_json := lv_resource_json || lv_resources_total_json || ']';
        
        --dbms_output.put_line(lv_resources_total_json);

    END IF;
 
 
    if resource_exists then
      return ','|| lv_resources_total_json;
    else 
      return ', "resources":[]';
    end if;
    
  END GET_RESOURCES; 
          
  PROCEDURE P_Sel_rorwebr(aidy robinst.robinst_aidy_code%TYPE) IS

    CURSOR get_rorwebr_c( p_aidy_code rorwebr.rorwebr_aidy_code%TYPE ) IS
      SELECT *
      FROM   rorwebr
      WHERE  rorwebr_aidy_code = p_aidy_code ;

  BEGIN
    OPEN get_rorwebr_c( p_aidy_code => aidy ) ;
    FETCH get_rorwebr_c INTO rorwebr_rec ;
    CLOSE get_rorwebr_c ;

    info_access := f_getinfoaccess(pidm, aidy);
  END P_Sel_rorwebr;
  
  BEGIN

pidm  := ?;
--pidm := 200;
  
    hist_exist := 'N';

    --
    <<outer_loop>>
    OPEN getactiveaidyearc(pidm);
    FETCH getactiveaidyearc
      INTO robinst_rec.robinst_aidy_desc,
           robinst_rec.robinst_aidy_start_date,
           aidy;
    IF getactiveaidyearc%NOTFOUND THEN
      aidy := NULL;
    END IF;

    IF aidy IS NULL THEN
      msg_no_aid_year := g\$_nls.get('BWRKRHS1-0001',
                        'SQL',
                        'No award history has been recorded.%01%',
                        htf.br);
      msg_no_aid_year := 'NO_AID_YEAR';
      
      GOTO end_outer_loop;
    END IF;
    --
    P_Sel_rorwebr(aidy);

    WHILE getactiveaidyearc%FOUND LOOP
    
      <<inner_loop>>
      total_accept_amt  := 0;
      total_paid_amt    := 0;
      total_offer_amt   := 0;                          -- 8180100-5
      total_decline_amt := 0;                          -- 8180100-5
      total_cancel_amt  := 0;                          -- 8180100-5
      total_total_amt   := 0;                          -- 8180100-5

      --bwrkolib.P_ProcessUserDefTxt(pidm, aidy, 'AH');  -- 081801-2
      --
      OPEN GetPackGroupC(pidm, aidy);
      FETCH GetPackGroupC
        INTO packgroup_rec;
      IF GetPackGroupC%FOUND THEN
        hist_exist := 'Y';
        
        --dbms_output.put_line('HISTORY:' || hist_exist);
      END IF;

      IF GetPackGroupC%NOTFOUND THEN
        GOTO end_inner_loop;
      END IF;
      --
      
      -- 080500-9
      OPEN GetAwdDtlC(pidm, aidy);
      FETCH GetAwdDtlC
        INTO award_dtl_rec;
      IF GetAwdDtlC%FOUND THEN
  
          null;
          
          lv_award_json := '"rows": [';

     WHILE GetAwdDtlC%FOUND LOOP
        
                 lv_award_json := lv_award_json || '{' ||
                      '"fund_title" ' || ':' || '"' || award_dtl_rec.rfrbase_fund_title || '"' ||
                      ',"offer_amt" ' || ':'  || '"' || TRIM(to_char(award_dtl_rec.rprawrd_offer_amt, 'L999G999G999D99')) || '"' ||
                      ',"accept_amt" ' || ':'  || '"' || TRIM(to_char(award_dtl_rec.rprawrd_offer_amt, 'L999G999G999D99')) || '"' ||
                      ',"decline_amt" ' || ':'  || '"' || TRIM(to_char(award_dtl_rec.rprawrd_decline_amt, 'L999G999G999D99')) || '"' ||
                      ',"cancel_amt" ' || ':'  || '"' || TRIM(to_char(award_dtl_rec.rprawrd_cancel_amt, 'L999G999G999D99')) || '"' ;
                      
                      --||
                      -- '},';
                             
                      total_offer_amt := total_offer_amt + NVL(award_dtl_rec.rprawrd_offer_amt,0); 
                      total_accept_amt := total_accept_amt + NVL(award_dtl_rec.rprawrd_accept_amt,0); 
                      total_decline_amt := total_decline_amt + NVL(award_dtl_rec.rprawrd_decline_amt,0);
                      total_cancel_amt := total_cancel_amt + NVL(award_dtl_rec.rprawrd_cancel_amt,0); 
                   
                   
                    IF award_dtl_rec.rprawrd_offer_amt IS NOT NULL THEN   
                      -- 8180100-5
                      lv_award_json := lv_award_json || ',"total_amt" ' || ':'  || '"' || TRIM(to_char(award_dtl_rec.rprawrd_offer_amt, 'L999G999G999D99')) || '"';
                        
                        total_total_amt := total_total_amt + award_dtl_rec.rprawrd_offer_amt;                        -- 8180100-5
                     ELSE
                       lv_award_json := lv_award_json || ',"total_amt" ' || ':'  || '"' || TRIM(to_char(0, 'L0D99')) || '"';
                    END IF;
                    
                    
                    IF award_dtl_rec.rprawrd_paid_amt IS NOT NULL THEN   
                      -- 8180100-5
                      lv_award_json := lv_award_json || ',"paid_amt" ' || ':'  || '"' || TRIM(to_char(award_dtl_rec.rprawrd_paid_amt, 'L999G999G999D99')) || '"';
                        
                        total_paid_amt := total_paid_amt + award_dtl_rec.rprawrd_paid_amt;                        -- 8180100-5
                     ELSE
                       lv_award_json := lv_award_json || ',"paid_amt" ' || ':'  || '"' || TRIM(to_char(0, 'L0D99')) || '"';
                    END IF;
                    
                    
                    lv_award_json := lv_award_json || '},';
          
     
           FETCH GetAwdDtlC
            INTO award_dtl_rec;
        END LOOP;
        
        lv_award_json := TRIM(TRAILING ',' FROM lv_award_json );
--
        -- calculate total ammout to display
        
        lv_award_total_json := ', {';
        
        lv_award_total_json := lv_award_total_json || ',"fund_title" ' || ':'  || '"AWARD_TOTAL"';
        
        IF total_offer_amt != 0 THEN 
          lv_award_total_json := lv_award_total_json || ',"offer_amt" ' || ':'  || '"' || TRIM(to_char(total_offer_amt, 'L999G999G999D99')) || '"';
        ELSE 
          lv_award_total_json := lv_award_total_json || ',"offer_amt" ' || ':'  || '"' || TRIM(to_char(0, 'L0D99')) || '"';
        END IF;

        IF total_accept_amt != 0 THEN
            lv_award_total_json := lv_award_total_json || ',"accept_amt" ' || ':'  || '"' || TRIM(to_char(total_accept_amt, 'L999G999G999D99')) || '"';
        ELSE
            lv_award_total_json := lv_award_total_json || ',"accept_amt" ' || ':'  || '"' || TRIM(to_char(0, 'L0D99')) || '"';
        END IF;

        IF total_decline_amt != 0 THEN
           lv_award_total_json := lv_award_total_json || ',"decline_amt" ' || ':'  || '"' || TRIM(to_char(total_decline_amt, 'L999G999G999D99')) || '"';
        ELSE 
          lv_award_total_json := lv_award_total_json || ',"decline_amt" ' || ':'  || '"' || TRIM(to_char(0, 'L0D99')) || '"';
        END IF;

        IF total_cancel_amt != 0 THEN
          lv_award_total_json := lv_award_total_json || ',"cancel_amt" ' || ':'  || '"' || TRIM(to_char(total_cancel_amt, 'L999G999G999D99')) || '"';
        ELSE
          lv_award_total_json := lv_award_total_json || ',"cancel_amt" ' || ':'  || '"' || TRIM(to_char(0, 'L0D99')) || '"';
        END IF; 

        IF total_total_amt != 0 THEN 
          lv_award_total_json := lv_award_total_json || ',"total_amt" ' || ':'  || '"' || TRIM(to_char(total_total_amt, 'L999G999G999D99')) || '"';
        ELSE
          lv_award_total_json := lv_award_total_json || ',"total_amt" ' || ':'  || '"' || TRIM(to_char(0, 'L0D99')) || '"';
        END IF;

        IF total_paid_amt != 0 THEN
        lv_award_total_json := lv_award_total_json || ',"paid_amt" ' || ':'  || '"' || TRIM(to_char(total_paid_amt, 'L999G999G999D99')) || '"';
        ELSE
          lv_award_total_json := lv_award_total_json || ',"paid_amt" ' || ':'  || '"' || TRIM(to_char(0, 'L0D99')) || '"';
        END IF;
        
        lv_award_total_json :=  lv_award_total_json || '}';
        
        lv_award_json  := lv_award_json || lv_award_total_json;
        -- end calculate total ammout to display
        
        lv_award_json := lv_award_json || ']';
        
        --dbms_output.put_line('DATA:' ||lv_award_json);

       
      END IF;
      CLOSE GetAwdDtlC;
 
      -- 80200-1
     -- P_ShowResource(pidm, aidy);

      --
   
      <<end_inner_loop>>
      FETCH getactiveaidyearc
        INTO robinst_rec.robinst_aidy_desc,
             robinst_rec.robinst_aidy_start_date,
             aidy;
      --
      CLOSE GetPackGroupC;
      
      -- add data
      
       lv_aid_year_json  := '{"aidYear":' || '"' || robinst_rec.robinst_aidy_desc ||'"' || ',';
       lv_response_json := lv_aid_year_json || '"data": {';
       lv_response_json := lv_response_json  || nvl(lv_award_json,'"rows": []') || '}' || GET_RESOURCES(pidm, aidy) || '}';
       
      
      --dbms_output.put_line ('RESPONSE: ' || lv_response_json );
      -- end data
    END LOOP;
    CLOSE getactiveaidyearc;
    
    --lv_response_json := 
    
    <<end_outer_loop>>
    --
    IF hist_exist = 'N' THEN
      msg_no_hist := g\$_nls.get('BWRKRHS1-0013',
                        'SQL',
                        'No award history is available for you at this time, please contact your financial aid office  if you have questions.');
                            
    msg_no_hist :=  'NO_AWARD_HISTORY';
    
    END IF;
    
    IF (msg_no_hist IS NOT NULL OR msg_no_aid_year IS NOT NULL) THEN
        lv_award_message_json := '"messages": [';
        
        IF (msg_no_hist IS NOT NULL OR msg_no_aid_year IS NULL) THEN                    
          lv_award_message_json := lv_award_message_json || '"' || msg_no_hist || '"' || ']';         
        ELSIF (msg_no_hist IS NULL OR msg_no_aid_year IS NULL) THEN
           lv_award_message_json := lv_award_message_json || '"' || msg_no_aid_year || '"' || ']';
        ELSE
          lv_award_message_json := lv_award_message_json || '{' || msg_no_hist|| '}';
          lv_award_message_json := lv_award_message_json || ', {' || msg_no_aid_year|| '}]';
        END IF;
               
    END IF;
    
    -- build final json load
     -- lv_response_json := '{"data": {';
     -- lv_response_json := lv_response_json  || nvl(lv_award_json,'"rows": []') || ', ' || nvl(lv_award_message_json,'"messages": []') || '}}';
     
     --lv_response_json := lv_response_json  || nvl(lv_award_json,'"rows": []') || '}}';
      
      --dbms_output.put_line ('RESPONSE: ' || lv_response_json );
      
      -- new final json
      
       lv_response_json_final := '{"awards": [';
       --lv_response_json_final := lv_response_json_final || lv_response_json || ']}'; -- GOOD WO messages
       
       lv_response_json_final := lv_response_json_final || lv_response_json || ']';
       -- add messages
       lv_response_json_final := lv_response_json_final ||  ' , ' || nvl(lv_award_message_json,'"messages": []') || '}';
      
       --? := lv_response_json;
       
       ? := lv_response_json_final;

  END;
"""
}
