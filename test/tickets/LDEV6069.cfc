component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		include template="/admin/ext.functions.cfm";
	}

	function run( testResults, testBox ) {

		describe( title="LDEV-6069: toVersionSortable padding consistency", body=function() {

			describe( title="toOSGiVersion sortable format", body=function() {

				it( "should return struct with sortable key for valid OSGi version", function() {
					var result = toOSGiVersion( "6.0.7728.100" );
					expect( result ).toBeStruct();
					expect( result ).toHaveKey( "sortable" );
				});

				it( "should parse major.minor.micro.qualifier correctly", function() {
					var result = toOSGiVersion( "6.0.7728.100" );
					expect( result.major ).toBe( 6 );
					expect( result.minor ).toBe( 0 );
					expect( result.micro ).toBe( 7728 );
					expect( result.qualifier ).toBe( 100 );
				});

				it( "should return empty struct for non-OSGi versions", function() {
					var result = toOSGiVersion( "13.2.1" );
					expect( structCount( result ) ).toBe( 0 );
				});

			});

			describe( title="toVersionSortable consistency between OSGi and non-OSGi", body=function() {

				it( "BUG REPRO: should correctly compare 13.2.1 vs 6.0.7728.100 (MSSQL driver)", function() {
					// This is the actual bug - MSSQL driver 13.2.1 shown as having update to 6.0.7728.100
					var installed = toVersionSortable( "13.2.1" );
					var available = toVersionSortable( "6.0.7728.100" );

					// 13.x should be GREATER than 6.x - this is what's failing
					expect( installed > available ).toBeTrue( "13.2.1 (#installed#) should be > 6.0.7728.100 (#available#)" );
				});

				it( "should compare major versions correctly across formats", function() {
					// Non-OSGi (3 parts) vs OSGi (4 parts)
					var v13 = toVersionSortable( "13.2.1" );
					var v6 = toVersionSortable( "6.0.7728.100" );
					var v7 = toVersionSortable( "7.0.0.1" );
					var v14 = toVersionSortable( "14.0.0.1" );

					expect( v13 > v6 ).toBeTrue( "13.2.1 should be > 6.0.7728.100" );
					expect( v13 > v7 ).toBeTrue( "13.2.1 should be > 7.0.0.1" );
					expect( v14 > v13 ).toBeTrue( "14.0.0.1 should be > 13.2.1" );
				});

			});

			describe( title="toVersionSortable edge cases", body=function() {

				it( "should handle single digit major versions", function() {
					var v1 = toVersionSortable( "1.0.0.1" );
					var v2 = toVersionSortable( "2.0.0.1" );
					var v9 = toVersionSortable( "9.0.0.1" );
					var v10 = toVersionSortable( "10.0.0.1" );

					expect( v2 > v1 ).toBeTrue();
					expect( v10 > v9 ).toBeTrue();
					expect( v10 > v1 ).toBeTrue();
				});

				it( "should handle large version numbers", function() {
					var small = toVersionSortable( "1.0.0.1" );
					var large = toVersionSortable( "99.999.9999.9999" );

					expect( large > small ).toBeTrue();
				});

				it( "should handle versions with SNAPSHOT suffix", function() {
					var stable = toVersionSortable( "6.2.5.22" );
					var snapshot = toVersionSortable( "6.2.5.22-SNAPSHOT" );

					// stable should be > snapshot for same base version
					expect( stable > snapshot ).toBeTrue( "6.2.5.22 should be > 6.2.5.22-SNAPSHOT" );
				});

				it( "should handle versions with BETA suffix", function() {
					var stable = toVersionSortable( "6.2.5.22" );
					var beta = toVersionSortable( "6.2.5.22-BETA" );

					expect( stable > beta ).toBeTrue( "6.2.5.22 should be > 6.2.5.22-BETA" );
				});

				it( "should handle 3-part versions (non-OSGi)", function() {
					var v1 = toVersionSortable( "1.2.3" );
					var v2 = toVersionSortable( "1.2.4" );
					var v3 = toVersionSortable( "2.0.0" );

					expect( v2 > v1 ).toBeTrue();
					expect( v3 > v2 ).toBeTrue();
				});

				it( "should handle 4-part versions (OSGi)", function() {
					var v1 = toVersionSortable( "1.2.3.4" );
					var v2 = toVersionSortable( "1.2.3.5" );
					var v3 = toVersionSortable( "1.2.4.1" );

					expect( v2 > v1 ).toBeTrue();
					expect( v3 > v2 ).toBeTrue();
				});

				it( "should compare 3-part and 4-part versions correctly", function() {
					var threePartOlder = toVersionSortable( "5.0.0" );
					var fourPartNewer = toVersionSortable( "6.0.0.1" );
					var threePartNewer = toVersionSortable( "7.0.0" );

					expect( fourPartNewer > threePartOlder ).toBeTrue( "6.0.0.1 should be > 5.0.0" );
					expect( threePartNewer > fourPartNewer ).toBeTrue( "7.0.0 should be > 6.0.0.1" );
				});

			});

			describe( title="Real world extension version scenarios", body=function() {

				it( "should not show downgrade as upgrade for MSSQL driver", function() {
					// Microsoft changed their versioning scheme
					// Old: 13.x.x (e.g. 13.2.1)
					// New: 6.x.x, 7.x.x, etc (e.g. 6.0.7728.100)
					var installed = toVersionSortable( "13.2.1" );
					var wrongUpdate = toVersionSortable( "6.0.7728.100" );

					expect( wrongUpdate > installed ).toBeFalse( "6.0.7728.100 should NOT be > 13.2.1" );
				});

				it( "should correctly identify actual upgrades", function() {
					// Same versioning scheme
					var old = toVersionSortable( "6.0.7728.100" );
					var newer = toVersionSortable( "6.0.7728.200" );
					var newest = toVersionSortable( "7.0.0.1" );

					expect( newer > old ).toBeTrue();
					expect( newest > newer ).toBeTrue();
				});

				it( "should handle Lucee extension versions", function() {
					var v1 = toVersionSortable( "2.0.0.54" );
					var v2 = toVersionSortable( "2.0.0.55" );
					var v3 = toVersionSortable( "2.1.0.1" );

					expect( v2 > v1 ).toBeTrue();
					expect( v3 > v2 ).toBeTrue();
				});

				it( "should handle extension versions with qualifiers", function() {
					var snapshot = toVersionSortable( "2.0.0.54-SNAPSHOT" );
					var beta = toVersionSortable( "2.0.0.54-BETA" );
					var stable = toVersionSortable( "2.0.0.54" );

					expect( beta > snapshot ).toBeTrue( "BETA should be > SNAPSHOT" );
					expect( stable > beta ).toBeTrue( "stable should be > BETA" );
				});

			});

			describe( title="Sortable string format validation", body=function() {

				it( "OSGi and non-OSGi should produce comparable sortable strings", function() {
					// Both formats should use same padding so string comparison works
					var osgi = toVersionSortable( "6.0.0.1" );
					var nonOsgi = toVersionSortable( "13.2.1" );

					// Check they have the same structure (dots separating padded numbers)
					expect( listLen( osgi, "." ) >= 4 ).toBeTrue();
					expect( listLen( nonOsgi, "." ) >= 4 ).toBeTrue();

					// First segment should be zero-padded to same length
					var osgiMajor = listFirst( osgi, "." );
					var nonOsgiMajor = listFirst( nonOsgi, "." );
					expect( len( osgiMajor ) ).toBe( len( nonOsgiMajor ),
						"Major version padding should match: OSGi=#osgiMajor# vs non-OSGi=#nonOsgiMajor#" );
				});

			});

		});

	}

}
