<cfscript>
	setting requesttimeout=10;
	local.res = true;
	try {
		saveData = EntityNew('Attribute');
		tmp = EntityLoadByPK("ContentType","1");
		saveData.setContentType(tmp);
		EntitySave(saveData);

		oRMFlush();
	}
	catch(any e) {
		local.res = e.Message;
	}
	writeOutput(local.res);
</cfscript>