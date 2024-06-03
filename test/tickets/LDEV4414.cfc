component extends="org.lucee.cfml.test.LuceeTestCase"  {
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4414", function() {

			it( title="checking ObjectEquals() simple string", body=function( currentSpec ) {
				expect(ObjectEquals(
					"PHONE",
					"PHONE"
				)).toBeTrue();
			});

			it( title="checking ObjectEquals() simple arrays", body=function( currentSpec ) {
				expect(ObjectEquals(
					["PHONE", "EMAIL"],
					["PHONE", "EMAIL"]
				)).toBeTrue();
			});

			it( title="checking ObjectEquals() nested arrays, different",  body=function( currentSpec ) {
				expect(ObjectEquals(
					[["PHONE", "EMAIL"], ["PHONE"], ["PHONE", "EMAIL"]],
					[["PHONE", "EMAIL"], ["PHONE"], ["PHONE"]]
				)).toBeFalse();
			});

			it( title="checking ObjectEquals() nested arrays, same", body=function( currentSpec ) {
				expect(ObjectEquals(
					[["PHONE", "EMAIL"], ["PHONE"], ["PHONE", "EMAIL"]],
					[["PHONE", "EMAIL"], ["PHONE"], ["PHONE", "EMAIL"]]
				)).toBeTrue();
			});

			it( title="checking ObjectEquals() simple struct same", body=function( currentSpec ) {
				expect(ObjectEquals(
					{ id: 1, name: 'Lucee' },
					{ id: 1, name: 'Lucee' }
				)).toBeTrue();
			});

			it( title="checking ObjectEquals() simple struct different", body=function( currentSpec ) {
				expect(ObjectEquals(
					{ id: 1, name: 'Lucee' },
					{ id: 1, name: 'Lucee', type: "language" }
				)).toBeFalse();
			});

			it( title="checking ObjectEquals() nested struct different", body=function( currentSpec ) {
				expect(ObjectEquals(
					{ id: 1, name: { engine: 'Lucee'} },
					{ id: 1, name: { engine: 'ACF'} }
				)).toBeFalse();
			});

			it( title="checking ObjectEquals() nested struct, different arrays", body=function( currentSpec ) {
				expect(ObjectEquals(
					{ id: 1, name: [ 'engine', 'Lucee'] },
					{ id: 1, name: [ 'engine', 'ACF'] }
				)).toBeFalse();
			});
		});
	}
}