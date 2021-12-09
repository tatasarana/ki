package com.docotel.ki.util;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

public final class FieldValidationUtil {
	public static final String FIELD_ERROR_CONTAINS_SPACE = "field.error.contains.space";
	public static final String FIELD_ERROR_FORBIDDEN = "field.error.forbidden";
	public static final String FIELD_ERROR_INVALID_FILE_SIZE = "field.error.invalid.file.size";
	public static final String FIELD_ERROR_INVALID_FORMAT_DATE = "field.error.invalid.format.date";
	public static final String FIELD_ERROR_INVALID_FORMAT_DATETIME = "field.error.invalid.format.datetime";
	public static final String FIELD_ERROR_INVALID_FORMAT_DECIMAL = "field.error.invalid.format.decimal";
	public static final String FIELD_ERROR_INVALID_FORMAT_EMAIL = "field.error.invalid.format.email";
	public static final String FIELD_ERROR_INVALID_FORMAT_FILE = "field.error.invalid.format.file";
	public static final String FIELD_ERROR_INVALID_FORMAT_TIME = "field.error.invalid.format.time";
	public static final String FIELD_ERROR_INVALID_VALUE = "field.error.invalid.value";
	public static final String FIELD_ERROR_MAXLENGTH = "field.error.maxlength";
	public static final String FIELD_ERROR_MINLENGTH = "field.error.minlength";
	public static final String FIELD_ERROR_NOT_ALPHA = "field.error.not.alpha";
	public static final String FIELD_ERROR_NOT_ALPHA_SPACE = "field.error.not.alpha.space";
	public static final String FIELD_ERROR_NOT_ALPHANUMERIC = "field.error.not.alphanumeric";
	public static final String FIELD_ERROR_NOT_ALPHANUMERIC_SPACE = "field.error.not.alphanumeric.space";
	public static final String FIELD_ERROR_NOT_FOUND = "field.error.not.found";
	public static final String FIELD_ERROR_NOT_IN_SELECTION_LIST = "field.error.not.in.selection.list";
	public static final String FIELD_ERROR_NOT_MATCH_FIELD = "field.error.not.match.field";
	public static final String FIELD_ERROR_NOT_MATCH_IGNORECASE_FIELD = "field.error.not.match.ignorecase.field";
	public static final String FIELD_ERROR_NOT_NUMERIC = "field.error.not.numeric";
	public static final String FIELD_ERROR_NOT_UNIQUE = "field.error.not.unique";
	public static final String FIELD_ERROR_REQUIRED = "field.error.required";
	public static final String FIELD_ERROR_REQUIRED_LIST_VALUE = "field.error.required.list.value";
	public static final String FIELD_ERROR_VALUE_MAXIMUM = "field.error.value.maximum";
	public static final String FIELD_ERROR_VALUE_MINIMUM = "field.error.value.minimum";

	private FieldValidationUtil() {
	}

	public static final boolean alpha(BindingResult errors, String name, String value, String label) {
		if (ValidationUtil.isNotAlpha(value)) {
			errors.rejectValue(name, "field.error.not.alpha", new Object[]{label}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean alphaNumeric(BindingResult errors, String name, String value, String label) {
		if (ValidationUtil.isNotAlphaNumeric(value)) {
			errors.rejectValue(name, "field.error.not.alphanumeric", new Object[]{label}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean alphaNumericSpace(BindingResult errors, String name, String value, String label) {
		if (ValidationUtil.isNotAlphaNumericSpace(value)) {
			errors.rejectValue(name, "field.error.not.alphanumeric.space", new Object[]{label}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean alphaSpace(BindingResult errors, String name, String value, String label) {
		if (ValidationUtil.isNotAlphaSpace(value)) {
			errors.rejectValue(name, "field.error.not.alpha.space", new Object[]{label}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean captcha(BindingResult errors, String name, String value, String label, String captcha, boolean ignoreCase) {
		if (ignoreCase) {
			if (ValidationUtil.isNotEqualsIgnoreCase(value, captcha)) {
				errors.rejectValue(name, "field.error.invalid.value", new Object[]{label}, "");
				return false;
			}
		} else if (ValidationUtil.isNotEquals(value, captcha)) {
			errors.rejectValue(name, "field.error.invalid.value", new Object[]{label}, "");
			return false;
		}

		return true;
	}

	public static final boolean date(BindingResult errors, String name, String value, String label) {
		try {
			if (ValidationUtil.isInvalidFormatDate(value)) {
				errors.rejectValue(name, "field.error.invalid.format.date", new Object[]{label}, "");
				return false;
			} else {
				return true;
			}
		} catch (Exception var5) {
			errors.rejectValue(name, "field.error.invalid.format.date", new Object[]{label}, "");
			return false;
		}
	}

	public static final boolean date(BindingResult errors, String name, String value, String label, String dateFormat, String dateSeparator) {
		try {
			if (ValidationUtil.isInvalidFormatDate(value, dateFormat, dateSeparator)) {
				errors.rejectValue(name, "field.error.invalid.format.date", new Object[]{label}, "");
				return false;
			} else {
				return true;
			}
		} catch (Exception var7) {
			errors.rejectValue(name, "field.error.invalid.format.date", new Object[]{label}, "");
			return false;
		}
	}

	public static final boolean datetime(BindingResult errors, String name, String value, String label, String dateTimeFormat, String dateTimeSeparator, String dateSeparator, String timeSeparator) {
		try {
			String[] dates = value.split(dateTimeSeparator);
			if (dates.length != 2) {
				errors.rejectValue(name, "field.error.invalid.format.datetime", new Object[]{label}, "");
				return false;
			} else {
				String[] formats = dateTimeFormat.split(dateTimeSeparator);
				if (!ValidationUtil.isInvalidFormatDate(value, formats[0], dateSeparator) && !ValidationUtil.isInvalidFormatTime(value, formats[1], timeSeparator)) {
					return true;
				} else {
					errors.rejectValue(name, "field.error.invalid.format.datetime", new Object[]{label}, "");
					return false;
				}
			}
		} catch (Exception var10) {
			errors.rejectValue(name, "field.error.invalid.format.datetime", new Object[]{label}, "");
			return false;
		}
	}

	public static final boolean decimal(BindingResult errors, String name, String value, String label) {
		if (ValidationUtil.isNotNumberDecimal(value)) {
			errors.rejectValue(name, "field.error.invalid.format.decimal", new Object[]{label}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean decimalCommaSeparator(BindingResult errors, String name, String value, String label) {
		if (ValidationUtil.isNotNumberDecimalCommaSeparator(value)) {
			errors.rejectValue(name, "field.error.invalid.format.decimal", new Object[]{label}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean email(BindingResult errors, String name, String value, String label) {
		if (ValidationUtil.isInvalidFormatEmail(value)) {
			errors.rejectValue(name, "field.error.invalid.format.email", new Object[]{label}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean fileFormat(BindingResult errors, String name, String originalFilename, String label, String... fileFormatArr) {
		String fileExt = originalFilename.substring(originalFilename.lastIndexOf("."));
		String[] acceptableFileFormatArr = new String[fileFormatArr.length];

		for(int i = 0; i < fileFormatArr.length; ++i) {
			String format = fileFormatArr[i];
			if (format.startsWith(".")) {
				acceptableFileFormatArr[i] = format;
			} else {
				acceptableFileFormatArr[i] = "." + format;
			}
		}

		if (ValidationUtil.isNotInArray(fileExt, acceptableFileFormatArr)) {
			errors.rejectValue(name, "field.error.invalid.format.file", new Object[]{label, StringUtils.arrayToCommaDelimitedString(acceptableFileFormatArr)}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean fileSize(BindingResult errors, String name, long fileSize, String label, long maxFileSize) {
		if (fileSize > maxFileSize) {
			String[] fileSizeUnitArr = new String[]{"KB", "MB", "GB", "TB"};
			double humanFileSize = (double)maxFileSize / 1.0D;
			String readableHumanFileSize = null;
			String[] arr$ = fileSizeUnitArr;
			int len$ = fileSizeUnitArr.length;

			for(int i$ = 0; i$ < len$; ++i$) {
				String fileSizeUnit = arr$[i$];
				humanFileSize /= 1024.0D;
				if (humanFileSize < 1024.0D) {
					readableHumanFileSize = humanFileSize + fileSizeUnit;
					break;
				}
			}

			errors.rejectValue(name, "field.error.invalid.file.size", new Object[]{label, readableHumanFileSize}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean listValue(BindingResult errors, String name, String value, String label, String... values) {
		if (ValidationUtil.isNotInArray(value, values)) {
			errors.rejectValue(name, "field.error.not.in.selection.list", new Object[]{label}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean listValue(BindingResult errors, String name, String value, String label, List<String> valueList) {
		if (ValidationUtil.isNotInList(value, valueList)) {
			errors.rejectValue(name, "field.error.not.in.selection.list", new Object[]{label}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean match(BindingResult errors, String name, String value, String label, String matchValue, String matchLabel) {
		if (ValidationUtil.isNotEquals(value, matchValue)) {
			errors.rejectValue(name, "field.error.not.match.field", new Object[]{label, matchLabel}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean matchIgnoreCase(BindingResult errors, String name, String value, String label, String matchValue, String matchLabel) {
		if (ValidationUtil.isNotEqualsIgnoreCase(value, matchValue)) {
			errors.rejectValue(name, "field.error.not.match.ignorecase.field", new Object[]{label, matchLabel}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean matchRegex(BindingResult errors, String name, String value, String label, String regex) {
		if (ValidationUtil.isNotRegexMatch(value, regex)) {
			errors.rejectValue(name, "field.error.invalid.value", new Object[]{label}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean maxLength(BindingResult errors, String name, String value, String label, int maxLength) {
		if (ValidationUtil.isLengthGreaterThan(value, maxLength)) {
			errors.rejectValue(name, "field.error.maxlength", new Object[]{label, maxLength}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean maxValue(BindingResult errors, String name, Date value, String label, Date maxValue) {
		if (ValidationUtil.isValueGreaterThan(value, maxValue)) {
			errors.rejectValue(name, "field.error.value.maximum", new Object[]{label, DateUtil.formatDate(maxValue)}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean maxValue(BindingResult errors, String name, Date value, String label, Date maxValue, String formattedMaxValue) {
		if (ValidationUtil.isValueGreaterThan(value, maxValue)) {
			errors.rejectValue(name, "field.error.value.maximum", new Object[]{label, formattedMaxValue}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean maxValue(BindingResult errors, String name, Number value, String label, Number maxValue) {
		if (ValidationUtil.isValueGreaterThan(value, maxValue)) {
			errors.rejectValue(name, "field.error.value.maximum", new Object[]{label, maxValue}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean maxValue(BindingResult errors, String name, String value, String label, Number maxValue) {
		if (ValidationUtil.isValueGreaterThan(value, maxValue)) {
			errors.rejectValue(name, "field.error.value.maximum", new Object[]{label, maxValue}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean minLength(BindingResult errors, String name, String value, String label, int minLength) {
		if (ValidationUtil.isLengthLessThan(value, minLength)) {
			errors.rejectValue(name, "field.error.minlength", new Object[]{label, minLength}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean minValue(BindingResult errors, String name, Date value, String label, Date minValue) {
		if (ValidationUtil.isValueLessThan(value, minValue)) {
			errors.rejectValue(name, "field.error.value.minimum", new Object[]{label, DateUtil.formatDate(minValue)}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean minValue(BindingResult errors, String name, Date value, String label, Date minValue, String formattedMinValue) {
		if (ValidationUtil.isValueLessThan(value, minValue)) {
			errors.rejectValue(name, "field.error.value.minimum", new Object[]{label, formattedMinValue}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean minValue(BindingResult errors, String name, Number value, String label, Number minValue) {
		if (ValidationUtil.isValueLessThan(value, minValue)) {
			errors.rejectValue(name, "field.error.value.minimum", new Object[]{label, minValue}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean minValue(BindingResult errors, String name, String value, String label, Number minValue) {
		if (ValidationUtil.isValueLessThan(value, minValue)) {
			errors.rejectValue(name, "field.error.value.minimum", new Object[]{label, minValue}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean noSpace(BindingResult errors, String name, String value, String label) {
		if (ValidationUtil.containsSpace(value)) {
			errors.rejectValue(name, "field.error.contains.space", new Object[]{label}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean numericOnly(BindingResult errors, String name, String value, String label) {
		if (ValidationUtil.isNotNumericOnly(value)) {
			errors.rejectValue(name, "field.error.not.numeric", new Object[]{label}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean required(BindingResult errors, String name, String value, String label) {
		if (ValidationUtil.isBlank(value)) {
			errors.rejectValue(name, "field.error.required", new Object[]{label}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean required(BindingResult errors, String name, List<String> values, String label) {
		if (ValidationUtil.isEmpty(values)) {
			errors.rejectValue(name, "field.error.required", new Object[]{label}, "");
			return false;
		} else {
			for(int i = 0; i < values.size(); ++i) {
				String value = (String)values.get(i);
				if (ValidationUtil.isBlank(value)) {
					errors.rejectValue(name, "field.error.required.list.value", new Object[]{label, i + 1}, "");
					return false;
				}
			}

			return true;
		}
	}

	public static final boolean required(BindingResult errors, String name, MultipartFile value, String label) {
		if (ValidationUtil.isEmpty(value)) {
			errors.rejectValue(name, "field.error.required", new Object[]{label}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean required(BindingResult errors, String name, Object value, String label) {
		if (ValidationUtil.isNull(value)) {
			errors.rejectValue(name, "field.error.required", new Object[]{label}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean time(BindingResult errors, String name, String value, String label) {
		try {
			if (ValidationUtil.isInvalidFormatTime(value)) {
				errors.rejectValue(name, "field.error.invalid.format.time", new Object[]{label}, "");
				return false;
			} else {
				return true;
			}
		} catch (Exception var5) {
			errors.rejectValue(name, "field.error.invalid.format.time", new Object[]{label}, "");
			return false;
		}
	}

	public static final boolean time(BindingResult errors, String name, String value, String label, String timeFormat, String timeSeparator) {
		try {
			if (ValidationUtil.isInvalidFormatTime(value, timeFormat, timeSeparator)) {
				errors.rejectValue(name, "field.error.invalid.format.time", new Object[]{label}, "");
				return false;
			} else {
				return true;
			}
		} catch (Exception var7) {
			errors.rejectValue(name, "field.error.invalid.format.time", new Object[]{label}, "");
			return false;
		}
	}

	public static final boolean uniqueValue(BindingResult errors, String name, String value, String label, String... values) {
		if (ValidationUtil.isInArray(value, values)) {
			errors.rejectValue(name, "field.error.not.unique", new Object[]{label}, "");
			return false;
		} else {
			return true;
		}
	}

	public static final boolean uniqueValue(BindingResult errors, String name, String value, String label, List<String> valueList) {
		if (ValidationUtil.isInList(value, valueList)) {
			errors.rejectValue(name, "field.error.not.unique", new Object[]{label}, "");
			return false;
		} else {
			return true;
		}
	}

	public static boolean isNotNull(Object obj) {
		if (obj == null) return false;
		if (obj instanceof String) {
			return !obj.equals("null");
		}
		return true;
	}
}
