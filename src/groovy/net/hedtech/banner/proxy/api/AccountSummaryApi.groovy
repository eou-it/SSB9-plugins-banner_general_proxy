/********************************************************************************
  Copyright 2018 Ellucian Company L.P. and its affiliates.
********************************************************************************/
package net.hedtech.banner.proxy.api

class AccountSummaryApi {

    public final static String ACCOUNT_SUMMARY = """
declare

   CURSOR tbraccd_totalc (pidm spriden.spriden_pidm%TYPE)
   IS
      SELECT SUM (tbraccd_balance) acct_total
        FROM tbbdetc, tbraccd
       WHERE tbraccd_pidm = pidm
         AND tbbdetc_detail_code = tbraccd_detail_code;
         
   CURSOR tbraccd_viewc (pidm spriden.spriden_pidm%TYPE)
   IS
      SELECT stvterm_desc, tbraccd_term_code, tbraccd_detail_code,
             tbbdetc_desc,
             tbbdetc_type_ind, SUM (tbraccd_amount) amount,
             SUM (tbraccd_balance) balance
        FROM stvterm, tbbdetc, tbraccd
       WHERE tbraccd_pidm = pidm
         AND tbbdetc_detail_code = tbraccd_detail_code
         AND stvterm_code (+) = tbraccd_term_code
       GROUP BY stvterm_desc,
                tbraccd_term_code,
                tbbdetc_type_ind,
                tbraccd_detail_code,
                tbbdetc_desc
       ORDER BY tbraccd_term_code DESC,
                tbbdetc_type_ind,
                tbraccd_detail_code,
                tbbdetc_desc;

   TYPE tbraccd_viewc_type IS RECORD(

      stvterm_desc                  stvterm.stvterm_desc%TYPE,
      tbraccd_term_code             tbraccd.tbraccd_term_code%TYPE,
      tbraccd_detail_code           tbraccd.tbraccd_detail_code%TYPE,
      tbbdetc_desc                  tbbdetc.tbbdetc_desc%TYPE,
      tbbdetc_type_ind              tbbdetc.tbbdetc_type_ind%TYPE,
      amount                        NUMBER (14, 2),
      balance                       NUMBER (14, 2));

   tbraccd_viewc_rec    tbraccd_viewc_type;         -- Account Summary Row.

   TYPE tbraccd_totalc_type IS RECORD(
      acct_total                    NUMBER (14, 2));

   tbraccd_totalc_rec   tbraccd_totalc_type;          -- Account Total Row.
   term_chrg            NUMBER (14, 2)                     := 0;
   term_pay             NUMBER (14, 2)                     := 0;
   term_bal             NUMBER (14, 2)                     := 0;
   total_bal            NUMBER (14, 2)                     := 0;
   old_term             VARCHAR2 (6)                       := ' ';
   term_chrg_var        NUMBER (14, 2)                     := 0;
   term_pay_var         NUMBER (14, 2)                     := 0;
   gtv_ext_code         GTVSDAX.GTVSDAX_EXTERNAL_CODE%TYPE := 'N';
   colspan_var          NUMBER(1);
   tbbterm_rec          tbbterm%ROWTYPE  ;
   
   global_pidm  spriden.spriden_pidm%TYPE := ?;
   lv_accSummJson CLOB := '{}';
BEGIN

-- The NUMBER will be retrieved as 100.99
-- The banner_general_proxy will handle the actual number format for NLS_TERRITORY
   dbms_session.set_nls('NLS_TERRITORY',''''||'AMERICA'||'''');
--

   OPEN tbraccd_viewc (global_pidm);

   LOOP
      FETCH tbraccd_viewc INTO tbraccd_viewc_rec;

      IF tbraccd_viewc%FOUND
      THEN
--
-- Show the account balance (all terms) at the top.
-- =========================================================
         IF tbraccd_viewc%rowcount = 1
         THEN
            OPEN tbraccd_totalc (global_pidm);
            FETCH tbraccd_totalc INTO tbraccd_totalc_rec;
            lv_accSummJson := '{"accountBal":' || nvl(tbraccd_totalc_rec.acct_total, 0) || ',';
            lv_accSummJson := lv_accSummJson || '"terms":['; --for term
         END IF;

--
-- As we loop through accounting records, look for break on term.
-- =======================================================
         IF old_term <> tbraccd_viewc_rec.tbraccd_term_code
         THEN
--
-- Print previous term subtotals.
-- =======================================================
            IF tbraccd_viewc%rowcount <> 1
            THEN
               lv_accSummJson := lv_accSummJson || '],'; --for ledger
               lv_accSummJson := lv_accSummJson || '"termCharge":' || term_chrg || ',';
               lv_accSummJson := lv_accSummJson || '"termPay":' || term_pay || ',';
               lv_accSummJson := lv_accSummJson || '"termBalance":' || term_bal;
               lv_accSummJson := lv_accSummJson || '},'; --for term
            END IF;

--
-- Reinitialize term specific variables.
-- =======================================================
            old_term := tbraccd_viewc_rec.tbraccd_term_code;
            term_chrg := 0;
            term_pay := 0;
            term_bal := 0;
--
-- Print the next term as a header.
-- =======================================================
            lv_accSummJson := lv_accSummJson || '{'; --for term
            
            IF tbraccd_viewc_rec.tbraccd_term_code = 'ARTERM'
            THEN
               lv_accSummJson := lv_accSummJson || '"termCode":"' || tbraccd_viewc_rec.tbraccd_term_code || '",';
            ELSE
               lv_accSummJson := lv_accSummJson || '"termDesc":"' || tbraccd_viewc_rec.stvterm_desc || '",';
            END IF;

--
-- Print column headings.

            lv_accSummJson := lv_accSummJson || '"ledger":['; --for ledger
         ELSE
            lv_accSummJson := lv_accSummJson || ','; --for ledger
         END IF;

-- Print detail records. Add up totals as we go.
-- =======================================================
         term_chrg_var := '';
         term_pay_var := '';

--
-- =======================================================
         IF tbraccd_viewc_rec.tbbdetc_type_ind = 'C'
         THEN
            term_chrg_var := tbraccd_viewc_rec.amount;
         ELSE
            term_pay_var := tbraccd_viewc_rec.amount;
         END IF;

--
-- Add to term subtotals.
-- =======================================================
         IF tbraccd_viewc_rec.tbbdetc_type_ind = 'C'
         THEN
            term_chrg := term_chrg + tbraccd_viewc_rec.amount;
         ELSE
            term_pay := term_pay + tbraccd_viewc_rec.amount;
         END IF;

         term_bal := term_bal + tbraccd_viewc_rec.balance;
         total_bal := total_bal + tbraccd_viewc_rec.balance;

         lv_accSummJson := lv_accSummJson || '{"detailCode":"' || tbraccd_viewc_rec.tbraccd_detail_code || '",';

         lv_accSummJson := lv_accSummJson || '"description":"' || tbraccd_viewc_rec.tbbdetc_desc || '",';

         lv_accSummJson := lv_accSummJson || '"charge":' || nvl(term_chrg_var, 0) || ',';

         lv_accSummJson := lv_accSummJson || '"payment":' || nvl(term_pay_var, 0) || ',';

         lv_accSummJson := lv_accSummJson || '"balance":' || nvl(tbraccd_viewc_rec.balance, 0) || '}';
      ELSE
         IF tbraccd_viewc%rowcount = 0
         THEN
            EXIT;
         ELSE
            lv_accSummJson := lv_accSummJson || '],'; --for ledger
            lv_accSummJson := lv_accSummJson || '"termCharge":' || term_chrg || ',';
            lv_accSummJson := lv_accSummJson || '"termPay":' || term_pay || ',';
            lv_accSummJson := lv_accSummJson || '"termBalance":' || term_bal || '}],'; --for term
            lv_accSummJson := lv_accSummJson || '"acctTotal":' || total_bal || '}';

            EXIT;
         END IF;
      END IF;
   END LOOP;

   ? := lv_accSummJson;
end;
"""
}