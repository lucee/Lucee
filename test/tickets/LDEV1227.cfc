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
			/*it(title="calling super component's static method without scope resolution operator", body = function( currentSpec ) {
				var myObj = createObject("component", "#compPath#LDEV1227.comp2");
				expect(myObj.func1('pothys')).toBeWithCase("POTHYS");
			});*/

			/*it(title="calling component's static method without scope resolution operator", body = function( currentSpec ) {
				var myObj = createObject("component", "#compPath#LDEV1227.comp2");
				expect(myObj.func2('pothys')).toBeWithCase("POTHYS");
			});*/

			it(title="calling super component's static method with scope resolution operator", body = function( currentSpec ) {
				var myObj = createObject("component", "#compPath#LDEV1227.comp2");
				expect(myObj.func3('pothys')).toBeWithCase("POTHYS");
			});

			it(title="calling component's static method with scope resolution operator", body = function( currentSpec ) {
				var myObj = createObject("component", "#compPath#LDEV1227.comp2");
				expect(myObj.func4('pothys')).toBeWithCase("POTHYS");
			});

			it(title="calling component's static method without .static", body = function( currentSpec ) {
				var myObj = createObject("component", "#compPath#LDEV1227.comp2");
				expect(myObj.func5('pothys')).toBeWithCase("POTHYS");
			});

			it(title="calling component's static method without .static", body = function( currentSpec ) {
				var myObj = createObject("component", "#compPath#LDEV1227.comp2");
				expect(myObj.func6('pothys')).toBeWithCase("POTHYS");
			});

			it(title="calling component's super static method with super:: ", body = function( currentSpec ) {
				var myObj = createObject("component", "#compPath#LDEV1227.comp2");
				expect(myObj.func7('pothys')).toBeWithCase("POTHYS");
			});
		});
	}
}