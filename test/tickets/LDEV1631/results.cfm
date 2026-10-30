<cfscript>
	str = "The quick brown Fox jumps over the lazy dog, fox@example.com and DOG@example.org";
	res = {};
	res.reFind = reFind( "[a-z]+@[a-z]+\.com", str );
	res.reFindOffset = reFind( "o", str, 20 );
	res.reFindNoMatch = reFind( "zzz", str );
	res.reFindNoCase = reFindNoCase( "fox", str );
	res.reFindNoCaseOffset = reFindNoCase( "fox", str, 20 );
	res.reFindSub = reFind( "([a-z]+)@([a-z]+)\.com", str, 1, true );
	res.reFindNoCaseSub = reFindNoCase( "(dog)@(example)\.org", str, 1, true );
	res.reFindNoMatchSub = reFind( "zzz", str, 1, true );
	res.reFindAll = reFind( "o", str, 1, false, "all" );
	res.reFindNoCaseAll = reFindNoCase( "dog", str, 1, false, "all" );
	res.reFindMultiline = reFind( "^b", "a#chr( 10 )#b", 1, false );
	res.reFindMatches = reMatch( "[a-z]+@[a-z]+\.[a-z]+", str );
	res.reMatchNoCase = reMatchNoCase( "[a-z]+@[a-z]+\.[a-z]+", str );
	res.reMatchNone = reMatch( "zzz", str );
	res.reReplace = reReplace( str, "o", "0" );
	res.reReplaceAll = reReplace( str, "o", "0", "all" );
	res.reReplaceNoCase = reReplaceNoCase( str, "dog", "cat" );
	res.reReplaceNoCaseAll = reReplaceNoCase( str, "dog", "cat", "all" );
	// the back reference syntax differs between the engines
	backRef = url.engine == "java" ? "$2 at $1" : "\2 at \1";
	res.reReplaceBackRef = reReplace( str, "([a-z]+)@([a-z]+)\.com", backRef );
	res.reReplaceNoMatch = reReplace( str, "zzz", "x", "all" );
	res.isValid = isValid( "regex", "abc123", "^[a-z]+[0-9]+$" );
	res.isNotValid = isValid( "regex", "abc", "^[a-z]+[0-9]+$" );
	res.emptyString = reReplace( "", "a", "b", "all" );
	writeOutput( serializeJSON( res ) );
</cfscript>
