<cfsilent>
<cfset error.message="">
<cfset error.detail="">

<cfset stText.setting.typeChecking="UDF Type Checking">
<cfset stText.setting.typeCheckingDesc="If disabled Lucee ignores type definitions with function arguments and return values">
<cfset stText.setting.developMode="If enabled the Admininstrator no longer cache data.">
<cfset stText.setting.ctCacheDesc="Press the button above to clear the custom tag path cache.">

<cfset stText.setting.cachedAfter="Query cachedAfter">
<cfset stText.setting.cachedAfterDesc="In case the attribute ""cacheAfter"" is set without the attribute ""cachedwithin"" in the tag ""query"" this time span is used for the element cached.">
				
<cfparam name="stText.general.elements" default="item(s)">

<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="hasAccess"
	secType="setting"
	secValue="yes">

<!--- 
Defaults --->	
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">
<cfset objectCache={}>
<!--- Query --->
<cftry>
	<cfobjectcache type="query" action="size" result="objectCache.query">
    <cfcatch>
    	<cfset objectCache.query=-1>
    </cfcatch>
</cftry>

<!--- Function --->
<cftry>
	<cfobjectcache type="function" action="size" result="objectCache.function">
    <cfcatch>
    	<cfset objectCache.function=-1>
    </cfcatch>
</cftry>

<!--- Include --->
<cftry>
	<cfobjectcache type="include" action="size" result="objectCache.include">
    <cfcatch>
    	<cfset objectCache.include=-1>
    </cfcatch>
</cftry>

<!--- Object --->
<cftry>
	<cfobjectcache type="object" action="size" result="objectCache.object">
    <cfcatch>
    	<cfset objectCache.object=-1>
    </cfcatch>
</cftry>

<!--- Query --->
<cftry>
	<cfobjectcache type="query" action="size" result="objectCache.query">
    <cfcatch>
    	<cfset objectCache.query=-1>
    </cfcatch>
</cftry>

<!--- Resource --->
<cftry>
	<cfobjectcache type="resource" action="size" result="objectCache.resource">
    <cfcatch>
    	<cfset objectCache.resource=-1>
    </cfcatch>
</cftry>

<!--- template --->
<cftry>
	<cfobjectcache type="template" action="size" result="objectCache.template">
    <cfcatch>
    	<cfset objectCache.template=-1>
    </cfcatch>
</cftry>


<cfset clearButton={}>
<cfloop collection="#objectCache#" index="cacheType" item="cacheSize">
	<cfset clearButton[cacheType]=stText.setting["CacheClear"]>
	<cfif cacheSize GTE 0>
		<cfset clearButton[cacheType]=replace(stText.setting["CacheClearCount"],'{count}',cacheSize)>
	</cfif>
	<cfset clearButton[cacheType]=replace(clearButton[cacheType],'{name}',ucFirst(string:cacheType,doLowerIfAllUppercase:true))>
</cfloop>


<cfset btnClearTemplateCache=replace(stText.setting.templateCacheClearCount,'{count}',arrayLen(pagePoolList()))>
<!--- 
<cfset qrySize=objectCache.query>
<cfset btnClearQueryCache=stText.setting.queryCacheClear>
<cfif qrySize GTE 0>
	<cfset btnClearQueryCache=replace(stText.setting.queryCacheClearCount,'{count}',qrySize)>
</cfif>
--->


<cfset stText.setting.applicationCacheClear="Clear Application path Cache">
<cfset stText.setting.applicationCache="Application path Cache">
<cfset stText.setting.applicationCacheDesc="Press the button above to clear the Application path cache. This cache caches the location of the Application.[cfc|cfm] files.">

<cfset btnClearComponentCache=replace(stText.setting.componentCacheClear,'{count}',structCount(componentCacheList()))>
<cfset btnClearApplicationCache=replace(stText.setting.applicationCacheClear,'{count}',0)>
<cfset btnClearCTCache=replace(stText.setting.ctCacheClear,'{count}',structCount(ctCacheList()))>
</cfsilent>
<cfif hasAccess>
	<cftry>
		<cfswitch expression="#form.mainAction#">
		
			<cfcase value="#btnClearComponentCache#">
				<cfset componentCacheClear()>
			</cfcase>
			<cfcase value="#btnClearApplicationCache#">
				<cfset ApplicationPathCacheClear()>
			</cfcase>
			<cfcase value="#btnClearCTCache#">
				<cfset ctCacheClear()>
			</cfcase>
			<cfcase value="#btnClearTemplateCache#">
				<cfset pagePoolClear()>
			</cfcase>
			<cfcase value="#clearButton['function']#">
				<cfobjectcache type="function" action="clear">
			</cfcase>
			<cfcase value="#clearButton.include#">
				<cfobjectcache type="include" action="clear">
			</cfcase>
			<cfcase value="#clearButton.object#">
				<cfobjectcache type="object" action="clear">
			</cfcase>
			<cfcase value="#clearButton.query#">
				<cfobjectcache type="query" action="clear">
			</cfcase>
			<cfcase value="#clearButton.resource#">
				<cfobjectcache type="resource" action="clear">
			</cfcase>
			<cfcase value="#clearButton.template#">
				<cfobjectcache type="template" action="clear">
			</cfcase>
			<!--- Update ---->
			<cfcase value="#stText.Buttons.Update#">
				<cfset cachedAfter=isNull(form.cachedAfter_days)?"":
					CreateTimeSpan(
						form.cachedAfter_days,
						form.cachedAfter_hours,
						form.cachedAfter_minutes,
						form.cachedAfter_seconds
					)>
				<cfadmin 
					action="updatePerformanceSettings"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					cachedAfter="#cachedAfter#"
					inspectTemplate="#form.inspectTemplate#"
					typeChecking="#!isNull(form.typeChecking) and form.typeChecking EQ true#"
					remoteClients="#request.getRemoteClients()#"
					>
				<cfif request.adminType EQ "server">

					<cfadmin
						action="updateDevelopMode"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						mode="#!isNull(form.mode) and form.mode EQ true#"
					>
				</cfif>
			
			</cfcase>
		<!--- reset to server setting --->
			<cfcase value="#stText.Buttons.resetServerAdmin#">
				<cfadmin 
					action="updatePerformanceSettings"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					cachedAfter=""
					inspectTemplate=""
					typeChecking=""
					
					remoteClients="#request.getRemoteClients()#"
					>
			
			</cfcase>
		</cfswitch>
		<cfcatch>
			<cfset error.message=cfcatch.message>
			<cfset error.detail=cfcatch.Detail>
			<cfset error.cfcatch=cfcatch>
		</cfcatch>
	</cftry>
</cfif>

<cfadmin 
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	action="getPerformanceSettings"
	returnVariable="Settings">
	
<!--- 
Redirect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<!--- 
Error Output --->
<cfset printError(error)>
<!--- 
Create Datasource --->

<cfoutput>
	<cfif not hasAccess>
		<cfset noAccess(stText.setting.noAccess)>
	</cfif>

	<div class="pageintro">#stText.setting.cacheDesc#</div>
	
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<tbody>
				<!--- Template Cache for Request --->
				<tr>
					<th scope="row">#stText.setting.inspectTemplate#</th>
					<td>
						<cfif hasAccess>
							<ul class="radiolist">
								<li>
									<!--- never --->
									<label>
										<input class="radio" type="radio" name="inspectTemplate" value="never"<cfif settings.inspectTemplate EQ "never"> checked="checked"</cfif>>
										<b>#stText.setting.inspectTemplateNever#</b>
									</label>
									<div class="comment">#stText.setting.inspectTemplateNeverDesc#</div>
								</li>
								<li>
									<!--- once --->
									<label>
										<input class="radio" type="radio" name="inspectTemplate" value="once"<cfif settings.inspectTemplate EQ "once"> checked="checked"</cfif>>
										<b>#stText.setting.inspectTemplateOnce#</b>
									</label>
									<div class="comment">#stText.setting.inspectTemplateOnceDesc#</div>
								</li>
								<li>
									<!--- always --->
									<label>
										<input class="radio" type="radio" name="inspectTemplate" value="always"<cfif settings.inspectTemplate EQ "always"> checked="checked"</cfif>>
										<b>#stText.setting.inspectTemplateAlways#</b>
									</label>
									<div class="comment">#stText.setting.inspectTemplateAlwaysDesc#</div>
								</li>
							</ul>
						<cfelse>
							<cfif ListFindNoCase("never,once,always",settings.inspectTemplate)>
								<input type="hidden" name="inspectTemplate" value="#settings.inspectTemplate#">
								<b>#stText.setting["inspectTemplate"& settings.inspectTemplate]#</b><br />
								<div class="comment">#stText.setting["inspectTemplate#settings.inspectTemplate#Desc"]#</div>
							</cfif>
						</cfif>
					</td>
				</tr>
				
				
				<!--- Type Checking --->
				<tr>
					<th scope="row">#stText.setting.typeChecking#</th>
					<td class="fieldPadded">
						<label>
							<input class="checkbox" type="checkbox" name="typeChecking" value="true"<cfif settings.typeChecking EQ true> checked="checked"</cfif>>
						</label>
						<div class="comment">#stText.setting.typeCheckingDesc#</div>
						<cfset renderCodingTip( "this.typeChecking = "&settings.typeChecking&";" )>
					</td>
				</tr>

				<!--- Query.chachedAfter --->
				<cfif true>
				<tr>
					<th scope="row">#stText.setting.cachedAfter#</th>
					<td>
						<cfset timeout=settings.cachedAfter>
						<table class="maintbl" style="width:auto">
							<thead>
								<tr>
									<th>#stText.General.Days#</td>
									<th>#stText.General.Hours#</td>
									<th>#stText.General.Minutes#</td>
									<th>#stText.General.Seconds#</td>
								</tr>
							</thead>
							<tbody>
								<cfif hasAccess>
									<tr>
										<td><cfinputClassic type="text" name="cachedAfter_days" value="#settings.cachedAfter_day#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutDaysValue#application#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="cachedAfter_hours" value="#settings.cachedAfter_hour#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutHoursValue#application#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="cachedAfter_minutes" value="#settings.cachedAfter_minute#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutMinutesValue#application#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="cachedAfter_seconds" value="#settings.cachedAfter_second#" class="number" required="yes" validate="integer" message="#stText.Scopes.TimeoutSecondsValue#application#stText.Scopes.TimeoutEndValue#"></td>
									</tr>
								<cfelse>
									<tr>
										<td align="center"><b>#settings.cachedAfter_day#</b></td>
										<td align="center"><b>#settings.cachedAfter_hour#</b></td>
										<td align="center"><b>#settings.cachedAfter_minute#</b></td>
										<td align="center"><b>#settings.cachedAfter_second#</b></td>
									</tr>
								</cfif>
							</tbody>
						</table>
						<div class="comment">#stText.setting.cachedAfterDesc#</div>

						<cfsavecontent variable="codeSample">
							this.query.cachedAfter = createTimeSpan(#settings.cachedAfter_day#,#settings.cachedAfter_hour#,#settings.cachedAfter_minute#,#settings.cachedAfter_second#);
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
						<!---
						<cfsavecontent variable="codeSample">
							this.applicationTimeout = createTimeSpan( #settings.cachedAfter_day#, #settings.cachedAfter_hour#, #settings.cachedAfter_minute#, #settings.cachedAfter_second# );
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>--->
					</td>
				</tr>
				</cfif>

				
				<!--- PagePool --->
				<tr>
					<th scope="row">#stText.setting.templateCache#</th>
					<td class="fieldPadded">
						<input class="button submit" type="submit" name="mainAction" value="#btnClearTemplateCache#">
						<div class="comment">#stText.setting.templateCacheClearDesc#</div>


						<cfsavecontent variable="codeSample">
							pagePoolClear();
						</cfsavecontent>
						<cfset renderCodingTip( codeSample, stText.settings.codetip )>
					</td>
				</tr>
				
				<!--- Object Cache --->
				<tr>
					<th scope="row">#stText.setting.objectCache#</th>
					<td class="fieldPadded">
						<cfloop collection="#objectCache#" index="cacheType" item="cacheSize">
							<cfif cacheSize EQ -1><cfcontinue></cfif>
							<input class="button submit" type="submit" name="mainAction" value="#clearButton[cacheType]#">
							<div class="comment">#replace(stText.setting.cacheClearDesc,'{name}',ucFirst(string:cacheType,doLowerIfAllUppercase:true))#</div>
						</cfloop>
						
						

						<cfsavecontent variable="codeSample">
&lt;cfobjectcache type="function"	action="clear"&gt;
&lt;cfobjectcache type="include"	action="clear"&gt;
&lt;cfobjectcache type="object"	action="clear"&gt;
&lt;cfobjectcache type="query"		action="clear"&gt;
&lt;cfobjectcache type="resource"	action="clear"&gt;
&lt;cfobjectcache type="template"	action="clear"&gt;
						</cfsavecontent>
						<cfset renderCodingTip( codeSample, stText.settings.codetip )>
					</td>
				</tr>
				<cfset stText.setting.applicationCache="Application path Cache">
				<cfset stText.setting.applicationCacheDesc="Press the button above to clear the Application path cache. This cache caches the location of the Application.[cfc|cfm] files.">
				<!--- Application.cfc Path Cache --->
				<tr>
					<th scope="row">#stText.setting.applicationCache#</th>
					<td class="fieldPadded">
						<input class="button submit" type="submit" name="mainAction" value="#btnClearApplicationCache#">
						<div class="comment">#stText.setting.applicationCacheDesc#</div>


						<cfsavecontent variable="codeSample">
							applicationPathCacheClear();
						</cfsavecontent>
						<cfset renderCodingTip( codeSample, stText.settings.codetip )>
					</td>
				</tr>
				
				<!--- Component Path Cache --->
				<tr>
					<th scope="row">#stText.setting.componentCache#</th>
					<td class="fieldPadded">
						<input class="button submit" type="submit" name="mainAction" value="#btnClearComponentCache#">
						<div class="comment">#stText.setting.componentCacheClearDesc#</div>


						<cfsavecontent variable="codeSample">
							componentCacheClear();
						</cfsavecontent>
						<cfset renderCodingTip( codeSample, stText.settings.codetip )>
					</td>
				</tr>
				
				<!--- Customtag Path Cache --->
				<tr>
					<th scope="row">#stText.setting.ctCache#</th>
					<td class="fieldPadded">
						<input class="button submit" type="submit" name="mainAction" value="#btnClearCTCache#">
						<div class="comment">#stText.setting.ctCacheDesc#</div>
						

						<cfsavecontent variable="codeSample">
							ctCacheClear();
						</cfsavecontent>
						<cfset renderCodingTip( codeSample, stText.settings.codetip )>
					</td>
				</tr>
				<cfif request.adminType EQ "server">
					<cfadmin
						action="getDevelopMode"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						returnVariable="mode">
					<!--- Type Checking --->
					<tr>
						<th scope="row">Develop Mode</th>
						<td class="fieldPadded">
							<label>
								<input class="checkbox" type="checkbox" name="mode" value="true"<cfif  mode.developMode EQ true> checked="checked"</cfif>>
							</label>
							<div class="comment">#stText.setting.developMode#</div>
						</td>
					</tr>
				</cfif>
				
				<cfif hasAccess>
					<cfmodule template="remoteclients.cfm" colspan="2">
				</cfif>
				
			</tbody>
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="2">
							<input class="bl button submit" type="submit" name="mainAction" value="#stText.Buttons.update#">
							<input class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" type="reset" name="cancel" value="#stText.Buttons.Cancel#">
							<cfif request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#"></cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>
</cfoutput>