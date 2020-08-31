<cfif request.adminType EQ "web">
	<cfset resetLabel=stText.Buttons.resetServerAdmin>
	<cfelse>
	<cfset resetLabel=stText.Buttons.reset>
</cfif>

<cftry>
	<cfset stVeritfyMessages = StructNew()>
	<cfswitch expression="#form.mainAction#">
		<!--- UPDATE --->
		<cfcase value="#stText.Buttons.update#">
			<cfadmin 
				action="updateORMSetting"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				
				autogenmap="#structKeyExists(form,'autogenmap') and form.autogenmap#"
				eventHandling="#structKeyExists(form,'eventHandling') and form.eventHandling#"
				flushatrequestend="#structKeyExists(form,'flushatrequestend') and form.flushatrequestend#"
				logSQL="#structKeyExists(form,'logSQL') and form.logSQL#"
				savemapping="#structKeyExists(form,'savemapping') and form.savemapping#"
				useDBForMapping="#structKeyExists(form,'useDBForMapping') and form.useDBForMapping#"
				 
				catalog="#form.catalog#"
				cfclocation="#form.cfclocation#"
				dbcreate="#form.dbcreate#"
				schema="#form.schema#"
				
				
				sqlscript="#settings.sqlscript#"
				cacheconfig="#settings.cacheconfig#"
				cacheProvider="#settings.cacheProvider#"
				ormConfig="#settings.ormConfig#"
				secondarycacheenabled="#settings.secondarycacheenabled#"
				
				
				remoteClients="#request.getRemoteClients()#">
		</cfcase>
		<!--- RESET --->
		<cfcase value="#resetLabel#">
			<cfadmin 
				action="resetORMSetting"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
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
Redirtect to entry  --->
<cfif cgi.request_method EQ "POST" and error.message EQ "" and form.mainAction neq stText.Buttons.verify>
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<cfoutput> 
	<!--- Error Output--->
	<cfset printError(error)>
	<h2>#stText.Settings.orm.title#</h2>
	<div class="pageintro">#stText.Settings.orm.desc#</div>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<tbody>
				<!--- autogenmap --->
				<tr>
					<th scope="row">#stText.Settings.orm.autogenmap#</th>
					<td><cfif not hasAccess>
							<b>#yesNoFormat(settings.autogenmap)#</b>
							<cfelse>
							<input type="checkbox" class="checkbox" name="autogenmap" value="true"<cfif settings.autogenmap>  checked="checked"</cfif>>
						</cfif>
						<div class="comment">#stText.Settings.orm.autogenmapDesc#</div></td>
				</tr>
				
				<!--- catalog --->
				<tr>
					<th scope="row">#stText.Settings.orm.catalog#</th>
					<td><cfif not hasAccess>
							<b>#settings.catalog#</b>
							<cfelse>
							<input type="text" class="large" name="catalog" size="80" value="#settings.catalog#" />
						</cfif>
						<div class="comment">#stText.Settings.orm.catalogDesc#</div></td>
				</tr>
				
				<!--- schema --->
				<tr>
					<th scope="row">#stText.Settings.orm.schema#</th>
					<td><cfif not hasAccess>
							<b>#settings.schema#</b>
						<cfelse>
							<input type="text" class="large" name="schema" size="80" value="#settings.schema#" />
						</cfif>
						<div class="comment">#stText.Settings.orm.schemaDesc#</div></td>
				</tr>
				
				<!--- cfc location --->
				<tr>
					<th scope="row">#stText.Settings.orm.cfclocation#</th>
					<td><cfif not hasAccess>
							<b>#settings.isDefaultCfclocation?"":arrayToList(settings.cfclocation)#</b>
						<cfelse>
							<input type="text" class="large" name="cfclocation" size="80" value="#settings.isDefaultCfclocation?"":arraytolist(settings.cfclocation)#" />
						</cfif>
						<div class="comment">#stText.Settings.orm.cfclocationDesc#</div></td>
				</tr>
				
				<!--- dbcreate --->
				<tr>
					<th scope="row">#stText.Settings.orm.dbcreate#</th>
					<td><cfif not hasAccess>
							<b>#settings.dbcreate#</b>
						</cfif>
						<div class="comment">#stText.Settings.orm.autogenmapDesc#</div>
						<cfif hasAccess>
							<ul class="radiolist">
								<cfloop index="item" list="none,update,dropcreate">
									<li>
										<label>
											<input type="radio" class="radio" name="dbcreate" value="#item#" <cfif settings.dbcreate EQ item>checked="checked"<cfelseif item eq "none">checked="checked"</cfif>>
											<b>#item#</b>
										</label>
										<div class="comment">#stText.Settings.orm['dbcreate'& item]#</div>
									</li>
								</cfloop>
							</ul>
						</cfif>
					</td>
				</tr>
				
				<!--- dialect
				
				makes no sense to define this here
				<tr>
					<th scope="row">#stText.Settings.orm.dialect#</th>
					<td>
							<input type="text" name="dialect" size="80" value="#settings.dialect#" /><br />
							<span class="comment">#stText.Settings.orm.dialectDesc#</span>
						
						
					</td>
				</tr>
				 ---> 
				
				<!--- eventHandling --->
				<tr>
					<th scope="row">#stText.Settings.orm.eventHandling#</th>
					<td><cfif not hasAccess>
							<b>#yesNoFormat(settings.eventHandling)#</b>
						<cfelse>
							<input type="checkbox" class="checkbox" name="eventHandling" value="true"<cfif settings.eventHandling>  checked="checked"</cfif>>
						</cfif>
						<div class="comment">#stText.Settings.orm.eventHandlingDesc#</div></td>
				</tr>
				
				<!--- flushatrequestend --->
				<tr>
					<th scope="row">#stText.Settings.orm.flushatrequestend#</th>
					<td><cfif not hasAccess>
							<b>#yesNoFormat(settings.flushatrequestend)#</b>
						<cfelse>
							<input type="checkbox" class="checkbox" name="flushatrequestend" value="true"<cfif settings.flushatrequestend>  checked="checked"</cfif>>
						</cfif>
						<div class="comment">#stText.Settings.orm.flushatrequestendDesc#</div></td>
				</tr>
				
				<!--- logSQL --->
				<tr>
					<th scope="row">#stText.Settings.orm.logSQL#</th>
					<td><cfif not hasAccess>
							<b>#yesNoFormat(settings.logSQL)#</b>
						<cfelse>
							<input type="checkbox" class="checkbox" name="logSQL" value="true"<cfif settings.logSQL>  checked="checked"</cfif>>
						</cfif>
						<div class="comment">#stText.Settings.orm.logSQLDesc#</div></td>
				</tr>
				
				<!--- savemapping --->
				<tr>
					<th scope="row">#stText.Settings.orm.savemapping#</th>
					<td><cfif not hasAccess>
							<b>#yesNoFormat(settings.savemapping)#</b>
						<cfelse>
							<input type="checkbox" class="checkbox" name="savemapping" value="true"<cfif settings.savemapping>  checked="checked"</cfif>>
						</cfif>
						<div class="comment">#stText.Settings.orm.savemappingDesc#</div></td>
				</tr>
				
				<!--- useDBForMapping --->
				<tr>
					<th scope="row">#stText.Settings.orm.useDBForMapping#</th>
					<td><cfif not hasAccess>
							<b>#yesNoFormat(settings.useDBForMapping)#</b>
						<cfelse>
							<input type="checkbox" class="checkbox" name="useDBForMapping" value="true"<cfif settings.useDBForMapping>  checked="checked"</cfif>>
						</cfif>
						<div class="comment">#stText.Settings.orm.useDBForMappingDesc#</div></td>
				</tr>
				
				<!--- sqlscript
				<tr>
					<th scope="row">#stText.Settings.orm.sqlscript#</th>
					<td>
							<input type="text" class="large" name="sqlscript" size="80" value="#settings.sqlscript#" /><br />
							<div class="comment">#stText.Settings.orm.sqlscriptDesc#</div>
						
						
					</td>
				</tr>
				  ---> 
				<!--- 
					public static final Collection.Key SECONDARY_CACHE_ENABLED = KeyImpl.getInstance("secondarycacheenabled");
					public static final Collection.Key CACHE_CONFIG = KeyImpl.getInstance("cacheconfig");
					public static final Collection.Key CACHE_PROVIDER = KeyImpl.getInstance("cacheProvider");
					public static final Collection.Key ORM_CONFIG = KeyImpl.getInstance("ormConfig");
				
				--->
				<cfif hasAccess>
					<cfmodule template="remoteclients.cfm" colspan="2">
				</cfif>
			</tbody>
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="submit" class="bl button submit" name="mainAction" value="#stText.Buttons.Update#">
							<input type="reset" class="bm button reset" name="cancel" value="#stText.Buttons.Cancel#">
							<input class="br button submit" type="submit" name="mainAction" value="#resetLabel#">
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>
</cfoutput>