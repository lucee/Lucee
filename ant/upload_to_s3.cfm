<cfscript>
	// firstly, check are we even deploying to s3
	DO_DEPLOY = server.system.environment.DO_DEPLOY ?: false;

	// LDEV-6537 / LDEV-XXXX: the one and only cut-off date for S3 uploads (bucket lucee-downloads = cdn.lucee.org).
	// until the cut-off: every build (incl. SNAPSHOT) goes to the bucket root (legacy names) AND the Maven layout org/lucee/lucee/<version>/
	// from the cut-off:  SNAPSHOTs are not uploaded to S3 at all (they live on Sonatype snapshots),
	//                    RC/BETA/final builds only go to the Maven layout, the bucket root is no longer written.
	S3_CUTOFF = createDateTime( 2027, 3, 1, 0, 0, 0, 0, "UTC" );
	CDN_RETIRED = now() GTE S3_CUTOFF;
	IS_SNAPSHOT = findNoCase( "-SNAPSHOT", server.system.properties.luceeVersion ?: listLast( server.system.properties.luceeCore, "\/" ) ) GT 0;
	PUBLISH_TO_S3 = DO_DEPLOY && ( !CDN_RETIRED || !IS_SNAPSHOT );
	PUBLISH_TO_S3_ROOT = PUBLISH_TO_S3 && !CDN_RETIRED;

	_logger( "" );
	_logger( " #### Publish Builds to S3" );
	if ( DO_DEPLOY && !PUBLISH_TO_S3 ) {
		_logger( "Not publishing SNAPSHOT to S3, since #dateTimeFormat( S3_CUTOFF, "yyyy-mm-dd", "UTC" )# snapshots are only on Sonatype (LDEV-6537), only building Light and Zero" );
	} else if ( PUBLISH_TO_S3 && !PUBLISH_TO_S3_ROOT ) {
		_logger( "Publishing to the Maven layout only, the bucket root is retired since #dateTimeFormat( S3_CUTOFF, "yyyy-mm-dd", "UTC" )# (LDEV-6537)" );
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
	src.mvnPomName = "lucee-"&src.version&".pom";
	src.mvnMetadataName = "maven-metadata.xml";
	// flattened pom (${revision} resolved) written by flatten-maven-plugin
	src.pom = server.system.properties.luceePom ?: "";
	// lucee-6.2.2.78-SNAPSHOT.jar
	if ( ! FileExists( src.jar ) || ! FileExists( src.core ) ){
		_logger( src.jar & " exists: " & FileExists( src.jar ) );
		_logger( src.core & " exists: " & FileExists( src.core ) );
		throw "missing jar or .lco file";
	}

	trg = {};

	// test s3 access
	if ( PUBLISH_TO_S3 ) {
		s3_bucket = server.system.environment.S3_DOWNLOAD_BUCKET ?: "lucee-downloads"; // set in .github/workflows/main.yml
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
	if ( PUBLISH_TO_S3 && fileExists( trg.mvnDir & src.mvnJarName ) && ( !PUBLISH_TO_S3_ROOT || ( fileExists( trg.jar ) && fileExists( trg.core ) ) ) ){
		_logger( "Build artifacts have already been uploaded to s3 for this version" );
		buildExistsOnS3 = true;
	}

	if ( PUBLISH_TO_S3 && !buildExistsOnS3 ){
		// copy jar
		if ( PUBLISH_TO_S3_ROOT ) publishToS3( src.jar, trg.jar, "Publish [#src.jar#] to S3: ");
		publishToS3( src.jar, trg.mvnDir & src.mvnJarName, "Publish [#src.jar#] to [#trg.mvnDirRel & src.mvnJarName#] on S3: ");


		// copy core
		if ( PUBLISH_TO_S3_ROOT ) publishToS3( src.core, trg.core, "Publish [#src.core#] to S3: ");
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
		if ( PUBLISH_TO_S3_ROOT ) publishToS3( src.light, trg.light, "Publish Light build to s3: ");
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
		if ( PUBLISH_TO_S3_ROOT ) publishToS3( src.zero, trg.zero, "Publish Zero build to s3: " );
		publishToS3( src.zero, trg.mvnDir & src.mvnJarZeroName, "Publish [#src.zero#] to [#trg.mvnDirRel & src.mvnJarZeroName#] on S3");
	}

	// pom (LDEV-XXXX), so Maven builds can resolve org.lucee:lucee from the Maven layout
	if ( PUBLISH_TO_S3 && !buildExistsOnS3 ) {
		if ( len( src.pom ) && fileExists( src.pom ) ) {
			publishToS3( src.pom, trg.mvnDir & src.mvnPomName, "Publish [#src.pom#] to [#trg.mvnDirRel & src.mvnPomName#] on S3: " );
		} else {
			_logger( "WARNING: flattened pom [#src.pom#] not found, no pom uploaded to S3" );
		}
	}

	// metadata
	if ( PUBLISH_TO_S3 ) {
		publishMetadataToS3( src.version ,trg.mvnDir & src.mvnMetadataName ,trg.mvnRootDir& src.mvnMetadataName,"Publish Metadata to S3: ");
	}

	if ( PUBLISH_TO_S3 && len( server.system.environment.GITHUB_ENV ?: "" ) ) {
		// main.yml regenerates org/lucee/index.html + org/lucee/maven-metadata.xml only when this is set
		fileAppend( server.system.environment.GITHUB_ENV, "S3_PUBLISHED=true" & chr( 10 ) );
	}

	if ( !DO_DEPLOY ){
		_logger( "Skipping build triggers, DO_DEPLOY is false" );
		return;
	}

	// The update provider (buildLatest) and the extension provider (reset) are
	// no-ops on the Maven Cloud Run provider: versions and listings come from
	// Maven metadata on a TTL cache. A core publish does not change extensions.
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
		fileWrite( extDir & sep & ".index", "" ); // empty index, no bundled extensions

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
			publishHashesToS3( src, trg, mess );
		}
	}

	// LDEV-6106: static <file>.sha256 and <file>.sha512 next to every published file, in the format of sha256sum / sha512sum,
	// so a download can be verified with: sha256sum -c <file>.sha256 (the name inside is the name of the published file)
	function publishHashesToS3( src, trg, mess ){
		var name = listLast( arguments.trg, "/" );
		var hashes = getHashes( arguments.src );
		loop list="sha256,sha512" item="local.ext" {
			try {
				fileWrite( arguments.trg & "." & ext, hashes[ ext ] & "  " & name & chr( 10 ) );
			} catch (e){
				// censored error message due to s3 creds!
				_logger( message=arguments.mess & " writing #name#.#ext# failed", throw=true );
			}
			_logger( "Publish #name#.#ext# to S3: SUCCESS" );
		}
	}

	// lower case hex SHA-256 and SHA-512 of a local file, calculated once per file (a file is published more than once, root and Maven layout)
	private function getHashes( src ){
		variables.hashCache = variables.hashCache ?: {};
		if ( !structKeyExists( variables.hashCache, arguments.src ) ) {
			var bytes = fileReadBinary( arguments.src );
			variables.hashCache[ arguments.src ] = {
				sha256: lCase( hash( bytes, "SHA-256" ) ),
				sha512: lCase( hash( bytes, "SHA-512" ) )
			};
		}
		return variables.hashCache[ arguments.src ];
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
        <extension>pom</extension>
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

// rewrites the artifact maven-metadata.xml with the new version added: <versions> in Maven version order (oldest first),
// <latest> = highest version, <release> = highest non-SNAPSHOT version. It used to append the new version at the end and
// set latest/release to it, so the file was in publish order and latest/release jumped between the 6.2/7.1/8.0/9.0 lines.
private function updateMetadata(version, existing) {
	var versions = [];
	var block = reFind( "<versions>(.*?)</versions>", arguments.existing, 1, true );
	if ( !block.len[ 1 ] ) throw "invalid input:#arguments.existing#";
	var content = mid( arguments.existing, block.pos[ 2 ], block.len[ 2 ] );
	var pos = 1;
	while ( true ) {
		var m = reFind( "<version>([^<]+)</version>", content, pos, true );
		if ( !m.len[ 1 ] ) break;
		var v = trim( mid( content, m.pos[ 2 ], m.len[ 2 ] ) );
		if ( !arrayFindNoCase( versions, v ) ) arrayAppend( versions, v );
		pos = m.pos[ 1 ] + m.len[ 1 ];
	}
	if ( !arrayFindNoCase( versions, arguments.version ) ) arrayAppend( versions, arguments.version );
	versions = sortMavenVersions( versions );

	var latest = versions[ arrayLen( versions ) ];
	var release = "";
	for ( var i = arrayLen( versions ); i >= 1; i-- ) {
		if ( !findNoCase( "-SNAPSHOT", versions[ i ] ) ) {
			release = versions[ i ];
			break;
		}
	}

	var groupId = reFind( "<groupId>([^<]+)</groupId>", arguments.existing, 1, true );
	var artifactId = reFind( "<artifactId>([^<]+)</artifactId>", arguments.existing, 1, true );
	var nl = chr( 10 );
	var xml = '<?xml version="1.0" encoding="UTF-8"?>' & nl
		& '<metadata>' & nl
		& '  <groupId>' & ( groupId.len[ 1 ] ? mid( arguments.existing, groupId.pos[ 2 ], groupId.len[ 2 ] ) : "org.lucee" ) & '</groupId>' & nl
		& '  <artifactId>' & ( artifactId.len[ 1 ] ? mid( arguments.existing, artifactId.pos[ 2 ], artifactId.len[ 2 ] ) : "lucee" ) & '</artifactId>' & nl
		& '  <versioning>' & nl
		& '    <latest>' & latest & '</latest>' & nl
		& '    <release>' & release & '</release>' & nl
		& '    <versions>' & nl;
	for ( var v in versions ) xml &= '      <version>' & v & '</version>' & nl;
	xml &= '    </versions>' & nl
		& '    <lastUpdated>' & dateTimeFormat( now(), "yyyyMMddHHnnss" ) & '</lastUpdated>' & nl
		& '  </versioning>' & nl
		& '</metadata>';
	return xml;
}

// Maven ComparableVersion order (org.apache.maven.artifact.versioning.ComparableVersion), same as
// .github/scripts/maven-group-metadata.js: numbers compare numerically, alpha < beta < milestone < rc < snapshot < release < sp
private function sortMavenVersions( required array versions ) {
	var parsed = {};
	for ( var v in arguments.versions ) parsed[ v ] = parseMavenVersion( v );
	var sorted = duplicate( arguments.versions );
	arraySort( sorted, function( a, b ) {
		var c = compareMavenItems( parsed[ a ], parsed[ b ] );
		return c != 0 ? c : compare( a, b );
	} );
	return sorted;
}

private function compareMavenVersions( required string a, required string b ) {
	return compareMavenItems( parseMavenVersion( arguments.a ), parseMavenVersion( arguments.b ) );
}

private function mavenQualifier( required string q ) {
	var qualifiers = [ "alpha", "beta", "milestone", "rc", "snapshot", "", "sp" ];
	var i = arrayFind( qualifiers, arguments.q );
	return i ? toString( i - 1 ) : arrayLen( qualifiers ) & "-" & arguments.q;
}

private function mavenStringItem( required string value, boolean followedByDigit=false ) {
	var v = arguments.value;
	if ( arguments.followedByDigit && len( v ) == 1 ) {
		if ( v == "a" ) v = "alpha";
		else if ( v == "b" ) v = "beta";
		else if ( v == "m" ) v = "milestone";
	}
	if ( v == "ga" || v == "final" || v == "release" ) v = "";
	else if ( v == "cr" ) v = "rc";
	return { type: "string", value: v };
}

private function mavenParseItem( required boolean isDigit, required string buf ) {
	if ( !arguments.isDigit ) return mavenStringItem( arguments.buf );
	var n = reReplace( arguments.buf, "^0+", "" );
	return { type: "int", value: n }; // digits without leading zeros, "" is 0
}

private function mavenIsNull( required struct item ) {
	if ( item.type == "int" ) return item.value == "";
	if ( item.type == "string" ) return mavenQualifier( item.value ) == "5";
	return arrayLen( item.items ) == 0;
}

private function parseMavenVersion( required string version ) {
	var v = lCase( trim( arguments.version ) );
	var root = { type: "list", items: [] };
	var list = root;
	var stack = [ root ];
	var isDigit = false;
	var start = 1;
	var l = len( v );
	for ( var i = 1; i <= l; i++ ) {
		var c = mid( v, i, 1 );
		if ( c == "." || c == "-" ) {
			arrayAppend( list.items, i == start ? { type: "int", value: "" } : mavenParseItem( isDigit, mid( v, start, i - start ) ) );
			start = i + 1;
			if ( c == "-" ) {
				var sub = { type: "list", items: [] };
				arrayAppend( list.items, sub );
				list = sub;
				arrayAppend( stack, sub );
			}
		}
		else if ( asc( c ) >= 48 && asc( c ) <= 57 ) {
			if ( !isDigit && i > start ) {
				arrayAppend( list.items, mavenStringItem( mid( v, start, i - start ), true ) );
				start = i;
				var sub = { type: "list", items: [] };
				arrayAppend( list.items, sub );
				list = sub;
				arrayAppend( stack, sub );
			}
			isDigit = true;
		}
		else {
			if ( isDigit && i > start ) {
				arrayAppend( list.items, mavenParseItem( true, mid( v, start, i - start ) ) );
				start = i;
				var sub = { type: "list", items: [] };
				arrayAppend( list.items, sub );
				list = sub;
				arrayAppend( stack, sub );
			}
			isDigit = false;
		}
	}
	if ( l >= start ) arrayAppend( list.items, mavenParseItem( isDigit, mid( v, start, l - start + 1 ) ) );
	// normalize: drop trailing null items (0, "", release qualifiers, empty lists) of every list, innermost first
	for ( var s = arrayLen( stack ); s >= 1; s-- ) {
		var items = stack[ s ].items;
		for ( var i = arrayLen( items ); i >= 1; i-- ) {
			if ( mavenIsNull( items[ i ] ) ) arrayDeleteAt( items, i );
			else if ( items[ i ].type != "list" ) break;
		}
	}
	return root;
}

private function compareMavenItems( required struct a, struct b ) {
	if ( isNull( arguments.b ) ) {
		if ( a.type == "int" ) return a.value == "" ? 0 : 1;
		if ( a.type == "string" ) return sgn( compare( mavenQualifier( a.value ), "5" ) );
		return arrayLen( a.items ) ? compareMavenItems( a.items[ 1 ] ) : 0;
	}
	if ( a.type == "int" ) {
		if ( b.type != "int" ) return 1;
		if ( len( a.value ) != len( b.value ) ) return len( a.value ) < len( b.value ) ? -1 : 1;
		return sgn( compare( a.value, b.value ) );
	}
	if ( a.type == "string" ) {
		if ( b.type != "string" ) return -1;
		return sgn( compare( mavenQualifier( a.value ), mavenQualifier( b.value ) ) );
	}
	if ( b.type == "int" ) return -1;
	if ( b.type == "string" ) return 1;
	var n = max( arrayLen( a.items ), arrayLen( b.items ) );
	for ( var i = 1; i <= n; i++ ) {
		var hasL = i <= arrayLen( a.items );
		var hasR = i <= arrayLen( b.items );
		var r = 0;
		if ( !hasL ) r = hasR ? -compareMavenItems( b.items[ i ] ) : 0;
		else r = hasR ? compareMavenItems( a.items[ i ], b.items[ i ] ) : compareMavenItems( a.items[ i ] );
		if ( r != 0 ) return r;
	}
	return 0;
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