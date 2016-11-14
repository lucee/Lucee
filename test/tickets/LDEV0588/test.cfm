<cfsetting enablecfoutputonly="true">
<cfparam name="FORM.Scene" default="GET">
<cfhttp method="#FORM.Scene#" url="http://#CGI.SERVER_NAME#/test/testcases/LDEV0588/test1.cfm" result="result">
</cfhttp>
<cfoutput>#result.FileContent#</cfoutput>