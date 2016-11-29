component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1098", function() {
			it("javacast with type 'bigdecimal',value from string.charAt()", function( currentSpec ){
				try {
					var string= 'test';
					var result = JavaCast("bigdecimal", string.charAt(0));
				} catch ( any e){
					var result = e.message;
				}
				expect(result).toBe(116);
			});

			it("javacast with type 'boolean'", function( currentSpec ){
				var string= '1';
				var result = JavaCast("boolean", string);
				expect(result).toBe("true");
			});

			it("javacast with type 'byte', value from string.charAt()", function( currentSpec ){
				var string= 'test';
				var result = JavaCast("byte", string.charAt(0));
				expect(result).toBe(116);
			});

			
			it("javacast with type 'int', value from string.charAt()", function( currentSpec ){
				try{
					var string= 'test';
					var result= JavaCast("int", string.charAt(0));
				} catch ( any e){
					var result = e.message
				}
				expect(result).toBe(116);
			});

			it("javacast with type 'long', value from string.charAt()", function( currentSpec ){
				try{
					var string= 'test';
					var result= JavaCast("long", string.charAt(0));
				} catch ( any e){
					var result = e.message
				}
				expect(result).toBe(116);
			});

			it("javacast with type 'float', value from string.charAt()", function( currentSpec ){
				try{
					var string= 'test';
					var result= JavaCast("float", string.charAt(0));
				} catch ( any e){
					var result = e.message
				}
				expect(result).toBe('116.0');
			});

			it("javacast with type 'double', value from string.charAt()", function( currentSpec ){
				try{
					var string= 'test';
					var result= JavaCast("double", string.charAt(0));
				} catch ( any e){
					var result = e.message
				}
				expect(result).toBe(116);
			});

			it("javacast with type 'short', value from string.charAt()", function( currentSpec ){
				try{
					var string= 'test';
					var result= JavaCast("short", string.charAt(0));
				} catch ( any e){
					var result = e.message
				}
				expect(result).toBe(116);
			});
		});
	}
}