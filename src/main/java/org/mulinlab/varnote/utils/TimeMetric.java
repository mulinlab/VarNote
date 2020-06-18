package org.mulinlab.varnote.utils;

import org.apache.logging.log4j.Logger;
import java.text.DecimalFormat;
import java.text.NumberFormat;


public class TimeMetric {
    public static final NumberFormat timeFmt = new DecimalFormat("00");
    public final Logger logger = LoggingUtils.logger;

    protected String name;
    protected long start;
    protected long count;
    protected int maxCount = 10000;
    protected long lvCount;

    public TimeMetric(String name) {
        this.name = name;
        this.start = System.currentTimeMillis();
        this.count = 0;
        this.lvCount = 0;
    }

    public void addRecord() {
        count++;
        printCount(false);
    }

    public void addRecord(long count) {
        this.count = count;
        printCount(false);
    }

    public void addLVCount() {
        lvCount++;
    }

    public void doEnd() {
        printCount(true);
    }

    public void doEnd(long count) {
        this.count = count;
        printCount(true);
    }

    private void printCount(final boolean isEnd) {
        if(isEnd || count % maxCount == 0) {
            long seconds = (System.currentTimeMillis() - this.start) / 1000L;
            String elapsed = formatElapseTime(seconds);
            info(name + " processed " + count + " variants, elapsed time: " + elapsed + "s.");
        }
    }

    public void printLVCount() {
        if (lvCount > 0) {
            info(name + " skipped " + lvCount + " large variants.");
        }
    }

    protected void info(final String info) {
        logger.info(info);
    }

    public static String formatElapseTime(long seconds) {
        long s = seconds % 60L;
        long allMinutes = seconds / 60L;
        long m = allMinutes % 60L;
        long h = allMinutes / 60L;
        return timeFmt.format(h) + ":" + timeFmt.format(m) + ":" + timeFmt.format(s);
    }

    public long getCount() {
        return count;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public long getLvCount() {
        return lvCount;
    }
}
