component accessors="true" persistent="true" {
   property name="id" fieldtype="id" generator="increment";
   property name;
   property name="fruits" singularName="fruit" fieldtype="one-to-many" cfc="FruitEntity" fkcolumn="basketId" cascade="all" lazy="false" fetch="join";

   function init(string name) {
      setName(arguments.name);
      variables.fruits = [];

   }

   function print() {
      echo(getName() & ";");

      getFruits().each(function(fruit){
         echo(fruit.getName() & ";");
      });

   }

}
