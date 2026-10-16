// Mixed literal + expression — any non-literal fragment triggers the placeholder; the literal portion is discarded.
component hint="prefix-#now()#-suffix" {
	function test() {}
}
