component extends="org.lucee.cfml.test.LuceeTestCase" labels="LDEV-6278,maven" {

	function run( testResults, testBox ) {

		describe( "LDEV-6278 — runtime resolver supports maven version range specs", function() {

			// ---------------------------------------------------------------------
			// Acceptance criteria — unit tests (no network required).
			// Calls MavenUtil.resolveVersionRange directly to prove each spec-form
			// lands on the right code path.
			// ---------------------------------------------------------------------

			describe( "resolveVersionRange — parse + best-effort resolution (no metadata fetch)", function() {

				it( "pinned [1.0] returns '1.0' and does not throw", function() {
					var util = createObject( "java", "lucee.runtime.mvn.MavenUtil" );
					expect( util.resolveVersionRange( "[1.0]" ) ).toBe( "1.0" );
				});

				it( "bare version passes through unchanged (soft pin)", function() {
					var util = createObject( "java", "lucee.runtime.mvn.MavenUtil" );
					expect( util.resolveVersionRange( "1.0" ) ).toBe( "1.0" );
					expect( util.resolveVersionRange( "1.0-SNAPSHOT" ) ).toBe( "1.0-SNAPSHOT" );
					expect( util.resolveVersionRange( "2.3.9" ) ).toBe( "2.3.9" );
				});

				it( "any bracketed range with bounds falls through to the raw spec on the single-arg path", function() {
					var util = createObject( "java", "lucee.runtime.mvn.MavenUtil" );
					// The single-arg form has no metadata context and MUST NOT guess
					// that the lower bound of a range is a real version. `[2.2,3)` means
					// "any version in [2.2, 3)", not "2.2 exists" — many artifacts
					// (jaxb-runtime, etc.) declare such ranges where the literal lower
					// bound was never published. Caller must use the 5-arg form for
					// metadata-driven resolution; otherwise the raw spec bubbles up
					// and the download fails loudly.
					expect( util.resolveVersionRange( "[2.2,3)" ) ).toBe( "[2.2,3)" );
					expect( util.resolveVersionRange( "[1.0,)" ) ).toBe( "[1.0,)" );
					expect( util.resolveVersionRange( "(1.0,2.0]" ) ).toBe( "(1.0,2.0]" );
					expect( util.resolveVersionRange( "(,1.0]" ) ).toBe( "(,1.0]" );
					expect( util.resolveVersionRange( "[1.0,1.5],[2.0,)" ) ).toBe( "[1.0,1.5],[2.0,)" );
					expect( util.resolveVersionRange( "[1.2.*]" ) ).toBe( "[1.2.*]" );
				});

				it( "malformed specs fall through gracefully (no stack trace at parse time)", function() {
					var util = createObject( "java", "lucee.runtime.mvn.MavenUtil" );
					expect( util.resolveVersionRange( "[garbage" ) ).toBe( "[garbage" );
					expect( util.resolveVersionRange( "" ) ).toBe( "" );
				});
			});

			// ---------------------------------------------------------------------
			// MavenVersion.isPreRelease — used by pickHighest to filter transitive
			// milestone/alpha/beta/rc/snapshot candidates that a bare-release range
			// shouldn't pull in.
			// ---------------------------------------------------------------------
			describe( "MavenVersion.isPreRelease — qualifier detection", function() {

				it( "returns false for plain release versions", function() {
					expect( createObject( "java", "lucee.runtime.mvn.MavenVersion" ).init( "1.0" ).isPreRelease() ).toBeFalse();
					expect( createObject( "java", "lucee.runtime.mvn.MavenVersion" ).init( "2.3.9" ).isPreRelease() ).toBeFalse();
					expect( createObject( "java", "lucee.runtime.mvn.MavenVersion" ).init( "1.0-ga" ).isPreRelease() ).toBeFalse();
					expect( createObject( "java", "lucee.runtime.mvn.MavenVersion" ).init( "1.0-final" ).isPreRelease() ).toBeFalse();
					expect( createObject( "java", "lucee.runtime.mvn.MavenVersion" ).init( "1.0-sp1" ).isPreRelease() ).toBeFalse();
				});

				it( "returns true for alpha/beta/milestone/rc/cr/snapshot qualifiers", function() {
					expect( createObject( "java", "lucee.runtime.mvn.MavenVersion" ).init( "1.0-alpha-1" ).isPreRelease() ).toBeTrue();
					expect( createObject( "java", "lucee.runtime.mvn.MavenVersion" ).init( "1.0-beta" ).isPreRelease() ).toBeTrue();
					expect( createObject( "java", "lucee.runtime.mvn.MavenVersion" ).init( "3.0.0-M5" ).isPreRelease() ).toBeTrue();
					expect( createObject( "java", "lucee.runtime.mvn.MavenVersion" ).init( "1.0-rc1" ).isPreRelease() ).toBeTrue();
					expect( createObject( "java", "lucee.runtime.mvn.MavenVersion" ).init( "1.0-cr1" ).isPreRelease() ).toBeTrue();
					expect( createObject( "java", "lucee.runtime.mvn.MavenVersion" ).init( "1.0-SNAPSHOT" ).isPreRelease() ).toBeTrue();
				});
			});

			// ---------------------------------------------------------------------
			// MavenVersionRange.pickHighest — critical piece of metadata-driven
			// resolution. Pre-releases must be filtered unless the range spec
			// itself names a pre-release bound (otherwise transitive [2.2,3)
			// pulls in 3.0.0-M5 over 2.3.9, which is nobody's expectation).
			// ---------------------------------------------------------------------
			describe( "MavenVersionRange.pickHighest — pre-release filtering", function() {

				it( "skips milestones/alphas/betas when range is release-bounded", function() {
					var range = createObject( "java", "lucee.runtime.mvn.MavenVersionRange" ).init( "[2.2,3)" );
					var list  = createObject( "java", "java.util.ArrayList" ).init();
					list.add( "2.2.11" );
					list.add( "2.3.9" );
					list.add( "3.0.0-M5" );
					list.add( "3.0.0-beta-1" );
					local.picked = range.pickHighest( list );
					expect( isNull( local.picked ) ).toBeFalse();
					expect( local.picked.asString() ).toBe( "2.3.9",
						"expected 2.3.9 (highest stable release in [2.2,3)), got #local.picked.asString()#" );
				});

				it( "allows pre-releases when the range bound is itself a pre-release", function() {
					var range = createObject( "java", "lucee.runtime.mvn.MavenVersionRange" ).init( "[3.0.0-M1,3.0.0)" );
					var list  = createObject( "java", "java.util.ArrayList" ).init();
					list.add( "3.0.0-M1" );
					list.add( "3.0.0-M3" );
					list.add( "3.0.0-M5" );
					local.picked = range.pickHighest( list );
					expect( isNull( local.picked ) ).toBeFalse();
					expect( local.picked.asString() ).toBe( "3.0.0-M5" );
				});

				it( "returns null when only pre-releases are available in a release-bounded range", function() {
					var range = createObject( "java", "lucee.runtime.mvn.MavenVersionRange" ).init( "[1.0,2.0)" );
					var list  = createObject( "java", "java.util.ArrayList" ).init();
					list.add( "1.5-M1" );
					list.add( "1.5-beta" );
					list.add( "1.5-SNAPSHOT" );
					expect( isNull( range.pickHighest( list ) ) ).toBeTrue();
				});

				it( "picks a real release when range is pinned and candidate list has extras", function() {
					var range = createObject( "java", "lucee.runtime.mvn.MavenVersionRange" ).init( "[1.0]" );
					var list  = createObject( "java", "java.util.ArrayList" ).init();
					list.add( "1.0" );
					list.add( "1.0-SNAPSHOT" );
					list.add( "1.0.1" );
					local.picked = range.pickHighest( list );
					expect( isNull( local.picked ) ).toBeFalse();
					expect( local.picked.asString() ).toBe( "1.0" );
				});
			});

			// ---------------------------------------------------------------------
			// Integrated resolver path — 6-arg resolveVersionRange against a
			// pre-seeded local mvnDir. Exercises the full parse → local-scan →
			// cache → pickHighest chain without any real HTTP. Catches any
			// mismatch between unit-level behaviour and the end-to-end path.
			// ---------------------------------------------------------------------
			describe( "MavenUtil.resolveVersionRange (6-arg) — end-to-end with seeded mvnDir", function() {

				it( "picks highest locally-installed version in range (no cache, no HTTP)", function() {
					var mvnDir = seedMvnDir();
					// Simulate prior installs: version subdirs exist on disk
					createVersionDir( mvnDir, "org.glassfish.jaxb", "jaxb-runtime", "2.2.11" );
					createVersionDir( mvnDir, "org.glassfish.jaxb", "jaxb-runtime", "2.3.9" );
					createVersionDir( mvnDir, "org.glassfish.jaxb", "jaxb-runtime", "2.3.5" );
					var util = createObject( "java", "lucee.runtime.mvn.MavenUtil" );
					var result = util.resolveVersionRange( mvnDir, "org.glassfish.jaxb", "jaxb-runtime",
						"[2.2,3)", createObject( "java", "java.util.ArrayList" ).init(), javaCast( "null", "" ) );
					expect( result ).toBe( "2.3.9" );
				});

				it( "picks highest in-range from seeded metadata cache when no local version", function() {
					var mvnDir = seedMvnDir();
					seedMetadataCache( mvnDir, "org.glassfish.jaxb", "jaxb-runtime",
						[ "2.2.11", "2.3.5", "2.3.9", "3.0.0-M5" ] );
					var util = createObject( "java", "lucee.runtime.mvn.MavenUtil" );
					var result = util.resolveVersionRange( mvnDir, "org.glassfish.jaxb", "jaxb-runtime",
						"[2.2,3)", createObject( "java", "java.util.ArrayList" ).init(), javaCast( "null", "" ) );
					// 3.0.0-M5 excluded by pre-release filter; 2.3.9 is highest stable in range
					expect( result ).toBe( "2.3.9" );
				});

				it( "throws IOException when range unsatisfiable by seeded metadata", function() {
					var mvnDir = seedMvnDir();
					seedMetadataCache( mvnDir, "org.example", "nothing",
						[ "1.0", "1.1", "1.2" ] );
					var util = createObject( "java", "lucee.runtime.mvn.MavenUtil" );
					expect( function() {
						util.resolveVersionRange( mvnDir, "org.example", "nothing",
							"[5.0,6.0)", createObject( "java", "java.util.ArrayList" ).init(), javaCast( "null", "" ) );
					} ).toThrow();
				});

				it( "local version wins even if metadata cache has newer", function() {
					var mvnDir = seedMvnDir();
					createVersionDir( mvnDir, "org.example", "lib", "2.0.0" );
					seedMetadataCache( mvnDir, "org.example", "lib",
						[ "1.0", "2.0.0", "2.5.0", "2.9.0" ] );
					var util = createObject( "java", "lucee.runtime.mvn.MavenUtil" );
					var result = util.resolveVersionRange( mvnDir, "org.example", "lib",
						"[2.0,3.0)", createObject( "java", "java.util.ArrayList" ).init(), javaCast( "null", "" ) );
					// local-first: 2.0.0 is on disk, so we don't consult the cache
					expect( result ).toBe( "2.0.0" );
				});
			});

			// ---------------------------------------------------------------------
			// End-to-end integration — exercises the full install pipeline through
			// the user-facing mavenLoad() BIF. Only runs transitive range resolution
			// because mavenLoad takes a concrete top-level GAV, but the transitive
			// graph is where the range bug bites in real extensions.
			// ---------------------------------------------------------------------
			describe( "mavenLoad() — end-to-end install with transitive ranges (hits Central)", function() {

				it( "ehcache 3.10.8 loads cleanly (transitively pulls jaxb-runtime:[2.2,3))", function() {
					// Before the fix: walker hit jaxb-runtime:[2.2,3), resolved to literal "2.2",
					// tried to fetch jaxb-runtime-2.2.pom → 404 → missing dep.
					// After: metadata fetch picks an actual 2.x release → resolves cleanly.
					var jars = mavenLoad( {
						"groupId":    "org.ehcache",
						"artifactId": "ehcache",
						"version":    "3.10.8"
					} );
					expect( isArray( jars ) ).toBeTrue();
					expect( arrayLen( jars ) ).toBeGT( 0, "expected ehcache to resolve to at least one jar" );
					// jaxb-runtime should be in the resolved set at a real 2.x version,
					// not at the non-existent bare "2.2"
					var hasRealJaxb = false;
					for ( var p in jars ) {
						if ( findNoCase( "jaxb-runtime-2.", p ) && !findNoCase( "jaxb-runtime-2.2.jar", p ) ) {
							hasRealJaxb = true;
							break;
						}
					}
					expect( hasRealJaxb ).toBeTrue( "expected jaxb-runtime at a real 2.x version, not bare 2.2" );
				});
			});
		});
	}

	// ---------------------------------------------------------------------
	// Helpers for the seeded-mvnDir block. Each call produces a unique tmp
	// root so tests stay isolated. Per test-hygiene convention we clean up
	// *before* each test, not after, leaving artifacts for inspection.
	// ---------------------------------------------------------------------

	private any function seedMvnDir() {
		var path = getTempDirectory() & "/ldev6278-mvn-" & createUUID() & "/";
		directoryCreate( path, true, true );
		return createObject( "java", "lucee.commons.io.res.util.ResourceUtil" )
			.toResourceNotExisting( getPageContext(), path );
	}

	private void function createVersionDir( required any mvnDir, required string groupId, required string artifactId, required string version ) {
		var rel = replace( arguments.groupId, ".", "/", "all" ) & "/" & arguments.artifactId & "/" & arguments.version & "/";
		directoryCreate( arguments.mvnDir.getAbsolutePath() & "/" & rel, true, true );
	}

	private void function seedMetadataCache( required any mvnDir, required string groupId, required string artifactId, required array versions ) {
		var rel = replace( arguments.groupId, ".", "/", "all" ) & "/" & arguments.artifactId & "/";
		var dir = arguments.mvnDir.getAbsolutePath() & "/" & rel;
		directoryCreate( dir, true, true );
		var xml = '<?xml version="1.0" encoding="UTF-8"?>' & chr(10)
			& '<metadata>' & chr(10)
			& '  <groupId>' & arguments.groupId & '</groupId>' & chr(10)
			& '  <artifactId>' & arguments.artifactId & '</artifactId>' & chr(10)
			& '  <versioning>' & chr(10)
			& '    <versions>' & chr(10);
		for ( var v in arguments.versions ) {
			xml &= '      <version>' & v & '</version>' & chr(10);
		}
		xml &= '    </versions>' & chr(10) & '  </versioning>' & chr(10) & '</metadata>' & chr(10);
		fileWrite( dir & "maven-metadata.xml", xml );
		fileWrite( dir & "maven-metadata.xml.lastUpdated", toString( getTickCount() ) );
	}
}
