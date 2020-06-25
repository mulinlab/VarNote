package org.mulinlab.varnote.cmdline.programgroups;

import org.broadinstitute.barclay.argparser.CommandLineProgramGroup;

/**
 * Tools that manipulate read data in SAM, BAM or CRAM format
 */
public class AnnoProgramGroup implements CommandLineProgramGroup {

    @Override
    public String getName() { return "VarNote Annotation"; }

    @Override
    public String getDescription() { return "Tools that identifies desired annotation fields from database(s)."; }
}