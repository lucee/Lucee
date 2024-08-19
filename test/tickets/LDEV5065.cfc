component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-5065", function() {

			it( title='assignment regression', body=function( currentSpec ) {
				var brad=5;
				writeDump( var=( brad=4) );
			});

			it( title='assignment regression', body=function( currentSpec ) {
				var brad=5;
				writeDump( ( brad+=4) );
			});

			it( title='assignment regression', body=function( currentSpec ) {
				var brad=5;
				writeDump( var=( brad+=4) );
			});

			it( title='assignment regression', body=function( currentSpec ) {
				var brad=5;
				writeDump( var=( brad=4) );
				writeDump( ( brad+=4) );
				writeDump( var=( brad+=4) );
			});
		});
	}

} 
