component {
	this.name="lucee-error-templates";
	this.clientmanagement="no";
	this.scriptprotect="all";
	this.sessionmanagement="no";
	this.setclientcookies="no";
	this.setdomaincookies="no"; 
	this.applicationtimeout="#createTimeSpan(0,0,5,0)#";
	
	function onRequestStart( target ) {
		setting showdebugoutput=false;
		return false;
	}
}