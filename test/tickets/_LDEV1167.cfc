component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1167", function() {
			it(title="checking WDDX tag, with action = 'wddx2cfml' when the struct empty ", body = function( currentSpec ) {
				var myWDDX = "<wddxPacket version='1.0'><header/><data><struct type='coldfusion.server.ConfigMap'/></data></wddxPacket>";
				cfwddx( action='wddx2cfml', input=myWDDX, output='local.myData');
				expect(local.myData).toBeTypeOf('struct')
			});

			it(title="checking WDDX tag, with action = 'wddx2cfml' when the query empty ", body = function( currentSpec ) {
				myWDDX = "<wddxPacket version='1.0'><header/><data><recordset rowCount='0' fieldNames='id,name' type='coldfusion.sql.QueryTable'><field name='id'></field><field name='name'></field></recordset></data></wddxPacket>";
				cfwddx( action='wddx2cfml', input=myWDDX, output='local.myData');
				expect(local.myData).toBeTypeOf('query')
			});
		});
	}
}
