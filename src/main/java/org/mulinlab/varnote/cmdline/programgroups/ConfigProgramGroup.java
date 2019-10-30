package org.mulinlab.varnote.cmdline.programgroups;

import org.broadinstitute.barclay.argparser.CommandLineProgramGroup;

/**
 * Tools that manipulate read data in SAM, BAM or CRAM format
 */
public class ConfigProgramGroup implements CommandLineProgramGroup {

    @Override
    public String getName() { return "Run Config"; }

    @Override
    public String getDescription() { return "Run program from a config file."; }
}