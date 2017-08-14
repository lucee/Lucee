<cfscript>
	// thread operations
	stText.services.update.serverNotReachable="Could not reach server {url}.";
	stText.services.update.serverFailed="server {url} failed to return a valid response.";
	if(!structKeyExists(application, "luceeUpdateProvider")){
		structInsert(application, "luceeUpdateProvider", structNew());
	}

	thread name="providers" action="run"{
		while(true){
			rsp = {};
			provider = "http://update.lucee.org/rest/update/provider/info/5.2.1.0"
			try{
				http
					url="#provider#"
					method="get" resolveurl="no" result="http";
				// i have a response
				if(isJson(http.filecontent)) {
					rsp = {"type":"info", "versions":"#deserializeJson(http.filecontent)#", "message" : "success"};
				}else if(http.status_code==404) {
					rsp={"type":"warning", "versions":[], "message":replace(stText.services.update.serverNotReachable,'{url}',provider)};
				}
				else {
					rsp={"type":"warning", "versions":[], "message":replace(stText.services.update.serverNotReachable,'{url}',provider)&" "&http.filecontent};
				}
				rsp.code=http.status_code?:404;
			}catch(e){
				rsp={"type":"warning", "versions":[], "message":replace(stText.services.update.serverNotReachable,'{url}',provider)&" "&e.message};
			}
			// update thread value in application scope
			structUpdate(Application, "luceeUpdateProvider", rsp);
			// wait for every 60 seconds
			sleep(60000);
		}
	}
</cfscript>