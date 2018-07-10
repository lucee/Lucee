component {
	function  subFunction(one, two=1) {
		container(one, two);
	}

	function container(one, two=1) {
		if( arguments.two == 1){
			try {
				test1 = variables.one;
			} catch (any e) {
				writeOutput(e.message);
			}
		}else{
			var closureCall = function () {
				try {
				 	test2 = variables.one;
				} catch (any e) {
					writeoutput(e.message);
				}
			};
			closureCall();
		}
	}
}
