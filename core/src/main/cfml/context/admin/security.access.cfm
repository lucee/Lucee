<cfif request.admintype EQ "web"><cflocation url="#request.self#" addtoken="no"></cfif>

<cfscript>
function isYes(accessor) {
	return accessor EQ ACCESS.YES;
}

function fb(key) {
	return StructKeyExists(form,key) and form[key];
}

function getFileAccessPath() {
	var arr=array();
	var index=1;
	while(StructKeyExists(form,'path_'&index)){
		arr[arrayLen(arr)+1]=form['path_'&index];
		index++;
	}
	return arr;
}
</cfscript>

<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">
<cfset error.message="">
<cfset error.detail="">

<script>
function checkTheBox(field) {
	var apendix=field.name.split('_')[1];
	var box=field.form['row_'+apendix];
	box.checked=true;
}
</script>
<cftry>
	<cfswitch expression="#url.action2#">
	<!--- UPDATE --->
		<cfcase value="updateDefaultSecurityManager">
        	
			<cfadmin 
				action="updateDefaultSecurityManager"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				
				setting="#fb('defaultSetting')#"
				file="#form.defaultFile#"
                file_access="#getFileAccessPath()#"
				direct_java_access="#fb('defaultDirectJavaAccess')#"
				mail="#fb('defaultMail')#"
				datasource="#form.defaultDatasource#"
				mapping="#fb('defaultMapping')#"
				remote="#fb('defaultRemote')#"
				custom_tag="#fb('defaultCustomTag')#"
				cfx_setting="#fb('defaultCfxSetting')#"
				cfx_usage="#fb('defaultCfxUsage')#"
				debugging="#fb('defaultDebugging')#"
				search="#fb('defaultSearch')#"
				scheduled_task="#fb('defaultScheduledTask')#"
				tag_execute="#fb('defaultTagExecute')#"
				tag_import="#fb('defaultTagImport')#"
				tag_object="#fb('defaultTagObject')#"
				tag_registry="#fb('defaultTagRegistry')#"
				cache="#fb('defaultCache')#"
				gateway="#fb('defaultGateway')#"
				orm="#fb('defaultOrm')#"
				access_read="#form.defaultaccess_read#"
				access_write="#form.defaultaccess_write#"
			remoteClients="#request.getRemoteClients()#">
			
		</cfcase>
		<cfcase value="updateSecurityManager">
			<cfadmin 
				action="updateSecurityManager"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				
				id="#url.id#"
				setting="#fb('defaultSetting')#"
				file="#form.defaultFile#"
				file_access="#getFileAccessPath()#"
				direct_java_access="#fb('defaultDirectJavaAccess')#"
				mail="#fb('defaultMail')#"
				datasource="#form.defaultDatasource#"
				mapping="#fb('defaultMapping')#"
				remote="#fb('defaultRemote')#"
				custom_tag="#fb('defaultCustomTag')#"
				cfx_setting="#fb('defaultCfxSetting')#"
				cfx_usage="#fb('defaultCfxUsage')#"
				debugging="#fb('defaultDebugging')#"
				search="#fb('defaultSearch')#"
				scheduled_task="#fb('defaultScheduledTask')#"
				tag_execute="#fb('defaultTagExecute')#"
				tag_import="#fb('defaultTagImport')#"
				tag_object="#fb('defaultTagObject')#"
				tag_registry="#fb('defaultTagRegistry')#"
				cache="#fb('defaultCache')#"
				gateway="#fb('defaultGateway')#"
				orm="#fb('defaultOrm')#"
				access_read="#form.defaultaccess_read#"
				access_write="#form.defaultaccess_write#">
			
		</cfcase>
		<cfcase value="createSecurityManager">
			<cfadmin 
				action="createSecurityManager"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				
				id="#form.id#">
		</cfcase>
		<cfcase value="removeSecurityManager">
			<cfset data.ids=toArrayFromForm("ids")>
				<cfloop index="idx" from="1" to="#arrayLen(data.ids)#">
					<cfif arrayIndexExists(data.ids,idx)>
					<cfadmin 
						action="removeSecurityManager"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						id="#data.ids[idx]#">
					</cfif>
				</cfloop>
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
	<cflocation url="#go(url.action)#" addtoken="no">
</cfif>

<!--- 
Error Output --->
<cfset printError(error)>

<cfoutput><div class="pageintro">#stText.Security.desc#</div></cfoutput>

<cfset prefix="default">
<cfset tabs=structNew("linked")>
<cfset tabs.generell=stText.Security.tabGeneral>
<cfset tabs.special=stText.Security.tabSpecial>
<cfmodule template="tabbedPane.cfm" name="sec" tabs="#tabs#" default="generell">
	<cfmodule template="tab.cfm" name="generell">
		<!---
		<cfoutput><div class="itemintro">#stText.Security.generalDesc#</div></cfoutput>
		--->
		<cfset type="generell">
		<cfadmin 
			action="getDefaultSecurityManager"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"
			returnVariable="access">
            
		<cfinclude template="security.access.form.cfm">
	</cfmodule>
	<cfmodule template="tab.cfm" name="special">
		<cfoutput><div class="itemintro">#stText.Security.specialDesc#</div></cfoutput>
		<cfset type="special">
		<cfinclude template="security.access.special.cfm">
	</cfmodule>
</cfmodule>
