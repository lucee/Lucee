component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {
	function beforeAll() { 
		variables.msSQL = server.getDatasource("mssql");
		if( structCount(msSQL) ) {
			application action="update" datasource=msSQL;
		}
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-4424", function() {
			it(title = "Checking cfquery sql has '?' without params", skip="#notHasMssql()#", body = function( currentSpec ) {
				expect(testQuery(checkWithParams=false)).toBe('sql with ? works');
			});
			it(title = "Checking cfquery sql has '?' with params", skip="#notHasMssql()#", body = function( currentSpec ) {
				expect(testQuery(checkWithParams=true)).toBe('sql with ? works');
			});
		});
	}

	private boolean function notHasMssql() {
		return !structCount(server.getDatasource("mssql"));
	}

	private string function testQuery(checkWithParams = false ) {
		var sql = "
			SET NOCOUNT ON;
				
			declare @tblCountries TABLE (countryID int, country varchar(2));
			insert into @tblCountries (countryID, country)
			values (1, 'US'), (2, 'CA');        
		
			SELECT country as [Country/Entity?], countryID as countryID
			FROM @tblCountries;"

		var attr = {};
		if (checkWithParams) {
			attr.params = {type="CF_SQL_INTEGER", value="0"};
			sql &= "declare @orgID int = ?";
		}

		var rtn = "sql with ? works"
		try {
			query attributeCollection="#attr#" {
				echo(sql);
			}
		}
		catch(any e) {
			rtn = e.message;
		}
		return rtn;
	}
}