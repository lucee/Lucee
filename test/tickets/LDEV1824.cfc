component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1824", function() {
			it( title='checking Numeric member function', body=function( currentSpec ) {
				var x=10;
				var y=0.5;
				var z=255;
				var k=90;
				var pi = 3.1415926535;
				assertEquals(10 , x.abs());
				assertEquals(1.0471975511965979 , y.aCos());
				assertEquals(0.5235987755982989, y.asin());
				assertEquals(0.4636476090008061 , y.atn());
				assertEquals(10 , x.bitAnd(14));
				assertEquals(31 , z.bitMaskClear( 5, 5));
				assertEquals(7 , z.bitMaskRead( 5, 5));
				assertEquals(1023 , z.bitMaskset( 255,5, 5));
				assertEquals(-256 , z.bitNot());
				assertEquals(255 , z.bitOR(0));
				assertEquals(8160, z.bitSHLN(5));
				assertEquals(250, z.bitXor(5));
				assertEquals(1, y.ceiling());
				assertEquals(-0.4480736161291702, k.cos());
				//assertEquals(22026.465794806718, x.exp());
				assertEquals(0 , y.fix());
				assertEquals(0, y.floor());
				assertEquals(1010, x.formatBaseN(2));
				assertEquals(2.302585092994046, x.log());
				assertEquals(1, x.log10());
				assertEquals(20 , x.max(20));
				assertEquals(10 , x.min(20));
				assertEquals(3 , pi.round());
				assertEquals(1, x.sgn());
				assertEquals(-0.5440211108893698, x.sin());
				assertEquals(3.1622776601683795, x.sqr());
				assertEquals(-1.995200412208242, k.tan());
				assertEquals(11, k.BitSHRN(3));
				//still not implemented
				//assertEquals(0.726130386579, k.randomize());
				//assertEquals(19, k.randRange(2));
			});
		});
	}
}