<cfparam name="error" default="#struct(message:"",detail:"")#">
<cfset stText.Settings.requestExclusive="Exclusive connections for request">
<cfset stText.Settings.requestExclusiveDesc="If set to true, the connections of this datasource are exclusive to one request, guaranteeing that you always have the same connection to the datasource for the request. Keep in mind that this limits the maximum possible concurrent requests to the maximum possible datasource connections and can cause a lot more open connections. Only use the requestExclusive setting when it is absolutely necessary to always have the same connection within a request, for example when your connection has set a specific state.">

<cfset stText.Settings.alwaysResetConnections="Always reset connection to default values before use">
<cfset stText.Settings.alwaysResetConnectionsDesc="This is only necessary in case you are using ""SET TRANSACTION ISOLATION LEVEL"" or ""SET AUTOCOMMIT"" within your SQL code. Enable this feature will increase traffic for this datasource.">




<!--- ACTIONS --->
<cftry>
	<cfif StructKeyExists(form,"_run") and form._run EQ stText.Settings.flushCache>
		<cfset DatasourceFlushMetaCache(form.name)>
		
	<cfelseif StructKeyExists(form,"run") and form.run EQ "create2">
		
		<cfset dbdriver = form.type>

		<cfset driver=createObject("component", drivernames[ dbdriver ])>
		<cfif dbdriver == "Other">
			
			<cfset driver.className = form.custom_class>
		</cfif>

		<cfset driver.onBeforeUpdate()>
		<cfset custom=struct()>
		<cfloop collection="#form#" item="key">
			<cfif findNoCase("custom_",key) EQ 1>
				<cfset l=len(key)>
				<cfset custom[mid(key,8,l-8+1)]=form[key]>
			</cfif>
		</cfloop>
		<cfif form.password EQ "****************">
			
			<cfadmin action="getDatasource" type="#request.adminType#" password="#session["password"&request.adminType]#"
				name="#form.name#"
				returnVariable="existing">
			<cfset form.password=existing.password>
		</cfif>
		<cfset verify=getForm('verify',false)>
		<cfparam name="form.metaCacheTimeout" default="60000">

		<cfadmin action="updateDatasource" type="#request.adminType#" password="#session["password"&request.adminType]#"
			
			id="#isNull(driver.getId)?'':driver.getId()#"
			classname="#driver.getClass()#"
			dsn="#driver.getDSN()#"
			customParameterSyntax="#isNull(driver.customParameterSyntax)?nullValue():driver.customParameterSyntax()#"
			literalTimestampWithTSOffset="#isNull(driver.literalTimestampWithTSOffset)?false:driver.literalTimestampWithTSOffset()#"
			alwaysSetTimeout="#isNull(driver.alwaysSetTimeout)?false:driver.alwaysSetTimeout()#"
			requestExclusive="#getForm('requestExclusive',false)#"
			alwaysResetConnections="#getForm('alwaysResetConnections',false)#"
			
			
			name="#form.name#"
			newName="#form.newName#"
			
			host="#form.host#"
			database="#form.database#"
			port="#form.port#"
			timezone="#form.timezone#"
			dbusername="#form.username#"
			dbpassword="#form.password#"
			
			connectionLimit="#form.connectionLimit#"
			connectionTimeout="#form.connectionTimeout#"
			liveTimeout="#form.LiveTimeout?:''#"
			
			metaCacheTimeout="#form.metaCacheTimeout#"
			blob="#getForm('blob',false)#"
			clob="#getForm('clob',false)#"
			validate="#getForm('validate',false)#"
			storage="#getForm('storage',false)#"
			
			allowed_select="#getForm('allowed_select',false)#"
			allowed_insert="#getForm('allowed_insert',false)#"
			allowed_update="#getForm('allowed_update',false)#"
			allowed_delete="#getForm('allowed_delete',false)#"
			allowed_alter="#getForm('allowed_alter',false)#"
			allowed_drop="#getForm('allowed_drop',false)#"
			allowed_revoke="#getForm('allowed_revoke',false)#"
			allowed_create="#getForm('allowed_create',false)#"
			allowed_grant="#getForm('allowed_grant',false)#"
			verify="#verify#"
			custom="#custom#"
			dbdriver="#dbdriver#"
			remoteClients="#request.getRemoteClients()#">
			<cfset form.mark="update">
		<cfset v="">
		<cfif verify>
			<cfset v="&verified="&form.name>
		</cfif>

		<cflocation url="#request.self#?action=#url.action##v#" addtoken="no">
	</cfif>
	<cfcatch>
		<cfset driver.onBeforeError(cfcatch)>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
		<cfset error.exception = cfcatch>
		<cfset error.cfcatch=cfcatch>
	</cfcatch>
</cftry>

<!--- Error Output--->
<cfset printError(error)>

<cfsilent>
	<cfset isInsert=structKeyExists(form,'mark') and form.mark EQ "create">
	
	<cfif isInsert>

		<cfset actionType="create">
		<cfset datasource=struct()>
		<cfset dbdriver = form.type>
		<cfset datasource.name=form.name>
		<cfset datasource.storage=false>
		<cfset datasource.validate=false>
		<cfset datasource.passwordEncrypted="">	

		<cfset driver = createObject("component",drivernames[ dbdriver ])>	
	<cfelse>

		<cfset actionType="update">
		<cfset _name="#structKeyExists(url,'name')?url.name:form.name#">

		<cfadmin action="getDatasource"	type="#request.adminType#" password="#session["password"&request.adminType]#"
			name="#_name#"
			returnVariable="datasource">

		<!--- <cftry> TODO add again with timeout
			<cfdbinfo type="Version" datasource="#_name#" name='dbinfo'>
			<cfcatch></cfcatch>
		</cftry> --->
		
		<cfset datasource._password=datasource.password>
		<cfset datasource.password="****************">
		<cfset dbdriver = datasource.dbdriver ?: "">

		<cfif isEmpty( dbdriver )>
			<cfset dbdriver = getDbDriverType( datasource.classname, datasource.dsn )>
		</cfif>

		<cfset driver = createObject("component",drivernames[ dbdriver ])>
		<cfif dbdriver == "Other">
			<cfset driver.className = datasource.className>
		</cfif>
	</cfif>

	<cfset driver = createObject("component",drivernames[ dbdriver ])>

	<cfif isInsert>

		<cfset datasource.className= driver.getClass()>
		<cfset datasource.host     = driver.getValue('host')>
		<cfset datasource.database = driver.getValue('database')>
		<cfif datasource.database == "">
 			<cfset datasource.database = datasource.name>
 		</cfif>
		<cfset datasource.port     = driver.getValue('port')>
		<cfset datasource.timezone = "">
		<cfset datasource.username = driver.getValue('username')>
		<cfset datasource.password = driver.getValue('password')>
		<cfset datasource.ConnectionLimit   = driver.getValue('ConnectionLimit')>
		<cfset datasource.ConnectionTimeout = driver.getValue('ConnectionTimeout')>
		<cftry>
			<cfset lt=driver.getValue('LiveTimeout')>
			<cfcatch>
				<cfset lt=60>
			</cfcatch>
		</cftry>
		<cfset datasource.LiveTimeout = lt>
		<cfset datasource.blob     = driver.getValue('blob')>
		<cfset datasource.clob     = driver.getValue('clob')>
		
		<cfset datasource.select   = driver.getValue('allowed_select')>
		<cfset datasource.insert   = driver.getValue('allowed_insert')>
		<cfset datasource.update   = driver.getValue('allowed_update')>
		<cfset datasource.delete   = driver.getValue('allowed_delete')>
		<cfset datasource.create   = driver.getValue('allowed_create')>
		<cfset datasource.drop     = driver.getValue('allowed_drop')>
		<cfset datasource.revoke   = driver.getValue('allowed_revoke')>
		<cfset datasource.alter    = driver.getValue('allowed_alter')>
		<cfset datasource.grant    = driver.getValue('allowed_grant')>
		<cfset datasource.metaCacheTimeout=60000>
	</cfif>
	
	<cfif !structKeyExists(datasource,'metaCacheTimeout')>
		<cfset datasource.metaCacheTimeout=60000>
	</cfif>

	<!--- overwrite values, with values from form scope --->
	<cfloop collection="#form#" item="key">
		<cfif structKeyExists(datasource,key)>
			<cfset datasource[key]=form[key]>
		</cfif>
	</cfloop>
	<cfset driver.init(datasource)>
	<cfset fields=driver.getFields()>

	<cfadmin action="getTimeZones"
		locale="#stText.locale#"
		returnVariable="timezones">

</cfsilent>

<cfoutput>
	<h2>#stText.Settings[ "DatasourceDescription" & ( actionType == "update" ? "Update" : "Create" ) ]# #driver.getName()#</h2>

	<div class="pageintro">#driver.getDescription()#</div>
	
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#&action2=create" method="post">

		<input type="hidden" name="name" value="#datasource.name#">
		<input type="hidden" name="type" value="#dbdriver#">

		<cfset TYPE_HIDDEN=0>
		<cfset TYPE_FREE=1>
		<cfset TYPE_REQUIRED=2>
		
		<cfset typeHost=driver.getType('host')>
		<cfset typeDatabase=driver.getType('database')>
		<cfset typePort=driver.getType('port')>
		<cfset typeUsername=driver.getType('username')>
		<cfset typePassword=driver.getType('password')>

		<cfif typeHost EQ TYPE_HIDDEN><input type="hidden" name="host" value="#datasource.host#"></cfif>
		<cfif typeDatabase EQ TYPE_HIDDEN><input type="hidden" name="database" value="#datasource.database#"></cfif>
		<cfif typePort EQ TYPE_HIDDEN><input type="hidden" name="port" value="#datasource.port#"></cfif>
		<cfif typeUsername EQ TYPE_HIDDEN><input type="hidden" name="username" value="#datasource.username#"></cfif>
		<cfif typePassword EQ TYPE_HIDDEN><input type="hidden" name="password" value="#datasource.password#"></cfif>
		
		
		<h3>Datasource configuration</h3>
		<table class="maintbl">
			<tbody>
				<cfif isDefined("dbInfo")>
				<tr>
					<th scope="row">#stText.settings.datasource.databaseName#</th>
					<td class="comment">
						#dbInfo.DATABASE_PRODUCTNAME# #dbInfo.DATABASE_VERSION#
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.settings.datasource.driverName#</th>
					<td class="comment">
						#dbInfo.DRIVER_NAME# #dbInfo.DRIVER_VERSION# (JDBC #dbInfo.JDBC_MAJOR_VERSION#.#dbInfo.JDBC_MINOR_VERSION#)
					</td>
				</tr>
				</cfif>
				<tr>
					<th scope="row">Name</th>
					<td>
						<cfif error.message NEQ "" OR error.Detail NEQ "">
							<cfinputClassic type="text" name="newName" value="#form.newname#" class="large">
						<cfelse>
							<cfinputClassic type="text" name="newName" value="#datasource.name#" class="large">
						</cfif>
					</td>
				</tr>
				
				<!--- Host --->
				<cfif typeHost NEQ TYPE_HIDDEN>
					<tr>
						<th scope="row">#stText.Settings.dbHost#</th>
						<td>
							<cfinputClassic type="text" name="host" 
							value="#datasource.host#" class="large" required="#typeHost EQ TYPE_REQUIRED#">
							<div class="comment">#stText.Settings.dbHostDesc#</div>
						</td>
					</tr>
				</cfif>
				<!--- DataBase --->
				<cfif typeDataBase NEQ TYPE_HIDDEN>
					<tr>
						<th scope="row">#stText.Settings.dbDatabase#</th>
						<td>
							<cfinputClassic type="text" name="database" 
							value="#datasource.database#" class="large" required="#typeDataBase EQ TYPE_REQUIRED#">
							<div class="comment">#stText.Settings.dbDatabaseDesc#</div>
						</td>
					</tr>
				</cfif>
				<!--- Port --->
				<cfif typePort NEQ TYPE_HIDDEN>
					<tr>
						<th scope="row">#stText.Settings.dbPort#</th>
						<td>
							<cfinputClassic type="text" name="port" validate="integer" 
							value="#datasource.port#" class="small" required="#typePort EQ TYPE_REQUIRED#">
							<div class="comment">#stText.Settings.dbPortDesc#</div>
						</td>
					</tr>
				</cfif>
				<!--- Timezone --->
				<tr>
					<th scope="row">#stText.Settings.dbtimezone#</th>
					<td>
						<select name="timezone" class="large">
							<option value=""> ---- #stText.Settings.dbtimezoneSame# ---- </option>
							<cfoutput query="timezones">
								<option value="#timezones.id#"
								<cfif timezones.id EQ datasource.timezone>selected</cfif>>
								#timezones.id# - #timezones.display#</option>
							</cfoutput>
						</select>
						<div class="comment">#stText.Settings.dbtimezoneDesc#</div>
						
					</td>
				</tr>
				<!--- Username --->
				<cfif typeUsername NEQ TYPE_HIDDEN>
					<tr>
						<th scope="row">#stText.Settings.dbUser#</th>
						<td>
							<cfinputClassic type="text" name="username" 
							value="#datasource.username#" class="medium" required="#typeUsername EQ TYPE_REQUIRED#">
							<div class="comment">#stText.Settings.dbUserDesc#</div>
						</td>
					</tr>
				</cfif>
				<!--- Password --->
				<cfif typePassword NEQ TYPE_HIDDEN>
					<tr>
						<th scope="row">#stText.Settings.dbPass#</th>
						<td>
							<cfinputClassic type="password" name="Password"  passthrough='autocomplete="off"'
							value="#datasource.password#" class="medium" onClick="this.value='';" required="#typePassword EQ TYPE_REQUIRED#">
							<div class="comment">#stText.Settings.dbPassDesc#</div>
						</td>
					</tr>
				</cfif>
			</tbody>
		</table>
		<br />
		<table class="maintbl">
			<tbody>
				<!--- Connection Limit --->
				<tr>
					<th scope="row">#stText.Settings.dbConnLimit#</th>
					<td>
						<select name="ConnectionLimit" class="select small">
							<option value="-1" <cfif datasource.ConnectionLimit EQ -1>selected</cfif>>#stText.Settings.dbConnLimitInf#</option>
							<cfloop index="idx" from="1" to="10"><option  <cfif datasource.ConnectionLimit EQ idx>selected</cfif>>#idx#</option></cfloop>
							<cfloop index="idx" from="20" to="100" step="10"><option  <cfif datasource.ConnectionLimit EQ idx>selected</cfif>>#idx#</option></cfloop>
							<cfloop index="idx" from="200" to="1000" step="100"><option  <cfif datasource.ConnectionLimit EQ idx>selected</cfif>>#idx#</option></cfloop>
						</select>
						<div class="comment">#stText.Settings.dbConnLimitDesc#</div>
					</td>
				</tr>
				<!--- Idle Timeout --->
				<tr>
					<th scope="row">#stText.Settings.dbIdleTimeout#</th>
					<td>
						<select name="ConnectionTimeout" class="select small">
							<cfloop index="idx" from="0" to="20"><option  <cfif datasource.ConnectionTimeout EQ idx>selected</cfif>><cfif idx==0>- inf -
	
<cfelse>#idx#</cfif></option></cfloop>
						</select>
						<div class="comment">#stText.Settings.dbIdleTimeoutDesc#</div>
					</td>
				</tr>
				<!--- Live Timeout --->
				<tr>
					<th scope="row">#stText.Settings.dbLiveTimeout#</th>
					<td><cfset match=false>
						<select name="LiveTimeout" class="select small">
							
							<option value="0" <cfif !isNumeric(datasource.LiveTimeout) or datasource.LiveTimeout LT 1><cfset match=true>selected</cfif>>- inf -</option>
							<cfloop index="label" item="val" struct="#[
								'1 min':1
								,'5 min':5
								,'15 min':15
								,'30 min':30
								,'1 hr':60
								,'2 hr':120
								,'5 hr':300
								,'12 hr':720
								,'1 day':1440
							]#"><option value="#val#"  <cfif datasource.LiveTimeout EQ val><cfset match=true>selected</cfif>>#label#</option></cfloop>
							<cfif not match><option selected>#datasource.LiveTimeout# min</option></cfif>
						</select>
						<div class="comment">#stText.Settings.dbLiveTimeoutDesc#</div>
					</td>
				</tr>
				<!--- Request Exclusive --->
				<tr>
					<th scope="row">#stText.Settings.requestExclusive#</th>
					<td>
						<div class="warning nofocus">
							This feature is experimental.
							If you have any problems while using this functionality,
							please post the bugs and errors in our
							<a href="https://issues.lucee.org" target="_blank">bugtracking system</a>. 
						</div>
						<cfinputClassic type="checkbox" class="checkbox" name="requestExclusive" value="yes" checked="#isDefined('datasource.requestExclusive') and datasource.requestExclusive#">
						<div class="comment">#stText.Settings.requestExclusiveDesc#</div>
					</td>
				</tr>
				<!--- Always reset connection --->
				<tr>
					<th scope="row">#stText.Settings.alwaysResetConnections#</th>
					<td>
						<div class="warning nofocus">
							This feature is experimental.
							If you have any problems while using this functionality,
							please post the bugs and errors in our
							<a href="https://issues.lucee.org" target="_blank">bugtracking system</a>. 
						</div>
						<cfinputClassic type="checkbox" class="checkbox" name="alwaysResetConnections" value="yes" checked="#isDefined('datasource.alwaysResetConnections') and datasource.alwaysResetConnections#">
						<div class="comment">#stText.Settings.alwaysResetConnectionsDesc#</div>
					</td>
				</tr>
				<!--- validate --->
				<tr>
					<th scope="row">#stText.Settings.dbValidate#</th>
					<td>
						<cfinputClassic type="checkbox" class="checkbox" name="validate" value="yes" checked="#isDefined('datasource.validate') and datasource.validate#">
						<div class="comment">#stText.Settings.dbValidateDesc#</div>
					</td>
				</tr>

				<!--- Meta Cache--->
				<cfif dbdriver EQ "oracle">
					<tr>
						<th scope="row">#stText.Settings.dbMetaCacheTimeout#</th>
						<td>
							<cfset selected=false>
							<select name="metaCacheTimeout" class="select small">
								<option value="-1" <cfif datasource.metaCacheTimeout EQ -1><cfset selected=true>selected</cfif>>#stText.Settings.dbConnLimitInf#</option>
								
								<optgroup label="#stText.Settings.minutes#">
									<cfloop index="idx" from="1" to="10"><option value="#idx*60000#"  <cfif datasource.metaCacheTimeout EQ idx*60000><cfset selected=true>selected</cfif>>#idx# #stText.Settings.minutes#</option></cfloop>
									<cfloop index="idx" from="20" to="50" step="10"><option value="#idx*60000#"  <cfif datasource.metaCacheTimeout EQ idx*60000><cfset selected=true>selected</cfif>>#idx# #stText.Settings.minutes#</option></cfloop>
								</optgroup>
								<optgroup label="#stText.Settings.hours#">
									<cfloop index="idx" from="1" to="23"><option value="#idx*60000*60#"  <cfif datasource.metaCacheTimeout EQ idx*60000*60><cfset selected=true>selected</cfif>>#idx# #stText.Settings.hours#</option></cfloop>
								</optgroup>
								<optgroup label="#stText.Settings.days#">
									<cfloop index="idx" from="1" to="30"><option value="#idx*60000*60*24#"  <cfif datasource.metaCacheTimeout EQ idx*60000*60*24><cfset selected=true>selected</cfif>>#idx# #stText.Settings.days#</option></cfloop>
								</optgroup>
							</select>
							<div class="comment">#stText.Settings.dbMetaCacheTimeoutDesc#</div>
						</td>
					</tr>
					<cfif actionType EQ "update">
						<tr>
							<th scope="row">#stText.Settings.flushCache#</th>
							<td>
								<input type="submit" class="button submit" name="_run" value="#stText.Settings.flushCache#">
							</td>
						</tr>
					</cfif>
				</cfif>
				<!--- Blob --->
				<tr>
					<th scope="row">#stText.Settings.dbBlob#</th>
					<td>
						<cfinputClassic type="checkbox" class="checkbox" name="blob" value="yes" checked="#datasource.blob#">
						<div class="comment">#stText.Settings.dbBlobDesc#</div>
					</td>
				</tr>
				<!--- Clob --->
				<tr>
					<th scope="row">#stText.Settings.dbClob#</th>
					<td>
						<cfinputClassic type="checkbox" class="checkbox" name="clob" value="yes" checked="#datasource.clob#">
						<div class="comment">#stText.Settings.dbClobDesc#</div>
					</td>
				</tr>
				<!--- Allow --->
				<tr>
					<th scope="row">#stText.Settings.dbAllowed#</th>
					<td>

						<div class="warningops">
							This functionality is deprecated and will be removed in future versions, 
							restrict operations by using roles and permissions in your DBMS!
						</div>

						<ul class="radiolist float">
							<li class="small"><label><cfinputClassic type="checkbox" class="checkbox" name="allowed_select" value="yes" checked="#datasource.select#"> <b>Select</b></label></li>
							<li class="small"><label><cfinputClassic type="checkbox" class="checkbox" name="allowed_insert" value="yes" checked="#datasource.insert#"> <b>Insert</b></label></li>
							<li class="small"><label><cfinputClassic type="checkbox" class="checkbox" name="allowed_update" value="yes" checked="#datasource.update#"> <b>Update</b></label></li>
							<li class="small"><label><cfinputClassic type="checkbox" class="checkbox" name="allowed_delete" value="yes" checked="#datasource.delete#"> <b>Delete</b></label></li>
							<li class="small"><label><cfinputClassic type="checkbox" class="checkbox" name="allowed_create" value="yes" checked="#datasource.create#"> <b>Create</b></label></li>
							<li class="small"><label><cfinputClassic type="checkbox" class="checkbox" name="allowed_drop" value="yes" checked="#datasource.drop#"> <b>Drop</b></label></li>
							<li class="small"><label><cfinputClassic type="checkbox" class="checkbox" name="allowed_revoke" value="yes" checked="#datasource.revoke#"> <b>Revoke</b></label></li>
							<li class="small"><label><cfinputClassic type="checkbox" class="checkbox" name="allowed_alter" value="yes" checked="#datasource.alter#"> <b>Alter</b></label></li>
							<li class="small"><label><cfinputClassic type="checkbox" class="checkbox" name="allowed_grant" value="yes" checked="#datasource.grant#"> <b>Grant</b></label></li>
						</ul>
					</td>
				</tr>
				<!--- storage --->
				<tr>
					<th scope="row">#stText.Settings.dbStorage#</th>
					<td>
						<cfinputClassic type="checkbox" class="checkbox" name="storage" value="yes" checked="#isDefined('datasource.storage') and datasource.storage#">
						<div class="comment">#stText.Settings.dbStorageDesc#</div>
					</td>
				</tr>

				<!--- custom !--->
				<cfif arrayLen(fields)>
						</tbody>
					</table>
					<br />
					<table class="maintbl">
						<tbody>
				</cfif>
				<cfloop collection="#fields#" item="idx">
					
					<cfset field = fields[idx]>
					<cfset type  = field.getType()>
					<cfset fname = field.getName()>

					<cfset isOther = ( dbdriver == "Other" )>

					<cfif isOther>
						<cfswitch expression="#fname#">
							
							<cfcase value="Class">
								
								<cfset default = datasource.className>
							</cfcase>
							<cfcase value="DSN">
								
								<cfset default = datasource.dsn>
							</cfcase>
						</cfswitch>
					<cfelse>
						<cfif datasource.keyExists( "custom" ) && datasource.custom.keyExists( fname )>
							
							<cfset default=datasource.custom[ fname ]>
						<cfelse>

							<cfset default=field.getDefaultValue()>
						</cfif>
					</cfif>

					<tr>
						<th scope="row">#field.getDisplayName()#</th>
						<td>
							<cfif type EQ "text" or type EQ "password">
								<cfinputClassic type="#type#" 
									name="custom_#field.getName()#" 
									value="#default#" class="large" required="#field.getRequired()#" 
									message="Missing value for field #field.getDisplayName()#">
							<cfelseif type EQ "select">
								<cfif default EQ field.getDefaultValue() and field.getRequired()><cfset default=listFirst(default)></cfif>
								<select name="custom_#field.getName()#" class="large">
									<cfif not field.getRequired()><option value=""> ---------- </option></cfif>
									<cfif len(trim(default))>
										<cfloop index="item" list="#field.getDefaultValue()#">
											<option <cfif item EQ default>selected="selected"</cfif> >#item#</option>
										</cfloop>
									</cfif>
								</select>
							<!--- @todo type checkbox --->
							<cfelseif type EQ "radio">
								<cfif default EQ field.getDefaultValue() and field.getRequired()><cfset default=listGetAt(default,field.getDefaultValueIndex())></cfif>
								<cfloop index="item" list="#field.getDefaultValue()#">
									<cfinputClassic type="radio" class="radio" name="custom_#field.getName()#" value="#item#" checked="#item EQ default#">
									#item#
								</cfloop>
							<!--- @todo type checkbox,radio --->
							</cfif>
								<cfif len(trim(field.getDescription()))><br><div class="comment">#field.getDescription()#</div></cfif>
						</td>
					</tr>
				</cfloop>
				<tr>
					<th scope="row">#stText.Settings.verifyConnection#</th>
					<td><input type="checkbox" class="checkbox" checked="checked" name="verify" value="true" /></td>
				</tr>
				<cfmodule template="remoteclients.cfm" colspan="2">
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2">
						<input type="hidden" name="mark" value="#structKeyExists(form,'mark')?form.mark:'update'#">
						<input type="hidden" name="run" value="create2">
						<input type="submit" class="bl button submit" name="_run" value="#stText.Buttons[actionType]#">
						<input onclick="window.location='#request.self#?action=#url.action#';" type="button" class="br button" name="cancel" value="#stText.Buttons.Cancel#">
					</td>
				</tr>
			</tfoot>
		</table>
	</cfformClassic>
	
		
		<cfif actionType EQ "update">
<!--- optional settings --->
<cfscript>
optional=[];
// not supported yet optional.append('default:false // default: false');
if(datasource.blob) optional.append('blob:#datasource.blob# // default: false');
if(datasource.clob) optional.append('clob:#datasource.clob# // default: false');
if(isNumeric(datasource.connectionLimit))optional.append('connectionLimit:#datasource.connectionLimit# // default:-1');
if(datasource.connectionTimeout NEQ 1)optional.append('connectionTimeout:#datasource.connectionTimeout# // default: 1; unit: minutes');
if(isNumeric(datasource.liveTimeout) && datasource.liveTimeout>0)optional.append('liveTimeout:#datasource.liveTimeout# // default: -1; unit: minutes');
if(datasource.metaCacheTimeout NEQ 60000)optional.append(',metaCacheTimeout:#datasource.metaCacheTimeout# // default: 60000; unit: milliseconds');
if(len(datasource.timezone))optional.append("timezone:'#replace(datasource.timezone,"'","''","all")#'");
if(datasource.storage) optional.append('storage:#datasource.storage# // default: false');
if(datasource.readOnly) optional.append('readOnly:#datasource.readOnly# // default: false');
if(!isNull(driver.literalTimestampWithTSOffset) && driver.literalTimestampWithTSOffset()) 
	optional.append('literalTimestampWithTSOffset:true // default: false');
if(!isNull(driver.alwaysSetTimeout) && driver.alwaysSetTimeout()) 
	optional.append('alwaysSetTimeout:true // default: false');
if(datasource.requestExclusive) 
	optional.append('requestExclusive:true // default: false');
if(datasource.alwaysResetConnections) 
	optional.append('alwaysResetConnections:true // default: false');


optional.append('validate:#truefalseformat(datasource.validate?:false)# // default: false');
</cfscript>
<cfsavecontent variable="codeSample">
	this.datasources["#datasource.name#"] = {
	  class: '#datasource.classname#'#isNull(datasource.bundleName)?"":"
	, bundleName: '"&datasource.bundleName&"'"##isNull(datasource.bundleVersion)?"":"
	, bundleVersion: '"&datasource.bundleVersion&"'"#
	, connectionString: '#replace(datasource.dsnTranslated,"'","''","all")#'<cfif len(datasource._password)>
	, username: '#replace(datasource.username,"'","''","all")#'
	, password: "#datasource.passwordEncrypted#"</cfif><cfif optional.len()>
	
	// optional settings
	<cfloop array="#optional#" index="i" item="value">, #value#<cfif i LT optional.len()>
	</cfif></cfloop></cfif>
};
</cfsavecontent>
<cfset renderCodingTip( codeSample, "", true )>



		</cfif>
</cfoutput>