package org.mulinlab.varnote.config.param.postDB;


public final class DBAnnoParam extends PostDBParam{

    private String fields;
    private String infofields;
    private String cols;
    private String outNames;
    private String vcfInfoPath;

    public DBAnnoParam(final String label) {
        this.label = label;
    }

    public static DBAnnoParam defaultParam(final String label) {
        DBAnnoParam annoParam = new DBAnnoParam(label);
        annoParam.fields = "[ALL]";
        return annoParam;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getInfofields() {
        return infofields;
    }

    public void setInfofields(String infofields) {
        this.infofields = infofields;
    }

    public String getCols() {
        return cols;
    }

    public void setCols(String cols) {
        this.cols = cols;
    }

    public String getOutNames() {
        return outNames;
    }

    public void setOutNames(String outNames) {
        this.outNames = outNames;
    }

    public String getVcfInfoPath() {
        return vcfInfoPath;
    }

    public void setVcfInfoPath(String vcfInfoPath) {
        this.vcfInfoPath = vcfInfoPath;
    }
}
