package assignment2019;

/**
 * WineSampleCellar.java
 * 
 * @version	1.0
 * @author	Jamie Huddlestone
 */

import assignment2019.codeprovided.*;
import java.util.*;
import java.io.*;

public class WineSampleCellar extends AbstractWineSampleCellar {
    
    /**
     * constructor - reads wine sample datasets and list of queries from text file,
     * initialises the wineSampleRacks Map, and processes queries from query file
     */
    public WineSampleCellar(String redWineFilename, String whiteWineFilename, String queryFilename) {
	super(redWineFilename, whiteWineFilename, queryFilename);
	// Fetch, answer and print queries
	List<String> textQueries = readQueryFile(queryFilename);	
	List<Query> queries = readQueries(textQueries);
	for (int q=0; q < queries.size(); q++) {
	    System.out.println("------------------------- QUERY #"+ (q+1) +" -------------------------");
	    System.out.println(textQueries.get(q));
	    displayQueryResults(queries.get(q));
	}
	System.out.println("------------------------------------------------------------");
    }
    
    /**
     * readQueryFile - reads each query from queryFile and stores them in an ArrayList
     * for further processing and interpretation by the readQueries method
     * (override of readQueryFile method from AbstractWineSampleCellar; keeps queries as intact strings)
     * @param queryFile The text file containing relevant queries
     * @return ArrayList<ArrayList<String>> Multi-dimensional ArrayList of queries split into tokens
     */
    public static List<String> readQueryFile(String queryFile) throws IllegalArgumentException {

        List<String> textQueries = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(queryFile))) {
            String line = br.readLine();
            if (line == null) {
                throw new IllegalArgumentException("File is empty");
            }
            while (line != null) {
		textQueries.add(line.toLowerCase());
                line = br.readLine();
            }

        } catch (FileNotFoundException e) {
            System.out.println(queryFile + " could not be found");

        } catch (IOException e) {
            System.out.println("File could not be handled");
            // enable the next line for debugging purposes
            // e.printStackTrace();
        }
        return textQueries;
    }
    
    /**
     * readQueries method - 
     * 1 - receives the List of Strings, each of the is one query as specified
     * in the handout (section "The queries")
     * 2 - assesses their content, creates the relevant QueryCondition and Query objects 
     * 3 - and then returns a List of the Query objects
     *
     * @param queryList The List of Strings from the readQueryFile method
     * @return List of all Query objects
     */
    public List<Query> readQueries(List<String> queryList) {
	List<Query> queryObjectList = new ArrayList<>();
	
	for (String query : queryList) {
	    
	    // Split query into tokens representing keywords, wine types or conditions
	    List<String> queryTokens = new ArrayList<>(Arrays.asList(query.split("(?<=\\w)\\s+(?=\\w)")));
	    // Remove any remaining spaces
	    queryTokens.replaceAll(s -> s.replaceAll("\\s", ""));
	    // Filter extraneous keywords
	    queryTokens.removeIf(s -> s.matches("select|and|or"));
	    // Partition remainder of tokens into those representing WineTypes and QueryConditions
	    int where = queryTokens.indexOf("where");
	    List<String> typeTokens = queryTokens.subList(0, where);
	    List<String> conditionTokens = queryTokens.subList(where+1, queryTokens.size());
	    
	    // If there is more than one element in typeTokens, select all wines
	    WineType wineType = WineType.valueOf(
		typeTokens.size() > 1 ? "ALL" : typeTokens.get(0).toUpperCase()
	    );
	
	    List<QueryCondition> queryConditionList = new ArrayList<>();
	    while (!conditionTokens.isEmpty()) {
		// Split condition into separate tokens along word boundaries:
		String[] tokens = conditionTokens.remove(0).split("\\b");
		List<String> conditions = new ArrayList<String>(Arrays.asList(tokens));
		// First take the property code...
		WineProperty wineProperty = WineProperty.fromFileIdentifier(conditions.remove(0));
		// ...then grab the operator...
		String operator = conditions.remove(0);
		// ... then interpret the remainder as a numeric value (this handles the radix point!)
		double value = Double.parseDouble(String.join("", conditions));
		// And finally, process all of that as a QueryCondition.
		queryConditionList.add(new QueryCondition(wineProperty, operator, value));
	    }
	    
	    // Create new Query object and add to list
	    queryObjectList.add(new Query(getWineSampleList(wineType), queryConditionList, wineType));
	}
	return queryObjectList;
    }
    
    /**
     * updateCellar method - updates wineSampleRacks to contain 'also' an additional list 
     * containing ALL wine samples (in this case red and white)
     */
    public void updateCellar() {
	List<WineSample> allWines = new ArrayList<>();
	allWines.addAll(wineSampleRacks.get(WineType.RED));
	allWines.addAll(wineSampleRacks.get(WineType.WHITE));
	wineSampleRacks.put(WineType.ALL, allWines);
    }

    /**
     * displayQueryResults method - displays in console the results of a query in a meaningful format to the user
     *
     * @param query The Query object to be printed to console
     */
    public void displayQueryResults(Query query) {
	List<WineSample> filteredWineList = query.solveQuery();
	System.out.println(displayWineList(filteredWineList));
    }

    /**
     * displayWineList method - displays in console the contents of a wine list in a meaningful format to the user
     *
     * @param query The Query object to be printed
     * @return String the text to be displayed in the console
     */
    public String displayWineList(List<WineSample> wineList) {
	List<String> text = new ArrayList<>();
	int n = wineList.size();
	text.add(n +" wine sample"+ (n == 1 ? " matches" : "s match") +" your query.");
	for (WineSample wine : wineList) {
	    text.add(
		"Sample #"+ wine.getId() +": "+
		wine.getType() +" wine, "+
		String.join(", ", new String[] {
		    "f_acid: "+ wine.getFixedAcidity(),
		    "v_acid: "+ wine.getVolatileAcidity(),
		    "c_acid: "+ wine.getCitricAcid(),
		    "r_sugar: "+ wine.getResidualSugar(),
		    "chlorid: "+ wine.getChlorides(),
		    "f_sulf: "+ wine.getFreeSulfurDioxide(),
		    "t_sulf: "+ wine.getTotalSulfurDioxide(),
		    "dens: "+ wine.getDensity(),
		    "pH: "+ wine.getpH(),
		    "sulph: "+ wine.getSulphates(),
		    "alc: "+ wine.getAlcohol(),
		    "qual: "+ wine.getQuality()
		})
	    );
	}
	return String.join("\n", text);
    }

    /**
     * PropertyComparator.java
     *
     * Comparator class necessary to sort the contents in the TreeSet built by solveQuery in Query object
     * (copied here from Query.java, as it's package-private there!) 
     *
     * @version 1.0  06/04/2019
     *
     * @author Maria-Cruz Villa-Uriol (m.villa-uriol@sheffield.ac.uk)
     */
    private class PropertyComparator implements Comparator<WineSample>
    {
	WineProperty propertyToCompare;

	public PropertyComparator(WineProperty wineProperty)
	{
	    propertyToCompare = wineProperty;
	}

	public int compare(WineSample a, WineSample b)
	{
	    double propA = a.getProperty(propertyToCompare);
	    double propB = b.getProperty(propertyToCompare);

	    if (propA > propB) return 1;
	    if (propA < propB) return -1;
	    return 0;
	}
    }

    /**
     * bestQualityWine method - receives the wine type
     * Returns a list of objects which have been assigned the highest quality score in the list.
     *
     * @param wineType Either RED, WHITE or ALL
     * @return collection of WineSample objects with the highest quality
     */
    public List<WineSample> bestQualityWine(WineType wineType) {
	// Fetch wine list by type
	List<WineSample> wineList = new ArrayList<>(getWineSampleList(wineType));
	// Obtain max value for Quality
	double value = Collections.max(wineList, new PropertyComparator(WineProperty.Quality)).getQuality();
	// Filter out wines that do not match that value, and return the remaining list
	wineList.removeIf(x -> x.getQuality() != value);
	return wineList;
    }

    /**
     * worstQualityWine method - receives the wine type 
     * Returns a list of objects which have been assigned the lowest quality score in the list.
     *
     * @param wineType Either RED, WHITE or ALL
     * @return collection of WineSample objects with the lowest quality
     */
    public List<WineSample> worstQualityWine(WineType wineType) {
	// Fetch wine list by type
	List<WineSample> wineList = new ArrayList<>(getWineSampleList(wineType));
	// Obtain min value for Quality
	double value = Collections.min(wineList, new PropertyComparator(WineProperty.Quality)).getQuality();
	// Filter out wines that do not match that value, and return the remaining list
	wineList.removeIf(x -> x.getQuality() != value);
	return wineList;
    }

    /**
     * highestPH method - receives the wine type 
     * Returns a list of objects which have the highest pH in the list.
     *
     * @param wineType Either RED, WHITE or ALL
     * @return collection of WineSample objects with the highest pH
     */
    public List<WineSample> highestPH(WineType wineType) {
	// Fetch wine list by type
	List<WineSample> wineList = new ArrayList<>(getWineSampleList(wineType));
	// Obtain max value for pH
	double value = Collections.max(wineList, new PropertyComparator(WineProperty.PH)).getpH();
	// Filter out wines that do not match that value, and return the remaining list
	wineList.removeIf(x -> x.getpH() != value);
	return wineList;
    }
    
    /**
     * lowestPH method - receives the wine type 
     * Returns a list of objects which have the lowest pH in the list.
     *
     * @param wineType Either RED, WHITE or ALL
     * @return collection of WineSample objects with the lowest pH
     */
    public List<WineSample> lowestPH(WineType wineType) {
	// Fetch wine list by type
	List<WineSample> wineList = new ArrayList<>(getWineSampleList(wineType));
	// Obtain min value for pH
	double value = Collections.min(wineList, new PropertyComparator(WineProperty.PH)).getpH();
	// Filter out wines that do not match that value, and return the remaining list
	wineList.removeIf(x -> x.getpH() != value);
	return wineList;
    }

    /**
     * highestAlcoholContent method - receives the wine type
     * Returns the group of objects which have the highest alcohol content in the list.
     *
     * @param wineType Either RED, WHITE or ALL
     * @return collection of WineSample objects with the highest alcohol content
     */
    public double highestAlcoholContent(WineType wineType) {
	// Fetch wine list by type
	List<WineSample> wineList = new ArrayList<>(getWineSampleList(wineType));
	// Obtain max value for Alcohol
	return Collections.max(wineList, new PropertyComparator(WineProperty.Alcohol)).getAlcohol();
    }

    /**
     * lowestCitricAcid method - receives the wine type
     * Returns the group of objects which have the lowest citric acid content in the list.
     *
     * @param wineType Either RED, WHITE or ALL
     * @return collection of WineSample objects with the lowest citric acid content
     */
    public double lowestCitricAcid(WineType wineType) {
	// Fetch wine list by type
	List<WineSample> wineList = new ArrayList<>(getWineSampleList(wineType));
	// Obtain min value for CitricAcid
	return Collections.min(wineList, new PropertyComparator(WineProperty.CitricAcid)).getCitricAcid();
    }

    /**
     * averageAlcoholContent method - receives the wine type
     * Returns the count variable divided by the number of objects in the List.
     *
     * @param wineType Either RED, WHITE or ALL
     * @return average alcohol content of the list as a double
     */
    public double averageAlcoholContent(WineType wineType) {
	// Fetch wine list by type
	List<WineSample> wineList = new ArrayList<>(getWineSampleList(wineType));
	// Obtain average value for Alcohol
	double value = 0;
	for (WineSample wine : wineList) {
	    value += wine.getAlcohol(); // summing for now, will average below
	}
	return value / wineList.size();
    }
}
