<cfset _img=structKeyExists(url,"img") ? createObject("java","lucee.commons.io.res.util.ResourceUtil").toFileName(url.img,"") : "">
<cfif len(_img) && structKeyExists(url,"type") && fileExists("#GetTempDirectory()#/graph/#_img#")>
	<cfcontent file="#GetTempDirectory()#/graph/#_img#" type="image/#url.type#"><cfsetting showdebugoutput="no">
<cfelse>
	<cfheader statuscode="404" statustext="Invalid Access">
</cfif>