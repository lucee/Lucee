component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1667", body=function() {
			it(title="Checking numberFormat() with mask value '9,9' ", body = function( currentSpec ) {
				var res = 234223.423;
				var result = numberformat(res, "9,9");
				expect(result).toBe('234,223');
			});

			it(title="Checking numberFormat() with mask value '99,99' ", body = function( currentSpec ) {
				var res = 234223.423;
				var result = numberformat(res, "99,99");
				expect(result).toBe('234,223');
			});

			it(title="Checking numberFormat() with mask value '99,99.00' ", body = function( currentSpec ) {
				var res = 234223.423;
				var result = numberformat(res, "99,99.00");
				expect(result).toBe('234,223.42');
			});

			it(title="Checking numberFormat() with mask value '0,0' ", body = function( currentSpec ) {
				var res = 234223.423;
				var result = numberformat(res, "0,0");
				expect(result).toBe('234,223');
			});

			it(title="Checking numberFormat() with mask value '00,00' ", body = function( currentSpec ) {
				var res = 234223.423;
				var result = numberformat(res, "00,00");
				expect(result).toBe('234,223');
			});

			it(title="Checking numberFormat() with mask value '000,000.00' ", body = function( currentSpec ) {
				var res = 234223.423;
				var result = numberformat(res, "000,000.00");
				expect(result).toBe('234,223.42');
			});

			it(title="Checking numberFormat() with mask value '_,_' ", body = function( currentSpec ) {
				var res = 234223.423;
				var result = numberformat(res, "_,_");
				expect(result).toBe('234,223');
			});

			it(title="Checking numberFormat() with mask value '_,_._' ", body = function( currentSpec ) {
				var res = 234223.423;
				var result = numberformat(res, "_,_._");
				expect(result).toBe('234,223.4');
			});
		});
	}
}

