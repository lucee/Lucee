<cfset error.message="">
<cfset error.detail="">

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

<cfset stText.setting.handleUnquotedAttrValueAsStringDesc='Handle unquoted tag attribute values as strings.
<br>Example:<br>
&lt;cfmail subject=sub from="##f##" to="##t##"/><br>
<br>The value from attribute "subject" is not quoted, in that case if enabled the string "sub" submitted to the tag, if not enabled Lucee looks for a variable "sub".'>

<cftry>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="#stText.Buttons.Update#">
			<cfset dotNotUpper=true>
			<cfif isDefined('form.dotNotation') and form.dotNotation EQ "oc">
            	<cfset dotNotUpper=false>
            </cfif>
            <cfif not isDefined('form.suppressWSBeforeArg')>
            	<cfset form.suppressWSBeforeArg=false>
            </cfif>
            <cfif not isDefined('form.nullSupport')>
            	<cfset form.nullSupport=false>
            </cfif>
			<cfif not isDefined('form.handleUnquotedAttrValueAsString')>
            	<cfset form.handleUnquotedAttrValueAsString=false>
            </cfif>
            
			<cfadmin 
				action="updateCompilerSettings"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				
				nullSupport="#form.nullSupport#"
				dotNotationUpperCase="#dotNotUpper#"
                suppressWSBeforeArg="#form.suppressWSBeforeArg#"
                handleUnquotedAttrValueAsString="#form.handleUnquotedAttrValueAsString#"
				templateCharset="#form.templateCharset#"
				externalizeStringGTE="#form.externalizeStringGTE#"
				remoteClients="#request.getRemoteClients()#">
	
		</cfcase>
	<!--- reset to server setting --->
		<cfcase value="#stText.Buttons.resetServerAdmin#">
			
			<cfadmin 
				action="updateCompilerSettings"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				
				nullSupport=""
				dotNotationUpperCase=""
				suppressWSBeforeArg=""
				templateCharset=""
				handleUnquotedAttrValueAsString=""
				externalizeStringGTE=""

				remoteClients="#request.getRemoteClients()#">
	
		</cfcase>
	</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
		<cfset error.cfcatch=cfcatch>
	</cfcatch>
</cftry>


<!---  	templates.error.error_cfm$cf.str(Llucee/runtime/PageContext;II)Ljava/lang/String;
Error Output --->
<cfset printError(error)>


<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>




<cfadmin 
	action="getCompilerSettings"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="setting">

<cfif not hasAccess><cfset noAccess(stText.setting.noAccess)></cfif>

<cfoutput>
	<div class="pageintro">#stText.setting.compiler#</div>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<tbody>
				
				
				<!--- Template --->
				<tr>
					<th scope="row">#stText.charset.templateCharset#</th>
					<td>
						<cfif hasAccess>
							<input type="text" class="small" name="templateCharset" value="#setting.templateCharset#" />
						<cfelse>
							<input type="hidden" name="templateCharset" value="#setting.templateCharset#">
							<b>#setting.templateCharset#</b>
						</cfif>
						<div class="comment">#stText.charset.templateCharsetDescription#</div>
						<cfsavecontent variable="codeSample">
&lt;cfprocessingdirective pageEncoding="#setting.templateCharset#">
&lt;!--- or --->
&lt;cfscript>processingdirective pageEncoding="#setting.templateCharset#";&lt;/cfscript>
						</cfsavecontent>
						<cfset renderCodingTip( codeSample ,stText.settings.codetip)>
					</td>
				</tr>

				<!--- Externalize Strings --->
				<cfset stText.settings.externalizeStringGTE="Externalize strings">
				<cfset stText.settings.externalizeStringGTEDesc="Externalize strings from generated class files to separate files. This can drastically reduce the memory footprint for templates but can have a negative impact on execution times. A lower ""breakpoint"" will cause slower execution than a higher breakpoint.">

				<cfset stText.settings.externalizeString_1="do not externalize any strings">
				<cfset stText.settings.externalizeString10="externalize strings larger than 10 characters">
				<cfset stText.settings.externalizeString100="externalize strings larger than 100 characters">
				<cfset stText.settings.externalizeString1000="externalize strings larger than 1000 characters">
				<cfset stText.settings.externalizeStringDisabled="disabled">
				<cfscript>
					if(setting.externalizeStringGTE < 10)setting.externalizeStringGTE=-1;
					else if(setting.externalizeStringGTE < 100)setting.externalizeStringGTE=10;
					else if(setting.externalizeStringGTE < 1000)setting.externalizeStringGTE=100;
					else  setting.externalizeStringGTE=1000;
				</cfscript>
				
				<tr>
					<th scope="row">#stText.settings.externalizeStringGTE#</th>
					<td>
						<!---<div class="warning nofocus">
					This feature is experimental.
					If you have any problems while using this functionality,
					please post the bugs and errors in our
					<a href="https://issues.lucee.org" target="_blank">bugtracking system</a>. 
				</div>--->

						<cfif hasAccess>


							<ul class="radiolist">
								
								<!--- not --->
								<cfloop list="-1,1000,100,10" item="val">
									<li>
										<label>
											<input class="radio" type="radio" name="externalizeStringGTE" value="#val#"<cfif setting.externalizeStringGTE == val> checked="checked"</cfif>>
											<b>#stText.settings["externalizeString"&replace(val,"-","_")]#</b>
										</label>
									</li>
								</cfloop>
								<!--- <div class="comment">#replace(stText.setting.dotNotationOriginalCaseDesc, server.separator.line, '<br />', 'all')#</div> --->
								
							</ul>
						<cfelse>
							<input type="hidden" name="externalizeStringGTE" value="#setting.externalizeStringGTE#">
							<b><cfif setting.externalizeStringGTE==-1>#yesNoFormat(false)#<cfelse>#stText.settings["externalizeString"&replace(setting.externalizeStringGTE,"-","_")]#</cfif></b>
						</cfif>
						<div class="comment">#stText.settings.externalizeStringGTEDesc#</div>
						
					</td>
				</tr>

<!---
			</tbody>
		</table>

		<h3>#stText.general.dialect.cfml#</h3>
		<div class="itemintro">#stText.general.dialect.cfmlDesc#</div>
		
		<table class="maintbl">
			<tbody>
--->
				<!--- Null Support --->
				<tr>
					<th scope="row">#stText.compiler.nullSupport#</th>
					<td>
						<cfif hasAccess >
							<ul class="radiolist">
								<li>
									<!--- full --->
									<label>
										<input class="radio" type="radio" name="nullSupport" value="true"<cfif setting.nullSupport> checked="checked"</cfif>>
										<b>#stText.compiler.nullSupportFull#</b>
									</label>
									<div class="comment">#stText.compiler.nullSupportFullDesc#</div>
								</li>
								<li>
									<!--- partial --->
									<label>
										<input class="radio" type="radio" name="nullSupport" value="false"<cfif !setting.nullSupport> checked="checked"</cfif>>
										<b>#stText.compiler.nullSupportPartial#</b>
									</label>
									<div class="comment">#stText.compiler.nullSupportPartialDesc#</div>
								</li>
							</ul>
						<cfelse>
							<cfset strNullSupport=setting.nullSupport?"full":"partial">
							<input type="hidden" name="nullSupport" value="#setting.nullSupport#">
							<b>#stText.compiler["nullSupport"& strNullSupport]#</b><br />
							<div class="comment">#stText.compiler["nullSupport"& strNullSupport&"Desc"]#</div>
						</cfif>
					</td>
				</tr>

				<!--- Dot Notation --->
				<tr>
					<th scope="row">#stText.setting.dotNotation#</th>
					<td>
						<cfif hasAccess>
							<ul class="radiolist">
								<li>
									<!--- original case --->
									<label>
										<input class="radio" type="radio" name="dotNotation" value="oc"<cfif !setting.dotNotationUpperCase> checked="checked"</cfif>>
										<b>#stText.setting.dotNotationOriginalCase#</b>
									</label>
									<div class="comment">#replace(stText.setting.dotNotationOriginalCaseDesc, server.separator.line, '<br />', 'all')#</div>
								</li>
								<li>
									<!--- upper case --->
									<label>
										<input class="radio" type="radio" name="dotNotation" value="uc"<cfif setting.dotNotationUpperCase> checked="checked"</cfif>>
										<b>#stText.setting.dotNotationUpperCase#</b>
									</label>
									<div class="comment">#replace(stText.setting.dotNotationUpperCaseDesc, server.separator.line, '<br />', 'all')#</div>
								</li>
							</ul>
						<cfelse>
							<cfset strDotNotation=setting.dotNotationUpperCase?"uc":"oc">
							<cfset strDotNotationID=setting.dotNotationUpperCase?"Upper":"Original">
							<input type="hidden" name="dotNotation" value="#strDotNotation#">
							<b>#stText.setting["dotNotation"& strDotNotationID &"Case"]#</b><br />
							<div class="comment">#replace(stText.setting["dotNotation"& strDotNotationID &"CaseDesc"], server.separator.line, '<br />', 'all')#</div>
						</cfif>
						<cfsavecontent variable="codeSample">
&lt;cfprocessingdirective preserveCase="#!setting.DotNotationUpperCase#">
&lt;!--- or --->
&lt;cfscript>processingdirective preserveCase="#!setting.DotNotationUpperCase#";&lt;/cfscript>
						</cfsavecontent>
						<cfset renderCodingTip( codeSample ,stText.settings.codetip)>
					</td>
				</tr>
				
				<!--- Suppress Whitespace in front of cfargument --->
				<tr>
					<th scope="row">#stText.setting.suppressWSBeforeArg#</th>
					<td>
						<cfif hasAccess>
        					<input class="checkbox" type="checkbox" name="suppressWSBeforeArg" value="true" <cfif setting.suppressWSBeforeArg>checked="checked"</cfif> />
						<cfelse>
							<b>#yesNoFormat(setting.suppressWSBeforeArg)#</b><br /><input type="hidden" name="suppressWSBeforeArg" value="#setting.suppressWSBeforeArg#">
						</cfif>
						<div class="comment">#stText.setting.suppressWSBeforeArgDesc#</div>
					</td>
				</tr>
				
				<!--- how to handle unquoted attribute values --->
				<tr>
					<th scope="row">#stText.setting.handleUnquotedAttrValueAsString#</th>
					<td>
						<cfif hasAccess>
        					<input class="checkbox" type="checkbox" name="handleUnquotedAttrValueAsString" value="true" <cfif setting.handleUnquotedAttrValueAsString>checked="checked"</cfif> />
						<cfelse>
							<b>#yesNoFormat(setting.handleUnquotedAttrValueAsString)#</b><br /><input type="hidden" 
							name="handleUnquotedAttrValueAsString" value="#setting.handleUnquotedAttrValueAsString#">
						</cfif>
						<div class="comment">#stText.setting.handleUnquotedAttrValueAsStringDesc#</div>
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
							<input type="submit" class="bl submit" name="mainAction" value="#stText.Buttons.Update#">
							<input type="reset" class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" name="cancel" value="#stText.Buttons.Cancel#">
							<cfif request.adminType EQ "web"><input class="br submit" type="submit" class="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#"></cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>
</cfoutput>