<script type="text/javascript">disableBlockUI=true;</script>
<cfoutput>
	<cfif NOT isDebugMode()>
		<span>Please set "Classic or Mordern" template to view the debug details!</span>
		<table class="tbl" width="740">
			<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post" name="debug_settings">
				<tr>
					<td ><input type="submit" name="mainAction" class="submit" value="#stText.buttons.back#" /></td>
				</tr>
			</cfformClassic>
		</table>
	<cfelse>
		<!--- load available drivers --->
		<cfset driverNames=structnew("linked")>
		<cfset driverNames=ComponentListPackageAsStruct("lucee-server.admin.debug",driverNames)>
		<cfset driverNames=ComponentListPackageAsStruct("lucee.admin.debug",driverNames)>
		<cfset driverNames=ComponentListPackageAsStruct("debug",driverNames)>

		<cfset drivers={}>
		<cfloop collection="#driverNames#" index="n" item="fn">
			<cfif n EQ "Debug" or n EQ "Field" or n EQ "Group">
				<cfcontinue>
			</cfif>
			<cfset tmp=createObject('component',fn)>
			<cfset drivers[trim(tmp.getId())]=tmp>
		</cfloop>
			<cfset driver=drivers["#entries.type#"]>
			<cfset entry={}>
			<cfloop query="entries">
				<cfif entries.type EQ "lucee-modern" OR entries.type EQ "lucee-classic" OR entries.type EQ "lucee-comment">
					<cfset entry=querySlice(entries, entries.currentrow ,1)>
				</cfif>    
			</cfloop>
						
			<!--- get matching log entry --->
			<cfset log="">
			<cfloop from="1" to="#arrayLen(logs)#" index="i">
				<cfset el=logs[i]>
				<cfset id=hash(el.id&":"&el.startTime)>
				<cfif url.id EQ id>
					<cfset log=el>
				</cfif>
			</cfloop>
			
			<table width="100%">
			<tr>
				<td>
					<!--- <cfset log = logs[1]> --->
					<cfset request.fromAdmin = true>
					<cfif !isSimpleValue(log)>
					<cfset c=structKeyExists(entry,'custom')?entry.custom:{}>
					<cfset c.scopes=false>
					<cfset driver.output(c,log,"admin")><cfelse>Data no longer available</cfif> </td>
			</tr>
			</table>
			
		<table class="tbl" width="740">
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post" name="debug_settings">
		<tr>
			<td ><input type="submit" name="mainAction" class="submit" value="#stText.buttons.back#" /></td>
		</tr>
		</cfformClassic>
		</table>	
		<div id="blockerContainer" class="jquery-modal current">
			<div id="mdlWnd" class="modal" >
				<a href="##close-modal" rel="modal:close" class="close-modal ">Close</a>
				<div class="modal-body"></div>
			</div>
		</div>	
	</cfif>
</cfoutput>
<br><br>
