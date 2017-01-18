<cfsetting enablecfoutputonly="true">
<cfparam name="FORM.Scene" default="1">
<cfif FORM.Scene EQ 1>
	<cfhttp method="get" url="http://#CGI.SERVER_NAME#/test/testcases/LDEV0580/thread-test.cfm" result="result" charset="utf-8">
	<cfoutput>#result.filecontent#</cfoutput>
<cfelse>
	<cfschedule
		action="update"
		task="thread-test"
		operation="HTTPRequest"
		startDate="5/12/2016"
		startTime="7:00 AM"
		url="http://#CGI.SERVER_NAME#/test/testcases/LDEV0580/thread-test.cfm"
		interval="daily"
		publish = "Yes"
		file = "file.log"
		path = "#expandPath('./')#" />

	<cfschedule action = "run"
		task = "thread-test">

	<cffile action="read" file="#expandPath('./')#/file.log" variable="MyContent">
	<cfoutput>#MyContent#</cfoutput>
</cfif>