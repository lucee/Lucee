<cfset error.message="">
<cfset error.detail="">
<cfset hasAccess=true>

<cfif request.admintype EQ "web">
	<cflocation url="#request.self#" addtoken="no">
</cfif>



<!--- 
Defaults --->
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">

<cfif StructKeyExists(form,"host")>
	<cfset session.certHost=form.host>
	<cfset session.certPort=form.port>
</cfif>

<cfparam name="session.certHost" default="">
<cfparam name="session.certPort" default="443">
<cfset _host=session.certHost>
<cfset _port=session.certPort>

<cfscript>
	LuceeTrustStore = false;
	if ((server.system.properties["lucee.use.lucee.SSL.TrustStore"]?: false)
			|| (server.system.environment["lucee_use_lucee_SSL_TrustStore"]?: false)){
		LuceeTrustStore = true;
	};
	
</cfscript>

<cfif !LuceeTrustStore>
	<p>
	<b>As Lucee is currently using the JVM TrustStore/cacerts file, this functionality isn't available.</b>
	<br><br>
	Set the following System or Environment variables to enable: <code>lucee.use.lucee.SSL.TrustStore = true;</code>
	</p>
</cfif>

<cftry>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
    
		<cfcase value="#stText.services.certificate.install#">
			<cfadmin 
                type="#request.adminType#"
				password="#session["password"&request.adminType]#"
                action="updatesslcertificate" host="#form.host#" port="#form.port#">
			
		
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
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<!--- 
Error Output --->
<cfset printError(error)>
<cfoutput>
	<cfif not hasAccess><cfset noAccess(stText.setting.noAccess)></cfif>
	<div class="pageintro">#stText.services.certificate.desc#</div>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.services.certificate.host#</th>
					<td>
						<cfinputClassic type="text" name="host" value="#_host#" class="large" required="yes" message="#stText.services.certificate.hostValidation#">
						<div class="comment">#stText.services.certificate.hostDesc#</div>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.services.certificate.port#</th>
					<td>
						<cfinputClassic type="text" name="port" value="#_port#" class="number" required="yes" validate="integer"><br />
						<div class="comment">#stText.services.certificate.portDesc#</div>
					</td>
				</tr>
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2">
						<input class="bl button submit" type="submit" name="mainAction" value="#stText.services.certificate.list#">
						<input class="bm button submit" type="submit" name="mainAction" value="#stText.services.certificate.install#">
						<input class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" type="reset" name="cancel" value="#stText.Buttons.Cancel#">
						<cfif request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#"></cfif>
					</td>
				</tr>
			</tfoot>
		</table>
	</cfformClassic>

	<cfif len(_host) and len(_port)>
		<cftry>
			<cfadmin 
                type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				action="getsslcertificate" host="#_host#" port="#_port#" returnvariable="qry">

			<h2>#replace(stText.services.certificate.result,'{host}',_host)#</h2>
			<cfif qry.recordcount>
				<table class="maintbl">
					<thead>
						<tr>
							<th>#stText.services.certificate.subject#</th>
							<th>#stText.services.certificate.issuer#</th>
						</tr>
					</thead>
					<tbody>
						<cfloop query="qry">
							<tr>
								<td>#qry.subject#</td>
								<td>#qry.issuer#</td>
							</tr>
						</cfloop>
					</tbody>
				</table>
			<cfelse>
				<div class="error">#stText.services.certificate.noCert#</div>
			</cfif>
			<cfcatch>
				<cfset session.certHost = "">
				<cfset session.certPort = "443">
				<div class="error">#cfcatch.message# #cfcatch.detail#</div>
			</cfcatch>
		</cftry>
	</cfif>
</cfoutput>