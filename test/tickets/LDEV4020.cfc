component extends="org.lucee.cfml.test.LuceeTestCase" {

	function afterAll(){
		application action='update' nullSupport=false;
	};

	function run( testResults , testBox ) { 

		describe( title='check null argument handling with full null support' , body=function(){

			beforeEach( function( currentSpec, data ){
				application action='update' nullSupport=true;
			});

			afterEach( function( currentSpec, data ){
				application action='update' nullSupport=false;
			});

			it( title='named arguments', body=function() {
				var named = _proxy( arg1=456 );
				expect( named.arg1 ).toBe( 456 );
				expect( named.arg2 ).toBe( "default 2" );  // was null in 5.4
			});

			it( title='positional arguments', body=function() {
				var positional = _proxy( 456 );
				expect( positional.arg1 ).toBe( 456 );
				expect( positional.arg2 ).toBe( "default 2" );
			});
		});

		describe( title='check null argument handling without full null support' , body=function(){

			beforeEach( function( currentSpec, data ){
				application action='update' nullSupport=false;
			});

			afterEach( function( currentSpec, data ){
				application action='update' nullSupport=false;
			});

			it( title='named arguments', body=function() {
				var named = _proxy( arg1=456 );
				expect( named.arg1 ).toBe( 456 );
				expect( named.arg2 ).toBe( "default 2" );
			});

			it( title='positional arguments', body=function() {
				var positional = _proxy( 456 );
				expect( positional.arg1 ).toBe( 456 );
				expect( positional.arg2 ).toBe( "default 2" );
			});
		});

	}

	private function _proxy( arg1, arg2 ) {
		return _udf( argumentCollection = arguments );
	}
	private function _udf( arg1='default 1', arg2='default 2' ){
		return arguments;
	}
}