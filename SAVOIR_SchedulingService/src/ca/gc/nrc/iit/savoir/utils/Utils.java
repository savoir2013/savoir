// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class Utils {

	// private static final Log log = LogFactory.getLog(GeneratorUtils.class);

	private static Map<Integer, Integer> nbInstConst = new HashMap<Integer, Integer>();

	private static int lastResourceIndex = 0;

	public static int getLastResourceIndex() {
		return lastResourceIndex;
	}

	private static Integer randomIntInArray(Integer[] integers) {
		return integers[randomIntBetween(0, integers.length)];
	}

	public static int randomNonZeroIntNoGreater(int n) {
		// Returns an int between 1 and n
		return (int) ((Math.random()) * (float) n) + 1;
	}

	public static int randomIntNoGreater(int n) {
		// Returns an int between 0 and n
		return (int) ((Math.random()) * (float) n);
	}

	static public int randomIntBetween(int a, int b) {
		// Returns an int between a and b
		return (int) ((Math.random()) * (float) (b - a)) + a;
	}

	static public int randomIntBetweenDifferent(int a, int b, int c) {
		// Returns an int between a and b and different from c
		int ret = c;
		while (ret == c) {
			ret = (int) ((Math.random()) * (float) (b - a)) + a;
		}
		return ret;
	}

	static public long randomLongBetween(long a, long b) {
		// Returns an int between a and b
		return (long) ((Math.random()) * (float) (b - a)) + a;
	}

	static public Calendar getLastMidnightTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}

	public static void saveToFile(String str, String fileName) {
		Writer output = null;
		try {
			output = new BufferedWriter(new FileWriter(new File(fileName)));
			output.write(str);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (output != null)
					output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void appendToFile(String str, String fileName) {
		Writer output = null;
		try {
			output = new BufferedWriter(
					new FileWriter(new File(fileName), true));
			output.write(str);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (output != null)
					output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String dateToString(Long date) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss aa");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		Calendar cl = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cl.setTimeInMillis(date);
		return formatter.format(cl.getTime());
	}

	private static boolean isActive(Long sTime, Long eTime, Long minTime,
			Long maxTime) {
		if ((minTime <= sTime && eTime <= maxTime)
				|| (minTime <= sTime && maxTime <= eTime && sTime < maxTime)
				|| (sTime <= minTime && eTime <= maxTime && eTime > minTime)
				|| (sTime <= minTime && maxTime <= eTime)) {
			return true;
		}
		return false;
	}

	public static String getConstraintsOnResources() {
		StringBuilder sb = new StringBuilder("Constraints on Resources:\n");
		for (int i : nbInstConst.keySet()) {
			sb.append(i + "\t=>\t" + nbInstConst.get(i) + "\n");
		}
		return sb.toString();
	}

}
