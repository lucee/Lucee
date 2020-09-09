component extends="org.lucee.cfml.test.LuceeTestCase"	{

	function beforeAll() {
			employees = queryNew( 'name,age,email,department,isContract,yearsEmployed,sickDaysLeft,hireDate,isActive', 'varchar,integer,varchar,varchar,boolean,integer,integer,date,boolean', [
			[ 'John Doe',28,'John@company.com','Acounting',false,2,4,createDate(2010,1,21),true ],
			[ 'Jane Doe',22,'Jane@company.com','Acounting',false,0,8,createDate(2011,2,21),true ],
			[ 'Bane Doe',28,'Bane@company.com','Acounting',true,3,2,createDate(2012,3,21),true ],
			[ 'Tom Smith',25,'Tom@company.com','Acounting',false,6,4,createDate(2013,4,21),false ],
			[ 'Harry Johnson',38,'Harry@company.com','IT',false,8,6,createDate(2014,5,21),true ],
			[ 'Jason Wood',37,'Jason@company.com','IT',false,19,4,createDate(2015,6,21),true ],
			[ 'Doris Calhoun',67,'Doris@company.com','IT',true,3,6,createDate(2016,7,21),true ],
			[ 'Mary Root',17,'Mary@company.com','IT',false,8,2,createDate(2017,8,21),true ],
			[ 'Aurthur Duff',23,'Aurthur@company.com','IT',false,4,0,createDate(2018,9,21),true ],
			[ 'Luis Hake',29,'Luis@company.com','IT',true,9,5,createDate(2019,10,21),true ],
			[ 'Gavin Bezos',46,'Gavin@company.com','HR',false,2,5,createDate(2020,11,21),false ],
			[ 'Nancy Garmon',57,'Nancy@company.com','HR',false,14,9,createDate(2005,12,21),true ],
			[ 'Tom Zuckerburg',27,'Tom@company.com','HR',true,16,10,createDate(2006,1,21),true ],
			[ 'Richard Gates',62,'Richard@company.com','Executive',false,11,1,createDate(2007,2,21),true ],
			[ 'Amy Merryweather',58,'Amy@company.com','Executive',false,12,2,createDate(2008,3,21),true ]
			] );

	}

	function run( testResults , testBox ) {

		describe( 'QofQ' , function(){

			it( 'Can select *' , function() {				
				actual = QueryExecute(
					sql = "SELECT * FROM employees",
					options = { dbtype: 'query' }
				);
				expect( actual ).toBeQuery();
				expect( actual.recordcount ).toBe( employees.recordcount );
			});

			it( 'Can select with functions' , function() {				
				actual = QueryExecute(
					sql = "SELECT upper(name), lower(email), coalesce( null, age ) FROM employees",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( employees.recordcount );
			});
			
				
			it( 'test' , function() {				
				actual = QueryExecute(
					sql = "SELECT yearsEmployed/sickDaysLeft as calc from employees",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 15 );
			});

			it( 'Can select with functions that are aliased' , function() {				
				actual = QueryExecute(
					sql = "SELECT upper(name) as name, lower(email) email, coalesce( null, age ) as foo FROM employees",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( employees.recordcount );
			});
				
			it( 'Can select with order bys' , function() {				
				actual = QueryExecute(
					sql = "SELECT * from employees ORDER BY department, isActive desc, name, email",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 15 );
			});
				
			it( 'Can order by alias' , function() {				
				actual = QueryExecute(
					sql = "SELECT department as dept from employees ORDER BY dept",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 15 );
			});
				
			it( 'Can order by literal' , function() {				
				actual = QueryExecute(
					sql = "SELECT department as dept from employees ORDER BY 'test', 'foo', 7, false",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 15 );
			});
				
			it( 'Can order by columns not in select' , function() {				
				actual = QueryExecute(
					sql = "SELECT department from employees ORDER BY department, isActive desc, name, email",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 15 );
			});
				
			it( 'Can have extra whitespace in group by and order by clauses' , function() {
				actual = QueryExecute(
					sql = "SELECT department from employees group       by department ORDER       BY department",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 4 );
			});
				
			it( 'Can filter on date column' , function() {
				actual = QueryExecute(
					sql = "SELECT * from employees where hireDate = '2019-10-21 00:00:00.000'",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 1 );
				expect( actual.name).toBe( 'Luis Hake' );
			});

			describe( 'Distinct' , function(){
		
				it( 'Can select distinct' , function() {				
					actual = QueryExecute(
						sql = "SELECT distinct department FROM employees",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 4 );
				});
		
				it( 'Can select distinct with order by' , function() {				
					actual = QueryExecute(
						sql = "SELECT distinct department FROM employees order by department",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 4 );
				});
	
				it( 'Can select distinct with top' , function() {				
					actual = QueryExecute(
						sql = "SELECT top 2 distinct department as foo FROM employees",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 2 );
				});
	
				it( 'Can select distinct with maxrows' , function() {				
					actual = QueryExecute(
						sql = "SELECT distinct department FROM employees",
						options = { dbtype: 'query', maxrows: 2 }
					);
					expect( actual.recordcount ).toBe( 2 );
				});
	
				it( 'Can select distinct with maxrows' , function() {				
					actual = QueryExecute(
						sql = "SELECT distinct department FROM employees",
						options = { dbtype: 'query', maxrows: 2 }
					);
					expect( actual.recordcount ).toBe( 2 );
				});
	
				it( 'Can select distinct with *' , function() {				
					actual = QueryExecute(
						sql = "SELECT distinct * FROM employees",
						options = { dbtype: 'query'}
					);
					expect( actual.recordcount ).toBe( employees.recordcount );
				});
				
			});

			describe( 'Query Union' , function(){
				
					it( 'Can union' , function() {				
					actual = QueryExecute(
						sql = "SELECT * FROM employees
							union
							SELECT * FROM employees",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 15 );
				});
				
				it( 'Can union all' , function() {				
					actual = QueryExecute(
						sql = "SELECT * FROM employees
							union all
							SELECT * FROM employees",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 30 );
				});
				
				it( 'Can union distinct' , function() {				
					actual = QueryExecute(
						sql = "SELECT * FROM employees
							union distinct
							SELECT * FROM employees",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 15 );
				});
				
				it( 'Can union with top' , function() {				
					actual = QueryExecute(
						sql = "SELECT top 2 * FROM employees
							union all
							SELECT top 3 * FROM employees",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 5 );
				});
				
				it( 'Can union with maxrows' , function() {				
					actual = QueryExecute(
						sql = "SELECT * FROM employees
							union all
							SELECT * FROM employees",
						options = { dbtype: 'query', maxrows: 2  }
					);
					expect( actual.recordcount ).toBe( 2 );
				});
				
				it( 'Can union with order' , function() {				
					actual = QueryExecute(
						sql = "SELECT * FROM employees
							union
							SELECT * FROM employees
							order by department, name desc",
						options = { dbtype: 'query'  }
					);
					expect( actual.recordcount ).toBe( 15 );
				});
				
				it( 'Can union with group by' , function() {				
					actual = QueryExecute(
						sql = "SELECT department as thing, count(1) as count, max(age) as age FROM employees
							GROUP BY department
							union all
							SELECT age, count(*), min(sickDaysLeft) FROM employees
							GROUP BY age
							ORDER BY thing",
						options = { dbtype: 'query'  }
					);
					expect( actual.recordcount ).toBe( 18 );
				});
				
				it( 'Can union with literals' , function() {				
					actual = QueryExecute(
						sql = "SELECT TOP 1 'brad' as firstname, 'wood' as lastname FROM employees
							union all SELECT TOP 1 'Scott', 'Steinbeck' FROM employees
							union all SELECT TOP 1 'Gavin', 'Pickin' FROM employees
							union all SELECT TOP 1 'Luis', 'Majano' FROM employees
							",
						options = { dbtype: 'query'  }
					);
					expect( actual.recordcount ).toBe( 4 );
				});
				
			});

			describe( 'Query grouping' , function(){
				
				it( 'Can use aggregates with no group by' , function() {				
					actual = QueryExecute(
						sql = "SELECT avg(age) as avgAge, count(num) as totalEmps, max(hireDate) as mostRecentHire, min(sickDaysLeft) as fewestSickDays FROM employees",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 1 );
					expect( actual.avgAge ).toBe( 37.6 );
					expect( actual.totalEmps ).toBe( 15 );
					expect( actual.mostRecentHire ).toBe( "{ts '2020-11-21 00:00:00'}" );
					expect( actual.fewestSickDays ).toBe( 0 );
				});
				
				it( 'Can use aggregates with no group by and where' , function() {				
					actual = QueryExecute(
						sql = "SELECT sum(age) sumAge FROM employees where department = 'IT'",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 1 );
					expect( actual.sumAge ).toBe( 211 );
				});
				
				it( 'Can use group by' , function() {				
					actual = QueryExecute(
						sql = "SELECT department as dept FROM employees GROUP BY department",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 4 );
				});
				
				it( 'Can use group by with distinct' , function() {				
					actual = QueryExecute(
						sql = "SELECT distinct department as dept FROM employees GROUP BY department",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 4 );
				});
				
				it( 'Can use group by with aggregates' , function() {				
					actual = QueryExecute(
						sql = "SELECT department as dept, max(hireDate) as mostRecentHire, min(age) as youngestAge, max( email ) FROM employees GROUP BY department",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 4 );
				});
				
				it( 'Can use group by with more than one group by' , function() {				
					actual = QueryExecute(
						sql = "SELECT department, isContract, isActive FROM employees GROUP BY department, isContract, isActive ORDER BY department, isContract, isActive",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 9 );
				});
				
				it( 'Can use group by with having clause' , function() {				
					actual = QueryExecute(
						sql = "SELECT department, max(age) as maxAge from employees GROUP BY department HAVING max(age) > 30 ORDER BY department",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 3 );
				});
				
				it( 'Can use group by with having clause and distinct' , function() {				
					actual = QueryExecute(
						sql = "SELECT department, max(age) as maxAge from employees GROUP BY department HAVING max(age) > 30 ORDER BY department",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 3 );
				});
				
				it( 'Can use group by with operations' , function() {				
					actual = QueryExecute(
						sql = "SELECT upper(department) as upperDept from employees GROUP BY upper(department) HAVING max(age) > 30 ORDER BY upper(department)",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 3 );
				});
				
				it( 'Can use group by with columns not in select' , function() {				
					actual = QueryExecute(
						sql = "SELECT 'test' as val from employees GROUP BY department, age, lower(email) ORDER BY upper(department)",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 15 );
				});
				
				it( 'Can order by aggregate columns' , function() {				
					actual = QueryExecute(
						sql = "SELECT department, max(age) as maxAge from employees GROUP BY department HAVING max(age) > 30 ORDER BY max(age)",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 3 );
				});
				
				it( 'Can reference more than one column in aggregate function' , function() {				
					actual = QueryExecute(
						sql = "SELECT department, sum(yearsEmployed * sickDaysLeft) as calc from employees GROUP BY department order by department",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 4 );
					expect( actual.calc[1] ).toBe( 38 );
					expect( actual.calc[2] ).toBe( 35 );
					expect( actual.calc[3] ).toBe( 296 );
					expect( actual.calc[4] ).toBe( 203 );
				});
				
				it( 'Can wrap aggregate function in scalar function' , function() {				
					actual = QueryExecute(
						sql = "SELECT floor(sum(yearsEmployed * sickDaysLeft)) as calc from employees group by department order by department",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 4 );
					expect( actual.calc[1] ).toBe( 38 );
					expect( actual.calc[2] ).toBe( 35 );
					expect( actual.calc[3] ).toBe( 296 );
					expect( actual.calc[4] ).toBe( 203 );
				});
				
				it( 'Can nest scalar functions inside of aggregates inside of scalar functions and use more than aggregate in a single operation' , function() {				
					actual = QueryExecute(
						sql = "SELECT department, max( yearsEmployed ) as max, count(1) as count, ceiling( max( floor( yearsEmployed ) )+count(1) )  as calc from employees group by department order by department",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 4 );
					expect( actual.calc[1] ).toBe( 10 );
					expect( actual.calc[2] ).toBe( 14 );
					expect( actual.calc[3] ).toBe( 19 );
					expect( actual.calc[4] ).toBe( 25 );
				});
				
			});

			
		});

	}
	
	
} 