component{
	this.name = getTemplatePath();
	this.sessionManagement = true;
	this.sessionTimeout = createTimeSpan(0,0,30,0);
}