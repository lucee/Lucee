component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1068", function() {
			it("Checking serializeJSON() for serializing query", function( currentSpec ){
				var empDetails = queryNew("name,age,sex","varchar,integer,varchar", [['saravana',35,'male'],['Bob',20, 'female'],['pothy',25, 'male']]);
				var serializedEmpDetails = serializeJSON(empDetails,true);
				expect(find('"NAME":', serializedEmpDetails) > 0).toBeTrue();
				expect(find('"AGE":', serializedEmpDetails) > 0).toBeTrue();
				expect(find('"SEX":', serializedEmpDetails) > 0).toBeTrue();
			});

			it("Checking cfwddx tag for serializing query", function( currentSpec ){
				var empDetails = queryNew("name,age,sex","varchar,integer,varchar", [['saravana',35,'male'],['Bob',20, 'female'],['pothy',25, 'male']]);
				
				wddx action="cfml2wddx" input=empDetails output="wddx";

				expect(find("fieldNames='NAME,AGE,SEX'", wddx) > 0).toBeTrue();
			});

			it("Checking query.columnlist", function( currentSpec ){
				var empDetails = queryNew("name,age,sex","varchar,integer,varchar", [['saravana',35,'male'],['Bob',20, 'female'],['pothy',25, 'male']]);
				
				expect(find("NAME,AGE,SEX", empDetails.columnlist) > 0).toBeTrue();
			});


		});



	}
}