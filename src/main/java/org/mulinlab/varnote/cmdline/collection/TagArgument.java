package org.mulinlab.varnote.cmdline.collection;


import org.broadinstitute.barclay.argparser.TaggedArgument;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.FormatType;
import org.mulinlab.varnote.utils.format.Format;

import java.util.HashMap;
import java.util.Map;

public final class TagArgument implements TaggedArgument {
    private String tagName;
    private Map<String, String> tagAttributes = new HashMap<>();
    public String argValue;

    public TagArgument(final String value) {
        this.argValue = value;
    }

    public TagArgument(final String value, final String tagName)
    {
        this(value);
        this.tagName = tagName;
    }

    @Override
    public void setTag(final String tagName) {
        this.tagName = tagName;
    }

    @Override
    public String getTag() {
        return tagName;
    }

    public String getArgValue() {
        return argValue;
    }

    @Override
    public void setTagAttributes(final Map<String, String> attributes) {
        this.tagAttributes.putAll(attributes);
    }

    @Override
    public Map<String, String> getTagAttributes() {
        return tagAttributes;
    }

    @Override
    public String toString() { return argValue; }

    public DBParam getDBParam() {
        DBParam dbParam = new DBParam(argValue);
        String index = tagAttributes.get("index");
        String mode = tagAttributes.get("mode");
        String tag = tagAttributes.get("tag");

        if(index != null) {
            dbParam.setIndexType(index);
        }

        if(mode != null) {
            dbParam.setIntersect(mode);
        }

        if(tag != null) {
            dbParam.setOutName(tag);
        }
        return dbParam;
    }

    public Format getFormat() {
        if(this.tagName == null) return null;
        return VannoUtils.checkQueryFormat(this.tagName);
    }

    public Format setFormat(Format format) {
        if(format.type == FormatType.TAB) {
            String chrom = tagAttributes.get("c");
            String begin = tagAttributes.get("b");
            String end = tagAttributes.get("e");
            if(chrom != null) format.sequenceColumn = Integer.parseInt(chrom);
            if(begin != null) format.startPositionColumn = Integer.parseInt(begin);
            if(end != null) format.endPositionColumn = Integer.parseInt(end);

            String zeroBased = tagAttributes.get("0");
            if(zeroBased != null && VannoUtils.strToBool(zeroBased)) format.setZeroBased();
        }

        if(format.type == FormatType.TAB || format.type == FormatType.BED) {
            String ref = tagAttributes.get("ref");
            String alt = tagAttributes.get("alt");
            String commentIndicator = tagAttributes.get("ci");

            if(ref != null) format.refPositionColumn = Integer.parseInt(ref);
            if(alt != null) format.altPositionColumn = Integer.parseInt(alt);

            if(commentIndicator != null &&!commentIndicator.equals(GlobalParameter.DEFAULT_COMMENT_INDICATOR))
                format.setCommentIndicator(commentIndicator);
        }

        return format;
    }
}


