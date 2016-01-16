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

	public function setUp(){
	}

	public void function test(){
		var textAscii="test";
		var textNonAscii=chr(228)&chr(25104)&chr(1604)&chr(1506);
		
		
		// same base testing
		assertEquals(hash(textAscii),"098F6BCD4621D373CADE4E832627B4F6");
		assertEquals(hash(textAscii,"MD5"),"098F6BCD4621D373CADE4E832627B4F6");
		assertEquals(hash(textAscii,"SHA"),"A94A8FE5CCB19BA61C4C0873D391E987982FBBD3");
		assertEquals(hash(textAscii,"SHA-256"),"9F86D081884C7D659A2FEAA0C55AD015A3BF4F1B2B0B822CD15D6C15B0F00A08");
		assertEquals(hash(textAscii,"SHA-384"),"768412320F7B0AA5812FCE428DC4706B3CAE50E02A64CAA16A782249BFE8EFC4B7EF1CCB126255D196047DFEDF17A0A9");
		assertEquals(hash(textAscii,"SHA-512"),"EE26B0DD4AF7E749AA1A8EE3C10AE9923F618980772E473F8819A5D4940E0DB27AC185F8A0E1D5F84F88BC887FD67B143732C304CC5FA9AD8E6F57F50028A8FF");
		
		assertEquals(hash(textNonAscii),"08DE4E43F1B378DD0A40B79C82BC75E1");
		assertEquals(hash(textNonAscii,"MD5"),"08DE4E43F1B378DD0A40B79C82BC75E1");
		assertEquals(hash(textNonAscii,"SHA"),"39102F7C61C3170F968049A34FCCF5407CC97A3B");
		assertEquals(hash(textNonAscii,"SHA-256"),"9390614023FC1E4024BF8ED95D3F27933685E4118C04053A067077FFF053626C");
		assertEquals(hash(textNonAscii,"SHA-384"),"1E87795A63CE46ED09B0636B73BE7F6D6B657BA3041DBD228708F982D77CE8DF8A9D53722BE55D8E4B745E8B766A351E");
		assertEquals(hash(textNonAscii,"SHA-512"),"7315BB4B0A439EE7BAB1EAF7946203FC767720732A29BFFDB07F591250D2A071638485E7F2891AFAAA082EF1E88EF8E017C5ED87E588F71AB966C66D10D43807");
		
		
		assertEquals(hash(textNonAscii,"MD5","UTF-8"),"08DE4E43F1B378DD0A40B79C82BC75E1");
		assertEquals(hash(textNonAscii,"SHA","UTF-8"),"39102F7C61C3170F968049A34FCCF5407CC97A3B");
		assertEquals(hash(textNonAscii,"SHA-256","UTF-8"),"9390614023FC1E4024BF8ED95D3F27933685E4118C04053A067077FFF053626C");
		assertEquals(hash(textNonAscii,"SHA-384","UTF-8"),"1E87795A63CE46ED09B0636B73BE7F6D6B657BA3041DBD228708F982D77CE8DF8A9D53722BE55D8E4B745E8B766A351E");
		assertEquals(hash(textNonAscii,"SHA-512","UTF-8"),"7315BB4B0A439EE7BAB1EAF7946203FC767720732A29BFFDB07F591250D2A071638485E7F2891AFAAA082EF1E88EF8E017C5ED87E588F71AB966C66D10D43807");
		
		
		assertEquals(hash(textNonAscii,"MD5","iso-8859-1"),"23B1CAD4E29CB02246982114424E15AB");
		
		
		
		// users example
		var salt="[h{cH9}o'2~GMpn/K&?3zn`YGYxX=^AZ(Zs17Wz2X4>(F)4G-=c,<tGK3dAps1I=+5b4Cws?FZ6Nr2+[.Q!FjpO.`t':%d(!$ONgP[]AZkTdH]|$h:;HHQ[~Xps|9<zAaP}fK?7`Dc}Ko,{.zlQ@9w6ecN`ZK{JiQW1}1yX'`3'`^f|$GW}`;gcJn 3jq:`Y@k_s_>@ ,&Ulo=##I[B||'@lgUxNK7X[Ekz$G*iAcE4QuY!-PQQk/GB:o GI[SA<]";
		var result=convertPlainTextToSecurePassword("test",salt);
		assertEquals(result,"fbcc17f3993e0c68e6d58f02ae852d0af09025c0b6674786f8c6ba257e96946b0eccc6b093a20711c491740fff0433db7090485660c3fc5675f3bf068f44de4cde3f15c5");
	}
	
	
	
	private function convertPlainTextToSecurePassword(required string password, string salt) {
	
        var i=0;
		var local=structnew();
		
		local.hashStruct={
			passwordPlusSalt=insert(arguments.password,arguments.salt, 128),
			algorithms=[{
				encoding='iso-8859-1',
				algorithm='MD5'
			},
			{
				encoding='utf-8',
				algorithm='SHA'
			},
			{
				encoding='utf-8',
				algorithm='SHA-256'
			}]
		}	
		
		local.storedPasswordValue="";
		for(i=1;i LTE arraylen(local.hashStruct.algorithms);i++){
			local.storedPasswordValue&=hash(local.hashStruct.passwordPlusSalt, local.hashStruct.algorithms[i].algorithm, local.hashStruct.algorithms[i].encoding, 50000);
		}
		return local.storedPasswordValue;
	}
} 
</cfscript>