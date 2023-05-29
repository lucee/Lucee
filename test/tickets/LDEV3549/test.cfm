<cfscript>
    try{
        path = url.tempJarFolder;
        testJarPath = path & "/test.jar";

        if ( !directoryExists( "#path#/jar" ) ) 
            directoryCreate( "#path#/jar" );
        filecopy( testJarPath, "#path#/jar/test.jar" );
        
        createObject( "java","com.github.sushantmimani.App" ); // To check able to create a object for the class

        if ( directoryExists("#path#/jar" ) ) 
            directoryDelete( "#path#/jar",true ); // To check able to delete the file
        writeoutput( "jar file wasn't locked" );
    }
    catch(any e) {
        writeoutput(e.message);
    } 
</cfscript>