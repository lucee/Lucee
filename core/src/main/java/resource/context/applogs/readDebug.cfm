<cfscript>	
	setting showdebugoutput=true;
	param name="url.id" default="";
	param name="url.tab" default="debug";
	if (url.id eq ""){
		header statustext="Debugging log id is required" statusCode="404";
		echo("Debugging log id is required");		
		abort;
	}

	function isLoggedInAdmin(){
		// TODO check for lucee admin login
		/*
		this doesn't work coz of the session scope for admin being different

		if (structKeyExists(session, "passwordweb")){
			admin action="connect"
				type="web"
				password="#session.passwordweb#";
		} else if (structKeyExists(session, "passwordserver")){
			admin action="connect"
				type="web"
				password="#session.passwordserver#";
		} else {
			return false;
		}
		*/
		return false;
	}
	
	admin
		action="getLoggedDebugData"
		type="web"
		id=url.id
		returnVariable="log";

	if (not isLoggedInAdmin()){
		// access control, should the user be able to see this debug entry?	
		logCookies = {};
		loop list="#log.scope.cgi.http_cookie#" item="c" delimiters=";"{		
			logCookies[trim(listFirst(c,"="))]=trim(listLast(c,"="));
		}

		cookiesMatch={};
		// TODO auth needs to respect JSESSIONID vs CF sessions
		loop list="JSESSIONID,CFID,cftoken" item="c" delimiters=","{
			if (structKeyExists(logCookies, c) 
					and structKeyExists(cookie, c) 
					and len(trim(cookie[c])) gt 0
					and logCookies[c] eq cookie[c]){
				cookiesMatch[c] = true;
			}
		}

		if (not cookiesMatch.len() eq 3){
			// TODO check for lucee admin login
			header statustext="Access Denied" statusCode="403";
			echo("Debugging Log Access Denied");
			cflog(text="Debugging Log Access Denied - id: #htmleditFormat(url.id)#", type="warning");
			abort;
		}	
	}

	admin
		action="getDebugEntry"
		type="web"
		returnVariable="entries";

	selectedDebugTemplates=structNew("linked");
	if (entries.recordcount){
		configuredTemplates=entries.columnData("type");
		configuredTemplates.each(function(key){
			selectedDebugTemplates[key]=true;
		});
	} else {
		selectedDebugTemplates["lucee-modern"]=true; // default
	}

	driverNames=structnew("linked");
	driverNames=ComponentListPackageAsStruct("lucee-server.admin.debug",driverNames);
	driverNames=ComponentListPackageAsStruct("lucee.admin.debug",driverNames);
	driverNames=ComponentListPackageAsStruct("debug",driverNames);
	
	drivers={};
	driver="";
	loop collection=driverNames index="n" item="fn" {
		if(n == "Debug" || n == "Field" || n == "Group") {
			continue;
		}
		tmp=createObject('component',fn);
		templateId = trim(tmp.getId());
		drivers[templateId]=tmp;		
		if (selectedDebugTemplates.keyExists(templateId)){
			driver=drivers[templateId];
			break;
		}
	}
	if (not IsObject(driver)){
		throw message="Configured debug template(s): #selectedDebugTemplates.keyList()# not found";
	}

	entry={};

	loop query="entries" {
		if(entries.type == templateId) {
			entry=querySlice(entries, entries.currentrow ,1);
		}
	}

	if(!isSimpleValue(log)) {
		c=structKeyExists(entry,'custom')?entry.custom:{};
		c.scopes=false;
		fun = "read#URL.tab#";
		try {
			driver[fun](c,log,"admin"); 
		} catch(e){
			dump(e.message);
		}
	}

	function ComponentListPackageAsStruct(string package, cfcNames=structnew("linked")){
		try{
			local._cfcNames=ComponentListPackage(package);
			loop array=_cfcNames index="i" item="el" {
				cfcNames[el]=package&"."&el;
			}
		}
		catch(e){}
		return cfcNames;
	}
</cfscript>