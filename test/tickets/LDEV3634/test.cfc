component accessors=true {
	property name="sum" type="string";

	public String function getSum() {
		return "From custom getter function";
	}
}