package org.mulinlab.varnote.operations.readers.db;

import org.junit.Test;
import java.io.IOException;

public class VannoMixReaderTest {
    @Test
    public void query() {
        try {
            VannoReader reader = new VannoMixReader("src/test/resources/database2.sorted.tab.gz", true);
            reader.query("1:69169-69170");
            reader.close();
            System.out.println(reader.getResultSize());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}