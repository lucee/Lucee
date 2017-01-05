component {
	public boolean function testA(){
		return this.testMethod();
	}

	public boolean function testB(){
		return testMethod();
	}

	private boolean function testMethod(){
		return true;
	}
}