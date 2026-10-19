component {
	final num = 5;
	final fn = () => "fn";
	plain = 5;

	function info() {
		var r = {
			num: variables.num,
			plain: variables.plain,
			plainClass: getMetadata( variables.plain ).getName(),
			numClass: getMetadata( variables.num ).getName(),
			isNumeric: isNumeric( variables.num )
		};
		try { r.call = variables.fn(); }
		catch ( any e ) { r.call = "ERROR: " & e.message; }
		return r;
	}
}
