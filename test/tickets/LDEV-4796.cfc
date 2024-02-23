component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	public function run( testResults, textbox ) {
		describe( title="Testcase for LDEV-4796", body=function() {
			it( title="checking configImport sessionstorage", body=function( currentSpec ) {
				
				configImport(path:{
					"caches": {
						"ldev4796": {
						  "class": "lucee.runtime.cache.ram.RamCache",
						  "custom": "timeToIdleSeconds=4&timeToLiveSeconds=5",
						  "readOnly": "false",
						  "storage": "true"
						}
					  },
					  "sessionstorage": "ldev4796"
			
				}, type:"server", password:request.SERVERADMINPASSWORD);
			


			   expect(isNull(ConfigWebUtil.getFile(config, config.getConfigDir(), path, ResourceUtil.TYPE_FILE))).toBeFalse();
			});


		});
	}
}
