package org.mulinlab.varnote.cmdline.programgroups;

import org.broadinstitute.barclay.argparser.CommandLineProgramGroup;

/**
 * Tools that manipulate read data in SAM, BAM or CRAM format
 */
public class AdvanceProgramGroup implements CommandLineProgramGroup {

    @Override
    public String getName() { return "Advance Function"; }

    @Override
    public String getDescription() { return "Advance Function Tools"; }
}