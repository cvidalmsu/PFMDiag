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


public class Principal2 {

	public static void main(String[] args) throws WrongFormatException, IOException {	    
		String op = args[0];// flexdiag - prods - evolutionary
		String modelPath = args[1];
		
		if (op.equals("flexdiag")) {
			String productPath = args[2];
			Integer m = Integer.parseInt(args[3]);
			FMDiag(modelPath, productPath, m, 0);		
		}
			

		else if (op.equals("flexdiagP")) {
				String productPath = args[2];
				Integer m = Integer.parseInt(args[3]);
				Integer t = Integer.parseInt(args[4]);
	
				PFMDiag(modelPath, productPath, m, t);				
		} 
	}
	
	
	static void PFMDiag(String modelPath, String productPath, Integer m, Integer t) throws WrongFormatException{
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
			
		System.out.println(modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
				+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" + prod+ "|" + m + "|" + t + "|"
				+ fm.getFeaturesNumber() + "|" + fm.getNumberOfDependencies() + "|" + r.getVariables().size()
				+ "|" + r.getRelations().size() + "|" +start+ "|"+ end+"|"+ flexdiagP.result.keySet());
		
	}
	
	static void FMDiag(String modelPath, String productPath, Integer m, Integer t) throws WrongFormatException{
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