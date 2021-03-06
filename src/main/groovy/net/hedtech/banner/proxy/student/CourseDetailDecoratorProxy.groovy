/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.proxy.student

import net.hedtech.banner.proxy.student.history.HistoryStudentCourseDetailProxy

/**
 * Decorator to display the course Work of the student
 **/
class CourseDetailDecoratorProxy {

    String id
    Integer studentPidm
    String termCode
    String termDescription
    String courseReferenceNumber
    String subjectCode
    String courseNumber
    String courseTitle
    String section
    String finalGrade
    String midtermGrade
    String historyFinalGrade
    String calculatedFinalGrade
    String gradeDetailDisplayInd
    String gradeCodeIncmpFinal
    Date incmpExtnDate
    String campusCode
    String campusDescription
    BigDecimal hoursEarned
    BigDecimal hoursAttempted
    BigDecimal gpaHours
    BigDecimal qualityPoints
    String levelCode
    String levelDescription
    String hasComponent
    String studyPath
    String studyPathName

    CourseDetailDecoratorProxy(HistoryStudentCourseDetailProxy record) {
        id = record.id
        studentPidm = record.studentPidm
        termCode = record.termCode
        termDescription = record.termDescription
        courseReferenceNumber = record.courseReferenceNumber
        subjectCode = record.subjectCode
        courseNumber = record.courseNumber
        courseTitle = record.courseTitle
        section = record.section
        finalGrade = record.finalGrade
        midtermGrade = record.midtermGrade
        historyFinalGrade = record.historyFinalGrade
        calculatedFinalGrade = record.calculatedFinalGrade
        gradeDetailDisplayInd = record.gradeDetailDisplayInd
        gradeCodeIncmpFinal = record.gradeCodeIncmpFinal
        incmpExtnDate = record.incmpExtnDate
        campusCode = record.campusCode
        hoursEarned = record.hoursEarned
        hoursAttempted = record.hoursAttempted
        gpaHours = record.gpaHours
        qualityPoints = record.qualityPoints
        levelCode = record.levelCode
        levelDescription = record.levelDescription
        hasComponent = record.hasComponent
        studyPath = (record.studyPath == '-1') ? '' : record.studyPath
        studyPathName = record.studyPathName
    }


    @Override
    public String toString() {
        return """CourseDetailDecoratorProxy[
                 id=$id,
                 studentPidm=$studentPidm,
                 termCode=$termCode,
                 termDescription=$termDescription,
                 courseReferenceNumber=$courseReferenceNumber,
                 subjectCode=$subjectCode,
                 courseNumber=$courseNumber,
                 courseTitle=$courseTitle,
                 section=$section,
                 finalGrade=$finalGrade,
                 midtermGrade=$midtermGrade,
                 historyFinalGrade=$historyFinalGrade,
                 calculatedFinalGrade=$calculatedFinalGrade,
                 gradeDetailDisplayInd=$gradeDetailDisplayInd,
                 gradeCodeIncmpFinal=$gradeCodeIncmpFinal,
                 incmpExtnDate=$incmpExtnDate
                 campusCode=$campusCode,
                 campusDescription=$campusDescription,
                 hoursEarned=$hoursEarned,
                 hoursAttempted=$hoursAttempted,
                 gpaHours=$gpaHours,
                 qualityPoints=$qualityPoints,
                 levelCode=$levelCode,
                 levelDescription=$levelDescription,
                 hasComponent=$hasComponent,
                 studyPath=$studyPath,
                 studyPathName=$studyPathName
              ]"""
    }
}
