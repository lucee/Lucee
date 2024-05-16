<cfif structKeyExists(url,"img") && structKeyExists(url,"type") && fileExists("#GetTempDirectory()#/graph/#listLast(url.img,'/\#server.separator.file#')#")>
	<cfcontent file="#GetTempDirectory()#/graph/#listLast(url.img,'/\#server.separator.file#')#" type="image/#url.type#"><cfsetting showdebugoutput="no">
<cfelse>
	<cfheader statuscode="404" statustext="Invalid Access">
</cfif>