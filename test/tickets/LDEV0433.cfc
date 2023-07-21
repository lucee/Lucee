component extends="org.lucee.cfml.test.LuceeTestCase"{


	private function getJavaVersion() {
        var raw=server.java.version;
        var arr=listToArray(raw,'.');
        if(arr[1]==1) // version 1-9
            return arr[2];
        return arr[1];
    }

	function beforeAll(){
		variables.original = getLocale();
	}

	function afterAll(){
		setLocale(variables.original);
		var localeObj = createObject("java", "java.util.Locale");
		localeObj.setDefault("en");
	}

	function run(){
		describe( title="Test suite for LDEV-433", body=function(){
			it(title="Checking lsNumberFormat with german locale", body=function(){
				setLocale("german (switzerland)");
				
				if(getJavaVersion()>=11) {
					expect(lsNumberFormat(12345.78)).toBe("12’346");

				}
				else {	
					expect(lsNumberFormat(12345.78)).toBe("12'346");
				}

			});

			it(title="Checking lsNumberFormat's equivalent java code", body=function(){
				// creating object for java math class & rounding the actual number
				var strObj = createObject("java", "java.lang.Math");
				var roundedVal = strObj.round(12345.78);

				// creating object for java locale class & setting german switzerland as default locale
				var localeObj = createObject("java", "java.util.Locale");
				var availableLocales = localeObj.getAvailableLocales();
				for(var i=1;i<arrayLen(availableLocales);i++){
					if(availableLocales[i].Country == "CH" && availableLocales[i].Language == "de"){
						localeObj.setDefault(availableLocales[i]);
					}
				}

				// formating rounded number to swiss locale
				var numberFormatObj = createObject("java", "java.text.NumberFormat");
				numberFormatObj = numberFormatObj.getNumberInstance(localeObj.getDefault());
				if( getJavaVersion() >= 11 ) {
					expect(numberFormatObj.format(roundedVal)).toBe("12’346");
				}
				else {	
					expect(numberFormatObj.format(roundedVal)).toBe("12'346");
				}
				localeObj.setDefault("en");
			});
		});
	}
}