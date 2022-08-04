<cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	public void function testImageFormats() localmode="true" {
		var imgf = ImageFormats();
		
		expect( imgf ).toBeStruct();
		expect( imgf ).toHaveKey( "decoder" );
		expect( imgf ).toHaveKey( "encoder" );

		expect( imgf.encoder ).notToBeEmpty();
		expect( imgf.decoder ).notToBeEmpty();
		
		expect( imgf.decoder ).toInclude( "JPG" );
		expect( imgf.encoder ).toInclude( "PNG" );

		// LDEV-4080
		expect( imgf.decoder ).toBeTypeOf( "array" );
		expect( imgf.encoder ).toBeTypeOf( "array" );

		// expect( isObject( imgf.decoder ) ).toBeTrue(); // i.e. should be a cfml array
		// expect( isObject( imgf.encoder ) ).toBeTrue(); // i.e. should be a cfml array
	}
} 
</cfscript>