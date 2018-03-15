package main;

import java.io.File;

public class generateBash {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File f = new File("models/betty-partialConfs");
		for (File file:f.listFiles()) {
			if(file.getName().endsWith(".prod")) {
				String pP=file.getAbsolutePath();
				String mP=pP.substring(0,pP.lastIndexOf('-'))+".xml";
				
				String[] ops= {"CSP","FMDIAG"};
				for(String op:ops) {
					System.out.println("java -jar BOLON.jar "+mP+" "+pP+" "+op);
				}
			}
		}
	}

}
