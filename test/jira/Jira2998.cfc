<!--- 
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{

	public void function testUCase(){
		assertEquals(asc("A"),asc("a".ucase()));
	}

	public void function testLCase(){
		assertEquals(asc("a"),asc("A".lcase()));
	}

	public void function testTrim(){
		assertEquals("a"," a ".trim());
	}
	public void function testLTrim(){
		assertEquals("a "," a ".ltrim());
	}
	public void function testRTrim(){
		assertEquals(" a"," a ".rtrim());
	}

	public void function testLeft(){
		assertEquals("abc","abcdefg".left(3));
	}
	public void function testRight(){
		assertEquals("efg","abcdefg".right(3));
	}

	public void function testLJustify(){
		assertEquals("abc       ","abc".lJustify(10));
	}
	public void function testCJustify(){
		assertEquals("   abc    ","abc".cJustify(10));
	}
	public void function testRJustify(){
		assertEquals("       abc","abc".rJustify(10));
	}

	public void function testMid(){
		assertEquals("bcdefghijkl","abcdefghijkl".mid(2));
		assertEquals("bcd","abcdefghijkl".mid(2,3));
	}
	public void function testLen(){
		assertEquals(3,"abc".len());
		assertEquals(0, [].len());
		assertEquals(0, {}.len());
		assertEquals(0, {}.len());
		assertEquals(3, ["abc", "xyz", 1].len());
		assertEquals(3, {a=1, b=2, c=3}.len());
		assertEquals(3, [a=1, b=2, c=3].len());
		local.a = [];
		arrayResize(local.a, 3)
		$assert.isEqual(3, local.a.len());
	}
	public void function testRemoveChars(){
		assertEquals("aefghijklm","abcdefghijklm".RemoveChars(2,3));
	}

	public void function testCompare(){
		assertEquals(0,"abc".compare("abc"));
		assertEquals(1,"abc".compare("ABC"));
		assertEquals(-1,"abb".compare("abc"));
		assertEquals(1,"abd".compare("abc"));
	}

	public void function testCompareNoCase(){
		assertEquals(0,"abc".CompareNoCase("ABC"));
		assertEquals(-1,"abb".CompareNoCase("abc"));
		assertEquals(1,"abd".CompareNoCase("abc"));
	}

	public void function testRepeatString(){
		assertEquals("abcabcabc","abc".RepeatString(3));
	}

	public void function testReplace(){
		assertEquals("12cabc","abcabc".replace("ab","12"));
		assertEquals("abcabc","abcabc".replace("AB","12"));
		assertEquals("12c12c","abcabc".replace("ab","12","all"));
		assertEquals("123123","abcabc".replace({'abc':'123'}));
	}

	public void function testReplaceNoCase(){
		assertEquals("12cabc","abcabc".replaceNoCase("ab","12"));
		assertEquals("12cabc","abcabc".replaceNoCase("AB","12"));
		assertEquals("12c12c","abcabc".replaceNoCase("ab","12","all"));
		assertEquals("123123","abcabc".replaceNoCase({'abc':'123'}));
	}

	public void function testWrap(){
		assertEquals("ab
c","abc".wrap(2));
	}

	public void function testSpanExcluding(){
		assertEquals("yyy","yyysss".spanExcluding("s"));
	}

	public void function testSpanIncluding(){
		assertEquals("mystr","mystring".spanIncluding("mystery"));
	}

	public void function testReverse(){
		assertEquals("cba","abc".reverse());
	}

	public void function testStripCR(){
		assertEquals("abc","a#chr(13)#b#chr(13)#c".stripCR());
	}

	public void function testFind(){
		assertEquals(3,"abcdefabcdef".find("cd"));
		assertEquals(9,"ABCDEFabcdef".find("cd"));
		assertEquals(9,"abcdefabcdef".find("cd",4));
	}

	public void function testFindNoCase(){
		assertEquals(3,"abcdefabcdef".findNoCase("cD"));
		assertEquals(9,"abcdefabcdef".findNoCase("cD",4));
	}

	public void function testREFind(){
		assertEquals(4,"abcaaccdd".REFind("a+c+"));
		assertEquals(1,"abcaaccdd".REFind("a+c*"));
		assertEquals(0,"abcaaccdd".REFind("A+C+"));
	}

	public void function testREFindNoCase(){
		assertEquals(4,"abcaaccdd".REFindNoCase("a+c+"));
		assertEquals(1,"abcaaccdd".REFindNoCase("a+c*"));
		assertEquals(4,"abcaaccdd".REFindNoCase("A+C+"));
	}

	public void function testInsert(){
		assertEquals("aabbbbaaa","aaaaa".insert("bbbb",2));
	}


	public void function testGetToken(){
		assertEquals("b","a b c".getToken(2));
		assertEquals("b","a b c".getToken(2,' '));
	}


	public void function testREMatch(){
		var string="Hallo https://www.lucee.org Susi";
		var regex="https?://([-\w\.]+)+(:\d+)?(/([\w/_\.]*(\?\S+)?)?)?";
		assertEquals(REMatch(regex, string),string.REMatch(regex));
	}
	public void function testREMatchNoCase(){
		var string="Hallo https://www.lucee.org Susi";
		var regex="https?://([-\w\.]+)+(:\d+)?(/([\w/_\.]*(\?\S+)?)?)?";
		assertEquals(REMatchNoCase(regex, string),string.REMatchNoCase(regex));
	}


	public void function testREReplace(){
		assertEquals("GAGARET","CABARET".REReplace("C|B","G","ALL"));
	}
	public void function testREReplaceNoCase(){
		assertEquals("GAGARET","CABARET".REReplaceNoCase("C|B","G","ALL"));
	}
	public void function testUCFirst(){
		assertEqualsCase("Susi","susi".ucFirst());
		assertEqualsCase("Susi Sorglos","susi sorglos".ucFirst(true));
		assertEqualsCase("Susi sorglos","susi sorglos".ucFirst(false));
		assertEqualsCase("SORGLOS","SORGLOS".ucFirst(true,false));
		assertEqualsCase("Sorglos","SORGLOS".ucFirst(true,true));
	}
	public void function testASC(){
		assertEquals(97,"a".asc());
		assertEquals(97,"abc".asc(1));
		assertEquals(99,"abc".asc(3));
	}

	/*
	LIST MEMBER FUNCTIONS
	
	public void function testAppend(){
		assertEquals("a,b","a".append('b'));
	}
	public void function testAvg(){
		assertEquals(2,"1,2,3".avg());
		assertEquals(2,"1,2,3".avg(','));
	}
	public void function testChangeDelims(){
		assertEquals("1;2;3","1,2,3".ChangeDelims(';'));
	}
	public void function testCompact(){
		assertEquals("1,2,3",",,,1,2,3,,,".compact());
	}
	public void function testContains(){
		assertEquals(2,"a,bb,ccc".contains('bb'));
		assertEquals(0,"a,bb,ccc".contains('BB'));
	}
	public void function testContainsNoCase(){
		assertEquals(2,"a,bb,ccc".containsNoCase('bb'));
		assertEquals(2,"a,bb,ccc".containsNoCase('BB'));
		assertEquals(0,"a,bb,ccc".containsNoCase('dd'));
	}
	public void function testDeleteAt(){
		assertEquals("a,ccc","a,bb,ccc".deleteAt(2));
	}
	public void function testListFind(){
		assertEquals(2,"a,bb,ccc".listfind("bb"));
		assertEquals(0,"a,bb,ccc".listfind("BB"));
	}
	public void function testListFindNoCase(){
		assertEquals(2,"a,bb,ccc".listfindNoCase("bb"));
		assertEquals(2,"a,bb,ccc".listfindNoCase("BB"));
		assertEquals(0,"a,bb,ccc".listfindNoCase("ddd"));
	}
	public void function testFirst(){
		assertEquals("a","a,bb,ccc".first());
	}
	public void function testLast(){
		assertEquals("ccc","a,bb,ccc".last());
	}
	public void function testGetAt(){
		assertEquals("bb","a,bb,ccc".getAt(2));
	}
	public void function testIndexExists(){
		assertEquals(true,"a,bb,ccc".IndexExists(2));
	}
	public void function testInsertAt(){
		assertEquals("a,dddd,bb,ccc","a,bb,ccc".InsertAt(2,"dddd"));
	}
	public void function testItemTrim(){
		assertEquals("a,bb,ccc","a , bb , ccc   ".ItemTrim());
	}
	public void function testListlen(){
		assertEquals(3,"a,bb,ccc".listLen());
	}
	public void function testPrepend(){
		assertEquals("zzz,a,bb,ccc","a,bb,ccc".prepend('zzz'));
	}
	public void function testqualify(){
		assertEquals("*a*,*bb*,*ccc*","a,bb,ccc".qualify('*'));
	}
	public void function testRemoveDuplicates(){
		assertEquals("a,bb,ccc","a,bb,bb,ccc".RemoveDuplicates());
	}
	public void function testRest(){
		assertEquals("bb,ccc","a,bb,ccc".rest());
	}
	public void function testSetAt(){
		assertEquals("a,bbb,ccc","a,bb,ccc".setAt(2,'bbb'));
	}
	public void function testSort(){
		assertEquals("ccc,bb,a","a,bb,ccc".sort('text','desc'));
	}
	public void function testToArray(){
		assertEquals(['a','bb','ccc'],"a,bb,ccc".toArray());
	}
	public void function testValueCount(){
		assertEquals(1,"a,bb,ccc".valueCount('bb'));
		assertEquals(0,"a,bb,ccc".valueCount('BB'));
	}
	public void function testValueCountNoCase(){
		assertEquals(1,"a,bb,ccc".valueCountNoCase('bb'));
		assertEquals(1,"a,bb,ccc".valueCountNoCase('BB'));
	}
*/



	
} 
</cfscript>
