<cfscript>
	// firstly, check are we even deploying to s3
	DO_DEPLOY = server.system.environment.DO_DEPLOY ?: false;

	// LDEV-6537 cdn.lucee.org is retired, from 2027-03-01 00:00 UTC nothing is uploaded to the S3 bucket behind it anymore.
	// Builds are still published to Maven (Sonatype snapshots / Maven Central) and GitHub releases,
	// light and zero are still built here because the maven build attaches them.
	CDN_RETIRED = now() GTE createDateTime( 2027, 3, 1, 0, 0, 0, 0, "UTC" );
	PUBLISH_TO_S3 = DO_DEPLOY && !CDN_RETIRED;

	_logger( "" );
	_logger( " #### Publish Builds to S3" );
	if ( DO_DEPLOY && CDN_RETIRED ) {
		_logger( "Not publishing to S3, cdn.lucee.org is retired since 2027-03-01 (LDEV-6537), only building Light and Zero" );
	}

	// secondly, do we have the s3 extension?
	if ( PUBLISH_TO_S3 ) {
		s3ExtVersion = extensionList().filter( function(row){ return row.name contains "s3"; }).version;
		if ( s3Extversion eq "" ){
			_logger( "ERROR! The S3 Extension isn't installed!" );
			return;
			//throw "The S3 Extension isn't installed!"; // fatal
		} else {
			_logger( "Using S3 Extension: #s3ExtVersion#" );
		}
	}

	// finally check for S3 credentials
	if ( isNull( server.system.environment.S3_ACCESS_ID_DOWNLOAD )
			|| isNull( server.system.environment.S3_SECRET_KEY_DOWNLOAD ) ) {
		if ( PUBLISH_TO_S3 ){
			_logger( "no S3 credentials defined to upload to S3");
			return;
		}
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

	src.mvnJarName = "lucee-"&src.version&".jar";
	src.mvnJarLightName = "lucee-"&src.version&"-light.jar";
	src.mvnJarZeroName = "lucee-"&src.version&"-zero.jar";
	src.mvnLCOName = "lucee-"&src.version&".lco";
	src.mvnMetadataName = "maven-metadata.xml";
	// lucee-6.2.2.78-SNAPSHOT.jar
	if ( ! FileExists( src.jar ) || ! FileExists( src.core ) ){
		_logger( src.jar & " exists: " & FileExists( src.jar ) );
		_logger( src.core & " exists: " & FileExists( src.core ) );
		throw "missing jar or .lco file";
	}

	trg = {};

	// test s3 access
	if ( PUBLISH_TO_S3 ) {
		s3_bucket = "lucee-downloads";
		trg.dir = "s3://#server.system.environment.S3_ACCESS_ID_DOWNLOAD#:#server.system.environment.S3_SECRET_KEY_DOWNLOAD#@/#s3_bucket#/";
		trg.mvnRootDir = trg.dir & "org/lucee/lucee/";
		trg.mvnDir = trg.dir & "org/lucee/lucee/#src.version#/";
		trg.mvnDirRel =  "org/lucee/lucee/#src.version#/";
		
		trg.jar = trg.dir & src.jarName;
		trg.core = trg.dir & src.coreName;

		_logger( "Testing S3 Bucket Access" );
		// it usually will throw an error, rather than even reach this throw, if it fails
		if (! DirectoryExists( trg.dir ) ) {
			_logger( "DirectoryExists failed for s3 bucket [#s3_bucket#]", true );
		}
		else {
			_logger( "S3 Bucket Access OK: [#s3_bucket#]" );
		}
	} 
	else if ( !DO_DEPLOY ) {
		_logger( "Not publishing to S3 as DO_DEPLOY is false, only building Light and Zero" );
	}

	// we only upload / publish artifacts once LDEV-3921
	buildExistsOnS3 = false;
	if ( PUBLISH_TO_S3 && fileExists( trg.jar ) && fileExists( trg.core ) && fileExists( trg.mvnDir & src.mvnJarName ) ){
		_logger( "Build artifacts have already been uploaded to s3 for this version" );
		buildExistsOnS3 = true;
	}

	if ( PUBLISH_TO_S3 && !buildExistsOnS3 ){
		// copy jar
		publishToS3( src.jar, trg.jar, "Publish [#src.jar#] to S3: ");
		publishToS3( src.jar, trg.mvnDir & src.mvnJarName, "Publish [#src.jar#] to [#trg.mvnDirRel & src.mvnJarName#] on S3: ");


		// copy core
		publishToS3( src.core, trg.core, "Publish [#src.core#] to S3: ");
		publishToS3( src.core, trg.mvnDir & src.mvnLCOName, "Publish [#src.core#] to [#trg.mvnDirRel & src.mvnLCOName#] on S3: ");
	}

	// Lucee light build (no extensions)
	src.lightName = "lucee-light-" & src.version & ".jar";
	src.light = src.dir & src.lightName;
	if ( PUBLISH_TO_S3 ){
		if ( !buildExistsOnS3 ){
			_logger( "Build and upload [#src.light#] to S3 / maven" );
		} else {
			_logger( "Build and upload [#src.light#] to maven (already published to s3)" );
		}
	} else {
		_logger( "Build #src.light#" );
	}

	createLight( src.jar,src.light,src.version, false );
	if ( PUBLISH_TO_S3 && !buildExistsOnS3 ){
		trg.light = trg.dir & src.lightName;
		publishToS3( src.light, trg.light, "Publish Light build to s3: ");
		publishToS3( src.light, trg.mvnDir & src.mvnJarLightName, "Publish [#src.light#] to [#trg.mvnDirRel & src.mvnJarLightName#] on S3: ");
	}

	// Lucee zero build, built from light but also no admin or docs
	src.zeroName = "lucee-zero-" & src.version & ".jar";
	src.zero = src.dir & src.zeroName;
	if ( PUBLISH_TO_S3 ){
		if ( !buildExistsOnS3 ){
			_logger( "Build and upload [#src.zero#] to S3 / maven" );
		} else{
			_logger( "Build and upload [#src.zero#] to maven (already published to s3)" );
		}
	} else {
		_logger( "Build #src.zero#"  );
	}

	createLight( src.light, src.zero, src.version, true );

	if ( PUBLISH_TO_S3 && !buildExistsOnS3 ) {
		trg.zero = trg.dir & src.zeroName;
		publishToS3( src.zero, trg.zero, "Publish Zero build to s3: " );
		publishToS3( src.zero, trg.mvnDir & src.mvnJarZeroName, "Publish [#src.zero#] to [#trg.mvnDirRel & src.mvnJarZeroName#] on S3");
	}

	// metadata
	if ( PUBLISH_TO_S3 ) {
		publishMetadataToS3( src.version ,trg.mvnDir & src.mvnMetadataName ,trg.mvnRootDir& src.mvnMetadataName,"Publish Metadata to S3: ");
	}

	if ( !DO_DEPLOY ){
		_logger( "Skipping build triggers, DO_DEPLOY is false" );
		return;
	}

	// update provider
	_logger("Trigger builds" );
	http url="https://update.lucee.org/rest/update/provider/buildLatest" method="GET" timeout=90 result="buildLatest";
	_logger(buildLatest.fileContent );

	_logger("Update Extension Provider" );
	http url="https://extension.lucee.org/rest/extension/provider/reset" method="GET" timeout=90 result="extensionReset";
	_logger(extensionReset.fileContent );

	_logger("Update Downloads Page" );
	http url="https://download.lucee.org/?type=snapshots&reset=force" method="GET" timeout=90 result="downloadUpdate";
	_logger("Server response status code: " & downloadUpdate.statusCode );

	// forgebox

	_logger("Trigger forgebox builds" );

	gha_pat_token = server.system.environment.LUCEE_DOCKER_FILES_PAT_TOKEN; // github person action token
	body = {
		"event_type": "forgebox_deploy"
	};
	try {
		http url="https://api.github.com/repos/Ortus-Lucee/forgebox-cfengine-publisher/dispatches" method="POST" result="result" timeout="90"{
			httpparam type="header" name='authorization' value='Bearer #gha_pat_token#';
			httpparam type="body" value='#body.toJson()#';

		}
		_logger("Forgebox build triggered, #result.statuscode# (always returns a 204 no content, see https://github.com/Ortus-Lucee/forgebox-cfengine-publisher/actions for output)" );
	} catch (e){
		_logger("Forgebox build ERRORED?" );
		echo(e);
	}

	// the Lucee Docker build is triggered by .github/workflows/main.yml after the maven deploy (LDEV-6536)

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

	function _logger( string message="", boolean throw=false ){
		systemOutput( arguments.message, true );
		if ( !len( server.system.environment.GITHUB_STEP_SUMMARY?:"" ))
			return;
		if ( !FileExists( server.system.environment.GITHUB_STEP_SUMMARY  ) ){
			fileWrite( server.system.environment.GITHUB_STEP_SUMMARY, "#### #server.lucee.version# ");
		}

		if ( arguments.throw ) {
			fileAppend( server.system.environment.GITHUB_STEP_SUMMARY, "> [!WARNING]" & chr(10) );
			fileAppend( server.system.environment.GITHUB_STEP_SUMMARY, "> #arguments.message##chr(10)#");
			throw arguments.message;
		} else {
			fileAppend( server.system.environment.GITHUB_STEP_SUMMARY, " #arguments.message##chr(10)#");
		}
	}

	function publishToS3( src, trg, mess ){
		var copyIt=true;
		if ( fileExists( trg ) ) {
			try {
				fileDelete( trg );
			}
			catch(e){
				_logger( message=mess & " file deleted failed" );
				copyIt=false;
			}
		}
		if( copyIt ){
			try {
				fileCopy( src, trg );
			} catch (e){
				// censored error message due to s3 creds!
				 _logger( message=mess & " file copy failed", throw=true );
			}
			_logger( mess & " SUCCESS");
		}
	}

	private function publishMetadataToS3( version,trg, mvnRootMetadata, mess ){
		var dateStr = dateTimeFormat( now(), "yyyyMMddHHnnss" );


		var src='<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>lucee</artifactId>
  <version>#arguments.version#</version>
  <versioning>
    <lastUpdated>#dateStr#</lastUpdated>
    <snapshotVersions>
      <snapshotVersion>
        <extension>jar</extension>
        <value>#arguments.version#</value>
        <updated>#dateStr#</updated>
      </snapshotVersion>
      <snapshotVersion>
        <classifier>light</classifier>
        <extension>jar</extension>
        <value>#arguments.version#</value>
        <updated>#dateStr#</updated>
      </snapshotVersion>
      <snapshotVersion>
        <classifier>zero</classifier>
        <extension>jar</extension>
        <value>#arguments.version#</value>
        <updated>#dateStr#</updated>
      </snapshotVersion>
      <snapshotVersion>
        <extension>lco</extension>
        <value>#arguments.version#</value>
        <updated>#dateStr#</updated>
      </snapshotVersion>
    </snapshotVersions>
  </versioning>
</metadata>';
		var doIt=true;
		if ( fileExists( trg ) ) {
			try {
				fileDelete( trg );
			}
			catch(e){
				_logger( message=mess & " file deleted failed" );
				doIt=false;
			}
		}
		if( doIt ){
			try {
				fileWrite( trg, src );
				_logger( "Publish Metadata to S3: " & trg );
			} catch (e){
				// censored error message due to s3 creds!
				 _logger( message=mess & " file writing failed", throw=true );
			}
		}


		// root
				var srcRoot='<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>lucee</artifactId>
  <versioning>
    <latest>#arguments.version#</latest>
    <release>#findNoCase("-SNAPSHOT", arguments.version) ? "" : arguments.version#</release>
    <versions>
      <version>#arguments.version#</version>
    </versions>
    <lastUpdated>#dateStr#</lastUpdated>
  </versioning>
</metadata>';

		var doIt=true;
		// update
		if ( fileExists( mvnRootMetadata ) ) {
			try {
				var existing=fileRead( mvnRootMetadata );
				existing=updateMetadata(version, existing);
				fileWrite( mvnRootMetadata, existing );
			}
			catch(e){
				_logger( message=mess & " file updating failed" );
				doIt=false;
			}
		}
		// insert
		else {
			try {
				fileWrite( mvnRootMetadata, srcRoot );
				_logger( "Publish Metadata root data to S3: " & mvnRootMetadata );
			} catch (e){
				// censored error message due to s3 creds!
				 _logger( message=mess & " file writing failed", throw=true );
			}
		}



	}

private function updateMetadata(version, existing) {
	if(find("<version>#version#</version>",existing)) return existing;
    
    var dateStr = dateTimeFormat( now(), "yyyyMMddHHnnss" );
    
    
    // lastUpdated
    var start=find("<lastUpdated>",existing);
    if(start==0) throw "invalid input:#existing#";
    var end=find("</lastUpdated>",existing,start+1);
    if(end==0) throw "invalid input:#existing#";
    existing=mid(existing,1,start+12)&dateStr&mid(existing,end);
    
    
    // latest
    var start=find("<latest>",existing);
    if(start==0) throw "invalid input:#existing#";
    var end=find("</latest>",existing,start+1);
    if(end==0) throw "invalid input:#existing#";
    existing=mid(existing,1,start+7)&arguments.version&mid(existing,end);
    
    // release
    if(!findNoCase("-SNAPSHOT",arguments.version)) {
        var start=find("<release>",existing);
        if(start==0) throw "invalid input:#existing#";
        var end=find("</release>",existing,start+1);
        if(end==0) throw "invalid input:#existing#";
        existing=mid(existing,1,start+8)&arguments.version&mid(existing,end);
    }
    
    // add version
    var start=findLast("</version>",existing);
    if(start==0) throw "invalid input:#existing#";
    var end=find("</versions>",existing,start+1);
    if(end==0) throw "invalid input:#existing#";
    
    existing=mid(existing,1,start+9)
        &"
        <version>#arguments.version#</version>
"
        &mid(existing,end);
    
    return existing;
}




	// not used
	private function createWAR( string loader, string trg, version, boolean noArchives=false ) {
		/*
		// create war
		src.warName = "lucee-" & src.version & ".war";
		src.war = src.dir & src.warName;
		trg.war = trg.dir & src.warName;


		_logger( "upload #src.warName# to S3" );
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
	}
</cfscript>