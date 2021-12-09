package com.docotel.ki.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtil {
	public static final Locale LOCALE_ID = new Locale("in", "ID");

	private static SimpleDateFormat defaultDateFormat = new SimpleDateFormat("dd-MM-yyyy", LOCALE_ID);
	private static SimpleDateFormat defaultDateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", LOCALE_ID);

	static {
		defaultDateFormat.setLenient(false);
		defaultDateTimeFormat.setLenient(false);
	}

	public static String formatDate(Date date) {
		return formatDate(date, defaultDateFormat);
	}

	public static String formatDateTime(Date date) {
		return formatDate(date, defaultDateTimeFormat);
	}

	public static String formatDate(Date date, String dateFormat) {
		return formatDate(date, new SimpleDateFormat(dateFormat, LOCALE_ID));
	}

	public static String formatDate(Date date, SimpleDateFormat sdf) {
		if (date == null) {
			return null;
		} else {
			synchronized(sdf) {
				return sdf.format(date);
			}
		}
	}

	public static Timestamp toDate(String strDate) throws ParseException {
		try {
			SimpleDateFormat var1 = defaultDateFormat;
			synchronized(defaultDateFormat) {
				return new Timestamp(defaultDateFormat.parse(strDate).getTime());
			}
		} catch (ParseException var4) {
			throw new ParseException(var4.getMessage(), var4.getErrorOffset());
		}
	}

	public static Timestamp toDate(String aMask, String strDate) throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat(aMask, LOCALE_ID);
		df.setLenient(false);

		try {
			return new Timestamp(df.parse(strDate).getTime());
		} catch (ParseException var4) {
			throw new ParseException(var4.getMessage(), var4.getErrorOffset());
		}
	}

	public static Date forceToDate(String aMask, String strDate) throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat(aMask, LOCALE_ID);
		df.setLenient(false);

		try {
			return df.parse(strDate);
		} catch (ParseException var4) {
			throw new ParseException(var4.getMessage(), var4.getErrorOffset());
		}
	}

	public static Timestamp toDateTime(String strDate) throws ParseException {
		try {
			SimpleDateFormat var1 = defaultDateTimeFormat;
			synchronized(defaultDateTimeFormat) {
				return new Timestamp(defaultDateTimeFormat.parse(strDate).getTime());
			}
		} catch (ParseException var4) {
			throw new ParseException(var4.getMessage(), var4.getErrorOffset());
		}
	}

	public static int currentYear() {
		Calendar calendar = Calendar.getInstance(LOCALE_ID);
		return calendar.get(Calendar.YEAR);
	}

	public static Timestamp currentDate() {
		Calendar calendar = Calendar.getInstance(LOCALE_ID);
		return new Timestamp(calendar.getTimeInMillis());
	}
}
