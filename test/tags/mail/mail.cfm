<cfscript>
	param name="form.subject";
	param name="form.to";
	param name="form.from";

	systemOutput("", true);
	systemOutput(form.toJson(), true);
	systemOutput(getApplicationSettings().mails, true);
	
	mail to="#form.to#" from="#form.from#" subject="#form.subject#" spoolEnable=false {
		echo("This is a text email!");
	}
</cfscript>