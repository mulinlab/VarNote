package org.mulinlab.varnote.config.param;


import org.mulinlab.varnote.utils.format.Format;
import htsjdk.samtools.util.IOUtil;
import java.io.File;

public final class IndexParam extends Param {

    private String input;
    private String inputFileName;
    private String outputDir;
    protected Format format = Format.newTAB();

    public IndexParam(final File input) {
        IOUtil.assertFileIsReadable(input);
        this.inputFileName = input.getName();
        this.input = input.getAbsolutePath();
    }

    public IndexParam(final File input, final String outputDir) {
        this(input);
        setOutputDir(outputDir);
    }

    public IndexParam(final File input, final String outputDir, final Format format) {
        this(input, outputDir);
        setFormat(format);
    }

    public void setDefaultOutDir() {
        this.outputDir = new File(this.input).getParent();
    }

    @Override
    public void checkParam() {
        if(outputDir == null) {
            setDefaultOutDir();
        }

        if(format == null) this.format = Format.defaultFormat(this.input, false);
    }


    public String getInput() {
        return input;
    }

    public void setOutputDir(String outputDir) {
        if(outputDir != null) {
            File dir = new File(outputDir);
            IOUtil.assertDirectoryIsWritable(dir);
            this.outputDir = dir.getAbsolutePath();
            dir = null;
        }
    }


    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public String getInputFileName() {
        return inputFileName;
    }
}
