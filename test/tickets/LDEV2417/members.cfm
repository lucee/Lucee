<cfscript>
	phones = [ { price: 100 }, { price: 200 }, { price: 300 } ];
	prices = phones.map( phone => phone.price );
	expensive = prices.filter( price => price > 150 );
	total = prices.reduce( ( sum, price ) => sum + price, 0 );
	writeOutput( arrayToList( prices ) & "|" & arrayToList( expensive ) & "|" & total );
</cfscript>
