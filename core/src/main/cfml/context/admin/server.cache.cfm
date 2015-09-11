<cfsilent>
<cfset error.message="">
<cfset error.detail="">

<cfset stText.setting.typeChecking="UDF Type Checking">
<cfset stText.setting.typeCheckingDesc="If disabled Lucee ignores type defintions with function arguments and return values">
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




<cfset btnClearComponentCache=replace(stText.setting.componentCacheClear,'{count}',structCount(componentCacheList()))>
<cfset btnClearCTCache=replace(stText.setting.ctCacheClear,'{count}',structCount(ctCacheList()))>
</cfsilent>
<cfif hasAccess>
	<cftry>
		<cfswitch expression="#form.mainAction#">
		
			<cfcase value="#btnClearComponentCache#">
				<cfset componentCacheClear()>
			</cfcase>
			<cfcase value="#btnClearCTCache#">
				<cfset ctCacheClear()>
			</cfcase>
			<cfcase value="#btnClearTemplateCache#">
				<cfset pagePoolClear()>
			</cfcase>
			<cfcase value="#clearButton.function#">
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
				<cfadmin 
					action="updatePerformanceSettings"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					inspectTemplate="#form.inspectTemplate#"
					typeChecking="#!isNull(form.typeChecking) and form.typeChecking EQ true#"
					remoteClients="#request.getRemoteClients()#"
					>
			
			</cfcase>
		<!--- reset to server setting --->
			<cfcase value="#stText.Buttons.resetServerAdmin#">
				<cfadmin 
					action="updatePerformanceSettings"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					inspectTemplate=""
					typeChecking=""
					
					remoteClients="#request.getRemoteClients()#"
					>
			
			</cfcase>
		</cfswitch>
		<cfcatch>
			<cfset error.message=cfcatch.message>
			<cfset error.detail=cfcatch.Detail>
		</cfcatch>
	</cftry>
</cfif>

<cfadmin 
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	action="getPerformanceSettings"
	returnVariable="Settings">
	
<!--- 
Redirtect to entry --->
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
	
	<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">
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
						<div class="comment">#stText.setting.ctCacheClearDesc#</div>
						

						<cfsavecontent variable="codeSample">
							ctCacheClear();
						</cfsavecontent>
						<cfset renderCodingTip( codeSample, stText.settings.codetip )>
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
							<input class="bl button submit" type="submit" name="mainAction" value="#stText.Buttons.update#">
							<input class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" type="reset" name="cancel" value="#stText.Buttons.Cancel#">
							<cfif request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#"></cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfform>
</cfoutput>