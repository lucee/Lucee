component extends="org.lucee.cfml.test.LuceeTestCase" labels="java" {

	function run( testResults , testBox ) {
		describe( title='QofQ' , body=function(){
			it( title='test createObject' , body=function() {
				expect(function(){
					var nioPath = createObject("java", "java.nio.file.Paths").get( getTempFile( getTempDirectory(), "ldev4859" ), [] );
				}).notToThrow();
			});
		});
	}

} 