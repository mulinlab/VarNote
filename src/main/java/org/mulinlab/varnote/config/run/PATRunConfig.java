package org.mulinlab.varnote.config.run;

import htsjdk.variant.variantcontext.VariantContext;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.config.param.FilterParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.filters.mendelian.MendelianInheritanceADFilter;
import org.mulinlab.varnote.filters.query.gt.DepthFilter;
import org.mulinlab.varnote.filters.query.gt.GenotypeQualityFilter;
import org.mulinlab.varnote.utils.enumset.IndexType;
import org.mulinlab.varnote.utils.enumset.IntersectType;
import org.mulinlab.varnote.utils.node.LocFeature;
import org.mulinlab.varnote.utils.pedigree.PedFiles;
import org.mulinlab.varnote.utils.pedigree.Pedigree;
import org.mulinlab.varnote.utils.pedigree.PedigreeConverter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PATRunConfig extends OverlapRunConfig {

	private List<DBParam> dbParams;
	private String[] token = new String[2];
	private int count = 0;

	public PATRunConfig(final QueryFileParam query) {
		getDBPath();
		setQueryParam(query);
		setDbParams(dbParams);
	}

	@Override
	protected void initOutput() {
		outParam.setOutFileSuffix(".pat");
	}

	@Override
	protected void initOther() {
		initPrintter();
		databses.get(0).setVCFLocCodec(true, databses.get(0).getVcfParser().getCodec());
	}

	private void getDBPath() {
		if(dbParams == null) {
			dbParams = new ArrayList<>();
			dbParams.add(new DBParam("/Users/hdd/Desktop/data/data/gnomad.genomes.r2.0.1.sites.GRCh38.noVEP.vcf.gz", "d0",
					IntersectType.EXACT, IndexType.VARNOTE));
		}
	}


	@Override
	public List<String> getHeader() {
		return null;
	}

	public void processNode(final LocFeature node, final Map<String, LocFeature[]> results, final int index) {
		LocFeature[] gnomad = results.get("d0");

//		System.out.println("query: " + node.origStr);
		if(node.chr.equals("2") && node.beg == 219513555) {
			System.out.println();
		}

		VariantContext ctx;
		String AF;
		if(gnomad != null) {
			for (LocFeature feature : gnomad) {
				ctx = feature.variantContext;
				AF = ctx.getAttributeAsString("AF", "ERROR");
				AF = AF.replace("[", "").replace("]", "");
				if (!AF.equals("ERROR") && AF.indexOf(",") != -1) {
					for (String part : AF.split(",")) {
						if (Double.parseDouble(part) < 0.05) {
							System.out.println("hit gnomad: " + node.origStr);
							count++;
							break;
						}
					}
				} else {
					if (Double.parseDouble(AF) < 0.05) {
						System.out.println("hit gnomad: " + node.origStr);
						count++;
					}
				}
			}
		} else {
			System.out.println("not hit: " + node.origStr);
			count++;
		}
	}

	public static FilterParam getFilterParam() {
		Pedigree pedigree = PedFiles.readPedigree(new File("/Users/hdd/Desktop/vanno/wkegg/FSGS_0.ped").toPath());

		FilterParam filterParam = new FilterParam();
		filterParam.setMiFilter(new MendelianInheritanceADFilter(PedigreeConverter.convertToJannovarPedigree(pedigree)));
		filterParam.addGenotypeFilters(new DepthFilter(4));
		filterParam.addGenotypeFilters(new GenotypeQualityFilter(20));

		return filterParam;
	}

	public int getCount() {
		return count;
	}
}
