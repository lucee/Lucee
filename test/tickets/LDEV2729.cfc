component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "test case for LDEV2729", function() {
			it( title="replacelist with includeemptyfields in 4th/5th/6th position", body=function( currentSpec ) {

				expect(replacelist( "{name}", "{,}", ",", false)).toBe("name");
				expect(replacelist( "{name}", "{,}", ",", true)).toBe("name");
				expect(replacelist( "{name}", "{,}", ",", ",", true)).toBe("name");
				expect(replacelist( "{name}", "{,}", ",", ",", false)).toBe("name");

			});

			it( title="replacelist with includeemptyfields in correct position", body=function( currentSpec ) {

				expect(replacelist( "{name}", "{,}", ",", ",", ",", false)).toBe("name");
				expect(replacelist( "{name}", "{,}", ",", ",", ",", true)).toBe("name");
				expect(replacelist( "{name,,}", "{,}", ",", ",", ",", false)).toBe("name,,");
				expect(replacelist( "{name,,}", "{,}", ",", ",", ",", true)).toBe("name,,");
				// using multicharacter value as delimeter
				expect(replacelist( "{name}", "{del}", "[,]", "del", ",",true)).toBe("[name]");
				expect(replacelist( "{name}", "{del}", "[,]", "del", ",",false)).toBe("[name]");
			});

			
			it( title="replacelist() using named argument with includeemptyfields", body=function( currentSpec ) {

				expect(replacelist( string="{name}", list1="{,}", list2=",", includeemptyfields=false)).toBe("name");
				expect(replacelist( string="{name}", list1="{,}", list2=",", includeemptyfields=true)).toBe("name");
				expect(replacelist( string="{name}", list1="{,}", list2=",", delimiterlist1=",", delimiterlist2=",", includeemptyfields=false)).toBe("name");
				expect(replacelist( string="{name}", list1="{,}", list2=",", delimiterlist1=",", includeemptyfields=true)).toBe("name");
				expect(replacelist( string="{name}", list1="{,}", list2=",", delimiterlist1=",", delimiterlist2=",", includeemptyfields=false)).toBe("name");
				expect(replacelist( string="{name}", list1="{,}", list2=",", delimiterlist1=",", delimiterlist2=",", includeemptyfields=false)).toBe("name");
				// using multicharacter value as delimeter
				expect(replacelist( string="{name}", list1="{del}", list2="[,]", delimiterlist1="del", delimiterlist2=",", includeemptyfields=true)).toBe("[name]");
				expect(replacelist( string="{name}", list1="{del}", list2="[,]", delimiterlist1="del", delimiterlist2=",", includeemptyfields=false)).toBe("[name]");
				expect(replacelist( string="{name}", list1="{del}", list2="[dd]", delimiterlist1="del", delimiterlist2="dd", includeemptyfields=true)).toBe("[name");
				expect(replacelist( string="{name}", list1="{del}", list2="[dd]", delimiterlist1="del", delimiterlist2="dd", includeemptyfields=false)).toBe("[name]");
				// using boolean value as delimeter with named arguments
				expect(replacelist( string="{name}", list1="{true}", list2="[,]", delimiterlist1="true", includeemptyfields=true)).toBe("[name]");
				expect(replacelist( string="{name}", list1="{true}", list2="[,]", delimiterlist1="true", delimiterlist2=",", includeemptyfields=false)).toBe("[name]");
			});
			
			it( title="String.replacelist() with includeemptyfields", body=function( currentSpec ) {
				expect("{name}".replacelist("{,}", ",", false)).toBe("name");
				expect("{name}".replacelist("{,}", ",", true)).toBe("name");
				expect("{name}".replacelist("{,}", ",", ",", true)).toBe("name");
				expect("{name}".replacelist("{,}", ",", ",", false)).toBe("name");
				expect("{name}".replacelist("{,}", ",", ",", ",", false)).toBe("name");
				expect("{name}".replacelist("{,}", ",", ",", ",", true)).toBe("name");
				expect("{name,,}".replacelist("{,}", ",", ",", ",", false)).toBe("name,,");
				expect("{name,,}".replacelist("{,}", ",", ",", ",", true)).toBe("name,,");
				// using multicharacter value as delimeter
				expect("{name}".replacelist("{del}", "[,]", "del", ",",true)).toBe("[name]");
				expect("{name}".replacelist("{del}", "[,]", "del", ",",false)).toBe("[name]");
			});
		});

		describe( "test case for LDEV2729", function() {
			it( title="ReplaceListNoCase with includeemptyfields in 4th/5th/6th position", body=function( currentSpec ) {

				expect(ReplaceListNoCase( "{name}", "{,}", ",", false)).toBe("name");
				expect(ReplaceListNoCase( "{name}", "{,}", ",", true)).toBe("name");
				expect(ReplaceListNoCase( "{name}", "{,}", ",", ",", true)).toBe("name");
				expect(ReplaceListNoCase( "{name}", "{,}", ",", ",", false)).toBe("name");
				// using multicharacter value as delimeter
				expect(ReplaceListNoCase( "{name}", "{del}", "[,]", "del", ",",true)).toBe("[name]");
				expect(ReplaceListNoCase( "{name}", "{del}", "[,]", "del", ",",false)).toBe("[name]");
			});

			it( title="ReplaceListNoCase with includeemptyfields in correct position", body=function( currentSpec ) {

				expect(ReplaceListNoCase( "{name}", "{,}", ",", ",", ",", false)).toBe("name");
				expect(ReplaceListNoCase( "{name}", "{,}", ",", ",", ",", true)).toBe("name");
				expect(ReplaceListNoCase( "{name,,}", "{,}", ",", ",", ",", false)).toBe("name,,");
				expect(ReplaceListNoCase( "{name,,}", "{,}", ",", ",", ",", true)).toBe("name,,");
			});
			
			it( title="replacelistNoCase() using named argument with includeemptyfields", body=function( currentSpec ) {

				expect(ReplaceListNoCase( string="{name}", list1="{,}", list2=",", includeemptyfields=false)).toBe("name");
				expect(ReplaceListNoCase( string="{name}", list1="{,}", list2=",", includeemptyfields=true)).toBe("name");
				expect(ReplaceListNoCase( string="{name}", list1="{,}", list2=",", delimiterlist1=",", delimiterlist2=",", includeemptyfields=false)).toBe("name");
				expect(ReplaceListNoCase( string="{name}", list1="{,}", list2=",", delimiterlist1=",", includeemptyfields=true)).toBe("name");
				expect(ReplaceListNoCase( string="{name}", list1="{,}", list2=",", delimiterlist1=",", delimiterlist2=",", includeemptyfields=false)).toBe("name");
				expect(ReplaceListNoCase( string="{name}", list1="{,}", list2=",", delimiterlist1=",", delimiterlist2=",", includeemptyfields=false)).toBe("name");
				// using multicharacter value as delimeter
				expect(ReplaceListNoCase( string="{name}", list1="{del}", list2="[,]", delimiterlist1="del", delimiterlist2=",", includeemptyfields=true)).toBe("[name]");
				expect(ReplaceListNoCase( string="{name}", list1="{del}", list2="[,]", delimiterlist1="del", delimiterlist2=",", includeemptyfields=false)).toBe("[name]");
				expect(ReplaceListNoCase( string="{name}", list1="{del}", list2="[dd]", delimiterlist1="del", delimiterlist2="dd", includeemptyfields=true)).toBe("[name");
				expect(ReplaceListNoCase( string="{name}", list1="{del}", list2="[dd]", delimiterlist1="del", delimiterlist2="dd", includeemptyfields=false)).toBe("[name]");
				// using boolean value as delimeter with named arguments
				expect(ReplaceListNoCase( string="{name}", list1="{true}", list2="[,]", delimiterlist1="true", includeemptyfields=true)).toBe("[name]");
				expect(ReplaceListNoCase( string="{name}", list1="{true}", list2="[,]", delimiterlist1="true", delimiterlist2=",", includeemptyfields=false)).toBe("[name]");
			});

			it( title="String.replacelistNoCase() with includeemptyfields", body=function( currentSpec ) {

				expect("{name}".replacelistNoCase("{,}", ",", false)).toBe("name");
				expect("{name}".replacelistNoCase("{,}", ",", true)).toBe("name");
				expect("{name}".replacelistNoCase("{,}", ",", ",", true)).toBe("name");
				expect("{name}".replacelistNoCase("{,}", ",", ",", false)).toBe("name");
				expect("{name}".replacelistNoCase("{,}", ",", ",", ",", false)).toBe("name");
				expect("{name}".replacelistNoCase("{,}", ",", ",", ",", true)).toBe("name");
				expect("{name,,}".replacelistNoCase("{,}", ",", ",", ",", false)).toBe("name,,");
				expect("{name,,}".replacelistNoCase("{,}", ",", ",", ",", true)).toBe("name,,");
				// using multicharacter value as delimeter
				expect("{name}".replacelistNoCase("{del}", "[,]", "del", ",",true)).toBe("[name]");
				expect("{name}".replacelistNoCase("{del}", "[,]", "del", ",",false)).toBe("[name]");
			});
		});
	}

}