<script type="text/javascript">disableBlockUI=true;</script>
<cfparam name="session.debug.template" default="">
<cfscript>
	//  load available drivers (i.e debug templates) 
	driverNames=structnew("linked");
	driverNames=ComponentListPackageAsStruct("lucee-server.admin.debug",driverNames);
	driverNames=ComponentListPackageAsStruct("lucee.admin.debug",driverNames);
	driverNames=ComponentListPackageAsStruct("debug",driverNames);

	drivers={};
	loop collection="#driverNames#" index="n" item="fn" {
		if ( n == "Debug" || n == "Field" || n == "Group" ) {
			continue;
		}
		tmp=createObject('component',fn);
		drivers[trim(tmp.getId())]=tmp;
	}
	
	//  determine which debug template to use
	template ="";
	if  (structKeyExists(url, "template") and StructKeyExists(drivers, url.template)) {
		driver=drivers[url.template];
		template=url.template;
		session.debug.template = url.template;
	} else if (len(session.debug.template)){
		driver=drivers[session.debug.template];
		template=session.debug.template;		
	} else if ( IsEmpty(entries.type) || !StructKeyExists(drivers, entries.type) ) {
		driver=drivers["lucee-modern"];
		template= "lucee-modern";
	} else {
		driver=drivers["#entries.type#"];
		template=entries.type;
	}
	entry={};
	
	// this is a lucky dip
	loop query="entries" {
		if (entries.type EQ template) {
			entry = querySlice(entries, entries.currentrow ,1);
			template = entry.type;
			break;
		}
	}

	//  get matching log entry 
	log="";
	for ( i=1 ; i<=arrayLen(logs) ; i++ ) {
		el=logs[i];
		id=hash(el.id & ":" & el.startTime);
		if ( url.id == id ) {
			log=el;
		}
	}
	
	if (url.format eq "json"){
		setting showdebugoutput="false";
		content reset="yes" type="application/json";
		echo(serializeJson(log));
		abort;		
	}
</cfscript>
<cfoutput>
	<cfif !isSimpleValue(log) && structCount(drivers)>
		<cfformClassic onerror="customError" action="?" method="get" name="debug_select_template">
		<table class="maintbl">
			<tbody>
		<tr>
					<th scope="row">#stText.debug.selectDebugTemplate#</th>
			<td>				
				<cfloop collection="#drivers#" item="fn">
							<label title="#encodeForHTMLAttribute(drivers[fn].getDescription())#" style="margin-right:15px;">
						<input name="template" type="radio" value="#drivers[fn].getId()#" class="select-template"
							<cfif template eq drivers[fn].getId()>checked</cfif>
						>
						#drivers[fn].getLabel()#
					</label>
				</cfloop>
				<cfloop collection="#url#" item="u">
					<cfif u neq "template">
						<input type="hidden" name="#u#" value="#encodeForHtml(url[u])#">
					</cfif>
				</cfloop>				
						<input type="submit" value="#stText.debug.switchTemplate#" class="bs button submit">
			</td>
		</tr>
			</tbody>
		</table>
		</cfformClassic>
	</cfif>
	<table width="100%">	
	<tr>
		<td>
			<!--- <cfset log = logs[1]> --->
			<cfset request.fromAdmin = true>
			<cfif !isSimpleValue(log)>
				<cfif NOT isDebugMode()>
					<cfset c ={callStack:"Enabled",colorHighlight:"Enabled",displayPercentages:"Enabled",expression:"Enabled",general:"Enabled",highlight:"0",metrics_charts:"HeapChart,NonHeapChart,WholeSystem",minimal:"0",sessionSize:"100",size:"medium",Tab_Debug:"Enabled",tab_Metrics:"Enabled",tab_Reference:"Enabled"} >
				<cfelse>
					<cfset c=structKeyExists(entry,'custom')?entry.custom:{}>
				</cfif>
				<cfset c.scopes=false>
				<cfset driver.output(c,log,"admin")>
			<cfelse>
				Debug Data no longer available
			</cfif> 
		</td>
	</tr>
	</table>
	
	<table class="tbl" width="740">
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post" name="debug_settings">
	<tr>
		<td ><input type="submit" name="mainAction" class="submit" value="#stText.buttons.back#" /></td>
	</tr>
	</cfformClassic>
	</table>
	<cfif template EQ "lucee-modern">
		<div id="blockerContainer" class="jquery-modal current">
			<div id="mdlWnd" class="modal" >
				<a href="#request.self#?action=#url.action#" rel="modal:close" class="close-modal ">Close</a>
				<div class="modal-body"></div>
			</div>
		</div>	
	</cfif>
</cfoutput>
<br><br>
