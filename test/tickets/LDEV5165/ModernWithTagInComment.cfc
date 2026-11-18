component {
	// a comment mentioning <cfoutput> should not turn this into a tag based component
	function hello() {
		return "<cfoutput>";
	}
}
