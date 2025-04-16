
component extends="org.lucee.cfml.test.LuceeTestCase"	{

	function run(){

		describe( 'elvis operator' , function() {

			it( 'returns "false" when first operand is a variable containing false' , function() {
				var first_operand = false;
				var actual = first_operand ?: 'foo';
				expect( actual ).toBeFalse();
			});

			it( 'returns "false" when first operand is an inline false' , function() {
				var actual = false ?: 'foo';
				expect( actual ).toBeFalse();
			});

			it( 'returns "true" when first operand is an equality' , function() {
				var actual = (1==1) ?: 'foo';
				expect( actual ).toBeTrue();
			});

			it( 'returns "true" when first operand is a function that returns true' , function() {
				var temp = function() { return true; };
				var actual = temp() ?: 'foo';
				expect( actual ).toBeTrue();
			});

			it( 'returns second operand when first operand is null' , function() {
				var actual = NullValue() ?: 'foo';
				expect( actual ).toBe( 'foo' );
			});

		});

	}

}