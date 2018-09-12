package net.hedtech.banner.proxy.api

class AccountSummaryApi {

    public final static String ACCOUNT_SUMMARY = """
/* Fully define subprograms specified in package */
/* View Account Summary form */
--PROCEDURE p_viewacct
--IS
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
--
-- Validate the user.
-- =========================================================
   --IF NOT twbkwbis.f_validuser (global_pidm)
   --THEN
   --   RETURN;
   --END IF;

   --OPEN gtvsdax_extc ('WEBDETCODE', 'WEBACCTSUM');
   --FETCH gtvsdax_extc INTO gtv_ext_code;
   --CLOSE gtvsdax_extc;

   --IF gtv_ext_code = 'Y'
   --THEN
   --  colspan_var := 1;
   --END IF;
--
-- Start the web page. Print titles and infotext.
-- =========================================================
   --bwckfrmt.p_open_doc ('bwskoacc.P_ViewAcct');
   --twbkfrmt.p_paragraph (1);
   --twbkwbis.p_dispinfo ('bwskoacc.P_ViewAcct', 'DEFAULT');
--
-- Loop through accounting records for the student.
-- =========================================================
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
            --twbkfrmt.p_tableopen (
            --   'DATADISPLAY',
            --   cattributes   => 'summary="' ||
            --                    G\$_NLS.Get ('BWSKOAC1-0015',
            --                       'SQL',
            --                       'This table displays summarized charge and payment transactions by term on your academic record.') || '"',
            --   ccaption      => G\$_NLS.Get ('BWSKOAC1-0016', 'SQL', 'Summary')
            --    );
            OPEN tbraccd_totalc (global_pidm);
            FETCH tbraccd_totalc INTO tbraccd_totalc_rec;
            --twbkfrmt.p_tablerowopen ();

            --twbkfrmt.p_tabledatalabel (
            --   G\$_NLS.Get ('BWSKOAC1-0017', 'SQL', 'Account Balance:'),
            --   ccolspan   => 3 + colspan_var
            --);
            --twbkfrmt.p_tabledata (
            --   TO_CHAR (
            --      tbraccd_totalc_rec.acct_total,
            --      'L999G999G999G990D99'
            --   ),
            --   'right'
            --);
            lv_accSummJson := '{"accountBal":' || nvl(tbraccd_totalc_rec.acct_total, 0) || ',';
            lv_accSummJson := lv_accSummJson || '"terms":['; --for term
            --CLOSE tbraccd_totalc;
            --twbkfrmt.p_tablerowclose;
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
               --twbkfrmt.p_tablerowopen ();

               --twbkfrmt.p_tabledatalabel (
               --   G\$_NLS.Get ('BWSKOAC1-0018', 'SQL', 'Term Charges:'),
               --   ccolspan   => 1 + colspan_var
               --);
               --twbkfrmt.p_tabledata (
               --   TO_CHAR (term_chrg, 'L999G999G999G990D99'),
               --   'right'
               --);
               lv_accSummJson := lv_accSummJson || '"termCharge":' || term_chrg || ',';

               --twbkfrmt.p_tablerowclose;

               --twbkfrmt.p_tablerowopen ();
               --twbkfrmt.p_tabledatalabel (
               --   G\$_NLS.Get ('BWSKOAC1-0019',
               --      'SQL',
               --      'Term Credits and Payments:'),
               --   ccolspan   => 2 + colspan_var
               --);
               --twbkfrmt.p_tabledata (
               --   TO_CHAR (term_pay, 'L999G999G999G990D99'),
               --   'right'
               --);
               lv_accSummJson := lv_accSummJson || '"termPay":' || term_pay || ',';

               --twbkfrmt.p_tablerowclose;

               --twbkfrmt.p_tablerowopen ();
               --twbkfrmt.p_tabledatalabel (
               --   G\$_NLS.Get ('BWSKOAC1-0020', 'SQL', 'Term Balance:'),
               --   ccolspan   => 3 + colspan_var
               --);
               --twbkfrmt.p_tabledata (
               --   TO_CHAR (term_bal, 'L999G999G999G990D99'),
               --   'right'
               --);
               lv_accSummJson := lv_accSummJson || '"termBalance":' || term_bal;

               --twbkfrmt.p_tablerowclose;
               --twbkfrmt.p_tablerowopen ();
               --twbkfrmt.p_tabledataseparator (ccolspan => 5);
               --twbkfrmt.p_tablerowclose;
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
            --twbkfrmt.p_tablerowopen ();
            lv_accSummJson := lv_accSummJson || '{'; --for term
            
            IF tbraccd_viewc_rec.tbraccd_term_code = 'ARTERM'
            THEN
               --twbkfrmt.p_tabledatalabel (
               --   G\$_NLS.Get ('BWSKOAC1-0021',
               --      'SQL',
               --      'Items not related to a term'),
               --   ccolspan   => 5
               --);
               lv_accSummJson := lv_accSummJson || '"termCode":"' || tbraccd_viewc_rec.tbraccd_term_code || '",';
            ELSE
               --IF F_ValidTerm(tbraccd_viewc_rec.tbraccd_term_code,tbbterm_rec)
               --THEN
                  --twbkfrmt.p_tabledatalabel (
                  --twbkfrmt.f_printanchor (
                  --twbkfrmt.f_encodeurl ('bwskoacc.P_ViewAcctTerm?term_in='||tbraccd_viewc_rec.tbraccd_term_code),
                  --   tbraccd_viewc_rec.stvterm_desc),
                  --   ccolspan   => 5
                  --);
               --ELSE
                 
                  --twbkfrmt.p_tabledatalabel (
                  --  twbkfrmt.f_printtext (
                  --  tbraccd_viewc_rec.stvterm_desc,
                  --  class_in => 'fieldOrangetextbold'),
                  --  ccolspan   => 5
                  --);
               --END IF;
               lv_accSummJson := lv_accSummJson || '"termDesc":"' || tbraccd_viewc_rec.stvterm_desc || '",';
            END IF;

            --twbkfrmt.p_tablerowclose;
--
-- Print column headings.

            --twbkfrmt.p_tablerowopen ();

            --IF gtv_ext_code = 'Y'
            --THEN
            --   twbkfrmt.p_tabledataheader (
            --      G\$_NLS.Get ('BWSKOAC1-0022', 'SQL', 'Detail Code')
            --   );
            --END IF;

            --twbkfrmt.p_tabledataheader (
            --   G\$_NLS.Get ('BWSKOAC1-0023', 'SQL', 'Description')
            --);
            --twbkfrmt.p_tabledataheader (
            --   G\$_NLS.Get ('BWSKOAC1-0024', 'SQL', 'Charge')
            --);
            --twbkfrmt.p_tabledataheader (
            --   G\$_NLS.Get ('BWSKOAC1-0025', 'SQL', 'Payment')
            --);
            --twbkfrmt.p_tabledataheader (
            --   G\$_NLS.Get ('BWSKOAC1-0026', 'SQL', 'Balance')
            --);
            --twbkfrmt.p_tablerowclose;
            lv_accSummJson := lv_accSummJson || '"ledger":['; --for ledger
         ELSE
            lv_accSummJson := lv_accSummJson || ','; --for ledger
         END IF;

--
-- Print detail records. Add up totals as we go.
-- =======================================================
         term_chrg_var := '';
         term_pay_var := '';

--
-- Huh?
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
         --twbkfrmt.p_tablerowopen ();

         --IF gtv_ext_code = 'Y'
         --THEN
         --   twbkfrmt.p_tabledata (tbraccd_viewc_rec.tbraccd_detail_code);
            lv_accSummJson := lv_accSummJson || '{"detailCode":"' || tbraccd_viewc_rec.tbraccd_detail_code || '",';
         --END IF;

         --twbkfrmt.p_tabledata (tbraccd_viewc_rec.tbbdetc_desc);
         lv_accSummJson := lv_accSummJson || '"description":"' || tbraccd_viewc_rec.tbbdetc_desc || '",';
         --twbkfrmt.p_tabledata (
         --   TO_CHAR (term_chrg_var, 'L999G999G999G990D99'),
         --   'right'
         --);
         lv_accSummJson := lv_accSummJson || '"charge":' || nvl(term_chrg_var, 0) || ',';
         --twbkfrmt.p_tabledata (
         --   TO_CHAR (term_pay_var, 'L999G999G999G990D99'),
         --   'right'
         --);
         lv_accSummJson := lv_accSummJson || '"payment":' || nvl(term_pay_var, 0) || ',';
         --twbkfrmt.p_tabledata (
         --   TO_CHAR (tbraccd_viewc_rec.balance, 'L999G999G999G990D99'),
         --   'right'
         --);
         lv_accSummJson := lv_accSummJson || '"balance":' || nvl(tbraccd_viewc_rec.balance, 0) || '}';
         --twbkfrmt.p_tablerowclose;
      ELSE
         IF tbraccd_viewc%rowcount = 0
         THEN
            --twbkfrmt.p_printmessage (
            --   G\$_NLS.Get ('BWSKOAC1-0027',
            --      'SQL',
            --      'No account detail exists on your record.'),
            --   'WARNING'
            --);
            EXIT;
         ELSE
            lv_accSummJson := lv_accSummJson || '],'; --for ledger
            --twbkfrmt.p_tablerowopen ();

            --twbkfrmt.p_tabledatalabel (
            --   G\$_NLS.Get ('BWSKOAC1-0028', 'SQL', 'Term Charges:'),
            --   ccolspan   => 1 + colspan_var
            --);
            --twbkfrmt.p_tabledata (
            --   TO_CHAR (term_chrg, 'L999G999G999G990D99'),
            --   'right'
            --);
            lv_accSummJson := lv_accSummJson || '"termCharge":' || term_chrg || ',';

            --twbkfrmt.p_tablerowclose;
            --twbkfrmt.p_tablerowopen ();

            --twbkfrmt.p_tabledatalabel (
            --   G\$_NLS.Get ('BWSKOAC1-0029',
            --      'SQL',
            --      'Term Credits and Payments:'),
            --   ccolspan   => 2 + colspan_var
            --);
            --twbkfrmt.p_tabledata (
            --   TO_CHAR (term_pay, 'L999G999G999G990D99'),
            --   'right'
            --);
            lv_accSummJson := lv_accSummJson || '"termPay":' || term_pay || ',';

            --twbkfrmt.p_tablerowclose;
            --twbkfrmt.p_tablerowopen ();

            --twbkfrmt.p_tabledatalabel (
            --   G\$_NLS.Get ('BWSKOAC1-0030', 'SQL', 'Term Balance:'),
            --   ccolspan   => 3 + colspan_var
            --);
            --twbkfrmt.p_tabledata (
            --   TO_CHAR (term_bal, 'L999G999G999G990D99'),
            --   'right'
            --);
            lv_accSummJson := lv_accSummJson || '"termBalance":' || term_bal || '}],'; --for term

            --twbkfrmt.p_tablerowclose;
            --twbkfrmt.p_tablerowopen ();

            --twbkfrmt.p_tabledatalabel (
            --   G\$_NLS.Get ('BWSKOAC1-0031', 'SQL', 'Account Balance:'),
            --   ccolspan   => 3 + colspan_var
            --);
            --twbkfrmt.p_tabledata (
            --   TO_CHAR (total_bal, 'L999G999G999G990D99'),
            --   'right'
            --);
            lv_accSummJson := lv_accSummJson || '"acctTotal":' || total_bal || '}';

            --twbkfrmt.p_tablerowclose;
            --twbkfrmt.p_tableclose;
            EXIT;
         END IF;
      END IF;
   END LOOP;

   --CLOSE tbraccd_viewc;
   --twbkwbis.p_closedoc (curr_release);
   --COMMIT;
   ? := lv_accSummJson;
end;
--END p_viewacct;
"""
}