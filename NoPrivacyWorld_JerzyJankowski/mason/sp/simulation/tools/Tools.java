package sp.simulation.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
/**
 * class with useful methods across the simulator
 */
public class Tools {
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	/**
	 * 
	 * @param value value to round
	 * @return value rounded to second decimal place
	 */
	public static double round(double value) {
	    return round(value, 2);
	}
	
	public static ArrayList<List<String>> readCSV() {
		String csvFile = "input.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		ArrayList<List<String>> values = new ArrayList<List<String>>();
		try {
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				 
		        // use comma as separator
				List<String> splitted =  Arrays.asList(line.split(cvsSplitBy, -1));
				if(splitted.size() > 1)
					values.add(splitted);
	 
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return values;
		
	}
	
    private static void waitSeconds(int x) {
   	 	try {
			Thread.sleep(1000 *x);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}
