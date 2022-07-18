component {
	this.name = "LDEV3185";
	this.sessionmanagement = true;

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}