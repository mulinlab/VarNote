package org.mulinlab.varnote.cmdline.abstractclass;


import com.intel.gkl.compression.IntelInflaterFactory;
import htsjdk.samtools.util.BlockGunzipper;
import htsjdk.samtools.util.zip.InflaterFactory;
import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.ArgumentCollection;
import org.mulinlab.varnote.cmdline.collection.DBArgumentCollection;
import org.mulinlab.varnote.cmdline.constant.Arguments;

public abstract class QueryProgram extends RunProgram {

    @ArgumentCollection
    protected final DBArgumentCollection dbArguments = new DBArgumentCollection();

}
