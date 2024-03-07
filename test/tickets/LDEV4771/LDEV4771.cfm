<cfscript>

	qry = queryNew("id", "integer", [1]);

	function f0(id) {
		var a = [10];
		a.each(function(item){
			writeOutput(id);
			var b = [20];
			b.each(function(item){
				writeOutput(id);
			});
		});
		var f1 = function(){return id;}
		writeOutput(f1() );
		f2();
	}

	function f2() {
		writeOutput(id);
	}

		cfloop (query=qry ) {
	   writeOutput(f0(id = 2));
	};

</cfscript>