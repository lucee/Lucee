// plain component (not a TestBox spec), so the variables scope captured by the closures
// only contains what the test is about, TestBox 7 puts non serializable java objects into the variables scope of a spec
component {

	public function createClosure() {
		local.y="abc";
		variables.z="def";

		return function() {
			return y&z;
		};
	}

	public function createInner() {
		return function() {
			local.y="abc";
			variables.z="def";

			local.x=function() {
				return y&z;
			}
			local.os=objectSave(x);
			local.o=objectLoad(os);

			return o();
		};
	}

}
