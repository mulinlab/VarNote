package org.mulinlab.varnote.config.run;

import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.parser.ResultParser;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.JannovarUtils;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.enumset.FormatType;
import org.mulinlab.varnote.utils.enumset.IntersectType;
import org.mulinlab.varnote.utils.node.LocFeature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AdvanceToolRunConfig extends OverlapRunConfig {

    public final static String ROAD_MAP_LABEL = GlobalParameter.ROAD_MAP_LABEL;
    public final static String REGBASE_MAP_LABEL = GlobalParameter.REGBASE_MAP_LABEL;
    public final static String GENOMAD_LABEL = GlobalParameter.GENOMAD_LABEL;
    public final static String COSMIC_LABEL = GlobalParameter.COSMIC_LABEL;
    public final static String ICGC_LABEL = GlobalParameter.ICGC_LABEL;
    public final static String DBNSFP_LABEL = GlobalParameter.DBNSFP_LABEL;


    protected JannovarUtils jannovarUtils = null;
    protected final static Map<String, IntersectType> requiredDB = new HashMap<>();
    protected ResultParser[] parsers;

    public AdvanceToolRunConfig(final QueryFileParam query, final List<DBParam> dbConfigs, final JannovarUtils jannovarUtils) {
        super(query, dbConfigs);

        this.jannovarUtils = jannovarUtils;
        if(query.getQueryFormat().isRefAndAltExsit() && this.jannovarUtils != null) {
            this.jannovarUtils.setJannovarData();
        }
    }

    @Override
    protected void initDB() {
        final Map<String, Boolean> checkedDB = new HashMap<>();
        for (String key: requiredDB.keySet()) {
            checkedDB.put(key, false);
        }

        for (int i = 0; i < dbParams.size(); i++) {
            if(requiredDB.get(dbParams.get(i).getOutName()) != null) {
                checkedDB.put(dbParams.get(i).getOutName(), true);
                dbParams.get(i).setIntersect(requiredDB.get(dbParams.get(i).getOutName()));
            } else {
                dbParams.remove(i);
                i--;
            }
        }

        for (String db: checkedDB.keySet()) {
            if(!checkedDB.get(db)) throw new InvalidArgumentException(String.format("%s genomes database is required.", db));
        }
    }

    @Override
    protected void initOther() {
        initPrintter();
    }

    protected String getVariantEffectHeader() {
        if(jannovarUtils != null) {
            return String.format("%s\t%s\t%s\t", "Variant_Effect", "Gene_Id", "Gene_Symbol");
        } else {
            return "";
        }
    }

    protected void initDatabaseByDefault() {
        for (Database db: databses) {
            db.readHeader();
            if (db.getFormat().getType() == FormatType.VCF) {
                db.setVCFLocCodec(true, db.getVcfParser().getCodec());
            } else {
                db.setDefaultLocCodec(true);
            }
        }
    }

    public abstract void processNode(final LocFeature node, final Map<String, LocFeature[]> results, final int index);
}
