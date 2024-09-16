<cfscript>

	stText.Settings.ai.titleExisting="Defined AI Connections";
	stText.Settings.ai.descExisting="Below is a list of all AI connections that have been configured in this environment.";
	stText.Settings.ai.model="Model";	
	stText.Settings.ai.default="Default";	
	stText.Settings.ai.defaultTitle="Default AI connections";
	stText.Settings.ai.defaultDesc="In addition to explicitly using AI connections, you can also set default connections for specific functionalities.";
	
	stText.Settings.ai.descCreate="Set up a new connection to an AI engine, allowing you to integrate and manage different AI models within your environment.";
	stText.Settings.cache.typeDesc="Select the type of AI engine you want to connect to, allowing integration with various AI models and platforms."


	stText.Settings.ai.defaultTypeException="Exception";
	stText.Settings.ai.defaultTypeExceptionDesc="Lucee will use that connection to analyze any exceptions that are thrown and displayed in the error output. Lucee will detact secrets before sending it to the AI and log everything it send to the ai to the ai.log.";
	


	error.message="";
	error.detail="";
	param name="url.action2" default="list";
	param name="form.mainAction" default="none";
	param name="form.subAction" default="none";
	defaults=["exception"];


	sctDefaults=[:];
	loop array=defaults item="d" {
		sctDefaults[d]=d;
	}


	/*admin 
		action="getCacheConnections"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		returnVariable="connections";*/
	admin 
		action="getAIConnections"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		returnVariable="connections";
 
	// load available drivers
	driverNames=structnew("linked");
	driverNames=ComponentListPackageAsStruct("lucee-server.admin.aidriver",driverNames);
	driverNames=ComponentListPackageAsStruct("lucee.admin.aidriver",driverNames);
	driverNames=ComponentListPackageAsStruct("aidriver",driverNames);

	drivers=[:];
	
	loop collection="#driverNames#" index="n" item="fn" {
		if(n NEQ "AI" and n NEQ "Field" and n NEQ "Group") {
			tmp = createObject("component",fn);
			drivers[tmp.getClass()]=tmp;
		}
	}
	admin 
		action="securityManager"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		returnVariable="access"
		secType="cache";
	</cfscript>
		<div class="warning nofocus">
			This feature is experimental and may be subject to change.
			If you encounter any issues while using this functionality, 
			please report bugs and errors in our 
			<a href="https://issues.lucee.org" target="_blank">bug tracking system</a>.
		</div>
<cfscript>

	if("list"==url.action2)  include "services.ai.list.cfm";
	if("create"==url.action2)  include "services.ai.create.cfm";
</cfscript>