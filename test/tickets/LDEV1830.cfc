component extends="org.lucee.cfml.test.LuceeTestCase"{
	

	public void function testSaveLoad(){
		local.y="abc";
		variables.z="def";

		local.x=function() {
			return y&z;
		}
		local.os=objectSave(x);
		local.o=objectLoad(os);

		assertEquals("abcdef",x());
		assertEquals("abcdef",o());
	}

	public void function testSaveLoadInner() {

		local.inner=function() {
			local.y="abc";
			variables.z="def";

			local.x=function() {
				return y&z;
			}
			local.os=objectSave(x);
			local.o=objectLoad(os);

			return o();
		}

		assertEquals("abcdef",inner());
	}

}