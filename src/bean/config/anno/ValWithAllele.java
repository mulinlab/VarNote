package bean.config.anno;

public class ValWithAllele {
	String allele;
	final String str;
	
	public ValWithAllele(final String allele, final String str) {
		this.allele = allele;
		this.str = str;
	}

	public String getAllele() {
		return allele;
	}

	public String getStrWithAllele() {
		return str.replace(FieldFormat.VARIABLE_BEGIN + FieldFormat.ALLELE, this.allele);
	}
	
	public String getStrWithAllele(final String allele) {
		return str.replace(FieldFormat.VARIABLE_BEGIN + FieldFormat.ALLELE, allele);
	}
	
	public String getStr() {
		return str;
	}

	public void setAllele(String allele) {
		this.allele = allele;
	}
}
