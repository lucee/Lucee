component {


	/**
	* returns a struct of structs where the keys at the top level represent Object names,
	* the keys at 2nd level represent member method name, and their value shows the corresponding
	* BIF name.
	* 
	* result is cached in the this scope for faster execution in subsequent calls
	*/
	function getMemberFunctionList() {

		var result = {};
		var funcList = getFunctionList();

		if ( !isDefined( "this.data.MemberFunctionList" ) ) {

			loop collection="#funcList#" index="local.key" {
				
				local.data = getFunctionData( key );
				
				if ( isDefined( "data.member.name" ) ) 
					result[ data.member.type ][ data.member.name ] = key;
			}

			this.data.MemberFunctionList = result;
		}

		return this.data.MemberFunctionList;
	}


	function getBIFName( objectDotMethod, method="" ) {

		var memberFunctionList = getMemberFunctionList();

		var object = objectDotMethod;

		if ( !len( method ) ) {

			object = listFirst( objectDotMethod, '.' );
			method = listLast ( objectDotMethod, '.' );
		}

		if ( memberFunctionList.keyExists( object ) && memberFunctionList[ object ].keyExists( method ) )
			return memberFunctionList[ object ][ method ];

		return "";
	}


	function formatAttrDesc( desc ) {

		var NL = chr(10);

		desc = replace( trim( desc ), NL & "-", "<br><li>", "all" );
		desc = replace( desc, NL, "<br>", "all" );
	
		return desc;
	}

}