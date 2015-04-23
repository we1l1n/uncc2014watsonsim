package edu.uncc.cs.watsonsim.scorers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uncc.cs.watsonsim.Answer;
import edu.uncc.cs.watsonsim.Question;

/**
 * Check if: the question needs a date, and the answer is one 
 * @author Sean
 */
public class DateMatches extends AnswerScorer {
	private static boolean maybeMonth(String in) {
		return in.matches("[Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec]\\.?\\w*"
				+ "|\\d{1,2}");
	}
	
	private static boolean maybeYear(String in) {
		return in.matches("\\d{2}|\\d{4}");
	}
	
	private static boolean maybeDay(String in) {
		return in.matches("\\d{1,2}");
	}
	
	private static boolean maybeDate(String in) {
		boolean years=false, months=false, days=false;
		Matcher m = Pattern.compile("([^- ]+)[- ]+([^- ]+)[- ]+([^- ]+)?").matcher(in);
		if (m.find()) {
			for (int group=0; group<m.groupCount(); group++) {
				years |= maybeYear(m.group(group));
				months |= maybeMonth(m.group(group));
				days |= maybeDay(m.group(group));
			}
		}
		return (months && days)  // year optional
				|| (years && months); // day optional
	}

	@Override
	public double scoreAnswer(Question q, Answer a) {
		
		switch (q.simple_lat.toLowerCase()) {
		case "year": return maybeYear(a.text)? 1 : 0;
		case "date":
		case "day": return maybeDate(a.text)? 1 : 0;
		}
		
		
		return 0;
	}
	
}