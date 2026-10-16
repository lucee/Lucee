/**
 * LDEV-6078: Application.cfc pattern
 * Tests line number bug with this scope assignments
 * Bug: Label L9 maps to component line and first this.xxx assignment
 */
component {

	this.name = "LDEV6078Test";
	this.sessionManagement = true;
	this.sessionTimeout = createTimeSpan( 0, 0, 1, 0 );
	this.setClientCookies = true;

	// Variable assignment
	variables.API_ROOT = getDirectoryFromPath( getCurrentTemplatePath() );

	// Mappings
	this.mappings[ "/test" ] = variables.API_ROOT & "test";
	this.mappings[ "/other" ] = variables.API_ROOT & "other";

}
