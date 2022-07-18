<cfscript>
    try{
        path = getDirectoryFromPath(getCurrentTemplatePath());
        if(!directoryExists("#path#/jar")) directoryCreate("#path#/jar");
        filecopy("#path#/test.jar","#path#/jar/test.jar");
        createObject("java","com.github.sushantmimani.App"); // To check able to create a object for the class
        if(directoryExists("#path#/jar")) directoryDelete("#path#/jar",true); // To check able to delete the file
        writeoutput("jar file not get locked");
    }
    catch(any e) {
        writeoutput(e.message);
    } 
</cfscript>