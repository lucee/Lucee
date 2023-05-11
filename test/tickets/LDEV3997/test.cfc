component {
	public any function init(){
		instance.out = createObject( "java", "java.lang.System" ).out;

		return this;
	}
}