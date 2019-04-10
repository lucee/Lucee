component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1841", function() {
			it( title='Checking Asynchronous programming with normal function call', body=function( currentSpec ) {
				getAccountBalance = function(){
					var balance = 120000;
					return balance;
				}

				function payCreditCardBill(accountBalance){
					var ccBill = 1890;
					return accountBalance-ccBill;
				}

				payEMIs = function(accountBalance){
					var mortgageEMI = 1000;
					var carLeaseEMI = 750;
					var healthInsuranceEMI = 250;
				    return accountBalance-(mortgageEMI+carLeaseEMI+healthInsuranceEMI);
				}

				miscellenousExpenses = function(accountBalance){
					var shopping = 1500;
					var clubExpense  =1000;
					var casinoExpense = 2000;
					return accountBalance-(shopping+clubExpense+casinoExpense);
				}

				checkBalance = function(accountBalance){
					while(accountBalance > 5000){
						accountBalance = miscellenousExpenses(accountBalance);
				    }
				    if(accountBalance < 5000)
				    	throw (message="Account balance below threshold!!!", type="info");
				}

				errorHandler = function(error){
					if(error.message contains "Account balance below threshold!"){
					 return "You have reached your spending limit!";
					}
				}

				future = runAsync(getAccountBalance).then(payCreditCardBill).then(payEMIs).then(miscellenousExpenses);
				assertEquals(false , future.isCancelled());
				assertEquals(111610, future.get());
				assertEquals(true, future.isDone());
				assertEquals(111610, future.get(6000));
				future = runAsync(getAccountBalance).then(checkBalance).error(errorHandler);
				assertEquals("You have reached your spending limit!", future.get());
			});

			it( title='Checking Asynchronous programming with UDF', body=function( currentSpec ) {
				function add(){
					return 10+20;
				}
				Future = runAsync(add);
				assertEquals(30, Future.get());
				assertEquals(true, Future.isDone());
				assertEquals(false, Future.isCancelled());
			});

			xit( title='Checking Asynchronous programming with Empty future', body=function( currentSpec ) {
				// p = runAsync(); // empty future
				// p.complete(10);
				// assertEquals( false, p.isCancelled()); // no
				// assertEquals( false, p.cancel()); // no
				// assertEquals( 10, p.get()); // 10
				// assertEquals( true, p.isdone()); // yes

			});
		});
	}
}