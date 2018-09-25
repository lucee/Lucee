<cfscript>
    if (not directoryExists( getDirectoryFromPath(getCurrenttemplatepath()) & "zip"))
    directoryCreate(getDirectoryFromPath(getCurrenttemplatepath()) & "zip");
</cfscript>

<cfset dirName = "zip" & "\zippedDir">

<cfzip action="zip" file="#expandPath('.')#/#dirName#.zip"  overwrite="true" password="safe">
    <cfzipparam  encryptionAlgorithm="standard" source="#expandPath('.')#"  entryPath = "#expandPath('.')#/#dirName#.zip"  recurse = "yes">
</cfzip>

<cfif not directoryExists(getDirectoryFromPath(getCurrenttemplatepath()) & "zip\zippedDir")>
    <cfset directoryCreate(getDirectoryFromPath(getCurrenttemplatepath()) & "zip\zippedDir")>
</cfif>

<cfzip action="unzip" file="#expandPath('.')#/#dirName#.zip" destination="#expandPath('.')#/zip/zippedDir/" password="safe">

<cfif FileExists(expandPath("./zip/zippedDir.zip"))>
    true
<cfelse>
    false
</cfif>
