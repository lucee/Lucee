<cfset error.message="">
<cfset error.detail="">
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">
<cftry>
	<cfset stVeritfyMessages = StructNew()>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="#stText.Buttons.Update#">

			<cfif form.debug == "resetServerAdmin">
				
				<cfadmin action="updateDebug"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					debug=""
					debugTemplate=""	                
					remoteClients="#request.getRemoteClients()#">
			
			<cfelse>

				<cfadmin action="updateDebug"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					debug="#form.debug#"
					debugTemplate=""
					remoteClients="#request.getRemoteClients()#">
			</cfif>
		</cfcase>

	<!--- delete --->
		<cfcase value="#stText.Buttons.Delete#">
			<cfset data.rows=toArrayFromForm("row")>
			<cfset data.ids=toArrayFromForm("id")>
			<cfloop index="idx" from="1" to="#arrayLen(data.ids)#">
				<cfif isDefined("data.rows[#idx#]") and data.ids[idx] NEQ "">
					<cfadmin 
						action="removeDebugEntry"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						id="#data.ids[idx]#"
						remoteClients="#request.getRemoteClients()#">
					
				</cfif>
			</cfloop>
		</cfcase>
	</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
		<cfset error.cfcatch=cfcatch>
	</cfcatch>
</cftry>


<cfadmin
	action="getDebugEntry"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="debug">


<cfadmin
	action="getDebug"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="_debug">

<cfadmin
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="access"
	secType="debugging"
	secValue="yes">
<cfset hasAccess=access>


<cftry>
	<cfset stVeritfyMessages = StructNew()>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="#stText.Buttons.Update#">
				<cfadmin action="updateDebug"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					debug="#isDefined('form.debug') && form.debug#"
					database="#isDefined('form.database') && form.database#"
					exception="#isDefined('form.exception') && form.exception#"
					tracing="#isDefined('form.tracing') && form.tracing#"
					dump="#isDefined('form.dump') && form.dump#"
					timer="#isDefined('form.timer') && form.timer#"
					implicitAccess="#isDefined('form.implicitAccess') && form.implicitAccess#"
					queryUsage="#isDefined('form.queryUsage') && form.queryUsage#"
					template="#isDefined('form.template') && form.template#"
					thread="#isDefined('form.thread') && form.thread#"


					debugTemplate=""
					remoteClients="#request.getRemoteClients()#">
		</cfcase>
		<cfcase value="#stText.Buttons.resetServerAdmin#">

				<cfadmin action="updateDebug"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					debug=""
					database=""
					exception=""
					tracing=""
					dump=""
					timer=""
					implicitAccess=""
					queryUsage=""
					thread=""

					debugTemplate=""
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

<cfset querySort(debug,"id")>
<cfset qryWeb=queryNew("id,label,iprange,type,custom,readonly,driver")>
<cfset qryServer=queryNew("id,label,iprange,type,custom,readonly,driver")>


<cfset stText.debug.settings.generalYes="Lucee logs debug information you have checked below.">
<cfset stText.debug.settings.generalNo="Lucee does not log any debug information at all.">


<cfhtmlbody>
	<script type="text/javascript">
		function sp_clicked()
		{
			var iscustom = $('#sp_radio_debug')[0].checked;
			var tbl = $('#debugoptionstbl').css('opacity', (iscustom ? 1:.5));
			var inputs = $('input', tbl).prop('disabled', !iscustom);
			if (!iscustom)
			{
				inputs.prop('checked', false);
			}
		}
		$(function(){
			$('#sp_options input.radio').bind('click change', sp_clicked);
			sp_clicked();
		});
	</script>
</cfhtmlbody>

<cfoutput>


	<!--- Error Output--->
	<cfset printError(error)>

	#stText.Debug.EnableDescription#

	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post" name="debug_settings">
		<table class="maintbl autowidth">
			<tbody>
				<tr>
					<th scope="row">
						#stText.Debug.EnableDebugging#
					</th>
					<td>
						<cfset lbl = _debug.debug ? stText.general.yes : stText.general.no>
						<cfif hasAccess>
							<ul class="radiolist" id="sp_options">
								<li>
									<label>
										<input type="radio" class="radio" name="debug" value="false" #!_debug.debug ? 'checked="checked"' : ''#>
										#stText.general.no#
									</label>

									<div class="comment">#stText.debug.settings.generalNo#</div>

								</li>
								<li>
									<label>
										<input type="radio" class="radio" name="debug" id="sp_radio_debug" value="true" #_debug.debug ? 'checked="checked"' : ''#>
										#stText.general.yes#
									</label>
									<div class="comment">#stText.debug.settings.generalYes#</div>
									<table class="maintbl autowidth" id="debugoptionstbl">
									<tbody>
										<cfloop list="template,database,exception,tracing,dump,timer,implicitAccess,thread" item="item">
										<tr>
											<th scope="row">#stText.debug.settings[item]#</th>
											<td>
												<cfset lbl = _debug[item] ? stText.general.yes : stText.general.no>
												<cfif hasAccess>
													<label><input type="checkbox" name="#item#" value="true"  <cfif item EQ "database">id="sp_radio_qu"</cfif> #_debug[item] ? 'checked="checked"' : ''#>
													#stText.general.enabled#</label>
												<cfelse>
													<b>#_debug[item] ? stText.general.yes : stText.general.no#</b>
													<input type="hidden" name="#item#" value="#_debug[item]#">
												</cfif>
												<cfif structKeyExists(stText.debug.settings, item&"Alert")>
													<div>
														<b class="comment" style="color:##bf4f36">#stText.debug.settings[item&"Alert"]#</b>
													</div>
												</cfif>
												<div class="comment">#stText.debug.settings[item&"Desc"]#</div>

												<cfif item EQ "database">
												<table class="maintbl autowidth" id="debugoptionqutbl">
												<tbody>
													<tr>
														<th scope="row">#stText.debug.settings.queryUsage#</th>
														<td>
															<cfset lbl = _debug.queryUsage ? stText.general.yes : stText.general.no>
															<cfif hasAccess>
																<label><input type="checkbox" name="queryUsage" value="true" #_debug.queryUsage ? 'checked="checked"' : ''#>
																#stText.general.enabled#</label>
															<cfelse>
																<b>#_debug.queryUsage ? stText.general.yes : stText.general.no#</b>
																<input type="hidden" name="queryUsage" value="#_debug.queryUsage#">
															</cfif>
															<div class="comment">#stText.debug.settings["queryUsageDesc"]#</div>
														</td>
													</tr>
												</table>
												</cfif>



											</td>
										</tr>
										</cfloop>
								</table>
								</li>
							</ul>
						<cfelse>
							<!---<input type="hidden" name="scriptProtect" value="#appSettings.scriptProtect#">--->
							<b>#lbl#</b>
							<div class="comment">#_debug.debug?stText.debug.settings.generalYes:stText.debug.settings.generalNo#</div>
							<cfloop list="database,exception,tracing,dump,timer,implicitAccess" item="item">
								<cfif _debug[item]>- #stText.debug.settings[item]#<br></cfif>
							</cfloop>


						</cfif>
					</td>
				</tr>



				<cfif hasAccess>
					<cfmodule template="remoteclients.cfm" colspan="2">
				</cfif>
			</tbody>
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="submit" class="bl button submit" name="mainAction" value="#stText.Buttons.Update#">
							<input type="reset" class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" name="cancel" value="#stText.Buttons.Cancel#">
							<cfif request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#"></cfif>

						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>

	<cfset error.message="">
	<cfset error.detail="">
	<cfparam name="url.action1" default="list">
	<cfparam name="form.mainAction" default="none">
	<cfparam name="form.subAction" default="none">


	<cfadmin 
		action="getDebugEntry"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		returnVariable="debug">
		

	<cfadmin 
		action="getDebug"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		returnVariable="_debug">
		
	<cfadmin 
		action="securityManager"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		returnVariable="access"
		secType="debugging">
	<cfset hasAccess=access>

		
	<!--- load available drivers --->
	<cfset driverNames=structnew("linked")>
	<cfset driverNames=ComponentListPackageAsStruct("lucee-server.admin.debug",driverNames)>
	<cfset driverNames=ComponentListPackageAsStruct("lucee.admin.debug",driverNames)>
	<cfset driverNames=ComponentListPackageAsStruct("debug",driverNames)>


	<cfset drivers={}>
		<cfloop collection="#driverNames#" index="n" item="fn">
			<cfif n EQ "Debug" or n EQ "Field" or n EQ "Group">
				<cfcontinue>
			</cfif>
			<cfset tmp=createObject('component',fn)>
			<cfset drivers[trim(tmp.getId())]=tmp>
		</cfloop>	
	<!--- 
	<span class="CheckError">
	The Gateway Implementation is currently in Beta State. Its functionality can change before it's final release.
	If you have any problems while using the Gateway Implementation, please post the bugs and errors in our <a href="https://jira.jboss.org/jira/browse/Lucee" target="_blank" class="CheckError">bugtracking system</a>. 
	</span><br /><br />
	--->

	<!--- 
	Redirtect to entry --->
	<cfif cgi.request_method EQ "POST" and error.message EQ "" and form.mainAction neq stText.Buttons.verify>
		<cflocation url="#request.self#?action=#url.action#" addtoken="no">
	</cfif>

	<cfset querySort(debug,"id")>
	<cfset qryWeb=queryNew("id,label,iprange,type,custom,readonly,driver")>
	<cfset qryServer=queryNew("id,label,iprange,type,custom,readonly,driver")>


	<cfloop query="debug">	
		<cfif not debug.readOnly>
			<cfset tmp=qryWeb>
		<cfelse>
			<cfset tmp=qryServer>
		</cfif>
		<cfset QueryAddRow(tmp)>
		<cfset QuerySetCell(tmp,"id",debug.id)>
		<cfset QuerySetCell(tmp,"label",debug.label)>
		<cfset QuerySetCell(tmp,"iprange",debug.iprange)>
		<cfset QuerySetCell(tmp,"type",debug.type)>
		<cfset QuerySetCell(tmp,"custom",debug.custom)>
		<cfset QuerySetCell(tmp,"readonly",debug.readonly)>
		<cfif structKeyExists(drivers,debug.type)><cfset QuerySetCell(tmp,"driver",drivers[debug.type])></cfif>
	</cfloop>

	<cfoutput>
		<!--- Error Output--->
		<cfset printError(error)>
		<script type="text/javascript">
			var drivers={};
			<cfloop collection="#drivers#" item="key">drivers['#JSStringFormat(key)#']='#JSStringFormat(drivers[key].getDescription())#';
			</cfloop>
			function setDesc(id,key){
				var div = document.getElementById(id);
				if(div.hasChildNodes())
					div.removeChild(div.firstChild);
				div.appendChild(document.createTextNode(drivers[key]));
			}
		</script>
		
				#stText.debug.list.createDesc#

		<!--- LIST --->
		<cfloop list="server,web" index="k">
			<cfset isWeb=k EQ "web">
			<cfset qry=variables["qry"&k]>
			<cfif qry.recordcount>
				<h2>#stText.debug.list[k & "title"]#</h2>
				<div class="itemintro">#stText.debug.list[k & "titleDesc"]#</div>
				<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
					<table class="maintbl">
						<thead>
							<tr>
								<cfif isWeb>
									<th width="3%">
										<input type="checkbox" class="checkbox" name="rowreadonly" onclick="selectAll(this)">
									</th>
								</cfif>
								<th width="25%">#stText.debug.label#</th>
								<th>#stText.debug.ipRange#</th>
								<th width="15%"># stText.debug.type#</td>
								<cfif isWeb>
									<th width="3%"></th>
								</cfif>
							</tr>
						</thead>
						<tbody>
							<cfloop query="qry">
								<cfset drv=qry.driver>
								<cfif isNull(drv) or IsSimpleValue(drv)>
									<cfcontinue>
								</cfif>
								<tr>
									<cfif isWeb>
										<td>
											<input type="checkbox" class="checkbox" name="row_#qry.currentrow#" id="clickCheckbox" value="#qry.currentrow#">
										</td>
									</cfif>
									<td>
										<input type="hidden" name="id_#qry.currentrow#" value="#qry.id#">
										<input type="hidden" name="type_#qry.currentrow#" value="#qry.type#">
										#qry.label#
									</td>
									<td>#replace(qry.ipRange,",","<br />","all")#</td>
									
									<td>#qry.driver.getLabel()#</td>
									<cfif isWeb>
										<td>
											#renderEditButton("#request.self#?action=debugging.templates&action2=create&id=#qry.id#")#
										</td>
									</cfif>
								</tr>
							</cfloop>
						</tbody>
						<cfif isWeb>
							<tfoot>
								<tr>
									<td colspan="#isWeb?5:3#">
										<input type="submit" class="bl button submit enablebutton" name="mainAction" value="#stText.Buttons.delete#" disabled style="opacity:0.5">
										<input type="reset" class="br button reset enablebutton" id="clickCancel" name="cancel" value="#stText.Buttons.Cancel#" disabled style="opacity:0.5">
									</td>	
								</tr>
							</tfoot>
						</cfif>
					</table>
				</cfformClassic>
			</cfif>
		</cfloop>

		<!--- Create debug entry --->
		<cfif access EQ "yes">
			<cfset _drivers=ListSort(StructKeyList(drivers),'textnocase')>
		
			<cfif listLen(_drivers)>
				<h2>#stText.debug.createTitle#</h2>
				<cfformClassic onerror="customError" action="#go("debugging.templates","create")#" method="post">
					<table class="maintbl autowidth" style="width:400px;">
						<tbody>
							<tr>
								<th scope="row">#stText.debug.label#</th>
								<td><cfinputClassic type="text" name="label" value="" class="large" required="yes" 
									message="#stText.debug.labelMissing#"></td>
							</tr>
							<tr>
								<th scope="row">#stText.Settings.gateway.type#</th>
								<td>
									<select name="type" onchange="setDesc('typeDesc',this.value);" class="large">
										<cfloop list="#_drivers#" index="key">
										<cfset driver=drivers[key]>
											<option value="#trim(driver.getId())#">#trim(driver.getLabel())#</option>
										</cfloop>
									</select>
									<div id="typeDesc" style="position:relative"></div>
									<script>setDesc('typeDesc','#JSStringFormat(listFirst(_drivers))#');</script>
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
				#stText.debug.noDriver#
			</cfif>
		<cfelse>
			<cfset noAccess(stText.debug.noAccess)>
		</cfif>
	</cfoutput>

</cfoutput>