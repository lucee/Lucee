<!--- Action --->
<cfinclude template="ext.functions.cfm">

<cfset stVeritfyMessages=struct()>
<cfparam name="error" default="#struct(message:"",detail:"")#">
<cfparam name="form.mainAction" default="none">
<cfset error.message="">



<cftry>
	<cfswitch expression="#form.mainAction#">
		<cfcase value="#stText.Buttons.verify#">
			<cfset data.urls=toArrayFromForm("url")>
			<cfset data.rows=toArrayFromForm("row")>
			<cfset data.validUrls=[]>
			<cfloop from="1" to="#arrayLen(data.urls)#" index="idx">
				<cfif !isNull(data.rows[idx])>
					<cfset arrayAppend(data.validUrls,data.urls[idx])>
				</cfif>
			</cfloop>
			
			<cfif arrayLen(data.validUrls)>
				<cfset datas=getProvidersInfo(data.validUrls,false)>
			<cfelse>
				<cfset datas={}>
			</cfif>
			
			<cfloop collection="#datas#" index="provider" item="data">
				<!--- fails --->
				<cfif structKeyExists(data,"error")>
					<cfset stVeritfyMessages[provider].Label = "Error">
					<cfif data.status_code == 404>
						<cfset stVeritfyMessages[provider].message = "Was not able to retrieve data from ["&provider&"]. status code : 404">
						<cfset stVeritfyMessages[provider].detail ="">
					<cfelse>
						<cfset stVeritfyMessages[provider].message = "Failed to retrieve data from ["&provider&"].">
						<cfset stVeritfyMessages[provider].detail = "Message from server: "&data.error>
					</cfif>
				<cfelse>
					<cfset stVeritfyMessages[provider].Label = "OK">
				</cfif>
			</cfloop>
		</cfcase>
		<cfcase value="#stText.Buttons.save#">
			<cfset data.urls=toArrayFromForm("url")>
			<cfset data.rows=toArrayFromForm("row")>
			<cfloop from="1" to="#arrayLen(data.urls)#" index="idx">
				<cfif !isNull(data.rows[idx])>
					<cfadmin 
						action="updateRHExtensionProvider"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						url="#trim(data.urls[idx])#">
				</cfif>
			</cfloop>
		</cfcase>
		<cfcase value="#stText.Buttons.delete#">
			<cfset data.urls=toArrayFromForm("url")>
			<cfset data.rows=toArrayFromForm("row")>
			
			<cfloop from="1" to="#arrayLen(data.urls)#" index="idx">
				<cfif  !isNull(data.rows[idx])>
					<cfadmin 
						action="removeRHExtensionProvider"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						url="#trim(data.urls[idx])#">
				</cfif>
			</cfloop>
		</cfcase>
		<cfcase value="#stText.Buttons.install#">
			<cfif StructKeyExists(form,"row") and StructKeyExists(data,"ids") and ArrayIndexExists(data.ids,row)>
				<cflocation url="#request.self#?action=#url.action#&action2=install1&provider=#data.hashProviders[row]#&app=#data.ids[row]#" addtoken="no">
			</cfif>
		</cfcase>
	</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
		<cfset error.cfcatch=cfcatch>
	</cfcatch>
</cftry>

<!--- Redirect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "" and form.mainAction neq stText.Buttons.verify>
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>


<!--- Error Output --->
<cfset printError(error)>

<cfadmin 
	action="getRHExtensionProviders"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="providers">


<cfscript>
	hasAccess=true;
	datas = {};
	loop array="#queryColumnData(providers,'url')#" index="i" item="providerURL" {
		if (structKeyExists( session,"rhproviders") && structKeyExists( session.rhproviders,providerURL) ) {
			datas[providerURL] = session.rhproviders[providerURL]; // gets the provider info from the session which is already loaded
		} 
		else {
			thread name="provider:#providerURL#" {
			   thread.datas=getProviderInfo(provider:providerURL);
		   	}
			thread action="join" name="provider:#providerURL#" timeout=100;
			datas[providerURL]=isNull(cfthread["provider:#providerURL#"].datas)?{}:cfthread["provider:#providerURL#"].datas;
		}
	}
</cfscript>


<!--- 

list all mappings and display necessary edit fields --->

<cfoutput>
	
	<cfset doMode=false>
	<cfloop query="providers">
		<cfif 
			StructKeyExists(datas,providers.url) and 
			!isSimpleValue(datas[providers.url]) and
			StructKeyExists(datas[providers.url],"meta") and 
			StructKeyExists(datas[providers.url].meta,"mode") and 
			trim(datas[providers.url].meta.mode) EQ "develop">
			<cfset doMode=true>
		</cfif>
	</cfloop>
	
	<cfset columns=doMode?5:4>

	<div class="itemintro">#stText.ext.prov.IntroText#</div>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		
		<table class="maintbl checkboxtbl">
			<thead>
				<tr>
					<th><input type="checkbox" class="checkbox" name="rro" onclick="selectAll(this)"></th>
					<th>#stText.ext.prov.url#</th>
					<th>#stText.ext.prov.title#</th>
					<cfif doMode>
						<th>#stText.ext.prov.mode#</th>
					</cfif>
					<th>#stText.Settings.DBCheck#</th>
				</tr>
			</thead>
			<tbody id="extproviderlist">
				<cfloop query="providers">
					<tr>
						<!--- checkbox ---->
						<td>
							<cfif not providers.readOnly>
								<input type="checkbox" class="checkbox" name="row_#providers.currentrow#" value="#providers.currentrow#">
							</cfif>
						</td>
						<!--- url --->
						<td>
							<input type="hidden" name="url_#providers.currentrow#" value="#providers.url#">
							#providers.url#
							<cfset title="">
							<cfif StructKeyExists(stVeritfyMessages, providers.url)>
								<cfset msg=stVeritfyMessages[providers.url]>
								<cfif (structKeyExists(msg,"message") && len(trim(msg.message))) || 
									(structKeyExists(msg,"detail")  && len(trim(msg.detail)))>
									<cfset m=structKeyExists(msg,"message")?trim(msg.message):"">
									<cfset d=structKeyExists(msg,"detail")?trim(msg.detail):"">
									<cfset title=' title="#m# #d#"'>
									<div class="commentError">#m# #d#</div>
								</cfif>
							</cfif>
						</td>
						<cfset hasData = 
								StructKeyExists(datas,providers.url) and 
								!isSimpleValue(datas[providers.url]) and
								StructKeyExists(datas[providers.url],"meta")/>
						<cfif hasData>
							<cfset info=datas[providers.url].meta>
						</cfif>
						 
						<!--- title --->
						<td>
							<cfif hasData and StructKeyExists(info,"image")>
								<cfset dn=getDumpNail(info.image,100,30)>
								<cfif len(dn)>
									<img src="#dn#" border="0"/> &nbsp;
								</cfif>
							</cfif>
							<cfif hasData and StructKeyExists(info,"title") and len(trim(info.title))>
								#info.title#
							</cfif>
						</td>
						<!--- mode --->
						<cfif doMode>
							<td>
								<cfif hasData>
									<cfif StructKeyExists(info,"mode") and len(trim(info.mode))>
										#info.mode#
									<cfelse>
										production
									</cfif>
								</cfif>
							</td>
						</cfif>
						<!--- check --->
						<cfif StructKeyExists(stVeritfyMessages, providers.url)>
							<td class="tooltipMe favorite_inactive"#title#>#msg.label#</td>
						<cfelse>
							<td>&nbsp;</td>
						</cfif>
					</tr>
				</cfloop>
			</tbody>
			<cfif hasAccess>
				<tfoot>
					 <tr>
						<td colspan="#columns#">
							<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.verify#">
							<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.Delete#">
							<input type="reset" class="reset" name="cancel" value="#stText.Buttons.Cancel#">
						</td>	
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>
	
	<cfif hasAccess>
		<h2>#stText.ext.prov.new#</h2>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<input type="hidden" name="row_1" value="1">
			<table class="maintbl" style="width:75%">
				<tbody>
					<tr> 
						<th scope="row">
							#stText.ext.prov.host#
						</th>
						<td>
							<cfinputClassic onKeyDown="checkTheBox(this)" type="text" 
							name="url_1" value="" required="yes" class="xlarge">
							<div class="comment">#stText.ext.prov.hostDesc#</div>
						</td>
					</tr>
				</tbody>
				<tfoot>
					 <tr>
						<td colspan="2">
							<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.save#">
						</td>	
					</tr>
				</tfoot>
			</table>
		</cfformClassic>
	</cfif>


</cfoutput>
