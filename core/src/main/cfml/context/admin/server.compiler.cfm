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

<cftry>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="#stText.Buttons.Update#">
			<cfset preserveCase=false>
			<cfif isDefined('form.dotNotation') and form.dotNotation EQ "oc">
            	<cfset preserveCase=true>
            </cfif>
			<cfif isDefined('form.preserveCase') >
            	<cfset preserveCase=form.preserveCase>
            </cfif>

            <cfif not isDefined('form.suppressWhitespaceBeforeArgument')>
            	<cfset form.suppressWhitespaceBeforeArgument=false>
            </cfif>
            <cfif not isDefined('form.nullSupport')>
            	<cfset form.nullSupport=false>
            </cfif>
            <cfif not isDefined('form.preciseMath')>
            	<cfset form.preciseMath=false>
            </cfif>
			<cfif not isDefined('form.handleUnquotedAttributeValueAsString')>
            	<cfset form.handleUnquotedAttributeValueAsString=false>
            </cfif>
            <cfset systemOutput("preserveCase: #preserveCase#",1,1)>
			<cfadmin 
				action="updateCompilerSettings"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				
				nullSupport="#form.nullSupport#"
				preserveCase="#preserveCase#"
                suppressWSBeforeArg="#form.suppressWhitespaceBeforeArgument#"
                handleUnquotedAttrValueAsString="#form.handleUnquotedAttributeValueAsString#"
				templateCharset="#form.templateCharset#"
				externalizeStringGTE="#form.externalizeStringGTE#"
				preciseMath="#form.preciseMath#"
				remoteClients="#request.getRemoteClients()#">
	
		</cfcase>
	<!--- reset to server setting --->
		<cfcase value="#stText.Buttons.resetServerAdmin#">
			
			<cfadmin 
				action="updateCompilerSettings"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				
				nullSupport=""
				preserveCase=""
				suppressWSBeforeArg=""
				templateCharset=""
				handleUnquotedAttrValueAsString=""
				externalizeStringGTE=""
				preciseMath=""

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
						<cfsavecontent variable="codeSample">
&lt;cfprocessingdirective pageEncoding="#setting.templateCharset#">
&lt;!--- or --->
&lt;cfscript>processingdirective pageEncoding="#setting.templateCharset#";&lt;/cfscript>
						</cfsavecontent>
						<cfmodule template="systemSetting.cfm" 
							name="templateCharset" 
							value="#setting.templateCharset#"
							description="#stText.charset.templateCharsetDescription#"
							access="#hasAccess#"
							codeTip="#codeSample#"
							codeTipDesc="#stText.settings.codetip#">
							<input type="text" class="small" name="templateCharset" value="#setting.templateCharset#" />
						</cfmodule>
					</td>
				</tr>

				<!--- Externalize Strings --->
				<cfscript>
					if(setting.externalizeStringGTE < 10)setting.externalizeStringGTE=-1;
					else if(setting.externalizeStringGTE < 100)setting.externalizeStringGTE=10;
					else if(setting.externalizeStringGTE < 1000)setting.externalizeStringGTE=100;
					else  setting.externalizeStringGTE=1000;
				</cfscript>
				
				<tr>
					<th scope="row">#stText.setting.externalizeStringGTE?:""#</th>
					<td>
						
						<cfmodule template="systemSetting.cfm" 
							name="externalizeStringGte" 
							value="#setting.externalizeStringGTE#"
							description="#stText.setting.externalizeStringGTEDesc#"
							access="#hasAccess#"
							sp=false>
							<ul class="radiolist">
								<!--- not --->
								<cfloop list="-1,1000,100,10" item="val">
									<li>
										<label>
											<input class="radio" type="radio" name="externalizeStringGTE" value="#val#"<cfif setting.externalizeStringGTE == val> checked="checked"</cfif>>
											<b>#stText.setting["externalizeString"&replace(val,"-","_")]#</b>
										</label>
									</li>
								</cfloop>
							</ul>
						</cfmodule>
						
					</td>
				</tr>

				<!--- Null Support --->
				<tr>
					<th scope="row">#stText.compiler.nullSupport#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="nullSupport" 
							value="#setting.nullSupport#"
							access="#hasAccess#"
							sp=false>

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
						</cfmodule>
					</td>
				</tr>

				<!--- Dot Notation --->
				<tr>
					<th scope="row">#stText.setting.dotNotation#</th>
					<td>
<cfsavecontent variable="codeSample">
&lt;cfprocessingdirective preserveCase="#!setting.DotNotationUpperCase#">
&lt;!--- or --->
&lt;cfscript>processingdirective preserveCase="#!setting.DotNotationUpperCase#";&lt;/cfscript>
</cfsavecontent>

						<cfmodule template="systemSetting.cfm" 
							name="preserveCase" 
							value="#setting.nullSupport#"
							access="#hasAccess#"
							codeTip="#codeSample#"
							codeTipDesc="#stText.settings.codetip#"
							descOnTop=true>
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
						
						</cfmodule>
					</td>
				</tr>
				
				<!--- precise math --->
				<tr>
					<th scope="row">#stText.setting.preciseMath#</th>
					<td>

						<cfmodule template="systemSetting.cfm" 
							name="preciseMath" 
							value="#setting.preciseMath#"
							access="#hasAccess#"
							description="#stText.setting.preciseMathDesc#">
						<input class="checkbox" type="checkbox" name="preciseMath" value="true" <cfif setting.preciseMath>checked="checked"</cfif> />
						</cfmodule>

					</td>
				</tr>
				
				<!--- Suppress Whitespace in front of cfargument --->
				<tr>
					<th scope="row">#stText.setting.suppressWSBeforeArg#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="suppressWhitespaceBeforeArgument" 
							value="#setting.suppressWSBeforeArg#"
							access="#hasAccess#"
							description="#stText.setting.suppressWSBeforeArgDesc#"
							sp=false>
						<input class="checkbox" type="checkbox" name="suppressWhitespaceBeforeArgument" value="true" <cfif setting.suppressWSBeforeArg>checked="checked"</cfif> />
						</cfmodule>
					</td>
				</tr>
				
				<!--- how to handle unquoted attribute values --->
				<tr>
					<th scope="row">#stText.setting.handleUnquotedAttrValueAsString#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="handleUnquotedAttributeValueAsString" 
							value="#setting.handleUnquotedAttrValueAsString#"
							access="#hasAccess#"
							description="#stText.setting.handleUnquotedAttrValueAsStringDesc#"
							sp=false>
							<input class="checkbox" type="checkbox" name="handleUnquotedAttributeValueAsString" value="true" <cfif setting.handleUnquotedAttrValueAsString>checked="checked"</cfif> />
						</cfmodule>
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
							<cfif not request.singleMode and request.adminType EQ "web"><input class="br submit" type="submit" class="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#"></cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>
</cfoutput>