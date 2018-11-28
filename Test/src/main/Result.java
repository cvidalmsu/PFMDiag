package main;

import java.util.Set;

import es.us.isa.FAMA.models.featureModel.Product;

public class Result {
	public String modelPath;
	public String productPath;
	public Product prod;
	
	public Integer m, t;
	public Integer featuresNumber_FM;
	
	public long start1, end1;
	public long start2, end2;
	
	
	public Set<String> resultsPFMDiag;
	public Set<String> resultsFMDiag;
	
		
	public Result(String modelPath, String productPath, Product prod, Integer m, Integer t, Integer featuresNumber_FM,  
			      long start1, long end1, long start2, long end2,  Set<String> resultsFMDiag,  Set<String> resultsPFMDiag){
		this.modelPath = modelPath;
		this.productPath = productPath;
		this.prod = prod;
		this.m = m;
		this.t = t;
		this.featuresNumber_FM = featuresNumber_FM;
		
		this.start1 = start1; this.end1 = end1;
		this.start2 = start2; this.end2 = end2;
		
		this.resultsPFMDiag = resultsPFMDiag;
		this.resultsFMDiag = resultsFMDiag;
	}
}
