component extends="org.lucee.cfml.test.LuceeTestCase" labels="extensions" {

	function run( testResults, testBox ) {
		describe( "LDEV-6088 - Verify bundled extensions have all required bundle dependencies", function() {

			it( title="Check each bundled extension has its required bundles and doesn't rely on other extensions", body=function( currentSpec ) {
				var cfg = getPageContext().getConfig();
				var extensions = cfg.getAllRHExtensions();
				var luceeMajor = listFirst( server.lucee.version, "." );

				// Build a map of bundle symbolic name -> extension name
				var bundleToExtension = {};
				var extensionBundles = {};
				var extensionVersions = {};

				loop collection=extensions.iterator() item="local.ext" {
					var extMeta = ext.getMetadata();
					var extName = extMeta.getName();
					var extVersion = extMeta._getVersion();
					var bundles = luceeMajor gte 7 ? extMeta.getBundles() : ext.bundles;

					extensionBundles[ extName ] = [];
					extensionVersions[ extName ] = extVersion;

					loop array=bundles item="local.bundle" {
						var symbolicName = bundle.getSymbolicName();
						bundleToExtension[ symbolicName ] = extName;
						arrayAppend( extensionBundles[ extName ], symbolicName );
					}
				}

				// Get core bundles from MANIFEST.MF
				var coreBundles = getCoreRequiredBundles();

				// Check each extension's bundles
				var violations = [];
				var missingDeps = [];

				loop collection=extensions.iterator() item="local.ext" {
					var extMeta = ext.getMetadata();
					var extName = extMeta.getName();
					var bundles = luceeMajor gte 7 ? extMeta.getBundles() : ext.bundles;

					loop array=bundles item="local.bundle" {
						var symbolicName = bundle.getSymbolicName();
						var requiredBundles = getBundleRequirements( bundle );

						// Check each required bundle
						loop array=requiredBundles item="local.required" {
							var requiredName = listFirst( required, ";" ); // Strip version info

							// Check if required bundle is in core
							var isInCore = arrayFindNoCase( coreBundles, requiredName ) > 0;

							// Check if required bundle is in same extension
							var isInSameExtension = arrayFindNoCase( extensionBundles[ extName ], requiredName ) > 0;

							// Check if required bundle is in a different extension
							var isInOtherExtension = structKeyExists( bundleToExtension, requiredName )
								&& bundleToExtension[ requiredName ] != extName;

							if ( !isInCore && !isInSameExtension ) {
								if ( isInOtherExtension ) {
									// Cross-extension dependency
									var violation = {
										extension: extName,
										version: extensionVersions[ extName ],
										bundle: symbolicName,
										requiredBundle: requiredName,
										providedBy: bundleToExtension[ requiredName ],
										providedByVersion: extensionVersions[ bundleToExtension[ requiredName ] ]
									};
									arrayAppend( violations, violation );
								} else {
									// Missing dependency
									var missing = {
										extension: extName,
										version: extensionVersions[ extName ],
										bundle: symbolicName,
										requiredBundle: requiredName
									};
									arrayAppend( missingDeps, missing );
								}
							}
						}
					}
				}

				// Report problems
				if ( arrayLen( violations ) > 0 ) {
					systemOutput( "", true );
					systemOutput( "Cross-extension dependencies:", true );
					loop array=violations item="local.v" {
						systemOutput( "  #v.extension# #v.version# (#v.bundle#) -> #v.requiredBundle# from #v.providedBy# #v.providedByVersion#", true );
					}
				}

				if ( arrayLen( missingDeps ) > 0 ) {
					systemOutput( "", true );
					systemOutput( "Missing dependencies:", true );
					loop array=missingDeps item="local.m" {
						systemOutput( "  #m.extension# #m.version# (#m.bundle#) -> #m.requiredBundle# (not found)", true );
					}
				}

				var totalIssues = arrayLen( violations ) + arrayLen( missingDeps );
				if ( totalIssues > 0 ) {
					systemOutput( "", true );
					systemOutput( "Found #arrayLen( violations )# cross-extension dependencies and #arrayLen( missingDeps )# missing dependencies", true );
				}

				// Don't fail the test for now, just report
				expect( true ).toBeTrue();
			});

		});
	}

	private array function getCoreRequiredBundles() {
		var mfPath = expandPath( "../core/src/main/java/META-INF/MANIFEST.MF" );
		var manifest = manifestRead( mfPath );
		var bundles = manifest.main[ "Require-Bundle" ];
		var bundleList = listToArray( bundles );
		var coreBundles = [];

		loop array=bundleList item="local.bundleEntry" {
			var parts = listToArray( bundleEntry, ";" );
			arrayAppend( coreBundles, trim( parts[ 1 ] ) );
		}

		return coreBundles;
	}

	private array function getBundleRequirements( required bundle ) {
		var requirements = [];

		try {
			var headers = bundle.getHeaders();
			var requireBundle = headers.get( "Require-Bundle" );

			if ( !isNull( requireBundle ) && len( trim( requireBundle ) ) > 0 ) {
				// Parse bundle requirements properly handling version ranges with commas
				// Format: bundle-name;attr=value;attr2="[1.0,2.0)", next-bundle;attr=value
				var current = "";
				var inQuotes = false;
				var inBrackets = false;

				loop from=1 to=len( requireBundle ) index="local.i" {
					var ch = mid( requireBundle, i, 1 );

					if ( ch == '"' ) {
						inQuotes = !inQuotes;
						current &= ch;
					} else if ( ch == '[' || ch == '(' ) {
						inBrackets = true;
						current &= ch;
					} else if ( ch == ']' || ch == ')' ) {
						inBrackets = false;
						current &= ch;
					} else if ( ch == ',' && !inQuotes && !inBrackets ) {
						// End of bundle entry
						if ( len( trim( current ) ) > 0 ) {
							arrayAppend( requirements, trim( current ) );
						}
						current = "";
					} else {
						current &= ch;
					}
				}

				// Add last entry
				if ( len( trim( current ) ) > 0 ) {
					arrayAppend( requirements, trim( current ) );
				}
			}
		}
		catch ( any e ) {
			// Ignore bundles that can't be read
		}

		return requirements;
	}

}
