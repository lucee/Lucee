component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1295", body=function() {
			it(title="checking duplicate function on query", body = function( currentSpec ) {
				var qry = queryNew("id,name,age");
				var dupQuery = qry.Duplicate();
				expect(dupQuery).toBeTypeOf("Query");
			});

			it(title="checking duplicate function on struct", body = function( currentSpec ) {
				var struct = {};
				struct.one="123";
				struct.two="2";
				var dupStruct = struct.Duplicate();
				expect(dupStruct).toBeTypeOf("Struct");
			});

			it(title="checking duplicate function on array", body = function( currentSpec ) {
				var array = [];
				array[1] = "one";
				array[2] = "two";
				array[3] = "three";
				var dupArray = array.Duplicate();
				expect(dupArray).toBeTypeOf("Array");
			});

			it(title="checking duplicate function on string", body = function( currentSpec ) {
				var str = "lucee";
				var dupStr = str.duplicate();
				expect(dupStr).toBeTypeOf("String");
			});

			it(title="checking duplicate function on dateTime", body = function( currentSpec ) {
				var date = now();
				var dupDate = date.duplicate();
				expect(dupDate).toBeTypeOf("date");
			});

			it(title="checking duplicate function on number", body = function( currentSpec ) {
				var num = 7;
				var dupNum = num.duplicate();
				expect(dupNum).toBeTypeOf("numeric");
			});

			it(title="checking duplicate function on boolean", body = function( currentSpec ) {
				var bln = true
				var dupBln= bln.duplicate();
				expect(dupBln).toBeTypeOf("boolean");
			});
		});
	}
}