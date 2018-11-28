package main;

import java.io.File;

import es.us.isa.Choco.fmdiag.configuration.ChocoConfigurationExtensionBOLONFMDIAG;
import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorFMDIAGParalell;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;
import helpers.ProductManager;

public class ParallelVSSecuential {

	public static void main(String[] args) throws WrongFormatException {
		
			String modelPath =  "./tests/1/model.xml";;
			String productPath = "./tests/1/product.prod";
			Integer m = 2;
			Integer t = 1;
			
			// ------------------------------
			// Secuential
			//------------------------------
			
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

			System.out.println(modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
					+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" + prod+ "|" + m + "|"
					+ fm.getFeaturesNumber() + "|" + fm.getNumberOfDependencies() + "|" + reasoner.getVariables().size()
					+ "|" + reasoner.getRelations().size() + "|" +start+ "|"+ end+"|"+ fmdiag.result.keySet());

			
			//------------------------------
			// Parallel
			//------------------------------
			

			reasoner = new ChocoReasoner();
			fm.transformTo(reasoner);

			ChocoExplainErrorFMDIAGParalell flexdiagP  = new ChocoExplainErrorFMDIAGParalell(m, t);
			flexdiagP.flexactive = true;
	
			start = System.currentTimeMillis();
			reasoner.ask(flexdiagP);
			end = System.currentTimeMillis();

			System.out.println(modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
					+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" + prod+ "|" + m + "|" + t + "|"
					+ fm.getFeaturesNumber() + "|" + fm.getNumberOfDependencies() + "|" + reasoner.getVariables().size()
					+ "|" + reasoner.getRelations().size() + "|" +start+ "|"+ end+"|"+ flexdiagP.result.keySet());
		
	}

}
