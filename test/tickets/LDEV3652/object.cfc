component accessors=true
{
	property name="number1" type=numeric getter="false";
	property name="number2" type=numeric;
    property name="number3" type=numeric remotingFetch="false";
	property name="sum" type=numeric;

	public numeric function getSum() {
		return variables.number1 + variables.number2 + variables.number3;
	}
}