component {
	public any function init(){
		var instance.out = createObject( "java", "java.lang.System" ).out;

		return this;
	}
}