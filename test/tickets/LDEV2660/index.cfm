<cfscript>
    if (not directoryExists( getDirectoryFromPath(getCurrenttemplatepath()) & "target")){
        directoryCreate(getDirectoryFromPath(getCurrenttemplatepath()) & "target");
    }

    if (directoryExists( getDirectoryFromPath(getCurrenttemplatepath()) & "target/folder1")){
        directoryDelete( getDirectoryFromPath(getCurrenttemplatepath()) & "target/folder1", true);
    }

    if (directoryExists( getDirectoryFromPath(getCurrenttemplatepath()) & "target/folder2")){
        directoryDelete( getDirectoryFromPath(getCurrenttemplatepath()) & "target/folder2", true);
    }
</cfscript>

<cfzip file="#expandPath('.')#/bundle.zip" action="unzip" overwrite="true" destination="#expandPath('.')#/target" entrypath="folder1">

<cfzip file="#expandPath('.')#/bundle.zip" action="unzip" overwrite="true" destination="#expandPath('.')#/target" entrypath="folder2">

<cfoutput>#fileExists("#expandPath('.')#/target/folder1/file.txt")#</cfoutput>
