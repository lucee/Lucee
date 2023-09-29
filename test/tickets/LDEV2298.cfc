component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV2298");
	}

	function run( testResults, testBox ) {
		describe( "Test case for LDEV2298, inserting date with null=false, no sqltype", function(){

			xit( title="queryExecute() column doesn't allow nulls", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 1,tablename = 'ldev2298_null'}
				);
				expect( result.filecontent ).toInclude("can't cast [] to date value"); // feels wrong, returns {ts '1900-01-01 00:00:00'} aka cfml epoch
			});

			xit( title="queryExecute() column allows nulls", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 3,tablename = 'ldev2298_notnull'}
				);
				expect( result.filecontent ).toInclude("can't cast [] to date value"); // feels wrong, returns {ts '1900-01-01 00:00:00'} aka cfml epoch
			});
		});

		describe( "Test case for LDEV2298, inserting date with null=false, no sqltype, missing date param", function(){

			it( title="queryExecute() column doesn't allow nulls", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 1,tablename = 'ldev2298_null', passDateParam=false}
				);
				expect( result.filecontent ).toInclude("param [utcNow] not found");
			});

			it( title="queryExecute() column allows nulls", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 3,tablename = 'ldev2298_notnull', passDateParam=false}
				);
				expect( result.filecontent ).toInclude("param [utcNow] not found");
			});
		});

		describe( "Test case for LDEV2298, inserting date with null=false, with sqltype, via array of structs", function(){

			it( title="queryExecute() column allows nulls", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 4,tablename = 'ldev2298_null'}
				);
				expect( result.filecontent ).toInclude("can't cast [] to date value");
			}); 

			it( title="queryExecute() column doesn't allow nulls", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 2,tablename = 'ldev2298_notnull'}
				);
				expect( result.filecontent ).toInclude("can't cast [] to date value");
			});

		});

		describe( "Test case for LDEV2298, inserting date with null=false, with sqltype, via array of structs, missing date param", function(){

			it( title="queryExecute() column allows nulls", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 4,tablename = 'ldev2298_null', passDateParam=false}
				);
				expect( result.filecontent ).toInclude("param [utcNow] not found");
			}); 

			it( title="queryExecute() column doesn't allow nulls", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 2,tablename = 'ldev2298_notnull', passDateParam=false}
				);
				expect( result.filecontent ).toInclude("param [utcNow] not found");
			});

		});

		describe( "Test case for LDEV2298, inserting date with null=false, with sqltype", function(){

			
			it( title="queryExecute() column allows nulls", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 4,tablename = 'ldev2298_null'}
				);
				expect( result.filecontent ).toInclude("can't cast [] to date value");
			}); 

			it( title="queryExecute() column doesn't allow nulls", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 2,tablename = 'ldev2298_notnull',}
				);
				expect( result.filecontent ).toInclude("can't cast [] to date value");
			});

		});

		describe( "Test case for LDEV2298, inserting date with null=true, with sqltype", function(){

			it( title="queryExecute() column allows nulls", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 4,tablename = 'ldev2298_null', allowNull=true}
				);
				expect( trim( result.filecontent ) ).toBe("");
			}); 

			it( title="queryExecute() column doesn't allow nulls", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 2,tablename = 'ldev2298_notnull',allowNull=true}
				);  //  Cannot insert the value NULL into column
				expect( result.filecontent ).toInclude("lucee.runtime.exp.DatabaseException");
			});

		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}