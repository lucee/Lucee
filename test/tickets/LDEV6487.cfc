component extends="org.lucee.cfml.test.LuceeTestCase"  {

	function run( testResults , testBox ) {
		describe( title='LDEV-6487: isValid( "email" ) must accept apostrophes and reject invalid dot placement in the local part', body=function(){

			it( title='returns true for an email address containing an apostrophe in the local part (o''reilly@example.com)', skip=true, body=function() {

				expect( isValid( "email", "o'reilly@example.com" ) ).toBeTrue();

			});

			it( title='returns false for an email address whose local part starts with a dot (.foo@example.com)', skip=true, body=function() {

				expect( isValid( "email", ".foo@example.com" ) ).toBeFalse();

			});

			it( title='returns false for an email address whose local part ends with a dot (user.@example.com)', skip=true, body=function() {

				expect( isValid( "email", "user.@example.com" ) ).toBeFalse();

			});

			it( title='returns false for an email address with consecutive dots in the local part (a..b@example.com)', skip=true, body=function() {

				expect( isValid( "email", "a..b@example.com" ) ).toBeFalse();
				
			});

		});
	}

}
