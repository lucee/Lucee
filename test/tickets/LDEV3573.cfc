component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title="Testcase for cfwddx", body=function() {
			it(title="Checking cfwddx with action=cfml2wddx", body=function( currentSpec ) {
				var struct = { "brad's test":"value"};
				cfwddx( action='cfml2wddx', input=struct, output='local.WDDX')
				expect(WDDX).toBe("<wddxPacket version='1.0'><header/><data><struct><var name='brad&apos;s test'><string>value</string></var></struct></data></wddxPacket>");
			});
		});
	}
}