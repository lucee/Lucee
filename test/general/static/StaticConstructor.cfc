component {
	
	// static constructor
	static {
		 static.pstaticConstr1="p-static-constr-1";
		 static.staticConstr1="static-constr-1";
		staticConstr2="static-constr-2";
		staticConstr3=function (abc,def){
			insideClosure=true;
			local.localvar=1;
			return {static:static,arguments:arguments,local:local};
		}
		function staticConstr4(ghj){
			insideUDF=true;
			return variables;
		}
	}

	// static variables set outsite 
	static.constr1="constr-1";


}