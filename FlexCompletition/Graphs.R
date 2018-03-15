library(plyr)
library(doBy)
library(ggplot2)
library(reshape2)
library(stringr)
library(xtable)
library(tableHTML)

data = read.csv("./result.csv", header = TRUE,sep='|')
data$time<-data$end-data$start

data$pctc <-sapply(strsplit(as.character(gsub(".xml","",data$modelName)), "-"), tail, 1)
data$pfeats <-sapply(strsplit(as.character(gsub(".prod","",data$productName)), "-"), tail, 1)


mf_labeller <- function(var, value){
  value <- as.character(value)
  if (var=="eCTC") { 
    value[value=="0"] <- "0 complex constraints"
    value[value=="2"]   <- "2 complex constraints"
    value[value=="5"]   <- "5 complex constraints"
  }
  if (var=="pctc") { 
    value[value=="5"] <- "0% cross-tree constraints"
    value[value=="10"]   <- "10% cross-tree constraints"
    value[value=="15"]   <- "15% cross-tree constraints"
    value[value=="30"]   <- "30% cross-tree constraints"
    value[value=="50"]   <- "50% cross-tree constraints"
    
      }
  return(value)
}

data <- subset(data, select = -c(start,end,product) )
summary <- summaryBy( time ~ feats  + technique, data = data, FUN = function(x) { c(october = mean(x)) } )

firstExp<-ggplot(summary, aes(reorder(feats,as.numeric(feats)),time.october, fill = as.factor(technique))) + geom_bar(position="dodge",stat="identity",colour="black") + scale_y_continuous("Time in milliseconds")    +
  scale_x_discrete("Number of Features") +  theme_bw()+
  scale_fill_grey(start = 0.3, end = 1,name="Completition Technique") +   
  theme(axis.text.x = element_text(angle = 90, hjust = 1, size = 9))  +   theme(legend.position="bottom") 

ggsave("experiment1.pdf",  width = 12)

#save it

