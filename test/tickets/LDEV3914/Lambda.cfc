component {

	function run() {
		var fn = () => {
			thread name="LDEV3914cfc" {
				thread.result = "thread inside a lambda";
			}
			thread action="join" name="LDEV3914cfc";
			return cfthread.LDEV3914cfc.result;
		};
		return fn();
	}

}
