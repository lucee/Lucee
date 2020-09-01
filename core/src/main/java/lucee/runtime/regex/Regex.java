package lucee.runtime.regex;

import org.apache.oro.text.regex.MalformedPatternException;

import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;

public interface Regex {

	public boolean matches(String strPattern, String strInput) throws PageException;

	public boolean matches(String strPattern, String strInput, boolean defaultValue);

	public String match(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException;

	public Array matchAll(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException;

	/**
	 * return index of the first occurrence of the pattern in input text
	 * 
	 * @param strPattern pattern to search
	 * @param strInput text to search pattern
	 * @param offset
	 * @param caseSensitive
	 * @return position of the first occurrence
	 */
	public int indexOf(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException;

	/**
	 * return index of all occurrences of the pattern in input text as an array of int
	 * 
	 * @param strPattern pattern to search
	 * @param strInput text to search pattern
	 * @param offset
	 * @param caseSensitive
	 * @return position of the first occurrence
	 */
	public Array indexOfAll(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException;

	/**
	 * find occurrence of a pattern in a string (same like indexOf), but it returns a struct with more
	 * details
	 * 
	 * @param strPattern
	 * @param strInput
	 * @param offset
	 * @param caseSensitive
	 * @return
	 * @throws MalformedPatternException
	 */
	public Struct find(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException;

	/**
	 * find occurrence of a pattern in a string (same like indexOfAll), but it returns a struct with
	 * more details
	 * 
	 * @param strPattern
	 * @param strInput
	 * @param offset
	 * @param caseSensitive
	 * @return
	 * @throws MalformedPatternException
	 */
	public Array findAll(String strPattern, String strInput, int offset, boolean caseSensitive, boolean multiLine) throws PageException;

	public String replace(String strInput, String strPattern, String replacement, boolean caseSensitive, boolean multiLine) throws PageException;

	public String replaceAll(String strInput, String strPattern, String replacement, boolean caseSensitive, boolean multiLine) throws PageException;

	public String getTypeName();

}
