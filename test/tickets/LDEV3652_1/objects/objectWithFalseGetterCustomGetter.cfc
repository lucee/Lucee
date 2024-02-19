component accessors=true
{
	property name="name" type="string";
	property name="password" type="string" getter="false";

	public string function getPassword() {
		return "Not Available";
	}
}