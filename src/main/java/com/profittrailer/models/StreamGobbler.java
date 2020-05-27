package com.profittrailer.models;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import java.io.IOException;
import java.io.InputStream;

@Log4j2
public class StreamGobbler extends Thread {

	InputStream is;
	String output;

	public StreamGobbler(InputStream is) {
		this.is = is;
		this.setDaemon(true);
	}

	public String getOutput() {
		return output;
	}

	public void run() {
		try {
			IOUtils.copy(is, new NullOutputStream());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
