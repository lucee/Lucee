<cfif request.admintype == "server">
<cfscript>
	error.message="";
	error.detail="";

	admin 
		action="getAIEngines"
		type=request.adminType
		password=session["password"&request.adminType]
		returnVariable="engines";
</cfscript>
<!--- 
<cftry>
	<cfset stVeritfyMessages = StructNew()>
	<cfswitch expression="#form.mainAction#">
		<cfcase value="#stText.Buttons.resetServerAdmin#">
			<cfadmin 
				action="removeCacheDefaultConnection"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				remoteClients="#request.getRemoteClients()#">				
		</cfcase>
		<cfcase value="#stText.Buttons.update#">
            <cfadmin 
                action="updateCacheDefaultConnection"
                type="#request.adminType#"
                password="#session["password"&request.adminType]#"
                object="#StructKeyExists(form,'default_object')?form.default_object:''#"
                template="#StructKeyExists(form,'default_template')?form.default_template:''#"
                query="#StructKeyExists(form,'default_query')?form.default_query:''#"
                resource="#StructKeyExists(form,'default_resource')?form.default_resource:''#"
                function="#StructKeyExists(form,'default_function')?form.default_function:''#"
                include="#StructKeyExists(form,'default_include')?form.default_include:''#"
                http="#StructKeyExists(form,'default_http')?form.default_http:''#"
                file="#StructKeyExists(form,'default_file')?form.default_file:''#"
                webservice="#StructKeyExists(form,'default_webservice')?form.default_webservice:''#"
                remoteClients="#request.getRemoteClients()#">				
		</cfcase>
		<cfcase value="#stText.Buttons.Delete#">
			<cfset data.names=toArrayFromForm("name")>
				<cfset data.rows=toArrayFromForm("row")>
				<cfset data.names=toArrayFromForm("name")>
				<cfloop index="idx" from="1" to="#arrayLen(data.names)#">
					<cfif arrayIndexExists(data.rows, idx) and data.names[idx] NEQ "">
						<cfadmin 
							action="removeCacheConnection"
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
				<cfset data.names=toArrayFromForm("name")>
				
				<cfloop index="idx" from="1" to="#arrayLen(data.names)#">
					<cfif arrayIndexExists(data.rows, idx) and data.names[idx] NEQ "">
						<cftry>
							<cfadmin 
								action="verifyCacheConnection"
								type="#request.adminType#"
								password="#session["password"&request.adminType]#"
								name="#data.names[idx]#">
								<cfset stVeritfyMessages["#data.names[idx]#"].Label = "OK">
							<cfcatch>
								<!--- <cfset error.message=error.message&data.names[idx]&": "&cfcatch.message&"<br>"> --->
								<cfset stVeritfyMessages[data.names[idx]].Label = "Error">
								<cfset stVeritfyMessages[data.names[idx]].message = cfcatch.message>
							</cfcatch>
						</cftry>
					</cfif>
				</cfloop>
				
		</cfcase>
	</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
	</cfcatch>
</cftry>
--->
<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "" and form.mainAction neq stText.Buttons.verify>
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>
<cfscript>
	querySort(engines,"name");

	stText.Settings.ai.model="Model";
	stText.Settings.ai.class="Class";
	stText.Settings.ai.default="Default";
	stText.Settings.ai.typeUrl="Type / URL";
	stText.Settings.ai.titleExisting="List of Defined AI Engines";
	stText.Settings.ai.descExisting="This section displays all the AI engines currently configured for this environment. An AI engine represents a Java class that implements the 'AIEngine' interface, allowing you to point to various AI service endpoints (such as ChatGPT, Gemini, or Ollama). The same Java class can be used with different endpoints to manage different AI engines. Please note that this feature is still in an experimental phase, and the interface is subject to changes and refinements before the final release.";
</cfscript>
<cfoutput>
	<div class="warning nofocus">
		This feature is experimental and may be subject to change.
		If you encounter any issues while using this functionality, 
		please report bugs and errors in our 
		<a href="https://issues.lucee.org" target="_blank">bug tracking system</a>.
	</div>


	<!--- LIST CACHE --->
		<h2>#stText.Settings.ai.titleExisting#</h2>
		<div class="itemintro">#stText.Settings.ai.descExisting#</div>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<table class="maintbl checkboxtbl">
				<thead>
					<tr>
						<th width="1%"><input type="checkbox" class="checkbox" name="rowreadonly" <!---  onclick="selectAll(this)"--->></th>
						<th>#stText.Settings.cache.name#</th>
						<th>#stText.Settings.ai.typeURL#</th>
						<th>#stText.Settings.ai.model#</th>
						<th>#stText.Settings.ai.class#</th>
						<th>#stText.Settings.cache.default#</th>
						<th width="3%"></th>
					</tr>
				</thead>
				<tbody>
					<cfloop query="engines">
						<cfscript>
							hash=Hash(engines.name);
							if(hash==(url.hash?:"")) {
								nam=engines.name;
								val=[
									"class": engines.class
									,"custom":engines.properties
									
								];
								if(!isEmpty(trim(engines.default?:""))) val["default"]=engines.default;
								val=serializeJSON(var:val,compact:false);
							}

						</cfscript>
						<tr>
							<td>
								<input type="checkbox" class="checkbox" name="row_#engines.currentrow#" value="#engines.currentrow#">
							</td>
							<td nowrap><input type="hidden" name="name_#engines.currentrow#" value="#engines.name#">#engines.name#</td>
							<td nowrap>#engines.properties.type?:(engines.properties.url?:"-")#</td>
							<td nowrap>#engines.properties.model?:""#</td>
							<td nowrap>#listLast(engines.class,".")#</td>
							<td nowrap>#engines.default?:"-"#</td>
							<td>
								#renderEditButton("#request.self#?action=#url.action#&hash=#hash#")#
							</td>
						</tr>
					</cfloop>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="7">
							<div class="warning nofocus">
								Currently, these settings can only be modified in the <code>.CFCConfig.json</code> file. 
								This page only displays the AI engines configured in <code>.CFCConfig.json</code>, but the ability to modify them directly here will be available soon.
							</div>
<!---
							<input type="submit" class="bl button submit enablebutton" name="mainAction" value="#stText.Buttons.verify#">
							<input type="submit" class="bm button submit enablebutton" name="mainAction" value="#stText.Buttons.delete#">--->
						
							<input type="reset" class="b button reset enablebutton" id="clickCancel" name="cancel" value="#stText.Buttons.Cancel#">
						</td>	
					</tr>
				</tfoot>
			 </table>
		</cfformClassic>
</cfoutput>



<!--- 
	Create/Update Engine --->
<cfscript>
	stText.Settings.ai.newUpate="Create or update a AI Engine";
	stText.Settings.ai.newUpateDesc="Create or update a AI Engine";
	stText.Settings.ai.raw="Raw Json Value";


	default='{
	"class": "lucee.runtime.ai.openai.OpenAIEngine",
	"custom": {
		"type": "",
		"model": "",
		"message": "",
		"timeout": 3000
	},
	"default": ""
}';
</cfscript>
	<cfoutput>
			<h2>#stText.Settings.ai.newUpate#</h2>
			<div class="itemintro">Use this form to configure a new AI engine endpoint that integrates with different AI services, such as ChatGPT, Gemini, or Ollama. By setting up a new endpoint, you can define the specific details required for connecting to the AI service, including the model, API endpoint, and any necessary authentication credentials. This allows you to manage multiple AI engines within your environment, each pointing to different services or configurations.</div>
			<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
				<table class="maintbl" wstyle="width:400px;">
					<tbody>
						<tr>
							<th width="1%" scope="row" nowrap="nowrap">#stText.Settings.cache.Name#</th>
							<td><cfinputClassic type="text" name="_name" value="#nam?:""#" class="xlarge" required="yes" 
								message="#stText.Settings.cache.nameMissing#"></td>
						</tr>
						<tr>
							<th scope="row" nowrap="nowrap">#stText.Settings.ai.raw#</th>
							<td><textarea style="width:100%;height:160px;" class="large" name="_name">#val?:(default?:"")#</textarea></td>
						</tr>
						<!---<tr>
							<th scope="row">#stText.Settings.cache.type#</th>
							<td>
								<select name="class" class="xlarge">
									<cfloop list="#_drivers#" index="key">
										<cfset driver=drivers[key]>
										<!--- Workaround for EHCache Extension --->
										<cfset clazz=trim(driver.getClass())>
										<cfif "lucee.extension.io.cache.eh.EHCache" EQ clazz or "lucee.runtime.cache.eh.EHCache" EQ clazz>
											<cfset clazz="org.lucee.extension.cache.eh.EHCache">
										</cfif>
										<option value="#clazz#">#trim(driver.getLabel())#</option>
									</cfloop>
								</select>
							</td>
						</tr>--->
					</tbody>
					<tfoot>
						<tr>
							<td colspan="2">
								<div class="warning nofocus">
									Currently, these settings can only be modified in the <code>.CFCConfig.json</code> file. 
									This page only displays the AI engines configured in <code>.CFCConfig.json</code>, but the ability to modify them directly here will be available soon.
								</div>
								<!--- <input type="submit" class="bl button submit" name="run" value="#stText.Buttons.create#"> --->
								<input type="reset" class="b button reset" name="cancel" value="#stText.Buttons.Cancel#">
							</td>
						</tr>
					</tfoot>
				</table>   
			</cfformClassic>
	</cfoutput>
</cfif>