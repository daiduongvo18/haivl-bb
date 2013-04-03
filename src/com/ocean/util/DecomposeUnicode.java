package com.ocean.util;

import java.util.Hashtable;

/**
 * Class DecomposeUnicode is used to decompode composed unicode characters
 */
public class DecomposeUnicode {

	private static Hashtable UNICODE_TABLE;
	
	static{
		UNICODE_TABLE = new Hashtable();

		UNICODE_TABLE.put(new Character('\u1ea3'), "\u0061\u0309"); // a?
		UNICODE_TABLE.put(new Character('\u1ea1'), "\u0061\u0323"); // a.

		UNICODE_TABLE.put(new Character('\u1eaf'), "\u0103\u0301"); // a('
		UNICODE_TABLE.put(new Character('\u1eb1'), "\u0103\u0300"); // a(`
		UNICODE_TABLE.put(new Character('\u1eb3'), "\u0103\u0309"); // a(?
		UNICODE_TABLE.put(new Character('\u1eb5'), "\u0103\u0303"); // a(~
		UNICODE_TABLE.put(new Character('\u1eb7'), "\u0103\u0323"); // a(.

		UNICODE_TABLE.put(new Character('\u1ea5'), "\u00e2\u0301"); // a^'
		UNICODE_TABLE.put(new Character('\u1ea7'), "\u00e2\u0300"); // a^`
		UNICODE_TABLE.put(new Character('\u1ea9'), "\u00e2\u0309"); // a^?
		UNICODE_TABLE.put(new Character('\u1eab'), "\u00e2\u0303"); // a^~
		UNICODE_TABLE.put(new Character('\u1ead'), "\u00e2\u0323"); // a^.

		UNICODE_TABLE.put(new Character('\u1ebb'), "\u0065\u0309"); // e?
		UNICODE_TABLE.put(new Character('\u1ebd'), "\u0065\u0303"); // e~
		UNICODE_TABLE.put(new Character('\u1eb9'), "\u0065\u0323"); // e.

		UNICODE_TABLE.put(new Character('\u1ebf'), "\u00ea\u0301"); // e^'
		UNICODE_TABLE.put(new Character('\u1ec1'), "\u00ea\u0300"); // e^`
		UNICODE_TABLE.put(new Character('\u1ec3'), "\u00ea\u0309"); // e^?
		UNICODE_TABLE.put(new Character('\u1ec5'), "\u00ea\u0303"); // e^~
		UNICODE_TABLE.put(new Character('\u1ec7'), "\u00ea\u0323"); // e^.

		UNICODE_TABLE.put(new Character('\u1ec9'), "\u0069\u0309"); // i?
		UNICODE_TABLE.put(new Character('\u1ecb'), "\u0069\u0323"); // i.

		UNICODE_TABLE.put(new Character('\u1ecf'), "\u006f\u0309"); // o?
		UNICODE_TABLE.put(new Character('\u1ecd'), "\u006f\u0323"); // o.

		UNICODE_TABLE.put(new Character('\u1ed1'), "\u00f4\u0301"); // o^'
		UNICODE_TABLE.put(new Character('\u1ed3'), "\u00f4\u0300"); // o^`
		UNICODE_TABLE.put(new Character('\u1ed5'), "\u00f4\u0309"); // o^?
		UNICODE_TABLE.put(new Character('\u1ed7'), "\u00f4\u0303"); // o^~
		UNICODE_TABLE.put(new Character('\u1ed9'), "\u00f4\u0323"); // o^.

		UNICODE_TABLE.put(new Character('\u1edb'), "\u01a1\u0301"); // o+'
		UNICODE_TABLE.put(new Character('\u1edd'), "\u01a1\u0300"); // o+`
		UNICODE_TABLE.put(new Character('\u1edf'), "\u01a1\u0309"); // o+?
		UNICODE_TABLE.put(new Character('\u1ee1'), "\u01a1\u0303"); // o+~
		UNICODE_TABLE.put(new Character('\u1ee3'), "\u01a1\u0323"); // o+.

		UNICODE_TABLE.put(new Character('\u1ee7'), "\u0075\u0309"); // u?
		UNICODE_TABLE.put(new Character('\u1ee5'), "\u0075\u0323"); // u.

		UNICODE_TABLE.put(new Character('\u1ee9'), "\u01b0\u0301"); // u+'
		UNICODE_TABLE.put(new Character('\u1eeb'), "\u01b0\u0300"); // u+`
		UNICODE_TABLE.put(new Character('\u1eed'), "\u01b0\u0309"); // u+?
		UNICODE_TABLE.put(new Character('\u1eef'), "\u01b0\u0303"); // u+~
		UNICODE_TABLE.put(new Character('\u1ef1'), "\u01b0\u0323"); // u+.

		UNICODE_TABLE.put(new Character('\u1ef7'), "\u0079\u0309"); // y?
		UNICODE_TABLE.put(new Character('\u1ef9'), "\u0079\u0303"); // y~
		UNICODE_TABLE.put(new Character('\u1ef5'), "\u0079\u0323"); // y.

		// Capital

		UNICODE_TABLE.put(new Character('\u1ea2'), "\u0041\u0309");
		UNICODE_TABLE.put(new Character('\u1ea0'), "\u0041\u0323");

		UNICODE_TABLE.put(new Character('\u1eae'), "\u0102\u0301");
		UNICODE_TABLE.put(new Character('\u1eb0'), "\u0102\u0300");
		UNICODE_TABLE.put(new Character('\u1eb2'), "\u0102\u0309");
		UNICODE_TABLE.put(new Character('\u1eb4'), "\u0102\u0303");
		UNICODE_TABLE.put(new Character('\u1eb6'), "\u0102\u0323");

		UNICODE_TABLE.put(new Character('\u1ea4'), "\u00c2\u0301");
		UNICODE_TABLE.put(new Character('\u1ea6'), "\u00c2\u0300");
		UNICODE_TABLE.put(new Character('\u1ea8'), "\u00c2\u0309");
		UNICODE_TABLE.put(new Character('\u1eaa'), "\u00c2\u0303");
		UNICODE_TABLE.put(new Character('\u1eac'), "\u00c2\u0323");

		UNICODE_TABLE.put(new Character('\u1eba'), "\u0045\u0309");
		UNICODE_TABLE.put(new Character('\u1ebc'), "\u0045\u0303");
		UNICODE_TABLE.put(new Character('\u1eb8'), "\u0045\u0323");

		UNICODE_TABLE.put(new Character('\u1ebe'), "\u00ca\u0301");
		UNICODE_TABLE.put(new Character('\u1ec0'), "\u00ca\u0300");
		UNICODE_TABLE.put(new Character('\u1ec2'), "\u00ca\u0309");
		UNICODE_TABLE.put(new Character('\u1ec4'), "\u00ca\u0303");
		UNICODE_TABLE.put(new Character('\u1ec6'), "\u00ca\u0323");

		UNICODE_TABLE.put(new Character('\u1ec8'), "\u0049\u0309");
		UNICODE_TABLE.put(new Character('\u1eca'), "\u0049\u0323");

		UNICODE_TABLE.put(new Character('\u1ece'), "\u004f\u0309");
		UNICODE_TABLE.put(new Character('\u1ecc'), "\u004f\u0323");

		UNICODE_TABLE.put(new Character('\u1ed0'), "\u00d4\u0301");
		UNICODE_TABLE.put(new Character('\u1ed2'), "\u00d4\u0300");
		UNICODE_TABLE.put(new Character('\u1ed4'), "\u00d4\u0309");
		UNICODE_TABLE.put(new Character('\u1ed6'), "\u00d4\u0303");
		UNICODE_TABLE.put(new Character('\u1ed8'), "\u00d4\u0323");

		UNICODE_TABLE.put(new Character('\u1eda'), "\u01a0\u0301");
		UNICODE_TABLE.put(new Character('\u1edc'), "\u01a0\u0300");
		UNICODE_TABLE.put(new Character('\u1ede'), "\u01a0\u0309");
		UNICODE_TABLE.put(new Character('\u1ee0'), "\u01a0\u0303");
		UNICODE_TABLE.put(new Character('\u1ee2'), "\u01a0\u0323");

		UNICODE_TABLE.put(new Character('\u1ee6'), "\u0055\u0309");
		UNICODE_TABLE.put(new Character('\u1ee4'), "\u0055\u0323");

		UNICODE_TABLE.put(new Character('\u1ee8'), "\u01af\u0301");
		UNICODE_TABLE.put(new Character('\u1eea'), "\u01af\u0300");
		UNICODE_TABLE.put(new Character('\u1eec'), "\u01af\u0309");
		UNICODE_TABLE.put(new Character('\u1eee'), "\u01af\u0303");
		UNICODE_TABLE.put(new Character('\u1ef0'), "\u01af\u0323");

		UNICODE_TABLE.put(new Character('\u1ef6'), "\u0059\u0309");
		UNICODE_TABLE.put(new Character('\u1ef8'), "\u0059\u0303");
		UNICODE_TABLE.put(new Character('\u1ef4'), "\u0059\u0323");
		
	}
	// Constructor
	public DecomposeUnicode() {

		

	}

	/**
	 * Convert the composed unicode character to decomposed unicode String.
	 * 
	 * @param composedUniChar
	 *            The composed unicode character.
	 * @return The decomposed unicode string.
	 */
	public static String getUnicodeComposeString(char composedUniChar) {
		// char aChar = new Character(composedUniChar);
		if (UNICODE_TABLE.get(new Character(composedUniChar)) != null) {
			return (String) UNICODE_TABLE.get(new Character(composedUniChar));
		} else {
			return composedUniChar + "";
		}
	}

	/**
	 * Decompose composed unicode string and return new string.
	 * 
	 * @param composedUniStr
	 *            The composed unicode string.
	 * @return The decomposed unicode string.
	 */
	public static String getUnicodeComposeString(String composedUniStr) {
		String newStr = "";
		for (int i = 0; i < composedUniStr.length(); i++) {
			if (UNICODE_TABLE.get(new Character(composedUniStr.charAt(i))) != null) {
				newStr += (String) UNICODE_TABLE.get(new Character(
						composedUniStr.charAt(i)));
			} else {
				newStr += composedUniStr.charAt(i);
			}
		}
		return newStr;
	}
}
