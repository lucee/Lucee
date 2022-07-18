<cftry>
	<cfset stVeritfyMessages = StructNew()>
	<cfswitch expression="#form.mainAction#">
	<!--- delete --->
		<cfcase value="#stText.Buttons.Delete#">
			<cfset data.names=toArrayFromForm("name")>
			<cfset data.rows=toArrayFromForm("row")>
			<cfloop index="idx" from="1" to="#arrayLen(data.names)#">
				<cfif isDefined("data.rows[#idx#]") and data.names[idx] NEQ "">
					<cfadmin 
						action="removeLogSetting"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						name="#data.names[idx]#"
						remoteClients="#request.getRemoteClients()#">
					
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
<cfif cgi.request_method EQ "POST" and error.message EQ "" and form.mainAction neq stText.Buttons.verify>
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>
<cfscript>
	hasReadOnly = false;
	hasReadable = false;
	loop query="logs"{
		if (logs.readonly)
			hasReadOnly = true;
		else
			hasReadable = true;
	}
	QuerySort( logs, "name" );
</cfscript>

<!---
<cfloop query="connections">
	<cfif not connections.readOnly>
		<cfset tmp=srcLocal>
	<cfelse>
		<cfset tmp=srcGlobal>
	</cfif>
	<cfset QueryAddRow(tmp)>
	<cfset QuerySetCell(tmp,"name",connections.name)>
	<cfset QuerySetCell(tmp,"class",connections.class)>
	<cfset QuerySetCell(tmp,"custom",connections.custom)>
	<cfset QuerySetCell(tmp,"default",connections.default)>
	<cfset QuerySetCell(tmp,"storage",connections.storage)>
	<cfset QuerySetCell(tmp,"readonly",connections.readonly)>
</cfloop>
<cfset querySort(connections,"default")>
---->
<cfoutput>

	<cfif access NEQ "yes">
		<cfset noAccess(stText.Settings.cache.noAccess)>
	</cfif>
	
	<!---- READ ONLY ---->
	<cfif request.adminType EQ "web" and hasReadOnly>
		<h2>#stText.Settings.logging.readOnlyTitle#</h2>
		<div class="itemintro">#stText.Settings.logging.readOnlyDesc#</div>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<table class="maintbl checkboxtbl">
				<thead>
					<tr>
						<th width="30%">#stText.Settings.logging.name#</th>
						<th width="30%">#stText.Settings.logging.appender#</th>
						<th width="30%">#stText.Settings.logging.layout#</th>
						<th>#stText.Settings.logging.level#</th>
					</tr>
				</thead>
				<tbody>
					<cfloop query="logs">
						<cfif !logs.readonly><cfcontinue/></cfif>
						
						<cfset appender=appenders[logs.appenderClass]?:nullValue()>
						<cfset showLayout=isNull(appender.getLayout)>
						<cfset layout=layouts[logs.layoutClass]?:nullValue()>

						<tr>
							<td>#logs.name#
								<input type="hidden" name="name_#logs.currentrow#" value="#logs.name#">
							</td>
							<td>#isNull(appender)?listLast(logs.appenderClass,'/'):appender.getLabel()#</td>
							<td><cfif showLayout>#isNull(layout)?listLast(logs.layoutClass,'.'):layout.getLabel()#<cfelse>&nbsp;</cfif></td>
							<td>#logs.level#</td>
						</tr>
					</cfloop>
				</tbody>
			</table>
		</cfformClassic>
	</cfif>

	<!--- LIST CACHE --->
	<cfif logs.recordcount and hasReadable>
		<h2>#stText.Settings.logging.title#</h2>
		<div class="itemintro">#stText.Settings.logging.desc#</div>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<table class="maintbl checkboxtbl">
				<thead>
					<tr>
						<th width="1%"><input type="checkbox" class="checkbox" name="rowreadonly" onclick="selectAll(this)"></th>
						<th>#stText.Settings.logging.name#</th>
						<th>#stText.Settings.logging.appender#</th>
						<th>#stText.Settings.logging.layout#</th>
						<th>#stText.Settings.logging.level#</th>
						<th width="3%"></th>
					</tr>
				</thead>
				<tbody>
					<cfloop query="logs">
						<cfif logs.readonly><cfcontinue/></cfif>
						
						<cfset appender=appenders[logs.appenderClass]?:nullValue()>
						<cfset showLayout=isNull(appender.getLayout)>
						<cfset layout=layouts[logs.layoutClass]?:nullValue()>
						
						<tr>
							<td>
								<input type="checkbox" class="checkbox" name="row_#logs.currentrow#" value="#logs.currentrow#">
							</td>
							<td nowrap><input type="hidden" name="name_#logs.currentrow#" value="#logs.name#">#logs.name#</td>
							<td>#isNull(appender)?listLast(logs.appenderClass,'.'):appender.getLabel()#</td>
							<td><cfif showLayout>#isNull(layout)?listLast(logs.layoutClass,'.'):layout.getLabel()#<cfelse>&nbsp;</cfif></td>
							<td>#logs.level#</td>
							<td>
								<cfif !isNull(appender)><a href="#request.self#?action=#url.action#&action2=create&name=#Hash(logs.name)#" class="sprite edit"></a></cfif>
							</td>
						</tr>
					</cfloop>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="4">
							<input type="submit" class="bl button submit enablebutton" name="mainAction" value="#stText.Buttons.delete#">
							<input type="reset" class="br button reset enablebutton" id="clickCancel" name="cancel" value="#stText.Buttons.Cancel#">
						</td>	
					</tr>
				</tfoot>
			 </table>
		</cfformClassic>
		
	</cfif>
</cfoutput>
<script>
function defaultValue(field) {
	var form=field.form;
	for(var i=0;i<form.elements.length;i++){
		var f=form.elements[i];
		if(f.name.substring(0,8)=='default_' && field.name!=f.name && f.value==field.value) {
			f.selectedIndex = 0;
		}
	}
}
</script>

<!--- 
	Create Logger --->
<cfif access EQ "yes">
	<cfoutput>
		<cfset _layouts=ListSort(StructKeyList(layouts),'textnocase')>
		<cfset _appenders=ListSort(StructKeyList(appenders),'textnocase')>
		<cfif listLen(_appenders) and listLen(_layouts)>
			<h2>#stText.Settings.logging.titleCreate#</h2>
			<cfformClassic onerror="customError" action="#request.self#?action=#url.action#&action2=create" method="post">
				<table class="maintbl" style="width:400px;">
					<tbody>
						<tr>
							<th scope="row" nowrap="nowrap">#stText.Settings.cache.Name#</th>
							<td><cfinputClassic type="text" name="_name" value="" class="xlarge" required="yes" 
								message="#stText.Settings.cache.nameMissing#"></td>
						</tr>
						<tr>
							<th scope="row">#stText.Settings.logging.appender#</th>
							<td>
								<select name="appenderClass" class="xlarge">
									<cfloop collection="#appenders#" index="key" item="appender">
										<cfset v=trim(appender.getClass())>
										<option value="#v#">#trim(appender.getLabel())#</option>
									</cfloop>
								</select>
							</td>
						</tr>
						<tr>
							<th scope="row">#stText.Settings.logging.layout#</th>
							<td>
								<select name="layoutClass" class="xlarge">
									<cfloop collection="#layouts#" index="key" item="layout">
										<cfset v=trim(layout.getClass())>
										<option value="#v#">#trim(layout.getLabel())#</option>
									</cfloop>
								</select>
							</td>
						</tr>
					</tbody>
					<tfoot>
						<tr>
							<td colspan="2">
								<input type="submit" class="bl button submit" name="run" value="#stText.Buttons.create#">
								<input type="reset" class="br button reset" name="cancel" value="#stText.Buttons.Cancel#">
							</td>
						</tr>
					</tfoot>
				</table>   
			</cfformClassic>
		<cfelse>
			<div class="text">#stText.Settings.logging.noDriver#</div>
		</cfif>
	</cfoutput>
</cfif>
