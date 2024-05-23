component {
	this.name="lucee-error-templates";
	this.clientmanagement="no";
	this.scriptprotect="all";
	this.sessionmanagement="no";
	this.setclientcookies="no";
	this.setdomaincookies="no"; 
	this.applicationtimeout="#createTimeSpan(0,0,5,0)#";
	this.showDebug=false;
	this.showDoc=false;
	this.showMetric=false;
	this.showTest=false;
	
	function onRequestStart( target ) {
		
		return false;
	}
}
