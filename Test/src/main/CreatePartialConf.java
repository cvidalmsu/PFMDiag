package main;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;
import es.us.isa.FAMA.stagedConfigManager.Configuration;
import helpers.ProductManager;

public class CreatePartialConf {

	public static void main(String[] args) throws WrongFormatException {

		File dir = new File(args[0]);
		for (File f : dir.listFiles()) {
			if (f.getName().endsWith(".xml")) {
				XMLReader reader = new XMLReader();
				FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(f.getAbsolutePath());
				String name = f.getName().replaceAll("model-", "").replaceAll(".xml", "");

				// Integer ctcsNum = Integer.parseInt(name.substring(name.indexOf("-") + 1));

				System.out.println(name);
				int num = 5;
				Integer[] percentages= new Integer[] {5,10,30};
				ProductManager pm = new ProductManager();
				Map<Configuration, String> cs = pm.generateConfiguration(fm, percentages, num,f.getAbsolutePath());
				for (Entry<Configuration, String> c : cs.entrySet()) {
					pm.saveProduct(c.getKey().getAsProduct(), c.getValue());
					System.out.println(c.getValue());
				}
			}
		}

	}

}
