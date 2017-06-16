<cfscript>
	letters='0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';
	
	function createRandomText(string length){
		var str='';
		for(var i=0;i<length;i++){
			str&=mid(letters,RandRange(1,len(letters)),1);
		}
		return str;
		
	}
</cfscript>

<cfadmin 
	action="getLoginSettings"
	type="#request.adminType#"
	returnVariable="loginSettings">

<cfparam name="form.rememberMe" default="s" />
<cfparam name="cookie.lucee_admin_lang" default="en">
<cfset session.lucee_admin_lang = cookie.lucee_admin_lang>
<cfif isDefined('url.action')>
	<cfset self=request.self&"?action="&url.action>
<cfelse>
	<cfset self=request.self>
</cfif>
<cfparam name="languages" default="#{en:'English',de:'Deutsch'}#">

<cfoutput>
	<script type="text/javascript">
		function doFocus() {
			document.forms.login.login_password#request.adminType#.focus();
			document.forms.login.login_password#request.adminType#.select();
		}
	</script>
	<cfif request.adminType EQ "web">
		<cfset filePath = getPageContext().getConfig().getConfigDir().getRealResource("lucee-web.xml.cfm")>
	<cfelse>
		<cfset filePath = "#replaceNocase(expandPath("/lucee-server"), "\context\context", "\context")#\lucee-server.xml" />
	</cfif>
	<cfformClassic name="login" action="#request.self#" method="post"><!--- onerror="customError"--->
		<cfset xmlObj = xmlParse(fileRead(filePath))>
		<cfset ipElem = XmlSearch(xmlObj, '//*[ local-name()=''IPsList'' ]')>
		<cfset IpList = "">
		<cfloop array="#ipElem#" index="i">
			<cfset ipList = listAppend(Iplist, "#i.xmlText#")>
		</cfloop>
			<table class="maintbl autowidth">
				<cfif listContains(Iplist, CGI.remote_addr) || CGI.remote_addr LTE "127.0.0.255" &&  CGI.remote_addr GTE "127.0.0.0" || CGI.remote_addr EQ "0:0:0:0:0:0:0:1">
					<tbody>
						<tr>
							<th scope="row" class="right">#stText.Login.Password#</th>
							<td><cfinputClassic type="password" name="login_password#request.adminType#" value="" passthrough='autocomplete="off"'
								class="medium" required="yes" message="#stText.Login.PasswordMissing#">
							</td>
						</tr>
						<cfset f="">
						<cfloop collection="#languages#" item="key">
							<cfif f EQ "" or key EQ session.lucee_admin_lang>
								<cfset f=key>
							</cfif>
						</cfloop>
						<tr>
							<th scope="row" class="right">#stText.Login.language#</th>
							<cfset aLangKeys = structKeyArray(languages)>
							<cfset arraySort(aLangKeys, "text")>
							<td>
								<select name="lang" class="medium">
									<cfloop from="1" to="#arrayLen(aLangKeys)#" index="iKey">
										<cfset key = aLangKeys[iKey]>
										<option value="#key#" <cfif key EQ session.lucee_admin_lang>selected</cfif>>#languages[key]#</option>
									</cfloop>
								</select>
							</td>
						</tr>
						<cfif loginSettings.captcha>
							<cfset cap=createRandomText(6)>
							<cfset session.cap=cap>
							<tr>
								<th scope="row" class="right">#stText.login.captchaHelp#</th>
								<td>
									<cfimage action="captcha" width="180" height="35" text="#cap#" difficulty="medium">
									<a style="font-size : 10px" href="#request.self#<cfif structKeyExists(url,"action")>?action=#url.action#</cfif>">Reload</a><br />
									<cfinputClassic type="text" name="captcha" value="" passthrough='autocomplete="off"'
										class="medium" required="yes" message="#stText.login.captchaHelpMiss#">
									<div class="comment">#stText.login.captchaHelpDesc#</div>
								</td>
							</tr>
						<cfelse>
							<cfset StructDelete(session,"cap",false)>
						</cfif>
						<cfif loginSettings.rememberMe>
						<tr>
							<th scope="row" class="right" nowrap="nowrap">#stText.Login.rememberMe#</th>
							<td>
								<select name="rememberMe" class="medium">
									<cfloop list="s,d,ww,m,yyyy" index="i">
										<option value="#i#"<cfif i eq form.rememberMe> selected</cfif>>#stText.Login[i]#</option>
									</cfloop>
								</select>
							</td>
						</tr>
					<cfelse>
						<input type="hidden" name="rememberMe" value="s">
					</cfif>
					</tbody>
					<tfoot>
						<tr>
							<td colspan="2" class="right"><input class="button submit" type="submit" name="submit" value="#stText.Buttons.Submit#"></td>
						</tr>
					</tfoot>
				<cfelse>
					<tbody>
						<tr>
							<td scope="row" nowrap="nowrap">
								<cfset printError(stText.login.restricted.ip)>
							</td>
						<tr>
					</tbody>
				</cfif>
			</table>
	</cfformClassic>
</cfoutput>