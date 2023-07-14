component extends="org.lucee.cfml.test.LuceeTestCase"  {

	variables.baseDir =  getDirectoryFromPath( getCurrentTemplatePath() );

	function run( testResults, textbox ) {
		describe("testcase for configTranslate()", function() {

			it(title="checking lucee-server.xml via file", body=function( currentSpec ) {

				var src = variables.baseDir & "configTranslate/lucee-server.xml";
				var single = getTempFile(getTempDirectory(), "lucee-server-single", "json");
				var multi = getTempFile(getTempDirectory(), "lucee-server-multi", "json");

				var res = configtranslate( source=src, target=single, type="server", mode="single" );
				expect( res ).toBeStruct();
				expect( res ).notToBeEmpty();

				var out = fileRead( single );
				expect( isJson( out.toJson() ) ).toBeTrue();

				res= configtranslate( source=src, target=multi, type="server", mode="multi" );
				expect( res ).toBeStruct();
				expect( res ).notToBeEmpty();

				var out = fileRead( multi );
				expect( isJson( out.toJson() ) ).toBeTrue();

			});

			it(title="checking lucee-server.xml via string", body=function( currentSpec ) {

				var src = variables.baseDir & "configTranslate/lucee-server.xml";
				var srcString = fileRead( src );

				var single = getTempFile(getTempDirectory(), "lucee-server-single", "json");
				var multi = getTempFile(getTempDirectory(), "lucee-server-multi", "json");

				var res = configtranslate( source=srcString, target=single, type="server", mode="single" );
				expect( res ).toBeStruct();
				expect( res ).notToBeEmpty();

				var out = fileRead( single );
				expect( isJson( out.toJson() ) ).toBeTrue();

				res= configtranslate( source=srcString, target=multi, type="server", mode="multi" );
				expect( res ).toBeStruct();
				expect( res ).notToBeEmpty();

				out = fileRead( multi );
				expect( isJson( out.toJson() ) ).toBeTrue();

			});

			it(title="checking lucee-web.xml.cfm via string", body=function( currentSpec ) {

				var src = variables.baseDir & "configTranslate/lucee-web.xml.cfm";
				var srcString = fileRead( src );

				var singleOut = getTempFile(getTempDirectory(), "lucee-web-single", "json");
				var multiOut = getTempFile(getTempDirectory(), "lucee-web-multi", "json");

				var res = configtranslate( source=src, target=singleOut, type="web", mode="single" );
				expect( res ).toBeStruct();
				expect( res ).notToBeEmpty();

				var out = fileRead( singleOut );
				expect( isJson( out.toJson() ) ).toBeTrue();

				res = configtranslate( source=src, target=multiOut, type="web", mode="multi" );
				expect( res ).toBeStruct();
				expect( res ).notToBeEmpty();

				out = fileRead( multiOut );
				expect( isJson( out.toJson() ) ).toBeTrue();

			});

			it(title="checking lucee-web.xml.cfm file via string", body=function( currentSpec ) {

				var src = variables.baseDir & "configTranslate/lucee-web.xml.cfm";

				var srcString = fileRead( src );
				var singleOut = getTempFile(getTempDirectory(), "lucee-web-single", "json");
				var multiOut = getTempFile(getTempDirectory(), "lucee-web-multi", "json");

				var res = configtranslate( source=srcString, target=singleOut, type="web", mode="single" );
				expect( res ).toBeStruct();
				expect( res ).notToBeEmpty();

				var out = fileRead( singleOut );
				expect( isJson( out.toJson() ) ).toBeTrue();

				res = configtranslate( source=srcString, target=multiOut, type="web", mode="multi" );
				expect( res ).toBeStruct();
				expect( res ).notToBeEmpty();

				out = fileRead( multiOut );
				expect( isJson( out.toJson() ) ).toBeTrue();

			});

		});
	}
}