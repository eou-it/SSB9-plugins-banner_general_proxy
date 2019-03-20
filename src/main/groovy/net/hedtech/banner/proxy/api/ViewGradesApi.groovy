package net.hedtech.banner.proxy.api

class ViewGradesApi {

    public final static String VIEW_GRADES_HOLDS  = """
DECLARE
       holds VARCHAR2(1);
       CURSOR sprhold_grdec (pidm NUMBER) IS
      SELECT 'X' hold_grde
        FROM stvhldd, sprhold
       WHERE sprhold_pidm = pidm
         AND sprhold_from_date <= SYSDATE
         AND sprhold_to_date >= SYSDATE
         AND stvhldd_code = sprhold_hldd_code
         AND stvhldd_grade_hold_ind = 'Y';
--        
       TYPE sprhold_grdec_type IS RECORD(
       hold_grde stvhldd.stvhldd_grade_hold_ind%TYPE);
--
       sprhold_grdec_rec sprhold_grdec_type;
--       
      BEGIN
      OPEN sprhold_grdec (?);
      FETCH sprhold_grdec INTO sprhold_grdec_rec;
      CLOSE sprhold_grdec;
--
      IF sprhold_grdec_rec.hold_grde IS NOT NULL THEN
         holds := 'Y';
      ELSE
         holds := 'N';
      END IF;
--     
      ? := holds;
--     
END;
    """
}
