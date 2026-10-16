<!--- Image extension handler for extension logo processing --->
<cfif isImage(data)>
	<cfset local.img=imageRead(data)>
	<!--- shrink images if needed --->
	<cfif img.height GT arguments.height || img.width GT arguments.width>
		<cfif img.height GT arguments.height >
			<cfset imageResize(img,"",arguments.height)>
		</cfif>
		<cfif img.width GT arguments.width>
			<cfset imageResize(img,arguments.width,"")>
		</cfif>
	</cfif>
	<!--- we go this way to influence the quality of the image
		and cache the local file
	--->
	<cfset imagewrite(image:img,destination:tmpfile)>
	<cfset local.b64=toBase64(fileReadBinary(tmpfile))>
</cfif>
