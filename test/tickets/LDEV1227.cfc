component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		var list=listChangeDelims(CGI.SCRIPT_NAME, ',', '/');
		compPath="";
		cfloop(list="#list#", index="idx"){
			if(listLast(list) != idx)
				compPath=listAppend(compPath, idx, ".");
		}
		if(len(compPath))
			compPath&=".";
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1227", function() {
			it(title="calling super component's static method without scope resolution operator", body = function( currentSpec ) {
				var myObj = createObject("component", "#compPath#LDEV1227.comp2");
				expect(myObj.myUCase1InComp2('pothys')).toBeWithCase("POTHYS");
			});

			it(title="calling super component's static method with scope resolution operator", body = function( currentSpec ) {
				var myObj = createObject("component", "#compPath#LDEV1227.comp2");
				expect(myObj.myUCase2InComp2('pothys')).toBeWithCase("POTHYS");
			});
		});
	}
}