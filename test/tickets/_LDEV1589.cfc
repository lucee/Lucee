component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		var list=listChangeDelims(CGI.SCRIPT_NAME, ',', '/');
		variables.compPath="";
		cfloop(list="#list#", index="idx"){
			if(listLast(list) != idx)
				compPath=listAppend(compPath, idx, ".");
		}
		if(len(compPath))
			compPath&=".";
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1589", function() {
			it(title = "Checking Pseudo Constructor with function annotations, ends without semi-colon", body = function( currentSpec ) {
				var obj = createObject("component","#compPath#LDEV1589.test1");
				var metaData = getMetaData(obj).functions;
				assertEquals(true, structKeyExists(metaData[1], "param"));
				assertEquals(true, structKeyExists(metaData[1], "hint"));
				assertEquals(true, structKeyExists(metaData[1], "return"));
				if(structKeyExists(metaData[1], "param")) assertEquals("value the value to return", metaData[1].param);
				if(structKeyExists(metaData[1], "hint")) assertEquals("just returns a value", metaData[1].hint);
				if(structKeyExists(metaData[1], "return")) assertEquals("the value", metaData[1].return);
			});

			it(title = "Checking function annotations with Pseudo Constructor, ends with semi-colon", body = function( currentSpec ) {
				var obj = createObject("component","#compPath#LDEV1589.test2");
				var metaData = getMetaData(obj).functions;
				assertEquals(true, structKeyExists(metaData[1], "param"));
				assertEquals(true, structKeyExists(metaData[1], "hint"));
				assertEquals(true, structKeyExists(metaData[1], "return"));
				if(structKeyExists(metaData[1], "param")) assertEquals("value the value to return", metaData[1].param);
				if(structKeyExists(metaData[1], "hint")) assertEquals("just returns a value", metaData[1].hint);
				if(structKeyExists(metaData[1], "return")) assertEquals("the value", metaData[1].return);
			});

			it(title = "Checking function annotations without Pseudo Constructor" , body = function( currentSpec ) {
				var obj = createObject("component","#compPath#LDEV1589.test3");
				var metaData = getMetaData(obj).functions;
				assertEquals(true, structKeyExists(metaData[1], "param"));
				assertEquals(true, structKeyExists(metaData[1], "hint"));
				assertEquals(true, structKeyExists(metaData[1], "return"));
				if(structKeyExists(metaData[1], "param")) assertEquals("value the value to return", metaData[1].param);
				if(structKeyExists(metaData[1], "hint")) assertEquals("just returns a value", metaData[1].hint);
				if(structKeyExists(metaData[1], "return")) assertEquals("the value", metaData[1].return);
			});
		});
	}
}
