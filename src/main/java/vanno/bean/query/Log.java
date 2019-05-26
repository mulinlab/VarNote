package main.java.vanno.bean.query;

import java.io.PrintStream;

import main.java.vanno.constants.BasicUtils;

public class Log {
	private boolean verbose;
	private boolean isLog;
	private PrintStream logOut;
	
	public Log(boolean verbose, boolean isLog) {
		super();
		this.verbose = verbose;
		this.isLog = isLog;
	}
	
	public void printStrSystemOri(final String str) {
		if(!verbose) return;
		System.out.println(str);
	}
	
	public void printStrNon(final String str) {
		printStr(str, "", false);
	}
	
	public void printStrNonSystem(final String str) {
		printStr(str, "", true);
	}
	
	public void printStrWhite(final String str) {
		printStr(str, BasicUtils.KWHT, false);
	}
	
	public void printStrWhiteSystem(final String str) {
		printStr(str, BasicUtils.KWHT, true);
	}
	
	public void printKVKCYN(final String key, final String val) {
		printKV(key, val, BasicUtils.KCYN, false);
	}
	
	public void printKVKCYNSystem(final String key, final String val) {
		printKV(key, val, BasicUtils.KCYN, true);
	}
	
	public void printStr(final String str, final String color, final boolean system) {
		if(!verbose) return;
		if(!isLog || system) {
			System.out.println(color + str + BasicUtils.KNRM);
		} else {
			logOut.println(str);
		}
	}

	public void printKV(final String key, final String val, final String color, final boolean system) {
		if(!verbose) return;
		if(!isLog || system) {
			System.out.println(key + ": " + color + val + BasicUtils.KNRM);
		} else {
			logOut.println(key + ": " + val);
		}
	}

	public boolean isVerbose() {
		return verbose;
	}

	public boolean isLog() {
		return isLog;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void setLogOut(PrintStream logOut) {
		this.logOut = logOut;
	}
}
