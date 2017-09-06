<cfsilent>
<cfscript>
include template="ext.functions.cfm";
application.external = queryNew("");
thread name="extUptd" action="run"{
	while(structKeyExists(application, "external")){
		admin
			action="getRHExtensionProviders"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"
			returnVariable="providers";

		//url for UpateExtension
		providerURLs=queryColumnData(providers,"url");
		//function call getExternalData
		extUpdate = getExternalData(providerURLs,true);
		//update in application scope
		structUpdate(Application, "external", extUpdate);
		// wait for every 60 seconds
		sleep(60000);
	}
}
</cfscript>
</cfsilent>
