<cfscript>
	// firstly, check are we even deploying to s3
	if ( (server.system.environment.DO_DEPLOY?:false) eq false ){
		SystemOutput( "skip, DO_DEPLOY is false", 1 ,1 );
		return;
	} 
	// secondly, do we have the s3 extension?
	s3ExtVersion = extensionList().filter( function(row){ return row.name contains "s3"; }).version;
	if ( s3Extversion eq "" ){
		SystemOutput( "ERROR! The S3 Extension isn't installed!", true );
		return; 
		//throw "The S3 Extension isn't installed!"; // fatal
	} else {
		SystemOutput( "Using S3 Extension: #s3ExtVersion#", true );
	}

	// finally check for S3 credentials
	if ( isNull( server.system.environment.S3_ACCESS_ID_DOWNLOAD )
			|| isNull( server.system.environment.S3_SECRET_KEY_DOWNLOAD ) ) {
		SystemOutput( "no S3 credentials defined to upload to S3", 1, 1 );
		return;
		//throw "no S3 credentials defined to upload to S3";
		//trg.dir = "";
	}

	NL = "
";

	src.jar = server.system.properties.luceejar;
	src.core = server.system.properties.luceeCore;
	src.dir = getDirectoryFromPath( src.jar );
	src.jarName = listLast( src.jar,"\/" );
	src.coreName = listLast( src.core,"\/" );
	src.version = mid( src.coreName,1,len( src.coreName )-4 );

	if ( ! FileExists( src.jar ) || ! FileExists( src.core ) ){
		SystemOutput( src.jar & " exists: " & FileExists( src.jar ), true );
		SystemOutput( src.core & " exists: " & FileExists( src.core ), true );
		throw "missing jar or .lco file";
	}

	s3_bucket = "lucee-downloads";
	trg.dir = "s3://#server.system.environment.S3_ACCESS_ID_DOWNLOAD#:#server.system.environment.S3_SECRET_KEY_DOWNLOAD#@/#s3_bucket#/";
	
	// test s3 access
	SystemOutput( "Testing S3 Bucket Access", 1, 1 );
	if (! DirectoryExists( trg.dir ) )
		throw "DirectoryExists failed for s3 bucket [#s3_bucket#]"; // it usually will throw an error, rather than even reach this throw, if it fails

	trg.jar = trg.dir & src.jarName;
	trg.core = trg.dir & src.coreName;

	// we only upload / publish artifacts once LDEV-3921

	if ( fileExists( trg.jar ) && fileExists( trg.core ) ){
		SystemOutput( "Build artifacts have already been uploaded for this version, nothing to do", 1, 1 );
		return;
	}

	// copy jar
	SystemOutput( "upload #src.jarName# to S3",1,1 );
	if ( fileExists( trg.jar ) ){
		systemOutput("deleting", true);
		fileDelete( trg.jar );
	}
	systemOutput(src.jar & " exists: " & fileExists(src.jar), true);
	fileCopy( src.jar, trg.jar );

	// copy core
	SystemOutput( "upload #src.coreName# to S3",1,1 );
	if ( fileExists( trg.core ) ){
		systemOutput("deleting", true);
		fileDelete( trg.core );
	}
	systemOutput(src.core & " exists: " & fileExists(src.core), true);
	fileCopy( src.core, trg.core );

	// create war
	src.warName = "lucee-" & src.version & ".war";
	src.war = src.dir & src.warName;
	trg.war = trg.dir & src.warName;

	/*
	SystemOutput( "upload #src.warName# to S3",1,1 );
	zip action = "zip" file = src.war overwrite = true {

		// loader
		zipparam source = src.jar entrypath = "WEB-INF/lib/lucee.jar";

		// common files
		// zipparam source = commonDir;

		// website files
		// zipparam source = webDir;

		// war files
		// zipparam source = warDir;
	}
	fileCopy( src.war,trg.war );
	*/

	// Lucee light build (no extensions)	
	src.lightName = "lucee-light-" & src.version & ".jar";
	SystemOutput( "build and upload #src.lightName# to S3",1,1 );
	src.light = src.dir & src.lightName;
	trg.light = trg.dir & src.lightName;
	createLight( src.jar,src.light,src.version, false );
	fileCopy( src.light,trg.light );

	// Lucee zero build, built from light but also no admin or docs (disabled, 6.0 only)
	/*
	src.zeroName = "lucee-zero-" & src.version & ".jar";
	SystemOutput( "build and upload #src.zeroName# to S3",1,1 );
	src.zero = src.dir & src.zeroName;
	trg.zero = trg.dir & src.zeroName;
	createLight( src.light, src.zero,src.version, true );
	fileCopy( src.zero, trg.zero );
	*/
	// update provider

	systemOutput("Trigger builds", true);
	http url="https://update.lucee.org/rest/update/provider/buildLatest" method="GET" timeout=90 result="buildLatest";
	systemOutput(buildLatest.fileContent, true);

	systemOutput("Update Extension Provider", true);
	http url="https://extension.lucee.org/rest/extension/provider/reset" method="GET" timeout=90 result="extensionReset";
	systemOutput(extensionReset.fileContent, true);

	systemOutput("Update Downloads Page", true);
	http url="https://download.lucee.org/?type=snapshots&reset=force" method="GET" timeout=90 result="downloadUpdate";
	systemOutput("Server response status code: " & downloadUpdate.statusCode, true);

	// forgebox

	systemOutput("Trigger forgebox builds", true);

	gha_pat_token = server.system.environment.LUCEE_DOCKER_FILES_PAT_TOKEN; // github person action token
	body = {
		"event_type": "forgebox_deploy"
	};
	try {
		http url="https://api.github.com/repos/Ortus-Lucee/forgebox-cfengine-publisher/dispatches" method="POST" result="result" timeout="90"{
			httpparam type="header" name='authorization' value='Bearer #gha_pat_token#';
			httpparam type="body" value='#body.toJson()#';
			
		}
		systemOutput("Forgebox build triggered, #result.statuscode# (always returns a 204 no content, see https://github.com/Ortus-Lucee/forgebox-cfengine-publisher/actions for output)", true);
	} catch (e){
		systemOutput("Forgebox build ERRORED?", true);
		echo(e);
	}
	
	// Lucee Docker builds

	systemOutput("Trigger Lucee Docker builds", true);

	gha_pat_token = server.system.environment.LUCEE_DOCKER_FILES_PAT_TOKEN; // github person action token
	body = {
		"event_type": "build-docker-images", 
		"client_payload": { 
			"LUCEE_VERSION": server.system.properties.luceeVersion
		} 
	};
	try {
		http url="https://api.github.com/repos/lucee/lucee-dockerfiles/dispatches" method="POST" result="result" timeout="90"{
			httpparam type="header" name='authorization' value='Bearer #gha_pat_token#';
			httpparam type="body" value='#body.toJson()#';
		}
		systemOutput("Lucee Docker builds triggered, #result.statuscode# (always returns a 204 no content, see https://github.com/lucee/lucee-dockerfiles/actions for output)", true);
	} catch (e){
		systemOutput("Lucee Docker build ERRORED?", true);
		echo(e);
	}

	// express

	private function createLight( string loader, string trg, version, boolean noArchives=false ) {
		var sep = server.separator.file;
		var tmpDir = getTempDirectory();

		local.tmpLoader = tmpDir & "lucee-loader-" & createUniqueId(  ); // the jar
		if ( directoryExists( tmpLoader ) ) 
			directoryDelete( tmpLoader,true );
		directoryCreate( tmpLoader );

		// unzip
		zip action = "unzip" file = loader destination = tmpLoader;

		// remove extensions
		var extDir = tmpLoader&sep&"extensions";
		if ( directoryExists( extDir ) )
			directoryDelete( extDir, true ); // deletes directory with all files inside
		directoryCreate( extDir ); // create empty dir again ( maybe Lucee expect this directory to exist )

		// unzip core
		var lcoFile = tmpLoader & sep & "core" & sep & "core.lco";
		local.tmpCore = tmpDir & "lucee-core-" & createUniqueId(  ); // the jar
		directoryCreate( tmpCore );
		zip action = "unzip" file = lcoFile destination = tmpCore;

		if (arguments.noArchives) {
			// delete the lucee-admin.lar and lucee-docs.lar
			var lightContext =  tmpCore & sep & "resource/context" & sep;
			loop list="lucee-admin.lar,lucee-doc.lar" item="local.larFile" {
				fileDelete( lightContext & larFile );
			}
		}

		// rewrite manifest
		var manifest = tmpCore & sep & "META-INF" & sep & "MANIFEST.MF";
		var content = fileRead( manifest );
		var index = find( 'Require-Extension',content );
		if ( index > 0 ) 
			content = mid( content, 1, index - 1 ) & variables.NL;
		fileWrite( manifest,content );
		
		// zip core
		fileDelete( lcoFile );
		zip action = "zip" source = tmpCore file = lcoFile;
		
		// zip loader
		if ( fileExists( trg ) ) 
			fileDelete( trg );
		zip action = "zip" source = tmpLoader file = trg;
		
	}
</cfscript>