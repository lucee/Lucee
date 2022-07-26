component extends="org.lucee.cfml.test.LuceeTestCase"{


	function beforeAll(){
		setLocale("en_us");
	}

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

 			it(title="Checking numberFormat() with mask value '9,999,999.99' ", body = function( currentSpec ) {
 				var res = 2342234234;
 				var result = numberformat(res, "9,999,999.99");
 				expect(result).toBe('2,342,234,234.00');
 			});

 			it(title="Checking numberFormat() with mask value '0,000,000.00' ", body = function( currentSpec ) {
 				var res = 2342234234;
 				var result = numberformat(res, "0,000,000.00");
 				expect(result).toBe('2,342,234,234.00');
 			});
 
 			it(title="Checking numberFormat() with mask value '_,___,___.__' ", body = function( currentSpec ) {
 				var res = 2342234234;
 				var result = numberformat(res, "_,___,___.__");
 				expect(result).toBe('2,342,234,234.00');
 			});

 			it(title="Checking numberFormat() with mask value ',___.00' ", body = function( currentSpec ) {
 				var res = 2342234234;
 				var result = numberformat(res, ",___.00");
 				expect(result).toBe('2,342,234,234.00');
 			});

 			it(title="Checking lsnumberFormat() with mask value ',' ", body = function( currentSpec ) {
 				var result = LSNumberFormat(1, ",");
 				expect(result).toBe('1');
 				var result = LSNumberFormat(1.01, ",");
 				expect(result).toBe('1.01');
 			});
 		});
 	}
 }
 