component extends="org.lucee.cfml.test.LuceeTestCase" labels="smtp" skip=true {
	function beforeAll(){
		variables.uri = createURI("LDEV3687");
		executeSpoolerTask();
		variables.countSpoolerTasks = 0;
	}
	function run( testResults , testBox ) {
		describe( "test case for LDEV-3687", function() {
			it(title = "Checking 'cc' of mail with trailing spaces",  skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/LDEV3687.cfm",
					forms:"Scene=1"
				);
				expect(local.result.filecontent.trim()).toBe('success');
				variables.countSpoolerTasks += executeSpoolerTask();
			});
			
			it(title = "Checking 'from' of mail using display name with comma",  skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/LDEV3687.cfm",
					forms:{Scene=2}
				);
				expect(local.result.filecontent.trim()).toBe('success');
				variables.countSpoolerTasks += executeSpoolerTask();
			});
			
			it(title = "Checking 'to' of mail with trailing spaces",  skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/LDEV3687.cfm",
					forms:{Scene=3}
				);
				expect(local.result.filecontent.trim()).toBe('success');
				variables.countSpoolerTasks += executeSpoolerTask();
			});

			it(title = "Checking 'bcc' of mail with trailing spaces",  skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/LDEV3687.cfm",
					forms:{Scene=4}
				);
				expect(local.result.filecontent.trim()).toBe('success');
				variables.countSpoolerTasks += executeSpoolerTask();
			});

			it(title = "Checking to and from",  skip=isAvailable(), body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/LDEV3687.cfm",
					forms:{Scene=5}
				);
				expect(local.result.filecontent.trim()).toBe('success');
				variables.countSpoolerTasks += executeSpoolerTask();
			});

			it(title = "Checking mails where spooled",  skip=isAvailable(), body = function( currentSpec ) {
				// this might conflict with the background controller thread? so one is enough
				if (countSpoolerTasks neq 5)
					systemOutput("WARNING! LDEV-3867 only [#countSpoolerTasks#] mails found to spool, expected 5, maybe controller spooled them", true);
				expect( countSpoolerTasks ).toBeGT(0, "Error, expected at least one mail spooler task to be found, check remote-client.log, [#countSpoolerTasks#] found, 5 sent");
			});

		});
	}

	private function executeSpoolerTask(boolean throwWhenEmpty=false){
		admin
			action="getSpoolerTasks"
			type="server"
			password="#server.SERVERADMINPASSWORD#"
			returnVariable="local.spoolerTasks";
		for (var task in spoolerTasks){
			admin 
				action="executeSpoolerTask" 
				type="server" 
				password=server.SERVERADMINPASSWORD 
				id="#task.id#";
		}
		return len(spoolerTasks);
	}

	private boolean function isAvailable(){
		return isEmpty( server.getTestService( "smtp" ) );
	}
	
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
