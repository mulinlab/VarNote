package org.mulinlab.varnote.speed;

import org.junit.Test;
import org.mulinlab.varnote.operations.mapper.IntersetMapper;
import org.mulinlab.varnote.operations.mapper.TestReaderMapper;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.mapreduce.MapReduce;
import org.mulinlab.varnote.utils.mapreduce.Mapper;
import org.mulinlab.varnote.utils.mapreduce.Reducer;
import org.mulinlab.varnote.utils.mapreduce.SimpleMapReduce;
import org.mulinlab.varnote.utils.queryreader.ThreadLineReader;
import org.mulinlab.varnote.utils.queryreader.reader.GZipReader;
import org.mulinlab.varnote.utils.queryreader.reader.SpiderReader;
import org.mulinlab.varnote.utils.stream.BZIP2InputStream;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.mulinlab.varnote.utils.LoggingUtils.logger;

public class MultiThreadReaderSpeedTest {
    @Test
    public void testIndex() throws Exception {
        String file = "/Users/hdd/Downloads/test_data/database1.sorted.bed.gz";

        long t1 = System.currentTimeMillis();
        BZIP2InputStream bz2_text = new BZIP2InputStream(file, 1);
        bz2_text.adjustPos();
        bz2_text.creatSpider();

        ThreadLineReader reader = new ThreadLineReader(new SpiderReader(bz2_text.spider[0]), Format.newBED(), 0);
        String s;
        int i = 0;
        while((s = reader.readLine()) != null) {
            i++;
        }
        System.out.println("i=" + i);
        reader.close();

        long t2 = System.currentTimeMillis();
        logger.info(String.format("\n\nDone! Time: %d\n", (t2 - t1)));





        //4

        int thread = 5;
        bz2_text = new BZIP2InputStream(file, thread);
        bz2_text.adjustPos();
        bz2_text.creatSpider();

        Reducer<File, Mapper<Long>, Long> r = new Reducer<File, Mapper<Long>, Long>() {
            public File doReducer(List<Mapper<Long>> mappers) {
                return null;
            }
        };

        MapReduce<File, Long> mr = new SimpleMapReduce<File, Long>(thread, r);
        TestReaderMapper<Long> mapper = null;
        for(int k=0; k<thread; k++) {
            mapper = new TestReaderMapper<Long>(new ThreadLineReader(new SpiderReader(bz2_text.spider[k]), Format.newBED(), k));
            mr.addMapper(mapper);
        }
        mr.getResult();
    }

}
