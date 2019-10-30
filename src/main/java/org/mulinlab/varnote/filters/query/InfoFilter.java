package org.mulinlab.varnote.filters.query;


import htsjdk.variant.variantcontext.VariantContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.utils.ExpressionUtils;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.node.LocFeature;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public final class InfoFilter implements VariantFilter<LocFeature> {

    final static Logger logger = LoggingUtils.logger;

    final private String key;
    final private String expression;
    private int filterCount;

    public InfoFilter(final String key, final String expression) {
        this.key = key;
        this.expression = expression;

        filterCount = 0;
    }

    @Override
    public boolean isFilterLine(final LocFeature loc) {
        VariantContext ctx = loc.variantContext;

        final String val = ctx.getAttributeAsString(key, "ERROR");
        if(val.equals("ERROR")) {
            filterCount ++;
            return true;
        }

        if(val.indexOf(",") != -1) {
            for (String part: val.split(",")) {
                if(ExpressionUtils.evalJudgement(expression.replace(key, val))) {
                    System.out.println(part);
                    return false;
                }
            }

            filterCount ++;
            return true;
        } else {
            if(ExpressionUtils.evalJudgement(expression.replace(key, val))) {
//                System.out.println(expression.replace(key, val));
                return false;
            } else {

                filterCount ++;
                return true;
            }
        }
    }

    @Override
    public void printLog() {
        logger.info(String.format("Filter out %d data not qualify with INFO %s ", filterCount, key));
    }
}
