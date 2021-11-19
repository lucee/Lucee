component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1736", function() {
			it( title='serialize server scope', body=function( currentSpec ) {
				// TODO ZAC can you find out why this is no longer working on github (works fine locally), i assume that something get set to the server scope that vcann be serialized 
				//local.res=serialize(server);
				//evaluate(res);
				local.res=serialize(server.os.macAddress);
				evaluate(res);
			});
			it( title='serializeJson server scope', body=function( currentSpec ) {
				//local.res=serializeJson(server);
				//deserializeJson(res);
				local.res=serializeJson(server.os.macAddress);
				deserializeJson(res);
			});
			it( title='ObjectSave server scope', body=function( currentSpec ) {
				//local.res=ObjectSave(server);
				//ObjectLoad(res);
				local.res=ObjectSave(server.os.macAddress);
				ObjectLoad(res);
			});

			it( title='wddx server scope', body=function( currentSpec ) {
				wddx input=server action="cfml2wddx" output="local.res";
				wddx input=res action="wddx2cfml" output="local.res";
				wddx input=server.os.macAddress action="cfml2wddx" output="local.res";
				wddx input=res action="wddx2cfml" output="local.res";
			});
		});
	}

}