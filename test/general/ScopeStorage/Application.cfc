component {
	if(isNull(url.cacheName)) throw serialize(url);
	this.name = url.cacheName;
    this.sessionManagement = true;
    this.sessionStorage = url.cacheName;
    this.sessionCluster = true;
    this.sessionTimeout = createTimeSpan( 0, 0, 1, 0);

}