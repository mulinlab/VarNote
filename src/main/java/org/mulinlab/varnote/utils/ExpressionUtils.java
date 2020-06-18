package org.mulinlab.varnote.utils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public final class ExpressionUtils {
    final static ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

    public static boolean evalJudgement(final String expression) {
        try {
            Object obj = engine.eval(expression);
            return (boolean)obj;
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return false;
    }
}
