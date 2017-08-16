component accessors="true" {
   property name;

   function init(string name) {
      setName(arguments.name);
      variables.fruits = [];

   }

   function addFruit(Fruit fruit) {
      variables.fruits.append(arguments.fruit);

   }

   function print() {
      echo(getName() & ";");

      variables.fruits.each(function(fruit){
         echo(fruit.getName() & ";");
      });

   }

}
