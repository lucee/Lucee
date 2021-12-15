/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/

package lucee.runtime.sql.old;

import java.io.IOException;
import java.io.PrintStream;

// Referenced classes of package Zql:
//            TokenMgrError, ZqlJJParserConstants, SimpleCharStream, Token

public final class ZqlJJParserTokenManager implements ZqlJJParserConstants {

	public void setDebugStream(PrintStream printstream) {
		debugStream = printstream;
	}

	private final int jjStopStringLiteralDfa_0(int i, long l, long l1) {
		switch (i) {
		case 0: // '\0'
			if ((l1 & 0x4000000000L) != 0L) return 0;
			if ((l1 & 0x10020000000L) != 0L) return 47;
			if ((l1 & 0x80000000000L) != 0L) return 3;
			if ((l & -32L) != 0L || (l1 & 4095L) != 0L) {
				jjmatchedKind = 82;
				return 48;
			}
			// else{
			return -1;
		// }

		case 1: // '\001'
			if ((l & 0x1a003f00004300L) != 0L) return 48;
			if ((l & 0xffe5ffc0ffffbce0L) != 0L || (l1 & 4095L) != 0L) {
				if (jjmatchedPos != 1) {
					jjmatchedKind = 82;
					jjmatchedPos = 1;
				}
				return 48;
			}
			// else{
			return -1;
		// }

		case 2: // '\002'
			if ((l & 0xebf5d8deefffb800L) != 0L || (l1 & 4094L) != 0L) {
				if (jjmatchedPos != 2) {
					jjmatchedKind = 82;
					jjmatchedPos = 2;
				}
				return 48;
			}
			return (l & 0x14002700100006e0L) == 0L && (l1 & 1L) == 0L ? -1 : 48;

		case 3: // '\003'
			if ((l & 0x1c488d024508000L) != 0L || (l1 & 1536L) != 0L) return 48;
			if ((l & 0xea31540ecbaf3800L) != 0L || (l1 & 2558L) != 0L) {
				jjmatchedKind = 82;
				jjmatchedPos = 3;
				return 48;
			}
			// else{
			return -1;
		// }

		case 4: // '\004'
			if ((l & 0xa030040048080000L) != 0L || (l1 & 2314L) != 0L) return 48;
			if ((l & 0x4a01500e83a73800L) != 0L || (l1 & 244L) != 0L) {
				jjmatchedKind = 82;
				jjmatchedPos = 4;
				return 48;
			}
			// else{
			return -1;
		// }

		case 5: // '\005'
			if ((l & 0x4200100c01853800L) != 0L || (l1 & 196L) != 0L) {
				jjmatchedKind = 82;
				jjmatchedPos = 5;
				return 48;
			}
			return (l & 0x801400282220000L) == 0L && (l1 & 48L) == 0L ? -1 : 48;

		case 6: // '\006'
			if ((l & 4096L) != 0L) {
				if (jjmatchedPos != 6) {
					jjmatchedKind = 82;
					jjmatchedPos = 6;
				}
				return 11;
			}
			if ((l & 0x100400052800L) != 0L || (l1 & 192L) != 0L) return 48;
			if ((l & 0x4200000801800000L) != 0L || (l1 & 4L) != 0L) {
				if (jjmatchedPos != 6) {
					jjmatchedKind = 82;
					jjmatchedPos = 6;
				}
				return 48;
			}
			// else{
			return -1;
		// }

		case 7: // '\007'
			if ((l & 0x4200000000800000L) != 0L) return 48;
			if ((l & 4096L) != 0L) {
				jjmatchedKind = 82;
				jjmatchedPos = 7;
				return 11;
			}
			if ((l1 & 64L) != 0L) return 11;
			if ((l & 0x801000000L) != 0L || (l1 & 4L) != 0L) {
				jjmatchedKind = 82;
				jjmatchedPos = 7;
				return 48;
			}
			// else{
			return -1;
		// }

		case 8: // '\b'
			if ((l1 & 4L) != 0L) {
				jjmatchedKind = 82;
				jjmatchedPos = 8;
				return 48;
			}
			if ((l & 0x801000000L) != 0L) return 48;
			if ((l & 4096L) != 0L) {
				jjmatchedKind = 82;
				jjmatchedPos = 8;
				return 11;
			}
			// else{
			return -1;
		// }

		case 9: // '\t'
			if ((l1 & 4L) != 0L) {
				jjmatchedKind = 82;
				jjmatchedPos = 9;
				return 48;
			}
			if ((l & 4096L) != 0L) {
				jjmatchedKind = 82;
				jjmatchedPos = 9;
				return 11;
			}
			// else{
			return -1;
		// }

		case 10: // '\n'
			if ((l1 & 4L) != 0L) return 48;
			if ((l & 4096L) != 0L) {
				jjmatchedKind = 82;
				jjmatchedPos = 10;
				return 11;
			}
			// else{
			return -1;
		// }

		case 11: // '\013'
			if ((l & 4096L) != 0L) {
				jjmatchedKind = 82;
				jjmatchedPos = 11;
				return 11;
			}
			// else{
			return -1;
		// }

		case 12: // '\f'
			if ((l & 4096L) != 0L) {
				jjmatchedKind = 82;
				jjmatchedPos = 12;
				return 11;
			}
			// else{
			return -1;
		// }
		}
		return -1;
	}

	private final int jjStartNfa_0(int i, long l, long l1) {
		return jjMoveNfa_0(jjStopStringLiteralDfa_0(i, l, l1), i + 1);
	}

	private final int jjStopAtPos(int i, int j) {
		jjmatchedKind = j;
		jjmatchedPos = i;
		return i + 1;
	}

	private final int jjStartNfaWithStates_0(int i, int j, int k) {
		jjmatchedKind = j;
		jjmatchedPos = i;
		try {
			curChar = input_stream.readChar();
		}
		catch (IOException ioexception) {
			return i + 1;
		}
		return jjMoveNfa_0(k, i + 1);
	}

	private final int jjMoveStringLiteralDfa0_0() {
		switch (curChar) {
		case 33: // '!'
			return jjMoveStringLiteralDfa1_0(0L, 0x40000000L);

		case 35: // '#'
			return jjStopAtPos(0, 95);

		case 40: // '('
			return jjStopAtPos(0, 88);

		case 41: // ')'
			return jjStopAtPos(0, 90);

		case 42: // '*'
			jjmatchedKind = 103;
			return jjMoveStringLiteralDfa1_0(0L, 0x100000000000L);

		case 43: // '+'
			return jjStopAtPos(0, 101);

		case 44: // ','
			return jjStopAtPos(0, 89);

		case 45: // '-'
			return jjStartNfaWithStates_0(0, 102, 0);

		case 46: // '.'
			jjmatchedKind = 93;
			return jjMoveStringLiteralDfa1_0(0L, 0x10000000000L);

		case 47: // '/'
			return jjStartNfaWithStates_0(0, 107, 3);

		case 59: // ';'
			return jjStopAtPos(0, 91);

		case 60: // '<'
			jjmatchedKind = 99;
			return jjMoveStringLiteralDfa1_0(0L, 0x1100000000L);

		case 61: // '='
			return jjStopAtPos(0, 92);

		case 62: // '>'
			jjmatchedKind = 97;
			return jjMoveStringLiteralDfa1_0(0L, 0x400000000L);

		case 63: // '?'
			return jjStopAtPos(0, 105);

		case 65: // 'A'
		case 97: // 'a'
			return jjMoveStringLiteralDfa1_0(2016L, 0L);

		case 66: // 'B'
		case 98: // 'b'
			return jjMoveStringLiteralDfa1_0(30720L, 0L);

		case 67: // 'C'
		case 99: // 'c'
			return jjMoveStringLiteralDfa1_0(0xf8000L, 0L);

		case 68: // 'D'
		case 100: // 'd'
			return jjMoveStringLiteralDfa1_0(0xf00000L, 0L);

		case 69: // 'E'
		case 101: // 'e'
			return jjMoveStringLiteralDfa1_0(0x7000000L, 0L);

		case 70: // 'F'
		case 102: // 'f'
			return jjMoveStringLiteralDfa1_0(0x38000000L, 0L);

		case 71: // 'G'
		case 103: // 'g'
			return jjMoveStringLiteralDfa1_0(0x40000000L, 0L);

		case 72: // 'H'
		case 104: // 'h'
			return jjMoveStringLiteralDfa1_0(0x80000000L, 0L);

		case 73: // 'I'
		case 105: // 'i'
			return jjMoveStringLiteralDfa1_0(0x3f00000000L, 0L);

		case 76: // 'L'
		case 108: // 'l'
			return jjMoveStringLiteralDfa1_0(0xc000000000L, 0L);

		case 77: // 'M'
		case 109: // 'm'
			return jjMoveStringLiteralDfa1_0(0xf0000000000L, 0L);

		case 78: // 'N'
		case 110: // 'n'
			return jjMoveStringLiteralDfa1_0(0x1f00000000000L, 0L);

		case 79: // 'O'
		case 111: // 'o'
			return jjMoveStringLiteralDfa1_0(0x1e000000000000L, 0L);

		case 80: // 'P'
		case 112: // 'p'
			return jjMoveStringLiteralDfa1_0(0x20000000000000L, 0L);

		case 81: // 'Q'
		case 113: // 'q'
			return jjMoveStringLiteralDfa1_0(0x40000000000000L, 0L);

		case 82: // 'R'
		case 114: // 'r'
			return jjMoveStringLiteralDfa1_0(0x780000000000000L, 0L);

		case 83: // 'S'
		case 115: // 's'
			return jjMoveStringLiteralDfa1_0(0xf800000000000000L, 1L);

		case 84: // 'T'
		case 116: // 't'
			return jjMoveStringLiteralDfa1_0(0L, 6L);

		case 85: // 'U'
		case 117: // 'u'
			return jjMoveStringLiteralDfa1_0(0L, 24L);

		case 86: // 'V'
		case 118: // 'v'
			return jjMoveStringLiteralDfa1_0(0L, 224L);

		case 87: // 'W'
		case 119: // 'w'
			return jjMoveStringLiteralDfa1_0(0L, 3840L);

		case 124: // '|'
			return jjMoveStringLiteralDfa1_0(0L, 0x40000000000L);

		case 34: // '"'
		case 36: // '$'
		case 37: // '%'
		case 38: // '&'
		case 39: // '\''
		case 48: // '0'
		case 49: // '1'
		case 50: // '2'
		case 51: // '3'
		case 52: // '4'
		case 53: // '5'
		case 54: // '6'
		case 55: // '7'
		case 56: // '8'
		case 57: // '9'
		case 58: // ':'
		case 64: // '@'
		case 74: // 'J'
		case 75: // 'K'
		case 88: // 'X'
		case 89: // 'Y'
		case 90: // 'Z'
		case 91: // '['
		case 92: // '\\'
		case 93: // ']'
		case 94: // '^'
		case 95: // '_'
		case 96: // '`'
		case 106: // 'j'
		case 107: // 'k'
		case 120: // 'x'
		case 121: // 'y'
		case 122: // 'z'
		case 123: // '{'
		default:
			return jjMoveNfa_0(2, 0);
		}
	}

	private final int jjMoveStringLiteralDfa1_0(long l, long l1) {
		try {
			curChar = input_stream.readChar();
		}
		catch (IOException ioexception) {
			jjStopStringLiteralDfa_0(0, l, l1);
			return 1;
		}
		switch (curChar) {
		case 43: // '+'
		case 44: // ','
		case 45: // '-'
		case 46: // '.'
		case 47: // '/'
		case 48: // '0'
		case 49: // '1'
		case 50: // '2'
		case 51: // '3'
		case 52: // '4'
		case 53: // '5'
		case 54: // '6'
		case 55: // '7'
		case 56: // '8'
		case 57: // '9'
		case 58: // ':'
		case 59: // ';'
		case 60: // '<'
		case 63: // '?'
		case 64: // '@'
		case 66: // 'B'
		case 67: // 'C'
		case 68: // 'D'
		case 71: // 'G'
		case 74: // 'J'
		case 75: // 'K'
		case 81: // 'Q'
		case 87: // 'W'
		case 90: // 'Z'
		case 91: // '['
		case 92: // '\\'
		case 93: // ']'
		case 94: // '^'
		case 95: // '_'
		case 96: // '`'
		case 98: // 'b'
		case 99: // 'c'
		case 100: // 'd'
		case 103: // 'g'
		case 106: // 'j'
		case 107: // 'k'
		case 113: // 'q'
		case 119: // 'w'
		case 122: // 'z'
		case 123: // '{'
		default:
			break;

		case 42: // '*'
			if ((l1 & 0x10000000000L) != 0L) return jjStopAtPos(1, 104);
			if ((l1 & 0x100000000000L) != 0L) return jjStopAtPos(1, 108);
			break;

		case 61: // '='
			if ((l1 & 0x40000000L) != 0L) return jjStopAtPos(1, 94);
			if ((l1 & 0x400000000L) != 0L) return jjStopAtPos(1, 98);
			if ((l1 & 0x1000000000L) != 0L) return jjStopAtPos(1, 100);
			break;

		case 62: // '>'
			if ((l1 & 0x100000000L) != 0L) return jjStopAtPos(1, 96);
			break;

		case 65: // 'A'
		case 97: // 'a'
			return jjMoveStringLiteralDfa2_0(l, 0x110080100000L, l1, 226L);

		case 69: // 'E'
		case 101: // 'e'
			return jjMoveStringLiteralDfa2_0(l, 0x1980000000600800L, l1, 0L);

		case 70: // 'F'
		case 102: // 'f'
			if ((l & 0x2000000000000L) != 0L) return jjStartNfaWithStates_0(1, 49, 48);
			break;

		case 72: // 'H'
		case 104: // 'h'
			return jjMoveStringLiteralDfa2_0(l, 0x2000000000008000L, l1, 256L);

		case 73: // 'I'
		case 105: // 'i'
			return jjMoveStringLiteralDfa2_0(l, 0x64000801000L, l1, 512L);

		case 76: // 'L'
		case 108: // 'l'
			return jjMoveStringLiteralDfa2_0(l, 0x8000020L, l1, 0L);

		case 77: // 'M'
		case 109: // 'm'
			return jjMoveStringLiteralDfa2_0(l, 0x4000000000000000L, l1, 0L);

		case 78: // 'N'
		case 110: // 'n'
			if ((l & 0x100000000L) != 0L) {
				jjmatchedKind = 32;
				jjmatchedPos = 1;
			}
			return jjMoveStringLiteralDfa2_0(l, 0x4001e000000c0L, l1, 8L);

		case 79: // 'O'
		case 111: // 'o'
			return jjMoveStringLiteralDfa2_0(l, 0x6006880100f2000L, l1, 1024L);

		case 80: // 'P'
		case 112: // 'p'
			return jjMoveStringLiteralDfa2_0(l, 0L, l1, 16L);

		case 82: // 'R'
		case 114: // 'r'
			if ((l & 0x8000000000000L) != 0L) {
				jjmatchedKind = 51;
				jjmatchedPos = 1;
			}
			return jjMoveStringLiteralDfa2_0(l, 0x30000060000000L, l1, 2052L);

		case 83: // 'S'
		case 115: // 's'
			if ((l & 256L) != 0L) {
				jjmatchedKind = 8;
				jjmatchedPos = 1;
			}
			else if ((l & 0x2000000000L) != 0L) return jjStartNfaWithStates_0(1, 37, 48);
			return jjMoveStringLiteralDfa2_0(l, 512L, l1, 0L);

		case 84: // 'T'
		case 116: // 't'
			return jjMoveStringLiteralDfa2_0(l, 0x8000000000000000L, l1, 0L);

		case 85: // 'U'
		case 117: // 'u'
			return jjMoveStringLiteralDfa2_0(l, 0x41800000000000L, l1, 1L);

		case 86: // 'V'
		case 118: // 'v'
			return jjMoveStringLiteralDfa2_0(l, 1024L, l1, 0L);

		case 88: // 'X'
		case 120: // 'x'
			return jjMoveStringLiteralDfa2_0(l, 0x7000000L, l1, 0L);

		case 89: // 'Y'
		case 121: // 'y'
			if ((l & 16384L) != 0L) return jjStartNfaWithStates_0(1, 14, 48);
			break;

		case 124: // '|'
			if ((l1 & 0x40000000000L) != 0L) return jjStopAtPos(1, 106);
			break;
		}
		return jjStartNfa_0(0, l, l1);
	}

	private final int jjMoveStringLiteralDfa2_0(long l, long l1, long l2, long l3) {
		if (((l1 &= l) | (l3 &= l2)) == 0L) return jjStartNfa_0(0, l, l2);
		try {
			curChar = input_stream.readChar();
		}
		catch (IOException ioexception) {
			jjStopStringLiteralDfa_0(1, l1, l3);
			return 2;
		}
		switch (curChar) {
		case 70: // 'F'
		case 72: // 'H'
		case 74: // 'J'
		case 80: // 'P'
		case 81: // 'Q'
		case 90: // 'Z'
		case 91: // '['
		case 92: // '\\'
		case 93: // ']'
		case 94: // '^'
		case 95: // '_'
		case 96: // '`'
		case 102: // 'f'
		case 104: // 'h'
		case 106: // 'j'
		case 112: // 'p'
		case 113: // 'q'
		default:
			break;

		case 65: // 'A'
		case 97: // 'a'
			return jjMoveStringLiteralDfa3_0(l1, 0xe180000000008000L, l3, 4L);

		case 66: // 'B'
		case 98: // 'b'
			return jjMoveStringLiteralDfa3_0(l1, 0L, l3, 2L);

		case 67: // 'C'
		case 99: // 'c'
			if ((l1 & 512L) != 0L) return jjStartNfaWithStates_0(2, 9, 48);
			// else
			return jjMoveStringLiteralDfa3_0(l1, 0x8001000000L, l3, 0L);

		case 68: // 'D'
		case 100: // 'd'
			if ((l1 & 64L) != 0L) return jjStartNfaWithStates_0(2, 6, 48);
			// else
			return jjMoveStringLiteralDfa3_0(l1, 0x10080000000000L, l3, 16L);

		case 69: // 'E'
		case 101: // 'e'
			return jjMoveStringLiteralDfa3_0(l1, 0L, l3, 256L);

		case 71: // 'G'
		case 103: // 'g'
			if ((l1 & 1024L) != 0L) return jjStartNfaWithStates_0(2, 10, 48);
			break;

		case 73: // 'I'
		case 105: // 'i'
			return jjMoveStringLiteralDfa3_0(l1, 0x60000006000000L, l3, 2056L);

		case 75: // 'K'
		case 107: // 'k'
			return jjMoveStringLiteralDfa3_0(l1, 0x4000000000L, l3, 0L);

		case 76: // 'L'
		case 108: // 'l'
			if ((l1 & 32L) != 0L) return jjStartNfaWithStates_0(2, 5, 48);

			return jjMoveStringLiteralDfa3_0(l1, 0xa04800000200000L, l3, 32L);

		case 77: // 'M'
		case 109: // 'm'
			if ((l3 & 1L) != 0L) return jjStartNfaWithStates_0(2, 64, 48);

			return jjMoveStringLiteralDfa3_0(l1, 0x1000000030000L, l3, 0L);

		case 78: // 'N'
		case 110: // 'n'
			if ((l1 & 0x20000000000L) != 0L) {
				jjmatchedKind = 41;
				jjmatchedPos = 2;
			}
			return jjMoveStringLiteralDfa3_0(l1, 0x40000041000L, l3, 0L);

		case 79: // 'O'
		case 111: // 'o'
			return jjMoveStringLiteralDfa3_0(l1, 0x68002000L, l3, 0L);

		case 82: // 'R'
		case 114: // 'r'
			if ((l1 & 0x10000000L) != 0L) return jjStartNfaWithStates_0(2, 28, 48);
			return jjMoveStringLiteralDfa3_0(l1, 0L, l3, 1216L);

		case 83: // 'S'
		case 115: // 's'
			return jjMoveStringLiteralDfa3_0(l1, 0x200c00000L, l3, 0L);

		case 84: // 'T'
		case 116: // 't'
			if ((l1 & 0x200000000000L) != 0L) return jjStartNfaWithStates_0(2, 45, 48);
			if ((l1 & 0x1000000000000000L) != 0L) return jjStartNfaWithStates_0(2, 60, 48);
			return jjMoveStringLiteralDfa3_0(l1, 0x101c00100800L, l3, 512L);

		case 85: // 'U'
		case 117: // 'u'
			return jjMoveStringLiteralDfa3_0(l1, 0x80000L, l3, 0L);

		case 86: // 'V'
		case 118: // 'v'
			return jjMoveStringLiteralDfa3_0(l1, 0x80000000L, l3, 0L);

		case 87: // 'W'
		case 119: // 'w'
			if ((l1 & 0x400000000000000L) != 0L) return jjStartNfaWithStates_0(2, 58, 48);
			return jjMoveStringLiteralDfa3_0(l1, 0x400000000000L, l3, 0L);

		case 88: // 'X'
		case 120: // 'x'
			if ((l1 & 0x10000000000L) != 0L) return jjStartNfaWithStates_0(2, 40, 48);
			break;

		case 89: // 'Y'
		case 121: // 'y'
			if ((l1 & 128L) != 0L) return jjStartNfaWithStates_0(2, 7, 48);
			break;
		}
		return jjStartNfa_0(1, l1, l3);
	}

	private final int jjMoveStringLiteralDfa3_0(long l, long l1, long l2, long l3) {
		if (((l1 &= l) | (l3 &= l2)) == 0L) return jjStartNfa_0(1, l, l2);
		try {
			curChar = input_stream.readChar();
		}
		catch (IOException ioexception) {
			jjStopStringLiteralDfa_0(2, l1, l3);
			return 3;
		}
		switch (curChar) {
		case 70: // 'F'
		case 71: // 'G'
		case 74: // 'J'
		case 80: // 'P'
		case 81: // 'Q'
		case 86: // 'V'
		case 88: // 'X'
		case 90: // 'Z'
		case 91: // '['
		case 92: // '\\'
		case 93: // ']'
		case 94: // '^'
		case 95: // '_'
		case 96: // '`'
		case 102: // 'f'
		case 103: // 'g'
		case 106: // 'j'
		case 112: // 'p'
		case 113: // 'q'
		case 118: // 'v'
		case 120: // 'x'
		default:
			break;

		case 65: // 'A'
		case 97: // 'a'
			return jjMoveStringLiteralDfa4_0(l1, 0x400008001000L, l3, 16L);

		case 66: // 'B'
		case 98: // 'b'
			return jjMoveStringLiteralDfa4_0(l1, 0x1000000000000L, l3, 0L);

		case 67: // 'C'
		case 99: // 'c'
			if ((l1 & 0x400000L) != 0L) return jjStartNfaWithStates_0(3, 22, 48);
			return jjMoveStringLiteralDfa4_0(l1, 0L, l3, 192L);

		case 68: // 'D'
		case 100: // 'd'
			if ((l1 & 0x80000000000000L) != 0L) return jjStartNfaWithStates_0(3, 55, 48);
			break;

		case 69: // 'E'
		case 101: // 'e'
			if ((l1 & 0x100000L) != 0L) return jjStartNfaWithStates_0(3, 20, 48);
			if ((l1 & 0x4000000000L) != 0L) return jjStartNfaWithStates_0(3, 38, 48);
			if ((l1 & 0x80000000000L) != 0L) return jjStartNfaWithStates_0(3, 43, 48);
			return jjMoveStringLiteralDfa4_0(l1, 0x810000e00200000L, l3, 0L);

		case 72: // 'H'
		case 104: // 'h'
			if ((l3 & 512L) != 0L) return jjStartNfaWithStates_0(3, 73, 48);
			break;

		case 73: // 'I'
		case 105: // 'i'
			return jjMoveStringLiteralDfa4_0(l1, 0x80000000L, l3, 0L);

		case 75: // 'K'
		case 107: // 'k'
			if ((l1 & 0x8000000000L) != 0L) return jjStartNfaWithStates_0(3, 39, 48);
			if ((l3 & 1024L) != 0L) return jjStartNfaWithStates_0(3, 74, 48);
			break;

		case 76: // 'L'
		case 108: // 'l'
			if ((l1 & 0x800000000000L) != 0L) return jjStartNfaWithStates_0(3, 47, 48);
			if ((l1 & 0x100000000000000L) != 0L) return jjStartNfaWithStates_0(3, 56, 48);
			return jjMoveStringLiteralDfa4_0(l1, 0x4200000001002000L, l3, 2L);

		case 77: // 'M'
		case 109: // 'm'
			if ((l1 & 0x20000000L) != 0L) return jjStartNfaWithStates_0(3, 29, 48);
			return jjMoveStringLiteralDfa4_0(l1, 0x30000L, l3, 0L);

		case 78: // 'N'
		case 110: // 'n'
			return jjMoveStringLiteralDfa4_0(l1, 0xc0000L, l3, 4L);

		case 79: // 'O'
		case 111: // 'o'
			if ((l1 & 0x1000000000L) != 0L) return jjStartNfaWithStates_0(3, 36, 48);
			return jjMoveStringLiteralDfa4_0(l1, 0x20000000000000L, l3, 8L);

		case 82: // 'R'
		case 114: // 'r'
			if ((l1 & 32768L) != 0L) return jjStartNfaWithStates_0(3, 15, 48);
			return jjMoveStringLiteralDfa4_0(l1, 0xa000000000000000L, l3, 256L);

		case 83: // 'S'
		case 115: // 's'
			return jjMoveStringLiteralDfa4_0(l1, 0x2000000L, l3, 0L);

		case 84: // 'T'
		case 116: // 't'
			if ((l1 & 0x4000000L) != 0L) return jjStartNfaWithStates_0(3, 26, 48);
			if ((l1 & 0x40000000000000L) != 0L) return jjStartNfaWithStates_0(3, 54, 48);
			return jjMoveStringLiteralDfa4_0(l1, 0x800000L, l3, 2048L);

		case 85: // 'U'
		case 117: // 'u'
			return jjMoveStringLiteralDfa4_0(l1, 0x140040000000L, l3, 32L);

		case 87: // 'W'
		case 119: // 'w'
			return jjMoveStringLiteralDfa4_0(l1, 2048L, l3, 0L);

		case 89: // 'Y'
		case 121: // 'y'
			if ((l1 & 0x4000000000000L) != 0L) return jjStartNfaWithStates_0(3, 50, 48);
			break;
		}
		return jjStartNfa_0(2, l1, l3);
	}

	private final int jjMoveStringLiteralDfa4_0(long l, long l1, long l2, long l3) {
		if (((l1 &= l) | (l3 &= l2)) == 0L) return jjStartNfa_0(2, l, l2);
		try {
			curChar = input_stream.readChar();
		}
		catch (IOException ioexception) {
			jjStopStringLiteralDfa_0(3, l1, l3);
			return 4;
		}
		switch (curChar) {
		case 68: // 'D'
		case 70: // 'F'
		case 74: // 'J'
		case 75: // 'K'
		case 77: // 'M'
		case 79: // 'O'
		case 81: // 'Q'
		case 86: // 'V'
		case 87: // 'W'
		case 88: // 'X'
		case 89: // 'Y'
		case 90: // 'Z'
		case 91: // '['
		case 92: // '\\'
		case 93: // ']'
		case 94: // '^'
		case 95: // '_'
		case 96: // '`'
		case 97: // 'a'
		case 100: // 'd'
		case 102: // 'f'
		case 106: // 'j'
		case 107: // 'k'
		case 109: // 'm'
		case 111: // 'o'
		case 113: // 'q'
		default:
			break;

		case 66: // 'B'
		case 98: // 'b'
			return jjMoveStringLiteralDfa5_0(l1, 0x200000000000000L, l3, 0L);

		case 67: // 'C'
		case 99: // 'c'
			return jjMoveStringLiteralDfa5_0(l1, 0x800000000000000L, l3, 0L);

		case 69: // 'E'
		case 101: // 'e'
			if ((l1 & 0x2000000000000000L) != 0L) return jjStartNfaWithStates_0(4, 61, 48);
			if ((l3 & 2L) != 0L) return jjStartNfaWithStates_0(4, 65, 48);
			if ((l3 & 256L) != 0L) return jjStartNfaWithStates_0(4, 72, 48);
			if ((l3 & 2048L) != 0L) return jjStartNfaWithStates_0(4, 75, 48);
			return jjMoveStringLiteralDfa5_0(l1, 0x1000000052800L, l3, 32L);

		case 71: // 'G'
		case 103: // 'g'
			return jjMoveStringLiteralDfa5_0(l1, 0x400000000L, l3, 0L);

		case 72: // 'H'
		case 104: // 'h'
			return jjMoveStringLiteralDfa5_0(l1, 0L, l3, 192L);

		case 73: // 'I'
		case 105: // 'i'
			return jjMoveStringLiteralDfa5_0(l1, 0x400000820000L, l3, 0L);

		case 76: // 'L'
		case 108: // 'l'
			return jjMoveStringLiteralDfa5_0(l1, 0x4000000000000000L, l3, 0L);

		case 78: // 'N'
		case 110: // 'n'
			if ((l3 & 8L) != 0L) return jjStartNfaWithStates_0(4, 67, 48);
			return jjMoveStringLiteralDfa5_0(l1, 0x80000000L, l3, 0L);

		case 80: // 'P'
		case 112: // 'p'
			if ((l1 & 0x40000000L) != 0L) return jjStartNfaWithStates_0(4, 30, 48);
			break;

		case 82: // 'R'
		case 114: // 'r'
			if ((l1 & 0x10000000000000L) != 0L) return jjStartNfaWithStates_0(4, 52, 48);
			if ((l1 & 0x20000000000000L) != 0L) return jjStartNfaWithStates_0(4, 53, 48);
			return jjMoveStringLiteralDfa5_0(l1, 0x100a00001000L, l3, 0L);

		case 83: // 'S'
		case 115: // 's'
			if ((l1 & 0x40000000000L) != 0L) return jjStartNfaWithStates_0(4, 42, 48);
			return jjMoveStringLiteralDfa5_0(l1, 0L, l3, 4L);

		case 84: // 'T'
		case 116: // 't'
			if ((l1 & 0x80000L) != 0L) return jjStartNfaWithStates_0(4, 19, 48);
			if ((l1 & 0x8000000L) != 0L) return jjStartNfaWithStates_0(4, 27, 48);
			if ((l1 & 0x8000000000000000L) != 0L) return jjStartNfaWithStates_0(4, 63, 48);
			return jjMoveStringLiteralDfa5_0(l1, 0x2200000L, l3, 16L);

		case 85: // 'U'
		case 117: // 'u'
			return jjMoveStringLiteralDfa5_0(l1, 0x1000000L, l3, 0L);
		}
		return jjStartNfa_0(3, l1, l3);
	}

	private final int jjMoveStringLiteralDfa5_0(long l, long l1, long l2, long l3) {
		if (((l1 &= l) | (l3 &= l2)) == 0L) return jjStartNfa_0(3, l, l2);
		try {
			curChar = input_stream.readChar();
		}
		catch (IOException ioexception) {
			jjStopStringLiteralDfa_0(4, l1, l3);
			return 5;
		}
		switch (curChar) {
		case 66: // 'B'
		case 68: // 'D'
		case 70: // 'F'
		case 72: // 'H'
		case 74: // 'J'
		case 75: // 'K'
		case 76: // 'L'
		case 77: // 'M'
		case 79: // 'O'
		case 80: // 'P'
		case 81: // 'Q'
		case 85: // 'U'
		case 86: // 'V'
		case 87: // 'W'
		case 88: // 'X'
		case 90: // 'Z'
		case 91: // '['
		case 92: // '\\'
		case 93: // ']'
		case 94: // '^'
		case 95: // '_'
		case 96: // '`'
		case 98: // 'b'
		case 100: // 'd'
		case 102: // 'f'
		case 104: // 'h'
		case 106: // 'j'
		case 107: // 'k'
		case 108: // 'l'
		case 109: // 'm'
		case 111: // 'o'
		case 112: // 'p'
		case 113: // 'q'
		case 117: // 'u'
		case 118: // 'v'
		case 119: // 'w'
		case 120: // 'x'
		default:
			break;

		case 65: // 'A'
		case 97: // 'a'
			return jjMoveStringLiteralDfa6_0(l1, 0x200100000002000L, l3, 196L);

		case 67: // 'C'
		case 99: // 'c'
			return jjMoveStringLiteralDfa6_0(l1, 0x40000L, l3, 0L);

		case 69: // 'E'
		case 101: // 'e'
			if ((l1 & 0x200000L) != 0L) return jjStartNfaWithStates_0(5, 21, 48);
			if ((l3 & 16L) != 0L) return jjStartNfaWithStates_0(5, 68, 48);
			return jjMoveStringLiteralDfa6_0(l1, 0x400000800L, l3, 0L);

		case 71: // 'G'
		case 103: // 'g'
			if ((l1 & 0x80000000L) != 0L) return jjStartNfaWithStates_0(5, 31, 48);
			break;

		case 73: // 'I'
		case 105: // 'i'
			return jjMoveStringLiteralDfa6_0(l1, 0x4000000000000000L, l3, 0L);

		case 78: // 'N'
		case 110: // 'n'
			return jjMoveStringLiteralDfa6_0(l1, 0x810000L, l3, 0L);

		case 82: // 'R'
		case 114: // 'r'
			if ((l1 & 0x1000000000000L) != 0L) return jjStartNfaWithStates_0(5, 48, 48);
			break;

		case 83: // 'S'
		case 115: // 's'
			if ((l1 & 0x2000000L) != 0L) return jjStartNfaWithStates_0(5, 25, 48);
			if ((l3 & 32L) != 0L) return jjStartNfaWithStates_0(5, 69, 48);
			return jjMoveStringLiteralDfa6_0(l1, 0x801000000L, l3, 0L);

		case 84: // 'T'
		case 116: // 't'
			if ((l1 & 0x20000L) != 0L) return jjStartNfaWithStates_0(5, 17, 48);
			if ((l1 & 0x200000000L) != 0L) return jjStartNfaWithStates_0(5, 33, 48);
			if ((l1 & 0x400000000000L) != 0L) return jjStartNfaWithStates_0(5, 46, 48);
			if ((l1 & 0x800000000000000L) != 0L) return jjStartNfaWithStates_0(5, 59, 48);
			break;

		case 89: // 'Y'
		case 121: // 'y'
			return jjMoveStringLiteralDfa6_0(l1, 4096L, l3, 0L);
		}
		return jjStartNfa_0(4, l1, l3);
	}

	private final int jjMoveStringLiteralDfa6_0(long l, long l1, long l2, long l3) {
		if (((l1 &= l) | (l3 &= l2)) == 0L) return jjStartNfa_0(4, l, l2);
		try {
			curChar = input_stream.readChar();
		}
		catch (IOException ioexception) {
			jjStopStringLiteralDfa_0(5, l1, l3);
			return 6;
		}
		switch (curChar) {
		case 68: // 'D'
		case 70: // 'F'
		case 71: // 'G'
		case 72: // 'H'
		case 74: // 'J'
		case 75: // 'K'
		case 77: // 'M'
		case 79: // 'O'
		case 80: // 'P'
		case 81: // 'Q'
		case 83: // 'S'
		case 85: // 'U'
		case 86: // 'V'
		case 87: // 'W'
		case 88: // 'X'
		case 89: // 'Y'
		case 90: // 'Z'
		case 91: // '['
		case 92: // '\\'
		case 93: // ']'
		case 94: // '^'
		case 96: // '`'
		case 97: // 'a'
		case 98: // 'b'
		case 100: // 'd'
		case 102: // 'f'
		case 103: // 'g'
		case 104: // 'h'
		case 106: // 'j'
		case 107: // 'k'
		case 109: // 'm'
		case 111: // 'o'
		case 112: // 'p'
		case 113: // 'q'
		case 115: // 's'
		default:
			break;

		case 95: // '_'
			return jjMoveStringLiteralDfa7_0(l1, 4096L, l3, 0L);

		case 67: // 'C'
		case 99: // 'c'
			return jjMoveStringLiteralDfa7_0(l1, 0x200000000800000L, l3, 4L);

		case 69: // 'E'
		case 101: // 'e'
			return jjMoveStringLiteralDfa7_0(l1, 0x800000000L, l3, 0L);

		case 73: // 'I'
		case 105: // 'i'
			return jjMoveStringLiteralDfa7_0(l1, 0x1000000L, l3, 0L);

		case 76: // 'L'
		case 108: // 'l'
			if ((l1 & 0x100000000000L) != 0L) return jjStartNfaWithStates_0(6, 44, 48);
			break;

		case 78: // 'N'
		case 110: // 'n'
			if ((l1 & 2048L) != 0L) return jjStartNfaWithStates_0(6, 11, 48);
			if ((l1 & 8192L) != 0L) return jjStartNfaWithStates_0(6, 13, 48);
			return jjMoveStringLiteralDfa7_0(l1, 0x4000000000000000L, l3, 0L);

		case 82: // 'R'
		case 114: // 'r'
			if ((l1 & 0x400000000L) != 0L) return jjStartNfaWithStates_0(6, 34, 48);
			if ((l3 & 128L) != 0L) {
				jjmatchedKind = 71;
				jjmatchedPos = 6;
			}
			return jjMoveStringLiteralDfa7_0(l1, 0L, l3, 64L);

		case 84: // 'T'
		case 116: // 't'
			if ((l1 & 0x10000L) != 0L) return jjStartNfaWithStates_0(6, 16, 48);
			if ((l1 & 0x40000L) != 0L) return jjStartNfaWithStates_0(6, 18, 48);
			break;
		}
		return jjStartNfa_0(5, l1, l3);
	}

	private final int jjMoveStringLiteralDfa7_0(long l, long l1, long l2, long l3) {
		if (((l1 &= l) | (l3 &= l2)) == 0L) return jjStartNfa_0(5, l, l2);
		try {
			curChar = input_stream.readChar();
		}
		catch (IOException ioexception) {
			jjStopStringLiteralDfa_0(6, l1, l3);
			return 7;
		}
		switch (curChar) {
		default:
			break;

		case 50: // '2'
			if ((l3 & 64L) != 0L) return jjStartNfaWithStates_0(7, 70, 11);
			break;

		case 67: // 'C'
		case 99: // 'c'
			return jjMoveStringLiteralDfa8_0(l1, 0x800000000L, l3, 0L);

		case 73: // 'I'
		case 105: // 'i'
			return jjMoveStringLiteralDfa8_0(l1, 4096L, l3, 0L);

		case 75: // 'K'
		case 107: // 'k'
			if ((l1 & 0x200000000000000L) != 0L) return jjStartNfaWithStates_0(7, 57, 48);
			break;

		case 84: // 'T'
		case 116: // 't'
			if ((l1 & 0x800000L) != 0L) return jjStartNfaWithStates_0(7, 23, 48);
			if ((l1 & 0x4000000000000000L) != 0L) return jjStartNfaWithStates_0(7, 62, 48);
			return jjMoveStringLiteralDfa8_0(l1, 0L, l3, 4L);

		case 86: // 'V'
		case 118: // 'v'
			return jjMoveStringLiteralDfa8_0(l1, 0x1000000L, l3, 0L);
		}
		return jjStartNfa_0(6, l1, l3);
	}

	private final int jjMoveStringLiteralDfa8_0(long l, long l1, long l2, long l3) {
		if (((l1 &= l) | (l3 &= l2)) == 0L) return jjStartNfa_0(6, l, l2);
		try {
			curChar = input_stream.readChar();
		}
		catch (IOException ioexception) {
			jjStopStringLiteralDfa_0(7, l1, l3);
			return 8;
		}
		switch (curChar) {
		default:
			break;

		case 69: // 'E'
		case 101: // 'e'
			if ((l1 & 0x1000000L) != 0L) return jjStartNfaWithStates_0(8, 24, 48);
			break;

		case 73: // 'I'
		case 105: // 'i'
			return jjMoveStringLiteralDfa9_0(l1, 0L, l3, 4L);

		case 78: // 'N'
		case 110: // 'n'
			return jjMoveStringLiteralDfa9_0(l1, 4096L, l3, 0L);

		case 84: // 'T'
		case 116: // 't'
			if ((l1 & 0x800000000L) != 0L) return jjStartNfaWithStates_0(8, 35, 48);
			break;
		}
		return jjStartNfa_0(7, l1, l3);
	}

	private final int jjMoveStringLiteralDfa9_0(long l, long l1, long l2, long l3) {
		if (((l1 &= l) | (l3 &= l2)) == 0L) return jjStartNfa_0(7, l, l2);
		try {
			curChar = input_stream.readChar();
		}
		catch (IOException ioexception) {
			jjStopStringLiteralDfa_0(8, l1, l3);
			return 9;
		}
		switch (curChar) {
		case 79: // 'O'
		case 111: // 'o'
			return jjMoveStringLiteralDfa10_0(l1, 0L, l3, 4L);

		case 84: // 'T'
		case 116: // 't'
			return jjMoveStringLiteralDfa10_0(l1, 4096L, l3, 0L);
		}
		return jjStartNfa_0(8, l1, l3);
	}

	private final int jjMoveStringLiteralDfa10_0(long l, long l1, long l2, long l3) {
		if (((l1 &= l) | (l3 &= l2)) == 0L) return jjStartNfa_0(8, l, l2);
		try {
			curChar = input_stream.readChar();
		}
		catch (IOException ioexception) {
			jjStopStringLiteralDfa_0(9, l1, l3);
			return 10;
		}
		switch (curChar) {
		case 69: // 'E'
		case 101: // 'e'
			return jjMoveStringLiteralDfa11_0(l1, 4096L, l3, 0L);

		case 78: // 'N'
		case 110: // 'n'
			if ((l3 & 4L) != 0L) return jjStartNfaWithStates_0(10, 66, 48);
			break;
		}
		return jjStartNfa_0(9, l1, l3);
	}

	private final int jjMoveStringLiteralDfa11_0(long l, long l1, long l2, long l3) {
		if (((l1 &= l) | (l3 &= l2)) == 0L) return jjStartNfa_0(9, l, l2);
		try {
			curChar = input_stream.readChar();
		}
		catch (IOException ioexception) {
			jjStopStringLiteralDfa_0(10, l1, 0L);
			return 11;
		}
		switch (curChar) {
		case 71: // 'G'
		case 103: // 'g'
			return jjMoveStringLiteralDfa12_0(l1, 4096L);
		}
		return jjStartNfa_0(10, l1, 0L);
	}

	private final int jjMoveStringLiteralDfa12_0(long l, long l1) {
		if ((l1 &= l) == 0L) return jjStartNfa_0(10, l, 0L);
		try {
			curChar = input_stream.readChar();
		}
		catch (IOException ioexception) {
			jjStopStringLiteralDfa_0(11, l1, 0L);
			return 12;
		}
		switch (curChar) {
		case 69: // 'E'
		case 101: // 'e'
			return jjMoveStringLiteralDfa13_0(l1, 4096L);
		}
		return jjStartNfa_0(11, l1, 0L);
	}

	private final int jjMoveStringLiteralDfa13_0(long l, long l1) {
		if ((l1 &= l) == 0L) return jjStartNfa_0(11, l, 0L);
		try {
			curChar = input_stream.readChar();
		}
		catch (IOException ioexception) {
			jjStopStringLiteralDfa_0(12, l1, 0L);
			return 13;
		}
		switch (curChar) {
		case 82: // 'R'
		case 114: // 'r'
			if ((l1 & 4096L) != 0L) return jjStartNfaWithStates_0(13, 12, 11);
			break;
		}
		return jjStartNfa_0(12, l1, 0L);
	}

	private final void jjCheckNAdd(int i) {
		if (jjrounds[i] != jjround) {
			jjstateSet[jjnewStateCnt++] = i;
			jjrounds[i] = jjround;
		}
	}

	private final void jjAddStates(int i, int j) {
		do
			jjstateSet[jjnewStateCnt++] = jjnextStates[i];
		while (i++ != j);
	}

	private final void jjCheckNAddTwoStates(int i, int j) {
		jjCheckNAdd(i);
		jjCheckNAdd(j);
	}

	private final void jjCheckNAddStates(int i, int j) {
		do
			jjCheckNAdd(jjnextStates[i]);
		while (i++ != j);
	}

	private final void jjCheckNAddStates(int i) {
		jjCheckNAdd(jjnextStates[i]);
		jjCheckNAdd(jjnextStates[i + 1]);
	}

	private final int jjMoveNfa_0(int i, int j) {
		int k = 0;
		jjnewStateCnt = 47;
		int l = 1;
		jjstateSet[0] = i;
		int i1 = 0x7fffffff;
		do {
			if (++jjround == 0x7fffffff) ReInitRounds();
			if (curChar < '@') {
				long l1 = 1L << curChar;
				do
					switch (jjstateSet[--l]) {
					case 2: // '\002'
						if ((0x3ff000000000000L & l1) != 0L) {
							if (i1 > 76) i1 = 76;
							jjCheckNAddStates(0, 6);
						}
						else if (curChar == '.') jjCheckNAddTwoStates(27, 37);
						else if (curChar == '"') jjCheckNAddTwoStates(24, 25);
						else if (curChar == '\'') jjCheckNAddTwoStates(19, 20);
						else if (curChar == ':') jjstateSet[jjnewStateCnt++] = 13;
						else if (curChar == '/') jjstateSet[jjnewStateCnt++] = 3;
						else if (curChar == '-') jjstateSet[jjnewStateCnt++] = 0;
						break;

					case 11: // '\013'
					case 48: // '0'
						if ((0x3ff001000000000L & l1) != 0L) {
							if (i1 > 82) i1 = 82;
							jjCheckNAdd(11);
						}
						break;

					case 47: // '/'
						if ((0x3ff000000000000L & l1) != 0L) {
							if (i1 > 76) i1 = 76;
							jjCheckNAdd(37);
						}
						if ((0x3ff000000000000L & l1) != 0L) {
							if (i1 > 76) i1 = 76;
							jjCheckNAddTwoStates(27, 28);
						}
						break;

					case 0: // '\0'
						if (curChar == '-') {
							if (i1 > 80) i1 = 80;
							jjCheckNAdd(1);
						}
						break;

					case 1: // '\001'
						if ((-9217L & l1) != 0L) {
							if (i1 > 80) i1 = 80;
							jjCheckNAdd(1);
						}
						break;

					case 3: // '\003'
						if (curChar == '*') jjCheckNAddTwoStates(4, 5);
						break;

					case 4: // '\004'
						if ((0xfffffbffffffffffL & l1) != 0L) jjCheckNAddTwoStates(4, 5);
						break;

					case 5: // '\005'
						if (curChar == '*') jjCheckNAddStates(7, 9);
						break;

					case 6: // '\006'
						if ((0xffff7bffffffffffL & l1) != 0L) jjCheckNAddTwoStates(7, 5);
						break;

					case 7: // '\007'
						if ((0xfffffbffffffffffL & l1) != 0L) jjCheckNAddTwoStates(7, 5);
						break;

					case 8: // '\b'
						if (curChar == '/' && i1 > 81) i1 = 81;
						break;

					case 9: // '\t'
						if (curChar == '/') jjstateSet[jjnewStateCnt++] = 3;
						break;

					case 12: // '\f'
						if (curChar == ':') jjstateSet[jjnewStateCnt++] = 13;
						break;

					case 14: // '\016'
						if ((0x3ff001000000000L & l1) != 0L) {
							if (i1 > 85) i1 = 85;
							jjAddStates(10, 11);
						}
						break;

					case 15: // '\017'
						if (curChar == '.') jjstateSet[jjnewStateCnt++] = 16;
						break;

					case 17: // '\021'
						if ((0x3ff001000000000L & l1) != 0L) {
							if (i1 > 85) i1 = 85;
							jjstateSet[jjnewStateCnt++] = 17;
						}
						break;

					case 18: // '\022'
						if (curChar == '\'') jjCheckNAddTwoStates(19, 20);
						break;

					case 19: // '\023'
						if ((0xffffff7fffffffffL & l1) != 0L) jjCheckNAddTwoStates(19, 20);
						break;

					case 20: // '\024'
						if (curChar == '\'') {
							if (i1 > 86) i1 = 86;
							jjstateSet[jjnewStateCnt++] = 21;
						}
						break;

					case 21: // '\025'
						if (curChar == '\'') jjCheckNAddTwoStates(22, 20);
						break;

					case 22: // '\026'
						if ((0xffffff7fffffffffL & l1) != 0L) jjCheckNAddTwoStates(22, 20);
						break;

					case 23: // '\027'
						if (curChar == '"') jjCheckNAddTwoStates(24, 25);
						break;

					case 24: // '\030'
						if ((0xfffffffbffffdbffL & l1) != 0L) jjCheckNAddTwoStates(24, 25);
						break;

					case 25: // '\031'
						if (curChar == '"' && i1 > 87) i1 = 87;
						break;

					case 26: // '\032'
						if (curChar == '.') jjCheckNAddTwoStates(27, 37);
						break;

					case 27: // '\033'
						if ((0x3ff000000000000L & l1) != 0L) {
							if (i1 > 76) i1 = 76;
							jjCheckNAddTwoStates(27, 28);
						}
						break;

					case 29: // '\035'
						if ((0x280000000000L & l1) != 0L) jjAddStates(12, 13);
						break;

					case 30: // '\036'
						if (curChar == '.') jjCheckNAdd(31);
						break;

					case 31: // '\037'
						if ((0x3ff000000000000L & l1) != 0L) {
							if (i1 > 76) i1 = 76;
							jjCheckNAdd(31);
						}
						break;

					case 32: // ' '
						if ((0x3ff000000000000L & l1) != 0L) {
							if (i1 > 76) i1 = 76;
							jjCheckNAddStates(14, 16);
						}
						break;

					case 33: // '!'
						if ((0x3ff000000000000L & l1) != 0L) {
							if (i1 > 76) i1 = 76;
							jjCheckNAdd(33);
						}
						break;

					case 34: // '"'
						if ((0x3ff000000000000L & l1) != 0L) {
							if (i1 > 76) i1 = 76;
							jjCheckNAddTwoStates(34, 35);
						}
						break;

					case 35: // '#'
						if (curChar == '.') jjCheckNAdd(36);
						break;

					case 36: // '$'
						if ((0x3ff000000000000L & l1) != 0L) {
							if (i1 > 76) i1 = 76;
							jjCheckNAdd(36);
						}
						break;

					case 37: // '%'
						if ((0x3ff000000000000L & l1) != 0L) {
							if (i1 > 76) i1 = 76;
							jjCheckNAdd(37);
						}
						break;

					case 38: // '&'
						if ((0x3ff000000000000L & l1) != 0L) {
							if (i1 > 76) i1 = 76;
							jjCheckNAddStates(0, 6);
						}
						break;

					case 39: // '\''
						if ((0x3ff000000000000L & l1) != 0L) {
							if (i1 > 76) i1 = 76;
							jjCheckNAddTwoStates(39, 28);
						}
						break;

					case 40: // '('
						if ((0x3ff000000000000L & l1) != 0L) {
							if (i1 > 76) i1 = 76;
							jjCheckNAddStates(17, 19);
						}
						break;

					case 41: // ')'
						if (curChar == '.') jjCheckNAdd(42);
						break;

					case 42: // '*'
						if ((0x3ff000000000000L & l1) != 0L) {
							if (i1 > 76) i1 = 76;
							jjCheckNAddTwoStates(42, 28);
						}
						break;

					case 43: // '+'
						if ((0x3ff000000000000L & l1) != 0L) {
							if (i1 > 76) i1 = 76;
							jjCheckNAddTwoStates(43, 44);
						}
						break;

					case 44: // ','
						if (curChar == '.') jjCheckNAdd(45);
						break;

					case 45: // '-'
						if ((0x3ff000000000000L & l1) != 0L) {
							if (i1 > 76) i1 = 76;
							jjCheckNAdd(45);
						}
						break;

					case 46: // '.'
						if ((0x3ff000000000000L & l1) != 0L) {
							if (i1 > 76) i1 = 76;
							jjCheckNAdd(46);
						}
						break;
					}
				while (l != k);
			}
			else if (curChar < '\200') {
				long l2 = 1L << (curChar & 0x3f);
				do
					switch (jjstateSet[--l]) {
					case 2: // '\002'
					case 10: // '\n'
						if ((0x7fffffe07fffffeL & l2) != 0L) {
							if (i1 > 82) i1 = 82;
							jjCheckNAddTwoStates(10, 11);
						}
						break;

					case 48: // '0'
						if ((0x7fffffe87fffffeL & l2) != 0L) {
							if (i1 > 82) i1 = 82;
							jjCheckNAdd(11);
						}
						if ((0x7fffffe07fffffeL & l2) != 0L) {
							if (i1 > 82) i1 = 82;
							jjCheckNAddTwoStates(10, 11);
						}
						break;

					case 1: // '\001'
						if (i1 > 80) i1 = 80;
						jjstateSet[jjnewStateCnt++] = 1;
						break;

					case 4: // '\004'
						jjCheckNAddTwoStates(4, 5);
						break;

					case 6: // '\006'
					case 7: // '\007'
						jjCheckNAddTwoStates(7, 5);
						break;

					case 11: // '\013'
						if ((0x7fffffe87fffffeL & l2) != 0L) {
							if (i1 > 82) i1 = 82;
							jjCheckNAdd(11);
						}
						break;

					case 13: // '\r'
						if ((0x7fffffe07fffffeL & l2) != 0L) {
							if (i1 > 85) i1 = 85;
							jjCheckNAddStates(20, 22);
						}
						break;

					case 14: // '\016'
						if ((0x7fffffe87fffffeL & l2) != 0L) {
							if (i1 > 85) i1 = 85;
							jjCheckNAddTwoStates(14, 15);
						}
						break;

					case 16: // '\020'
						if ((0x7fffffe07fffffeL & l2) != 0L) {
							if (i1 > 85) i1 = 85;
							jjCheckNAddTwoStates(16, 17);
						}
						break;

					case 17: // '\021'
						if ((0x7fffffe87fffffeL & l2) != 0L) {
							if (i1 > 85) i1 = 85;
							jjCheckNAdd(17);
						}
						break;

					case 19: // '\023'
						jjCheckNAddTwoStates(19, 20);
						break;

					case 22: // '\026'
						jjCheckNAddTwoStates(22, 20);
						break;

					case 24: // '\030'
						jjAddStates(23, 24);
						break;

					case 28: // '\034'
						if ((0x2000000020L & l2) != 0L) jjAddStates(25, 27);
						break;
					}
				while (l != k);
			}
			else {
				int j1 = (curChar & 0xff) >> 6;
				long l3 = 1L << (curChar & 0x3f);
				do
					switch (jjstateSet[--l]) {
					case 1: // '\001'
						if ((jjbitVec0[j1] & l3) != 0L) {
							if (i1 > 80) i1 = 80;
							jjstateSet[jjnewStateCnt++] = 1;
						}
						break;

					case 4: // '\004'
						if ((jjbitVec0[j1] & l3) != 0L) jjCheckNAddTwoStates(4, 5);
						break;

					case 6: // '\006'
					case 7: // '\007'
						if ((jjbitVec0[j1] & l3) != 0L) jjCheckNAddTwoStates(7, 5);
						break;

					case 19: // '\023'
						if ((jjbitVec0[j1] & l3) != 0L) jjCheckNAddTwoStates(19, 20);
						break;

					case 22: // '\026'
						if ((jjbitVec0[j1] & l3) != 0L) jjCheckNAddTwoStates(22, 20);
						break;

					case 24: // '\030'
						if ((jjbitVec0[j1] & l3) != 0L) jjAddStates(23, 24);
						break;
					}
				while (l != k);
			}
			if (i1 != 0x7fffffff) {
				jjmatchedKind = i1;
				jjmatchedPos = j;
				i1 = 0x7fffffff;
			}
			j++;
			if ((l = jjnewStateCnt) == (k = 47 - (jjnewStateCnt = k))) return j;
			try {
				curChar = input_stream.readChar();
			}
			catch (IOException ioexception) {
				return j;
			}
		}
		while (true);
	}

	public ZqlJJParserTokenManager(SimpleCharStream simplecharstream) {
		debugStream = System.out;
		jjrounds = new int[47];
		jjstateSet = new int[94];
		curLexState = 0;
		defaultLexState = 0;
		input_stream = simplecharstream;
	}

	public ZqlJJParserTokenManager(SimpleCharStream simplecharstream, int i) {
		this(simplecharstream);
		SwitchTo(i);
	}

	public void ReInit(SimpleCharStream simplecharstream) {
		jjmatchedPos = jjnewStateCnt = 0;
		curLexState = defaultLexState;
		input_stream = simplecharstream;
		ReInitRounds();
	}

	private final void ReInitRounds() {
		jjround = 0x80000001;
		for (int i = 47; i-- > 0;)
			jjrounds[i] = 0x80000000;

	}

	public void ReInit(SimpleCharStream simplecharstream, int i) {
		ReInit(simplecharstream);
		SwitchTo(i);
	}

	public void SwitchTo(int i) {
		if (i >= 1 || i < 0) {
			throw new TokenMgrError("Error: Ignoring invalid lexical state : " + i + ". State unchanged.", 2);
		}
		// else{
		curLexState = i;
		return;
		// }
	}

	private final Token jjFillToken() {
		Token token = Token.newToken(jjmatchedKind);
		token.kind = jjmatchedKind;
		String s = jjstrLiteralImages[jjmatchedKind];
		token.image = (s != null ? s : input_stream.GetImage());
		token.beginLine = input_stream.getBeginLine();
		token.beginColumn = input_stream.getBeginColumn();
		token.endLine = input_stream.getEndLine();
		token.endColumn = input_stream.getEndColumn();
		return token;
	}

	public final Token getNextToken() {
		Token token = null;
		int i = 0;
		do {
			try {
				curChar = input_stream.BeginToken();
			}
			catch (IOException ioexception) {
				jjmatchedKind = 0;
				Token token1 = jjFillToken();
				token1.specialToken = token;
				return token1;
			}
			try {
				input_stream.backup(0);
				for (; curChar <= ' ' && (0x100002600L & 1L << curChar) != 0L; curChar = input_stream.BeginToken()) {
				}
			}
			catch (IOException ioexception1) {
				continue;
			}
			jjmatchedKind = 0x7fffffff;
			jjmatchedPos = 0;
			i = jjMoveStringLiteralDfa0_0();
			if (jjmatchedKind == 0x7fffffff) break;
			if (jjmatchedPos + 1 < i) input_stream.backup(i - jjmatchedPos - 1);
			if ((jjtoToken[jjmatchedKind >> 6] & 1L << (jjmatchedKind & 0x3f)) != 0L) {
				Token token2 = jjFillToken();
				token2.specialToken = token;
				return token2;
			}
			if ((jjtoSpecial[jjmatchedKind >> 6] & 1L << (jjmatchedKind & 0x3f)) != 0L) {
				Token token3 = jjFillToken();
				if (token == null) {
					token = token3;
				}
				else {
					token3.specialToken = token;
					token = token.next = token3;
				}
			}
		}
		while (true);
		int j = input_stream.getEndLine();
		int k = input_stream.getEndColumn();
		String s = null;
		boolean flag = false;
		try {
			input_stream.readChar();
			input_stream.backup(1);
		}
		catch (IOException ioexception2) {
			flag = true;
			s = i > 1 ? input_stream.GetImage() : "";
			if (curChar == '\n' || curChar == '\r') {
				j++;
				k = 0;
			}
			else {
				k++;
			}
		}
		if (!flag) {
			input_stream.backup(1);
			s = i > 1 ? input_stream.GetImage() : "";
		}
		throw new TokenMgrError(flag, curLexState, j, k, s, curChar, 0);
	}

	public PrintStream debugStream;
	static final long jjbitVec0[] = { 0L, 0L, -1L, -1L };
	static final int jjnextStates[] = { 39, 40, 41, 28, 43, 44, 46, 5, 6, 8, 14, 15, 30, 32, 33, 34, 35, 40, 41, 28, 13, 14, 15, 24, 25, 29, 30, 32 };
	public static final String jjstrLiteralImages[] = { "", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, "(", ",", ")", ";", "=", ".", "!=", "#", "<>", ">", ">=", "<", "<=", "+", "-", "*", ".*", "?", "||",
			"/", "**" };
	public static final String lexStateNames[] = { "DEFAULT" };
	static final long jjtoToken[] = { -31L, 0x1fffffe41fffL };
	static final long jjtoSkip[] = { 30L, 0x30000L };
	static final long jjtoSpecial[] = { 0L, 0x30000L };
	private SimpleCharStream input_stream;
	private final int jjrounds[];
	private final int jjstateSet[];
	protected char curChar;
	int curLexState;
	int defaultLexState;
	int jjnewStateCnt;
	int jjround;
	int jjmatchedPos;
	int jjmatchedKind;

}