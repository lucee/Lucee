component {
	// a script component containing "<cfscript>" in a string is first parsed tag based, the closure inside the string must not fail compilation
	function getCode() {
		return "<cfscript>test();
			function(){
			}
		</cfscript>";
	}
}
