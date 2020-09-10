1. Time and scalability benchmarks

1): To benchmark the VarNote and compared tools for interval-level intersection:

BCFtools	1.6 (using htslib 1.6)	bcftools annotate -a {DATABASE FILE} -c CHROM,FROM,TO,ANN -h {HEADER FILE} --threads 1 {QUERY FILE}
BEDOPS	2.4.37 (megarow)	bedops -i {QUERY FILE} {DATABASE FILE}
BEDTools	v2.27.1	bedtools intersect -wa -wb -a {QUERY FILE} -b {DATABASE File} -sorted
GIGGLE	v0.6.3	giggle search -i {DATABASE INDEX DIRECTORY} -q {QUERY FILE} -o -v
vcfanno	0.2.8 (built with go1.8)	vcfanno -p 1 {DATABASE CONFIGURE FILE} {QUERY FILE}
VEP	91.1	vep --dir {VEP DATABASE DIRECTORY} --assembly GRCh37 --vcf --format vcf -i {QUERY FILE} -custom {DATABASE FILE},ANN,bed,overlap,1 --offline --fork 1
VarNote	v1.0	java -jar VarNote.jar Intersect -Q {QUERY FILE} -D {DATABASE FILE} -T 1

2): To benchmark the VarNote and compared tools for variant-level annotation:

bcftools annotate -a {DATABASE FILE} -c {BED FIELDS} -h {HEADER LINES} --threads {THREADS}  {QUERY FILE}   #for BED
bcftools annotate -a {DATABASE FILE} -c {FIELDS} --threads {THREADS}  {QUERY FILE}   #for VCF

java -jar VarNote.jar Annotation -Q {QUERY FILE} -D:db,mode=1 {DATABASE FILE} -T {THREADS} -A {FIELDS} -loj true

vcfanno -p {THREADS} {CONFIGURE FILE} {QUERY FILE}

vep --dir {VEP DATABASE} --assembly GRCh37 --vcf --format vcf -i {QUERY FILE} -custom {DATABASE FILE},{DATABASE ID},bed,exact,0 --offline --fork {THREADS}   #for BED
vep --dir {VEP DATABASE} --assembly GRCh37 --vcf --format vcf -i {QUERY FILE} -custom {DATABASE FILE},{DATABASE ID},vcf,exact,0,{DATABASE FIELDS} --offline --fork {THREADS}     #for VCF


2. VarNote disease-causal variant/mutation prioritization benchmarks

--data: the folder contains evaluation input datasets for VarNote-REG, PAT, CAN, please refer to our paper for detailed information.
--data: the folder contains evaluation outputs for VarNote-REG, PAT, CAN, please refer to our paper for detailed information.
--prioritization: the folder contains codes for reproducing the prioritization comparison results.
