component extends="org.lucee.cfml.test.LuceeTestCase" labels="metadata" {

	function exampleFunctionWithDescriptionInAnnotation() description="Description shows up." {}

	/**
	 * @description Description does not show up.
	 */
	function exampleFunctionWithDescriptionInDocblock() {}

	/**
	 * This is the hint
	 * @description Description in a block with a hint
	 */
	function exampleFunctionWithHintAndDescription() {}

	/**
	 * @description Description in the docblock
	 */
	function exampleFunctionWithBoth() description="Description in the attribute" {}

	/**
	 * Just a hint
	 */
	function exampleFunctionWithoutDescription() {}

	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-3899", function() {

			it( title="description attribute shows up in the function metadata", body=function( currentSpec ) {
				expect( getMetadata( exampleFunctionWithDescriptionInAnnotation ).description ).toBe( "Description shows up." );
			});

			it( title="@description in a docblock shows up in the function metadata", body=function( currentSpec ) {
				expect( getMetadata( exampleFunctionWithDescriptionInDocblock ).description ).toBe( "Description does not show up." );
			});

			it( title="@description in a docblock with a hint", body=function( currentSpec ) {
				var md = getMetadata( exampleFunctionWithHintAndDescription );
				expect( md.description ).toBe( "Description in a block with a hint" );
				expect( md.hint ).toBe( "This is the hint" );
			});

			it( title="the description attribute wins over @description", body=function( currentSpec ) {
				expect( getMetadata( exampleFunctionWithBoth ).description ).toBe( "Description in the attribute" );
			});

			it( title="no description stays empty", body=function( currentSpec ) {
				var md = getMetadata( exampleFunctionWithoutDescription );
				expect( md.description ).toBe( "" );
				expect( md.hint ).toBe( "Just a hint" );
			});

			it( title="@description is found in the component metadata too", body=function( currentSpec ) {
				var funcs = getComponentMetadata( this ).functions;
				var found = false;
				loop array=funcs item="local.f" {
					if ( f.name == "exampleFunctionWithDescriptionInDocblock" ) {
						found = true;
						expect( f.description ).toBe( "Description does not show up." );
					}
				}
				expect( found ).toBeTrue();
			});
		});
	}
}
