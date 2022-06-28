component extends="org.lucee.cfml.test.LuceeTestCase" labels="mysql" {
	// skip closure
	function isNotSupported() {
		var mySql = getCredentials();
		if(!isNull(mysql.server)){
			return false;
		} else{
			return true;
		}
	}

	function beforeAll() skip="isNotSupported"{
		if(isNotSupported()) return;
		defineDatasource();
		createTable();
	}

	function afterAll() {
		if(isNotSupported()) return;
		query {
	        echo("DROP TABLE `test`");
		}
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1292", body=function() {
			it(title="checking this.datasource, dataBase name ends with '?' ", skip=isNotSupported(), body = function( currentSpec ) {
				var uri = createURI("LDEV1292")
				var result = _InternalRequest(
					template:"#uri#/test1/test.cfm"
				);
				expect(result.filecontent.trim()).toBe('1');
			});

			it(title="checking this.datasource, with dataBase only", skip=isNotSupported(), body = function( currentSpec ) {
				var uri = createURI("LDEV1292")
				var result = _InternalRequest(
					template:"#uri#/test2/test.cfm"
				);
				expect(result.filecontent.trim()).toBe('1');
			});
		});
	}
	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledNasme;
	}

	private struct function getCredentials() {
		// getting the credentials from the enviroment variables
		return server.getDatasource("mysql");
	}

	private string function defineDatasource(){
		var mySql = getCredentials();
		application action="update" datasource=mySQL;
	}

	private function createTable() {
		query {
	        echo("DROP TABLE IF EXISTS `test`");
		}
		query {
	        echo("CREATE TABLE `test` (`id` int)");
		}
		query {
	        echo("INSERT INTO `test` VALUES (1);");
		}
	}
}