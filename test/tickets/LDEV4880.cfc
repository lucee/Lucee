component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.admin = new org.lucee.cfml.Administrator("server",request.ServerAdminPassword );
		variables.compilerSettings = admin.getCompilerSettings();
	}

	function afterAll( ){
		_toggleCompilerSettings();
	}

	private function _toggleCompilerSettings( struct opts={} ){
		var _compilerSettings = duplicate( variables.compilerSettings );
		if ( StructCount( arguments.opts ) )
			 StructAppend( _compilerSettings, arguments.opts, true );
		admin.updateCompilerSettings( argumentCollection = _compilerSettings );
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4880 - getApplicationSettings().dotNotationUpperCase", function() {
			it( title="check dotNotationUpperCase setting exists and is boolean", body=function( currentSpec ) {
				var settings = getApplicationSettings();
				expect( settings ).toHaveKey( "dotNotationUpperCase" );
				expect( settings.dotNotationUpperCase ).toBeBoolean();
			});

			it( title="check dotNotationUpperCase setting can be toggled", body=function( currentSpec ) {
				_toggleCompilerSettings( { dotNotationUpperCase: false } );
				expect( getApplicationSettings().dotNotationUpperCase ).toBeFalse();
				
				_toggleCompilerSettings( { dotNotationUpperCase: true } );
				expect( getApplicationSettings().dotNotationUpperCase ).toBeTrue();
			});
		});
	}

}
