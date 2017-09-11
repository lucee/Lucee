component {
	this.name = "LDEV-1487";

	public function onApplicationStart(){
		application.obj = createObject('component', "demo");
	}
}