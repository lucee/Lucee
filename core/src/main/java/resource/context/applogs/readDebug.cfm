<cfscript>	
	setting showdebugoutput=true;
	param name="url.id" default="";
	param name="url.tab" default="debug";
	

	if (url.id eq ""){
		header statustext="Debugging log id is required" statusCode="404";
		echo("Debugging log id is required");		
		abort;
	} 
	
	admin
		action="getLoggedDebugData"
		type="web"
		id=url.id
		returnVariable="log";

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
			loop array=_cfcNames index="local.i" item="local.el" {
				cfcNames[local.el]=package&"."&local.el;
			}
		}
		catch(e){}
		return cfcNames;
	}
</cfscript>