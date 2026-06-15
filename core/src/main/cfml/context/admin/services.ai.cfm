<cfif request.admintype EQ "web"><cflocation url="#request.self#" addtoken="no"></cfif>
<cfscript>

	stText.Settings.ai.titleExisting="Defined AI Connections";
	stText.Settings.ai.descExisting="Below is a list of all AI connections that have been configured in this environment.";
	stText.Settings.ai.model="Model";	
	stText.Settings.ai.default="Default";	
	stText.Settings.ai.defaultTitle="Default AI connections";
	stText.Settings.ai.defaultDesc="In addition to explicitly using AI connections, you can also set default connections for specific functionalities.";
	stText.Settings.ai.titleCreate="Create a new AI connection";
	stText.Settings.ai.descCreate="Configure a connection to an AI provider of your choice to access their language models and features. Each provider offers different capabilities to enhance your applications.";
	stText.Settings.cache.typeDesc="Choose your AI provider to connect to their models and services. Each option offers different capabilities and some, like OpenAI, support multiple platforms through a compatible API.";

	stText.Settings.ai.default="Default";
	stText.Settings.ai.defaultDesc="Choose how this AI connection will be used. You can bind it to specific functionality like exception analysis or documentation support.";
	
	stText.Settings.ai.defaultTypeException="Exception";
	stText.Settings.ai.defaultTypeExceptionDesc="Use this connection to analyze exceptions in error templates. Sensitive information is automatically removed before sending to the AI, and all interactions are logged to ai.log.";
	
	stText.Settings.ai.defaultTypeDocumentation="Documentation";
	stText.Settings.ai.defaultTypeDocumentationDesc="Use this connection to provide additional documentation context in the Monitor section of the requests.";

	stText.Settings.ai.defaultTypeAdministrator="Administrator";
	stText.Settings.ai.defaultTypeAdministratorDesc="Use this connection to for AI related actions in the Lucee Administrator.";

	stText.Settings.ai.NameDesc="you can use this name to reference this connection in our code.";

	stText.Settings.ai.passthroughTitle="Passthrough configuration";
	stText.Settings.ai.passthroughDesc="Additional JSON properties merged into the AI provider API request. Use this for provider-specific parameters not shown above. Must be a JSON object; leave empty for none.";
	stText.Settings.ai.passthroughShortcutsDesc="Insert a predefined passthrough snippet. Existing JSON is merged with the shortcut values.";
	error.message="";
	error.detail="";
	param name="url.action2" default="list";
	param name="form.mainAction" default="none";
	param name="form.subAction" default="none";
	defaults=["administrator","documentation","exception"];



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
<cfscript>

	if("list"==url.action2)  include "services.ai.list.cfm";
	if("create"==url.action2)  include "services.ai.create.cfm";
</cfscript>