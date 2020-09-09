library(ggplot2)
library(ggpubr)

##############           REG         ################################################
rm(dataset)
cells = c("E114", "E115", "E116", "E117", "E118", "E119", "E120", "E121", "E122", "E123","E124","E125", "E126", "E127", "E128", "E129")
for (cell in cells) {
  celldata = read.delim(paste("~/reg/", cell, ".srank.txt", sep=""), head=FALSE)
  celldata$cell = cell
  if (!exists("dataset")){
    dataset <- celldata
  } else if (exists("dataset")){
    dataset <- rbind(dataset, celldata)
  }
}

colnames(dataset) = c("fun", "val", "rank", "rankRatio","prob", "group", "label", "rsid", "cell")
dataset = dataset[dataset$label == "Query", ]
dataset$fun = as.factor(dataset$fun)
levels(dataset$fun)[2] <- "FUN-LDA"
levels(dataset$fun)[5] <- "VarNote-REG" 
levels(dataset$fun)[6] <- "cepip" 
dataset$fun = factor(dataset$fun, levels=c("FitCons2","GenoSkylinePlus","FUN-LDA","GenoNet","cepip","VarNote-REG"))

outdir="~/reg/pdf/"
for (cell in cells) {
  dir.create(paste(outdir,cell, sep=""))
  cellData = dataset[dataset$cell == cell, ]
  cellData[cellData$prob >= 0.3, "probflag"] = 1 
  cellData[cellData$prob < 0.3 & cellData$prob >= 0.05, "probflag"] = 2
  cellData[cellData$prob < 0.05, "probflag"] = 3
  
  dp <- ggplot(cellData, aes(x=fun, y=rankRatio, fill=factor(probflag))) + geom_boxplot(width=0.5, outlier.shape=NA)
  dp <- dp + labs(x="", y="Rank Ratio") + theme_pubr(base_size=15, base_family="serif") + ylim(0, 1) + 
    scale_fill_manual(values=c( "#F39B7F", "#4DBBD5", "#999999"), name="Probability", breaks=c( "1", "2", "3"), labels=c( "[0.3, 1)", "[0.05, 0.3)", "(0, 0.05)"))
  dp <- dp +  theme(axis.text.x=element_text(angle=25, hjust=1, size=12), axis.text.y=element_text(size=12))
  
  ggsave(paste(outdir, cell,"/", cell, "-reg.pdf", sep=""), dp, width = 12, height = 8, units = "cm")
}



##############           PAT         ################################################
rm(dataset)
files = list.files("~/pat/rank/", pattern = ".rank.txt$")
for (file in files) {
  print(file)
  data = read.delim(paste("~/pat/rank/", file, sep=""), head=FALSE)
  if (!exists("dataset")){
    dataset <- data
  } else if (exists("dataset")){
    dataset<-rbind(dataset, data)
  }
}

colnames(dataset) = c("Function", "Rank")
dataset$Function = as.factor(dataset$Function)
levels(dataset$Function)[2] <- "VarNote-PAT" 
dataset$Function = factor(dataset$Function, levels=c("CADD","Eigen","FATHMM-MKL","FATHMM-XF","GenoCanyon","LINSIGHT","ReMM","VarNote-PAT"))
dataset = dataset[!is.na(dataset$Function), ]

dp <- ggplot(dataset, aes(x=Function, y=Rank, fill=Function)) + geom_boxplot(width=0.5, outlier.shape=NA)
dp <- dp + scale_fill_manual(values=c("#8491B4", "#4DBBD5", "#00A087", "#3C5488", "#F39B7F", "#B15928","#92D050", "#F54E54", "#1F78B4", "#FB9A99"))
dp <- dp + labs(x="", y="Rank Ratio", fill="") + theme_pubr(base_size=15, base_family="serif", legend="none")
dp <- dp + theme(axis.text.x=element_text(angle=25, hjust=1, size=12), axis.text.y=element_text(size=12))
dp <- dp + stat_compare_means(comparisons = list(c("VarNote-PAT", "ReMM"), c("VarNote-PAT", "LINSIGHT"), c("VarNote-PAT", "GenoCanyon"), c("VarNote-PAT", "FATHMM-XF"), c("VarNote-PAT", "FATHMM-MKL"), c("VarNote-PAT", "Eigen"), c("VarNote-PAT", "CADD"))
                              ,method = "wilcox.test", method.args = list(alternative="less"), label.y = c(0.4, 0.52, 0.64, 0.76, 0.88, 1, 1.12), size =2.5)

outfolder = "~/pat/"
fop <- file.path(outfolder, "pat_boxplot.png")
ggsave(fop, dp, dpi=300, width = 12, height = 8, units = "cm")




##############           CAN         ################################################
rm(dataset)
dataset <- read.delim("~/can/all.tsv", header=FALSE)
colnames(dataset) = c("Rank", "Function")
dataset$Function = as.factor(dataset$Function)
levels(dataset$Function)[2] <- "VarNote-CAN" 
dataset$Function = factor(dataset$Function, levels=c('CADD', 'Eigen', 'FATHMM-MKL','GenoCanyon', 'CScape', 'FunSeq2','VarNote-CAN'))
dataset = dataset[!is.na(dataset$Function), ]

dp <- ggplot(dataset,aes(x=Function, y=Rank, fill=Function)) + geom_boxplot(width=0.5, outlier.shape=NA) + ylim(0, 1.8)
dp <- dp + scale_fill_manual(values=c("#8491B4", "#4DBBD5", "#00A087", "#F39B7F", "#9c9ede", "#A97C73", "#E64B35"))
dp <- dp + labs(x="", y="Rank Ratio") + theme_pubr(base_size=15, base_family="serif", legend="none")
dp <- dp +  theme(axis.text.x=element_text(angle=25, hjust=1, size=12), axis.text.y=element_text(size=12))
dp <- dp + stat_compare_means(comparisons = list(c("VarNote-CAN", "FunSeq2"), c("VarNote-CAN", "CScape"), c("VarNote-CAN", "GenoCanyon"), c("VarNote-CAN", "FATHMM-MKL"), c("VarNote-CAN", "Eigen"), c("VarNote-CAN", "CADD")), method = "wilcox.test", method.args = list(alternative="less"), label.y = c(1.1, 1.22, 1.34, 1.46, 1.58, 1.7), size =2.5)

outfolder = "~/can/rank/"
fop <- file.path(outfolder, "can_boxplot.png")
ggsave(fop, dp, dpi=300, width = 12, height = 8, units = "cm")