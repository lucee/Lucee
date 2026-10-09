/**
 * LDEV-3095: isValid( "email" ) gives wrong results for the examples on https://en.wikipedia.org/wiki/Email_address#Examples
 * (RFC 5321 / 5322 addr-spec, RFC 6531 SMTPUTF8): quoted local parts with consecutive dots, IPv6 address literals
 * and non-ASCII symbols in the local part.
 * Dotless domains (admin@example) are still rejected on purpose, see the last spec.
 */
component extends="org.lucee.cfml.test.LuceeTestCase" labels="mail" {

	function run( testResults , testBox ) {
		describe( title='LDEV-3095: isValid( "email" ) with the examples from Wikipedia', body=function(){

			it( title="accepts the valid dot-atom examples", body=function() {
				assertValid( [
					"simple@example.com", "very.common@example.com", "FirstName.LastName@EasierReading.org", "x@example.com",
					"long.email-address-with-hyphens@and.subdomains.example.com", "user.name+tag+sorting@example.com", "name/surname@example.com",
					"example@s.example", "mailhost!username@example.org", "user%example.com@example.org", "user-@example.org",
					"disposable.style.email.with+symbol@example.com", "other.email-with-hyphen@example.com", "example-indeed@strange-example.com"
				] );
			});

			it( title="accepts quoted local parts, also with consecutive dots or escaped quotes", body=function() {
				assertValid( [
					'" "@example.org', '"john..doe"@example.org', '"jane..doe"@example.org',
					'"very.(),:;<>[]\".VERY.\"very@\\ \"very\".unusual"@strange.example.com'
				] );
			});

			it( title="accepts IPv4 and IPv6 address literals", body=function() {
				assertValid( [
					"postmaster@[123.123.123.123]", "postmaster@[IPv6:2001:0db8:85a3:0000:0000:8a2e:0370:7334]",
					"_test@[IPv6:2001:0db8:85a3:0000:0000:8a2e:0370:7334]", "user@[IPv6:2001:db8::1]", "user@[IPv6:::1]", "user@[ipv6:::]",
					"user@[IPv6:::ffff:192.0.2.1]", "user@[IPv6:2001:db8:0:0:0:0:192.0.2.1]"
				] );
			});

			it( title="accepts non-ASCII symbols in the local part (SMTPUTF8)", body=function() {
				assertValid( [ "I❤️CHOCOLATE@example.com", "jürgen@example.com", "用户@例子.广告" ] );
			});

			it( title="rejects the invalid examples", body=function() {
				assertInvalid( [
					"Abc.example.com", "A@b@c@example.com", 'a"b(c)d,e:f;g<h>i[j\k]l@example.com', 'just"not"right@example.com',
					'this is"not\allowed@example.com', 'this\ still\"not\\allowed@example.com',
					"1234567890123456789012345678901234567890123456789012345678901234+x@example.com",
					"i_like_underscore@but_its_not_allow_in_this_part.example.com", "i.like.underscores@but_they_are_not_allowed_in_this_part"
				] );
			});

			it( title="rejects malformed IPv6 address literals", body=function() {
				assertInvalid( [
					"user@[2001:db8::1]", "user@[IPv6:2001:db8::1::2]", "user@[IPv6:12345::1]", "user@[IPv6:g::1]", "user@[IPv6:1:2:3:4:5:6:7]",
					"user@[IPv6:1:2:3:4:5:6:7:8:9]", "user@[IPv6:]", "user@[IPv6::1]", "user@[IPv6:1.2.3.4]", "user@[IPv6:::1.2.3.256]",
					"user@[IPv6:2001:db8::1", "user@[IPv6: ::1]", "user@[foo:bar]"
				] );
			});

			it( title="rejects control characters and line breaks in a quoted local part", body=function() {
				assertInvalid( [
					'"a' & chr( 9 ) & 'b"@example.com', '"a' & chr( 1 ) & 'b"@example.com', '"a' & chr( 127 ) & 'b"@example.com',
					'"a' & chr( 13 ) & chr( 10 ) & 'b"@example.com', '"a\' & chr( 10 ) & 'b"@example.com', '"a' & chr( 8207 ) & 'b"@example.com'
				] );
			});

			it( title="rejects whitespace and invisible characters in an unquoted local part", body=function() {
				assertInvalid( [ "a" & chr( 160 ) & "b@example.com", "a" & chr( 8207 ) & "b@example.com", "a" & chr( 12288 ) & "b@example.com" ] );
			});

			it( title="rejects dotless domains (on purpose: ICANN prohibits them and Lucee never accepted them)", body=function() {
				assertInvalid( [ "admin@example", "admin@mailserver1" ] );
			});

		});
	}

	private function assertValid( required array addresses ) {
		var wrong = [];
		for ( var address in arguments.addresses ) {
			if ( !isValid( "email", address ) ) arrayAppend( wrong, address );
		}
		expect( wrong ).toBeEmpty( "rejected but valid: [" & arrayToList( wrong, "] [" ) & "]" );
	}

	private function assertInvalid( required array addresses ) {
		var wrong = [];
		for ( var address in arguments.addresses ) {
			if ( isValid( "email", address ) ) arrayAppend( wrong, address );
		}
		expect( wrong ).toBeEmpty( "accepted but invalid: [" & arrayToList( wrong, "] [" ) & "]" );
	}

}
