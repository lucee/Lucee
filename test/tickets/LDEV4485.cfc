component extends = "org.lucee.cfml.test.LuceeTestCase" skip="true" {
	
	function run( testResults , testBox ) {
		describe( title="Testcase for LDEV-4485 test configImport allow", body=function() {
			it(title="check with allowSelect", body = function( currentSpec ) {

				var allow = {
					"allowAlter":true,
					"allowCreate":true,
					"allowDelete":true,
					"allowDrop":true,
					"allowGrant":false,
					"allowInsert":true,
					"allowRevoke":true,
					"allowSelect":false,
					"allowUpdate":true,
				};

				var name = "test-ds-ldev-4485-allowSelect";

				configImport( data=getConfig( name, allow ), type="server", password=request.SERVERADMINPASSWORD );
				var ds = getDatasource( name );
				for (var a in allow ) {
					var n = mid( a, 6);
					expect ( ds[ n] ).toBe( allow[ a ] );
				}
			});

			it(title="check with allow (all)", body = function( currentSpec ) {

				var allow = {
					"allow": 511
				};
				// ignore these values, only used for key name
				var allowAll = {
					"allowAlter":true,
					"allowCreate":true,
					"allowDelete":true,
					"allowDrop":true,
					"allowGrant":true,
					"allowInsert":true,
					"allowRevoke":true,
					"allowSelect":true,
					"allowUpdate":true,
				};
				var name = "test-ds-ldev-4485-allow-all";
				configImport( data=getConfig( name, allow ), type="server", password=request.SERVERADMINPASSWORD );
				var ds = getDatasource( name );
				
				for (var a in allowAll ) {
					var n = mid( a, 6 );
					expect ( ds[ n ] ).toBe( true );
				}
			});

			it(title="check with allow (none)", body = function( currentSpec ) {

				var allow = {
					"allow": 0
				};
				// ignore these values, only used for key name
				var allowAll = {
					"allowAlter":true,
					"allowCreate":true,
					"allowDelete":true,
					"allowDrop":true,
					"allowGrant":true,
					"allowInsert":true,
					"allowRevoke":true,
					"allowSelect":true,
					"allowUpdate":true,
				};
				var name = "test-ds-ldev-4485-allow-none";
				configImport( data=getConfig( name, allow ), type="server", password=request.SERVERADMINPASSWORD );
				var ds = getDatasource( name );
				
				for (var a in allowAll ) {
					var n = mid( a, 6 );
					expect ( ds[ n ] ).toBe( false );
				}
			});
		});
	}

	private function getDatasource( required string name ){
		admin action="getDatasource"
			type="server"
			password="#request.SERVERADMINPASSWORD#"
			name="#arguments.name#"
			returnVariable="local.datasource";
		return datasource;
	}

	private function getConfig( string name, struct allow ){
		var cfg = {
			"alwaysSetTimeout":"true",
			"blob":"false",
			"class":"com.mysql.cj.jdbc.Driver",
			"clob":"true",
			"connectionLimit":"10",
			"connectionTimeout":"1",
			"custom":"useUnicode=false&characterEncoding=UTF-8&serverTimezone=US%2FEastern&maxReconnects=3",
			"database":"lucee",
			"dbdriver":"MySQL",
			"dsn":"jdbc:mysql://{host}:{port}/{database}",
			"host":"localhost",
			"liveTimeout":"1",
			"metaCacheTimeout":"60000",
			"password":"lucee",
			"port":"33306",
			"storage":"true",
			"username":"lucee",
			"validate":"false"
		};

		structAppend( cfg, arguments.allow );
		var ds = {
			"datasources": {
				"#arguments.name#": cfg
			}
		};
		return ds;
	}
	
}