package main;

import static org.junit.Assert.*;
import helpers.ProductManager;

import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorFMDIAG;
import es.us.isa.Choco.fmdiag.configuration.ChocoExplainErrorFMDIAGParalell;
import es.us.isa.ChocoReasoner.ChocoReasoner;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;

@RunWith(value = Parameterized.class)
public class PFMDiagTest{
	private String modelPath;
	private String productPath;

	@Parameters
	public static Collection testData() throws IOException {
		return getTestData("FMDiagVSPFMDiagSPLOT.csv");
	}
	
	
	public PFMDiagTest(String modelPath, String productPath){
		this.modelPath = modelPath;
		this.productPath = productPath;
	}

	public static Collection<String[]> getTestData(String fileName) throws IOException{
		ArrayList<String[]> records = new ArrayList<String[]>();
		
		String record;
		BufferedReader file = new BufferedReader(new FileReader(fileName));
		while ((record = file.readLine())!=null){
			String fields[]=record.split(",");
			records.add(fields);
		}
		
		file.close();
		return records;
	}
	
	@Test
	public void testFMDiagPFMDiag() throws WrongFormatException{
		XMLReader reader = new XMLReader();
		ProductManager pman = new ProductManager();
				
		FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseFile(modelPath);
		Product prod = pman.readProduct(fm, productPath);
		
		ChocoReasoner reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
		
		///FMDiag
		ChocoExplainErrorFMDIAG fmdiag = new ChocoExplainErrorFMDIAG();
		fmdiag.setConfiguration(prod);
		fmdiag.setRequirement(new Product());
		fmdiag.flexactive = false;
		fmdiag.m = 1;
		
		long start = System.currentTimeMillis();
		reasoner.ask(fmdiag);
		long end = System.currentTimeMillis();
		long dif = (end-start);
		
     	///PFMDiag (2 Threads)
		ChocoExplainErrorFMDIAGParalell fmdiagP2  = new ChocoExplainErrorFMDIAGParalell(1, 2);			
		fmdiagP2.flexactive = false;
		fmdiagP2.setConfiguration(prod);
		fmdiagP2.setRequirement(new Product());

		long start2 = System.currentTimeMillis();
		reasoner.ask(fmdiagP2);
		long end2 = System.currentTimeMillis();
		long dif2 = (end2-start2);
				
		////PFMDiag 4 Threads
	    ChocoExplainErrorFMDIAGParalell fmdiagP4  = new ChocoExplainErrorFMDIAGParalell(1, 2);			
		fmdiagP4.flexactive = false;
		fmdiagP4.setConfiguration(prod);
		fmdiagP4.setRequirement(new Product());
	
		long start4 = System.currentTimeMillis();
		reasoner.ask(fmdiagP4);
		long end4 = System.currentTimeMillis();
		long dif4 = (end4-start4);
			
		Object[] array1 = fmdiag.result.keySet().toArray();
		Object[] array2 = fmdiagP2.result.keySet().toArray();
		Object[] array4 = fmdiagP4.result.keySet().toArray();
   
		//FMDiag vs PFMDiag 2 Threads
		Assert.assertArrayEquals(array1, array2);

	//	assertTrue("FMDiad (" + dif + ") should be greater than PFMDiag using 2 Threads (" + dif2 + ")", dif > dif2);

		//FMDiag vs PFMDiag 4 Threads
		Assert.assertArrayEquals(array1, array4);

	//	assertTrue("FMDiad (" + dif + ") should be greater than PFMDiag using 4 Threads (" + dif4 + ")", dif > dif4);
	}
	
}