component  {

	function foo() {
		thread name="LDEV4157cfc" {
			thread.test = "thread";
		}

		```
			<cfset var res = "tag-island after the thread statement in cfc works">
		```
		thread action="join" name="LDEV4157cfc";

		return cfthread.LDEV4157cfc.test & " and " & res;
	}

}