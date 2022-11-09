component {
	function test() {
		local.foo = function(a="argclosuremain"){return "closure-insidemain:"&arguments.a;}
		return "main:"&foo();
	}
}

component name="sub" {
	function test() {
		local.foo = function(a="argclosuresub"){return "closure-insidesub:"&arguments.a;}
		return "sub:"&foo();
	}
}