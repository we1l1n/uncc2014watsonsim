package uncc2014watsonsim.sources;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import uncc2014watsonsim.Question;
import uncc2014watsonsim.DBQuestionSource;
import uncc2014watsonsim.Answer;
import uncc2014watsonsim.search.IndriSearcher;
import uncc2014watsonsim.search.LuceneSearcher;
import uncc2014watsonsim.search.GoogleSearcher;
import uncc2014watsonsim.search.Searcher;

/**
 *
 * @author Phani Rahul
 */
public class GenerateSearchResultDataset {

    /**
     * @param args the command line arguments
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        DBQuestionSource dbquestions = new DBQuestionSource();
        dbquestions.fetch_without_results(3162,50);
    	for (Question q : dbquestions) {
    		pool.execute(new SingleTrainingResult(q));
    		//new SingleTrainingResult(q).run();
    	}
        pool.shutdown();
        try {
            pool.awaitTermination(2, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            Logger.getLogger(GenerateSearchResultDataset.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Finished.");
    }
}

class SingleTrainingResult extends Thread {
	Question q;
	static Searcher[] searchers = {
		new LuceneSearcher(),
		new IndriSearcher(),
		new GoogleSearcher()
	};
	
	public SingleTrainingResult(Question q) {
		this.q = q;
	}
	
	public void run() {
		try {
			List<Answer> uncollated_results = new ArrayList<Answer>(); 
			for (Searcher searcher : searchers) {
				uncollated_results.addAll(searcher.runQuery(q.text));
			}
			DBQuestionSource.replace_cache(q, uncollated_results);
			// Let the user know things are moving along.
			System.out.print(".");
			// Somewhere around once in 80 times..
			if (q.id % 80 == 0) System.out.println(); 
		} catch (Exception e) {
			// Eat exceptions
			e.printStackTrace();
		}
	}
}
