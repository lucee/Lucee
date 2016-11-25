component accessors="true" implements="Irate" {
	property name="Rate" default="1";
	property name="CurrencyCode" default="2";

	public function init(Rate,CurrencyCode){
		this.setRate(Rate);
		this.setCurrencyCode(CurrencyCode);
		return this;
	}
	
}