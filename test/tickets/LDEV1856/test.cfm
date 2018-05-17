<cfscript>
	cfhttp(url= "http://" &CGI.server_name &GetDirectoryFromPath( CGI.script_name ) &"myfile.txt", charset="iso-8859-1") { }
	writeOutput(cfhttp.Filecontent);
</cfscript>
