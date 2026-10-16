component extends="org.lucee.cfml.test.LuceeTestCase" labels="config" {

	function run( testResults, testBox ) {
		describe( "LDEV-6248: ConfigUtil.getFile NPE on invalid paths", function() {

			it( "should not NPE when path has no parent (root path)", function() {
				var ConfigUtil = createObject( "java", "lucee.runtime.config.ConfigUtil" );
				var ResourceUtil = createObject( "java", "lucee.commons.io.res.util.ResourceUtil" );
				var config = getPageContext().getConfig();
				var configDir = config.getConfigDir();

				var result = ConfigUtil.getFile( config, configDir, "/", ResourceUtil.TYPE_FILE );
				// null is fine, NPE is not
			});

			it( "should not NPE when path is a bare filename (no directory)", function() {
				var ConfigUtil = createObject( "java", "lucee.runtime.config.ConfigUtil" );
				var ResourceUtil = createObject( "java", "lucee.commons.io.res.util.ResourceUtil" );
				var config = getPageContext().getConfig();
				var configDir = config.getConfigDir();

				var result = ConfigUtil.getFile( config, configDir, "test.log", ResourceUtil.TYPE_FILE );
			});

			it( "should not NPE when path is a URL-encoded placeholder", function() {
				var ConfigUtil = createObject( "java", "lucee.runtime.config.ConfigUtil" );
				var ResourceUtil = createObject( "java", "lucee.commons.io.res.util.ResourceUtil" );
				var config = getPageContext().getConfig();
				var configDir = config.getConfigDir();

				// this is the actual path from the reporter's .cfconfig.json
				var result = ConfigUtil.getFile( config, configDir, "%7Blucee%2Dconfig%7D%2Flogs%2Fdatasource%2Elog", ResourceUtil.TYPE_FILE );
			});

			it( "should resolve a valid placeholder path without error", function() {
				var ConfigUtil = createObject( "java", "lucee.runtime.config.ConfigUtil" );
				var ResourceUtil = createObject( "java", "lucee.commons.io.res.util.ResourceUtil" );
				var config = getPageContext().getConfig();
				var configDir = config.getConfigDir();

				var result = ConfigUtil.getFile( config, configDir, "{lucee-config}/logs/datasource.log", ResourceUtil.TYPE_FILE );
				expect( result ).notToBeNull();
			});

		});
	}
}
