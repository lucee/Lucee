<cfscript>
	
	setting showdebugoutput=false;
	admin
		action="getLoggedDebugData"
		type="web"
		returnVariable="all";

	listIds = "";
	loop array="#all#" index="i" {
		listIds = listAppend(listIds, i.id);
	}
	if(isNull(url.id)) {
		url.id=all[arrayLen(all)].id;
	}
	else if(listFind(listIds, url.id) == 0) {
		url.id=all[arrayLen(all)].id;
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

	driverNames=structnew("linked");
	driverNames=ComponentListPackageAsStruct("lucee-server.admin.debug",driverNames);
	driverNames=ComponentListPackageAsStruct("lucee.admin.debug",driverNames);
	driverNames=ComponentListPackageAsStruct("debug",driverNames);
	
	drivers={}
	loop collection=driverNames index="n" item="fn" {
		if(n == "Debug" || n == "Field" || n == "Group") {
			continue;
		}
		tmp=createObject('component',fn);
	    drivers[trim(tmp.getId())]=tmp;
	}
	

	driver=drivers["lucee-modern"];


	entry={};

	loop query="entries" {
		if(entries.type == "lucee-modern") {
			entry=querySlice(entries, entries.currentrow ,1);
		}
	}

	if(!isSimpleValue(log)) {
		c=structKeyExists(entry,'custom')?entry.custom:{};
		c.scopes=false;
		fun = "read#URL.tab#";
		driver[fun](c,log,"admin");
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