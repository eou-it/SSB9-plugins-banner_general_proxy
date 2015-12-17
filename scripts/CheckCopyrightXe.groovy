/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
import java.text.ParseException
import java.text.SimpleDateFormat


includeTargets << grailsScript("_GrailsInit")

/**
 * Find all copyright errors in files modified in app and plugins with branch name same as app
 *
 */

target(checkCopyrightXe: "Find incorrect copyright years") {
    def ln = File.separator
    def appDirectoryName = new File(System.properties['base.dir'])
    def appDirectoryName_txt = appDirectoryName.toString()
    def output = new File(appDirectoryName_txt + ln + "target" + ln + "copyrighterrors-report.html")
    // get current branch
    def command2 = "git rev-parse --abbrev-ref HEAD"
    def proc2 = command2.execute(null, appDirectoryName)
    proc2.waitFor()
    if (proc2.exitValue() != 0) {
        println "Error, ${proc.err.text}"
        System.exit(-1)
    }
    def branches = proc2.in.text.readLines().collect {
        it.replaceAll(/[a-z0-9]*\trefs\/heads\//, '')
    }
    def branch = branches[0]

    def appPlugins = []
    appPlugins.add(appDirectoryName)
    // find plugins that are on same branch
    def plugins = findPlugins(appDirectoryName, ln, branch)
    appPlugins.addAll(plugins)
    //
    def year = new Date().format("YYYY")

    def numberErrors = 0
    def numberFiles = 0
    def copyrightBody = ""

    appPlugins.each {
        def directoryname = it
        def command = "git diff --name-only origin/master"
        def proc = command.execute(null, directoryname)
        proc.waitFor()
        if (proc.exitValue() != 0) {
            println "Error, ${proc.err.text}"
            System.exit(-1)
        }

        changes = proc.in.text.readLines().collect {
            it.replaceAll(/[a-z0-9]*\trefs\/heads\//, '')
        }

        changes.unique().each {
            def filename_dir_txt = directoryname.toString() + ln + it
            def filename = new File(filename_dir_txt)
            def filename_txt = it
            def fileNameExt = filename.name.tokenize('.').last()


            if (!(filename_txt =~ "CheckCopyrightXe.groovy" || fileNameExt == "rst" || fileNameExt == "properties" ||
                    filename_txt =~ ".git")) {
                if (!filename.isDirectory() && filename.exists()) {
                    def commitDetail = findCommitDetail(filename_txt, directoryname)
                    def yearChanged = getYearChanged(commitDetail)
                    if (!yearChanged) yearChanged = year

                    numberFiles += 1
                    def lineNo = 1
                    def copyRightLine = 1
                    filename.eachLine { line ->
                        lineNo++
                        if (line.toString().toUpperCase() =~ "COPYRIGHT") {
                            copyRightLine = 0
                            if (!(line =~ yearChanged)) {
                                numberErrors += 1
                                copyrightBody += "<tr><td>${directoryname.toString()}</td><td>${filename_txt}</td><td>${line}</td><td>${commitDetail.toString()}</td></tr>"
                            }
                        }
                    }
                    if (copyRightLine) {
                        numberErrors += 1
                        def noCopyRightLine = "No copyright statement identified in file"
                        copyrightBody += "<tr><td>${directoryname.toString()}</td><td>${filename_txt}</td><td>${noCopyRightLine}</td><td>${commitDetail.toString()}</td></tr>"
                    }
                }
            }
        }
    }
    output.write """
	    <html>
	        <body>
	            <head>
	                <title>Copyright Error Report for files in branch ${branch}</title>
	                <style type="text/css">
	                    table.report {
	                        border-width: 1px;
	                        border-spacing: 1px;
	                        border-style: outset;
	                        border-color: gray;
	                        border-collapse: separate;
	                        background-color: white;
	                    }
	                    table.report th {
	                        border-width: 1px;
	                        padding: 2px;
	                        border-style: inset;
	                        border-color: gray;
	                        background-color: lightblue;
	                        -moz-border-radius: 0px 0px 0px 0px;
	                        white-space: nowrap;
	                    }
	                    table.report td {
	                        border-width: 1px;
	                        padding: 2px;
	                        border-style: inset;
	                        border-color: gray;
	                        background-color: white;
	                        -moz-border-radius: 0px 0px 0px 0px;
	                    }
	                </style>
	            </head>
	            <h2>Copyright Error Report for files modified in App ${appDirectoryName_txt} branch ${branch}</h2>
	            Generated: ${new Date()}
	            <table class="report">
	                <thead>
                        <th>Path</th>
	                    <th>File</th>
	                    <th>Copyright Statement</th>
                        <th>Commit Details</th>
	                </thead>
	                <tfoot>
	                    <tr>
	                        <td colspan="4">Total: ${numberErrors} Errors</td>
	                    </tr>
	                </tfoot>
	                <tbody>
	                    ${copyrightBody}
	                </tbody>
	            </table>
	        </body>
	    </html>"""


    println "Number unique files revised  in branches ${branch} is ${numberFiles}, number with incorrect copyright ${numberErrors}"
    println "Review report  ${output.toString()}"
    if (numberErrors > 0) {
        System.exit(-1)
    }

}

// target for process, execute as:   grails checkCopyrightXe

setDefaultTarget(checkCopyrightXe)

// helper methods to get git log and parse date changed
def gitLog(def filename_txt, def directoryname) {
    ['git', 'log', '-n', '1', "${filename_txt}"].execute(null, directoryname).text.trim()
}


def findCommitDetail(def filename_txt, def directoryname) {

    def procc = gitLog(filename_txt, directoryname)

    def commitinfo = procc?.readLines()?.collect {
        it.replaceAll(/[a-z0-9]*\trefs\/heads\//, '')
    }

    return commitinfo
}


def getYearChanged(commitDetail) {
    def valueDate
    def date
    commitDetail.each {
        if (it =~ "Date:") {
            date = it.replace("Date:", "")
        }
    }
    valueDate = date.trim()

    def valueYear = parseYearFromVCLog(valueDate)
    return valueYear
}


def parseYearFromVCLog(date) {
    SimpleDateFormat sdfY = new SimpleDateFormat('yyyy')
    def dateFormats = ["yyyyMMdd",
                       "MMddyyyy",
                       "yyyy-MM-dd",
                       "MM/dd/yyyy",
                       "dd/MM/yyyy", 'EEE, d MMM yyyy HH:mm:ss Z',
                       'EEE MMM d yyyy HH:mm:ss Z',
                       'EEE MMM d HH:mm:ss yyyy Z'
    ]

    def year
    dateFormats.each {
        SimpleDateFormat sdf = new SimpleDateFormat(it)
        try {
            def testdate = sdf.parse(date)
            sdf.applyPattern("yyyy")
            def yearTest = sdf.format(testdate)
            if (yearTest >= "2010" && yearTest <= "2030") {
                year = yearTest
                return true
            }
        }
        catch (ParseException e) {
        }
    }
    return year
}


def findPlugins(def directoryname, def pathSep, def appBranch) {
    def plugins = new File(directoryname.toString() + pathSep + "plugins")
    def pluginPaths = []
    plugins.eachDir { plugin ->
        if (plugin.isDirectory()) {
            def pluginName = plugin.toString().split(pathSep)[-1]

            def command2 = "git rev-parse --abbrev-ref HEAD"
            def proc2 = command2.execute(null, plugin)
            proc2.waitFor()
            def branches = proc2?.in?.text?.readLines()?.collect {
                it.replaceAll(/[a-z0-9]*\trefs\/heads\//, '')
            }
            def branch = branches[0]
            if (branch == appBranch) {
                pluginPaths.add(plugin)
            } else {
                if (branch == "HEAD") {
                    // get detached head
                    def headline = ["git", "reflog", "-1"].execute(null, plugin).text.trim()
                    def head = headline?.toString().split(" ")[0]
                    def headlog = ['git', 'reflog', 'show', '--all'].execute(null, plugin).text.trim()
                    def headinfo = headlog?.readLines()?.collect {
                        it.replaceAll(/[a-z0-9]*\trefs\/heads\//, '')
                    }
                    headinfo.each {
                        if (it.toString() =~ appBranch) {
                            pluginPaths.add(plugin)
                            return true
                        }
                    }
                }
            }
        }
    }
    return pluginPaths
}