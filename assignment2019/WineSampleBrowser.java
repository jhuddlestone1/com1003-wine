package assignment2019;

/**
 * WineSampleBrowser.java
 * 
 * @version		1.0
 * @author		Jamie Huddlestone
 */

import assignment2019.codeprovided.*;
import javax.swing.*;

public class WineSampleBrowser {
	
	public static void main(String[] args) {
		
		// Use defaults for file locations if not provided as command line arguments
		if (args.length < 3) 
			args = new String[] {
				"resources/winequality-red.csv",
				"resources/winequality-white.csv",
				"resources/queries.txt"
			};
		String redWineFile = args[0];
		String whiteWineFile = args[1];
		String queriesFile = args[2];
		
		// Initialise dataset and GUI
		WineSampleCellar cellar = new WineSampleCellar(redWineFile, whiteWineFile, queriesFile);
		WineSampleBrowserPanel panel = new WineSampleBrowserPanel(cellar);
		
		JFrame window = new JFrame("Portuguese Wine Browser");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.add(panel);
		window.setSize(1280,720);
		window.setVisible(true);
		panel.executeQuery();
		
		// Answer questions in console
		String[] questions = {
			"How many wine samples are there?",
			"How many red wine samples are there?",
			"How many white wine samples are there?",
			"Which wine samples were graded with the best quality?",
			"Which wine samples were graded with the worst quality?",
			"Which wine samples have the highest PH?",
			"Which wine samples have the lowest PH?",
			"What is the highest value of alcohol grade for the whole sample of red wines?",
			"What is the lowest value of citric acid for the whole sample of white wines?",
			"What is the average value of alcohol grade for the whole sample of white wines?"
		};
		// Nasty hacks for implicit string conversion...
		String[] answers = {
			""+ cellar.getWineSampleCount(WineType.ALL),
			""+ cellar.getWineSampleCount(WineType.RED),
			""+ cellar.getWineSampleCount(WineType.WHITE),
			cellar.displayWineList(cellar.bestQualityWine(WineType.ALL)),
			cellar.displayWineList(cellar.worstQualityWine(WineType.ALL)),
			cellar.displayWineList(cellar.highestPH(WineType.ALL)),
			cellar.displayWineList(cellar.lowestPH(WineType.ALL)),
			""+ cellar.highestAlcoholContent(WineType.RED),
			""+ cellar.lowestCitricAcid(WineType.WHITE),
			""+ cellar.averageAlcoholContent(WineType.WHITE)
		};
		
		System.out.println("");
		for (int q=0; q < questions.length; q++) {
			System.out.println("----------------------- QUESTION #"+ (q+1) +" ------------------------");
			System.out.println(questions[q]);
			System.out.println(answers[q]);
		}
		System.out.println("------------------------------------------------------------");
		
	}
}
