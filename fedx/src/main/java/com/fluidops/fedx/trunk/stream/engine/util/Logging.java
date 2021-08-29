package com.fluidops.fedx.trunk.stream.engine.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class Logging {

	static File logFile = null;
	static FileWriter writer = null;
	static int out = 0;
	static int in = 0;

	synchronized static public void out() {
		out++;
	}

	synchronized static public void in(int i) {
		in += i;
	}

	public static int getO() {
		return out;
	}

	static public int getI() {
		return in;
	}

	static public void log(String message) {
		String fn = null;
		if (logFile == null) {
			fn = Calendar.getInstance().getTime().toString();
		}
		log(fn, message);
	}

	static public void log(String name, String message) {
		if (logFile == null)
			logFile = new File(name + ".txt");

		try {
			if (writer == null)
				writer = new FileWriter(logFile);
			writer.write(message + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
