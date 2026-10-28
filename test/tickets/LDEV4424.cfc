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
			it(title = "SQL display puts param values in the right '?' when [ ] or ` ` contain a '?'", body = function( currentSpec ) {
				var conv = createObject( "java", "lucee.runtime.tag.util.QueryParamConverter" );
				var sql = conv.convert( "select x as [Country/Entity?] from t where id = ?", [ { value: 4, type: "integer" } ] );
				expect( sql.toString() ).toBe( "select x as [Country/Entity?] from t where id = 4" );
				expect( sql.toString( true ) ).toBe( 'select x as [Country/Entity?] from t where id = {type: "CF_SQL_INTEGER", value: 4}' );

				sql = conv.convert( "select x as `Country/Entity?` from t where id = :id", { id: { value: 4, type: "integer" } } );
				expect( sql.toString() ).toBe( "select x as `Country/Entity?` from t where id = 4" );

				sql = conv.convert( "select ARRAY[?, ?]", [ { value: 1, type: "integer" }, { value: 2, type: "integer" } ] );
				expect( sql.toString() ).toBe( "select ARRAY[1, 2]" );
			});
			it(title = "params with a '?' inside a [ ] identifier are converted", body = function( currentSpec ) {
				var conv = createObject( "java", "lucee.runtime.tag.util.QueryParamConverter" );
				var sql = conv.convert( "select x as [Country/Entity?] from t where id = ?", [ { value: 1, type: "integer" } ] );
				expect( sql.getSQLString() ).toBe( "select x as [Country/Entity?] from t where id = ?" );
				expect( arrayLen( sql.getItems() ) ).toBe( 1 );

				sql = conv.convert( "select x as [a?] from t where id in (?)", [ { value: "1,2,3", type: "integer", list: true } ] );
				expect( sql.getSQLString() ).toBe( "select x as [a?] from t where id in (?,?,?)" );

				sql = conv.convert( "select x as [a]]?] from t where id = :id", { id: { value: 1, type: "integer" } } );
				expect( sql.getSQLString() ).toBe( "select x as [a]]?] from t where id = ?" );
			});
			it(title = "params with a '?' inside a ` ` identifier are converted", body = function( currentSpec ) {
				var conv = createObject( "java", "lucee.runtime.tag.util.QueryParamConverter" );
				var sql = conv.convert( "select x as `Country/Entity?` from t where id = ?", [ { value: 1, type: "integer" } ] );
				expect( sql.getSQLString() ).toBe( "select x as `Country/Entity?` from t where id = ?" );
				expect( arrayLen( sql.getItems() ) ).toBe( 1 );

				sql = conv.convert( "select x as `a``?` from t where id = :id", { id: { value: 1, type: "integer" } } );
				expect( sql.getSQLString() ).toBe( "select x as `a``?` from t where id = ?" );
			});
			it(title = "a '?' inside [ ] is still a param when the count matches", body = function( currentSpec ) {
				var conv = createObject( "java", "lucee.runtime.tag.util.QueryParamConverter" );
				var sql = conv.convert( "select ARRAY[?, ?]", [ 1, 2 ] );
				expect( sql.getSQLString() ).toBe( "select ARRAY[?, ?]" );
				expect( arrayLen( sql.getItems() ) ).toBe( 2 );
			});
			it(title = "more question marks than params still throws", body = function( currentSpec ) {
				var conv = createObject( "java", "lucee.runtime.tag.util.QueryParamConverter" );
				expect( function() {
					conv.convert( "select [a?] from t where id = ? and x = ?", [ 1 ] );
				} ).toThrow( message="there are more question marks in the SQL than params defined" );
			});
			it(title = "Checking cfquery sql has '?' without params", skip="#notHasMssql()#", body = function( currentSpec ) {
				expect(testQuery(checkWithParams=false)).toBe('sql with ? works');
			});
			// the struct passed before were named params, so the ? had no positional param to bind
			it(title = "Checking cfquery sql has '?' with params", skip="#notHasMssql()#", body = function( currentSpec ) {
				expect(testQuery(checkWithParams=true)).toBe('sql with ? works');
			});
			it(title = "Checking cfquery sql has '?' with cfqueryparam", skip="#notHasMssql()#", body = function( currentSpec ) {
				query name="local.q" {
					echo( "SET NOCOUNT ON; declare @orgID int = " );
					queryparam cfsqltype="CF_SQL_INTEGER" value="0";
					echo( "; declare @tblCountries TABLE (countryID int, country varchar(2));
						insert into @tblCountries (countryID, country) values (1, 'US'), (2, 'CA');
						SELECT country as [Country/Entity?], countryID as countryID FROM @tblCountries;" );
				}
				expect( q.recordCount ).toBe( 2 );
				expect( q[ "Country/Entity?" ][ 1 ] ).toBe( "US" );
			});
			it(title = "Checking queryExecute sql has '?' in [ ] without params", skip="#notHasMssql()#", body = function( currentSpec ) {
				var q = queryExecute( "SELECT 'US' AS [Country/Entity?]", {} );
				expect( q[ "Country/Entity?" ][ 1 ] ).toBe( "US" );
			});
			it(title = "Checking queryExecute sql has '?' in [ ] with positional params", skip="#notHasMssql()#", body = function( currentSpec ) {
				var q = queryExecute( "SELECT 'US' AS [Country/Entity?], ? AS id", [ { value: 1, cfsqltype: "cf_sql_integer" } ] );
				expect( q[ "Country/Entity?" ][ 1 ] ).toBe( "US" );
				expect( q.id[ 1 ] ).toBe( 1 );
			});
			it(title = "Checking queryExecute sql has '?' in [ ] with named params", skip="#notHasMssql()#", body = function( currentSpec ) {
				var q = queryExecute( "SELECT 'US' AS [Country/Entity?], :id AS id", { id: { value: 1, cfsqltype: "cf_sql_integer" } } );
				expect( q[ "Country/Entity?" ][ 1 ] ).toBe( "US" );
				expect( q.id[ 1 ] ).toBe( 1 );
			});
			it(title = "Checking queryExecute sql has '?' in ` ` without params", skip="#notHasMysql()#", body = function( currentSpec ) {
				var q = queryExecute( "SELECT 'US' AS `Country/Entity?`", {}, { datasource: server.getDatasource( "mysql" ) } );
				expect( q[ "Country/Entity?" ][ 1 ] ).toBe( "US" );
			});
			it(title = "Checking queryExecute sql has '?' in ` ` with positional params", skip="#notHasMysql()#", body = function( currentSpec ) {
				var q = queryExecute( "SELECT 'US' AS `Country/Entity?`, ? AS id", [ { value: 1, cfsqltype: "cf_sql_integer" } ], { datasource: server.getDatasource( "mysql" ) } );
				expect( q[ "Country/Entity?" ][ 1 ] ).toBe( "US" );
				expect( q.id[ 1 ] ).toBe( 1 );
			});
			it(title = "Checking queryExecute sql has '?' in ` ` with named params", skip="#notHasMysql()#", body = function( currentSpec ) {
				var q = queryExecute( "SELECT 'US' AS `Country/Entity?`, :id AS id", { id: { value: 1, cfsqltype: "cf_sql_integer" } }, { datasource: server.getDatasource( "mysql" ) } );
				expect( q[ "Country/Entity?" ][ 1 ] ).toBe( "US" );
				expect( q.id[ 1 ] ).toBe( 1 );
			});
		});
	}

	private boolean function notHasMssql() {
		return !structCount(server.getDatasource("mssql"));
	}

	private boolean function notHasMysql() {
		return !structCount(server.getDatasource("mysql"));
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
			attr.params = [ {type="CF_SQL_INTEGER", value="0"} ];
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