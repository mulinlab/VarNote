package main.java.vanno.bean.config.anno.vcf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import htsjdk.tribble.TribbleException;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.variant.vcf.AbstractVCFCodec;
import htsjdk.variant.vcf.VCFConstants;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderVersion;


public class VCFCodec extends AbstractVCFCodec {
    // Our aim is to read in the records and convert to VariantContext as quickly as possible, relying on VariantContext to do the validation of any contradictory (or malformed) record parameters.
    public final static String VCF4_MAGIC_HEADER = "##fileformat=VCFv4";
    
    /**
     * Reads all of the header from the provided iterator, but no reads no further.
     * @param lineIterator the line reader to take header lines from
     * @return The parsed header
     */
    @Override
    public Object readActualHeader(final LineIterator lineIterator) {
        final List<String> headerStrings = new ArrayList<String>();

        String line;
        boolean foundHeaderVersion = false;
        while (lineIterator.hasNext()) {
            line = lineIterator.peek();
            lineNo++;
            if (line.startsWith(VCFHeader.METADATA_INDICATOR)) {
                final String[] lineFields = line.substring(2).split("=");
                if (lineFields.length == 2 && VCFHeaderVersion.isFormatString(lineFields[0]) ) {
                		if(lineFields[1].equals("VCFv4.3")) {
                			lineFields[1] = "VCFv4.2";
                			//todo
                		}
                		
                    if ( !VCFHeaderVersion.isVersionString(lineFields[1]))
                        throw new TribbleException.InvalidHeader(lineFields[1] + " is not a supported version");
                    foundHeaderVersion = true;
                    version = VCFHeaderVersion.toHeaderVersion(lineFields[1]);
                    
                		if ( ! version.isAtLeastAsRecentAs(VCFHeaderVersion.VCF4_0) )
                        throw new TribbleException.InvalidHeader("This codec is strictly for VCFv4; please use the VCF3 codec for " + lineFields[1]);
                    if ( version != VCFHeaderVersion.VCF4_0 && version != VCFHeaderVersion.VCF4_1 && version != VCFHeaderVersion.VCF4_2 )
                        throw new TribbleException.InvalidHeader("This codec is strictly for VCFv4 and does not support " + lineFields[1]);
                    
                }
                headerStrings.add(lineIterator.next());
            }
            else if (line.startsWith(VCFHeader.HEADER_INDICATOR)) {
                if (!foundHeaderVersion) {
                    throw new TribbleException.InvalidHeader("We never saw a header line specifying VCF version");
                }
                headerStrings.add(lineIterator.next());
                super.parseHeaderFromLines(headerStrings, version);
                return this.header;
            }
            else {
                throw new TribbleException.InvalidHeader("We never saw the required CHROM header line (starting with one #) for the input VCF file");
            }

        }
        throw new TribbleException.InvalidHeader("We never saw the required CHROM header line (starting with one #) for the input VCF file");
    }

    /**
     * parse the filter string, first checking to see if we already have parsed it in a previous attempt
     *
     * @param filterString the string to parse
     * @return a set of the filters applied or null if filters were not applied to the record (e.g. as per the missing value in a VCF)
     */
    @Override
    protected List<String> parseFilters(final String filterString) {
        // null for unfiltered
        if ( filterString.equals(VCFConstants.UNFILTERED) )
            return null;

        if ( filterString.equals(VCFConstants.PASSES_FILTERS_v4) )
            return Collections.emptyList();
        if ( filterString.equals(VCFConstants.PASSES_FILTERS_v3) )
            generateException(VCFConstants.PASSES_FILTERS_v3 + " is an invalid filter name in vcf4", lineNo);
        if (filterString.isEmpty())
            generateException("The VCF specification requires a valid filter status: filter was " + filterString, lineNo);

        // do we have the filter string cached?
        if ( filterHash.containsKey(filterString) )
            return filterHash.get(filterString);

        // empty set for passes filters
        final List<String> fFields = new LinkedList<String>();
        // otherwise we have to parse and cache the value
        if ( !filterString.contains(VCFConstants.FILTER_CODE_SEPARATOR) )
            fFields.add(filterString);
        else
            fFields.addAll(Arrays.asList(filterString.split(VCFConstants.FILTER_CODE_SEPARATOR)));

        filterHash.put(filterString, Collections.unmodifiableList(fFields));

        return fFields;
    }

    @Override
    public boolean canDecode(final String potentialInput) {
        return canDecodeFile(potentialInput, VCF4_MAGIC_HEADER);
    }
}
