component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "test the config", function() {
			it(title="test the config by touching all getters", body=function() {
				var cs=lucee.runtime.config.ConfigUtil::getConfigServerImpl(getPageContext().getConfig());
				cs.touchAll(nullValue());
			});
		});
	}
}
