// registers a request monitor in the server config, the monitor class is only available through the loadPaths of this component
component javaSettings='{ "loadPaths": [ "./LDEV3792/lib/BlockingRequestMonitor.jar" ] }' {

	function register( required string name, required string adminPassword ) {
		admin action="updateMonitor" type="server" password=arguments.adminPassword
			class="lucee.test.ldev3792.BlockingRequestMonitor" monitorType="request" name=arguments.name logEnabled=true;
	}

}
