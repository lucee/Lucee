component extends="org.lucee.cfml.test.LuceeTestCase" labels="isValid" {

	function run( testResults, testBox ) {
		describe( "LDEV-2434 isValid types ip, ipv4 and ipv6", function() {

			it( "ipv4 accepts valid dotted quads", function() {
				var valid = [ "0.0.0.0", "1.2.3.4", "127.0.0.1", "192.168.0.1", "255.255.255.255", "10.0.0.10", "100.200.99.9" ];
				for ( var ip in valid ) {
					expect( isValid( "ipv4", ip ) ).toBeTrue( ip );
					expect( isValid( "ip", ip ) ).toBeTrue( ip );
					expect( isValid( "ipv6", ip ) ).toBeFalse( ip );
				}
			});

			it( "ipv4 rejects invalid values", function() {
				var invalid = [ "", " ", "1.2.3", "1.2.3.4.5", "256.1.1.1", "1.2.3.999", "1.2.3.-1", "a.b.c.d", "1.2.3.", ".1.2.3", "1..2.3",
					"01.2.3.4", "1.2.3.04", "00.0.0.0", "1.2.3.4 ", " 1.2.3.4", "1.2.3.4\n", "1.2.3.4:80", "1.2.3.4/24", "1,2,3,4", "+1.2.3.4",
					"1.2.3.0004", "localhost", "example.com", "１.２.３.４" ];
				for ( var ip in invalid ) {
					expect( isValid( "ipv4", ip ) ).toBeFalse( "[#ip#]" );
					expect( isValid( "ip", ip ) ).toBeFalse( "[#ip#]" );
				}
			});

			it( "ipv6 accepts valid addresses", function() {
				var valid = [ "::", "::1", "1::", "2001:db8::1", "2001:0db8:0000:0000:0000:ff00:0042:8329", "2001:db8:0:0:0:ff00:42:8329",
					"1:2:3:4:5:6:7:8", "1:2:3:4:5:6:7::", "::2:3:4:5:6:7:8", "1::8", "FE80::1", "fe80::ABCD:ef01", "::ffff:192.168.0.1",
					"::192.168.0.1", "64:ff9b::192.0.2.33", "1:2:3:4:5:6:1.2.3.4", "1:2:3:4:5::1.2.3.4", "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff" ];
				for ( var ip in valid ) {
					expect( isValid( "ipv6", ip ) ).toBeTrue( ip );
					expect( isValid( "ip", ip ) ).toBeTrue( ip );
					expect( isValid( "ipv4", ip ) ).toBeFalse( ip );
				}
			});

			it( "ipv6 rejects invalid values", function() {
				var invalid = [ "", ":", ":::", "1:2:3:4:5:6:7", "1:2:3:4:5:6:7:8:9", "1:2:3:4:5:6:7:8::", "::1:2:3:4:5:6:7:8", "1::2::3", "1:::2", ":1:2:3:4:5:6:7",
					"1:2:3:4:5:6:7:", "12345::1", "g::1", "1::2:xyz", "::1.2.3", "::1.2.3.4.5", "::1.2.3.256", "::01.2.3.4", "1.2.3.4::", "1.2.3.4:1::",
					"::ffff:1.2.3.4:5", "1:2:3:4:5:6:7:1.2.3.4", "fe80::1%eth0", "fe80::1%1", "fe80::1%", "[::1]", "[::1]:80", "::1/64", " ::1", "::1 ",
					"2001:db8::1\n", "1.2.3.4" ];
				for ( var ip in invalid ) {
					expect( isValid( "ipv6", ip ) ).toBeFalse( "[#ip#]" );
				}
				// ip is v4 or v6 only, so the same values are invalid for ip as well, except the plain ipv4 one
				for ( var ip in invalid ) {
					if ( ip != "1.2.3.4" ) expect( isValid( "ip", ip ) ).toBeFalse( "[#ip#]" );
				}
			});

			it( "does not resolve hostnames", function() {
				expect( isValid( "ip", "localhost" ) ).toBeFalse();
				expect( isValid( "ipv4", "localhost" ) ).toBeFalse();
				expect( isValid( "ipv6", "localhost" ) ).toBeFalse();
				expect( isValid( "ip", "lucee.org" ) ).toBeFalse();
				expect( isValid( "ipv6", "ip6-localhost" ) ).toBeFalse();
			});

			it( "type names are case insensitive and non string values are handled", function() {
				expect( isValid( "IP", "1.2.3.4" ) ).toBeTrue();
				expect( isValid( "IPv4", "1.2.3.4" ) ).toBeTrue();
				expect( isValid( "IPV6", "::1" ) ).toBeTrue();
				expect( isValid( "ip", [] ) ).toBeFalse();
				expect( isValid( "ip", {} ) ).toBeFalse();
				expect( isValid( "ip", 12345 ) ).toBeFalse();
				expect( isValid( "ipv4", 1.5 ) ).toBeFalse();
				expect( isValid( "ipv4", true ) ).toBeFalse();
			});

			it( "existing types are unchanged", function() {
				expect( isValid( "email", "a@b.com" ) ).toBeTrue();
				expect( isValid( "integer", 1 ) ).toBeTrue();
				expect( isValid( "string", "1.2.3.4" ) ).toBeTrue();
			});

			it( "an unknown type still throws and lists the new types", function() {
				try {
					isValid( "ipx", "1.2.3.4" );
					fail( "must throw" );
				}
				catch ( any e ) {
					expect( e.message ).toInclude( "ipv4" );
					expect( e.message ).toInclude( "ipv6" );
				}
			});

		});
	}

}
