<cfscript>
  	// update provider
	systemOutput("Trigger builds", true);
	http url="https://update.lucee.org/rest/update/provider/buildLatest" method="GET" timeout=90 result="buildLatest";
	systemOutput(buildLatest.fileContent, true);

	systemOutput("Update Extension Provider", true);
	http url="https://extension.lucee.org/rest/extension/provider/reset" method="GET" timeout=90 result="extensionReset";
	systemOutput(extensionReset.fileContent, true);

	systemOutput("Update Downloads Page", true);
	http url="https://download.lucee.org/?type=snapshots&reset=force" method="GET" timeout=90 result="downloadUpdate";
	systemOutput("Server response status code: " & downloadUpdate.statusCode, true);
</cfscript>
