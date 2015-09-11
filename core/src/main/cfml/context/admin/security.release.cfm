<cfif request.adminType EQ "web">
	<cfthrow message="no access to this functionality">
</cfif>
<cfscript>
function isYes(accessor) {
	return accessor EQ ACCESS.YES;
}

function fb(key) {
	return StructKeyExists(form,key) and form[key];
}
</cfscript>




<cfadmin 
	action="getContexts"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="contextes">
		<cfsilent>
		<cfset size=0>
		<cfloop collection="#contextes#" item="idx">
				<cfif len(contextes[idx].label)><cfset path=contextes[idx].label&" ("&contextes[idx].path&")"><cfelse><cfset path=contextes[idx].path></cfif>
				<cfset contextes[idx].text=path>
				<cfif size LT len(path)><cfset size=len(path)></cfif>
		</cfloop>
		</cfsilent>
					

<!--- <cfset webs=config.configWebs>
<cfset factories=config.jspFactories>
<cfset keys=StructKeyArray(factories)> --->
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
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="defaultUdpate">
			<cfadmin 
				action="updateDefaultSecurity"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				
				setting="#fb('defaultSetting')#"
				file="#form.defaultFile#"
				direct_java_access="#fb('defaultDirectJavaAccess')#"
				mail="#fb('defaultMail')#"
				datasource="#fb('defaultDatasource')#"
				mapping="#fb('defaultMapping')#"
				custom_tag="#fb('defaultCustomTag')#"
				cfx_setting="#fb('defaultCfxSetting')#"
				cfx_usage="#fb('defaultCfxUsage')#"
				debugging="#fb('defaultDebugging')#"
				tag_execute="#fb('defaultTagExecute')#"
				tag_import="#fb('defaultTagImport')#"
				tag_object="#fb('defaultTagObject')#"
				tag_registry="#fb('defaultTagRegistry')#"
				cache="#fb('defaultCache')#"
				gateway="#fb('defaultGateway')#">
			
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
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<!--- 
Error Output --->
<cfset printError(error)>


<cfadmin 
	action="defaultSecurityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="access">

<cfset prefix="default">

















<cfif structkeyExists(form,'web_access')>
	<cfset access.web_access=form.web_access>
</cfif>
<cfparam name="access.web_access" default="#false#" type="boolean">

<cfset qry=querynew("id,label,access")>
	<cfset QueryAddRow(qry ,3)>
		<cfset qry['id'][1]="1">
		<cfset qry['label'][1]="www.lucee.org">
		<cfset qry['access'][1]=true>
		
		<cfset qry['id'][2]="2">
		<cfset qry['label'][2]="www.lucee2.org">
		<cfset qry['access'][2]=true>
		
		<cfset qry['id'][3]="3">
		<cfset qry['label'][3]="www.lucee3.org">
		<cfset qry['access'][3]=false>
<cfoutput>
<table class="tbl" width="600">
<form action="#request.self#?action=#url.action#" method="post">
<tr>
	<td colspan="2">Definieren Sie hier, wie der Zugriff der einzelnen Webs auf Lucee prinzipiell geregelt ist.</td>
</tr>
<tr>
	<td colspan="2">
	<select name="web_access">
	<option value="yes" <cfif access.web_access>selected</cfif>>Alle Webs duerfen Lucee verwenden</option>
	<option value="no" <cfif not access.web_access>selected</cfif>>Nur folgende Webs duerfen Lucee verwenden</option>
</select>
<input type="submit" class="button submit" name="subAction" value="#stText.Buttons.Update#">
	</td>
</tr>
</form>
<tr>
	<td colspan="2">
	
<br><br>
	
<cfif access.web_access>
<h2>Einzelne Webs freigeben</h2>
Alle Webs k￶oennen Lucee verwenden.
<cfelse>
<cfoutput><form action="#cgi.script_name#" method="post"></cfoutput>
<table class="tbl">
<tr>
	<td colspan="2">
	<h2>Einzelne Webs freigeben</h2>
	Nur die Webs, welche hier eine explizite Freigabe haben, k￶oennen Lucee auch verwenden.
	</td>
</tr>
<tr>
	<th scope="row">Freigegeben</th>
	<th scope="row">Web</th>
</tr>
<tr>
	<th scope="row"><input type="Checkbox" class="checkbox" name="selection" value="#qry.id#" onclick="doOthers(this)"></th>
	<th scope="row">&nbsp;</th>
</tr>
<cfoutput><cfloop collection="#contextes#" item="key">
<tr>
	<td align="center"><input type="Checkbox" class="checkbox" name="access" value="#qry.id#" <cfif qry.access>checked</cfif>></td>
	<td>#contextes[key].text#</td>
</tr>
</cfloop></cfoutput>
</table></form>
</cfif>
	</td>
</tr>
</table>
</cfoutput>
