component extends = "org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll() {
		variables.uri = createURI("LDEV2586");

		variables.mssql=getCredentials();
		if(structCount(variables.mssql)) {
			// define datasource
			application action="update" 
				datasources={'LDEV2586': mssql};

			// create necessary tables
			query datasource="LDEV2586" {
				echo("DROP TABLE IF EXISTS LDEV2586");
			}
			query datasource="LDEV2586"{
				echo("CREATE TABLE LDEV2586( id int, value decimal )");
			}
			query datasource="LDEV2586"{
				echo("INSERT INTO LDEV2586 VALUES( 1,'1000' )");
				echo("INSERT INTO LDEV2586 VALUES( 2,'23.45' )");
			}
		}
	}

	public function afterAll(){
		if(hasCredentials()) {
			query datasource="LDEV2586"{
				echo("DROP TABLE IF EXISTS LDEV2586");
			}
		}
	}

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-2586", function() {

			it(title = " cfqueryparam does handle decimal value = 1000 with maxLength = 8 ",skip=!hasCredentials(), body = function( currentSpec ) {
				query name="local.test" datasource="LDEV2586" {
					echo("SELECT * FROM LDEV2586");
				}
				query name="local.subUsers" datasource="LDEV2586" {
				   echo("SELECT * FROM LDEV2586 WHERE value = ");
				   queryparam value='#test.value[1]#' cfsqltype="CF_SQL_DECIMAL" maxlength="8";
				}
				expect(subUsers.value).toBe('1000');
			});

			it(title = " cfqueryparam doesn't handle deciaml value = 1000 with maxLength = 7",skip=!hasCredentials(), body = function( currentSpec ) {
				query name="local.test" datasource="LDEV2586" {
					echo("SELECT * FROM LDEV2586");
				}
				query name="local.subUsers" datasource="LDEV2586" {
				   echo("SELECT * FROM LDEV2586 WHERE value = ");
				   queryparam value='#test.value[1]#' cfsqltype="CF_SQL_DECIMAL" maxlength="7";
				}
				expect(subUsers.value).toBe('1000');
			});

			it(title = " cfqueryparam does handle decimal value = 23.45 with maxLength = 5",skip=!hasCredentials(), body = function( currentSpec ) {
				query name="local.test" datasource="LDEV2586" {
					echo("SELECT * FROM LDEV2586");
				}
				query name="local.subUsers" datasource="LDEV2586" {
				   echo("SELECT * FROM LDEV2586 WHERE value = ");
				   queryparam value='#test.value[2]#' cfsqltype="CF_SQL_DECIMAL" maxlength="5";
				}
				expect(subUsers.value).toBe('23');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private boolean function hasCredentials() {
		return structCount(getCredentials());
	}

	private struct function getCredentials() {
		return server.getDatasource("mssql");
	}
}
