package org.mulinlab.varnote.cmdline.abstractclass;


import org.broadinstitute.barclay.argparser.ArgumentCollection;
import org.mulinlab.varnote.cmdline.collection.InputFileArgumentCollection;
import org.mulinlab.varnote.cmdline.collection.RunArgumentCollection;
import org.mulinlab.varnote.utils.format.Format;

public abstract class QueryFileProgram extends QueryProgram {

    @ArgumentCollection()
    protected final InputFileArgumentCollection inputArguments = new InputFileArgumentCollection();

    @ArgumentCollection
    protected final RunArgumentCollection runArguments = new RunArgumentCollection();
    protected String getQueryFilePath() {
        return inputArguments.getQueryFilePath();
    };

    protected Format getFormat() {
        return inputArguments.getFormat(getQueryFilePath(), true);
    }
}
