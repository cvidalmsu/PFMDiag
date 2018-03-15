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
import helpers.ChocoBOLONExtendsConfiguration;
import helpers.ProductManager;

public class ConfigureVSFMDiag {

	static PrintWriter out;

	public static void main(String[] args) throws WrongFormatException, FileNotFoundException {

		// args[0]= inputModel
		// args[1]= productModel
		// args[2]= operation

		String modelP = args[0];
		String productP = args[1];
		String operation = args[2];
		// System.out.println("technique|modelName|productName|feats|ctc|start|end|suggestionSize|finalSize");

		XMLReader reader = new XMLReader();
		ProductManager pman = new ProductManager();

		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(modelP);
		Product prod = pman.readProduct(fm, productP);

		if (operation.equals("CSP")) {
			execMinCSP(modelP, productP, fm, prod);
		} else if (operation.equals("FMDIAG")) {
			execFMDiag(modelP, productP, fm, prod);
		}

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

	public static void execFMDiag(String modelName, String productName, FAMAFeatureModel fm, Product p) {
		ChocoReasoner r = new ChocoReasoner();
		fm.transformTo(r);

		ChocoBOLONExtendsConfiguration pq = new ChocoBOLONExtendsConfiguration();

		pq.configuration = getProductAsConfiguration(p, fm);
		long start = System.currentTimeMillis();
		r.ask(pq);
		long end = System.currentTimeMillis();

		System.out.println("FMDIAG|" + modelName + "|" + productName + "|" + fm.getFeaturesNumber() + "|"
				+ fm.getNumberOfDependencies() + "|" + start + "|" + end + "|"
				+ (p.getNumberOfFeatures() + pq.result.size()) + "|" + pq.result.keySet() + p);
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
