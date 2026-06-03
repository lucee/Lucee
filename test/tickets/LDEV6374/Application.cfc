component {
	this.name = "LDEV6374";
	this.sessionManagement = true;
	this.setClientCookies = true;
	this.sessionStorage = "memory";
	this.sessionTimeout = createTimeSpan( 0, 0, 10, 0 );
	this.applicationTimeout = createTimeSpan( 0, 1, 0, 0 );
	this.sessionType = "cfml";

	public function onRequestStart() {
		setting requesttimeout = 10;
	}
}
