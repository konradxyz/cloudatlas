package pl.edu.mimuw.cloudatlas.common.utils;

import java.util.Calendar;

public class Utils {
	public static final long getNowMs() {
		return Calendar.getInstance().getTimeInMillis();
	}
}
