package org.mulinlab.varnote.cmdline.programgroups;

import org.broadinstitute.barclay.argparser.CommandLineProgramGroup;

/**
 * Tools that manipulate read data in SAM, BAM or CRAM format
 */
public class QueryProgramGroup implements CommandLineProgramGroup {

    @Override
    public String getName() { return "Query"; }

    @Override
    public String getDescription() { return "Tools that quickly retrieve data lines from the database(or annotation) file(s)."; }
}