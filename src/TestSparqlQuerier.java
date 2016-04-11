import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import main.java.application.SparqlQuerier;
import main.java.application.SparqlQueryConstructor;

public class TestSparqlQuerier {

	static SparqlQuerier sparqlQuerier;
	static OntModel model;
	static String pathToOntology = "res/EmergencyOntologyTest.owl";
	
	@BeforeClass
	public static void readOntology(){
		sparqlQuerier = new SparqlQuerier(pathToOntology);
		model = sparqlQuerier.getModel();
	}

	@Before
	public void setUp() throws Exception {
		
	}

	@Ignore
	@Test
	public void testReadOntology(){
		SparqlQuerier sq = new SparqlQuerier("res/EmergencyOntology.owl");
	}
	
	@Test
	public void testQuerySelect(){
		SparqlQueryConstructor qc = new SparqlQueryConstructor();
		String selectQuery = qc.getStatementsWithEventsOfType("Robbery");
		System.out.println(selectQuery);
		ResultSet rs = sparqlQuerier.querySelect(selectQuery);
	}
	
	@Test
	public void testQuerySubjectHasDay(){
		SparqlQueryConstructor qc = new SparqlQueryConstructor();
		String query = qc.getSubjectWithHasDay("");
		System.out.println(query);
		System.out.println("---");
		ResultSet rs = sparqlQuerier.querySelect(query);
		while(rs.hasNext()){
			QuerySolution qs = rs.next();
			System.out.println(qs);
		}
		
	}
	
	

}
