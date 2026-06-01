component extends="IBase" {

	function ifaceMethod() {
		return "ichild:" & this.tag & ":" & super.ifaceMethod();
	}

}
