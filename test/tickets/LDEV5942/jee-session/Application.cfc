component {
	this.name = "ldev5942_jee_session_rotate";
	this.sessionManagement = true;
	this.sessionStorage = "memory";
	this.sessionTimeout = createTimeSpan( 0, 0, 0, 30 );
	this.setClientCookies = true;
	this.applicationTimeout = createTimeSpan( 0, 0, 1, 0 );
	this.sessionType = "jee";
}
