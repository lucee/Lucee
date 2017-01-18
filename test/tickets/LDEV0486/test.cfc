component {
	public boolean function testA(){
		return this.testMethod();
	}

	public boolean function testB(){
		return testMethod();
	}

	public test function getThis(){
		return this;
	}

	private boolean function testMethod(){
		return true;
	}
}