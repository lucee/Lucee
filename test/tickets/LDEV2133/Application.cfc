component {
	this.name="ldev2133"&createUUID();

	public function onRequestStart() {
		setting requesttimeout=10;
	}
	
	function onRequestEnd() {
		WriteOutPut('Page type');
	}
}