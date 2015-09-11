<cfset more=struct()>
<cfif isDefined('form.port')>
	<cfset more.port=form.port>
</cfif>

<cfset form.name = trim( form.name )>

<cfadmin action="schedule" type="#request.adminType#" password="#session["password"&request.adminType]#"

	attributeCollection="#more#"
	
	scheduleAction="update" 
	operation="httprequest"
	task="#form.name#"
	url="#form.url#"
	interval="#form.interval#" 
	startdate="#nullIfNoDate('start')#" 
	starttime="#nullIfNoTime('start')#"
	remoteClients="#request.getRemoteClients()#">


<cfif StructKeyExists(form,"paused") && form.paused>

	<cfadmin action="schedule" type="#request.adminType#" password="#session["password"&request.adminType]#"

        scheduleAction="pause"
        task="#form.name#"
        remoteClients="#request.getRemoteClients()#">
</cfif>

			
<cflocation url="#request.self#?action=#url.action#&action2=edit&task=#hash(form.name)#" addtoken="no">