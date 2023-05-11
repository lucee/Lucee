component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, textbox ) {

		describe("testcase for LDEV-3804", function(){

			it(title="duplicate shouldn't throw java.lang.NoSuchMethodException", body=function( currentSpec ){

				expect ( function(){
					duplicate( createObject( 'java', 'java.util.Collections' ).synchronizedMap({}) );
				}).notToThrow();
				
			});

		});

	}

}