/**
 * I'm a test component
 * @hint doc comments inside a function should be ignored!
 */
component name='testComponent' {
	function testBadDocComment() {	
		/** If you delete this comment, all will be well! */
		lock name='test' timeout=1 type="exclusive" {
			return true;
		} 
	}

	function testGoodDocComment() {
		/* I'm ok, as i'm on the second line */
		timer {
			return true;
		}
	}

	function testNoDocComment() {
		lock name='test' timeout=1 type="exclusive" {
			return true;
		}
	}
}