component extends="org.lucee.cfml.test.LuceeTestCase"{
	
	public function beforeAll(){
		variables.ts=getTimeZone();
	}
	
	public function afterAll(){
		setTimezone(variables.ts);
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1761", body=function() {
			it(title = "Checking evaluate() with datetime", skip =getJavaVersion()>8/* TODO improve test case, current version no longer supported because of access restrictions */, body = function( currentSpec ) {
				

				loop array=getTimeZone().getAvailableIds() item="local.listValue"{
					SetTimeZone(listValue);
					var testDate = ["02/09/2018","10:30:02"];
					var tempdate = parsedatetime(testDate[1] & " " & testDate[2]);
					var st = {timestamp: tempdate};
					var c = serialize(st);
					evaluate(c);
				}
			});

			it(title = "short and long display name in english", skip =getJavaVersion()>8/* TODO improve test case, current version no longer supported because of access restrictions */, body = function( currentSpec ) {
				var Locale=createObject('java','java.util.Locale');
				loop array=getTimeZone().getAvailableIds() item="local.listValue"{
					SetTimeZone(listValue);
					tz=getTimeZone();
					setTimeZone(tz.getDisplayName(true,tz.SHORT,Locale.US));
					setTimeZone(tz.getDisplayName(false,tz.SHORT,Locale.US));
					setTimeZone(tz.getDisplayName(true,tz.LONG,Locale.US));
					setTimeZone(tz.getDisplayName(false,tz.LONG,Locale.US));
				}
			});
		});
	}

	private function getJavaVersion() {
		var raw=server.java.version;
		var arr=listToArray(raw,'.');
		if(arr[1]==1) // version 1-9
			return arr[2];
		return arr[1];
	}
} 