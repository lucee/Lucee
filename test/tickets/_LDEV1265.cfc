component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1265", function() {
			it( title='Checking file upload content-type header using CFHTTP', body=function( currentSpec ) {
			 	cfhttp(method="POST", url="http://#CGI.SERVER_NAME#/test/testcases/LDEV1265/test1.cfm") {
					cfhttpparam(name="myfile", type="file", file=expandPath('./LDEV1265/myfile.txt') );
				}
				var result = findNocase( "charset=US-ASCII",cfhttp.filecontent);
				assertEquals( true, result EQ 0 );
			});

			it( title='Checking file upload content-type header using CFHTTP', body=function( currentSpec ) {
			 	cfhttp(method="POST", charset="utf-8", url="http://#CGI.SERVER_NAME#/test/testcases/LDEV1265/test1.cfm") {
					cfhttpparam(name="myfile", type="file", file=expandPath('./LDEV1265/myfile.txt') );
				}
				var result = findNocase( "charset=US-ASCII",cfhttp.filecontent);
				assertEquals( true, result EQ 0 );
			});
		});
	}
}