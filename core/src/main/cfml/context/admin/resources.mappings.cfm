<!--- <cfif isDefined("form")>
	<cfinclude template="act/resources.act_mapping.cfm">
</cfif> --->
<cfset error.message="">
<cfset error.detail="">

<!--- 
Defaults --->
<cfparam name="form.mainAction" default="none">
<cfparam name="url.action2" default="list">
<cfparam name="form.subAction" default="none">
<cfparam name="error" default="#struct(message:"",detail:"")#">

<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="hasAccess"
	secType="mapping"
	secValue="yes">

<cftry>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="#stText.Buttons.save#">
		<!--- compile all --->
			<cfif form.subAction EQ "#stText.Buttons.compileAll#">
				<cfsetting requesttimeout="3000">		
				<cfset data.virtuals=toArrayFromForm("virtual")>
				<cfset data.stoponerrors=toArrayFromForm("stoponerror")>
				<cfset data.rows=toArrayFromForm("row")>
				
				<cfloop index="idx" from="1" to="#arrayLen(data.virtuals)#">
					<cfif isDefined("data.rows[#idx#]") and data.virtuals[idx] NEQ "">
						<cfset data.stoponerrors[idx]=isDefined("data.stoponerrors[#idx#]") and data.stoponerrors[idx]>
					
					<cfadmin 
						action="compileMapping"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						virtual="#data.virtuals[idx]#"
						stoponerror="#data.stoponerrors[idx]#"
			remoteClients="#request.getRemoteClients()#">
						
					</cfif>
				</cfloop>
			</cfif>
		<!--- downloadArchive --->
			<cfset doDownload=form.subAction EQ stText.Buttons.downloadArchive>
			<cfif doDownload or form.subAction EQ stText.Buttons.addArchive>
				<cfsetting requesttimeout="3000">		
				<cfset data.virtuals=toArrayFromForm("virtual")>
				<cfset data.addCFMLFiles=toArrayFromForm("addCFMLFiles")>
				<cfset data.addNonCFMLFiles=toArrayFromForm("addNonCFMLFiles")>
				<cfset data.rows=toArrayFromForm("row")>
				
				<cfloop index="idx" from="1" to="#arrayLen(data.virtuals)#">
					<cfif isDefined("data.rows[#idx#]") and data.virtuals[idx] NEQ "">
						<cfset data.addCFMLFiles[idx]=isDefined("data.addCFMLFiles[#idx#]") and data.addCFMLFiles[idx]>
						<cfset data.addNonCFMLFiles[idx]=isDefined("data.addNonCFMLFiles[#idx#]") and data.addNonCFMLFiles[idx]>
					
					<cfset ext="lar">
					<cfset target=getTempDirectory() & Rand() & "."&ext>
					<cfset filename=data.virtuals[idx]>
					<cfset filename=mid(filename,2,len(filename))>
					<cfif len(filename)>
						<cfset filename="archive-"&filename&"."&ext>
					<cfelse>
						<cfset filename="archive-root."&ext>
					</cfif>
					<cfset filename=Replace(filename,"/","-","all")>
					
					
					<cfif not doDownload>
						<cfset target=expandPath("#cgi.context_path#/lucee/archives/"&filename)>
						<cfset count=0>
						<cfwhile fileExists(target)>
							<cfset count=count+1>
							<cfset target="#cgi.context_path#/lucee/archives/"&filename>
							<cfset target=replace(target,'.'&ext,count&'.'&ext)>
							<cfset target=expandPath(target)>
						</cfwhile>
					</cfif>
					<cfadmin 
						action="createArchive"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						file="#target#"
						virtual="#data.virtuals[idx]#"
						addCFMLFiles="#data.addCFMLFiles[idx]#"
						addNonCFMLFiles="#data.addNonCFMLFiles[idx]#"
						append="#not doDownload#"
						remoteClients="#request.getRemoteClients()#">
						<cfif not doDownload>
							
						<!---<cfadmin 
							action="getMapping"
							type="#request.adminType#"
							virtual="#data.virtuals[idx]#"
							password="#session["password"&request.adminType]#"
							returnVariable="mapping">
						
						<cfadmin 
							action="updateMapping"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							
							virtual="#mapping.virtual#"
							physical="#mapping.strPhysical#"
							archive="#target#"
							primary="#iif(mapping.physicalFirst,de('physical'),de('archive'))#"
							trusted="#mapping.trusted#"
							toplevel="#mapping.toplevel#"
			remoteClients="#request.getRemoteClients()#">--->
						
						<cfelse><cfheader name="Content-Disposition" value="inline; filename=#filename#"><!---
						 ---><cfcontent file="#target#" deletefile="yes" type="application/unknow"></cfif>
					</cfif>
				</cfloop>
			</cfif>
		<!--- update --->
			<cfif form.subAction EQ "#stText.Buttons.save#">
							
				<cfset data.physicals=toArrayFromForm("physical")>
				<cfset data.virtuals=toArrayFromForm("virtual")>
				<cfset data.archives=toArrayFromForm("archive")>
				<cfset data.primaries=toArrayFromForm("primary")>
				<cfset data.inspects=toArrayFromForm("inspect")>
				<cfset data.toplevels=toArrayFromForm("toplevel")>
				<cfset data.rows=toArrayFromForm("row")>
				
				<cfloop index="idx" from="1" to="#arrayLen(data.virtuals)#">
					<cfif isDefined("data.rows[#idx#]") and data.virtuals[idx] NEQ "">
						<cfset data.inspects[idx]=isDefined("data.inspects[#idx#]")?data.inspects[idx]:"">
						<cfset data.toplevels[idx]=isDefined("data.toplevels[#idx#]") and data.toplevels[idx]>
					<cfadmin 
						action="updateMapping"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						virtual="#data.virtuals[idx]#"
						physical="#data.physicals[idx]#"
						archive="#data.archives[idx]#"
						primary="#data.primaries[idx]#"
						inspect="#data.inspects[idx]#"
						toplevel="#data.toplevels[idx]#"
			remoteClients="#request.getRemoteClients()#">
						
					</cfif>
				</cfloop>
		<!--- delete --->
			<cfelseif form.subAction EQ "#stText.Buttons.Delete#">
				<cfset data.virtuals=toArrayFromForm("virtual")>
				<cfset data.rows=toArrayFromForm("row")>
				
				<cfloop index="idx" from="1" to="#arrayLen(data.virtuals)#">
					
					<cfif isDefined("data.rows[#idx#]") and data.virtuals[idx] NEQ "">
					
					<cfadmin 
						action="removeMapping"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						virtual="#data.virtuals[idx]#"
			remoteClients="#request.getRemoteClients()#">
					</cfif>
				</cfloop>
			</cfif>
		</cfcase>
	</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
	</cfcatch>
</cftry>

<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<!--- <cfif isDefined("url.action2")>
		<cfif isDefined("url.virtual")>
			<cflocation url="#request.self#?action=#url.action#&action2=#url.action2#&virtual=#url.virtual#" addtoken="no">
		</cfif>
		<cflocation url="#request.self#?action=#url.action#&action2=#url.action2#" addtoken="no">
	</cfif> --->
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<!--- 
Error Output--->
<cfif error.message NEQ "">
<cfoutput><span class="CheckError">
#replace(error.message,chr(10),'<br />','all')#
#replace(error.detail,chr(10),'<br />','all')#
</span><br><br></cfoutput>
</cfif>

<cfadmin 
	action="getMappings"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="mappings">

<cfif url.action2 EQ "create">
	<cfinclude template="resources.mappings.edit.cfm">
<cfelse>
	<cfinclude template="resources.mappings.list.cfm">
</cfif>