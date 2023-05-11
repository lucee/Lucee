component {
	// Define the application settings.
	this.name = hash( getCurrentTemplatePath());
	
	public boolean function onRequestStart(required string template) {
		setting requesttimeout=10;
		writeOutput('onRequestStart:'&listLast(arguments.template,'\/')&";");
		return true;
	}

	public void function onRequest(required string template) {
		writeOutput('onRequest:'&listLast(arguments.template,'\/')&";");
	}

	public void function onCFCRequest(required string component, required string methodName, required struct methodArguments) {
		writeOutput('onCFCRequest:'&listLast(arguments.component,'.')&','&arguments.methodName&','&serializeJson(arguments.methodArguments)&";");
	}


	public void function onMissingTemplate(required string template) {
		writeOutput('onMissingTemplate:'&listLast(arguments.template,'\/')&";");
	}
	
}
