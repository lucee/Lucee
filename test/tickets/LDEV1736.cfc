component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1736", function() {
			it( title='serialize server scope', body=function( currentSpec ) {
				systemOutput("", true);
				loop collection=server item="local.p" {
					if(left(p,1)=="_") continue;
					systemOutput(local.p, true);
					local.res=serialize( server[local.p] );
				}
				
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