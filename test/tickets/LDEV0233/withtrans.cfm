<cfscript>
setting requesttimeout=10;
	local.res = true;
	transaction {
		try {
			saveData = EntityNew('Attribute');
			tmp = EntityLoadByPK("ContentType","1");
			saveData.setContentType(tmp);
			EntitySave(saveData);

			oRMFlush();

			transaction action="commit";
		}
		catch(any e) {
			transaction action="rollback";
			local.res = e.Message;
		}
	}
	writeOutput(local.res);
</cfscript>