package org.mulinlab.varnote.cmdline.abstractclass;


import com.intel.gkl.compression.IntelInflaterFactory;
import htsjdk.samtools.util.BlockGunzipper;
import htsjdk.samtools.util.Log;
import htsjdk.samtools.util.zip.InflaterFactory;
import org.broadinstitute.barclay.argparser.Argument;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.utils.LoggingUtils;

import java.io.File;

public abstract class ConfigProgram extends CMDProgram {

    @Argument( shortName = Arguments.INPUT_SHORT, fullName = Arguments.INPUT_LONG, optional = false,
            doc = "Config file path."
    )
    protected File configFile = null;

    @Override
    protected void onStartup() {
    }

    protected void onShutdown() {

    }
}
