<cfsetting showdebugoutput="false"><cfscript>
ormReload();

echo("Array of Fruit objects:");
fruits = [
   new Fruit("banana"),
   new Fruit("kiwi"),
   new Fruit("apple")
];

fruits.each(function(fruit){
   echo(fruit.getName() & ";");
});

echo("Array of FruitEntity objects;");
fruits = [
   new FruitEntity("banana"),
   new FruitEntity("kiwi"),
   new FruitEntity("apple")
];

fruits.each(function(fruit){
   echo(fruit.getName() & ";");
});

echo("Basket object full of Fruit objects:");
basket = new Basket("My wonderful basket");
basket.addFruit(new Fruit("banana"));
basket.addFruit(new Fruit("kiwi"));
basket.addFruit(new Fruit("apple"));
basket.print();

echo("BasketEntity object full of FruitEntity objects:");
basket = new BasketEntity("My wonderful basket");
basket.addFruit(new FruitEntity("banana"));
basket.addFruit(new FruitEntity("kiwi"));
basket.addFruit(new FruitEntity("apple"));
basket.print();

entitySave(basket);

echo("BasketEntity object full of FruitEntity objects after saving:");
basket.print();

</cfscript>:fine:
