<cfscript>
// LDEV-6078: Consecutive this scope assignments
// Tests line number bug with multiple this.xxx = yyy; statements
// Bug: Multiple assignments may share same line number

component {
	this.name = "Test1";
	this.sessionManagement = true;
	this.sessionTimeout = createTimeSpan( 0, 0, 30, 0 );
	this.applicationTimeout = createTimeSpan( 0, 2, 0, 0 );
	this.clientManagement = false;
	this.setClientCookies = true;
	this.setDomainCookies = false;
	this.scriptProtect = "all";
}
</cfscript>
