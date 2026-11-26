<cfscript>
	setting showdebugoutput=false;
	if ( len( url.tagParser ?: "" ) ) application action="update" dateParser=url.tagParser;
	setLocale( url.locale );
	str = "02/01/2026 03:04";
	result = {
		"dateParser": getApplicationSettings().dateParser,
		"parseDateTime": dateFormat( parseDateTime( str ), "yyyy-mm-dd" ),
		"parseDateTimeDateOnly": dateFormat( parseDateTime( "02/01/2026" ), "yyyy-mm-dd" ),
		"dateTimeFormat": dateTimeFormat( str, "yyyy-mm-dd HH:nn" ),
		"dateFormat": dateFormat( "02/01/2026", "yyyy-mm-dd" ),
		"lsDateFormat": lsDateFormat( "02/01/2026", "yyyy-mm-dd" ),
		"dateAdd": dateFormat( dateAdd( "d", 1, "02/01/2026" ), "yyyy-mm-dd" ),
		"day": day( str ),
		"month": month( str ),
		"isDate": isDate( "13/01/2026" ),
		"isDateAmbiguous": isDate( str ),
		"iso": dateTimeFormat( "2026-01-02 03:04:05", "yyyy-mm-dd HH:nn:ss" ),
		"isoT": dateTimeFormat( "2026-01-02T03:04:05", "yyyy-mm-dd HH:nn:ss" ),
		"ts": dateTimeFormat( "{ts '2026-01-02 03:04:05'}", "yyyy-mm-dd HH:nn:ss" ),
		"monthName": dateFormat( "January 2, 2026", "yyyy-mm-dd" ),
		"compare": dateCompare( "02/01/2026", createDate( 2026, 1, 2 ), "d" )
	};
	echo( serializeJSON( result ) );
</cfscript>
