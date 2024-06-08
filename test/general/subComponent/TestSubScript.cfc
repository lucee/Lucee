component {
	function test() {
		local.foo = function(a="argclosuremain"){return "closure-insidemain:"&arguments.a;}
		return "main:"&foo();
	}
	function a() {
		return "a";
	}
	function b() {
		return "a";
	}
}

component name="sub" {
	function test() {
		local.foo = function(a="argclosuresub"){return "closure-insidesub:"&arguments.a;}
		return "sub:"&foo();
	}
	function c() {
		return "a";
	}
	function d() {
		return "a";
	}
}