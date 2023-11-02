component name='testComponent' {
	function testLock() {
		/** If you delete this comment, all will be well! */
		lock name='test' timeout=1 type="exclusive" {
			return true;
		}
	}

	function testoneLock() {
		lock name='test' timeout=1 type="exclusive" {
			return true;
		}
	}
}