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

import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;

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

public class Principal {
	static ArrayList<Result> listResults = new ArrayList<Result>();	
    static Calendar cal = Calendar.getInstance();
    static BufferedWriter outputResult = null;

	public static void main(String[] args) throws WrongFormatException, IOException { 
        File fileResults = new File("./currentResults.csv");
        outputResult = new BufferedWriter(new FileWriter(fileResults));
                
		String op = args[0];// flexdiag - prods - evolutionary
		
		if (op.equals("PFMDiagTest")){
		      org.junit.runner.Result result = JUnitCore.runClasses(PFMDiagReview.class);
				
		      for (Failure failure : result.getFailures()) {
		         System.out.println(failure.toString());
		      }
				
		      System.out.println(result.wasSuccessful());
		      
		      return;
		}
		
		String modelPath = args[1];


		if (op.equals("flexdiag")) {
			String productPath = args[2];
			Integer m = Integer.parseInt(args[3]);

			// ------------------------------
			String testing="";
			
			try{
			if (args[4] != null)
				testing = args[4];
			}
			catch(Exception ex){}
			
			if (!testing.equals("")){
				File folder = new File(modelPath);
				File[] listOfFiles = folder.listFiles();
				int countM = 0, maxM = 20;
				
				BufferedWriter writer = null;
	            //create a temporary file
	            String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
	            File logFile = new File(timeLog);

	            // This will output the full path where the file will be written to...
	            System.out.println(logFile.getCanonicalPath());

	            writer = new BufferedWriter(new FileWriter(logFile));
			            
				for (File file : listOfFiles) {
				    if (file.isFile()) {				    	
				    	String modelName = file.getName().substring(0,file.getName().length()-4);
				    	//System.out.println(modelName);
				    	
				    	File folderProduct = new File(productPath + "\\" + modelName);
						File[] listOfFilesP = folderProduct.listFiles();
						int countP = 0, maxP = 2;
						for (File fileP : listOfFilesP) {
					    	if  (fileP.getName().contains("50-5") ||  fileP.getName().contains("100-8")){
								
								///////////////////////////////////////////////////////
								XMLReader reader = new XMLReader();
								ProductManager pman = new ProductManager();

								FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(file.getPath());
								Product prod = pman.readProduct(fm, fileP.getPath());

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

								System.out.println("Prod Size: " + prod.getNumberOfFeatures());
								
								if (prod.getNumberOfFeatures() > 0 && reasoner.getRelations().size()>0){
								System.out.println(file.getName() + "|"
										+ fileP.getName() + "|" + prod + "|" + m + "|"
										+ fm.getFeaturesNumber() + "|" + fm.getNumberOfDependencies() + "|" + reasoner.getVariables().size()
										+ "|" + reasoner.getRelations().size() + "|" +start+ "|"+ end+"|"+ fmdiag.result.keySet());

								writer.write(file.getName() + " & " + fm.getFeaturesNumber() + " & " + fm.getNumberOfDependencies() + 
										      " & " + prod.getNumberOfFeatures() +  " & " + fmdiag.result.size() + " & " + (end-start));
								writer.newLine();
								countP++;
								}
								////////////////////////////////////					    	
					    	}
							if (countP==maxP)
								break;
						}
						countM++;
						if (countM==maxM)
							break;				
						 System.out.println(logFile.getCanonicalPath());
				    }
				    //System.out.println(logFile.getCanonicalPath());

				}
				
	            try {
	                // Close the writer regardless of what happens...
	                writer.close();
	            } catch (Exception e) {
	            }
			}				
			else{
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
				
				System.out.println("Prod Size: " + prod.getNumberOfFeatures());
				
				System.out.println(modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
					+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" + prod+ "|" + m + "|"
					+ fm.getFeaturesNumber() + "|" + fm.getNumberOfDependencies() + "|" + reasoner.getVariables().size()
					+ "|" + reasoner.getRelations().size() + "|" +start+ "|"+ end+"|"+ fmdiag.result.keySet());
				
				}
		}   		
		/*
		fmdiag.setConfiguration(prod);
		fmdiag.setRequirement(new Product());
		fmdiag.flexactive = true;
		fmdiag.m = m;
		
		long start = System.currentTimeMillis();
		reasoner.ask(fmdiag);
		long end = System.currentTimeMillis();

		System.out.println(modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
				+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" + prod+ "|" + m + "|"
				+ fm.getFeaturesNumber() + "|" + fm.getNumberOfDependencies() + "|" + reasoner.getVariables().size()
				+ "|" + reasoner..getRelations().size() + "|" +start+ "|"+ end+"|"+ fmdiag.result.keySet());
*/
	else if (op.equals("flexdiagP")) {
			String productPath = args[2];
			Integer m = Integer.parseInt(args[3]);
			Integer t = Integer.parseInt(args[4]);

			String testing="";
			
			try{
			if (args[5] != null)
				testing = args[5];
			}
			catch(Exception ex){}
			
			if (!testing.equals("")){	
				File file = new File(modelPath);
				
				boolean exists =      file.exists();      // Check if the file exists
				boolean isDirectory = file.isDirectory(); // Check if it's a directory
				boolean isFile =      file.isFile();  
			
				if (exists && isDirectory){
					File[] listOfFilesDir = file.listFiles();

					for (File fileModel : listOfFilesDir) {
					    if (fileModel.getAbsolutePath().contains(".xml")) {
					    	String model = fileModel.getName();
					    	File productDir = new File(productPath + "//" + model.substring(0, model.length()-4));
							
					   // 	System.out.println(fileModel + " - " + productDir);
					    	
							File[] listOfFilesProd = productDir.listFiles();

							for (File fileProduct : listOfFilesProd) {
							    if (fileProduct.getAbsolutePath().contains(".prod")) {
									FMDiag(fileModel.toString(), fileProduct.toString(), m, 0, testing);
						    	    PFMDiag(fileModel.toString(), fileProduct.toString(), m, t, testing);
						    	    Compare();
									outputResult.newLine();

							    }
							}					    	
					    }
					}
				}
				else if (exists && isFile){
					outputResult.newLine();
					FMDiag(modelPath, productPath, m, 0, testing);
					outputResult.newLine();
					
					PFMDiag(modelPath, productPath, m, t, testing);
					outputResult.newLine();					
					Compare();
					outputResult.newLine();
				}
			}
			else
				PFMDiag(modelPath, productPath, m, t, "");
		}
		
		outputResult.close();
	}
	
	static void Compare() throws IOException{
		Result actual = listResults.get(listResults.size()-1);
		
		String Dif = "";
		
        List<String> listFMDiag = new ArrayList<>(actual.resultsFMDiag);
        List<String> listPFMDiag = new ArrayList<>(actual.resultsPFMDiag);

        int i =0;
        
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
                
        String output = actual.modelPath + " | " + actual.productPath + " | ";
        
        if (Dif.isEmpty())
        	output = "Great" ;
        else
         	output = "Error";
        
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
         
		outputResult.write(output);
		outputResult.newLine();
	}
	
	static void PFMDiag(String modelPath, String productPath, Integer m, Integer t, String testing) throws WrongFormatException, IOException{
		//------------------------------
		XMLReader reader = new XMLReader();
		ProductManager pman = new ProductManager();

		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(modelPath);
		Product prod = pman.readProduct(fm, productPath);
		
		ChocoReasoner r = new ChocoReasoner();
		fm.transformTo(r);

		ChocoExplainErrorFMDIAGParalell3 flexdiagP  = new ChocoExplainErrorFMDIAGParalell3(m, t);			
		flexdiagP.flexactive = true;
		flexdiagP.m = m;
	
		flexdiagP.setConfiguration(prod);
		flexdiagP.setRequirement(new Product());

		long start = System.currentTimeMillis();
		r.ask(flexdiagP);
		long end = System.currentTimeMillis();
			
		String outputMsg = "PFMDiag: " + modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
				+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" + prod+ "|" + m + "|" + t + "|"
				+ fm.getFeaturesNumber() + "|" + fm.getNumberOfDependencies() + "|" + r.getVariables().size()
				+ "|" + r.getRelations().size() + "|" +start+ "|"+ end+"|"+ flexdiagP.result.keySet();
		
		System.out.println(outputMsg);
		outputResult.write(outputMsg);
		
		if (!testing.equals("")){
			Result actual = listResults.get(listResults.size()-1);
			actual.resultsPFMDiag = flexdiagP.result.keySet();
			actual.start2 = start; actual.end2 = end;
			actual.resultsPFMDiag = flexdiagP.result.keySet();
			
			listResults.set(listResults.size()-1, actual);
			
			System.out.println(actual.resultsFMDiag + "(" + (actual.end1-actual.start1) + ") - " + actual.resultsPFMDiag + "(" + (actual.end2-actual.start2) + ")");
			outputResult.write(actual.resultsFMDiag + "(" + (actual.end1-actual.start1) + ") - " + actual.resultsPFMDiag + "(" + (actual.end2-actual.start2) + ")");
			outputResult.newLine();
		}
	}
	
	static void FMDiag(String modelPath, String productPath, Integer m, Integer t, String testing) throws WrongFormatException, IOException{
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
		
//		System.out.println("Prod Size: " + prod.getNumberOfFeatures());
		
		String outputMsg = "FMDiag: " + modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
			+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" + prod+ "|" + m + "|"
			+ fm.getFeaturesNumber() + "|" + fm.getNumberOfDependencies() + "|" + reasoner.getVariables().size()
			+ "|" + reasoner.getRelations().size() + "|" +start+ "|"+ end+"|"+ fmdiag.result.keySet();
	
		System.out.println(outputMsg);
		outputResult.write(outputMsg);
		outputResult.newLine();
		
		if (!testing.equals("")){
			listResults.add(new Result(modelPath, productPath, prod, m, 0, fm.getFeaturesNumber(), start, end, 0, 0,fmdiag.result.keySet(), null));
		}
	}
}