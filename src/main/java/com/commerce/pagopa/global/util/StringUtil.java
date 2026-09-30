package com.commerce.pagopa.global.util;

import java.util.Locale;

import static org.springframework.util.StringUtils.hasText;

public class StringUtil {

	public static String normalize(String str) {
		if (!hasText(str)) {
			return null;
		}
		return str.trim().toLowerCase(Locale.ROOT);
	}
}
