<cfscript>

base =  getDirectoryFromPath(getCurrentTemplatePath());

if(fileExists("#base#demo.cfc")){
	filecopy("#base#demo.cfc" , "#base#_demo.cfc");
	fileDelete("#base#demo.cfc");
}

</cfscript>
