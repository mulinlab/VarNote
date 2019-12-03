package org.mulinlab.varnote.cmdline.programgroups;

import org.broadinstitute.barclay.argparser.CommandLineProgramGroup;

/**
 * Tools that manipulate read data in SAM, BAM or CRAM format
 */
public class LDProgramGroup implements CommandLineProgramGroup {

    @Override
    public String getName() { return "LD Function"; }

    @Override
    public String getDescription() { return "LD Related Functions"; }
}