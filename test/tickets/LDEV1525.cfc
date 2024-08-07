component extends="org.lucee.cfml.test.LuceeTestCase" label="qoq" {
	function beforeAll(){
		variables.uri = createURI("LDEV1525");
	}
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1525", function() {
			it(title = "Checking QoQ while missing column (native)", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/ldev1525.cfm",
					url: "scene=native"
				);
				var q = deserializeJSON( local.result.filecontent, false );
				expect( QueryColumnExists(q, "column_0") ).toBeTrue();
			});

			it(title = "Checking QoQ while missing column (native) with order by", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/ldev1525.cfm",
					url: "scene=native&orderby=true"
				);
				var q = deserializeJSON( local.result.filecontent, false );
				expect( QueryColumnExists(q, "column_0") ).toBeTrue();
			});

			it(title = "Checking QoQ while missing column (hsqldb)", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/ldev1525.cfm",
					url: "scene=hsqldb"
				);
				var q = deserializeJSON( local.result.filecontent, false );
				expect( QueryColumnExists( q, "c1") ).toBeTrue();
			});

			it(title = "Checking QoQ while missing column (hsqldb) with order by", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/ldev1525.cfm",
					url: "scene=hsqldb&orderby=true"
				);
				systemOutput( local.result.filecontent, true );
				expect ( local.result.filecontent).toInclude("invalid ORDER BY expression");
			});


		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}