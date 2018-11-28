package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.ChocoReasoner.questions.ChocoMaxFeatsProductQuestion;
import es.us.isa.ChocoReasoner.questions.ChocoMinimalOneProductQuestion;
import es.us.isa.ChocoReasoner.questions.ChocoValidProductQuestion;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.GenericFeature;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;
import es.us.isa.FAMA.stagedConfigManager.Configuration;
import es.us.isa.Sat4j.fmdiag.Sat4jExplainErrorFMDIAG;
import es.us.isa.Sat4jReasoner.Sat4jReasoner;
import helpers.ChocoBOLONExtendsConfiguration;
import helpers.ProductManager;
import helpers.SATBOLONExtendsConfiguration;

public class ConfigureVSFMDiag {

	static PrintWriter out;

	public static void main(String[] args) throws WrongFormatException, FileNotFoundException {

		// args[0]= inputModel
		// args[1]= productModel
		// args[2]= operation
		// args[3]= kind of FMDiag

		String modelP = args[0];
		String productP = args[1];
		String operation = args[2];

		// System.out.println("technique|modelName|productName|feats|ctc|start|end|suggestionSize|finalSize");
		String option = "";
		if (args.length>3)
		   option = args[3];

		XMLReader reader = new XMLReader();
		ProductManager pman = new ProductManager();

		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(modelP);
		Product prod = pman.readProduct(fm, productP);

		if (operation.equals("CSP")) {
			execMinCSP(modelP, productP, fm, prod);
		} else if (operation.equals("FMDIAG")) {
			if (option.isEmpty() || option.equals("CSP"))
				execFMDiag(modelP, productP, fm, prod);
			else
				execFMDiagSAT(modelP, productP, fm, prod);					
		}

	}

	public static void execFMDiag(String modelName, String productName, FAMAFeatureModel fm, Product p) {
		ChocoReasoner r = new ChocoReasoner();
		fm.transformTo(r);

		ChocoBOLONExtendsConfiguration pq = new ChocoBOLONExtendsConfiguration();

		pq.configuration = getProductAsConfiguration(p, fm);
		long start = System.currentTimeMillis();
		r.ask(pq);
		long end = System.currentTimeMillis();

		System.out.println("FMDIAG CSP|" + modelName + "|" + productName + "|" + fm.getFeaturesNumber() + "|"
				+ fm.getNumberOfDependencies() + "|" + start + "|" + end + "|"
				+ (p.getNumberOfFeatures() + pq.result.size()) + "|" + pq.result.keySet() + p);
	}

	public static void execFMDiagSAT(String modelName, String productName, FAMAFeatureModel fm, Product p) {
		Sat4jReasoner r = new Sat4jReasoner();
		fm.transformTo(r);

		SATBOLONExtendsConfiguration pq = new SATBOLONExtendsConfiguration();

		pq.configuration = getProductAsConfiguration(p, fm);
		long start = System.currentTimeMillis();
		r.ask(pq);
		long end = System.currentTimeMillis();

		System.out.println("FMDIAG SAT|" + modelName + "|" + productName + "|" + fm.getFeaturesNumber() + "|"
				+ fm.getNumberOfDependencies() + "|" + start + "|" + end + "|"
				+ (p.getNumberOfFeatures() + pq.result.size()) + "|" + pq.result + p);
	}

	public static void execMinCSP(String modelName, String productName, FAMAFeatureModel fm, Product p) {
		ChocoReasoner r = new ChocoReasoner();
		fm.transformTo(r);
		ChocoMinimalOneProductQuestion pq = new ChocoMinimalOneProductQuestion();
		r.applyStagedConfiguration(p.getProductAsConfiguration());
		long start = System.currentTimeMillis();
		r.ask(pq);
		long end = System.currentTimeMillis();

		System.out.println("CSP|" + modelName + "|" + productName + "|" + fm.getFeaturesNumber() + "|"
				+ fm.getNumberOfDependencies() + "|" + start + "|" + end + "|" + pq.getProduct().getNumberOfElements()
				+ "|" + pq.getProduct());
	}

	public static void execCSP(FAMAFeatureModel fm, Product p) {
		ChocoReasoner r = new ChocoReasoner();
		fm.transformTo(r);
		ChocoMaxFeatsProductQuestion pq = new ChocoMaxFeatsProductQuestion();
		r.applyStagedConfiguration(p.getProductAsConfiguration());
		long start = System.currentTimeMillis();
		r.ask(pq);
		long end = System.currentTimeMillis();
		System.out.println("CSP:" + (end - start));
		System.out.println("CSP:" + pq.getProduct().getNumberOfElements());
		System.out.println("CSP:" + pq.getProduct());
		System.out.println(isValidProduct(fm, pq.getProduct()));

	}

	static boolean isValidProduct(FAMAFeatureModel fm, Product p) {
		ChocoReasoner r = new ChocoReasoner();
		fm.transformTo(r);
		ChocoValidProductQuestion vpq = new ChocoValidProductQuestion();
		vpq.setProduct(p);
		r.ask(vpq);
		return vpq.isValid();
	}

	private static Configuration getProductAsConfiguration(Product p, FAMAFeatureModel fm) {
		Configuration c = new Configuration();
		for (GenericFeature f : fm.getFeatures()) {

			if (p.getFeatures().contains(f)) {
				c.addElement(f, 1);
			} else {
				c.addElement(f, 0);
			}

		}
		return c;
	}

	// private static boolean isValidConfiguration(FAMAFeatureModel fm, Product
	// prod) {
	// ChocoReasoner r = new ChocoReasoner();
	// fm.transformTo(r);
	// ChocoValidConfigurationQuestion vpq = new ChocoValidConfigurationQuestion();
	// vpq.setConfiguration(prod.getProductAsConfiguration());
	// r.ask(vpq);
	// return vpq.isValid();
	// }
}
