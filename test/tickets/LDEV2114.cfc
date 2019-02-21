component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-2114", function() {
			it(title = "IsIpInRange() returns incorrect false values", body = function( currentSpec ) {
				// with different class C networks
				expect(IsIpInRange("10.0.0.5-10.0.1.10", "10.0.1.1")).toBe(True);
				expect(IsIpInRange("10.0.0.5-10.0.1.10", "10.0.2.10")).toBe(False);
				expect(IsIpInRange("10.0.0.5-10.0.1.10", "10.0.0.11")).toBe(True);
				expect(IsIpInRange("10.0.0.5-10.0.1.10", "11.0.0.11")).toBe(false);
				//  other than class C networks
				expect(IsIpInRange("240.0.0.0-240.0.10.0", "240.0.9.1")).toBe(True); 
				expect(IsIpInRange("2001:0db8:0000:0042:0000:8a2e:0370:7000-2001:0db8:0000:0042:0000:8a2e:0370:8000", "2001:0db8:0000:0042:0000:8a2e:0370:7334")).toBe(True);
				expect(IsIpInRange("2001:0db8:0000:0042:0000:8a2e:0370:7000-2001:0db8:0000:0042:0000:8a2e:0370:8000", "2001:0db8:0000:0042:0001:8a2e:0370:7334")).toBe(false);
				expect(IsIpInRange("59.90.85.152-59.90.86.200", "59.90.86.153")).toBe(True);
				expect(IsIpInRange("59.90.85.152-59.90.86.200", "59.91.86.153")).toBe(false);
				expect(IsIpInRange("192.168.0.000-192.168.1.200", "192.168.1.111")).toBe(True);
				expect(IsIpInRange("192.168.0.000-192.168.1.200", "193.168.1.111")).toBe(false);
				expect(IsIpInRange("172.16.0.0-172.32.17.10", "172.32.16.11")).toBe(true);
				expect(IsIpInRange("172.16.0.0-172.32.17.10", "172.32.18.11")).toBe(false);
			});
		});
	}
}