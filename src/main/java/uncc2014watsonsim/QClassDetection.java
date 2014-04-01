package uncc2014watsonsim;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uncc2014watsonism.qAnalysis.BlankAnnotation;
import uncc2014watsonism.qAnalysis.FITBAnnotations;

/**
 * Methods related to determining and returning the QType (or QClass) of the question
 * @author Ken Overholt
 *
 */
public class QClassDetection {

	/**
	 * Determines the QType (or QClass) of the question
	 * @param q Question object with the raw_text and category values set
	 * @return a QType enum representing the QType (or QClass)
	 */
	public static QType detectType(Question q) {
		if (isFITB(q)) {
			return QType.FITB;
		}
		else if (isCommonBonds(q.raw_text,q.getCategory())) {
			return QType.COMMON_BONDS;
		}
		else if (isAnagram(q.raw_text,q.getCategory())) {
			return QType.ANAGRAM;
		}
		else if (isBeforeAndAfter(q.raw_text,q.getCategory())) {
			return QType.BEFORE_AND_AFTER;
		}
		else if (isQuotation(q.raw_text,q.getCategory())) {
			return QType.QUOTATION;
		}
		else {
			return QType.FACTOID;
		}
	}
	
	/**
	 * Returns true if the QType if FITB, i.e., if there are blanks in the question
	 * If returning true, sets the FITB annotations in the Question object
	 * 
	 * @param question the Question object we are attempting to classify
	 * @return true/false
	 */
	private static boolean isFITB(Question question) {
		//TODO: Another, more common, indication of FITB is quoted phrases adjacent to the focus.
		// Use focus detection (and LAT detection) to identify these.
		
		FITBAnnotations annot = question.getFITBAnnotations(); //temporary holder to shorten calls to this
		boolean result = false;
		//find blanks and set their locations in the annotation (if they exist)
		Pattern pattern = Pattern.compile("_+");
		Matcher matcher = pattern.matcher(question.raw_text);
		while (matcher.find()) {
			annot.getBlanks().add( new BlankAnnotation(matcher.start(),matcher.end()) );
			//System.out.print(" blank: " + question.raw_text.subSequence(matcher.start(), matcher.end())); //debug
			result = true; //return will be true as a blank was found
		}
		
		//if blanks were found, find and set locations of sections 1 & 2 in the annotations (if they exist)
		if (result == true) {
			pattern = Pattern.compile("\"");
			matcher = pattern.matcher(question.raw_text);
			int firstBlankBeginning = annot.getBlanks().get(0).getBegin();
			annot.setSection1End(firstBlankBeginning);
			
			annot.setSection1Begin(0);
			while (matcher.find() && matcher.start()< firstBlankBeginning) {
				annot.setSection1Begin(matcher.start());
			}

			annot.setSection2Begin(annot.getBlanks().get(annot.getBlanks().size()-1).getEnd());
			matcher.region(annot.getSection2Begin(), question.raw_text.length());
			if (matcher.find()) {
				annot.setSection2End(matcher.end());
			}
			else {
				annot.setSection2End(question.raw_text.length());
			}
			
			//System.out.println("current question: " + question.getRaw_text()); //for debug
			//System.out.println(annot); //for debug
		}
		
		return result;
	}

	/**
	 * tester
	 * @param str
	 */
	public static void main(String... str) {
		//test data
		String text1 = "my test ___ phrase\"";
		String text2 = "Some other test text 78; second line.";
		String text3 = " Yet _ more__ text. W___ith further ___ in dd z\"it.";
		String testText = text3;
		String count = "01234567890123456789012345678901234567890";
		System.out.println(testText);
		System.out.println(count);
		
		//blank finder
		int blankStart = -1; int blankEnd = -1;
		Pattern pattern = Pattern.compile("_+", java.util.regex.Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(testText);
		while (matcher.find()) {
			System.out.println("blank found: " + matcher.start() + " to " + matcher.end() + ": " +testText.substring(matcher.start(), matcher.end()));
			if (blankStart == -1)  blankStart = matcher.start();
			blankEnd = matcher.end();
		};
		
		//section finders
		pattern = Pattern.compile("\"");
		matcher = pattern.matcher(testText);
		int firstBlankBeginning = blankStart;
		int sectionOneBegin = 0;
		while (matcher.find() && matcher.start()< firstBlankBeginning) {
			sectionOneBegin = matcher.start();
		}
		System.out.println("section 1: " + testText.substring(sectionOneBegin, firstBlankBeginning));
		
		matcher.region(blankEnd, testText.length());
		matcher.find();
		System.out.println("section 2: " + testText.substring(blankEnd,matcher.end()));
		
		//System.out.println("my test ___ phrase".matches(".*_.*"));
		
		System.out.println(QClassDetection.isQuotation("He not only wrote & directed \"Little Johnny Jones\", he also played the title role", "MUSICALS")); 
	}
	
	/**
	 * Returns true if the QType is COMMON BONDS, i.e., if "COMMON BONDS" is
	 * in the category
	 * 
	 * @param clue
	 * @param category
	 * @return
	 */
	private static boolean isCommonBonds(String clue, String category) {
		return category.toUpperCase().matches(".*COMMON BONDS.*");
	}
	
	/**
	 * Returns true if the QType if BEFORE & AFTER, i.e., if "BEFORE & AFTER" is
	 * in the category
	 * 
	 * @param clue
	 * @param category
	 * @return
	 */
	private static boolean isBeforeAndAfter(String clue, String category) {
		return category.toUpperCase().matches(".*BEFORE & AFTER.*");
	}
	
	/**
	 * Returns true if the QType is Anagram, i.e., if the category includes
	 * "ANAGRAM", "SCRAMBLED", or "JUMBLED"
	 * 
	 * @param clue
	 * @param category
	 * @return
	 */
	private static boolean isAnagram(String clue, String category) {
		return category.toUpperCase().matches(".*ANAGRAM.*") ||
				category.toUpperCase().matches(".*SCRAMBLED.*") ||
				category.toUpperCase().matches(".*JUMBLED.*");
	}

	/**
	 * Returns true if the QType is Quotation.  Need identify the requirements
	 * and implement this.
	 * 
	 * @param clue
	 * @param category
	 * @return
	 */
	private static boolean isQuotation(String clue, String category) {
		return clue.matches(".*\"(([^\"\\r\\n\\s]+)\\b[:;?!,.]?(\\s)*){3,}\".*");
	}

}
