package org.mulinlab.varnote.cmdline.collection;


import org.broadinstitute.barclay.argparser.TaggedArgument;
import org.mulinlab.varnote.config.param.DBParam;

import java.util.HashMap;
import java.util.Map;

public final class AnnotationFile implements TaggedArgument {
    private String tagName;
    private Map<String, String> tagAttributes = new HashMap<>();
    public String argValue;

    public AnnotationFile(final String value) {
        this.argValue = value;
    }

    public AnnotationFile(final String value, final String tagName)
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
}


