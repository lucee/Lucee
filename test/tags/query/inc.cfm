<cfset local.sctListener={
			before=function (caller,args,a) {
				arguments.args.sql="insert into QueryTestAsync(id,i,dec) values('3',1,1.0)"; // change SQL
		        return arguments.args;
		    }
		    ,after=function (caller,args) {
				//return arguments;
		    }
		}>