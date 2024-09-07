component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( title='LDEV-5085', body=function(){

			it( title='test preside anti samy service (full path)', body=function() {
				var path = getDirectoryFromPath( getCurrentTemplatePath() );
				var antisamy = new LDEV5085.AntiSamyService( path & "LDEV5085\antisamylib" );
				var str = "<div onclick='xss()'>xss</div>";
				var result = antisamy.clean( str );
				expect( result ).toBe( "<div>xss</div>" );
			});

		});
	}

}