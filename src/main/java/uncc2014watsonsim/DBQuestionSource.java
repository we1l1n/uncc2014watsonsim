package uncc2014watsonsim;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DBQuestionSource extends ArrayList<Question> {
	private static final long serialVersionUID = 1L;
	private static final Database db = new Database();
	
	/** Run an arbitrary query on the database to get questions.
	 */
	public DBQuestionSource(String conditions) throws SQLException {
		// Get a list of questions, ordered so that it is consistent
		PreparedStatement query = db.prep("select distinct on (question) question, answer, category from questions "
				+ conditions + ";");
		read_results(query.executeQuery());
	}
	
	public void read_results(ResultSet sql) throws SQLException {
		while(sql.next()){
			Question q = Question.known(
					sql.getString("question"),
					sql.getString("answer"),
					sql.getString("category")
				);
			add(q);
		}
	}
}
