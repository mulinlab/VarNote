package org.mulinlab.varnote.cmdline.abstractclass;

import com.intel.gkl.compression.IntelInflaterFactory;
import htsjdk.samtools.util.BlockGunzipper;
import htsjdk.samtools.util.Log;
import htsjdk.samtools.util.zip.InflaterFactory;
import org.broadinstitute.barclay.argparser.Argument;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.LoggingUtils;

public abstract class RunProgram extends CMDProgram {

    @Argument(fullName = Arguments.LOG_LONG, doc = "Whether to print log.", optional = true)
    protected Boolean islog = GlobalParameter.DEFAULT_LOG;

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

        if(!islog) LoggingUtils.setLoggingLevel(Log.LogLevel.ERROR);
        else LoggingUtils.setLoggingLevel(Log.LogLevel.INFO);
    }

    protected void onShutdown() {

    }
}
