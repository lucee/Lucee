component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.Dir = "#GetDirectoryFromPath(getCurrentTemplatePath())#LDEV1828";
		directoryCreate(variables.Dir);
		fileWrite("#variables.Dir#/test.cfc", 'component {}');
	}

	function afterAll(){
		directoryDelete(variables.Dir, true);
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1828", function() {
			it( title='Introduced Typed support Array', body=function( currentSpec ) {
				// using string
				var arr=arrayNew['string'](1);
				arr.add("Test");
				arr.add("String");
				assertequals(["Test", "String"], arr);

				// using struct
				var arr1=arrayNew['struct'](1);
				arr1.add(structNew());
				arr1.add(structNew());
				assertequals([{}, {}], arr1);

				//using boolean
				var arr2=arrayNew['boolean'](1);
				arr2.add(true);
				arr2.add(false);
				assertequals([true, false], arr2);

				// using Numeric
				var arr3=arrayNew['Numeric'](1);
				arr3.add(1);
				arr3.add(2);
				assertequals([1, 2], arr3);

				// using component
				var arr4=arrayNew['component'](1);
				arr4.add(new LDEV1828.test());
				assertequals([{}], arr4);

				// using binary
				var Bimg=toBinary(imageNew("../artifacts/image.jpg"));
				var arr5=arrayNew['binary'](1);
				arr5.add(Bimg);
				assertequals(true, isBinary(arr5[1]));

				// using function
				var test = function(){};
				var arr6=arrayNew['function'](1);
				arr6.add(test);
				assertequals(false, isObject(arr6[1]));
			});
		});
	}
}