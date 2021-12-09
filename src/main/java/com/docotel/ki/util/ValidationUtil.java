package com.docotel.ki.util;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public final class ValidationUtil {
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static Pattern emailPattern = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

	private ValidationUtil() {
	}

	public static boolean containsSpace(String str) {
		return str.contains(" ");
	}

	public static boolean isBlank(String value) {
		return value == null || value.trim().equalsIgnoreCase("");
	}

	public static boolean isEmpty(List list) {
		return list == null || list.size() == 0;
	}

	public static boolean isEmpty(MultipartFile multipartFile) {
		return multipartFile == null || multipartFile.isEmpty() || multipartFile.getSize() == 0L;
	}

	public static boolean isInArray(String value, String... values) {
		List<String> valueList = new ArrayList();
		Collections.addAll(valueList, values);
		return isInList(value, valueList);
	}

	public static boolean isInList(String value, List<String> valueList) {
		return valueList.contains(value);
	}

	public static boolean isInvalidFormatDate(String date) {
		return isInvalidFormatDate(date, "dd/MM/yyyy", "/");
	}

	public static boolean isInvalidFormatDate(String date, String dateFormat, String dateSeparator) {
		try {
			String[] dates = date.split(dateSeparator);
			String[] dateFormats = dateFormat.split(dateSeparator);
			if (dates.length != dateFormats.length) {
				return true;
			} else {
				for(int i = 0; i < dates.length; ++i) {
					if (!dateFormats[i].equals("MMMM") && dates[i].length() != dateFormats[i].length()) {
						return true;
					}
				}

				SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
				sdf.setLenient(false);
				sdf.parse(date);
				return false;
			}
		} catch (ParseException var6) {
			return true;
		}
	}

	public static boolean isInvalidFormatEmail(String email) {
		return !emailPattern.matcher(email).matches();
	}

	public static boolean isInvalidFormatTime(String time) {
		return isInvalidFormatDate(time, "HH:mm", ":");
	}

	public static boolean isInvalidFormatTime(String time, String timeFormat, String timeSeparator) {
		return isInvalidFormatDate(time, timeFormat, timeSeparator);
	}

	public static boolean isLengthGreaterThan(String value, int maxLength) {
		return value.length() > maxLength;
	}

	public static boolean isLengthLessThan(String value, int minLength) {
		return value.length() < minLength;
	}

	public static boolean isNotEquals(String str1, String str2) {
		return !str1.equals(str2);
	}

	public static boolean isNotEqualsIgnoreCase(String str1, String str2) {
		return !str1.equalsIgnoreCase(str2);
	}

	public static boolean isNotAlpha(String str) {
		return !Pattern.compile("^[a-zA-Z]+").matcher(str).matches();
	}

	public static boolean isNotAlphaNumeric(String str) {
		return !Pattern.compile("^[a-zA-Z0-9]+").matcher(str).matches();
	}

	public static boolean isNotAlphaNumericSpace(String str) {
		return !Pattern.compile("^[a-zA-Z0-9 ]+").matcher(str).matches();
	}

	public static boolean isNotAlphaSpace(String str) {
		return !Pattern.compile("^[a-zA-Z ]+").matcher(str).matches();
	}

	public static boolean isNotInArray(String value, String... values) {
		List<String> valueList = new ArrayList();
		Collections.addAll(valueList, values);
		return isNotInList(value, valueList);
	}

	public static boolean isNotInList(String value, List<String> valueList) {
		return !valueList.contains(value);
	}

	public static boolean isNotNumericOnly(String str) {
		return !Pattern.compile("^\\d+").matcher(str).matches();
	}

	public static boolean isNotNumberDecimal(String str) {
		return isNotNumberDecimal(str, ".");
	}

	public static boolean isNotNumberDecimal(String str, String decimalSeparator) {
		if (str.indexOf(decimalSeparator) != str.lastIndexOf(decimalSeparator)) {
			return false;
		} else if (str.equalsIgnoreCase("")) {
			return false;
		} else if (str.charAt(0) == '0') {
			return !Pattern.compile("0(\\.[0-9]+)?").matcher(str).matches();
		} else {
			return !Pattern.compile("[0-9]+(\\.[0-9]+)?").matcher(str).matches();
		}
	}

	public static boolean isNotNumberDecimalCommaSeparator(String str) {
		return isNotNumberDecimal(str, ",");
	}

	public static boolean isNotRegexMatch(String str, String regex) {
		return !Pattern.compile(regex).matcher(str).matches();
	}

	public static boolean isNull(Object o) {
		return o == null;
	}

	public static boolean isValueGreaterThan(Date value, Date maxValue) {
		return value.getTime() > maxValue.getTime();
	}

	public static boolean isValueGreaterThan(Number value, Number maxValue) {
		if (maxValue instanceof Byte) {
			return value.byteValue() > maxValue.byteValue();
		} else if (maxValue instanceof Short) {
			return value.shortValue() > maxValue.shortValue();
		} else if (maxValue instanceof Integer) {
			return value.intValue() > maxValue.intValue();
		} else if (maxValue instanceof Long) {
			return value.longValue() > maxValue.longValue();
		} else if (maxValue instanceof Float) {
			return value.floatValue() > maxValue.floatValue();
		} else if (maxValue instanceof Double) {
			return value.doubleValue() > maxValue.doubleValue();
		} else if (maxValue instanceof BigInteger) {
			return ((BigInteger)value).compareTo((BigInteger)maxValue) == 1;
		} else {
			return ((BigDecimal)value).compareTo((BigDecimal)maxValue) == 1;
		}
	}

	public static boolean isValueGreaterThan(String value, Number maxValue) {
		if (maxValue instanceof Byte) {
			return Byte.parseByte(value) > maxValue.byteValue();
		} else if (maxValue instanceof Short) {
			return Short.parseShort(value) > maxValue.shortValue();
		} else if (maxValue instanceof Integer) {
			return Integer.parseInt(value) > maxValue.intValue();
		} else if (maxValue instanceof Long) {
			return Long.parseLong(value) > maxValue.longValue();
		} else if (maxValue instanceof Float) {
			return Float.parseFloat(value) > maxValue.floatValue();
		} else if (maxValue instanceof Double) {
			return Double.parseDouble(value) > maxValue.doubleValue();
		} else if (maxValue instanceof BigInteger) {
			return (new BigInteger(value)).compareTo((BigInteger)maxValue) == 1;
		} else {
			return (new BigDecimal(value)).compareTo((BigDecimal)maxValue) == 1;
		}
	}

	public static boolean isValueLessThan(Date value, Date minValue) {
		return value.getTime() < minValue.getTime();
	}

	public static boolean isValueLessThan(Number value, Number minValue) {
		if (minValue instanceof Byte) {
			return value.byteValue() < minValue.byteValue();
		} else if (minValue instanceof Short) {
			return value.shortValue() < minValue.shortValue();
		} else if (minValue instanceof Integer) {
			return value.intValue() < minValue.intValue();
		} else if (minValue instanceof Long) {
			return value.longValue() < minValue.longValue();
		} else if (minValue instanceof Float) {
			return value.floatValue() < minValue.floatValue();
		} else if (minValue instanceof Double) {
			return value.doubleValue() < minValue.doubleValue();
		} else if (minValue instanceof BigInteger) {
			return ((BigInteger)value).compareTo((BigInteger)minValue) == -1;
		} else {
			return ((BigDecimal)value).compareTo((BigDecimal)minValue) == -1;
		}
	}

	public static boolean isValueLessThan(String value, Number minValue) {
		if (minValue instanceof Byte) {
			return Byte.parseByte(value) < minValue.byteValue();
		} else if (minValue instanceof Short) {
			return Short.parseShort(value) < minValue.shortValue();
		} else if (minValue instanceof Integer) {
			return Integer.parseInt(value) < minValue.intValue();
		} else if (minValue instanceof Long) {
			return Long.parseLong(value) < minValue.longValue();
		} else if (minValue instanceof Float) {
			return Float.parseFloat(value) < minValue.floatValue();
		} else if (minValue instanceof Double) {
			return Double.parseDouble(value) < minValue.doubleValue();
		} else if (minValue instanceof BigInteger) {
			return (new BigInteger(value)).compareTo((BigInteger)minValue) == -1;
		} else {
			return (new BigDecimal(value)).compareTo((BigDecimal)minValue) == -1;
		}
	}
}
