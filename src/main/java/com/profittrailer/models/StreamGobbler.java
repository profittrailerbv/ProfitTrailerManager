package com.profittrailer.models;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StreamGobbler extends Thread {
	InputStream is;
	String output;

	public StreamGobbler(InputStream is) {
		this.is = is;
	}

	public String getOutput() {
		return output;
	}

	public void run() {
		try {
			StringWriter writer = new StringWriter();
			IOUtils.copy(is, writer, StandardCharsets.UTF_8);
			output = writer.toString();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
