package org.mulinlab.varnote.cmdline.programgroups;

import org.broadinstitute.barclay.argparser.CommandLineProgramGroup;

/**
 * Tools that manipulate read data in SAM, BAM or CRAM format
 */
public class IndexProgramGroup implements CommandLineProgramGroup {

    @Override
    public String getName() { return "VarNote Index"; }

    @Override
    public String getDescription() { return "Tools that generates index for the compressed database file."; }
}