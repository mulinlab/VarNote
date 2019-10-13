package org.mulinlab.varnote.cmdline.collection;

import org.broadinstitute.barclay.argparser.Argument;
import org.mulinlab.varnote.cmdline.constant.Arguments;
import org.mulinlab.varnote.config.param.DBParam;


import java.util.ArrayList;
import java.util.List;

public final class DBArgumentCollection {
    private static final long serialVersionUID = 1L;

    @Argument( fullName = Arguments.DB_INPUT_LONG, shortName = Arguments.DB_INPUT_SHORT, optional = false,
            doc = "Local path or http/ftp address of indexed database(or annotation) file(s). Note: Either VarNote index(.vanno.vi) or Tabix index(.tbi) should be in the same location.\n" +
                  "\nPossible attributes: {index, mode, tag}.\n" +
                    "index - The index type that should be used to retrieve data. Default value is \"VarNote\". Possible values: {VarNote, TBI}, optional \n" +
                    "mode - Mode of Intersection. default value is \"0\". Possible values: {0, 1, 2}, optional.\n" +
                    "\t   0: Intersect mode, perform common interaction operation\n\t      according to query and database formats;\n"  +
                    "\t   1: Exact match mode, force the program only to consider\n\t      the chromosome position of database records that exactly match\n\t      the corresponding chromosome position of query;\n"  +
                    "\t   2: Full close mode, force the program to report database\n\t      records that overlap both endpoints of query interval regardless\n\t      of original query and database formats.\n"  +
                    "tag - A label to rename the database in the output file, optional. By default, the program will use original file name as tag for the database.\n\n"
    )
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
