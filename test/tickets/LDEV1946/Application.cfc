component { 
	this.name = "LDEV-1946";

	public function onMissingTemplate() {
		writeOutput("Missing");
		//location url="test4.cfm" addtoken=false; // works fin
		abort;
	}

		
	public function onError() {
		writeOutput("onError");
		abort;
	}

}