package lucee.commons.digest;


import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author attilax Old wow paw
 * @since o85 m_x_o$
 */
public class Base256Coder {
	static final char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/的一是了我不人在他有这个上们来到时大地为子中你说生国年着就那和要她出也得里后自以会家可下而过天去能对小多然于心学么之都好看起发当没成只如事把还用第样道想作种开美总从无情己面最女但现前些所同日手又行意动方期它头经长儿回位分爱老因很给名法间斯知世什两次使身者被高已亲其进此话常与活正感见明问力理尔点文几定本公特做外孩相西果走将月十实向声车全信重三机工物气每并别真打太新比才便夫再书部水像眼"
			.toCharArray();
	static Map<Character, Integer> mp = new HashMap<Character, Integer>();
	static {
		int n = 0;
		for (char ch: alphabet) {
			mp.put(ch, n);
			n++;

		}
	}

	public static String encode(byte[] bytes) {
		CharArrayWriter cw = new CharArrayWriter(bytes.length);

		int idx = 0;

		int x = 0;

		for (int i = 0; i < bytes.length; ++i) {
			int c = convert2Unsign(bytes[i]);
			cw.write(alphabet[c]);

		}

		return new String(cw.toCharArray());
	}

	/**
	 * @author attilax 老哇的爪子
	 * @since o8a 2_41_56
	 * 
	 * @param b
	 * @return
	 */
	private static int convert2Unsign(byte b) {
		// attilax 老哇的爪子 2_41_56 o8a
		// if(b<0)
		{
			// char c=(char) b;
			return 128 + b;
		}
		// else
		// return b+127;

	}

	public static byte[] decode(String str) throws IOException {

		char[] message = str.toCharArray();
		byte[] dest = new byte[message.length];

		for (int i = 0; i < message.length; ++i) {
			char c = message[i];
			int c_postion = convert2signByte(mp.get(c));

			dest[i] = (byte) c_postion;
		}

		return dest;
	}

	/**
	 * @author attilax 老哇的爪子
	 * @since o8a 3_0_1
	 * 
	 * @param integer
	 * @return
	 */
	private static int convert2signByte(Integer integer) {
		// attilax 老哇的爪子 3_0_1 o8a

		return integer - 128;
		// return integer;

	}
}
