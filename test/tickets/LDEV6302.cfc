component extends="org.lucee.cfml.test.LuceeTestCase" label="LDEV-6302"{

	function beforeAll() {

		// Struct with mixed-case keys for text and textNoCase tests
		variables.textStruct = {
			Zulu: { label: "Zulu" },
			alpha: { label: "Alpha" },
			MIKE: { label: "Mike" }
		};

		// Struct for numeric test
		variables.numericStruct = {
			"10": "ten",
			"2": "two",
			"1": "one"
		};

	}

	function run( testResults, testBox ) {
		describe( title="LDEV-6302: struct.toSorted/StructToSorted single-argument string defaults sortOrder to asc", body=function() {
			it( title="member toSorted('text') with one argument defaults to ascending order", body=function( currentSpec ) {
				expect(
					structKeyList(duplicate(variables.textStruct).toSorted("text"), ",")
				   ).toBe("alpha,MIKE,Zulu");
			} );

			it( title="member toSorted('textNoCase') with one argument defaults to ascending order", body=function( currentSpec ) {
				expect(
					structKeyList(duplicate(variables.textStruct).toSorted("textNoCase"), ",")
				).toBe("alpha,MIKE,Zulu");
			} );

			it( title="member toSorted('numeric') with one argument defaults to ascending order", body=function( currentSpec ) {
				expect(
					structKeyList(duplicate(variables.numericStruct).toSorted("numeric"), ",")
				).toBe("1,2,10");
			} );

			it( title="StructToSorted(base,'text') with one argument defaults to ascending order", body=function( currentSpec ) {
				expect(
					structKeyList(StructToSorted(duplicate(variables.textStruct), "text"), ",")
				   ).toBe("alpha,MIKE,Zulu");
			} );
		} );
	}

}
