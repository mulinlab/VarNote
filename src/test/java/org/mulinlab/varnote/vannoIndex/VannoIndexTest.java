package org.mulinlab.varnote.vannoIndex;

import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.database.index.IndexFactory;
import org.mulinlab.varnote.utils.database.index.vannoIndex.VannoIndexV1;


public class VannoIndexTest {

    final Logger logger = LoggingUtils.logger;

    @Test
    public void test() throws Exception {

        VannoIndexV1 index = (VannoIndexV1) IndexFactory.readIndex("/Users/hdd/Desktop/vanno/vanno_help/AF.ANN.bgz.vanno.vi") ;
//        System.out.println(index.mSc);
//        System.out.println(index.mBc);
//        System.out.println(index.mEc);
//        System.out.println(index.commentIndicator);
//        Map<Integer, Long> map = index.getMinOffForChr();
//        for (Integer key : map.keySet()) {
//            System.out.println(key + ", " +  map.get(key));
//        }
    }
}
