package lucee.runtime.text.xml.html;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.Writer;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class HTMLScanner implements Scanner, Locator {
	private static final int S_ANAME = 1;
	private static final int S_APOS = 2;
	private static final int S_AVAL = 3;
	private static final int S_BB = 4;
	private static final int S_BBC = 5;
	private static final int S_BBCD = 6;
	private static final int S_BBCDA = 7;
	private static final int S_BBCDAT = 8;
	private static final int S_BBCDATA = 9;
	private static final int S_CDATA = 10;
	private static final int S_CDATA2 = 11;
	private static final int S_CDSECT = 12;
	private static final int S_CDSECT1 = 13;
	private static final int S_CDSECT2 = 14;
	private static final int S_COM = 15;
	private static final int S_COM2 = 16;
	private static final int S_COM3 = 17;
	private static final int S_COM4 = 18;
	private static final int S_DECL = 19;
	private static final int S_DECL2 = 20;
	private static final int S_DONE = 21;
	private static final int S_EMPTYTAG = 22;
	private static final int S_ENT = 23;
	private static final int S_EQ = 24;
	private static final int S_ETAG = 25;
	private static final int S_GI = 26;
	private static final int S_NCR = 27;
	private static final int S_PCDATA = 28;
	private static final int S_PI = 29;
	private static final int S_PITARGET = 30;
	private static final int S_QUOT = 31;
	private static final int S_STAGC = 32;
	private static final int S_TAG = 33;
	private static final int S_TAGWS = 34;
	private static final int S_XNCR = 35;
	private static final int A_ADUP = 1;
	private static final int A_ADUP_SAVE = 2;
	private static final int A_ADUP_STAGC = 3;
	private static final int A_ANAME = 4;
	private static final int A_ANAME_ADUP = 5;
	private static final int A_ANAME_ADUP_STAGC = 6;
	private static final int A_AVAL = 7;
	private static final int A_AVAL_STAGC = 8;
	private static final int A_CDATA = 9;
	private static final int A_CMNT = 10;
	private static final int A_DECL = 11;
	private static final int A_EMPTYTAG = 12;
	private static final int A_ENTITY = 13;
	private static final int A_ENTITY_START = 14;
	private static final int A_ETAG = 15;
	private static final int A_GI = 16;
	private static final int A_GI_STAGC = 17;
	private static final int A_LT = 18;
	private static final int A_LT_PCDATA = 19;
	private static final int A_MINUS = 20;
	private static final int A_MINUS2 = 21;
	private static final int A_MINUS3 = 22;
	private static final int A_PCDATA = 23;
	private static final int A_PI = 24;
	private static final int A_PITARGET = 25;
	private static final int A_PITARGET_PI = 26;
	private static final int A_SAVE = 27;
	private static final int A_SKIP = 28;
	private static final int A_SP = 29;
	private static final int A_STAGC = 30;
	private static final int A_UNGET = 31;
	private static final int A_UNSAVE_PCDATA = 32;
	private static int[] statetable = new int[] { 1, 47, 5, 22, 1, 61, 4, 3, 1, 62, 6, 28, 1, 0, 27, 1, 1, -1, 6, 21, 1, 32, 4, 24, 1, 10, 4, 24, 1, 9, 4, 24, 2, 39, 7, 34, 2, 0,
			27, 2, 2, -1, 8, 21, 2, 32, 29, 2, 2, 10, 29, 2, 2, 9, 29, 2, 3, 34, 28, 31, 3, 39, 28, 2, 3, 62, 8, 28, 3, 0, 27, 32, 3, -1, 8, 21, 3, 32, 28, 3, 3, 10, 28, 3, 3, 9,
			28, 3, 4, 67, 28, 5, 4, 0, 28, 19, 4, -1, 28, 21, 5, 68, 28, 6, 5, 0, 28, 19, 5, -1, 28, 21, 6, 65, 28, 7, 6, 0, 28, 19, 6, -1, 28, 21, 7, 84, 28, 8, 7, 0, 28, 19, 7,
			-1, 28, 21, 8, 65, 28, 9, 8, 0, 28, 19, 8, -1, 28, 21, 9, 91, 28, 12, 9, 0, 28, 19, 9, -1, 28, 21, 10, 60, 27, 11, 10, 0, 27, 10, 10, -1, 23, 21, 11, 47, 32, 25, 11, 0,
			27, 10, 11, -1, 32, 21, 12, 93, 27, 13, 12, 0, 27, 12, 12, -1, 28, 21, 13, 93, 27, 14, 13, 0, 27, 12, 13, -1, 28, 21, 14, 62, 9, 28, 14, 93, 27, 14, 14, 0, 27, 12, 14,
			-1, 28, 21, 15, 45, 28, 16, 15, 0, 27, 16, 15, -1, 10, 21, 16, 45, 28, 17, 16, 0, 27, 16, 16, -1, 10, 21, 17, 45, 28, 18, 17, 0, 20, 16, 17, -1, 10, 21, 18, 45, 22, 18,
			18, 62, 10, 28, 18, 0, 21, 16, 18, -1, 10, 21, 19, 45, 28, 15, 19, 62, 28, 28, 19, 91, 28, 4, 19, 0, 27, 20, 19, -1, 28, 21, 20, 62, 11, 28, 20, 0, 27, 20, 20, -1, 28,
			21, 22, 62, 12, 28, 22, 0, 27, 1, 22, 32, 28, 34, 22, 10, 28, 34, 22, 9, 28, 34, 23, 0, 13, 23, 23, -1, 13, 21, 24, 61, 28, 3, 24, 62, 3, 28, 24, 0, 2, 1, 24, -1, 3,
			21, 24, 32, 28, 24, 24, 10, 28, 24, 24, 9, 28, 24, 25, 62, 15, 28, 25, 0, 27, 25, 25, -1, 15, 21, 25, 32, 28, 25, 25, 10, 28, 25, 25, 9, 28, 25, 26, 47, 28, 22, 26, 62,
			17, 28, 26, 0, 27, 26, 26, -1, 28, 21, 26, 32, 16, 34, 26, 10, 16, 34, 26, 9, 16, 34, 27, 0, 13, 27, 27, -1, 13, 21, 28, 38, 14, 23, 28, 60, 23, 33, 28, 0, 27, 28, 28,
			-1, 23, 21, 29, 62, 24, 28, 29, 0, 27, 29, 29, -1, 24, 21, 30, 62, 26, 28, 30, 0, 27, 30, 30, -1, 26, 21, 30, 32, 25, 29, 30, 10, 25, 29, 30, 9, 25, 29, 31, 34, 7, 34,
			31, 0, 27, 31, 31, -1, 8, 21, 31, 32, 29, 31, 31, 10, 29, 31, 31, 9, 29, 31, 32, 62, 8, 28, 32, 0, 27, 32, 32, -1, 8, 21, 32, 32, 7, 34, 32, 10, 7, 34, 32, 9, 7, 34,
			33, 33, 28, 19, 33, 47, 28, 25, 33, 60, 27, 33, 33, 63, 28, 30, 33, 0, 27, 26, 33, -1, 19, 21, 33, 32, 18, 28, 33, 10, 18, 28, 33, 9, 18, 28, 34, 47, 28, 22, 34, 62,
			30, 28, 34, 0, 27, 1, 34, -1, 30, 21, 34, 32, 28, 34, 34, 10, 28, 34, 34, 9, 28, 34, 35, 0, 13, 35, 35, -1, 13, 21 };
	private static final String[] debug_actionnames = new String[] { "", "A_ADUP", "A_ADUP_SAVE", "A_ADUP_STAGC", "A_ANAME", "A_ANAME_ADUP", "A_ANAME_ADUP_STAGC", "A_AVAL",
			"A_AVAL_STAGC", "A_CDATA", "A_CMNT", "A_DECL", "A_EMPTYTAG", "A_ENTITY", "A_ENTITY_START", "A_ETAG", "A_GI", "A_GI_STAGC", "A_LT", "A_LT_PCDATA", "A_MINUS", "A_MINUS2",
			"A_MINUS3", "A_PCDATA", "A_PI", "A_PITARGET", "A_PITARGET_PI", "A_SAVE", "A_SKIP", "A_SP", "A_STAGC", "A_UNGET", "A_UNSAVE_PCDATA" };
	private static final String[] debug_statenames = new String[] { "", "S_ANAME", "S_APOS", "S_AVAL", "S_BB", "S_BBC", "S_BBCD", "S_BBCDA", "S_BBCDAT", "S_BBCDATA", "S_CDATA",
			"S_CDATA2", "S_CDSECT", "S_CDSECT1", "S_CDSECT2", "S_COM", "S_COM2", "S_COM3", "S_COM4", "S_DECL", "S_DECL2", "S_DONE", "S_EMPTYTAG", "S_ENT", "S_EQ", "S_ETAG", "S_GI",
			"S_NCR", "S_PCDATA", "S_PI", "S_PITARGET", "S_QUOT", "S_STAGC", "S_TAG", "S_TAGWS", "S_XNCR" };
	private String thePublicid;
	private String theSystemid;
	private int theLastLine;
	private int theLastColumn;
	private int theCurrentLine;
	private int theCurrentColumn;
	int theState;
	int theNextState;
	char[] theOutputBuffer = new char[200];
	int theSize;
	int[] theWinMap = new int[] { 8364, 65533, 8218, 402, 8222, 8230, 8224, 8225, 710, 8240, 352, 8249, 338, 65533, 381, 65533, 65533, 8216, 8217, 8220, 8221, 8226, 8211, 8212,
			732, 8482, 353, 8250, 339, 65533, 382, 376 };
	static short[][] statetableIndex;
	static int statetableIndexMaxChar;

	private void unread(PushbackReader r, int c) throws IOException {
		if (c != -1) {
			r.unread(c);
		}

	}

	@Override
	public int getLineNumber() {
		return this.theLastLine;
	}

	@Override
	public int getColumnNumber() {
		return this.theLastColumn;
	}

	@Override
	public String getPublicId() {
		return this.thePublicid;
	}

	@Override
	public String getSystemId() {
		return this.theSystemid;
	}

	@Override
	public void resetDocumentLocator(String publicid, String systemid) {
		this.thePublicid = publicid;
		this.theSystemid = systemid;
		this.theLastLine = this.theLastColumn = this.theCurrentLine = this.theCurrentColumn = 0;
	}

	@Override
	public void scan(Reader r0, ScanHandler h) throws IOException, SAXException {
		this.theState = 28;
		PushbackReader r;
		if (r0 instanceof BufferedReader) {
			r = new PushbackReader(r0, 5);
		}
		else {
			r = new PushbackReader(new BufferedReader(r0), 5);
		}

		int firstChar = r.read();
		if (firstChar != 65279) {
			this.unread(r, firstChar);
		}

		while (this.theState != 21) {
			int ch = r.read();
			if (ch >= 128 && ch <= 159) {
				ch = this.theWinMap[ch - 128];
			}

			if (ch == 13) {
				ch = r.read();
				if (ch != 10) {
					this.unread(r, ch);
					ch = 10;
				}
			}

			if (ch == 10) {
				++this.theCurrentLine;
				this.theCurrentColumn = 0;
			}
			else {
				++this.theCurrentColumn;
			}

			if (ch >= 32 || ch == 10 || ch == 9 || ch == -1) {
				int adjCh = ch >= -1 && ch < statetableIndexMaxChar ? ch : -2;
				int statetableRow = statetableIndex[this.theState][adjCh + 2];
				int action = 0;
				if (statetableRow != -1) {
					action = statetable[statetableRow + 2];
					this.theNextState = statetable[statetableRow + 3];
				}

				switch (action) {
				case 0:
					throw new Error("HTMLScanner can't cope with " + Integer.toString(ch) + " in state " + Integer.toString(this.theState));
				case 1:
					h.adup(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					break;
				case 2:
					h.adup(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					this.save(ch, h);
					break;
				case 3:
					h.adup(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					h.stagc(this.theOutputBuffer, 0, this.theSize);
					break;
				case 4:
					h.aname(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					break;
				case 5:
					h.aname(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					h.adup(this.theOutputBuffer, 0, this.theSize);
					break;
				case 6:
					h.aname(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					h.adup(this.theOutputBuffer, 0, this.theSize);
					h.stagc(this.theOutputBuffer, 0, this.theSize);
					break;
				case 7:
					h.aval(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					break;
				case 8:
					h.aval(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					h.stagc(this.theOutputBuffer, 0, this.theSize);
					break;
				case 9:
					this.mark();
					if (this.theSize > 1) {
						this.theSize -= 2;
					}

					h.pcdata(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					break;
				case 10:
					this.mark();
					h.cmnt(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					break;
				case 11:
					h.decl(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					break;
				case 12:
					this.mark();
					if (this.theSize > 0) {
						h.gi(this.theOutputBuffer, 0, this.theSize);
					}

					this.theSize = 0;
					h.stage(this.theOutputBuffer, 0, this.theSize);
					break;
				case 13:
					this.mark();
					char ch1 = (char) ch;
					if (this.theState == 23 && ch1 == '#') {
						this.theNextState = 27;
						this.save(ch, h);
					}
					else if (this.theState != 27 || ch1 != 'x' && ch1 != 'X') {
						if (this.theState == 23 && Character.isLetterOrDigit(ch1)) {
							this.save(ch, h);
						}
						else if (this.theState == 27 && Character.isDigit(ch1)) {
							this.save(ch, h);
						}
						else if (this.theState != 35 || !Character.isDigit(ch1) && "abcdefABCDEF".indexOf(ch1) == -1) {
							h.entity(this.theOutputBuffer, 1, this.theSize - 1);
							int ent = h.getEntity();
							if (ent != 0) {
								this.theSize = 0;
								if (ent >= 128 && ent <= 159) {
									ent = this.theWinMap[ent - 128];
								}

								if (ent < 32) {
									ent = 32;
								}
								else if (ent >= 55296 && ent <= 57343) {
									ent = 0;
								}
								else if (ent <= 65535) {
									this.save(ent, h);
								}
								else {
									ent -= 65536;
									this.save((ent >> 10) + '\ud800', h);
									this.save((ent & 1023) + '\udc00', h);
								}

								if (ch != 59) {
									this.unread(r, ch);
									--this.theCurrentColumn;
								}
							}
							else {
								this.unread(r, ch);
								--this.theCurrentColumn;
							}

							this.theNextState = 28;
						}
						else {
							this.save(ch, h);
						}
					}
					else {
						this.theNextState = 35;
						this.save(ch, h);
					}
					break;
				case 14:
					h.pcdata(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					this.save(ch, h);
					break;
				case 15:
					h.etag(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					break;
				case 16:
					h.gi(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					break;
				case 17:
					h.gi(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					h.stagc(this.theOutputBuffer, 0, this.theSize);
					break;
				case 18:
					this.mark();
					this.save(60, h);
					this.save(ch, h);
					break;
				case 19:
					this.mark();
					this.save(60, h);
					h.pcdata(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					break;
				case 21:
					this.save(45, h);
					this.save(32, h);
				case 20:
					this.save(45, h);
					this.save(ch, h);
					break;
				case 22:
					this.save(45, h);
					this.save(32, h);
					break;
				case 23:
					this.mark();
					h.pcdata(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					break;
				case 24:
					this.mark();
					h.pi(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					break;
				case 25:
					h.pitarget(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					break;
				case 26:
					h.pitarget(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					h.pi(this.theOutputBuffer, 0, this.theSize);
					break;
				case 27:
					this.save(ch, h);
				case 28:
					break;
				case 29:
					this.save(32, h);
					break;
				case 30:
					h.stagc(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					break;
				case 31:
					this.unread(r, ch);
					--this.theCurrentColumn;
					break;
				case 32:
					if (this.theSize > 0) {
						--this.theSize;
					}

					h.pcdata(this.theOutputBuffer, 0, this.theSize);
					this.theSize = 0;
					break;
				default:
					throw new Error("Can't process state " + action);
				}

				this.theState = this.theNextState;
			}
		}

		h.eof(this.theOutputBuffer, 0, 0);
	}

	private void mark() {
		this.theLastColumn = this.theCurrentColumn;
		this.theLastLine = this.theCurrentLine;
	}

	@Override
	public void startCDATA() {
		this.theNextState = 10;
	}

	private void save(int ch, ScanHandler h) throws SAXException {
		if (this.theSize >= this.theOutputBuffer.length - 20) {
			if (this.theState != 28 && this.theState != 10) {
				char[] newOutputBuffer = new char[this.theOutputBuffer.length * 2];
				System.arraycopy(this.theOutputBuffer, 0, newOutputBuffer, 0, this.theSize + 1);
				this.theOutputBuffer = newOutputBuffer;
			}
			else {
				h.pcdata(this.theOutputBuffer, 0, this.theSize);
				this.theSize = 0;
			}
		}

		this.theOutputBuffer[this.theSize++] = (char) ch;
	}

	public static void main(String[] argv) throws IOException, SAXException {
		Scanner s = new HTMLScanner();
		Reader r = new InputStreamReader(System.in, "UTF-8");
		Writer w = new OutputStreamWriter(System.out, "UTF-8");
		PYXWriter pw = new PYXWriter(w);
		s.scan(r, pw);
		w.close();
	}

	private static String nicechar(int in) {
		if (in == 10) {
			return "\\n";
		}
		else {
			return in < 32 ? "0x" + Integer.toHexString(in) : "'" + (char) in + "'";
		}
	}

	static {
		int maxState = -1;
		int maxChar = -1;

		for (int i = 0; i < statetable.length; i += 4) {
			if (statetable[i] > maxState) {
				maxState = statetable[i];
			}

			if (statetable[i + 1] > maxChar) {
				maxChar = statetable[i + 1];
			}
		}

		statetableIndexMaxChar = maxChar + 1;
		statetableIndex = new short[maxState + 1][maxChar + 3];

		for (int theState = 0; theState <= maxState; ++theState) {
			for (int ch = -2; ch <= maxChar; ++ch) {
				int hit = -1;
				int action = 0;

				for (int i = 0; i < statetable.length; i += 4) {
					if (theState != statetable[i]) {
						if (action != 0) {
							break;
						}
					}
					else if (statetable[i + 1] == 0) {
						hit = i;
						action = statetable[i + 2];
					}
					else if (statetable[i + 1] == ch) {
						hit = i;
						int var10000 = statetable[i + 2];
						break;
					}
				}

				statetableIndex[theState][ch + 2] = (short) hit;
			}
		}

	}
}
