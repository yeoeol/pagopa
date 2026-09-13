package com.commerce.pagopa.global.util;

import static org.springframework.util.StringUtils.hasText;

public class StringUtil {

	public static String normalize(String str) {
		if (!hasText(str)) {
			return null;
		}
		return str.trim();
	}
}
