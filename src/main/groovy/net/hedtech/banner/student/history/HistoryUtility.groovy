/** *****************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 ***************************************************************************** */
package net.hedtech.banner.student.history

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.BusinessLogicValidationException
import net.hedtech.banner.i18n.MessageHelper
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger
import grails.web.context.ServletContextHolder as SCH

//import net.hedtech.banner.student.registration.RegistrationStudentCourseRegistration
import org.grails.web.util.GrailsApplicationAttributes as GA

import java.sql.CallableStatement
import java.sql.SQLException
import java.sql.Types

/*
 *  Utility for History module
 */

class HistoryUtility {

    private static final log = Logger.getLogger(HistoryUtility.class)

    public static String DEFAULT_ROUND_CODE = "R"
    public static Integer DEFAULT_DEFAULT_DISPLAY_NUMBER = 3

    public static String GPA_TYPE_INDICATOR_I = "I"
    public static String GPA_TYPE_INDICATOR_I_DESC = 'stuff' // = MessageHelper.message("default.gpa.institutional")
    public static String GPA_TYPE_INDICATOR_T = "T"
    public static String GPA_TYPE_INDICATOR_T_DESC = 'stuff'// = MessageHelper.message("default.gpa.transfer")
    public static String GPA_TYPE_INDICATOR_O = "O"
    public static String GPA_TYPE_INDICATOR_O_DESC = 'stuff'// = MessageHelper.message("default.gpa.overall")


    /*
    public static boolean isGradesRolled(String term, String courseReferenceNumber) {

        def rolled = RegistrationStudentCourseRegistration.fetchCountRolledNotWithdrawnByTermAndCourseReferenceNumber(term, courseReferenceNumber)

        return rolled as boolean
    }
    */


    public static def fetchNextValueFromSequenceGenerator() {
        def selectSql = """SELECT shbgseq.nextval FROM dual"""
        def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        def sql
        def result = []
        try {
            sql = new Sql(sessionFactory?.currentSession?.connection())
            result = sql.firstRow(selectSql)
        }
        catch (e) {
            log.error("Error executing sql in HistoryUtility: ", e)
            throw e
        }
        finally {
            try {
                sql?.close()
            } catch (SQLException se) { /* squash it*/
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }
        return result[0]
    }


    public
    static boolean validateGradeScaleForSection(String gradeScaleName, String term, String courseReferenceNumber) {
        def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        def selSql = """SELECT 'Y' as valid
                            FROM SHBGSCH G, SSBSECT S, SCBCRSE C
                            WHERE SHBGSCH_NAME = :name
                            AND SSBSECT_CRN = :courseReferenceNumber
                            AND SSBSECT_TERM_CODE = :term
                            AND SCBCRSE_EFF_TERM = ( SELECT MAX (SCBCRSE_EFF_TERM)
                                                     FROM SCBCRSE
                                                     WHERE SCBCRSE_SUBJ_CODE = S.SSBSECT_SUBJ_CODE
                                                     AND SCBCRSE_CRSE_NUMB = S.SSBSECT_CRSE_NUMB
                                                     AND SCBCRSE_EFF_TERM <= :term)
                            AND SCBCRSE_SUBJ_CODE = SSBSECT_SUBJ_CODE
                            AND SCBCRSE_CRSE_NUMB = SSBSECT_CRSE_NUMB
                            AND  ( SHBGSCH_SUBJ_CODE = SCBCRSE_SUBJ_CODE OR SHBGSCH_SUBJ_CODE IS NULL)
                            AND  ( SHBGSCH_CRSE_NUMB = SCBCRSE_CRSE_NUMB OR SHBGSCH_CRSE_NUMB IS NULL)
                            AND  ( SHBGSCH_SEQ_NUMB = SSBSECT_SEQ_NUMB OR SHBGSCH_SEQ_NUMB IS NULL)
                            AND  ( SHBGSCH_CAMP_CODE = SSBSECT_CAMP_CODE OR SHBGSCH_CAMP_CODE IS NULL)
                            AND  ( SHBGSCH_TERM_CODE = :term OR SHBGSCH_TERM_CODE IS NULL)
                            AND  ( SHBGSCH_COLL_CODE = SCBCRSE_COLL_CODE OR SHBGSCH_COLL_CODE IS NULL)
                            AND  ( SHBGSCH_DIVS_CODE = SCBCRSE_DIVS_CODE OR SHBGSCH_DIVS_CODE IS NULL)
                            AND  ( SHBGSCH_DEPT_CODE = SCBCRSE_DEPT_CODE OR SHBGSCH_DEPT_CODE IS NULL)
                            AND  ( SHBGSCH_CRN = :courseReferenceNumber OR SHBGSCH_CRN IS NULL)"""
        def sql
        def result = []
        try {
            sql = new Sql(sessionFactory.currentSession.connection())
            result = sql.firstRow(selSql, [name: gradeScaleName, term: term, courseReferenceNumber: courseReferenceNumber])
        }
        catch (e) {
            log.error("Error executing sql in HistoryUtility: ", e)
            throw e
        }
        finally {
            try {
                sql?.close()
            } catch (SQLException se) { /* squash it*/
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }
        return result?.valid as boolean
    }

    /**
     * Insert/update/delete Component Mark records following changes to the Component or Section
     */

    public static void updateComponentMarks(String term, String courseReferenceNumber, Integer gradeComponentId,
                                            String inclusionIndicator, String sectionUpdateIndicator, String dbAction, Date date) {
        def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        CallableStatement sqlCall
        try {
            def connection = sessionFactory.currentSession.connection()
            String gradeCodeReCalc = "{ call shkgcom.P_ShrgcomUpdateProc(?,?,?,?,?,?,?) }"
            sqlCall = connection.prepareCall(gradeCodeReCalc)
            sqlCall.setString(1, term)
            sqlCall.setString(2, courseReferenceNumber)
            gradeComponentId ? sqlCall.setInt(3, gradeComponentId) : sqlCall.setNull(3, Types.INTEGER)
            inclusionIndicator ? sqlCall.setString(4, inclusionIndicator) : sqlCall.setNull(4, Types.VARCHAR)
            sqlCall.setString(5, sectionUpdateIndicator)
            sqlCall.setString(6, dbAction)
            date ? sqlCall.setDate(7, new java.sql.Date(date.getTime())) : sqlCall.setNull(7, Types.DATE)

            sqlCall.executeUpdate()
        }
        catch (e) {
            log.error("Error executing sql in HistoryUtility: ", e)
            throw e
        }
        finally {
            try {
                sqlCall?.close()
            } catch (SQLException se) { /* squash it*/
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }
    }

    /**
     * Insert/update/delete Sub-Component Mark records following changes to the Sub-Component, Component or Section
     */

    public
    static void updateSubComponentMarks(String term, String courseReferenceNumber, Integer gradeComponentId, Integer gradeSubComponentId,
                                        String sectionUpdateIndicator, String componentUpdateIndicator, String dbAction, Date date) {
        def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        CallableStatement sqlCall
        try {
            def connection = sessionFactory.currentSession.connection()
            String gradeCodeReCalc = "{ call shkgcom.P_update_shrsmrk_from_shrscom(?,?,?,?,?,?,?,?) }"
            sqlCall = connection.prepareCall(gradeCodeReCalc)
            sqlCall.setString(1, term)
            sqlCall.setString(2, courseReferenceNumber)
            gradeComponentId ? sqlCall.setInt(3, gradeComponentId) : sqlCall.setNull(3, Types.INTEGER)
            gradeSubComponentId ? sqlCall.setInt(4, gradeSubComponentId) : sqlCall.setNull(4, Types.INTEGER)
            sqlCall.setString(5, sectionUpdateIndicator)
            sqlCall.setString(6, componentUpdateIndicator)
            sqlCall.setString(7, dbAction)
            date ? sqlCall.setDate(8, new java.sql.Date(date.getTime())) : sqlCall.setNull(8, Types.DATE)

            sqlCall.executeUpdate()
        }
        catch (e) {
            log.error("Error executing sql in HistoryUtility: ", e)
            throw e
        }
        finally {
            try {
                sqlCall?.close()
            } catch (SQLException se) { /* squash it*/
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }
    }

    /**
     * Calculate component marks for all components for all students in the section
     */

    public static void calculateComponentMarksForSection(String term, String courseReferenceNumber) {
        def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        CallableStatement sqlCall
        def sql
        try {
            def result
            def sqlString = """SELECT SHRSCOM_GCOM_ID as gradeComponentId,SFRSTCR_PIDM as pidm
                                FROM SHRSCOM, SFRSTCR
                                WHERE SHRSCOM_TERM_CODE = SFRSTCR_TERM_CODE
                                AND SHRSCOM_CRN = SFRSTCR_CRN
                                AND SFRSTCR_TERM_CODE = ?
                                AND SFRSTCR_CRN = ?"""
            def connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            result = sql.rows(sqlString, [term, courseReferenceNumber])

            String calculateComponentMarks = "{ call shkegrb.P_shrmrks_process(?, ?, ?, ?) }"
            sqlCall = connection.prepareCall(calculateComponentMarks)
            result.each {
                /*
                If all sub-component marks have been entered create a mark record for the parent component.
                This is the sub-component
                version of the composite mark process P_ShrcmrkProcess.
                */
                sqlCall.clearParameters()
                sqlCall.setString(1, term)
                sqlCall.setString(2, courseReferenceNumber)
                sqlCall.setInt(3, it.gradeComponentId as Integer)
                sqlCall.setInt(4, it.pidm as Integer)

                sqlCall.executeUpdate()
            }
        }
        catch (e) {
            log.error("Error executing sql in HistoryUtility: ", e)
            throw e
        }
        finally {
            try {
                sql?.close()
                sqlCall?.close()
            } catch (SQLException se) { /* squash it*/
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }
    }

    /**
     * Calculate the composite marks for all the students for the section
     */

    public static void calculateCompositeMarksForSection(String term, String courseReferenceNumber) {
        def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        CallableStatement sqlCall
        try {
            def connection = sessionFactory.currentSession.connection()
            String compositeMarkCalc = "{ call shkgcom.P_ShrcmrkCRNProc (?,?) }"
            sqlCall = connection.prepareCall(compositeMarkCalc)
            term ? sqlCall.setString(1, term) : sqlCall.setNull(1, Types.VARCHAR)
            courseReferenceNumber ? sqlCall.setString(2, courseReferenceNumber) : sqlCall.setNull(2, Types.VARCHAR)

            sqlCall.executeUpdate()
        }
        catch (e) {
            log.error("Error executing sql in HistoryUtility: ", e)
            throw e
        }
        finally {
            try {
                sqlCall?.close()
            } catch (SQLException se) { /* squash it*/
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }
    }

    // This method return true if the GRADE is used otherwise false
    public static boolean isGradesUsed(String gradeCode, String termCode, String levelCode) {
        if (!gradeCode || !termCode || !levelCode) return false
        def selectSql = """SELECT 1 FROM SHRTTRM
                           WHERE  EXISTS  (
                                  SELECT 'X'
                                  FROM SHRTCKG, SHRTCKL, SHRGRDE
                                  WHERE SHRGRDE_CODE = ?
                                  AND SHRGRDE_TERM_CODE_EFFECTIVE >= ?
                                  AND SHRGRDE_LEVL_CODE = ?
                                  AND SHRGRDE_LEVL_CODE = SHRTCKL_LEVL_CODE
                                  AND SHRGRDE_CODE = SHRTCKG_GRDE_CODE_FINAL
                                  AND SHRGRDE_TERM_CODE_EFFECTIVE = SHRTCKG_TERM_CODE
                                  AND SHRTCKG_TERM_CODE = SHRTCKL_TERM_CODE
                                  AND SHRTCKG_PIDM = SHRTCKL_PIDM
                                  AND SHRTCKG_TCKN_SEQ_NO = SHRTCKL_TCKN_SEQ_NO )"""
        def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        def sql
        def result = []
        try {
            sql = new Sql(sessionFactory?.currentSession?.connection())
            result = sql.firstRow(selectSql, [gradeCode, termCode, levelCode])
        }
        catch (e) {
            log.error("Error executing sql in HistoryUtility: ", e)
            throw e
        }
        finally {
            try {
                sql?.close()
            } catch (SQLException se) { /* squash it*/
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }
        return result ? result[0] as boolean : false
    }


    public
    static boolean hasGradeExistInAcademicHistory(String effectiveTerm, String levelCode, String gradeIncompleteFinal) {
        if (!effectiveTerm || !levelCode || !gradeIncompleteFinal) return false
        def selectSql = """SELECT 'X'
                                   FROM SHRTCKG a, SHRTCKN, SHRTCKL
                                   WHERE a.SHRTCKG_GRDE_CODE_INCMP_FINAL = ?
                                   AND SHRTCKG_PIDM = SHRTCKN_PIDM
                                   AND SHRTCKN_PIDM = SHRTCKL_PIDM
                                   AND SHRTCKG_TERM_CODE = SHRTCKN_TERM_CODE
                                   AND SHRTCKN_TERM_CODE = SHRTCKL_TERM_CODE
                                   AND SHRTCKG_TCKN_SEQ_NO = SHRTCKN_SEQ_NO
                                   AND SHRTCKN_SEQ_NO = SHRTCKL_TCKN_SEQ_NO
                                   AND SHRTCKL_LEVL_CODE = ?
                                   AND a.SHRTCKG_SEQ_NO =
                                    ( SELECT max(b.SHRTCKG_SEQ_NO)
                                                FROM SHRTCKG b
                                                WHERE a.SHRTCKG_PIDM = b.SHRTCKG_PIDM
                                                  and a.SHRTCKG_TERM_CODE = b.SHRTCKG_TERM_CODE
                                                  and a.SHRTCKG_TCKN_SEQ_NO = b.SHRTCKG_TCKN_SEQ_NO
                                    )
                                   AND EXISTS  (SELECT 'x'
                                                 FROM shrgrde a
                                                WHERE a.shrgrde_code = SHRTCKG_GRDE_CODE_INCMP_FINAL
                                                  and a.shrgrde_levl_code = shrtckl_levl_code
                                                  and a.shrgrde_term_code_effective =
                                                         (SELECT max(b.shrgrde_term_code_effective)
                                                            FROM shrgrde b
                                                             WHERE b.shrgrde_code = a.shrgrde_code
                                                             and b.shrgrde_levl_code = a.shrgrde_levl_code
                                                             and b.shrgrde_term_code_effective >= ?
                                                             and b.shrgrde_term_code_effective <= shrtckg_term_code
                                                           )
                                                     ) """
        def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        def sql
        def result = []
        try {
            sql = new Sql(sessionFactory?.currentSession?.connection())
            result = sql.firstRow(selectSql, [gradeIncompleteFinal, levelCode, effectiveTerm])
        }
        catch (e) {
            log.error("Error executing sql in HistoryUtility: ", e)
            throw e
        }
        finally {
            try {
                sql?.close()
            } catch (SQLException se) { /* squash it*/
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }
        return result ? result[0] as boolean : false
    }


    public static
    def isValidGradeIncompleteFinal(String level, String effectiveTerm, String grade, String gradeIncompleteFinal) {
        def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        CallableStatement sqlCall
        def incompleteFinalError
        def isValidGradeIncompleteFinal
        try {
            def connection = sessionFactory.currentSession.connection()
            String validateIncompleteFinalGrade = "{? = call shkincg.f_validate_incmp_final_grade(?,?,?,?,?) }"
            sqlCall = connection.prepareCall(validateIncompleteFinalGrade)
            sqlCall.registerOutParameter(1, java.sql.Types.VARCHAR)
            sqlCall.registerOutParameter(6, java.sql.Types.VARCHAR)
            sqlCall.setString(2, effectiveTerm)
            sqlCall.setString(3, gradeIncompleteFinal)
            sqlCall.setString(4, grade)
            sqlCall.setString(5, level)
            sqlCall.executeQuery()
            isValidGradeIncompleteFinal = sqlCall.getString(1)
            incompleteFinalError = sqlCall.getString(6)
        }
        catch (e) {
            log.error("Error executing sql in HistoryUtility: ", e)
            throw e
        }
        finally {
            try {
                sqlCall?.close()
            } catch (SQLException se) { /* squash it*/
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }
        return ['isValidGradeIncompleteFinal': isValidGradeIncompleteFinal, 'errorCode': incompleteFinalError]
    }


    public static boolean isCourseTermRestrictionExists(String subject, String courseNumber, String term) {
        def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        CallableStatement callableStatement
        boolean courseTermRestrictionExists = false
        try {
            def connection = sessionFactory?.currentSession?.connection()
            String checkTermRestrictionExists = "{? = call sb_section_rules.f_check_term_restrict(?,?,?)}"
            callableStatement = connection.prepareCall(checkTermRestrictionExists)
            callableStatement.registerOutParameter(1, Types.VARCHAR)
            callableStatement.setString(2, term)
            callableStatement.setString(3, subject)
            callableStatement.setString(4, courseNumber)
            callableStatement.executeQuery()
            courseTermRestrictionExists = callableStatement.getString(1)
        } catch (e) {
            log.error("Error executing sql in HistoryUtility: ", e)
            throw e
        }
        finally {
            try {
                callableStatement?.close()
            } catch (SQLException se) { /* squash it*/
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }
        return courseTermRestrictionExists
    }

    // This method gets Institutiona Term GPA for a given pidm, term, level and study path
    public static List fetchAllInstitutionalTermGpaByPidmTermLevelAndStudyPath(Integer pidm, String term, String level, Integer studyPath) {

        def selectSql = """ SELECT SHRTGSP_QUALITY_POINTS as qualityPoints, SHRTGSP_GPA as gpa, SHRTGSP_HOURS_ATTEMPTED as hoursAttempted, SHRTGSP_HOURS_EARNED as hoursEarned, SHRTGSP_GPA_HOURS as gpaHours
                            FROM SHRTGSP
                            WHERE SHRTGSP_PIDM = :pidm
                            AND SHRTGSP_TERM_CODE = :term
                            AND SHRTGSP_LEVL_CODE = :level
                            AND SHRTGSP_STSP_KEY_SEQUENCE = :studyPath """
        def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        def sql
        def result = []
        try {
            sql = new Sql(sessionFactory?.currentSession?.connection())
            result = sql.rows(selectSql, [pidm: pidm, term: term, level: level, studyPath: studyPath])
        }
        catch (e) {
            log.error("Error executing sql in HistoryUtility: ", e)
            throw e
        }
        finally {
            try {
                sql?.close()
            } catch (SQLException se) { /* squash it*/
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }
        return result
    }

    // This method gets Institutiona Level GPA for a given pidm, level study path and GPA type
    public static def fetchLevelGpaByPidmLevelStudyPathAndGpaType(Integer pidm, String level, Integer studyPath, String gpaType) {

        def selectSql = """ SELECT SHRLGSP_QUALITY_POINTS as qualityPoints, SHRLGSP_GPA as gpa, SHRLGSP_HOURS_ATTEMPTED as hoursAttempted, SHRLGSP_HOURS_EARNED as hoursEarned, SHRLGSP_GPA_HOURS as gpaHours
                            FROM SHRLGSP
                            WHERE SHRLGSP_PIDM = :pidm
                            AND SHRLGSP_LEVL_CODE = :level
                            AND SHRLGSP_STSP_KEY_SEQUENCE = :studyPath
                            AND SHRLGSP_GPA_TYPE_IND = :gpaType """
        def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        def sql
        def result
        try {
            sql = new Sql(sessionFactory?.currentSession?.connection())
            result = sql.firstRow(selectSql, [pidm: pidm, level: level, studyPath: studyPath, gpaType: gpaType])
        }
        catch (e) {
            log.error("Error executing sql in HistoryUtility: ", e)
            throw e
        }
        finally {
            try {
                sql?.close()
            } catch (SQLException se) { /* squash it*/
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }
        return result
    }

    // This method return true if the Grade is used for grading otherwise false
    public static boolean isGradeUsedForGrading(String term, String gradeCode, String level) {
        if (!term || !gradeCode || !level) return false
        def selectSql = """SELECT 1 FROM SFRSTCR
                           WHERE SFRSTCR_LEVL_CODE = ?
                           AND SFRSTCR_GRDE_CODE = ?
                           AND SFRSTCR_TERM_CODE >= ?
                           AND SFRSTCR_TERM_CODE < (SELECT MIN(SHRGRDE.SHRGRDE_TERM_CODE_EFFECTIVE)
                                                    FROM SHRGRDE
                                                    WHERE SHRGRDE.SHRGRDE_LEVL_CODE = ?
                                                    AND SHRGRDE.SHRGRDE_CODE = ?
                                                    AND SHRGRDE.SHRGRDE_TERM_CODE_EFFECTIVE > ?)"""
        def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        def sql
        def result = []
        try {
            sql = new Sql(sessionFactory?.currentSession?.connection())
            result = sql.firstRow(selectSql, [level, gradeCode, term, level, gradeCode, term])
        }
        catch (e) {
            log.error("Error executing sql in HistoryUtility: ", e)
            throw e
        }
        finally {
            try {
                sql?.close()
            } catch (SQLException se) { /* squash it*/
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }
        return result ? result[0] as boolean : false
    }

    /**
     * Method to call plsql package function that encrypts password
     * via Oracles dbms_obfuscation_toolkit.
     *
     * @param password. The String represent the password
     * @return String. The encrypted password.
     */

    public static String encryptPassword(String password) {
        def encryptedPassword
        CallableStatement cs
        try {
            def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
            def sessionFactory = ctx.sessionFactory
            def connection = sessionFactory.currentSession.connection()
            String queryString = "{ ? = call sokrest.f_encrypt(?) }"
            cs = connection.prepareCall(queryString)
            cs.registerOutParameter(1, java.sql.Types.VARCHAR)
            cs.setString(2, password)
            cs.executeQuery()
            encryptedPassword = cs.getString(1)
        }
        catch (e) {
            log.error("Error executing sql in HistoryUtility: ", e)
            throw e
        }
        finally {
            try {
                cs?.close()
            } catch (SQLException se) { /* squash it*/
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }
        return encryptedPassword
    }

    /**
     * Check if a property of the domain is dirty.
     * @param domainClass. The domain's Class.
     * @param domainObj. The domain object.
     * @param property. name of the property.
     * @return true if the property is dirty, false otherwise.
     */
    public static boolean isDomainPropertyDirty(def domainClass, def domainObj, String property) {
        def content = ServiceBase.extractParams(domainClass, domainObj)
        def domainObject = domainClass?.get(content?.id)
        use(org.codehaus.groovy.runtime.InvokerHelper) { domainObject.setProperties(content)}

        return (property in domainObject?.dirtyPropertyNames)
    }

    /**
     * This method fetches the GPA rule used for displaying GPA and Quality Points, for a given pidm, level, campus and term.
     * Returns a map of Display Number and Round/Truncate Indicator for student's GPA and Quality Points .
     * */

    public static Map fetchGpaRuleForStudent(Integer pidm, String level, String campus, String term) {
        def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        CallableStatement sqlCall
        Map result = new HashMap<String, Object>()
        try {
            def connection = sessionFactory.currentSession.connection()
            String gpaRuleSql = "{ call shkcgpa.p_get_student_formats(?,?,?,?,?,?,?,?) }"
            sqlCall = connection.prepareCall(gpaRuleSql)
            pidm ? sqlCall.setInt(1, pidm) : sqlCall.setNull(1, Types.INTEGER)
            level ? sqlCall.setString(2, level) : sqlCall.setNull(2, Types.VARCHAR)
            campus ? sqlCall.setString(3, campus) : sqlCall.setNull(3, Types.VARCHAR)
            term ? sqlCall.setString(4, term) : sqlCall.setNull(4, Types.VARCHAR)
            sqlCall.registerOutParameter(4, Types.VARCHAR)
            sqlCall.registerOutParameter(5, Types.VARCHAR)
            sqlCall.registerOutParameter(6, Types.INTEGER)
            sqlCall.registerOutParameter(7, Types.VARCHAR)
            sqlCall.registerOutParameter(8, Types.INTEGER)
            sqlCall.executeQuery()
            result << [
                    term                               : sqlCall.getString(4) ?: '',
                    gpaRoundTruncateIndicator          : sqlCall.getString(5) ?: '',
                    gpaDisplayNumber                   : Integer.valueOf(sqlCall.getInt(6) ?: 0),
                    qualityPointsRoundTruncateIndicator: sqlCall.getString(7) ?: '',
                    qualityPointsDisplayNumber         : Integer.valueOf(sqlCall.getInt(8) ?: 0)
            ]
        }
        catch (e) {
            log.error("Error executing sql in HistoryUtility: ", e)
            throw e
        }
        finally {
            try {
                sqlCall?.close()
            } catch (SQLException se) { /* squash it*/
                log.error "Sql Statement is already closed, no need to close it."
            }
        }
        return result
    }

    /**
     * This method calculates the format mask to be used for displaying Quality Points and GPA.
     * */

    public static String calculateGpaQualityPointsFormat(Integer maxLength, Integer precision) {
        String format
        if(null != maxLength && null != precision) {
            String integral = '###,###,###,###,###,###,##0'
            format = integral[(27 - (maxLength - precision - 1))..-1] + '.' + '0' * (precision - 1) + '0'
            if (format[0] == ',') {
                format = format[1..-1]
            }
        }
        return format
    }

    /**
     * This method calculates the format mask to be used for displaying Quality Points and GPA on self-service.
     * */

    public static String calculateGpaQualityPointsWebFormat(Integer maxLength, Integer precision) {
        String format
        if(null == precision || precision < 1) precision = 1
        if(null == maxLength || maxLength < 3) maxLength = 3
        format = '#' * (maxLength - precision - 2) + '0.' + '0' * (precision - 1) + '0'
        return format
    }


    public static String convertToBanner9NumberFormat(String INBNumberFormat) {
        INBNumberFormat.replaceAll(/^([9|G|0]*)D?([9|0]*)$/, {
            String integral = it[1].replaceAll('9', '#').replaceAll('G', ',').replaceAll(/#$/, '0')
            String decimal = it[2].replaceAll('9', '0')
            return (it[0].contains('D') ? [integral ?: '0', decimal ?: '0'].join('.') : integral)
        })
    }

    /**
     * This method calculates the truncated or rounded value for the given GPA or Quality Points based on the GPA Rule.
     * */

    static BigDecimal calculateTruncatedOrRoundedValue(Integer precision, String roundTruncateIndicator, BigDecimal value) {
        if (null == value || null == precision || null == roundTruncateIndicator) {
            return null
        }
        def result = new BigDecimal(value)
        if (roundTruncateIndicator == 'T') {
            result = result?.setScale(precision, BigDecimal.ROUND_DOWN)
        } else if (roundTruncateIndicator == 'R') {
            result = result?.setScale(precision, BigDecimal.ROUND_HALF_UP)
        }
        return result
    }


    public static List fetchAllInstitutionalAndTransferSubjectsForPidmAndLevel(Integer pidm, String level) {
        def selectSql = """SELECT STVSUBJ_CODE as code, STVSUBJ_DESC as description,
                            STVSUBJ_USER_ID as lastModifiedBy, STVSUBJ_ACTIVITY_DATE as lastModified
                            FROM STVSUBJ
                            WHERE
                            (STVSUBJ_CODE IN (SELECT SHRTCKN_SUBJ_CODE
                            FROM SHRTCKN
                            WHERE SHRTCKN_PIDM = :pidm
                            AND SHRTCKN_PIDM IN (SELECT SHRTCKL_PIDM
                                                 FROM SHRTCKL
                                                 WHERE SHRTCKL_PIDM = SHRTCKN_PIDM
                                                 AND SHRTCKL_TERM_CODE = SHRTCKN_TERM_CODE
                                                 AND SHRTCKL_TCKN_SEQ_NO = SHRTCKN_SEQ_NO
                                                 AND SHRTCKL_LEVL_CODE = :level)
                            GROUP BY SHRTCKN_SUBJ_CODE))
                            OR
                            (STVSUBJ_CODE IN (SELECT SHRTRCE_SUBJ_CODE
                                                 FROM SHRTRCE
                                                 WHERE SHRTRCE_PIDM = :pidm
                                                 AND SHRTRCE_LEVL_CODE = :level
                                                 GROUP BY SHRTRCE_SUBJ_CODE))
                            ORDER BY STVSUBJ_CODE"""
        def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        def sql
        def result = []
        try {
            sql = new Sql(sessionFactory?.currentSession?.connection())
            result = sql.rows(selectSql, [pidm: pidm, level: level])
        }
        catch (e) {
            log.error("Error executing sql in HistoryUtility: ", e)
            throw e
        }
        finally {
            try {
                sql?.close()
            } catch (SQLException se) { /* squash it*/
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }
        return result
    }

    /**
     * This method calculates Student's GPA for a given pidm, subject and level.
     * Returns a map containing Attempted Hours, Passed Hours, Earned Hours, GPA Hours, Quality Points
     * and GPA for Institution, Transfer and Overall courses.
     * */

    public static Map calculateStudentSubjectGpa(Integer pidm, String subject, String level) {
        def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
        def sessionFactory = ctx.sessionFactory
        CallableStatement sqlCall
        def result = [:]
        try {
            def connection = sessionFactory.currentSession.connection()
            String truncateOrRoundSql = "{ call shkcgpa.p_subj_gpa(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) }"
            sqlCall = connection.prepareCall(truncateOrRoundSql)
            pidm ? sqlCall.setInt(1, pidm) : sqlCall.setNull(1, Types.INTEGER)
            subject ? sqlCall.setString(2, subject) : sqlCall.setNull(2, Types.VARCHAR)
            level ? sqlCall.setString(3, level) : sqlCall.setNull(3, Types.VARCHAR)
            sqlCall.registerOutParameter(4, java.sql.Types.DECIMAL)
            sqlCall.registerOutParameter(5, java.sql.Types.DECIMAL)
            sqlCall.registerOutParameter(6, java.sql.Types.DECIMAL)
            sqlCall.registerOutParameter(7, java.sql.Types.DECIMAL)
            sqlCall.registerOutParameter(8, java.sql.Types.DECIMAL)
            sqlCall.registerOutParameter(9, java.sql.Types.DECIMAL)
            sqlCall.registerOutParameter(10, java.sql.Types.DECIMAL)
            sqlCall.registerOutParameter(11, java.sql.Types.DECIMAL)
            sqlCall.registerOutParameter(12, java.sql.Types.DECIMAL)
            sqlCall.registerOutParameter(13, java.sql.Types.DECIMAL)
            sqlCall.registerOutParameter(14, java.sql.Types.DECIMAL)
            sqlCall.registerOutParameter(15, java.sql.Types.DECIMAL)
            sqlCall.registerOutParameter(16, java.sql.Types.DECIMAL)
            sqlCall.registerOutParameter(17, java.sql.Types.DECIMAL)
            sqlCall.registerOutParameter(18, java.sql.Types.DECIMAL)
            sqlCall.registerOutParameter(19, java.sql.Types.DECIMAL)
            sqlCall.registerOutParameter(20, java.sql.Types.DECIMAL)
            sqlCall.registerOutParameter(21, java.sql.Types.DECIMAL)
            sqlCall.executeQuery()
            result << [
                    institution: [
                            hoursAttempted: sqlCall.getBigDecimal(4) ?: 0,
                            hoursPassed   : sqlCall.getBigDecimal(5) ?: 0,
                            hoursEarned   : sqlCall.getBigDecimal(6) ?: 0,
                            gpaHours      : sqlCall.getBigDecimal(7) ?: 0,
                            qualityPoints : sqlCall.getBigDecimal(8) ?: 0,
                            gpa           : sqlCall.getBigDecimal(9) ?: 0],
                    transfer   : [
                            hoursAttempted: sqlCall.getBigDecimal(10) ?: 0,
                            hoursPassed   : sqlCall.getBigDecimal(11) ?: 0,
                            hoursEarned   : sqlCall.getBigDecimal(12) ?: 0,
                            gpaHours      : sqlCall.getBigDecimal(13) ?: 0,
                            qualityPoints : sqlCall.getBigDecimal(14) ?: 0,
                            gpa           : sqlCall.getBigDecimal(15) ?: 0],
                    overall    : [
                            hoursAttempted: sqlCall.getBigDecimal(16) ?: 0,
                            hoursPassed   : sqlCall.getBigDecimal(17) ?: 0,
                            hoursEarned   : sqlCall.getBigDecimal(18) ?: 0,
                            gpaHours      : sqlCall.getBigDecimal(19) ?: 0,
                            qualityPoints : sqlCall.getBigDecimal(20) ?: 0,
                            gpa           : sqlCall.getBigDecimal(21) ?: 0]
            ]
        }
        catch (e) {
            log.error("Error executing sql in HistoryUtility: ", e)
            throw e
        }
        finally {
            try {
                sqlCall?.close()
            } catch (SQLException se) { /* squash it*/
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }
        return result
    }


    public static void calculateGpa(Integer pidm, String termCode, String finAidIndicator = 'N') {
        if (!pidm || !termCode) return
        def sessionFactory = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)?.sessionFactory
        CallableStatement sqlCall
        try {
            def connection = sessionFactory?.currentSession?.connection()
            String calculateGPA = "{ call SHKCGPA.P_TERM_GPA(?,?,?) }"
            sqlCall = connection.prepareCall(calculateGPA)
            sqlCall.setInt(1, pidm)
            sqlCall.setString(2, termCode)
            sqlCall.setString(3, finAidIndicator)
            sqlCall.executeUpdate()
        }
        catch (e) {
            log.error("Error executing sql in HistoryUtility: ", e)
            throw e
        }
        finally {
            try {
                sqlCall?.close()
            } catch (SQLException se) { /* squash it*/
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }
    }

    /**
     * Formats the number according to the GPA formatting rules
     * @param theNumber The number to format.
     * @param precision The number of decimal places to use.
     * @param roundCode 'T' if the value should be truncated. otherwise, round.
     * @return The formatted number
     */
    public static String formatByRules(BigDecimal gpa, Integer precision, String roundCode) {
        def returnNumber = "" + gpa
        if (roundCode == 'T') {
            returnNumber = "" + gpa.setScale(precision, BigDecimal.ROUND_DOWN)
        } else if (roundCode == 'R') {
            returnNumber = "" + gpa.setScale(precision, BigDecimal.ROUND_HALF_UP)
        }

        return returnNumber
    }


    public static processGpa(def gpa, def displayNumber, def roundCode) {

        GPA_TYPE_INDICATOR_I_DESC = MessageHelper.message("default.gpa.institutional")
        GPA_TYPE_INDICATOR_T_DESC = MessageHelper.message("default.gpa.transfer")
        GPA_TYPE_INDICATOR_O_DESC = MessageHelper.message("default.gpa.overall")

        def map = [:]
        map.gpa = HistoryUtility.formatByRules(gpa.gpa, displayNumber, roundCode)
        map.hours = gpa.hoursEarned
        map.hoursAttempted = gpa.hoursAttempted
        if (gpa?.level) map.levelDesc = gpa.level?.description
        map.typeDesc = gpa.gpaTypeIndicator
        if (gpa.gpaTypeIndicator == GPA_TYPE_INDICATOR_I) {
            map.gpaTypeIndicatorDesc = GPA_TYPE_INDICATOR_I_DESC
        } else if (gpa.gpaTypeIndicator == GPA_TYPE_INDICATOR_T) {
            map.gpaTypeIndicatorDesc = GPA_TYPE_INDICATOR_T_DESC
        } else if (gpa.gpaTypeIndicator == GPA_TYPE_INDICATOR_O) {
            map.gpaTypeIndicatorDesc = GPA_TYPE_INDICATOR_O_DESC
        }
        return map
    }


    public static formatGpaAndQualityPoints(
            def gpa, def gpaDisplayNumber, def gpaRoundCode, def qpDisplayNumber, def qpRoundCode) {

        GPA_TYPE_INDICATOR_I_DESC = MessageHelper.message("default.gpa.institutional")
        GPA_TYPE_INDICATOR_T_DESC = MessageHelper.message("default.gpa.transfer")
        GPA_TYPE_INDICATOR_O_DESC = MessageHelper.message("default.gpa.overall")

        def map = [:]
        map.gpa = HistoryUtility.formatByRules(gpa.gpa, gpaDisplayNumber, gpaRoundCode)
        map.qualityPoints = HistoryUtility.formatByRules(gpa.qualityPoints, qpDisplayNumber, qpRoundCode)
        if (gpa.gpaTypeIndicator == GPA_TYPE_INDICATOR_I) {
            map.gpaTypeIndicatorDesc = GPA_TYPE_INDICATOR_I_DESC
        } else if (gpa.gpaTypeIndicator == GPA_TYPE_INDICATOR_T) {
            map.gpaTypeIndicatorDesc = GPA_TYPE_INDICATOR_T_DESC
        } else if (gpa.gpaTypeIndicator == GPA_TYPE_INDICATOR_O) {
            map.gpaTypeIndicatorDesc = GPA_TYPE_INDICATOR_O_DESC
        }
        return map
    }

    /**
     * Returns the correct formatting rules based on the terms for the student and the selection rules as part of
     * HistoryInstitutionGpaSelection
     * @param termInformation a map with the following:
     *     level : <level code of the level that is related to the primary curriculum>
     *     campus: <The campus relaated to the primary curriculum>
     *     term : <usually the term selected>
     * @return The HistoryInstitutionGradePointAverageRule that matches the selection rules or null if there is no match
     */
    public static def getGpaFormatter(Integer pidm, Map termInformation) {
        def formatter = HistoryUtility.fetchGpaRuleForStudent(pidm, termInformation?.level, termInformation?.campus, termInformation?.term)

        // if formatter is not filled in, then we didn't get one back
        if (!formatter?.gpaRoundTruncateIndicator) {
            return null
        }

        return formatter
    }

    /**  methods used in faculty grade entry process **/

 /*
    public static List fetchHistoryIncompleteGradesRulesForTermAndCourse(String term, String crn) {
        def levelsList = RegistrationStudentCourseRegistration.fetchLevelsForStudentsInTermAndCourseQuery(term, crn)
        def incompleteRules = []
        levelsList.each {
            def incompleteGradesRule = HistoryIncompleteGradesRule.fetchByLevelAndTermEffective(it, term)
            incompleteRules << [level: it, doIncompleteProcessing: incompleteGradesRule.incmpGradingIndicator, incompGradeOverIndicator: incompleteGradesRule.incmpGradeOverIndicator, incmpDateOverType: incompleteGradesRule.incmpDateOverType]
        }
        return incompleteRules
    }
    */

    /**
     * Utility method to roll grade for single student
     * @param paramMap
     */
    public static void rollGradeForStudent(Map paramMap) {
        if (!paramMap.pidm || !paramMap.termCode || !paramMap.crn || !paramMap.userId || !paramMap.reportMode) {
            throw new ApplicationException('HistoryUtility', new BusinessLogicValidationException("rollGradeForStudent.required.parameter.missing", null))
        }
        def sessionFactory = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)?.sessionFactory
        CallableStatement sqlCall
        try {
            def connection = sessionFactory?.currentSession?.connection()
            String studentGradeRoll = "{ call SHKROLS.P_DO_GRADEROLL_PIDM(?,?,?,?,?,?,?,?,?,?,?) }"
            sqlCall = connection.prepareCall(studentGradeRoll)
            sqlCall.setString(1, paramMap.termCode)
            sqlCall.setString(2, paramMap.crn)
            sqlCall.setInt(3, paramMap.pidm)
            sqlCall.setString(4, paramMap.userId)
            sqlCall.setString(5, paramMap.sessionId)
            sqlCall.setString(6, paramMap.printSel)
            sqlCall.setString(7, paramMap.reportMode)
            sqlCall.setString(8, paramMap.startFromDate)
            sqlCall.setString(9, paramMap.startToDate)
            sqlCall.setString(10, paramMap.gradeTermCode)
            sqlCall.setString(11, paramMap.rollTitle)
            sqlCall.executeUpdate()
        }
        catch (e) {
            log.error("Error executing sql in HistoryUtility.rollingGradeForEachStudent: ", e)
            throw e
        }
        finally {
            try {
                sqlCall?.close()
            } catch (SQLException se) { /* squash it*/
                log.trace "Sql Statement is already closed, no need to close it."
            }
        }
    }

}
