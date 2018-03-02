<cfscript>
function loadjQuery(required string defaultFilePath) {
	var custom=false;
    var appSettings=getApplicationSettings();
    var filePath=defaultFilePath;
    
    // get filepath from application.cfc and disable if necessary
    if(!isNull(appSettings.jquery))  {
        var enabled=appSettings.jquery.enabled?:true;
        if(!enabled) return "/* jquery disabled in Application.cfc */"; // not enabled we load nothing at all
        if(!isNull(appSettings.jquery.location)) {
            filePath=appSettings.jquery.location;
            custom=true;
        }  
    }
   
    // search the file
	var expFilePath=expandPath(filePath);
    if(!fileExists(expFilePath)) return "/* file "&filePath&" "&(custom?"( defined in "&(appSettings.component?:"Application.cfc")&" -> this.jquery.location )":"")&" not found */";
    
    // load the file
    return fileRead(expFilePath);
}

content type="text/javascript";
setting showdebugoutput=false;
echo(loadjQuery("jquery/jquery-1.6.2.min.js"));

</cfscript>