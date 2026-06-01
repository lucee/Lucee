component accessors="true" {
	property name="pub" access="public" default="public-value";
	property name="priv" access="private" default="private-value";
	property name="pkg" access="package" default="package-value";
	property name="rem" access="remote" default="remote-value";

	public any function callPrivateInternally() {
		return getPriv();
	}
	public any function setPrivateInternally( required any val ) {
		setPriv( arguments.val );
		return this;
	}
}
