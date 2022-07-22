component extends="org.lucee.cfml.test.LuceeTestCase" labels="query,qoq" { 

	public void function testEmptyQueryParamArray() {
		local.p = {
			iids: { type: 'integer', value: [] }, // change to ids, ok
			id: { type: 'integer', value: "1" }
		};
		local.q = queryNew( "id", "numeric" );
		queryAddRow( q );
		querySetCell( q, "id", 1, 1 );
		local.sql = "select id from q where id = :id";
		local.r = queryExecute(
			sql: sql,
			params: p,
			options: { dbtype: "query" }
		);
		expect( r.recordcount ).toBe( 1 );
	}

	public void function testEmptyQueryParam() {
		local.p = {
			iids: { type: 'integer', value: "" },
			id: { type: 'integer', value: "1" }
		};
		local.q = queryNew( "id", "numeric" );
		queryAddRow( q );
		querySetCell( q, "id", 1, 1 );
		local.sql = "select id from q where id = :id";
		local.r = queryExecute(
			sql: sql,
			params: p,
			options: { dbtype: "query" }
		);
		expect( r.recordcount ).toBe( 1 );
	}

}