component accessors="true" {

    property name="a" default="1";
    property name="b" default="1";
    property name="c" default="1";
    property name="d" default="1";

    public function init(){
        return this;
    }

	public function geta(){
        return variables.a;
    }

	public function getb(){
        return variables.b;
    }

	public function getc(){
        return variables.c;
    }

	public function getd(){
        return variables.d;
    }

	public function seta(required any a=1){
        variables.a = arguments.a;
    }

	public function setb(required any b=1){
        variables.b = arguments.b;
    }

	public function setc(required any c=1){
        variables.c = arguments.c;
    }

	public function setd(required any d=1){
        variables.d = arguments.d;
    }

}