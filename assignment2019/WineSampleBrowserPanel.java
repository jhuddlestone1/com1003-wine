package assignment2019;

/**
 * WineSampleBrowserPanel.java
 * 
 * @version	1.0
 * @author	Jamie Huddlestone
 */

import assignment2019.codeprovided.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;

public class WineSampleBrowserPanel extends AbstractWineSampleBrowserPanel {
    
    // Arrays to hold statistical values by property
    private int wineListSize;
    private int filteredListSize;
    private int numberOfProperties;
    private double[] max;
    private double[] min;
    private double[] avg;
    
    // Constructor
    public WineSampleBrowserPanel(AbstractWineSampleCellar cellar) {
	super(cellar);
	
	// defining the combobox used to select the wine property to build the filter (or SubQuery object) that will be applied (overridden to allow easy creation of WineType enums by simply removing spaces)
	propertyNames = new String[] {
	    "Fixed Acidity",
            "Volatile Acidity",
            "Citric Acid",
            "Residual Sugar",
            "Chlorides",
            "Free Sulfur Dioxide",
            "Total Sulfur Dioxide",
            "Density",
            "PH",
            "Sulphates",
            "Alcohol",
            "Quality"
	};
	comboProperties.setModel(new DefaultComboBoxModel<String>(propertyNames));
	
	numberOfProperties = propertyNames.length;
	max = new double[numberOfProperties];
	min = new double[numberOfProperties];
	avg = new double[numberOfProperties];
	
	// Override the row depth of the text areas so I can see them in the window size I've specified...
	statisticsTextArea.setRows(32);
	filteredWineSamplesTextArea.setRows(32);
    }
    
    /**
     * addListeners method - adds relevant actionListeners to the GUI components
     * You will need to listen (at least) to the following:
     * - buttonAddFilter
     * - buttonClearFilters
     * - comboWineTypes, if you want the samplesTextArea to be updated to show only the wine samples
     *            specified by this combobox
     * 
     */
    public void addListeners() {
	buttonAddFilter.addActionListener(event -> addFilter());
	buttonClearFilters.addActionListener(event -> clearFilters());
	comboWineTypes.addActionListener(event -> executeQuery());
    }

    /**
     * addFilter method - 
     * 1- this method is called when the JButton buttonAddFilter is clicked 
     * 2- adds a new filter (a QueryCondition object) to queryConditionsList ArrayList
     * 3- updates the GUI results accordingly, i.e. updates the three JTextAreas as follows:
     *    3a- queryConditionsTextArea will show the new QueryCondition
     *    3b- statisticsTextArea will show the updated statistics for the results after applying this filter
     *    3c- samplesTextArea will show the contents of filteredWineList (the results after applying this filter)
     */
    public void addFilter() {
	try {
	    // Create new queryCondition from filter menu values
	    String wineProperty = (String) comboProperties.getSelectedItem();
	    String operator = (String) comboOperators.getSelectedItem();
	    double value = Double.parseDouble(this.value.getText());
	    QueryCondition condition = new QueryCondition(
		WineProperty.valueOf(wineProperty.replaceAll("\\s+", "")), // removes spaces
		operator,
		value
	    );
	    // Add to queryConditionList and update text areas
	    queryConditionList.add(condition);
	    queryConditionsTextArea.append(condition.toString() +"; ");
	    executeQuery();
	}
	catch (NumberFormatException e) {
	    // Warn user if filter cannot be created from value given
	    JOptionPane.showMessageDialog(null,
		"'Value' field must contain a number.",
		"Invalid filter",
		JOptionPane.WARNING_MESSAGE
	    );
	}
    }

    /**
     * clearFilters method - clears all filters from the queryConditionsList ArrayList and updates
     * the relevant GUI components when the button buttonClearFilters is clicked
     */
    public void clearFilters() {
	queryConditionList.clear();
	queryConditionsTextArea.setText(null);
	executeQuery();
    }

    /**
     * updateStatistics method - updates the statistics to be displayed in the 
     * statisticsTextArea when the results being shown in the GUI need to be updated,
     * recalculates the average, minimum and maximum values for each wine property.
     */
    public void updateStatistics() {
	// Set up arrays to collect text from data, as in updateWineList below
	List<String> statistics = new ArrayList<>();
	List<String> maxList = new ArrayList<>();
	List<String> minList = new ArrayList<>();
	List<String> avgList = new ArrayList<>();
	// Decided to forget column alignment here... life is too short :)
	for (int p=0; p < numberOfProperties; p++) {
	    // Round each value to 2 d.p. before converting to String
	    maxList.add(String.valueOf(Math.round(max[p] * 100) / 100.0));
	    minList.add(String.valueOf(Math.round(min[p] * 100) / 100.0));
	    avgList.add(String.valueOf(Math.round(avg[p] * 100) / 100.0));
	}
	// ...add each array of statistical values to the text array
	statistics.add("\t"+ String.join("\t", propertyNames) +"\t");
	statistics.add("Maximum\t"+ String.join("\t", maxList));
	statistics.add("Minimum\t"+ String.join("\t", minList));
	statistics.add("Average\t"+ String.join("\t", avgList));
	statistics.add("\nShowing "+ filteredListSize +" out of "+ wineListSize +" samples.");
	// Update text area with results
	statisticsTextArea.setText(String.join("\n", statistics));
	statisticsTextArea.setCaretPosition(0);
    }

    /**
     * updateWineList method - updates the wine list when changes are made
     */
    public void updateWineList() {
	
	// Clear statistics arrays
	Arrays.fill(max, filteredListSize == 0 ? 0 : Double.NEGATIVE_INFINITY);
	Arrays.fill(min, filteredListSize == 0 ? 0 : Double.POSITIVE_INFINITY);
	Arrays.fill(avg, 0);
	// Create a new array to hold lines of text for each sample
	List<String> wineList = new ArrayList<String>();
	// We can grab column headings from the same list as populates the combo box!
	wineList.add("ID\t"+"Wine Type\t"+ String.join("\t", propertyNames) +"\t");
	for (WineSample wine : filteredWineSampleList) {
	    // Create temporary array to iterate efficiently over numeric properties
	    // These must be in the same order as in propertyNames...
	    double[] wineSample = {
		wine.getFixedAcidity(),
		wine.getVolatileAcidity(),
		wine.getCitricAcid(),
		wine.getResidualSugar(),
		wine.getChlorides(),
		wine.getFreeSulfurDioxide(),
		wine.getTotalSulfurDioxide(),
		wine.getDensity(),
		wine.getpH(),
		wine.getSulphates(),
		wine.getAlcohol(),
		wine.getQuality()
	    };
	    // Add text to array... please forgive a couple of cheap hacks to make columns line up here!
	    // This layout seems to work on Windows JRE 1.8.0_201, can't guarantee other platforms
	    wineList.add(String.join("\t", new String[] {
		String.valueOf(wine.getId()),
		String.valueOf(wine.getType()),
		String.valueOf(wineSample[0]),
		String.valueOf(wineSample[1]),
		String.valueOf(wineSample[2]),
		String.valueOf(wineSample[3]),
		String.valueOf(wineSample[4]),
		String.valueOf(wineSample[5]) +"\t",
		String.valueOf(wineSample[6]) +"\t",
		String.valueOf(wineSample[7]),
		String.valueOf(wineSample[8]),
		String.valueOf(wineSample[9]),
		String.valueOf(Math.round(wineSample[10] * 100) / 100.0),  // rounding to 2 d.p.
		String.valueOf(wineSample[11])
	    }));
	    // Update each statistical value for later handling by updateStatistics()
	    for (int p=0; p < numberOfProperties; p++) {
		max[p] = Math.max(max[p], wineSample[p]);
		min[p] = Math.min(min[p], wineSample[p]);
		avg[p] += wineSample[p]; // summing for now, will average below
	    }
	}
	// Calculate average values now that this array contains the sums of values for each property
	for (int p=0; p < numberOfProperties; p++) {
	    avg[p] /= filteredListSize;
	}
	// Update text areas with results
	filteredWineSamplesTextArea.setText(String.join("\n", wineList));
	filteredWineSamplesTextArea.setCaretPosition(0);
	// And then...
	updateStatistics();
    }

    /**
     * executeQuery method - executes the complete query to the relevant wine list
     */
    public void executeQuery() {
	// Get wine type from current value of combo box
	WineType wineType = WineType.valueOf((String) comboWineTypes.getSelectedItem());
	// Set filtered list to result of query if conditions given...
	if (!queryConditionList.isEmpty()) {
	    Query query = new Query(cellar.getWineSampleList(wineType), queryConditionList, wineType);
	    filteredWineSampleList = query.solveQuery();
	}
	// ...else get the whole list for the current wine type
	else {
	    filteredWineSampleList = cellar.getWineSampleList(wineType);
	}
	// Store list sizes for further processing later
	wineListSize = cellar.getWineSampleCount(wineType);
	filteredListSize = filteredWineSampleList.size();
	// And then...
	updateWineList();
    }
}
