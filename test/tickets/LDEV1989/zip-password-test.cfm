<cfscript>

	workingDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "zip";
	
	if ( !directoryExists( workingDir ) )
		directoryCreate( workingDir );
		
	zipPassword = "safePassword";

	zipfile = "#workingDir#/passwordWithEncryptionAlgorithm-#url.encryptionAlgorithm#.zip";
	
	zip action="zip" file="#zipFile#"  overwrite="true" password="#zipPassword#" {
		zipparam encryptionAlgorithm="#url.encryptionAlgorithm#" source="#expandPath('.')#" filter="*.cfm";
	}

	zip action="list" file="#zipfile#" name="res";

	//systemOutput( res, true );

	unZipDir = workingDir & "/unzipped-#url.encryptionAlgorithm#/";
	if ( !directoryExists( unZipDir ) )
		directoryCreate( unZipDir );
	
	zip action="unzip" file="#zipFile#" destination="#unzipDir#" password="#zipPassword#";

	//systemOutput( directoryList( unzipDir ), true );

	origFileContent = FileRead( getCurrentTemplatePath() );
	outFileContent = FileRead( unzipDir & ListLast( getCurrentTemplatePath(), "\/" ) );

	// test that the contents of this script are the same after zip and unzip with a password and encryptionAlgorithm
	echo ( len(origFileContent) gt 0 && origFileContent eq outFileContent );
	
</cfscript>
