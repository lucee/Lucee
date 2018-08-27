<cfoutput>
	<cffile action="write" file="#expandPath('{temp-directory}/admin-ext-thumbnails/')#\__#url.file#" Output="#form.imgSrc#" createPath="true">
</cfoutput>