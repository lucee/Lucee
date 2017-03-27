component output="false" extends="Base" accessors="true"{

	property name="name" type="String";
	property name="qArray" type="Array";

	/**
	 * Tag Name
	 */
	variables.tagname = "query";


	/**
	 * @hint Constructor
	 */
	public Base function init(){
		super.init(argumentCollection=arguments);
		return this;
	}

	/**
	 * @hint Execute the query
	 */
	public Result function execute(){
		if(!structIsempty(arguments)){
			structappend(variables.attributes,arguments,"yes");
	}
		if(structKeyExists(arguments,"sql") && len(arguments.sql)){
			 this.setSql(arguments.sql);
		}

		//parse the sql into an array and save it
		setQArray(parseSql());

		// invoke the query and return the result
		return invokeTag();
	}

	/**
	 * @hint Parse the sql string converting into an array.
	 *       Named and positional params will populate the array too.
	 */
	private Array function parseSql(){
		var result = [];
		var sql = trim(this.getSql());
		var namedParams = getNamedParams();
		var positionalParams = getPositionalParams();
		var positionalCursor = 1;

		var Pos = 1;
		var TotalLen = Len(Sql);
		var Items = [];

		while ( Pos LTE TotalLen )
		{
			var StartPos = Pos ;

			var NextChar = Mid(Sql,Pos,1) ;

			// If quoted string, consume entire thing, ignoring escaped quotes.
			if ( NextChar EQ '"' OR NextChar EQ "'" )
			{
				var Len = 1 ;
				while ( Mid(Sql,Pos+Len,1) NEQ NextChar && ( TotalLen GT ( Pos + Len ) ) )
				{
					Len++ ;

					// If escaped quote, skip twice.
					if (Mid(Sql,Pos+Len,2) EQ "\" & NextChar) Len+=2;
				}
				// Include closing quote:
				Len++ ;

				result.add({type='String',value=Mid(Sql,StartPos,Len)})
				Pos += Len ;

			}
			// If SQL comment found, consume until closing comment.
			elseif ( Mid(Sql,Pos,2) EQ '/*' )
			{
				var Len = 2 ;
				while ( Mid(Sql,Pos+Len,2) NEQ '*/' AND Pos LTE TotalLen )
				{
					Len++ ;
				}
				Len += 2 ;

				result.add({type='String',value=Mid(Sql,StartPos,Len)});
				Pos += Len ;
			}
			// If colon found outside a string, check if named param.
			elseif (NextChar EQ ':')
			{
				var Match = refind( '::' , Sql , Pos , true )
				if (Match.Len[1] eq 2){
					result.add({type='String',value=':'})
						Pos += 2;
				}
				else{
					var Match = refind( ':\w+' , Sql , Pos , true ) ;
					if (ArrayLen(Match.Pos) and Match.Pos[1] EQ Pos)
					{
						result.add( findNamedParam(namedParams, Mid(Sql,Match.Pos[1]+1,Match.Len[1]-1) ) ) ;
						Pos += Match.Len[1] ;
					}
					else
					{
						result.add({type='String',value=':'})
						Pos += 1;
					}
				}
			}
			// If question mark found outside a string, assume unnamed param.
			elseif (NextChar EQ '?')
			{
				result.add( positionalParams[positionalCursor] );
				positionalCursor++ ;
				Pos++ ;
			}

			// If Pos marker has not changed, find any non-significant text and treat as string.
			if (Pos EQ StartPos)
			{
 				var Match = refind( '(?:[^:"''?/]+|/(?!\*))+' , Sql , Pos , true ) ;
				if (ArrayLen(Match.Pos) AND Match.Pos[1] EQ Pos)
				{
					result.add({type='String',value=Mid(Sql,Match.Pos[1],Match.Len[1])})
					Pos += Match.Len[1] ;
				}
				else
				{
					// This should never happen, but if it does it would result in an endless loop.
					// So to avoid any risk of this throw an error instead:
					throw(type="org.lucee.cfml.query.SqlUnknownSyntaxError", message="The sql statement [#sql#] contains an unknown syntax error.");

				}
			}

		}

		return result;
	}

	/**
	 * @hint Return just the named params
	 */
	private Array function getNamedParams(){
		var params = getParams();
		var result = [];

		for(var item in params){
			if(structKeyExists(item,'name')){
				result.add(item);
			}
		}

		return result;
	}


	/**
	 * @hint Return just the positional params
	 */
	private Array function getPositionalParams(){
		var params = getParams();
		var result = [];

		for(var item in params){
			if(not structKeyExists(item,'name')){
				result.add(item);
			}
		}

		return result;
	}


	/**
	 * @hint Scan the passed array looking for a "name" param match.
	 */
	private Struct function findNamedParam(Array params,String name){
		for(var item in params){
			if(structKeyExists(item,'name') && name == item.name){
				return item;
			}
		}

		throw(type="org.lucee.cfml.query.namedParameterNotFoundException", message="The named parameter [#arguments.name#] has not been provided");

	}

	/**
	 * @hint Scan the passed array looking for a "name" param match.
	 */
	public static query function new(required columnNames, columnTypes, data) {
		return queryNew(columnNames,columnTypes?:nullvalue(),data?:nullvalue());
	}

}