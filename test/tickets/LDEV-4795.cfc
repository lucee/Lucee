component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	public function run( testResults, textbox ) {
		describe( title="Testcase for LDEV-4795", body=function() {
			it( title="checking if we can have parent/parent not existing", body=function( currentSpec ) {
				var ConfigWebUtil=createObject("java","lucee.runtime.config.ConfigWebUtil");
				var ResourceUtil=createObject("java","lucee.commons.io.res.util.ResourceUtil");
				var pc=getPageContext();
				var config=pc.getConfig();
				var path="{lucee-config}/logs/#createUUID()#/#getTickCount()#/sub/mapping.log";

			   expect(isNull(ConfigWebUtil.getFile(config, config.getConfigDir(), path, ResourceUtil.TYPE_FILE))).toBeFalse();
			});


		});
	}
}
