<cfscript>
	private int function echoInt(int i) type="java" {
		if(i==1)throw new Exception("shit happens!!!");
		return i*2;
	}
	echo(echoInt(4));
</cfscript>