package lucee;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base32;

import lucee.commons.digest.Base64Encoder;
import lucee.runtime.coder.CoderException;
import lucee.runtime.functions.other.ToBinary;

public class Test {
	private static final String CS="ASCII";
	
	public static final int NEGOTIATE_UNICODE=0x1;			// Indicates that Unicode strings are supported for use in security buffer data.
	public static final int NEGOTIATE_OEM=0x2; 				// Indicates that OEM strings are supported for use in security buffer data.
	public static final int REQUEST_TARGET=0x4; 				// Requests that the server's authentication realm be included in the Type 2 message.
	public static final int NEGOTIATE_SIGN=0x10; 				// Specifies that authenticated communication between the client and server should carry a digital signature (message integrity).
	public static final int NEGOTIATE_SEAL=0x20;				// Specifies that authenticated communication between the client and server should be encrypted (message confidentiality).
	public static final int NEGOTIATE_DATAGRAM_STYLE=0x40;	// Indicates that datagram authentication is being used.
	public static final int NEGOTIATE_LAN_MANAGER_KEY=0x80;	// Indicates that the Lan Manager Session Key should be used for signing and sealing authenticated communications.
	public static final int NEGOTIATE_NETWARE=0x100;			// This flag's usage has not been identified.
	public static final int NEGOTIATE_NTLM=0x200;				// Indicates that NTLM authentication is being used.
	public static final int NEGOTIATE_ANONYMOUS=0x800;		// Sent by the client in the Type 3 message to indicate that an anonymous context has been established. This also affects the response fields (as detailed in the "Anonymous Response" section).
	public static final int NEGOTIATE_DOMAIN_SUPPLIED=0x1000;	// Sent by the client in the Type 1 message to indicate that the name of the domain in which the client workstation has membership is included in the message. This is used by the server to determine whether the client is eligible for local authentication.
	public static final int NEGOTIATE_WORKSTATION_SUPPLIED=0x2000;	// Sent by the client in the Type 1 message to indicate that the client workstation's name is included in the message. This is used by the server to determine whether the client is eligible for local authentication.
	public static final int NEGOTIATE_LOCAL_CALL=0x4000;		// Sent by the server to indicate that the server and client are on the same machine. Implies that the client may use the established local credentials for authentication instead of calculating a response to the challenge.
	public static final int NEGOTIATE_ALWAYS_SIGN=0x8000;		// Indicates that authenticated communication between the client and server should be signed with a "dummy" signature.
	public static final int TARGET_TYPE_DOMAIN=0x10000;		// Sent by the server in the Type 2 message to indicate that the target authentication realm is a domain.
	public static final int TARGET_TYPE_SERVER=0x20000;		// Sent by the server in the Type 2 message to indicate that the target authentication realm is a server.
	public static final int TARGET_TYPE_SHARE=0x40000;		// Sent by the server in the Type 2 message to indicate that the target authentication realm is a share. Presumably, this is for share-level authentication. Usage is unclear.
	public static final int NEGOTIATE_NTLM2_KEY=0x80000;		//Indicates that the NTLM2 signing and sealing scheme should be used for protecting authenticated communications. Note that this refers to a particular session security scheme, and is not related to the use of NTLMv2 authentication. This flag can, however, have an effect on the response calculations (as detailed in the "NTLM2 Session Response" section).
	public static final int REQUEST_INIT_RESPONSE=0x100000;	// This flag's usage has not been identified.
	public static final int REQUEST_ACCEPT_RESPONSE=0x200000;	// This flag's usage has not been identified.
	public static final int REQUEST_NOT_NT_SESSION_KEY=0x400000;// This flag's usage has not been identified.
	public static final int NEGOTIATE_TARGET_INFO=0x800000;	// Sent by the server in the Type 2 message to indicate that it is including a Target Information block in the message. The Target Information block is used in the calculation of the NTLMv2 response.
	public static final int NEGOTIATE_128=0x20000000;			// Indicates that 128-bit encryption is supported.
	public static final int NEGOTIATE_KEY_EXCHANGE=0x40000000;// Indicates that the client will provide an encrypted master key in the "Session Key" field of the Type 3 message.
	public static final int NEGOTIATE_56=0x80000000;			// Indicates that 56-bit encryption is supported.
	
	private static class Flags {
		public boolean NEGOTIATE_UNICODE;			// Indicates that Unicode strings are supported for use in security buffer data.
		public boolean NEGOTIATE_OEM; 				// Indicates that OEM strings are supported for use in security buffer data.
		public boolean REQUEST_TARGET; 				// Requests that the server's authentication realm be included in the Type 2 message.
		public boolean NEGOTIATE_SIGN; 				// Specifies that authenticated communication between the client and server should carry a digital signature (message integrity).
		public boolean NEGOTIATE_SEAL;				// Specifies that authenticated communication between the client and server should be encrypted (message confidentiality).
		public boolean NEGOTIATE_DATAGRAM_STYLE;	// Indicates that datagram authentication is being used.
		public boolean NEGOTIATE_LAN_MANAGER_KEY;	// Indicates that the Lan Manager Session Key should be used for signing and sealing authenticated communications.
		public boolean NEGOTIATE_NETWARE;			// This flag's usage has not been identified.
		public boolean NEGOTIATE_NTLM;				// Indicates that NTLM authentication is being used.
		public boolean NEGOTIATE_ANONYMOUS;		// Sent by the client in the Type 3 message to indicate that an anonymous context has been established. This also affects the response fields (as detailed in the "Anonymous Response" section).
		public boolean NEGOTIATE_DOMAIN_SUPPLIED;	// Sent by the client in the Type 1 message to indicate that the name of the domain in which the client workstation has membership is included in the message. This is used by the server to determine whether the client is eligible for local authentication.
		public boolean NEGOTIATE_WORKSTATION_SUPPLIED;	// Sent by the client in the Type 1 message to indicate that the client workstation's name is included in the message. This is used by the server to determine whether the client is eligible for local authentication.
		public boolean NEGOTIATE_LOCAL_CALL;		// Sent by the server to indicate that the server and client are on the same machine. Implies that the client may use the established local credentials for authentication instead of calculating a response to the challenge.
		public boolean NEGOTIATE_ALWAYS_SIGN;		// Indicates that authenticated communication between the client and server should be signed with a "dummy" signature.
		public boolean TARGET_TYPE_DOMAIN;		// Sent by the server in the Type 2 message to indicate that the target authentication realm is a domain.
		public boolean TARGET_TYPE_SERVER;		// Sent by the server in the Type 2 message to indicate that the target authentication realm is a server.
		public boolean TARGET_TYPE_SHARE;		// Sent by the server in the Type 2 message to indicate that the target authentication realm is a share. Presumably, this is for share-level authentication. Usage is unclear.
		public boolean NEGOTIATE_NTLM2_KEY;		//Indicates that the NTLM2 signing and sealing scheme should be used for protecting authenticated communications. Note that this refers to a particular session security scheme, and is not related to the use of NTLMv2 authentication. This flag can, however, have an effect on the response calculations (as detailed in the "NTLM2 Session Response" section).
		public boolean REQUEST_INIT_RESPONSE;	// This flag's usage has not been identified.
		public boolean REQUEST_ACCEPT_RESPONSE;	// This flag's usage has not been identified.
		public boolean REQUEST_NOT_NT_SESSION_KEY;// This flag's usage has not been identified.
		public boolean NEGOTIATE_TARGET_INFO;	// Sent by the server in the Type 2 message to indicate that it is including a Target Information block in the message. The Target Information block is used in the calculation of the NTLMv2 response.
		public boolean NEGOTIATE_128;			// Indicates that 128-bit encryption is supported.
		public boolean NEGOTIATE_KEY_EXCHANGE;// Indicates that the client will provide an encrypted master key in the "Session Key" field of the Type 3 message.
		public boolean NEGOTIATE_56;			// Indicates that 56-bit encryption is supported.
	}
	private static Flags toFlags(int flags) {
		Flags f=new Flags();
		if((NEGOTIATE_UNICODE&flags)>0)f.NEGOTIATE_UNICODE=true;
		if((NEGOTIATE_OEM&flags)>0)f.NEGOTIATE_OEM=true;
		if((REQUEST_TARGET&flags)>0)f.REQUEST_TARGET=true;
		if((NEGOTIATE_SIGN&flags)>0)f.NEGOTIATE_SIGN=true;
		if((NEGOTIATE_SEAL&flags)>0)f.NEGOTIATE_SEAL=true;
		if((NEGOTIATE_DATAGRAM_STYLE&flags)>0)f.NEGOTIATE_DATAGRAM_STYLE=true;
		if((NEGOTIATE_LAN_MANAGER_KEY&flags)>0)f.NEGOTIATE_LAN_MANAGER_KEY=true;
		if((NEGOTIATE_NETWARE&flags)>0)f.NEGOTIATE_NETWARE=true;
		if((NEGOTIATE_NTLM&flags)>0)f.NEGOTIATE_NTLM=true;
		if((NEGOTIATE_ANONYMOUS&flags)>0)f.NEGOTIATE_ANONYMOUS=true;
		if((NEGOTIATE_DOMAIN_SUPPLIED&flags)>0)f.NEGOTIATE_DOMAIN_SUPPLIED=true;
		if((NEGOTIATE_WORKSTATION_SUPPLIED&flags)>0)f.NEGOTIATE_WORKSTATION_SUPPLIED=true;
		if((NEGOTIATE_LOCAL_CALL&flags)>0)f.NEGOTIATE_LOCAL_CALL=true;
		if((NEGOTIATE_ALWAYS_SIGN&flags)>0)f.NEGOTIATE_ALWAYS_SIGN=true;
		if((TARGET_TYPE_DOMAIN&flags)>0)f.TARGET_TYPE_DOMAIN=true;
		if((TARGET_TYPE_SERVER&flags)>0)f.TARGET_TYPE_SERVER=true;
		if((TARGET_TYPE_SHARE&flags)>0)f.TARGET_TYPE_SHARE=true;
		if((NEGOTIATE_NTLM2_KEY&flags)>0)f.NEGOTIATE_NTLM2_KEY=true;
		if((REQUEST_INIT_RESPONSE&flags)>0)f.REQUEST_INIT_RESPONSE=true;
		if((REQUEST_ACCEPT_RESPONSE&flags)>0)f.REQUEST_ACCEPT_RESPONSE=true;
		if((REQUEST_NOT_NT_SESSION_KEY&flags)>0)f.REQUEST_NOT_NT_SESSION_KEY=true;
		if((NEGOTIATE_TARGET_INFO&flags)>0)f.NEGOTIATE_TARGET_INFO=true;
		if((NEGOTIATE_128&flags)>0)f.NEGOTIATE_128=true;
		if((NEGOTIATE_KEY_EXCHANGE&flags)>0)f.NEGOTIATE_KEY_EXCHANGE=true;
		if((NEGOTIATE_56&flags)>0)f.NEGOTIATE_56=true;
		return f;
	}
	private static void printFlags(int flags) {
		print.e("FLAGS:");
		Flags f = toFlags(flags);
		if(f.NEGOTIATE_UNICODE)print.e("- NEGOTIATE_UNICODE");
		if(f.NEGOTIATE_OEM)print.e("- NEGOTIATE_OEM");
		if(f.REQUEST_TARGET)print.e("- REQUEST_TARGET");
		if(f.NEGOTIATE_SIGN)print.e("- NEGOTIATE_SIGN");
		if(f.NEGOTIATE_SEAL)print.e("- NEGOTIATE_SEAL");
		if(f.NEGOTIATE_DATAGRAM_STYLE)print.e("- NEGOTIATE_DATAGRAM_STYLE");
		if(f.NEGOTIATE_LAN_MANAGER_KEY)print.e("- NEGOTIATE_LAN_MANAGER_KEY");
		if(f.NEGOTIATE_NETWARE)print.e("- NEGOTIATE_NETWARE");
		if(f.NEGOTIATE_NTLM)print.e("- NEGOTIATE_NTLM");
		if(f.NEGOTIATE_ANONYMOUS)print.e("- NEGOTIATE_ANONYMOUS");
		if(f.NEGOTIATE_DOMAIN_SUPPLIED)print.e("- NEGOTIATE_DOMAIN_SUPPLIED");
		if(f.NEGOTIATE_WORKSTATION_SUPPLIED)print.e("- NEGOTIATE_WORKSTATION_SUPPLIED");
		if(f.NEGOTIATE_LOCAL_CALL)print.e("- NEGOTIATE_LOCAL_CALL");
		if(f.NEGOTIATE_ALWAYS_SIGN)print.e("- NEGOTIATE_ALWAYS_SIGN");
		if(f.TARGET_TYPE_DOMAIN)print.e("- TARGET_TYPE_DOMAIN");
		if(f.TARGET_TYPE_SERVER)print.e("- TARGET_TYPE_SERVER");
		if(f.TARGET_TYPE_SHARE)print.e("- TARGET_TYPE_SHARE");
		if(f.NEGOTIATE_NTLM2_KEY)print.e("- NEGOTIATE_NTLM2_KEY");
		if(f.REQUEST_INIT_RESPONSE)print.e("- REQUEST_INIT_RESPONSE");
		if(f.REQUEST_ACCEPT_RESPONSE)print.e("- REQUEST_ACCEPT_RESPONSE");
		if(f.REQUEST_NOT_NT_SESSION_KEY)print.e("- REQUEST_NOT_NT_SESSION_KEY");
		if(f.NEGOTIATE_TARGET_INFO)print.e("- NEGOTIATE_TARGET_INFO");
		if(f.NEGOTIATE_128)print.e("- NEGOTIATE_128");
		if(f.NEGOTIATE_KEY_EXCHANGE)print.e("- NEGOTIATE_KEY_EXCHANGE");
		if(f.NEGOTIATE_56)print.e("- NEGOTIATE_56");
	}

	
	public static void main(String[] args) throws CoderException, UnsupportedEncodingException {
		
		/*{
		String msg="YHsGBisGAQUFAqBxMG+gMDAuBgorBgEEAYI3AgIKBgkqhkiC9xIBAgIGCSqGSIb3EgECAgYKKwYBBAGCNwICHqI7BDlOVExNU1NQAAEAAACXsgjiAwADADYAAAAOAA4AKAAAAAoA1zoAAAAPV1MtMTMwMDE1MzQyMTRBRFM=";
		print.e("----- 1 ------");
		print.e(base64ToByteArray(msg));
		print.e(base64ToString(msg));
		
		msg="oYIBAjCB/6ADCgEBoQwGCisGAQQBgjcCAgqigekEgeZOVExNU1NQAAIAAAAGAAYAOAAAABWCieKTKZDjhUDgPwAAAAAAAAAAqACoAD4AAAAGA4AlAAAAD0EARABTAAIABgBBAEQAUwABABQATQBTAFMAUwBQAEYARQBQADAAMQAEABwAYQBkAHMALgBoAGUAbAAuAGsAawBvAC4AYwBoAAMAMgBNAFMAUwBTAFAARgBFAFAAMAAxAC4AYQBkAHMALgBoAGUAbAAuAGsAawBvAC4AYwBoAAUAHABhAGQAcwAuAGgAZQBsAC4AawBrAG8ALgBjAGgABwAIAOYkU7f7adMBAAAAAA==";
		print.e("----- 2 ------");
		print.e(base64ToByteArray(msg));
		print.e(base64ToString(msg));
		
		msg="Negotiate oYICHTCCAhmgAwoBAaKCAfwEggH4TlRMTVNTUAADAAAAGAAYAIQAAABMAUwBnAAAAAYABgBYAAAACgAKAF4AAAAcABwAaAAAABAAEADoAQAAFYKI4goA1zoAAAAPzWqi//bkLR4mM+rivgonz0EARABTAEUAWAAyAEYAVwBXAFMALQAxADMAMAAwADEANQAzADQAMgAxADQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAwA1SU6SgmsmIUQlwbbwmZgEBAAAAAAAA5iRTt/tp0wFARxGqL2UbUQAAAAACAAYAQQBEAFMAAQAUAE0AUwBTAFMAUABGAEUAUAAwADEABAAcAGEAZABzAC4AaABlAGwALgBrAGsAbwAuAGMAaAADADIATQBTAFMAUwBQAEYARQBQADAAMQAuAGEAZABzAC4AaABlAGwALgBrAGsAbwAuAGMAaAAFABwAYQBkAHMALgBoAGUAbAAuAGsAawBvAC4AYwBoAAcACADmJFO3+2nTAQYABAACAAAACAAwADAAAAAAAAAAAAAAAAAgAAD0DbiPvTtWu82rcs3v+mU0uVl7N6ruE5HoFyxDaWD/9woAEAAAAAAAAAAAAAAAAAAAAAAACQAcAEgAVABUAFAALwBsAG8AYwBhAGwAaABvAHMAdAAAAAAAAAAAAAAAAAAI2/02QvJ2pX5Rn2y+3n6WoxIEEAEAAADWwGIXavUTmgAAAAA=";
		print.e("----- 3 ------");
		print.e(base64ToByteArray(msg));
		print.e(base64ToString(msg));
		}
		
		if(true) return;*/
		
		
		
		
		
		String str="TlRMTVNTUAABAAAAB7IIogMAAwA2AAAADgAOACgAAAAKANc6AAAAD1dTLTEzMDAxNTM0MjE0QURT";
		byte[] barr = Base64Encoder.decode(str);
	
		short domainLength=byteArrayToShort(barr, 16);
		short domainOffset=byteArrayToShort(barr, 20);
		String domain=new String(barr,domainOffset,domainLength,CS);
		short hostLength=byteArrayToShort(barr, 24);
		short hostOffset=byteArrayToShort(barr, 28);
		String host=new String(barr,hostOffset,hostLength,CS);
		
		// REQUEST_TARGET Requests that the server's authentication realm be included in the Type 2 message.
		// NEGOTIATE_ALWAYS_SIGN Indicates that authenticated communication between the client and server should be signed with a "dummy" signature.
		// NEGOTIATE_NTLM2_KEY Indicates that the NTLM2 signing and sealing scheme should be used for protecting authenticated communications. 
		//       Note that this refers to a particular session security scheme, and is not related to the use of NTLMv2 authentication. 
		//       This flag can, however, have an effect on the response calculations (as detailed in the "NTLM2 Session Response" section).
		// NEGOTIATE_128
		/*   struct {
        byte    protocol[8];     // 'N', 'T', 'L', 'M', 'S', 'S', 'P', '\0'
        byte    type;            // 0x01
        byte    zero[3];
        short   flags;           // 0xb203
        byte    zero[2];

        short   dom_len;         // domain string length
        short   dom_len;         // domain string length
        short   dom_off;         // domain string offset
        byte    zero[2];

        short   host_len;        // host string length
        short   host_len;        // host string length
        short   host_off;        // host string offset (always 0x20)
        byte    zero[2];

        byte    host[*];         // host string (ASCII)
        byte    dom[*];          // domain string (ASCII)
    } type-1-message*/
		
		print.e("++++++ MESSAGE 1++++++++");
		sub(barr,9,3);
		sub(barr,12,2);
		sub(barr,14,2);
		//toLong(barr, 12);
		//int i=byteArrayToInt(barr, 8);
		int fflags=toInt(barr, 12);
		print.e(fflags);
		printFlags(fflags);
		
		
		print.e("domain:"+domain+":"+domainOffset+":"+domainLength);
		print.e("host:"+host+":"+hostOffset+":"+hostLength);

		str=new String(barr,CS);
		print.e(barr);
		print.e(str);
		print.e((byte)0);
		
		
		print.e("++++++ MESSAGE 2.1++++++++");
		{
		String msg2="TlRMTVNTUAACAAAABgAGADgAAAAFgomiihwZBKEJ20UAAAAAAAAAAKgAqAA+AAAABgOAJQAAAA9BAEQAUwACAAYAQQBEAFMAAQAUAE0AUwBTAFMAUABGAEUAUAAwADIABAAcAGEAZABzAC4AaABlAGwALgBrAGsAbwAuAGMAaAADADIATQBTAFMAUwBQAEYARQBQADAAMgAuAGEAZABzAC4AaABlAGwALgBrAGsAbwAuAGMAaAAFABwAYQBkAHMALgBoAGUAbAAuAGsAawBvAC4AYwBoAAcACACAp27BCGrTAQAAAAA=";
		byte[] barr2 = Base64Encoder.decode(msg2);
		
		// Type 2 Indicator
		int type=toInt(barr2, 8);
		print.e("type:"+type);
		
		// Target Name Security Buffer:
		print.e("Target Name Security Buffer");
		short length=toShort(barr2, 12);
		short allocatedSpace=toShort(barr2, 14);
		int offset=toInt(barr2, 16);
		print.e("length:"+length);
		print.e("allocatedSpace:"+allocatedSpace);
		print.e("offset:"+offset);
		sub(barr2,offset,length);
		
		// Flags
		int flags=toInt(barr2, 20);
		print.e("flags:"+flags);
		//printFlags(flags);
		
		// Challenge
		sub(barr2,24,8);
		
		// Context
		sub(barr2,32,8);
		
		// Target Information Security Buffer:
		print.e("Target Information Security Buffer");
		short length2=toShort(barr2, 40);
		short allocatedSpace2=toShort(barr2, 42);
		int offset2=toInt(barr2, 44);
		print.e("length:"+length2);
		print.e("allocatedSpace:"+allocatedSpace2);
		print.e("offset:"+offset2);
		sub(barr2,offset2,length2);
		
		// Target Name Data
		sub(barr2,48,12);
		
		
		//sub(barr2,18,2);
		//sub(barr2,20,4);
		//sub(barr2,24,4);
		print.e(barr2);
        print.e(base64ToString(msg2));
		
        }
		//print.e( Base64Encoder.encode(hex2byteArray(hex)));

		
		
		print.e("++++++ MESSAGE 2++++++++");
		String msg2=create2Message(host, domain);
		print.e(msg2);
		print.e(Base64Encoder.decode(msg2));
		print.e(new String(Base64Encoder.decode(msg2)));
		
		
		msg2="TlRMTVNTUAACAAAAAAAAACgAAAABggAAVU9wgQTnlrgAAAAAAAAAAA==";
		print.e(msg2);
		print.e(Base64Encoder.decode("TlRMTVNTUAACAAAAAAAAACgAAAABggAAVU9wgQTnlrgAAAAAAAAAAA=="));
		print.e(new String(Base64Encoder.decode(msg2)));
		
		
		print.e("++++++ MESSAGE 3 ++++++++");
		{
		String msg3="TlRMTVNTUAACAAAABgAGADgAAAAFgomiihwZBKEJ20UAAAAAAAAAAKgAqAA+AAAABgOAJQAAAA9BAEQAUwACAAYAQQBEAFMAAQAUAE0AUwBTAFMAUABGAEUAUAAwADIABAAcAGEAZABzAC4AaABlAGwALgBrAGsAbwAuAGMAaAADADIATQBTAFMAUwBQAEYARQBQADAAMgAuAGEAZABzAC4AaABlAGwALgBrAGsAbwAuAGMAaAAFABwAYQBkAHMALgBoAGUAbAAuAGsAawBvAC4AYwBoAAcACACAp27BCGrTAQAAAAA=";
		byte[] barr3 = Base64Encoder.decode(msg3);
		
		// Type 2 Indicator
		int type=toInt(barr3, 8);
		print.e("type:"+type);
				
		
		
		
		
		/*
NTLMSSP Signature	Null-terminated ASCII "NTLMSSP" (0x4e544c4d53535000)
8	NTLM Message Type	long (0x03000000)
12	LM/LMv2 Response	security buffer
20	NTLM/NTLMv2 Response	security buffer
28	Target Name	security buffer
36	User Name	security buffer
44	Workstation Name	security buffer
(52)	Session Key (optional)	security buffer
(60)	Flags (optional)	long
(64)	OS Version Structure (Optional)	8 bytes
52 (64) (72)	start of data block
		 */
		print.e(barr3);
		print.e(new String(barr3));
		print.e(msg3);
		
		}
		
		
	}

	private static String base64ToString(String msg) throws CoderException {
		return new String(Base64Encoder.decode(msg));
	}
	private static byte[] base64ToByteArray(String msg) throws CoderException {
		return Base64Encoder.decode(msg);
	}
	private static void sub(byte[] barr, int off, int len) {
		System.err.print("barr["+off+"..."+(off+len-1)+"] ->");
		byte[] sbarr = Arrays.copyOfRange(barr, off, off+len);
		print.e(sbarr);
		print.e(new String(sbarr));
		
		// TODO Auto-generated method stub
		
	}

	private static byte[] hex2byteArray(String hex) {
		return new BigInteger(hex,16).toByteArray();
	}
	
	private static String hex2base64(String hex) {
		byte[] barr = new BigInteger(hex,16).toByteArray();
		print.e("--->>"+new String(barr));
		return Base64Encoder.encode(barr);
	}

	/*private static String sub(byte[] barr, int off, int len) throws UnsupportedEncodingException {
		return new String(barr,off,len,CS);
	}*/

	public static short toShort(byte[] barr, int offset) {
		ByteBuffer bb = ByteBuffer.wrap(barr,offset,2);
		bb.order( ByteOrder.LITTLE_ENDIAN);
		return bb.getShort();
	}
	
	public static int toInt(byte[] barr, int offset) {
		ByteBuffer bb = ByteBuffer.wrap(barr,offset,4);
		bb.order( ByteOrder.LITTLE_ENDIAN);
		return bb.getInt();
	}
	
	public static byte[] toByteArray(int i) {
	    final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    bb.putInt(i);
	    return bb.array();
	}
	
	
	public static short byteArrayToShort(byte[] barr, int offset) {
		return(short)(((barr[offset+1]   & 0xFF) << 8) +(barr[offset] & 0xFF));
	}
	
	
    private static String create2Message(String host, String domain) {
        host = host.toUpperCase();
        domain = domain.toUpperCase();
        
        
        /*
                 0       1       2       3
             +-------+-------+-------+-------+
         0:  |  'N'  |  'T'  |  'L'  |  'M'  |
             +-------+-------+-------+-------+
         4:  |  'S'  |  'S'  |  'P'  |   0   |
             +-------+-------+-------+-------+
         8:  |   2   |   0   |   0   |   0   |
             +-------+-------+-------+-------+
        12:  |   0   |   0   |   0   |   0   |
             +-------+-------+-------+-------+
        16:  |  message len  |   0   |   0   |
             +-------+-------+-------+-------+
        20:  | 0x01  | 0x82  |   0   |   0   |
             +-------+-------+-------+-------+
        24:  |                               |
             +          server nonce         |
        28:  |                               |
             +-------+-------+-------+-------+
        32:  |   0   |   0   |   0   |   0   |
             +-------+-------+-------+-------+
        36:  |   0   |   0   |   0   |   0   |
             +-------+-------+-------+-------+
        
         *
         Type-2 Message:

       0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f    0123456789abcdef
   0:  4e 54 4c 4d 53 53 50 00 02 00 00 00 00 00 00 00  "NTLMSSP........."
  10:  28 00 00 00 01 82 00 00 53 72 76 4e 6f 6e 63 65  "(.......SrvNonce"
  20:  00 00 00 00 00 00 00 00                          "........"
  
  0x   4e 54 4c 4d 53 53 50 00 02 00 00 00 
  
         */
        
        
        int flags=
        		NEGOTIATE_UNICODE
        		+REQUEST_TARGET
        		+NEGOTIATE_NTLM
        		+NEGOTIATE_ALWAYS_SIGN
        		+TARGET_TYPE_DOMAIN
        		+NEGOTIATE_TARGET_INFO
        		+NEGOTIATE_128;
        
        
        List<Byte> bytes=new ArrayList<Byte>();
        add(bytes,"NTLMSSP");add(bytes,new byte[]{0}); // 0-8 Signature 
        add(bytes,toByteArray(2)); // Type 2 Indicator 8-4
         // Target Name Security Buffer 12-8 
        add(bytes,toByteArray(flags)); // Flags 20-4
        add(bytes,"SrvNonce");  // Challenge 24-8
        add(bytes,new byte[]{0,0,0,0,0,0,0,0}); // Context 32-8
        add(bytes,"DOMAIN"); // Target Name Data ("DOMAIN")
        hex2base64("44004f004d00410049004e00");
        add(bytes,new byte[]{0,0,0,0,0,0,0,0});
        
        /*
        
        List<Byte> bytes=new ArrayList<Byte>();
        add(bytes,"NTLMSSP");
        add(bytes,new byte[]{0,
        		2,0,0,0,
        		0,0,0,0});
        add(bytes,convertShort(40)); // message length
        add(bytes,new byte[]{0,0,1,82,0,0});
        add(bytes,"SrvNonce"); // server nonce
        add(bytes,new byte[]{0,0,0,0,0,0,0,0});
        */
        
        
        return Base64Encoder.encode(toByteArray(bytes));
    }
	
	

	
	 private static byte[] toByteArray(List<Byte> bytes) {
		byte[] barr=new byte[bytes.size()];
		for(int i=0;i<barr.length;i++) {
			barr[i]=bytes.get(i).byteValue();
		}
		return barr;
	}

	private static void add(List<Byte> bytes, byte[] barr) {
		 for(int i=0;i<barr.length;i++){
				bytes.add(barr[i]);
		}
	}

	private static void add(List<Byte> bytes, String str) {
		char[] carr = str.toCharArray();
		for(int i=0;i<carr.length;i++){
			bytes.add((byte)carr[i]);
		}
	}

	private static byte[] convertShort(int num) {
	        byte[] val = new byte[2];
	        String hex = Integer.toString(num, 16);
	        while (hex.length() < 4) {
	            hex = "0" + hex;
	        }
	        String low = hex.substring(2, 4);
	        String high = hex.substring(0, 2);

	        val[0] = (byte) Integer.parseInt(low, 16);
	        val[1] = (byte) Integer.parseInt(high, 16);
	        return val;
	    }
	
	
	/**
     * Creates the first message (type 1 message) in the NTLM authentication sequence.
     * This message includes the user name, domain and host for the authentication session.
     * 
     * @param host the computer name of the host requesting authentication.
     * @param domain The domain to authenticate with.
     * @return String the message to add to the HTTP request header.
     */
    /*private String getType1Message(String host, String domain) {
        host = host.toUpperCase();
        domain = domain.toUpperCase();
        byte[] hostBytes = getBytes(host);
        byte[] domainBytes = getBytes(domain);

        int finalLength = 32 + hostBytes.length + domainBytes.length;
        prepareResponse(finalLength);
        
        // The initial id string.
        byte[] protocol = getBytes("NTLMSSP");
        addBytes(protocol);
        addByte((byte) 0);

        // Type
        addByte((byte) 1);
        addByte((byte) 0);
        addByte((byte) 0);
        addByte((byte) 0);

        // Flags
        addByte((byte) 6);
        addByte((byte) 82);
        addByte((byte) 0);
        addByte((byte) 0);

        // Domain length (first time).
        int iDomLen = domainBytes.length;
        byte[] domLen = convertShort(iDomLen);
        addByte(domLen[0]);
        addByte(domLen[1]);

        // Domain length (second time).
        addByte(domLen[0]);
        addByte(domLen[1]);

        // Domain offset.
        byte[] domOff = convertShort(hostBytes.length + 32);
        addByte(domOff[0]);
        addByte(domOff[1]);
        addByte((byte) 0);
        addByte((byte) 0);

        // Host length (first time).
        byte[] hostLen = convertShort(hostBytes.length);
        addByte(hostLen[0]);
        addByte(hostLen[1]);

        // Host length (second time).
        addByte(hostLen[0]);
        addByte(hostLen[1]);

        // Host offset (always 32).
        byte[] hostOff = convertShort(32);
        addByte(hostOff[0]);
        addByte(hostOff[1]);
        addByte((byte) 0);
        addByte((byte) 0);

        // Host String.
        addBytes(hostBytes);

        // Domain String.
        addBytes(domainBytes);

        return getResponse();
    }*/
}
