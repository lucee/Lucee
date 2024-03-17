component
	name="_BaseBean"
{
	/** 
	* Init
	*/	
	function Init(
	){			
		return this;	
	}	

	/**
	 * This method iterates over all the parent properties and returns a struct of all the properties it's collected
	 *
	 * @md 
	 * @props 
	 */
	public array function collectAllProperties(
		struct md=StructNew(),
		array props=ArrayNew(1)
	){
		if(StructIsEmpty(arguments.md)){
			arguments.md = getMetaData(this);	
		}
		
	    local.prop = 1;
	    if (structKeyExists(arguments.md,"properties")) {	    	
	        for (local.prop=1; local.prop <= ArrayLen(arguments.md.properties); local.prop++) {
	        	local.inner = 1;
	        	local.add_item = true;
	        	for(local.inner=1; local.inner <= ArrayLen(arguments.props); local.inner++){
	        		if(arguments.props[local.inner].name eq arguments.md.properties[local.prop].name){
	        			local.add_item = false;
	        			break;
	        		}
	        	}	        	
	        	
	        	if(local.add_item){
					arrayAppend(arguments.props,arguments.md.properties[local.prop]);
				}
	        }
	    }
	    if (StructKeyExists(arguments.md, 'extends') and arguments.md.extends.fullname neq "WEB-INF.cftags.component") {
	        arguments.props = collectAllProperties(arguments.md.extends,arguments.props);
	    }
	    return arguments.props;
	}	
}