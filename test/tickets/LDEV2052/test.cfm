<cfscript>
    if (not directoryExists( getDirectoryFromPath(getCurrenttemplatepath()) & "target"))
    directoryCreate(getDirectoryFromPath(getCurrenttemplatepath()) & "target");
</cfscript>

<cffile action="Write" file="#expandPath('.')#/target/myFile.txt" output="testContent">

<cfzip action="unzip" file="#expandPath('.')#/myZip.zip" destination="#expandPath('.')#/target/" overwrite="true" >

<cffile action="read" file="#expandPath('.')#/target/myFile.txt" variable="content">

<cfoutput>#content#</cfoutput>
