component {
	this.name="lucee-error-templates";
	this.clientmanagement="no";
	this.scriptprotect="all";
	this.sessionmanagement="no";
	this.setclientcookies="no";
	this.setdomaincookies="no"; 
	this.applicationtimeout="#createTimeSpan(0,0,5,0)#";
	this.monitoring.showDebug=false;
	this.monitoring.showDoc=false;
	this.monitoring.showMetric=false;
	this.monitoring.showTest=false;
	
	function onRequestStart( target ) {
		
		return false;
	}
}
