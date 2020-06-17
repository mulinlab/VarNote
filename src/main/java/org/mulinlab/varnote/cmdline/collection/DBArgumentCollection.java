package org.mulinlab.varnote.cmdline.collection;

import org.broadinstitute.barclay.argparser.Argument;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.config.param.DBParam;


import java.util.ArrayList;
import java.util.List;

public final class DBArgumentCollection {
    private static final long serialVersionUID = 1L;

    @Argument( fullName = Arguments.DB_INPUT_LONG, shortName = Arguments.DB_INPUT_SHORT, doc =
            "Local path or http/ftp address of indexed database(or annotation) file(s). Note: Either VarNote index(.vanno.vi) or Tabix index(.tbi) should be in the same location.\n" + Arguments.DB_DOC)
    public List<TagArgument> dbFiles;

    public List<DBParam> getDBList() {
        List<DBParam> dbParams = new ArrayList<>();
        DBParam dbParam;

        for (int i = 0; i < dbFiles.size(); i++) {
            dbParam = dbFiles.get(i).getDBParam();

            dbParam.checkParam();
            dbParams.add(dbParam);
        }
        return dbParams;
    }
}
