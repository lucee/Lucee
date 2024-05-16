<cfscript>
    s3Url = "s3://#url.vfs#@/#url.bucketName#";
    
    try {
        DirectoryExists( s3Url );
    } catch ( e ) {
        SystemOutput( "Testing VFS s3 profile: [#s3Url#]", true );
        rethrow;
    }
</cfscript>