package common;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@SuppressLint("SimpleDateFormat")
public class Common {
	public static final String PREFS_NAME = "CMOVClient";
	public static String FILENAME = "hello_file";
	public static final String SERVER_URL = "http://192.168.1.78:81/";

	public static class DateUtils {
		public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

		public static String now() {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
			return sdf.format(cal.getTime());
		}
	}
}
