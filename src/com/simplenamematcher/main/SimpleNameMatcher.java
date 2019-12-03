package com.simplenamematcher.main;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Locale;

import com.simplenamematcher.exceptions.NonPermittedSymbolException;
import com.simplenamematcher.exceptions.ThresholdOutOfRangeException;


//Created by Álvaro López-Müller 2019.

public class SimpleNameMatcher {

	private static final double changeInDiacriticWeight = 0.1; 
	private static final double halfSeparationWeight = 0.3; 
	private static final double fullSeparationWeight = 0.5; 
	private static final double changeInletterWeight = 1.2; 
	
	
	public double compareNames(String name1, String name2) throws NonPermittedSymbolException {
		
		//Avoids non-latin names and forbidden characters. 
		if(!name1.matches("^[\\p{IsLatin}|\\-| '|´|\\.|,|’|‘]*$") || !name2.matches("^[\\p{IsLatin}|\\-| '|´|\\.|,|’|‘]*$")) {
			NonPermittedSymbolException e = new NonPermittedSymbolException();
			throw e;
		}

		//Initial clean up of the names.
		name1 = cleanName(name1);
		name2 = cleanName(name2);
		
		String biggerName, smallerName;
		
		String name1WithoutSeparations = removeSeparators(name1);
		String name2WithoutSeparations = removeSeparators(name2);
		
		double impactFactor; // The bigger it is the name, the less impact have changes in it, and that is reflected by this impact factor. 
		
		if(name1WithoutSeparations.length() >= name2WithoutSeparations.length()) {
			
			biggerName = name1;
			smallerName = name2;
			impactFactor = (1.0/name1WithoutSeparations.length()) * 100; 
			
		}else {
			
			biggerName = name2;
			smallerName = name1;
			impactFactor = (1.0/name2WithoutSeparations.length()) * 100; 
			
		}	 
		
		int differenceOfLength = biggerName.length() - smallerName.length();
		
		if(100 - differenceOfLength*impactFactor*changeInletterWeight < 0) { // Saves time and avoids unnecessary operations. Zero is the minimum possible result. 
			return 0; 
		}	
		
		//Starts using a modified version of the Minimum Edit Distance (Levenshtein Distance) algorithm to calculate the result.
		int totalLen1 = biggerName.length();
		int totalLen2 = smallerName.length();
		
		double dp[][] = new double[totalLen2+1][totalLen1+1]; 

		for (int i=0; i<=totalLen2; i++){ 
			
			double mindDistanceRow = Double.MAX_VALUE; // Keeps the minimum distance in each row.
			
			for (int j=0; j<=totalLen1; j++){
				
				char c1 = 0;
				char c2 = 0;
				char c1Plain = 0;
				char c2Plain = 0;
				
				if(i!=0 && j!=0) {
					
					c1 = biggerName.charAt(j-1);
					c2 = smallerName.charAt(i-1);
					c1Plain = removeDiacritics(c1);
					c2Plain = removeDiacritics(c2);
					
				}
				
				if (i==0) {
					
					dp[i][j] = j * impactFactor * changeInletterWeight;
	
				}else if (j==0) {
					
					dp[i][j] = i * impactFactor * changeInletterWeight;
					
				}else if (c1Plain == c2Plain) {
					
					dp[i][j] = dp[i-1][j-1];
					
					if(c1 != c2) { //Difference in diacritic.
						dp[i][j] += impactFactor * changeInDiacriticWeight;
					}
					
				}else { 
					
					if(isSeparator(c1) || isSeparator(c2)) { //Difference in separation.
						
						double weight = 0;
						
						if(c1 == '-' && c2 != ' ' || c1 != ' ' && c2 == '-' 
								|| c1 == ' ' && c2 == '-' || c1 == '-' && c2 == ' ') {
							
							weight = halfSeparationWeight;
							
						}else if(c1 == ' ' && c2 != '-' || c1 != '-' && c2 == ' ') {
							
							weight = fullSeparationWeight;
							
						}
						
						double insert = dp[i-1][j] + impactFactor * weight;
						double delete = dp[i][j-1] + impactFactor * weight;
						double replace = dp[i-1][j-1] + impactFactor * weight;
						
						dp[i][j] = min(insert, delete, replace);
						
					}else { //Difference in letter.

						double insert = dp[i-1][j] + impactFactor * changeInletterWeight;
						double delete = dp[i][j-1] + impactFactor * changeInletterWeight;
						double replace = dp[i-1][j-1] + impactFactor * changeInletterWeight;
						
						dp[i][j] = min(insert, delete, replace);
						
					}

				}
				
				if(dp[i][j] < mindDistanceRow) {
					
					mindDistanceRow = dp[i][j];
					
				}
				
			} 
			
			if(100 - mindDistanceRow <= 0) { // Saves time and avoids unnecessary operations. Zero is the minimum possible result. 		
				return 0;	
			}
			
		} 
		
		double distance = dp[totalLen2][totalLen1];
		
		if(100 - distance < 0) { // Zero is the minimum possible result. 
			return 0; 
		}
		
		double result = 100 - distance;
		result = round(result,2);	
		return result; 


	}
	
	
	public double compareNames(String name1, String name2, double threshold) throws NonPermittedSymbolException, ThresholdOutOfRangeException {
		
		//Avoids invalid thresholds.
		if(threshold < 0 || threshold > 100) {
			ThresholdOutOfRangeException e = new ThresholdOutOfRangeException();
			throw e;
		}
		
		//Avoids non-latin names and forbidden characters. 
		if(!name1.matches("^[\\p{IsLatin}|\\-| '|´|\\.|,|’|‘]*$") || !name2.matches("^[\\p{IsLatin}|\\-| '|´|\\.|,|’|‘]*$")) {
			NonPermittedSymbolException e = new NonPermittedSymbolException();
			throw e;
		}

		//Initial clean up of the names.
		name1 = cleanName(name1);
		name2 = cleanName(name2);
		
		String biggerName, smallerName;
		
		String name1WithoutSeparations = removeSeparators(name1);
		String name2WithoutSeparations = removeSeparators(name2);
		
		double normalizationFactor;
		
		if(name1WithoutSeparations.length() >= name2WithoutSeparations.length()) {
			
			biggerName = name1;
			smallerName = name2;
			normalizationFactor = (1.0/name1WithoutSeparations.length()) * 100; 
			
		}else {
			
			biggerName = name2;
			smallerName = name1;
			normalizationFactor = (1.0/name2WithoutSeparations.length()) * 100; 
			
		}	 
		
		int differenceOfLength = biggerName.length() - smallerName.length();
		
		if(100 - differenceOfLength*normalizationFactor*changeInletterWeight < threshold) { //If the length difference exceeds the threshold a zero is returned to save time and avoid unnecessary operations.
			return 0; 
		}	
		
		//Starts using a modified version of the Minimum Edit Distance (Levenshtein Distance) algorithm to calculate the result.
		int totalLen1 = biggerName.length();
		int totalLen2 = smallerName.length();
		
		double dp[][] = new double[totalLen2+1][totalLen1+1]; 

		for (int i=0; i<=totalLen2; i++){ 
			
			double mindDistanceRow = Double.MAX_VALUE; // Keeps the minimum distance in each row.
			
			for (int j=0; j<=totalLen1; j++){ 
				
				char c1 = 0;
				char c2 = 0;
				char c1Plain = 0;
				char c2Plain = 0;
				
				if(i!=0 && j!=0) {
					
					c1 = biggerName.charAt(j-1);
					c2 = smallerName.charAt(i-1);
					c1Plain = removeDiacritics(c1);
					c2Plain = removeDiacritics(c2);
					
				}
				
				if (i==0) {
					
					dp[i][j] = j * normalizationFactor * changeInletterWeight;
	
				}else if (j==0) {
					
					dp[i][j] = i * normalizationFactor * changeInletterWeight;
					
				}else if (c1Plain == c2Plain) {
					
					dp[i][j] = dp[i-1][j-1];
					
					if(c1 != c2) { //Difference in diacritic.
						dp[i][j] += normalizationFactor * changeInDiacriticWeight;
					}
					
				}else { 
					
					if(isSeparator(c1) || isSeparator(c2)) { //Difference in separation.
						
						double weight = 0;
						
						if(c1 == '-' && c2 != ' ' || c1 != ' ' && c2 == '-' 
								|| c1 == ' ' && c2 == '-' || c1 == '-' && c2 == ' ') {
							
							weight = halfSeparationWeight;
							
						}else if(c1 == ' ' && c2 != '-' || c1 != '-' && c2 == ' ') {
							
							weight = fullSeparationWeight;
							
						}
						
						double insert = dp[i-1][j] + normalizationFactor * weight;
						double delete = dp[i][j-1] + normalizationFactor * weight;
						double replace = dp[i-1][j-1] + normalizationFactor * weight;
						
						dp[i][j] = min(insert, delete, replace);
						
					}else { //Difference in letter.

						double insert = dp[i-1][j] + normalizationFactor * changeInletterWeight;
						double delete = dp[i][j-1] + normalizationFactor * changeInletterWeight;
						double replace = dp[i-1][j-1] + normalizationFactor * changeInletterWeight;
						
						dp[i][j] = min(insert, delete, replace);
						
					}

				}
				
				if(dp[i][j] < mindDistanceRow) {
					
					mindDistanceRow = dp[i][j];
					
				}
				
			} 
			
			if(100 - mindDistanceRow < threshold) { // Saves time and avoids unnecessary operations. If the threshold is exceeded, the method returns a zero. 			
				return 0;				
			}
			
		} 
		
		double distance = dp[totalLen2][totalLen1];
		
		if(100 - distance < threshold) { //If the threshold is exceeded, the method returns a zero. 
			return 0; 
		}
		
		double result = 100 - distance;
		result = round(result,2);	
		return result; 


	}
	
	
	private String cleanName(String name) {

		String cleanName = name;

		if(cleanName.contains(",")) {
			String[] splittedName = cleanName.split(",");
			cleanName = splittedName[1] + " " + splittedName[0];
		}

		cleanName = cleanName.replaceAll("\\n", ""); 
		cleanName = cleanName.replaceAll("\\s{2,}", " ");
		cleanName = cleanName.trim();
		int lengthBeforeConversion = cleanName.length();
		cleanName = cleanName.toLowerCase();
		int lengthAfterConversion = cleanName.length();
		
		if(lengthAfterConversion > lengthBeforeConversion) { //Sometimes errors occur when converting characters with diacritics to lower case.
			cleanName = fixToLowerCaseError(cleanName);
		}

		return cleanName;
	}
	
	private static char removeDiacritics(char c) {

		String s = String.valueOf(c);

		s = Normalizer.normalize(s, Normalizer.Form.NFD);
		s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

		return s.charAt(0);

	}
	
	private static String removeSeparators(String n) {

		n = n.replaceAll("\\s+","");
		n = n.replaceAll("-","");

		return n;

	}
	
	private double min(double insert,double delete,double replace) { 
        if (insert <= delete && insert <= replace) return insert; 
        if (delete <= insert && delete <= replace) return delete; 
        else return replace; 
    } 
	
	private boolean isSeparator(char c) {
		
		if(c == ' ' || c == '-') {
			return true;
		}else {
			return false;
		}
		
	}
	
	private double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	 private String fixToLowerCaseError(String s) {

		 for(int i = 0; i < s.length(); i++) {

			 char c = s.charAt(i);

			 if(Character.toString(c).matches("\\p{InCombiningDiacriticalMarks}")) {

				 StringBuilder sb = new StringBuilder(s);
				 sb.deleteCharAt(i);
				 s = sb.toString();
				 i--;
				 
			 }
			 
		 }

		 return s;
	 }
	
}
