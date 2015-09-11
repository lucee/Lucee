<cfscript>
/**
* @caller.status hidden
* @hint Simple way to execute a SQL query with a function.
* @sql sql string to execute
* @params parameter used for the sql string, for details see attribute "params" from tag cfquery.
* @options a struct that contains options to configure the connection, for details see all attributes from tag cfquery (except params and name).
*/
function QueryExecute(required caller, required string sql, any params, struct options={}) callerscopes=true {
   
   //dump(caller);
   //abort;
   if(!isNull(arguments.params))arguments.options.params=arguments.params;
   
   if(!isNull(arguments.caller.local)) {
	   arguments.caller.local.____sqlString=arguments.sql;
	   arguments.caller.local.____attributeCollection=arguments.options;

	   // make the callers scope my own
	   structClear(local);
	   structAppend(local,arguments.caller.local);
	   local.____argumentsScope=arguments.caller.arguments;
	   structClear(arguments);
	   structAppend(arguments,local.____argumentsScope);
	   structDelete(local,"____argumentsScope",false);
   }
   else {
   		local.____attributeCollection=arguments.options;
   		local.____sqlString=arguments.sql;
   	}

   query name="local.____rtn" attributeCollection="#local.____attributeCollection#" nestingLevel="1" {
      echo(local.____sqlString);
   }

   if(isNull(local.____rtn)) return;
   return local.____rtn;
}
</cfscript>