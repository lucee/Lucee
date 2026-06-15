<cfscript>
function obfuscate(raw) {
	
	if(len(raw)<7)	{
		if(len(raw)==0) return "";
		return repeatString("*",len(raw));
	}
	return left(raw,3) & repeatString("*",len(raw)-6) & right(raw,3);
}
function addZero(str) {
	return arguments.str;
}

function aiGetDriverFieldNames(required any driver) {
	var names = [];
	for (var field in arguments.driver.getCustomFields()) {
		if (!isInstanceOf(field, "Group")) {
			arrayAppend(names, field.getName());
		}
	}
	return names;
}

function aiExtractPassthroughCustom(required struct custom, required array driverFieldNames) {
	var passthrough = {};
	for (var key in arguments.custom) {
		if (!arrayFindNoCase(arguments.driverFieldNames, key)) {
			passthrough[key] = arguments.custom[key];
		}
	}
	return passthrough;
}

function aiMergePassthroughJson(required struct custom, required string passthroughJson, required array driverFieldNames) {
	var json = trim(arguments.passthroughJson ?: "");
	if (!len(json)) {
		return arguments.custom;
	}
	var passthrough = deserializeJSON(json);
	if (!isStruct(passthrough)) {
		throw(message="Passthrough configuration must be a JSON object", type="admin.ai.passthrough");
	}
	for (var key in passthrough) {
		if (!arrayFindNoCase(arguments.driverFieldNames, key)) {
			arguments.custom[key] = passthrough[key];
		}
	}
	return arguments.custom;
}

function aiFormatPassthroughJson(any value) {
	if (isNull(arguments.value)) {
		return "";
	}
	if (isSimpleValue(arguments.value) && !len(trim(arguments.value))) {
		return "";
	}
	var data = arguments.value;
	if (isSimpleValue(data)) {
		try {
			data = deserializeJSON(trim(data));
		}
		catch (any e) {
			return trim(data);
		}
	}
	if (isStruct(data) && !structCount(data)) {
		return "";
	}
	return serializeJSON(var=data, compact=false);
}
</cfscript>



<cftry>
	<cfset stVeritfyMessages = StructNew()>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="#stText.Buttons.submit#">
			<cfscript>
				custom={};
				defaults={};

				// convert the custom part fields
				loop collection=form item="key" {
					if(left(key,13) EQ "custompart_d_") {
						name=mid(key,14,10000);
						custom[name]=(form["custompart_d_"&name]*86400)+(form["custompart_h_"&name]*3600)+(form["custompart_m_"&name]*60)+form["custompart_s_"&name];
					}
				}	

				// convert the custom fields
				loop collection=form item="key" {
					if(left(key,7) EQ "custom_") {
						custom[mid(key,8,10000)]=form[key];
					}
				}

				// convert the custom default fields
				loop collection=form item="key" {
					if(left(key,15) EQ "default_custom_") {
						defaults[mid(key,16,10000)]=form[key];
					}
				}
				connectionName = trim(form.name);
				if (structKeyExists(form, "_name") && len(trim(form._name))) {
					connectionName = trim(form._name);
				}
				else if (reFind("^.+,[0-9A-F]{32}$", connectionName)) {
					baseName = listFirst(connectionName);
					if (hash(baseName) == uCase(listLast(connectionName))) {
						connectionName = baseName;
					}
				}

				if(len(defaults)) {
					loop query=connections {
						if(connections.name==connectionName) {
							defaultCustom=connections.custom;
							break;
						}
					}
					if(!isNull(defaultCustom)) {
						loop struct=defaults index="k" item="v" {
							if(structKeyExists(custom,k) && custom[k]==v) {
								custom[k]=defaultCustom[k]?:v;
							}
						}
					}
				}

				driverFieldNames = aiGetDriverFieldNames(drivers[trim(form.class)]);
				try {
					custom = aiMergePassthroughJson(custom, structKeyExists(form, "passthroughJson") ? form.passthroughJson : "", driverFieldNames);
				}
				catch (any e) {
					if (e.type == "admin.ai.passthrough") {
						throw(message=e.message, detail="The passthrough configuration must be valid JSON defining an object, for example: {""max_tokens"":8192}");
					}
					throw(message="Failed to parse passthrough JSON", detail=e.message);
				}
			</cfscript>
			<cfadmin 
				action="updateAIConnection"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				
				
				name="#connectionName#" 
				class="#trim(form.class)#"
				bundleName="#isNull(form.bundleName)?"":trim(form.bundleName)#"
				bundleVersion="#isNull(form.bundleVersion)?"":trim(form.bundleVersion)#"


				default="#StructKeyExists(form,'default')?form.default:""#" 
				custom="#custom#"
				
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
<cfif cgi.request_method EQ "POST" and error.message EQ "" and form.mainAction neq "none">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<cfset isNew=false>
<cfif StructKeyExists(url,'name')>
	<cfloop query="connections" >
		<cfif hash(connections.name) EQ url.name>
			<cfset connection=querySlice(connections,connections.currentrow,1)>
			<cfset driver=drivers[connections.class]>
			<cftry>
				<cfset validConnection = true> 
				<!--- <cfadmin  
								
				action="verifyAIConnection"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				name="#connection.name#"
				returnvariable="qa">--->
				<cfcatch>
					<cfset validConnection = false>
					<cfset error.message = cfcatch.message>
				</cfcatch>
			</cftry>
		</cfif>
	</cfloop>
<cfelse>
	<cfset isNew=true>
	<cfset connection=struct()>
	<cfset connection.class=form.class>
	<cfset connection.storage=false>
	<cfset connection.default=false>
	<cfset connection.custom=struct()>
	<cfset driver=drivers[form.class]>
	<!--- <cfset connection.name=lcase(driver.getLabel())&"_"&FormatBaseN(randRange(1,999999),36)> --->
	<cfset connection.name="">
</cfif>

<cfset driverFieldNames = aiGetDriverFieldNames(driver)>
<cfset passthroughCustom = aiExtractPassthroughCustom(isStruct(connection.custom ?: {}) ? connection.custom : {}, driverFieldNames)>
<cfset passthroughJson = aiFormatPassthroughJson(passthroughCustom)>
<cfif structKeyExists(form, "passthroughJson") && cgi.request_method EQ "POST" && form.mainAction EQ stText.Buttons.submit && len(error.message)>
	<cfset passthroughJson = aiFormatPassthroughJson(form.passthroughJson)>
</cfif>
<cfset passthroughShortcuts = driver.getPassthroughShortcuts()>





<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "" and form.mainAction neq "none">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>
<cfoutput>
	<!--- 
	Error Output --->
	<cfset printError(error)>
	<h2><cfif structKeyExists(driver,"getLabelLong")>#driver.getLabelLong()#<cfelse>#driver.getLabel()#</cfif></h2>
	<div class="pageintro">#driver.getDescription()#</div>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#&action2=create#iif(isDefined('url.name'),de('&name=##url.name##'),de(''))#" method="post">
		<cfinputClassic type="hidden" name="class" value="#driver.getClass()#">
		<cfif !isNull(driver.getBundleName)><cfinputClassic type="hidden" name="bundleName" value="#driver.getBundleName()#"></cfif>
		<cfif !isNull(driver.getBundleVersion)><cfinputClassic type="hidden" name="bundleVersion" value="#driver.getBundleVersion()#"></cfif>
		
		<cfinputClassic type="hidden" name="_name" value="#connection.name#" >
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.Settings.cache.Name#</th>
					<td>
						<cfif isNew>
							<cfinputClassic type="text" 
									name="name" 
									value="#connection.name#" class="large" style="width:100%" required="true" 
									message="Missing value for field name">
							
						<cfelse>
						<h3>#connection.name#</h3>
						<cfinputClassic type="hidden" name="name" value="#connection.name#" >
						</cfif><div class="comment">#stText.Settings.ai.NameDesc#</div>
					</td>
				</tr>
			</tbody>
		</table>
		<br />
		<table class="maintbl">
			<tbody>
				<cfset custom=connection.custom>
				<cfloop array="#driver.getCustomFields()#" index="field">
					<cfif isInstanceOf(field,"Group")>
							</tbody>
						</table>
						<h#field.getLevel()#>#field.getDisplayName()#</h#field.getLevel()#>
						<div class="itemintro">#field.getDescription()#</div>
						<table class="maintbl">
							<tbody>
						<cfcontinue>
					</cfif>

					<cfset doBR=true>
					<cfif StructKeyExists(custom,field.getName())>
						<cfset default=custom[field.getName()]>
					<cfelseif isNew>
						<cfset default=field.getDefaultValue()>
					<cfelse>
						<cfset default="">
					</cfif>
					<cfset type=field.getType()>
					<tr>
						<th scope="row">#field.getDisplayName()#</th>
						<td>
							<cfif type EQ "text" or type EQ "password">
								<cfif type EQ "password">
									<cfset default=obfuscate(default)>
									<input type="hidden" name="default_custom_#field.getName()#" value="#default#">
								</cfif>
								<cfinputClassic type="text" 
										name="custom_#field.getName()#" 
										value="#default#" class="large" style="width:100%" required="#field.getRequired()#" 
										message="Missing value for field #field.getDisplayName()#">
								<!--- TODO more dynamic solution --->
								<cfif not isNew and "model" EQ field.getName()>
									<cftry>
										<cfset meta=aiGetMetadata(connection.name,true)>
										<cfif structKeyExists(meta,"models")>
											<p></p><b>available models:<br>#arrayToList(queryColumnData(meta.models,"name"),", ")#</b></p>
										</cfif>
										
										<cfcatch></cfcatch>
									</cftry>
								</cfif>
							<cfelseif type EQ "textarea">
								<textarea class="large" style="height:70px;width:100%" name="custom_#field.getName()#">#default#</textarea>
							<cfelseif type EQ "time">
								<cfsilent>
									<cfset doBR=false>
									<cfif len(default) EQ 0>
										<cfset default=0>
									<cfelse>
										<cfset default=default+0>
									</cfif> 
									
									<cfset s=default>
									<cfset m=0>
									<cfset h=0>
									<cfset d=0>
									
									<cfif s GT 0>
										<cfset m=int(s/60)>
										<cfset s-=m*60>
									</cfif>
									<cfif m GT 0>
										<cfset h=int(m/60)>
										<cfset m-=h*60>
									</cfif>
									<cfif h GT 0>
										<cfset d=int(h/24)>
										<cfset h-=d*24>
									</cfif>
								</cfsilent>
								<table class="maintbl autowidth">
									<thead>
										<tr>
											<th>#stText.General.Days#</td>
											<th>#stText.General.Hours#</td>
											<th>#stText.General.Minutes#</td>
											<th>#stText.General.Seconds#</td>
										</tr>
									</thead>
									<tbody>
										<tr>
											<td><cfinputClassic type="text" 
												name="custompart_d_#field.getName()#" 
												value="#addZero(d)#" class="number" required="#field.getRequired()#"   validate="integer"
												message="Missing value for field #field.getDisplayName()#"></td>
											<td><cfinputClassic type="text" 
												name="custompart_h_#field.getName()#" 
												value="#addZero(h)#" class="number" required="#field.getRequired()#"  maxlength="2"  validate="integer"
												message="Missing value for field #field.getDisplayName()#"></td>
											<td><cfinputClassic type="text" 
												name="custompart_m_#field.getName()#" 
												value="#addZero(m)#" class="number" required="#field.getRequired()#"  maxlength="2" validate="integer" 
												message="Missing value for field #field.getDisplayName()#"></td>
											<td><cfinputClassic type="text" 
												name="custompart_s_#field.getName()#" 
												value="#addZero(s)#" class="number" required="#field.getRequired()#"  maxlength="2"  validate="integer"
												message="Missing value for field #field.getDisplayName()#"></td>
										</tr>
									</tbody>
								</table>
							<cfelseif type EQ "select">
								<cfif default EQ field.getDefaultValue() and field.getRequired()>
									<cfset default=listFirst(default)>
								</cfif>
								<select name="custom_#field.getName()#">
									<cfif not field.getRequired()><option value=""> ---------- </option></cfif>
									<cfif true>
										<cfloop index="item" list="#field.getValues()#">
											<option <cfif item EQ default>selected="selected"</cfif> >#item#</option>
										</cfloop>
									</cfif>
								</select>
							<cfelseif type EQ "radio" or type EQ "checkbox">
								<cfset desc=field.getDescription()>
								<cfif isStruct(desc) and StructKeyExists(desc,'_top')>
									<div class="comment">#desc._top#</div>
								</cfif>
								<cfif listLen(field.getValues()) GT 1>
									<ul class="radiolist">
										<cfloop index="item" list="#field.getValues()#">
											<li>
												<label>
													<cfinputClassic type="#type#" class="#type#" name="custom_#field.getName()#" value="#item#" checked="#item EQ default#">
													<b>#item#</b>
												</label>
												<cfif isStruct(desc) and StructKeyExists(desc,item)>
													<div class="comment" style="padding-bottom:4px">#desc[item]#</div>
												</cfif>
											</li>
										</cfloop>
									</ul>
								<cfelse>
									<cfset item = field.getValues() />
									<cfinputClassic type="#type#" class="#type#" name="custom_#field.getName()#" value="#item#" checked="#item EQ default#">
								</cfif>
								<cfif isStruct(desc) and StructKeyExists(desc,'_bottom')>
									<div class="comment">#desc._bottom#</div>
								</cfif>
							</cfif>
							<cfif isSimpleValue(field.getDescription()) and len(trim(field.getDescription()))>
								<div class="comment">#field.getDescription()#</div>
							</cfif>
						</td>
					</tr>
				</cfloop>
				</tbody>
			</table>
			<h2>#stText.Settings.ai.passthroughTitle#</h2>
			<div class="itemintro">#stText.Settings.ai.passthroughDesc#</div>
			<table class="maintbl">
				<tbody>
					<tr>
						<th scope="row">JSON</th>
						<td>
							<textarea class="large ai-passthrough-json" name="passthroughJson" rows="12" style="width:100%;font-family:monospace;">#passthroughJson#</textarea>
							<cfif arrayLen(passthroughShortcuts)>
								<div class="ai-passthrough-shortcuts">
									<div class="comment">#stText.Settings.ai.passthroughShortcutsDesc#</div>
									<cfloop array="#passthroughShortcuts#" item="shortcut">
										<button type="button" class="button ai-passthrough-shortcut" data-json="#encodeForHTMLAttribute(isStruct(shortcut.json) ? serializeJSON(shortcut.json) : shortcut.json)#" title="#encodeForHTMLAttribute(shortcut.description ?: '')#">#encodeForHTML(shortcut.label)#</button>
									</cfloop>
								</div>
							</cfif>
						</td>
					</tr>
				</tbody>
			</table>
			<h2>#stText.Settings.ai.default#</h2>
			<div class="itemintro">#stText.Settings.ai.defaultDesc#</div>
			<table class="maintbl">
				<tbody>
				<tr>
					<td>
						<table>
							<cfloop array="#defaults#" item="type" >
								<tr>
									<td style="border:0;padding:0px;margin:0px"><input <cfif connection.default EQ type>checked="checked"</cfif> type="radio" class="radio" name="default" value="#type#"></td>
									<td style="border:0;padding:3px;margin:0px">
										<h3>#stText.Settings.ai['defaultType'& type]?:ucFirst(type)#</h3>
										<div>#stText.Settings.ai['defaultType'& type& 'Desc']?:''#</div>
									</td>
								</tr>
							</cfloop>
						</table>
					</td>
				</tr>
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2">
						<input type="submit" class="bl button submit" name="mainAction" value="#stText.Buttons.submit#">
						<input type="submit" class="br button submit" name="mainAction" value="#stText.Buttons.Cancel#">
					</td>
				</tr>
			</tfoot>
		</table>
	</cfformClassic>
</cfoutput>
<cfif arrayLen(passthroughShortcuts)>
<script>
(function() {
	function aiPassthroughMerge(current, incoming) {
		var out = {};
		var key;
		for (key in current) {
			if (Object.prototype.hasOwnProperty.call(current, key)) {
				out[key] = current[key];
			}
		}
		for (key in incoming) {
			if (!Object.prototype.hasOwnProperty.call(incoming, key)) continue;
			if (key === "headers" && out.headers && typeof incoming.headers === "object" && !Array.isArray(incoming.headers)) {
				out.headers = Object.assign({}, out.headers, incoming.headers);
			} else if (Array.isArray(incoming[key])) {
				out[key] = Array.isArray(out[key]) ? out[key].concat(incoming[key]) : incoming[key].slice();
			} else if (incoming[key] && typeof incoming[key] === "object" && !Array.isArray(incoming[key])) {
				out[key] = Object.assign({}, out[key] || {}, incoming[key]);
			} else {
				out[key] = incoming[key];
			}
		}
		return out;
	}

	document.querySelectorAll(".ai-passthrough-shortcut").forEach(function(btn) {
		btn.addEventListener("click", function() {
			var textarea = document.querySelector(".ai-passthrough-json");
			if (!textarea) return;
			var incoming;
			try {
				incoming = JSON.parse(btn.getAttribute("data-json") || "{}");
			} catch (e) {
				window.alert("Invalid shortcut JSON.");
				return;
			}
			if (!incoming || typeof incoming !== "object" || Array.isArray(incoming)) {
				window.alert("Shortcut JSON must be an object.");
				return;
			}
			var current = {};
			var raw = textarea.value.trim();
			if (raw) {
				try {
					current = JSON.parse(raw);
				} catch (e) {
					window.alert("Fix the passthrough JSON before inserting a shortcut.");
					return;
				}
				if (!current || typeof current !== "object" || Array.isArray(current)) {
					window.alert("Passthrough JSON must be an object.");
					return;
				}
			}
			textarea.value = JSON.stringify(aiPassthroughMerge(current, incoming), null, 2);
			textarea.focus();
		});
	});
})();
</script>
</cfif>
<cfif !isNew>
	
<cftry>
<cfoutput><cfsavecontent variable="codeSample">
<cfif isStruct(connection.custom)>
	<cfset newLineChar = Chr(13) & Chr(10)>
	<cfset tabChar = chr(9)>
	<cfset customTab = newLineChar & tabChar & tabChar>
	<cfset connectionCustom_Aligned = serialize(connection.custom)>
	<cfset connectionCustom_Aligned = replaceNoCase(connectionCustom_Aligned, '","', '",#customTab#"', 'ALL')>
	<cfset connectionCustom_Aligned = replaceNoCase(connectionCustom_Aligned, '{"', '{#customTab#"', "ALL")>
	<cfset connectionCustom_Aligned = replaceNoCase(connectionCustom_Aligned, '"}', '"#newLineChar##tabChar#}', "ALL")>
<cfelse>
	<cfset connectionCustom_Aligned = '{}'>
</cfif>
this.cache.connections["#connection.name#"] = {
	  class: '#connection.class#'#isNull(connection.bundleName) || isEmpty(connection.bundleName)?"":"
	, bundleName: '"&connection.bundleName&"'"##isNull(connection.bundleVersion) || isEmpty(connection.bundleVersion)?"":"
	, bundleVersion: '"&connection.bundleVersion&"'"##!connection.readOnly?"":"
	, readOnly: "&connection.readonly#
	, storage: #connection.storage#
	, custom: #connectionCustom_Aligned#
	, default: '#connection.default#'
};
</cfsavecontent></cfoutput>
<cfset renderCodingTip( codeSample, "", true )>
<cfcatch></cfcatch>
</cftry></cfif>
