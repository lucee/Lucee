component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.ds = server.getDatasource( service="h2", dbFile=server._getTempDir( "LDEV5704" ) );
		cfquery( sql="CREATE TABLE ldev5704 (id numeric NOT NULL, name VARCHAR(25),  PRIMARY KEY (id))", datasource=ds );
		systemOutput("", true);
	}

	function run( testResults, testBox ){
		describe( "Test batch query insert", function(){

			xit( "batch insert via query params", function(){
				truncate();
				var q = getData( "query" );
				var insertSql="insert into ldev5704 (id, name) values (?, ?)";
				cfquery( sql=insertSql, params=q, datasource=ds, batch=true );

				var r = select();
				expect( r.recordcount ).toBe( q.recordcount );
				expect( r.name[1] ).toBe( "lucee" );
				expect( r.name[3] ).toBe( "ralio" );
			});

			xit( "batch insert via struct params", function(){
				truncate();
				var st = getData( "struct", "id" );
				var insertSql="insert into ldev5704 (id, name) values (?, ?)";
				cfquery( sql=insertSql, params=st, datasource=ds, batch=true );

				var r = select();
				expect( r.recordcount ).toBe( len( st ) );
				expect( r.name[1] ).toBe( "lucee" );
				expect( r.name[3] ).toBe( "ralio" );
			});

			it( "batch insert via individual rows - no transaction", function(){
				truncate();
				var arr = getData( "array" );
				var insertSql="insert into ldev5704 (id, name) values (?, ?)";
				var s = getTickCount();
				for ( var row in arr ) {
					cfquery( sql=insertSql, params=row, datasource=ds);
				}
				systemOutput( "individual took " & numberFormat(getTickCount()-s) & "ms for " & len ( arr ) & " rows", true );
				var r = select();
				expect( r.recordcount ).toBe( len( arr ) );
				expect( r.name[1] ).toBe( "lucee" );
				expect( r.name[3] ).toBe( "ralio" );
			});

			it( "batch insert via individual rows  - transaction", function(){
				truncate();
				var arr = getData( "array" );
				var insertSql="insert into ldev5704 (id, name) values (?, ?)";
				var s = getTickCount();
				for ( var row in arr ) {
					cfquery( sql=insertSql, params=row, datasource=ds);
				}
				systemOutput( "individual (w/transaction) took " & numberFormat(getTickCount()-s) & "ms for " & len ( arr ) & " rows", true );
				var r = select();
				expect( r.recordcount ).toBe( len( arr ) );
				expect( r.name[1] ).toBe( "lucee" );
				expect( r.name[3] ).toBe( "ralio" );
			});


			it( "batch insert via array params - automatic transaction", function(){
				truncate();
				var arr = getData( "array" );
				var insertSql="insert into ldev5704 (id, name) values (?, ?)";
				var s = getTickCount();
				cfquery( sql=insertSql, params=arr, datasource=ds, batch=true );
				systemOutput( "batch took " & numberFormat(getTickCount()-s) & "ms for " & len ( arr ) & " rows" , true );
				var r = select();
				expect( r.recordcount ).toBe( len ( arr ) );
				expect( r.name[1] ).toBe( "lucee" );
				expect( r.name[3] ).toBe( "ralio" );
			});

			it( "batch insert via array params - validation", function(){
				truncate();
				var arr = getData( "array" );
				arr = arraySlice(arr, 1, 2);
				// delete the 2nd argument for the second row, 
				// thus triggering an exception due to a mismatch of parameters length from the first row
				arrayDeleteAt( arr[2], 2 );
				var insertSql="insert into ldev5704 (id, name) values (?, ?)";
				expect(function(){
					cfquery( sql=insertSql, params=arr, datasource=ds, batch=true );
				}).toThrow( "Application", ".*question marks.*" ); // TODO this will change to "The number of query batch params for row..."
				
				var r = select();
				expect( r.recordcount ).toBe( 0 ); // i.e. rollback on any error
			});


		} );
	}

	private function getData( returnType, columnKey="" ){
		var q = queryNew( "id,name", "numeric,varchar" );
		arrayEach( [ "lucee", "acf", "ralio" ], function( el, idx ){
			var r = queryAddRow( q );
			querySetCell( q, "id", idx, r );
			querySetCell( q, "name", el, r );
		});
		for (var i=1; i < 998; i++) {
			var r = queryAddRow( q );
			querySetCell( q, "id", r, r );
			querySetCell( q, "name", "row-" & r, r );
		}
		var result = convertQueryToParams( q, arguments.returnType, arguments.columnKey );
	//	systemOutput( "SRC: " & serializeJson(var=q,compact=true), true );
	//	systemOutput( "CONVERT: " & result.toJson(), true )
		return result;
	}

	private function truncate(){
	//	systemOutput("", true);
		cfquery( sql="truncate TABLE ldev5704", datasource=ds );
	}

	private function select(){
		cfquery( name="local.q" sql="select id, name from ldev5704 order by id", datasource=ds );
	//	systemOutput( "SELECT: " & serializeJson(var=q,compact=true), true );
		return local.q;
	}

	private function convertQueryToParams( q, returnType, columnKey ){
		var meta = getMetadata( arguments.q );
		switch ( returnType ){
			case "array":
				return _convertQueryToArray( arguments.q, meta );
				break;
			case "struct":
				return _convertQueryToStruct( arguments.q, meta, arguments.columnKey );
				break;
			case "query":
				return arguments.q;
				break;
			default:
				throw "unsupported #arguments.returnType#";
		}
		return q;
	}
	// like returnType=array, but with param metadata (value & type)
	private function _convertQueryToArray( q, meta ){
		var q = arguments.q;
		var meta = arguments.meta;
		var arr = [];
		var cols = queryColumnArray( arguments.q );
		for (var r=1; r <= arguments.q.recordcount; r++ ){
			var row = [];
			arrayEach( cols, function( col, idx ) {
				arrayAppend(row, {
					value: q[col][r],
					type: meta[ arguments.idx ].typeName
				});
			})
			arrayAppend( arr, row );
		}
		return arr;
	}

	// like returnType=struct, but with param metadata (value & type)
	private function _convertQueryToStruct( q, meta, columnKey ){
		var q = arguments.q;
		var st = structNew( "ordered" );
		for (var r=1; r < arguments.q.recordcount; r++ ){
			var row = {};
			arrayEach( meta, function( col, idx ) {
				row[ col.name ] = {
					value: q[ col.name ][ r ],
					type: col.typeName
				};
			})
			st[ q[ arguments.columnKey ][ r ] ] = row;
		}
		return st;
	}

}
