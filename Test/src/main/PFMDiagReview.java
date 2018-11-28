package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import choco.kernel.model.constraints.Constraint;
import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorEvolutionary;
import es.us.isa.Choco.fmdiag.configuration.ChocoConfigurationExtensionBOLONFMDIAG;
import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorFMDIAGParalell;
import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorFMDIAGParalell3;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.FAMA.Reasoner.questions.DetectErrorsQuestion;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.GenericFeature;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;
import es.us.isa.FAMA.stagedConfigManager.Configuration;
import es.us.isa.Sat4j.fmdiag.Sat4jExplainErrorFMDIAG;
import es.us.isa.Sat4jReasoner.Sat4jReasoner;
import es.us.isa.Sat4jReasoner.questions.Sat4jDetectErrorsQuestion;
import helpers.ChocoBOLONExtendsConfiguration;
import helpers.ChocoModel;
import helpers.ChocoPureExplainErrorEvolutionary;
import helpers.ChocoPureExplainErrorFMDIAG;
import helpers.ProductManager;

public class PFMDiagReview {
	static ArrayList<Result> listResults = new ArrayList<Result>();	
    static BufferedWriter outputResult = null;

    static String op1="", op2="", modelPath="", productPath="";
    static Integer m=0, t=0;
    static String output="";
    
    public static void main(String[] args) throws WrongFormatException, IOException { 
        try{
    	   op1 = args[0];// Alg1
		   op2 = args[1];// Alg2
		
  		   modelPath = args[2];
  		   productPath = args[3];
			
  		   m = Integer.parseInt(args[4]);
  	       t = Integer.parseInt(args[5]);
        }catch(Exception ex){
        	System.out.println("Error: wrong argument!"); 
        	System.exit(0);
        }
        
        if (!op1.equals("FMDiag") || !op2.equals("PFMDiag")){
        	System.out.println("Error: 1st Argument must be FMDiag and 2nd Argumen PFMDiagt!"); 
        	System.exit(0);
        }
        	
        if (m != 1){
        	System.out.println("Error: m must be 1!"); 
        	System.exit(0);
        }
        
        if (t != 2 && t!=4){
        	System.out.println("Error: t must 2 or 4!"); 
        	System.exit(0);
        }

        output="";
        File file = new File(modelPath);
				
		boolean exists =      file.exists();      // Check if the file exists
		boolean isFile =      file.isFile();  
			
		if (exists && isFile){
			FMDiag(modelPath, productPath, m, 0);
					
			PFMDiag(modelPath, productPath, m, t);
			Compare();
			//System.out.println("Fin");
		}			
	}
	
	static void Compare() throws IOException{
		Result actual = listResults.get(listResults.size()-1);
		
		String Dif = "";
		
        List<String> listFMDiag = new ArrayList<>(actual.resultsFMDiag);
        List<String> listPFMDiag = new ArrayList<>(actual.resultsPFMDiag);

        int i = 0;
        
        while ((i < listFMDiag.size()) || (i < listPFMDiag.size())){
        	String act1="";
        	String act2 = "";
	        
        	if (i<listFMDiag.size() && i < listPFMDiag.size()){
	        	act1 = listFMDiag.get(i);
	           	act2 = listPFMDiag.get(i);
	        }
        	else if (i<listFMDiag.size())
        		act1 = listFMDiag.get(i);
        	else
        		act2 = listPFMDiag.get(i);
        
           	if (!act1.equalsIgnoreCase(act2)){
           		if (!Dif.isEmpty())
           		    Dif = ", ";
        
           		Dif= Dif + act1 + " - " + act2;
           	}
        	i++;
        }
                
        output = output + actual.modelPath + " | " + actual.productPath + " | ";
        
        if (!Dif.isEmpty()){
         	output = "Error: (" + Dif + ") | ";
        
         	long time1 = (actual.end1-actual.start1);
         	long time2 = (actual.end2-actual.start2);
        
         	output = output + listFMDiag + " (" + time1 + ") | " + 
                          listPFMDiag + "(" + time2 + ") | ";
         	if (time1 < time2)
         		output = output + " FMDiag!";
         	else if (time1 == time2)
         		output = output + " Equals";
         	else
         		output = output + " PFMDiag!";
         
         	System.out.println(output);
        }
	}
	
	static void PFMDiag(String modelPath, String productPath, Integer m, Integer t) throws WrongFormatException, IOException{
		//------------------------------
		XMLReader reader = new XMLReader();
		ProductManager pman = new ProductManager();

		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(modelPath);
		Product prod = pman.readProduct(fm, productPath);
		
		ChocoReasoner r = new ChocoReasoner();
		fm.transformTo(r);

		ChocoExplainErrorFMDIAGParalell flexdiagP  = new ChocoExplainErrorFMDIAGParalell(m, t);			
		flexdiagP.flexactive = true;
		flexdiagP.m = m;
	
		flexdiagP.setConfiguration(prod);
		flexdiagP.setRequirement(new Product());

		long start = System.currentTimeMillis();
		r.ask(flexdiagP);
		long end = System.currentTimeMillis();
				
		output = output + "PFMDiag: " + modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
				+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" + prod+ "|" + m + "|" + t + "|"
				+ fm.getFeaturesNumber() + "|" + fm.getNumberOfDependencies() + "|" + r.getVariables().size()
				+ "|" + r.getRelations().size() + "|" +start+ "|"+ end+"|"+ flexdiagP.result.keySet();
		
		
		Result actual = listResults.get(listResults.size()-1);
		actual.resultsPFMDiag = flexdiagP.result.keySet();
		actual.start2 = start; actual.end2 = end;
		actual.resultsPFMDiag = flexdiagP.result.keySet();
			
		listResults.set(listResults.size()-1, actual);
	}
	
	static void FMDiag(String modelPath, String productPath, Integer m, Integer t) throws WrongFormatException, IOException{
		///////////////////////////////////////////////////////
		XMLReader reader = new XMLReader();
		ProductManager pman = new ProductManager();
		
		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(modelPath);
		Product prod = pman.readProduct(fm, productPath);
		
		ChocoReasoner reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
		
		ChocoConfigurationExtensionBOLONFMDIAG fmdiag = new ChocoConfigurationExtensionBOLONFMDIAG();
		fmdiag.setConfiguration(prod);
		fmdiag.setRequirement(new Product());
		fmdiag.flexactive = true;
		fmdiag.m = m;
		long start = System.currentTimeMillis();
		reasoner.ask(fmdiag);
		long end = System.currentTimeMillis();
		
		output = output + "FMDiag: " + modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
				+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" + prod+ "|" + m + "|"
				+ fm.getFeaturesNumber() + "|" + fm.getNumberOfDependencies() + "|" + reasoner.getVariables().size()
				+ "|" + reasoner.getRelations().size() + "|" +start+ "|"+ end+"|"+ fmdiag.result.keySet();

     	listResults.add(new Result(modelPath, productPath, prod, m, 0, fm.getFeaturesNumber(), start, end, 0, 0,fmdiag.result.keySet(), null));		
	}
}