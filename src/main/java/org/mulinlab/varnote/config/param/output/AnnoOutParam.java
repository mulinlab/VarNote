package org.mulinlab.varnote.config.param.output;

import htsjdk.samtools.util.IOUtil;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.AnnoOutFormat;
import org.mulinlab.varnote.utils.format.Format;

import java.text.Normalizer;

public final class AnnoOutParam extends OutParam {

    private AnnoOutFormat annoOutFormat ;


    public AnnoOutParam() { super(); }
    public AnnoOutParam(final String outputPath) {
        super(outputPath);
    }

    @Override
    public void checkParam() {

    }

    public void init() {

    }


    public AnnoOutFormat getAnnoOutFormat() {
        return annoOutFormat;
    }

    public void setAnnoOutFormat(AnnoOutFormat annoOutFormat) {
        this.annoOutFormat = annoOutFormat;
    }
    public void setAnnoOutFormat(final String _annoOutFormat) {
        setAnnoOutFormat(VannoUtils.checkFileFormat(_annoOutFormat));
    }



    public void setDefalutOutFormat(final Format queryFormat) {
        if(annoOutFormat == null) {
            if (queryFormat.getFlags() == Format.VCF_FLAGS)
                this.annoOutFormat = AnnoOutFormat.VCF;
            else this.annoOutFormat = AnnoOutFormat.BED;
        }
    }

}
