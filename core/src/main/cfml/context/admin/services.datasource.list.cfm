<cftry>
	<cfset stVeritfyMessages = StructNew()>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="#stText.Buttons.Delete#">
			<cfset data.names=toArrayFromForm("name")>
				<cfset data.rows=toArrayFromForm("row")>
				<cfset data.names=toArrayFromForm("name")>
				
				<cfloop index="idx" from="1" to="#arrayLen(data.names)#">
					<cfif isDefined("data.rows[#idx#]") and data.names[idx] NEQ "">
						<cfadmin 
							action="removeDatasource"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							name="#data.names[idx]#"
							remoteClients="#request.getRemoteClients()#">
						
					</cfif>
				</cfloop>
		</cfcase>
		<cfcase value="#stText.Buttons.verify#">
			<cfset data.names=toArrayFromForm("name")>
				<cfset data.rows=toArrayFromForm("row")>
				<cfset data.names=toArrayFromForm("name")>
				<cfset data.passwords=toArrayFromForm("password")>
				<cfset data.usernames=toArrayFromForm("username")>
				
				<cfloop index="idx" from="1" to="#arrayLen(data.names)#">
					<cfif isDefined("data.rows[#idx#]") and data.names[idx] NEQ "">
						<cftry>
							<cfadmin 
								action="verifyDatasource"
								type="#request.adminType#"
								password="#session["password"&request.adminType]#"
								name="#data.names[idx]#"
								dbusername="#data.usernames[idx]#"
								dbpassword="#data.passwords[idx]#">
								<cfset stVeritfyMessages["#data.names[idx]#"].Label = "OK">

								<cfdbinfo type="Version" datasource="#data.names[idx]#" name='stVeritfyMessages["#data.names[idx]#"].dbInfo'>
							<cfcatch>
								<!--- <cfset error.message=error.message&data.names[idx]&": "&cfcatch.message&"<br>"> --->
								<cfset stVeritfyMessages[data.names[idx]].Label = "Error">
								<cfset stVeritfyMessages[data.names[idx]].message = cfcatch.message>
							</cfcatch>
						</cftry>
					</cfif>
				</cfloop>
				
		</cfcase>
        <!--- update --->
		<cfcase value="#stText.Buttons.Update#">
			
			<cfadmin 
				action="updatePSQ"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				
				psq="#structKeyExists(form,"psq") and form.psq#"
				remoteClients="#request.getRemoteClients()#">
		</cfcase>
	<!--- reset to server setting --->
		<cfcase value="#stText.Buttons.resetServerAdmin#">
			<cfadmin 
				action="updatePSQ"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				
				psq=""
				remoteClients="#request.getRemoteClients()#">
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
<cfif cgi.request_method EQ "POST" and error.message EQ "" and form.mainAction neq stText.Buttons.verify>
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<!--- 
Error Output --->
<cfset printError(error)>

<cfif structKeyExists(url,'verified') and len(url.verified)>
	<cfset stVeritfyMessages={}>
	<cfset stVeritfyMessages[url.verified].Label = "OK">
</cfif>


<cfadmin 
	action="getDatasourceSetting"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="dbSetting">

<cfoutput>	
	<h2>#stText.Settings.DatasourceSettings#</h2>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.Settings.PreserveSingleQuotes#</th>
					<td>
						<cfif access NEQ 0>
							<input type="checkbox" class="checkbox" name="psq" value="yes" <cfif dbSetting.psq>checked</cfif>>
						<cfelse>
							<b>#yesNoFormat(dbSetting.psq)#</b>
						</cfif>
						<div class="comment">#stText.Settings.PreserveSingleQuotesDescription#</div>
					</td>
				</tr>
				<cfif access NEQ 0>
					<cfmodule template="remoteclients.cfm" colspan="2">
				</cfif>
			</tbody>
			<cfif access>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="submit" class="bl button submit" name="mainAction" value="#stText.Buttons.Update#">
							<input type="reset" class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> reset" name="cancel" value="#stText.Buttons.Cancel#">
							<cfif request.adminType EQ "web">
								<input type="submit" class="br button submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#">
							</cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>
</cfoutput>

<cfadmin 
	action="getDatasources"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="datasources">

<!--- 
list all mappings and display necessary edit fields --->


<!--- <cfset srcLocal=struct()>
<cfset srcGlobal=struct()>
<cfloop collection="#sources#" item="key">
	<cfif sources[key].isReadOnly()>
		<cfset srcGlobal[key]=sources[key]>
	<cfelse>
		<cfset srcLocal[key]=sources[key]>
	</cfif>
</cfloop> --->
<cfset querySort(datasources,"name")>
<cfset srcLocal=queryNew("name,classname,dsn,username,password,readonly,storage,openConnections,host,port")>
<cfset srcGlobal=queryNew("name,classname,dsn,username,password,readonly,storage,openConnections,host,port")>
<cfloop query="datasources">
	<cfif not datasources.readOnly>
		<cfset QueryAddRow(srcLocal)>
		<cfset QuerySetCell(srcLocal,"name",datasources.name)>
		<cfset QuerySetCell(srcLocal,"classname",datasources.classname)>
		<cfset QuerySetCell(srcLocal,"dsn",datasources.dsn)>
		<cfset QuerySetCell(srcLocal,"username",datasources.username)>
		<cfset QuerySetCell(srcLocal,"password",datasources.password)>
		<cfset QuerySetCell(srcLocal,"openConnections",datasources.openConnections)>
		<cfset QuerySetCell(srcLocal,"readonly",datasources.readonly)>
		<cfset QuerySetCell(srcLocal,"storage",datasources.storage)>
		<cfset QuerySetCell(srcLocal,"host",datasources.host?:'')>
		<cfset QuerySetCell(srcLocal,"port",datasources.port?:'')>
	<cfelse>
		<cfset QueryAddRow(srcGlobal)>
		<cfset QuerySetCell(srcGlobal,"name",datasources.name)>
		<cfset QuerySetCell(srcGlobal,"classname",datasources.classname)>
		<cfset QuerySetCell(srcGlobal,"dsn",datasources.dsn)>
		<cfset QuerySetCell(srcGlobal,"username",datasources.username)>
		<cfset QuerySetCell(srcGlobal,"password",datasources.password)>
		<cfset QuerySetCell(srcGlobal,"openConnections",datasources.openConnections)>
		<cfset QuerySetCell(srcGlobal,"readonly",datasources.readonly)>
		<cfset QuerySetCell(srcGlobal,"storage",datasources.storage)>
		<cfset QuerySetCell(srcGlobal,"host",datasources.host?:'')>
		<cfset QuerySetCell(srcGlobal,"port",datasources.port?:'')>
	</cfif>
</cfloop>


<cfif request.adminType EQ "web" and srcGlobal.recordcount>
	<cfoutput>
		<h2>#stText.Settings.ReadOnlyDatasources#</h2>
		<div class="itemintro">#stText.Settings.ReadOnlyDatasourcesDescription#</div>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<table class="maintbl checkboxtbl">
				<thead>
					<tr>
						<th width="3%"><input type="checkbox" class="checkbox" name="rowreadonly" onclick="selectAll(this)" /></th>
						<th>#stText.Settings.Name#</th>
						<th>#stText.Settings.Type#</th>
						<th>#stText.Settings.dbHost#:#stText.Settings.dbPort#</th>
						<th width="8%">#stText.Settings.openConn#</th>
						<th width="8%">#stText.Settings.dbStorage#</th>
						<th width="6%">#stText.Settings.DBCheck#</th>
					</tr>
				</thead>
				<tbody>
					<cfloop query="srcGlobal">
						<!--- and now display --->
						<tr>
							<td>
								<input type="checkbox" class="checkbox" name="row_#srcGlobal.currentrow#" value="#srcLocal.currentrow#">
								<input type="hidden" name="username_#srcGlobal.currentrow#" value="#srcGlobal.Username#">
								<input type="hidden" name="password_#srcGlobal.currentrow#" value="#srcGlobal.Password#">
							</td>
							<td>
								<input type="hidden" name="name_#srcGlobal.currentrow#" value="#srcGlobal.name#">
								#srcGlobal.name#
							</td>
							<td>#getDbDriverTypeName(srcGlobal.ClassName,srcGlobal.dsn)#</td>
							<td>#listCompact("#srcGlobal.host?:''#:#srcGlobal.port?:''#",":")#</td>							
							<td>#srcGlobal.openConnections#</td>
							<td>#yesNoFormat(srcGlobal.storage)#</td>
							<td>
								<cfif StructKeyExists(stVeritfyMessages, srcGlobal.name)>
									<cfif stVeritfyMessages[srcGlobal.name].label eq "OK">
										<span class="CheckOk">#stVeritfyMessages[srcGlobal.name].label#</span>
									<cfelse>
										<span class="CheckError" title="#stVeritfyMessages[srcGlobal.name].message##Chr(13)#">#stVeritfyMessages[srcGlobal.name].label#</span>
										<!---
										IMAGE DOESN'T EXIST!
										&nbsp;<img src="resources/img/red-info.gif.cfm" width="9" height="9" title="#stVeritfyMessages[srcGlobal.name].message##Chr(13)#">
										--->
									</cfif>
								<cfelse>
									&nbsp;				
								</cfif>
							</td>
						</tr>
					</cfloop>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="5">
							<input type="submit" class="button submit enablebutton" name="mainAction" value="#stText.Buttons.Verify#">
							<input type="reset" class="reset enablebutton" id="clickCancel" name="cancel" value="#stText.Buttons.Cancel#">
						 </td>
					</tr>
				</tfoot>
			</table>
		</cfformClassic>
	</cfoutput>
</cfif>

<cfif srcLocal.recordcount>
	<cfoutput>
		<h2>#stText.Settings.ListDatasources#</h2>
		<div class="itemintro">#stText.Settings['ListDatasourcesDesc'& request.adminType ]#</div>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<table class="maintbl checkboxtbl">
				<thead>
					<tr>
						<th width="3%"><input type="checkbox" class="checkbox" name="rowread" onclick="selectAll(this)" /></th>
						<th>#stText.Settings.Name#</th>
						<th>#stText.Settings.Type#</th>
						<th>#stText.Settings.dbHost#:#stText.Settings.dbPort#</th>
						<th width="8%">#stText.Settings.openConn#</th>
						<th width="8%">#stText.Settings.dbStorage#</th>
						<th width="6%">#stText.Settings.DBCheck#</th>
						<th width="3%">&nbsp;</th>
					</tr>
				</thead>
				<tbody>
					<cfloop query="srcLocal">
						<cfset hasDriver=false>
						<cftry>

							<cfset label=getDbDriverTypeName(srcLocal.ClassName,srcLocal.dsn)>
							<cfset hasDriver=true>
							<!--- <cfset hasDriver=!isNull(installed[srcLocal.className]) && installed[srcLocal.className]> --->
							<cfcatch>
								<cfset label=srcLocal.ClassName>
							</cfcatch>
						</cftry>
						<cfset css=hasDriver?"":"Red">
						<!--- and now display --->
						<tr>
							<td class="tblContent#css# longwords">
								 <input type="checkbox" class="checkbox" name="row_#srcLocal.currentrow#" value="#srcLocal.currentrow#">
								<input type="hidden" name="username_#srcLocal.currentrow#" value="#srcLocal.Username#">
								<input type="hidden" name="password_#srcLocal.currentrow#" value="#srcLocal.Password#">
							</td>
							<td class="tblContent#css# longwords"><input type="hidden" name="name_#srcLocal.currentrow#" value="#srcLocal.name#">#srcLocal.name#</td>
							<td class="tblContent#css# longwords">#label#
								<cfif !hasDriver><div class="commentError">#stText.Settings.noDriver#</div></cfif>
								<cfif isDefined( "stVeritfyMessages[srcLocal.name].dbInfo" ) && stVeritfyMessages[srcLocal.name].dbInfo.recordCount>
									<cfset qDbInfo = stVeritfyMessages[srcLocal.name].dbInfo>
									<div class="comment">#stText.settings.datasource.databaseName#: #qDbInfo.DATABASE_PRODUCTNAME# #qDbInfo.DATABASE_VERSION#</div>
									<div class="comment">#stText.settings.datasource.driverName#: #qDbInfo.DRIVER_NAME# #qDbInfo.DRIVER_VERSION# (JDBC #qDbInfo.JDBC_MAJOR_VERSION#.#qDbInfo.JDBC_MINOR_VERSION#)</div>
								</cfif>
							</td>
							<td class="tblContent#css# longwords">#listCompact("#srcLocal.host?:''#:#srcLocal.port?:''#",":")#</td>
							<td class="tblContent#css# longwords">#srcLocal.openConnections#</td>							
							<td class="tblContent#css# longwords">#yesNoFormat(srcLocal.storage)#</td>
							<td class="tblContent#css# longwords">
								<cfif StructKeyExists(stVeritfyMessages, srcLocal.name)>
									<cfif stVeritfyMessages[srcLocal.name].label eq "OK">
										<span class="CheckOk">#stVeritfyMessages[srcLocal.name].label#</span>
									<cfelse>
										<span class="CheckError" title="#stVeritfyMessages[srcLocal.name].message##Chr(13)#">#stVeritfyMessages[srcLocal.name].label#</span>
										<!---
										IMAGE DOESN'T EXIST!
										&nbsp;<img src="resources/img/red-info.gif.cfm" width="9" height="9" title="#stVeritfyMessages[srcLocal.name].message##Chr(13)#">
										--->
									</cfif>
								<cfelse>
									&nbsp;				
								</cfif>
							</td>
							<td class="tblContent#css# longwords"><cfif hasDriver>#renderEditButton("#request.self#?action=#url.action#&action2=create&name=#srcLocal.name#")#</cfif>
					

						</tr>						
					</cfloop>
					<cfmodule template="remoteclients.cfm" colspan="6" line="true">
				</tbody>
				<tfoot>
					<tr>
						<td colspan="6">

							<input type="submit" class="bl button submit enablebutton" name="mainAction" value="#stText.Buttons.Verify#">
							<input type="reset" class="bm reset enablebutton" id="clickCancel" name="cancel" value="#stText.Buttons.Cancel#">
							<input type="submit" class="br button submit enablebutton" name="mainAction" value="#stText.Buttons.Delete#">
						 </td>
					</tr>
				</tfoot>
			</table>
		</cfformClassic>
	</cfoutput>
</cfif>

<cfif access EQ -1 or access GT srcLocal.recordcount>
	<cfoutput>
		<!--- Create Datasource --->
		<h2>#stText.Settings.DatasourceModify#</h2>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#&action2=create" method="post">
			<table class="maintbl autowidth">
				<tbody>
					<tr>
						<th scope="row">#stText.Settings.Name#</th>
						<td><cfinputClassic type="text" name="name" value="" class="large" required="yes" 
							message="#stText.Settings.NameMissing#">
						</td>
					</tr>
					<cfset keys=StructKeyArray(drivers)>
					
					<cfset ArraySort(keys,"textNoCase")>
					<tr>
						<th scope="row">#stText.Settings.Type#</th>
						<td>
							<select name="type" class="large">
								<cfloop collection="#keys#" item="idx">
									<cfset key=keys[idx]>
									<cfset driver=drivers[key]>
									<cfif not findNoCase("(old)",driver.getName())>
										<option value="#key#">#driver.getName()#</option>
									</cfif>
								</cfloop>
							</select><br>
							<span class="comment">#replace(
								replace(
									replace(request.adminType=="web"?stText.Settings.DatasourceExtensionWeb:stText.Settings.DatasourceExtensionServer,'{linkServer}','<a href="server.cfm?action=ext.applications">','all')
								,'{link}','<a href="#request.self#?action=ext.applications">','all')
								,'{/link}','</a>','all')#</span>

							<br>
						</td>
					</tr>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="hidden" name="mark" value="create">
							<input type="submit" class="bl button submit" name="run" value="#stText.Buttons.Create#">
							<input type="reset" class="br reset" name="cancel" value="#stText.Buttons.Cancel#">
						</td>
					</tr>
				</tfoot>
			</table>   
		</cfformClassic>
	</cfoutput>
</cfif>