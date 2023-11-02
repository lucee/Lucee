component output="false" accessors="true" persistent="true" entityname="entityTest1" {
	this.test.unverified = 0;
	this.test.verified = 1;
	this.foo = 'Lucee';
	variables.bar = 'test';

	property name="testStatus" type="numeric" sqltype="tinyint" length="1" default="#this.test.verified#";

	public any function testfunc() {
		return "#this.foo##variables.bar#";
	}
}