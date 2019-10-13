package org.mulinlab.varnote.cmdline.abstractclass;


import com.intel.gkl.compression.IntelInflaterFactory;
import htsjdk.samtools.util.BlockGunzipper;
import htsjdk.samtools.util.zip.InflaterFactory;
import org.broadinstitute.barclay.argparser.Argument;
import org.broadinstitute.barclay.argparser.ArgumentCollection;
import org.mulinlab.varnote.cmdline.collection.DBArgumentCollection;
import org.mulinlab.varnote.cmdline.constant.Arguments;

public abstract class QueryProgram extends CMDProgram {

    @ArgumentCollection
    protected final DBArgumentCollection dbArguments = new DBArgumentCollection();

    @Argument(fullName = Arguments.USE_JDKI_LONG, shortName = Arguments.USE_JDKI_SHORT,
            doc = "Use the JDK Inflater instead of the IntelInflater for reading index.", optional = true)
    protected Boolean USE_JDK_INFLATER = false;

    @Override
    protected void onStartup() {
        if (!USE_JDK_INFLATER) {
            BlockGunzipper.setDefaultInflaterFactory(new IntelInflaterFactory());
        } else {
            BlockGunzipper.setDefaultInflaterFactory(new InflaterFactory());
        }
    }
}
