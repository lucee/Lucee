<cfscript>
component {



	/* ==================================================================================================
	   INIT invoked after tag is constructed                                                            =
	================================================================================================== */
	void function init(required boolean hasEndTag, component parent) {
	}

	/* ==================================================================================================
	   onStartTag                                                                                       =
	================================================================================================== */
	boolean function onStartTag(required struct attributes, required struct caller) {
		return true;
	}

	
	boolean function onEndTag(required struct attributes, required struct caller,generatedContent) {
		echo(generatedContent);
		return false;
	}

}
</cfscript>