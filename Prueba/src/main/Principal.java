package main;

import java.io.File;

import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorEvolutionary;
import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorFMDIAG;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;
import helpers.ProductManager;

public class Principal {

	public static void main(String[] args) throws WrongFormatException {

		String op = args[0];// flexdiag - prods - evolutionary
		String modelPath = args[1];

		if (op.equals("flexdiag")) {

			String productPath = args[2];
			Integer m = Integer.parseInt(args[3]);

			// ------------------------------

			XMLReader reader = new XMLReader();
			ProductManager pman = new ProductManager();

			FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(modelPath);
			Product prod = pman.readProduct(fm, productPath);

			ChocoReasoner reasoner = new ChocoReasoner();
			fm.transformTo(reasoner);

			ChocoExplainErrorFMDIAG fmdiag = new ChocoExplainErrorFMDIAG();
			fmdiag.setConfiguration(prod);
			fmdiag.setRequirement(new Product());

			fmdiag.flexactive = true;
			fmdiag.m = m;

			reasoner.ask(fmdiag);

			System.out.println(modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
					+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|" + m + "|" + fm.getFeaturesNumber()
					+ "|" + fm.getNumberOfDependencies() + "|" + reasoner.getVariables().size() + "|"
					+ reasoner.getRelations().size() + "|" + fmdiag.result.keySet());

		}else if(op.equals("evolutionary")){

			String productPath = args[2];
			XMLReader reader = new XMLReader();
			ProductManager pman = new ProductManager();

			FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(modelPath);
			Product prod = pman.readProduct(fm, productPath);

			ChocoReasoner reasoner = new ChocoReasoner();
			fm.transformTo(reasoner);

			ChocoExplainErrorEvolutionary evol = new ChocoExplainErrorEvolutionary();
			evol.setConfiguration(prod);
			evol.setRequirement(new Product());

			reasoner.ask(evol);

			System.out.println(modelPath.substring(modelPath.lastIndexOf(File.separator) + 1) + "|"
					+ productPath.substring(productPath.lastIndexOf(File.separator) + 1) + "|"  + fm.getFeaturesNumber()
					+ "|" + fm.getNumberOfDependencies() + "|" + reasoner.getVariables().size() + "|"
					+ reasoner.getRelations().size() + "|" + evol.result.keySet());

			
		}else if(op.equals("generateProducts")){
			
			ProductManager man = new ProductManager();
			File dir = new File(modelPath);//directorio donde andan los xml
			for(File f: dir.listFiles()){
				if (f.getName().endsWith(".xml")) {
					String newDirName = f.getName().replaceAll(".xml", "");
					XMLReader reader = new XMLReader();
					
					FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(f.getPath());
					int[] percentages = {10,20,30,40,50};
					for(int p:percentages){
						int psize=p*fm.getFeaturesNumber()/100;
						File newDir = new File(dir.getPath()+File.separator+newDirName+File.separator);
						newDir.mkdirs();
						
						for(int i =0;i<10;i++){
							Product generateProduct = man.generateProduct(fm, psize);
							for(int j=0;j<10;j++){
								man.saveShuffledProduct(generateProduct, newDir.getPath()+File.separator+p+"-"+i+"-"+j);
								System.out.println("Generating and saving " + generateProduct);
								
							}
						}
					}
					
	       
		        }

			}
		}

	}

}
