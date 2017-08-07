<cfscript>
	// thread operations
	stText.services.update.serverNotReachable="Could not reach server {url}.";
	stText.services.update.serverFailed="server {url} failed to return a valid response.";
	if(!structKeyExists(application, "updateProvider")){
		structInsert(application, "updateProvider", structNew());
	}

	// lucee updateProvider URL
	updateProvider = {};
	updateProvider.releases = "http://release.lucee.org";
	updateProvider.snapshot = "http://snapshot.lucee.org";
	restBasePath="/rest/update/provider/";
	thread name="providers" action="run"{
		while(true){
			tmpStr = {};
			for(provider in updateProvider){
				try{
					http
					url="#updateProvider[provider]##restBasePath#info/#server.lucee.version#"
					method="get" resolveurl="no" result="http";
					// i have a response
					if(isJson(http.filecontent)) {
						rsp=deserializeJson(http.filecontent);
					}else if(http.status_code==404) {
						rsp={"type":"warning","message":replace(stText.services.update.serverNotReachable,'{url}',updateProvider[provider])};
					}
					else {
						rsp={"type":"warning","message":replace(stText.services.update.serverNotReachable,'{url}',updateProvider[provider])&" "&http.filecontent};
					}
					rsp.code=http.status_code?:404;
				}catch(e){
					rsp={"type":"warning","message":replace(stText.services.update.serverNotReachable,'{url}',updateProvider[provider])&" "&e.message};
				}
				structInsert(tmpStr, provider, rsp, true);
				rsp = {};
			}
			// update thread value in application scope
			structUpdate(Application, "updateProvider", tmpStr);
			// wait for every 60 seconds
			sleep(60000);
		}
	}
</cfscript>