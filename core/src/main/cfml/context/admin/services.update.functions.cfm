<cfscript>
stText.services.update.serverNotReachable="Could not reach server {url}.";
stText.services.update.serverFailed="server {url} failed to return a valid response.";
	
	function getUpdateData() {
		admin
			action="getUpdate"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"
			returnvariable="local.update";
			
		// this should not be necessary, but needed for testing with dealing with current admin code and older Lucee versions
		if(update.location=="http://snapshot.lucee.org") update.location="http://update.lucee.org";
		if(update.location=="http://release.lucee.org") update.location="http://update.lucee.org";
		return update;
	}

	struct function getAvailableVersion() localmode="true"{
		restBasePath="/rest/update/provider/";
		try{

			admin
				action="getAPIKey"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				returnVariable="apiKey";
			
			var update=getUpdateData();

			http
			url="#update.location##restBasePath#info/#server.lucee.version#"
			method="get" resolveurl="no" result="local.http" {
				if(!isNull(apiKey))httpparam type="header" name="ioid" value="#apikey#";
			}
			
			// i have a response
			if(isJson(http.filecontent)) {
				rsp=deserializeJson(http.filecontent);
			}
			// service not available
			else if(http.status_code==404) {
				rsp={"type":"warning","message":replace(stText.services.update.serverNotReachable,'{url}',update.location)};
			}
			// server failed
			else {
				rsp={"type":"warning","message":replace(stText.services.update.serverFailed,'{url}',update.location)&" "&http.filecontent};
			}
			rsp.code=http.status_code?:404;
		}
		catch(e){
			rsp={"type":"warning","message":replace(stText.services.update.serverFailed,'{url}',update.location)&" "&e.message};
		}
		rsp.provider=update;
		return rsp;
	}

</cfscript>