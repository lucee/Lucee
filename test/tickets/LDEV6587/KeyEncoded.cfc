component {
	// Key.getEncoded() declares byte[], a base64 string converts like any other binary
	function getAlgorithm() {
		return "RAW";
	}
	function getFormat() {
		return "RAW";
	}
	function getEncoded() {
		return "AQID";
	}
}
