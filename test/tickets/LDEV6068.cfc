component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		include template="/admin/services.update.functions.cfm";
	}

	function run( testResults, testBox ) {
		describe( title="isStableVersion", body=function() {

			it( "should return true for stable versions", function() {
				expect( isStableVersion( "6.2.5.22" ) ).toBeTrue();
				expect( isStableVersion( "7.0.0.395" ) ).toBeTrue();
			});

			it( "should return false for SNAPSHOT versions", function() {
				expect( isStableVersion( "6.2.5.22-SNAPSHOT" ) ).toBeFalse();
			});

			it( "should return false for ALPHA versions", function() {
				expect( isStableVersion( "7.1.0.30-ALPHA" ) ).toBeFalse();
			});

			it( "should return false for BETA versions", function() {
				expect( isStableVersion( "6.1.0.100-BETA" ) ).toBeFalse();
			});

			it( "should return false for RC versions", function() {
				expect( isStableVersion( "6.0.0.503-RC" ) ).toBeFalse();
			});

		});

		describe( title="compareVersions", body=function() {

			it( "should return -1 when v1 is older", function() {
				expect( compareVersions( "6.2.5.22", "6.2.5.23" ) ).toBe( -1 );
			});

			it( "should return 1 when v1 is newer", function() {
				expect( compareVersions( "6.2.5.23", "6.2.5.22" ) ).toBe( 1 );
			});

			it( "should return 0 when versions are equal", function() {
				expect( compareVersions( "6.2.5.22", "6.2.5.22" ) ).toBe( 0 );
			});

			it( "should compare numerically not lexically", function() {
				expect( compareVersions( "6.2.5.9", "6.2.5.10" ) ).toBe( -1 );
				expect( compareVersions( "6.2.5.99", "6.2.5.100" ) ).toBe( -1 );
			});

			it( "should handle different segment counts", function() {
				expect( compareVersions( "6.2.5", "6.2.5.0" ) ).toBe( 0 );
				expect( compareVersions( "6.2.5", "6.2.5.1" ) ).toBe( -1 );
			});

			it( "should rank stable higher than SNAPSHOT of same base", function() {
				expect( compareVersions( "6.2.5.22", "6.2.5.22-SNAPSHOT" ) ).toBe( 1 );
				expect( compareVersions( "6.2.5.22-SNAPSHOT", "6.2.5.22" ) ).toBe( -1 );
			});

			it( "should handle SNAPSHOT vs SNAPSHOT", function() {
				expect( compareVersions( "6.2.5.22-SNAPSHOT", "6.2.5.23-SNAPSHOT" ) ).toBe( -1 );
				expect( compareVersions( "6.2.5.22-SNAPSHOT", "6.2.5.22-SNAPSHOT" ) ).toBe( 0 );
			});

			it( "should rank suffixes: stable > RC > BETA > SNAPSHOT > ALPHA", function() {
				// ALPHA < SNAPSHOT < BETA < RC < stable
				expect( compareVersions( "6.2.5.22-ALPHA", "6.2.5.22-SNAPSHOT" ) ).toBe( -1 );
				expect( compareVersions( "6.2.5.22-SNAPSHOT", "6.2.5.22-BETA" ) ).toBe( -1 );
				expect( compareVersions( "6.2.5.22-BETA", "6.2.5.22-RC" ) ).toBe( -1 );
				expect( compareVersions( "6.2.5.22-RC", "6.2.5.22" ) ).toBe( -1 );
			});

			it( "should detect BETA as upgrade from SNAPSHOT", function() {
				expect( compareVersions( "6.2.5.22-SNAPSHOT", "6.2.5.22-BETA" ) ).toBe( -1 );
			});

			it( "should detect stable as upgrade from SNAPSHOT", function() {
				expect( compareVersions( "6.2.5.22-SNAPSHOT", "6.2.5.22" ) ).toBe( -1 );
			});

			it( "should NOT detect ALPHA as upgrade from SNAPSHOT", function() {
				expect( compareVersions( "6.2.5.22-SNAPSHOT", "6.2.5.22-ALPHA" ) ).toBe( 1 );
			});

		});

		describe( title="getUpdateForMajorVersion", body=function() {

			it( "should find latest version for major version", function() {
				var versions = [ "6.2.5.20-SNAPSHOT", "6.2.5.21-SNAPSHOT", "7.0.0.1-SNAPSHOT" ];
				expect( getUpdateForMajorVersion( versions, 6 ) ).toBe( "6.2.5.21-SNAPSHOT" );
			});

			it( "should return empty string when no matching major version", function() {
				var versions = [ "7.0.0.1-SNAPSHOT", "7.0.0.2-SNAPSHOT" ];
				expect( getUpdateForMajorVersion( versions, 6 ) ).toBe( "" );
			});

			it( "should handle empty array", function() {
				expect( getUpdateForMajorVersion( [], 6 ) ).toBe( "" );
			});

		});

		describe( title="hasNewerVersion", body=function() {

			it( "should detect newer stable version", function() {
				var result = hasNewerVersion(
					"6.2.5.22",
					{ available: "6.2.5.23", otherVersions: [ "6.2.5.22", "6.2.5.23" ] }
				);
				expect( result.hasUpdate ).toBeTrue();
				expect( result.availableVersion ).toBe( "6.2.5.23" );
			});

			it( "should NOT detect older version as update", function() {
				var result = hasNewerVersion(
					"6.2.5.23-SNAPSHOT",
					{ available: "6.2.5.22-SNAPSHOT", otherVersions: [ "6.2.5.21-SNAPSHOT", "6.2.5.22-SNAPSHOT" ] }
				);
				expect( result.hasUpdate ).toBeFalse();
			});

			it( "should handle same version", function() {
				var result = hasNewerVersion(
					"6.2.5.22-SNAPSHOT",
					{ available: "6.2.5.22-SNAPSHOT", otherVersions: [ "6.2.5.22-SNAPSHOT" ] }
				);
				expect( result.hasUpdate ).toBeFalse();
			});

			it( "should compare version numbers numerically not lexically", function() {
				var result = hasNewerVersion(
					"6.2.5.9",
					{ available: "6.2.5.10", otherVersions: [ "6.2.5.10" ] }
				);
				expect( result.hasUpdate ).toBeTrue();
			});

			it( "should handle SNAPSHOT versions", function() {
				var result = hasNewerVersion(
					"6.2.5.22-SNAPSHOT",
					{ available: "6.2.5.23-SNAPSHOT", otherVersions: [ "6.2.5.23-SNAPSHOT" ] }
				);
				expect( result.hasUpdate ).toBeTrue();
				expect( result.availableVersion ).toBe( "6.2.5.23-SNAPSHOT" );
			});

			it( "should filter to same major version", function() {
				var result = hasNewerVersion(
					"6.2.5.22-SNAPSHOT",
					{ available: "7.0.0.1-SNAPSHOT", otherVersions: [ "6.2.5.23-SNAPSHOT", "7.0.0.1-SNAPSHOT" ] }
				);
				expect( result.hasUpdate ).toBeTrue();
				expect( result.availableVersion ).toBe( "6.2.5.23-SNAPSHOT" );
			});

			it( "stable versions should only show stable updates", function() {
				var result = hasNewerVersion(
					"6.2.5.22",
					{ available: "6.2.5.23-SNAPSHOT", otherVersions: [ "6.2.5.22", "6.2.5.23-SNAPSHOT" ] }
				);
				expect( result.hasUpdate ).toBeFalse();
			});

			it( "should handle missing available key", function() {
				var result = hasNewerVersion( "6.2.5.22", {} );
				expect( result.hasUpdate ).toBeFalse();
				expect( result.availableVersion ).toBe( "" );
			});

			it( "should handle empty otherVersions array", function() {
				var result = hasNewerVersion(
					"6.2.5.22",
					{ available: "6.2.5.23", otherVersions: [] }
				);
				expect( result.hasUpdate ).toBeFalse();
			});

			it( "stable users should see RC updates", function() {
				// stable users see stable AND RC (RC is close to release)
				var result = hasNewerVersion(
					"6.2.5.22",
					{ available: "6.2.5.23-SNAPSHOT", otherVersions: [ "6.2.5.23-SNAPSHOT", "6.2.5.24-RC" ] }
				);
				expect( result.hasUpdate ).toBeTrue();
				expect( result.availableVersion ).toBe( "6.2.5.24-RC" );
			});

			it( "stable users should NOT see SNAPSHOT or BETA updates", function() {
				// stable users don't see SNAPSHOT, ALPHA, or BETA
				var result = hasNewerVersion(
					"6.2.5.22",
					{ available: "6.2.5.23-SNAPSHOT", otherVersions: [ "6.2.5.23-SNAPSHOT", "6.2.5.24-BETA" ] }
				);
				expect( result.hasUpdate ).toBeFalse();
			});

			it( "should NOT show ALPHA as upgrade from SNAPSHOT (LDEV-6067)", function() {
				// 7.0.2.46-SNAPSHOT -> 7.1.0.30-ALPHA should NOT be an upgrade
				// ALPHA is lower stability than SNAPSHOT
				var result = hasNewerVersion(
					"7.0.2.46-SNAPSHOT",
					{ available: "7.1.0.30-ALPHA", otherVersions: [ "7.0.2.46-SNAPSHOT", "7.1.0.30-ALPHA" ] }
				);
				expect( result.hasUpdate ).toBeFalse();
			});

			it( "should show newer SNAPSHOT as upgrade from older SNAPSHOT", function() {
				var result = hasNewerVersion(
					"7.0.2.46-SNAPSHOT",
					{ available: "7.0.2.47-SNAPSHOT", otherVersions: [ "7.0.2.46-SNAPSHOT", "7.0.2.47-SNAPSHOT" ] }
				);
				expect( result.hasUpdate ).toBeTrue();
				expect( result.availableVersion ).toBe( "7.0.2.47-SNAPSHOT" );
			});

			it( "should show BETA as upgrade from SNAPSHOT", function() {
				// BETA is higher stability than SNAPSHOT
				var result = hasNewerVersion(
					"6.2.5.22-SNAPSHOT",
					{ available: "6.2.5.22-BETA", otherVersions: [ "6.2.5.22-SNAPSHOT", "6.2.5.22-BETA" ] }
				);
				expect( result.hasUpdate ).toBeTrue();
				expect( result.availableVersion ).toBe( "6.2.5.22-BETA" );
			});

			it( "should show RC as upgrade from SNAPSHOT", function() {
				// RC is higher stability than SNAPSHOT
				var result = hasNewerVersion(
					"6.2.5.22-SNAPSHOT",
					{ available: "6.2.5.22-RC", otherVersions: [ "6.2.5.22-SNAPSHOT", "6.2.5.22-RC" ] }
				);
				expect( result.hasUpdate ).toBeTrue();
				expect( result.availableVersion ).toBe( "6.2.5.22-RC" );
			});

			it( "should show stable as upgrade from SNAPSHOT", function() {
				// stable is higher stability than SNAPSHOT
				var result = hasNewerVersion(
					"6.2.5.22-SNAPSHOT",
					{ available: "6.2.5.22", otherVersions: [ "6.2.5.22-SNAPSHOT", "6.2.5.22" ] }
				);
				expect( result.hasUpdate ).toBeTrue();
				expect( result.availableVersion ).toBe( "6.2.5.22" );
			});

			it( "BETA users should see newer SNAPSHOT within same minor version", function() {
				// BETA/RC users see SNAPSHOT only within same minor version (bug fixes)
				var result = hasNewerVersion(
					"6.2.5.14-BETA",
					{ available: "6.2.5.18-SNAPSHOT", otherVersions: [ "6.2.5.14-BETA", "6.2.5.18-SNAPSHOT" ] }
				);
				expect( result.hasUpdate ).toBeTrue();
				expect( result.availableVersion ).toBe( "6.2.5.18-SNAPSHOT" );
			});

			it( "BETA users should NOT see SNAPSHOT from different minor version", function() {
				// BETA/RC users don't see SNAPSHOT from different minor version
				var result = hasNewerVersion(
					"6.2.5.14-BETA",
					{ available: "6.2.6.1-SNAPSHOT", otherVersions: [ "6.2.5.14-BETA", "6.2.6.1-SNAPSHOT" ] }
				);
				expect( result.hasUpdate ).toBeFalse();
			});

			it( "BETA users should see newer BETA from different minor version", function() {
				// BETA/RC users can see BETA+ from any minor version
				var result = hasNewerVersion(
					"6.2.5.14-BETA",
					{ available: "6.2.6.1-BETA", otherVersions: [ "6.2.5.14-BETA", "6.2.6.1-BETA" ] }
				);
				expect( result.hasUpdate ).toBeTrue();
				expect( result.availableVersion ).toBe( "6.2.6.1-BETA" );
			});

			it( "RC users should see newer SNAPSHOT within same minor version", function() {
				// BETA/RC users see SNAPSHOT only within same minor version
				var result = hasNewerVersion(
					"6.2.5.14-RC",
					{ available: "6.2.5.18-SNAPSHOT", otherVersions: [ "6.2.5.14-RC", "6.2.5.18-SNAPSHOT" ] }
				);
				expect( result.hasUpdate ).toBeTrue();
				expect( result.availableVersion ).toBe( "6.2.5.18-SNAPSHOT" );
			});

			it( "RC users should NOT see ALPHA updates", function() {
				// BETA/RC users never see ALPHA
				var result = hasNewerVersion(
					"6.2.6.14-RC",
					{ available: "6.2.6.15-ALPHA", otherVersions: [ "6.2.6.14-RC", "6.2.6.15-ALPHA" ] }
				);
				expect( result.hasUpdate ).toBeFalse();
			});

			it( "ALPHA users should see newer ALPHA updates", function() {
				// only ALPHA users see ALPHA
				var result = hasNewerVersion(
					"6.2.6.14-ALPHA",
					{ available: "6.2.6.15-ALPHA", otherVersions: [ "6.2.6.14-ALPHA", "6.2.6.15-ALPHA" ] }
				);
				expect( result.hasUpdate ).toBeTrue();
				expect( result.availableVersion ).toBe( "6.2.6.15-ALPHA" );
			});

		});

		describe( title="categorizeVersions", body=function() {

			it( "should categorize SNAPSHOT versions", function() {
				var versions = [ "6.2.5.20-SNAPSHOT", "6.2.5.22-SNAPSHOT", "6.2.5.23-SNAPSHOT" ];
				var result = categorizeVersions( versions, "6.2.5.21-SNAPSHOT", true );
				expect( result.snapShot.downgrade ).toInclude( "6.2.5.20-SNAPSHOT" );
				expect( result.snapShot.upgrade ).toInclude( "6.2.5.22-SNAPSHOT" );
				expect( result.snapShot.upgrade ).toInclude( "6.2.5.23-SNAPSHOT" );
			});

			it( "should categorize release versions", function() {
				var versions = [ "6.2.5.20", "6.2.5.22", "6.2.5.23" ];
				var result = categorizeVersions( versions, "6.2.5.21", true );
				expect( result.release.downgrade ).toInclude( "6.2.5.20" );
				expect( result.release.upgrade ).toInclude( "6.2.5.22" );
				expect( result.release.upgrade ).toInclude( "6.2.5.23" );
			});

			it( "should categorize pre-release versions (BETA, RC)", function() {
				var versions = [ "6.2.5.22-BETA", "6.2.5.23-RC" ];
				var result = categorizeVersions( versions, "6.2.5.21-BETA", true );
				expect( result.pre_Release.upgrade ).toInclude( "6.2.5.22-BETA" );
				expect( result.pre_Release.upgrade ).toInclude( "6.2.5.23-RC" );
			});

			it( "should categorize ALPHA versions separately", function() {
				var versions = [ "6.2.5.20-ALPHA", "6.2.5.22-ALPHA" ];
				var result = categorizeVersions( versions, "6.2.5.21-ALPHA", true );
				expect( result.alpha.downgrade ).toInclude( "6.2.5.20-ALPHA" );
				expect( result.alpha.upgrade ).toInclude( "6.2.5.22-ALPHA" );
			});

			it( "should exclude current version", function() {
				var versions = [ "6.2.5.21-SNAPSHOT", "6.2.5.22-SNAPSHOT" ];
				var result = categorizeVersions( versions, "6.2.5.21-SNAPSHOT", true );
				expect( result.snapShot.downgrade ).notToInclude( "6.2.5.21-SNAPSHOT" );
				expect( result.snapShot.upgrade ).notToInclude( "6.2.5.21-SNAPSHOT" );
			});

			it( "should exclude Lucee 7+ when hasLoader7 is false", function() {
				var versions = [ "6.2.5.22-SNAPSHOT", "7.0.0.1-SNAPSHOT" ];
				var result = categorizeVersions( versions, "6.2.5.21-SNAPSHOT", false );
				expect( result.snapShot.upgrade ).toInclude( "6.2.5.22-SNAPSHOT" );
				expect( result.snapShot.upgrade ).notToInclude( "7.0.0.1-SNAPSHOT" );
			});

			it( "should include Lucee 7+ when hasLoader7 is true", function() {
				var versions = [ "6.2.5.22-SNAPSHOT", "7.0.0.1-SNAPSHOT" ];
				var result = categorizeVersions( versions, "6.2.5.21-SNAPSHOT", true );
				expect( result.snapShot.upgrade ).toInclude( "6.2.5.22-SNAPSHOT" );
				expect( result.snapShot.upgrade ).toInclude( "7.0.0.1-SNAPSHOT" );
			});

			it( "should handle empty versions array", function() {
				var result = categorizeVersions( [], "6.2.5.21-SNAPSHOT", true );
				expect( arrayLen( result.snapShot.upgrade ) ).toBe( 0 );
				expect( arrayLen( result.snapShot.downgrade ) ).toBe( 0 );
				expect( arrayLen( result.release.upgrade ) ).toBe( 0 );
				expect( arrayLen( result.release.downgrade ) ).toBe( 0 );
			});

			it( "should categorize mixed version types", function() {
				var versions = [ "6.2.5.20-SNAPSHOT", "6.2.5.21", "6.2.5.22-RC" ];
				var result = categorizeVersions( versions, "6.2.5.19-SNAPSHOT", true );
				expect( result.snapShot.upgrade ).toInclude( "6.2.5.20-SNAPSHOT" );
				expect( result.release.upgrade ).toInclude( "6.2.5.21" );
				expect( result.pre_Release.upgrade ).toInclude( "6.2.5.22-RC" );
			});

		});
	}

}
