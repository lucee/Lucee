<cfscript>
	// the forms that always worked must keep working
	silent {
		echo( "hidden space" );
	}
	silent	{
		echo( "hidden tab" );
	}
	silent
	{
		echo( "hidden newline" );
	}
	silent /* comment */ {
		echo( "hidden comment" );
	}
	silent // comment
	{
		echo( "hidden line comment" );
	}
	silent;
	echo( "success" );
</cfscript>