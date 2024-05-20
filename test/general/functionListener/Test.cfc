component {
	function onSuccess(result) {
		thread.success=result;
	}
	function onFail(error) {
		thread.fail=error.message;
	}
}