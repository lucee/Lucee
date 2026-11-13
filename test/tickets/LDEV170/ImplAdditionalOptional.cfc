component implements="IBase" {
	public numeric function f( required numeric x, numeric y ) {
		if ( structKeyExists( arguments, "y" ) ) return x * y;
		return x * 2;
	}
}
