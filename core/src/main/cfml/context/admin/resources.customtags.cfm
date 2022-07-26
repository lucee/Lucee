<cfset stText.CustomTags.name="Name">
<cfset stText.CustomTags.nameMissing="Missing the name for the new Mapping, this name is used when you deploy a Lucee Archive (.lar) based on this Mapping.">
<cfset stText.CustomTags.nameDesc="The name is used as identifier when you automatically import a Lucee Archive build based on this Mapping.">
<cfset stText.CustomTags.PhysicalDesc="Directory path where the custom tags are located.">
<cfset stText.CustomTags.archiveDesc="File path to a custom tag Lucee Archive (.lar).">
<cfset stText.CustomTags.PrimaryDesc="Defines where Lucee looks first for a requested custom tags">
<cfset stText.CustomTags.trustedDesc="When does Lucee checks for changes in the source file for an already loaded custom tags">


<!--- <cfif isDefined("form")>
	<cfinclude template="act/resources.act_mapping.cfm">
</cfif> --->
<cfset error.message="">
<cfset error.detail="">
<cfparam name="url.action2" default="list">
<cfscript>
	function arrayRemoveValue(arr,value){
		var index=arrayFindNoCase(arr,value);
		if(index GT 0)ArrayDeleteAt(arr,index);
	}
</cfscript>

<!--- Defaults --->
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">
<cfparam name="error" default="#struct(message:"",detail:"")#">

<!--- <cfset hasAccess=securityManager.getAccess("custom_tag") EQ ACCESS.YES> --->
<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="hasAccess"
	secType="custom_tag"
	secValue="yes">


<cfset flushName="#stText.Buttons.flush# (#structCount(ctCacheList())#)">
<cftry>
	<cfswitch expression="#form.mainAction#">
		<!--- UPDATE --->
		<cfcase value="#flushName#">
			 <cfset ctCacheClear()>
		</cfcase>
		<!--- update --->
		<cfcase value="#stText.Buttons.Update#">
			
			
			
            <!--- create Archive --->
			<cfset doDownload=form.subAction EQ stText.Buttons.downloadArchive>
			<cfif doDownload or form.subAction EQ stText.Buttons.addArchive>
            	
				<cfsetting requesttimeout="3000">		
				<cfset data.virtuals=toArrayFromForm("virtual")>
				<cfset data.addCFMLFiles=toArrayFromForm("addCFMLFiles")>
				<cfset data.addNonCFMLFiles=toArrayFromForm("addNonCFMLFiles")>
				<cfset data.rows=toArrayFromForm("row")>
				
				<cfloop index="idx" from="1" to="#arrayLen(data.virtuals)#">
					<cfif isDefined("data.rows[#idx#]") and data.virtuals[idx] NEQ "">
						<cfset data.addNonCFMLFiles[idx]=isDefined("data.addNonCFMLFiles[#idx#]") and data.addNonCFMLFiles[idx]>
						<cfset data.addCFMLFiles[idx]=isDefined("data.addCFMLFiles[#idx#]") and data.addCFMLFiles[idx]>
					
					<cfset ext='lar'>
					<cfset target=getTempDirectory() & Rand() & "."&ext>
					<cfset filename=data.virtuals[idx]>
					<cfset filename=mid(filename,2,len(filename))>
					
					
					<cfadmin 
						action="getInfo"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						returnVariable="info">
					
					<cfif len(filename)>
						<cfset filename="ct-archive-"&filename&"."&ext>
					<cfelse>
						<cfset filename="ct-archive-root."&ext>
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
						action="createCTArchive"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						file="#target#"
						virtual="#data.virtuals[idx]#"
						addCFMLFiles="#data.addCFMLFiles[idx]#"
						addNonCFMLFiles="#data.addNonCFMLFiles[idx]#"
						append="#not doDownload#"
						remoteClients="#request.getRemoteClients()#">
						<cfif doDownload><cfheader name="Content-Disposition" value="inline; filename=#filename#"><!---
						 ---><cfcontent file="#target#" deletefile="yes" type="application/unknow"></cfif>
					</cfif>
				</cfloop>
			
			
			
			
			
		 <!--- compile mapping --->
			<cfelseif form.subAction EQ "#stText.Buttons.compileAll#">
            
				<cfsetting requesttimeout="3000">		
				<cfset data.virtuals=toArrayFromForm("virtual")>
				<cfset data.stoponerrors=toArrayFromForm("stoponerror")>
				<cfset data.rows=toArrayFromForm("row")>
				
				<cfloop index="idx" from="1" to="#arrayLen(data.virtuals)#">
				
					<cfif isDefined("data.rows[#idx#]") and data.virtuals[idx] NEQ "">
						<cfset data.toplevels[idx]=isDefined("data.toplevels[#idx#]") and data.toplevels[idx]>
						<cfset data.stoponerrors[idx]=isDefined("data.stoponerrors[#idx#]") and data.stoponerrors[idx]>
					
					<cfadmin 
						action="compileCTMapping"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						virtual="#data.virtuals[idx]#"
						stoponerror="#data.stoponerrors[idx]#"
			remoteClients="#request.getRemoteClients()#">
						
					</cfif>
				</cfloop>
  


			
		<!--- setting --->
			<cfelseif form.subAction EQ "setting">
				<cfif form.extensions EQ "custom">
					<cfset form.extensions=form.extensions_custom>
				</cfif>
			
				<cfadmin 
						action="updateCustomTagSetting"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						deepSearch="#isDefined('form.customTagDeepSearchDesc') and form.customTagDeepSearchDesc EQ true#"
						localSearch="#isDefined('form.customTagLocalSearchDesc') and form.customTagLocalSearchDesc EQ true#"
						customTagPathCache="#isDefined('form.customTagPathCache') and form.customTagPathCache EQ true#"
						
						
						
						extensions="#form.extensions#"
			remoteClients="#request.getRemoteClients()#">
			<cfelseif form.subAction EQ "#stText.Buttons.Update#">
				<cfset data.names=toArrayFromForm("name")>
				<cfset data.virtuals=toArrayFromForm("virtual")>
				<cfset data.physicals=toArrayFromForm("physical")>
				<cfset data.archives=toArrayFromForm("archive")>
				<cfset data.primaries=toArrayFromForm("primary")>
				<cfset data.inspects=toArrayFromForm("inspect")>
				<cfset data.rows=toArrayFromForm("row")>
				
				<cfloop index="idx" from="1" to="#arrayLen(data.physicals)#">
					<cfif isDefined("data.rows[#idx#]")>
						<cfset data.inspects[idx]=isDefined("data.inspects[#idx#]")?data.inspects[idx]:"">
					
					<cfset name=data.names[idx]?:"">
					<cfset virtual=trim(data.virtuals[idx]?:"")>
					<cfif len(name)>
						<cfset virtual="/"&name>
					</cfif>
					
					<cfadmin 
						action="updateCustomTag"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						virtual="#virtual#"
						physical="#data.physicals[idx]#"
						archive="#data.archives[idx]#"
						primary="#data.primaries[idx]#"
						inspect="#data.inspects[idx]#"
			remoteClients="#request.getRemoteClients()#">

					</cfif>
				</cfloop>
		
			<cfelseif form.subAction EQ "#stText.Buttons.Delete#">
				<cfset data.virtuals=toArrayFromForm("virtual")>
				<cfset data.rows=toArrayFromForm("row")>
				
				<cfloop index="idx" from="1" to="#arrayLen(data.virtuals)#">
					
					<cfif isDefined("data.rows[#idx#]") and data.virtuals[idx] NEQ "">
						<cfadmin 
							action="removeCustomTag"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							
							virtual="#data.virtuals[idx]#"
			remoteClients="#request.getRemoteClients()#">
					
						<!--- <cfset admin.removeCustomTag(data.virtuals[idx])> --->
					</cfif>
				</cfloop>
			</cfif>
		</cfcase>
	</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
		<cfset error.cfcatch=cfcatch>
	</cfcatch>
</cftry>

<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>


<cfadmin 
	action="getCustomTagMappings"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="mappings">
	
	

<cfadmin 
	action="getCustomtagSetting"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="setting">

<!--- Error Output --->
<cfset printError(error)>

<cfif url.action2 EQ "create">
	<cfinclude template="resources.customtags.edit.cfm">
<cfelse>
	<cfinclude template="resources.customtags.list.cfm">
</cfif>
