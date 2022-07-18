component {
	if(isNull(url.cacheName)) throw serialize(url);
	this.name = url.cacheName;
    this.sessionManagement = true;
    this.sessionCluster = true;
    this.sessionTimeout = createTimeSpan( 0, 0, 1, 0);
	
	this.clientManagement = true;
    this.clientStorage = url.cacheName;
    this.clientCluster = true;
    this.clientTimeout = createTimeSpan( 0, 0, 1, 0);
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}
}