component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {
	function beforeAll() { 
		variables.msSQL = server.getDatasource("mssql");
		if( structCount(msSQL) ) {
			application action="update" datasource=msSQL;
		}
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-4424", function() {
			it(title = "SQL display with a '?' that is not a param marker does not throw", body = function( currentSpec ) {
				var item = createObject( "java", "lucee.runtime.db.SQLItemImpl" ).init( javaCast( "int", 0 ), javaCast( "int", 4 ) );
				var items = createObject( "java", "java.lang.reflect.Array" ).newInstance( createObject( "java", "lucee.runtime.db.SQLItem" ).getClass(), javaCast( "int", 1 ) );
				items[ 1 ] = item;
				var sql = createObject( "java", "lucee.runtime.db.SQLImpl" ).init( "select x as [Country/Entity?] from t where id = ? and y = ?", items );
				var str = sql.toString();
				expect( str ).toInclude( "select x as [Country/Entity" );
				expect( sql.toString( true ) ).toInclude( "select x as [Country/Entity" );
			});
			it(title = "params with an extra '?' that is not a param marker are converted without error", body = function( currentSpec ) {
				var conv = createObject( "java", "lucee.runtime.tag.util.QueryParamConverter" );
				var sql = conv.convert( "select x as [Country/Entity?] from t where id = ?", [ { value: 1, type: "integer" } ] );
				expect( sql.getSQLString() ).toBe( "select x as [Country/Entity?] from t where id = ?" );
				expect( arrayLen( sql.getItems() ) ).toBe( 1 );
			});
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