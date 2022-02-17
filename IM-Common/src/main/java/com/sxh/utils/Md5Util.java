package com.sxh.utils;

import org.springframework.util.DigestUtils;

/**
 *
 * @author sxh
 * @date 2022年2月16日
 * @Description
 *
*/
public class Md5Util {
	private static final String slat = "&%5123***&&%%$$#@";

	/**
	 * Md5加密，不使用salt
	 * @param str
	 * @return
	 */
	public static String encode(String str) {
		return DigestUtils.md5DigestAsHex(str.getBytes());
	}

	/**
	 * Md5加密，使用固定salt
	 * @param str
	 * @return
	 */
	public static String encodeWithSalt(String str) {
		String base = str +"/"+slat;
		return DigestUtils.md5DigestAsHex(base.getBytes());
	}

	/**
	 * Md5加密，使用固定salt
	 * @param str
	 * @return
	 */
	public static String encodeWithSalt(CharSequence str) {
		String base = str +"/"+slat;
		return DigestUtils.md5DigestAsHex(base.getBytes());
	}
}
