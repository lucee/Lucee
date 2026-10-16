<cftry>
	<cfset stVerifyMessages = StructNew()>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="#stText.Buttons.Delete#">
			<cfset data.names=toArrayFromForm("name")>
			<cfset data.rows=toArrayFromForm("row")>
			<cfset data.names=toArrayFromForm("name")>
			
			<cfloop index="idx" from="1" to="#arrayLen(data.names)#">
				<cfif arrayIndexExists(data.rows, idx) and data.names[idx] NEQ "">
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

				<cfloop index="idx" from="1" to="#arrayLen(data.names)#">
					<cfif arrayIndexExists(data.rows, idx) and data.names[idx] NEQ "">
						<cftry>
							<cfadmin
								action="verifyDatasource"
								type="#request.adminType#"
								password="#session["password"&request.adminType]#"
								name="#data.names[idx]#">
								<cfset stVerifyMessages["#data.names[idx]#"].Label = "OK">

								<cfdbinfo type="Version" datasource="#data.names[idx]#" name='stVerifyMessages["#data.names[idx]#"].dbInfo'>
							<cfcatch>
								<!--- <cfset error.message=error.message&data.names[idx]&": "&cfcatch.message&"<br>"> --->
								<cfset stVerifyMessages[data.names[idx]].Label = "Error">
								<cfset stVerifyMessages[data.names[idx]].message = cfcatch.message>
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
				
				psq="#structKeyExists(form,"preserveSingleQuote") and form.preserveSingleQuote#">
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
	<cfset stVerifyMessages={}>
	<cfset stVerifyMessages[url.verified].Label = "OK">
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
						<cfmodule template="systemSetting.cfm" 
							name="preserveSingleQuote" 
							value="#dbSetting.psq#"
							access="#access NEQ 0#"
							description="#stText.Settings.PreserveSingleQuotesDescription#"
							br=false
							sp=false
							descOnTop=false>
							<input type="checkbox" class="checkbox" name="preserveSingleQuote" value="yes" <cfif dbSetting.psq>checked</cfif>>
						</cfmodule>
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
							<cfif not request.singleMode and request.adminType EQ "web">
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


<cfset querySort(datasources,"name")>


<cfif datasources.recordcount>
	<cfoutput>
		<h2>#stText.Settings.ListDatasources#</h2>
		<div class="itemintro">#stText.Settings['ListDatasourcesDesc'& request.adminType ]#</div>
		<cfset renderSettings("datasources",{columns:[
			"name",
			"class","bundleName","bundleVersion","maven","component",
			"database","host","port","username","password",
			"connectionString","idleTimeout","liveTimeout","connectionLimit","minIdle","maxIdle","maxTotal","metaCacheTimeout",
			"blob","clob","timezone","dbdriver",
			"literalTimestampWithTSOffset","alwaysSetTimeout","requestExclusive","alwaysResetConnections",
			"custom","storage","validate"
			], value:removeCoreBundle(datasources)} )>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<table class="maintbl checkboxtbl">
				<thead>
					<tr>
						<th width="3%"><input type="checkbox" class="checkbox" name="rowread" onclick="selectAll(this)" /></th>
						<th >#stText.Settings.Name#</th>
						<th >#stText.Settings.Type#</th>
						<th >#stText.Settings.dbHost#:#stText.Settings.dbPort#</th>
						<th title="#stText.Settings.activeConn#" width="8%">#stText.Settings.active#</th>
						<th title="#stText.Settings.idleConn#" width="8%">#stText.Settings.idle#</th>
						<th width="8%">#stText.Settings.dbStorage#</th>
						<th width="6%">#stText.Settings.DBCheck#</th>
						<th width="3%">&nbsp;</th>
					</tr>	
				</thead>
				<tbody>
					<cfloop query="datasources">
						<cfset hasDriver=false>
						<cftry>

							<cfset label=getDbDriverTypeName(datasources.ClassName,datasources.dsn)>
							<cfset hasDriver=true>
							<!--- <cfset hasDriver=!isNull(installed[datasources.className]) && installed[datasources.className]> --->
							<cfcatch>
								<cfset label=datasources.ClassName>
							</cfcatch>
						</cftry>
						<cfset css=hasDriver?"":"Red">
						<!--- and now display --->
						<tr>
							<td class="tblContent#css# longwords">
								 <input type="checkbox" class="checkbox" name="row_#datasources.currentrow#" value="#datasources.currentrow#">
							</td>
							<td class="tblContent#css# longwords"><input type="hidden" name="name_#datasources.currentrow#" value="#datasources.name#">#datasources.name#</td>
							<td class="tblContent#css# longwords">#label#
								<cfif !hasDriver><div class="commentError">#stText.Settings.noDriver#</div></cfif>
								<cfif !isNull( stVerifyMessages[datasources.name].dbInfo ) && stVerifyMessages[datasources.name].dbInfo.recordCount>
									<cfset qDbInfo = stVerifyMessages[datasources.name].dbInfo>
									<div class="comment">#stText.settings.datasource.databaseName#: #qDbInfo.DATABASE_PRODUCTNAME# #qDbInfo.DATABASE_VERSION#</div>
									<div class="comment">#stText.settings.datasource.driverName#: #qDbInfo.DRIVER_NAME# #qDbInfo.DRIVER_VERSION# (JDBC #qDbInfo.JDBC_MAJOR_VERSION#.#qDbInfo.JDBC_MINOR_VERSION#)</div>
								<cfelseif StructKeyExists(stVerifyMessages, datasources.name) && stVerifyMessages[datasources.name].label neq "OK">
									<div class="CheckError">#stVerifyMessages[datasources.name].message#</div>
								</cfif>
							</td>
							<td class="tblContent#css# longwords">#listCompact("#datasources.host?:''#:#datasources.port?:''#",":")#</td>
							<td class="tblContent#css# longwords">#datasources.activeConnections# </td>
							<td class="tblContent#css# longwords">#datasources.idleConnections# </td>
							<td class="tblContent#css# longwords">#yesNoFormat(datasources.storage)#</td>
							<td class="tblContent#css# longwords">
								<cfif StructKeyExists(stVerifyMessages, datasources.name)>
									<cfif stVerifyMessages[datasources.name].label eq "OK">
										<span class="CheckOk">#stVerifyMessages[datasources.name].label#</span>
									<cfelse>
										<span class="CheckError" title="#stVerifyMessages[datasources.name].message##Chr(13)#">#stVerifyMessages[datasources.name].label#</span>
										<!---
										IMAGE DOESN'T EXIST!
										&nbsp;<img src="resources/img/red-info.gif.cfm" width="9" height="9" title="#stVerifyMessages[datasources.name].message##Chr(13)#">
										--->
									</cfif>
								<cfelse>
									&nbsp;				
								</cfif>
							</td>
							<td class="tblContent#css# longwords">
								<cfif hasDriver>
							<cfif datasources.readOnly>
								#lockedReadOnly()#
							<cfelse>
								#renderEditButton2("dataSources","name",dataSources.name,"#request.self#?action=#url.action#&action2=create&name=#datasources.name#")#
							</cfif>	
							</cfif>
							</td>
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

<cfif access EQ -1 or access GT datasources.recordcount>
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