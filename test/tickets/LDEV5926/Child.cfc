component extends="Simple" {

	function hello() {
		return super.hello() & " child";
	}

}
