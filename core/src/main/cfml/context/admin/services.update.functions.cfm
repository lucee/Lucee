<cfscript>
	function getUpdateData() {
		admin
			action="getUpdate"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"
			returnvariable="local.update";
			
		// this should not be necessary, but needed for testing with dealing with current admin code and older Lucee versions
		if(update.location=="http://snapshot.lucee.org" || update.location=="https://snapshot.lucee.org") update.location="https://update.lucee.org";
		if(update.location=="http://release.lucee.org" || update.location=="https://release.lucee.org") update.location="https://update.lucee.org";
		return update;
	}

	struct function getAvailableVersion() localmode="true"{
		restBasePath="/rest/update/provider/";
		try{

			admin
				action="getAPIKey"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				returnVariable="apiKey";
			
			var update=getUpdateData();

			http cachedWithin=createTimespan(0,0,5,0)
			url="#update.location##restBasePath#info/#server.lucee.version#"
			method="get" resolveurl="no" result="local.http" {
				if(!isNull(apiKey))httpparam type="header" name="ioid" value="#apikey#";
			}
			
			// i have a response
			if(isJson(http.filecontent)) {
				rsp=deserializeJson(http.filecontent);
			}
			// service not available
			else if(http.status_code==404) {
				rsp={"type":"warning","message":replace(stText.services.update.serverNotReachable,'{url}',update.location)};
			}
			// server failed
			else {
				rsp={"type":"warning","message":replace(stText.services.update.serverFailed,'{url}',update.location)&" "&http.filecontent};
			}
			rsp.code=http.status_code?:404;
		}
		catch(e){
			rsp={"type":"warning","message":replace(stText.services.update.serverFailed,'{url}',update.location)&" "&e.message};
		}
		rsp.provider=update;
		return rsp;
	}

	string function getUpdateForMajorVersion( array versions, numeric majorVersion ){
		loop from="#arrayLen(arguments.versions)#" to="1" index="local.v" step="-1" {
			if ( listfirst(arguments.versions[ v ],".") eq arguments.majorVersion ){
				return arguments.versions[ v ];
			}
		}
		return "";
	}

	/**
	 * Checks if a version string is a stable release (no pre-release suffix)
	 * @version the version string to check
	 * @return true if stable, false if SNAPSHOT/ALPHA/BETA/RC
	 */
	function isStableVersion( version ) {
		return !findNoCase( "-SNAPSHOT", arguments.version )
			&& !findNoCase( "-ALPHA", arguments.version )
			&& !findNoCase( "-BETA", arguments.version )
			&& !findNoCase( "-RC", arguments.version );
	}

	/**
	 * Extracts the suffix from a version string
	 * @version the version string (e.g. "6.2.5.22-SNAPSHOT")
	 * @return the suffix (e.g. "SNAPSHOT") or empty string for stable
	 */
	function getVersionSuffix( version ) {
		return listLen( arguments.version, "-" ) > 1 ? listLast( arguments.version, "-" ) : "";
	}

	/**
	 * Gets the minor version (first 3 parts) from a version string
	 * e.g. "6.2.5.22-SNAPSHOT" -> "6.2.5"
	 */
	function getMinorVersion( version ) {
		var base = listFirst( arguments.version, "-" );
		var parts = listToArray( base, "." );
		if ( arrayLen( parts ) >= 3 ) {
			return parts[ 1 ] & "." & parts[ 2 ] & "." & parts[ 3 ];
		}
		return base;
	}

	/**
	 * Determines if there's a newer version available
	 * Rules:
	 * - Stable users: see RC and stable updates only
	 * - BETA/RC users: see SNAPSHOT only within same minor version (bug fixes), otherwise BETA+
	 * - SNAPSHOT users: see SNAPSHOT or higher (not ALPHA)
	 * - ALPHA users: see anything newer
	 * @currentVersion the current Lucee version string (e.g. "6.2.5.22-SNAPSHOT")
	 * @updateInfo struct containing "available" and "otherVersions" from update server
	 * @return struct with { hasUpdate: boolean, availableVersion: string }
	 */
	struct function hasNewerVersion( required string currentVersion, required struct updateInfo ) {
		var result = { hasUpdate: false, availableVersion: "" };

		if ( !structKeyExists( arguments.updateInfo, "available" ) ) {
			return result;
		}

		var curr = arguments.currentVersion;
		var majorVersion = listFirst( curr, "." );
		var currMinorVersion = getMinorVersion( curr );
		var currSuffix = getVersionSuffix( curr );
		var isBetaOrRC = ( currSuffix === "BETA" || currSuffix === "RC" );

		var candidates = [];
		if ( isArray( arguments.updateInfo.otherVersions ?: "" ) ) {
			for ( var ver in arguments.updateInfo.otherVersions ) {
				var verSuffix = getVersionSuffix( ver );
				var verSuffixRank = getSuffixRank( verSuffix );

				// ALPHA is never suggested except to ALPHA users
				if ( verSuffix === "ALPHA" && currSuffix !== "ALPHA" ) {
					continue;
				}

				// stable users: RC or higher only
				if ( isStableVersion( curr ) && verSuffixRank < getSuffixRank( "RC" ) ) {
					continue;
				}

				// BETA/RC users: SNAPSHOT only within same minor version
				if ( isBetaOrRC && verSuffix === "SNAPSHOT" ) {
					if ( getMinorVersion( ver ) !== currMinorVersion ) {
						continue;
					}
				}

				arrayAppend( candidates, ver );
			}
		}

		if ( arrayLen( candidates ) ) {
			// get latest candidate (array is sorted oldest to newest)
			var available = arrayLast( candidates );
			var availableMajor = listFirst( available, "." );

			// if different major, find one for our major
			if ( majorVersion != availableMajor ) {
				available = "";
				for ( var i = arrayLen( candidates ); i >= 1; i-- ) {
					if ( listFirst( candidates[ i ], "." ) == majorVersion ) {
						available = candidates[ i ];
						break;
					}
				}
			}

			if ( len( available ) && compareVersions( curr, available ) < 0 ) {
				result.availableVersion = available;
				result.hasUpdate = true;
			}
		}

		return result;
	}

	/**
	 * Categorizes versions into upgrade/downgrade lists by type (release, pre_release, snapshot, alpha)
	 * @versions array of version strings from the update server
	 * @currentVersion the current Lucee version
	 * @hasLoader7 boolean indicating if loader supports Lucee 7+
	 * @return struct with keys: release, pre_Release, snapShot, alpha - each containing upgrade[] and downgrade[]
	 */
	struct function categorizeVersions( required array versions, required string currentVersion, required boolean hasLoader7 ) {
		var result = {
			"snapShot": { "upgrade": [], "downgrade": [] },
			"pre_Release": { "upgrade": [], "downgrade": [] },
			"release": { "upgrade": [], "downgrade": [] },
			"alpha": { "upgrade": [], "downgrade": [] }
		};

		for ( var ver in arguments.versions ) {
			if ( ver == arguments.currentVersion ) continue;
			if ( !arguments.hasLoader7 && listFirst( ver, "." ) > 6 ) continue;

			var isDowngrade = compareVersions( ver, arguments.currentVersion ) <= 0;
			var targetArray = isDowngrade ? "downgrade" : "upgrade";

			if ( findNoCase( "SNAPSHOT", ver ) ) {
				arrayPrepend( result.snapShot[ targetArray ], ver );
			} else if ( findNoCase( "ALPHA", ver ) ) {
				arrayPrepend( result.alpha[ targetArray ], ver );
			} else if ( findNoCase( "BETA", ver ) || findNoCase( "RC", ver ) ) {
				arrayPrepend( result.pre_Release[ targetArray ], ver );
			} else {
				arrayPrepend( result.release[ targetArray ], ver );
			}
		}

		return result;
	}

	/**
	 * Returns numeric rank for version suffix (higher = more stable)
	 * Matches ranking from lucee-data-provider VersionUtils.cfc
	 */
	function getSuffixRank( suffix ) {
		if ( arguments.suffix === "SNAPSHOT" ) return 25;   // snapshot (dev builds, most common - check first)
		if ( arguments.suffix === "ALPHA" ) return 0;       // alpha (lowest, cowboy/experimental)
		if ( arguments.suffix === "BETA" ) return 50;       // beta
		if ( arguments.suffix === "RC" ) return 75;         // release candidate
		if ( arguments.suffix === "" ) return 100;          // stable release
		return 75;                                          // unknown suffix treated as RC-level
	}

	/**
	 * Compares two version strings numerically
	 * @return -1 if v1 < v2, 0 if equal, 1 if v1 > v2
	 */
	function compareVersions( v1,  v2 ) {
		// strip suffix for base comparison
		var base1 = listFirst( arguments.v1, "-" );
		var base2 = listFirst( arguments.v2, "-" );
		var suffix1 = listLen( arguments.v1, "-" ) > 1 ? listLast( arguments.v1, "-" ) : "";
		var suffix2 = listLen( arguments.v2, "-" ) > 1 ? listLast( arguments.v2, "-" ) : "";

		// compare each numeric part
		var parts1 = listToArray( base1, "." );
		var parts2 = listToArray( base2, "." );
		var maxLen = max( arrayLen( parts1 ), arrayLen( parts2 ) );

		for ( var i = 1; i <= maxLen; i++ ) {
			var num1 = i <= arrayLen( parts1 ) ? val( parts1[ i ] ) : 0;
			var num2 = i <= arrayLen( parts2 ) ? val( parts2[ i ] ) : 0;
			if ( num1 < num2 ) return -1;
			if ( num1 > num2 ) return 1;
		}

		// base versions equal, compare suffixes by rank
		var rank1 = getSuffixRank( suffix1 );
		var rank2 = getSuffixRank( suffix2 );
		if ( rank1 < rank2 ) return -1;
		if ( rank1 > rank2 ) return 1;
		return 0;
	}

</cfscript>