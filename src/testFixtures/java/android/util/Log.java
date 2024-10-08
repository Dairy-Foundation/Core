package android.util;

import java.util.Locale;

/**
 * mocks the android log class, for use by RobotLog
 */
public class Log {
	private static final long start = System.nanoTime();
	public static int d(String tag, String msg) {
		System.out.println("DEBUG: " + tag + ": " + msg);
		return 0;
	}
	
	public static int i(String tag, String msg) {
		System.out.println("INFO: " + tag + ": " + msg);
		return 0;
	}
	
	public static int w(String tag, String msg) {
		System.out.println("WARN: " + tag + ": " + msg);
		return 0;
	}
	
	public static int e(String tag, String msg) {
		System.out.println("ERROR: " + tag + ": " + msg);
		return 0;
	}
	
	// add other methods if required...
	public static int println(int priority, String tag, String msg) {
		String str = "|" + String.format(Locale.getDefault(), "%.3f", (System.nanoTime() - start) / 1.0e9) + "| " + tag + ": " + msg;
		System.out.println(str);
		return str.getBytes().length;
	}
}