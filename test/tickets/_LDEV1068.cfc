component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1068", function() {
			it("Checking serializeJSON() for serializing query", function( currentSpec ){
				empDetails = queryNew("name,age,sex","varchar,integer,varchar", [['saravana',35,'male'],['Bob',20, 'female'],['pothy',25, 'male']]);
				serializedEmpDetails = serializeJSON(empDetails,true);
				expect(find('"NAME":', serializedEmpDetails) > 0).toBeTrue();
				expect(find('"AGE":', serializedEmpDetails) > 0).toBeTrue();
				expect(find('"SEX":', serializedEmpDetails) > 0).toBeTrue();
			});
		});
	}
}