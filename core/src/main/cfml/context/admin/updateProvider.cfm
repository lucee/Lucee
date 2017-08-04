<cfscript>
	stText.services.update.serverNotReachable="Could not reach server {url}.";
	stText.services.update.serverFailed="server {url} failed to return a valid response.";
	if(!structKeyExists(application, "updateProvider")){
		structInsert(application, "updateProvider", structNew());
	}
	admin
		action="getUpdate"
		type="server"
		password="#session["password"&request.adminType]#"
		returnVariable="update";
	admin
		action="getAPIKey"
		type="server"
		password="#session["password"&request.adminType]#"
		returnVariable="apiKey";

	updateProviderURL = {};
	updateProviderURL.releases = "http://release.lucee.org";
	updateProviderURL.snapshot = "http://snapshot.lucee.org";
	restBasePath="/rest/update/provider/";
	thread name="providers" action="run"{
		while(true){
			tmpStr = {};
			for(prvs in updateProviderURL){
				try{
					http
					url="#updateProviderURL[prvs]##restBasePath#info/#server.lucee.version#"
					method="get" resolveurl="no" result="http" {
						if(!isNull(apiKey))httpparam type="header" name="ioid" value="#apikey#";
					}
					// i have a response
					if(isJson(http.filecontent)) {
						rsp=deserializeJson(http.filecontent);
					}else if(http.status_code==404) {
						rsp={"type":"warning","message":replace(stText.services.update.serverNotReachable,'{url}',updateProviderURL[prvs])};
					}
					else {
						rsp={"type":"warning","message":replace(stText.services.update.serverNotReachable,'{url}',updateProviderURL[prvs])&" "&http.filecontent};
					}
					rsp.code=http.status_code?:404;
				}catch(e){
					rsp={"type":"warning","message":replace(stText.services.update.serverNotReachable,'{url}',updateProviderURL[prvs])&" "&e.message};
				}
				structInsert(tmpStr, prvs, rsp, true);
				rsp = {};
			}
			structUpdate(Application, "updateProvider", tmpStr);
			sleep(60000);
		}
	}
</cfscript>