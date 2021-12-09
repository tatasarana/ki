package com.docotel.ki.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

public final class NumberUtil {
	public static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("###,###.##;(###,###.##)");
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,###.##;-###,###.##");
	public static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("###,###;-###,###");
	private static String decimalSeparator = ".";
	private static String thousandSeparator = ",";
	private static String tempThousandSeparator = "#";

	private NumberUtil() {
	}

	public static void setDecimalSeparator(String decimalSeparator) {
		decimalSeparator = decimalSeparator;
		if (decimalSeparator.equalsIgnoreCase(tempThousandSeparator)) {
			tempThousandSeparator = ".";
			if (tempThousandSeparator.equalsIgnoreCase(thousandSeparator)) {
				tempThousandSeparator = ",";
			}
		}

	}

	public static void setThousandSeparator(String thousandSeparator) {
		thousandSeparator = thousandSeparator;
		if (thousandSeparator.equalsIgnoreCase(tempThousandSeparator)) {
			tempThousandSeparator = "|";
			if (tempThousandSeparator.equalsIgnoreCase(decimalSeparator)) {
				tempThousandSeparator = ".";
			}
		}

	}

	public static String formatDecimal(Number number) {
		return formatNumber(number, DECIMAL_FORMAT);
	}

	public static String formatDecimalNoSeparator(Number number) {
		try {
			return formatDecimal(number).replace(thousandSeparator, "");
		} catch (NullPointerException var2) {
			return null;
		}
	}

	public static String formatInteger(Number number) {
		return formatNumber(number, INTEGER_FORMAT);
	}

	public static String formatIntegerNoSeparator(Number number) {
		try {
			return formatInteger(number).replace(thousandSeparator, "");
		} catch (NullPointerException var2) {
			return null;
		}
	}

	public static String formatNumber(Number number, String numberFormat) {
		return formatNumber(number, new DecimalFormat(numberFormat));
	}

	public static String formatNumber(Number number, DecimalFormat numberFormat) {
		if (number == null) {
			return null;
		} else {
			synchronized(numberFormat) {
				return maskNumber(numberFormat.format(number));
			}
		}
	}

	public static BigDecimal parseDecimal(String str) throws NumberFormatException {
		try {
			str = str.trim();
		} catch (NullPointerException var2) {
			return null;
		}

		return new BigDecimal(str);
	}

	public static BigDecimal safeParseDecimal(String str, BigDecimal defaultValue) {
		try {
			str = str.trim();
		} catch (NullPointerException var5) {
			return defaultValue;
		}

		BigDecimal parsed = defaultValue;

		try {
			parsed = new BigDecimal(unmaskNumber(str));
		} catch (NumberFormatException var4) {
			;
		}

		return parsed;
	}

	public static BigInteger parseInteger(String str) throws NumberFormatException {
		try {
			str = str.trim();
		} catch (NullPointerException var2) {
			return null;
		}

		return new BigInteger(unmaskNumber(str));
	}

	public static BigInteger safeParseInteger(String str, BigInteger defaultValue) {
		try {
			str = str.trim();
		} catch (NullPointerException var5) {
			return defaultValue;
		}

		BigInteger parsed = defaultValue;

		try {
			parsed = new BigInteger(unmaskNumber(str));
		} catch (NumberFormatException var4) {
			;
		}

		return parsed;
	}

	public static String maskNumber(String number) {
		return number.replace(",", tempThousandSeparator).replace(".", decimalSeparator).replace(tempThousandSeparator, thousandSeparator);
	}

	public static String unmaskNumber(String maskedNumber) {
		return maskedNumber.replace(thousandSeparator, "").replace(decimalSeparator, ".");
	}

	static {
		CURRENCY_FORMAT.setDecimalSeparatorAlwaysShown(false);
		DECIMAL_FORMAT.setDecimalSeparatorAlwaysShown(false);
	}

	public static Integer tryParseInt(String text) {
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException nfe) {
			return null;
		}
	}
}