package main.java.application;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Set;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;

import uk.ac.manchester.cs.jfact.JFactFactory;
import uk.ac.manchester.cs.jfact.JFactReasoner;
import uk.ac.manchester.cs.jfact.kernel.options.JFactReasonerConfiguration;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

public class Query {

	public void init(OWLOntology ontology){
		// Load an example ontology.
	    
		//OWLOntology ontology = manager
	    //        .loadOntologyFromOntologyDocument(new StringDocumentSource(ontology));
	    // We need a reasoner to do our query answering
	
	
	    // These two lines are the only relevant difference between this code and the original example
	    // This example uses HermiT: http://hermit-reasoner.com/
		//JFactFactory factory = new JFactFactory();
		StructuralReasonerFactory factory = new StructuralReasonerFactory();
		OWLReasoner reasoner = factory.createReasoner(ontology);
	    //OWLReasoner reasoner = new Reasoner.ReasonerFactory().createReasoner(ontology);
	
	    
	
	    ShortFormProvider shortFormProvider = new SimpleShortFormProvider();
	    // Create the DLQueryPrinter helper class. This will manage the
	    // parsing of input and printing of results
	    DLQueryPrinter dlQueryPrinter = new DLQueryPrinter(new DLQueryEngine(reasoner,
	            shortFormProvider), shortFormProvider);
	    // Enter the query loop. A user is expected to enter class
	    // expression on the command line.
	    try{
		    BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
		    while (true) {
		        System.out
		                .println("Type a class expression in Manchester Syntax and press Enter (or press x to exit):");
		        String classExpression = br.readLine();
		        // Check for exit condition
		        if (classExpression == null || classExpression.equalsIgnoreCase("x")) {
		            break;
		        }
		        dlQueryPrinter.askQuery(classExpression.trim());
		        System.out.println();
		    }
	    }
	    catch(Exception e){
	    	e.printStackTrace();
	    }
	}
	
	

	class DLQueryEngine {
		private final OWLReasoner reasoner;
		private final DLQueryParser parser;

		public DLQueryEngine(OWLReasoner reasoner, ShortFormProvider shortFormProvider) {
			this.reasoner = reasoner;
			parser = new DLQueryParser(reasoner.getRootOntology(), shortFormProvider);
		}

		public Set<OWLClass> getSuperClasses(String classExpressionString, boolean direct) {
			if (classExpressionString.trim().length() == 0) {
				return Collections.emptySet();
			}
			OWLClassExpression classExpression = parser
					.parseClassExpression(classExpressionString);
			NodeSet<OWLClass> superClasses = reasoner
					.getSuperClasses(classExpression, direct);
			return superClasses.getFlattened();
		}

		public Set<OWLClass> getEquivalentClasses(String classExpressionString) {
			if (classExpressionString.trim().length() == 0) {
				return Collections.emptySet();
			}
			OWLClassExpression classExpression = parser
					.parseClassExpression(classExpressionString);
			Node<OWLClass> equivalentClasses = reasoner.getEquivalentClasses(classExpression);
			Set<OWLClass> result = null;
			if (classExpression.isAnonymous()) {
				result = equivalentClasses.getEntities();
			} else {
				result = equivalentClasses.getEntitiesMinus(classExpression.asOWLClass());
			}
			return result;
		}

		public Set<OWLClass> getSubClasses(String classExpressionString, boolean direct) {
			if (classExpressionString.trim().length() == 0) {
				return Collections.emptySet();
			}
			OWLClassExpression classExpression = parser
					.parseClassExpression(classExpressionString);
			NodeSet<OWLClass> subClasses = reasoner.getSubClasses(classExpression, direct);
			return subClasses.getFlattened();
		}

		public Set<OWLNamedIndividual> getInstances(String classExpressionString,
				boolean direct) {
			if (classExpressionString.trim().length() == 0) {
				return Collections.emptySet();
			}
			OWLClassExpression classExpression = parser
					.parseClassExpression(classExpressionString);
			NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(classExpression,
					direct);
			return individuals.getFlattened();
		}
	}

	class DLQueryParser {
		private final OWLOntology rootOntology;
		private final BidirectionalShortFormProvider bidiShortFormProvider;

		public DLQueryParser(OWLOntology rootOntology, ShortFormProvider shortFormProvider) {
			this.rootOntology = rootOntology;
			OWLOntologyManager manager = rootOntology.getOWLOntologyManager();
			Set<OWLOntology> importsClosure = rootOntology.getImportsClosure();
			// Create a bidirectional short form provider to do the actual mapping.
			// It will generate names using the input
			// short form provider.
			bidiShortFormProvider = new BidirectionalShortFormProviderAdapter(manager,
					importsClosure, shortFormProvider);
		}

		public OWLClassExpression parseClassExpression(String classExpressionString) {
			OWLDataFactory dataFactory = rootOntology.getOWLOntologyManager()
					.getOWLDataFactory();
			ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(
					dataFactory, classExpressionString);
			parser.setDefaultOntology(rootOntology);
			OWLEntityChecker entityChecker = new ShortFormEntityChecker(bidiShortFormProvider);
			parser.setOWLEntityChecker(entityChecker);
			try {
				return parser.parseClassExpression();
			} catch (ParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
	}

	class DLQueryPrinter {
		private final DLQueryEngine dlQueryEngine;
		private final ShortFormProvider shortFormProvider;

		public DLQueryPrinter(DLQueryEngine engine, ShortFormProvider shortFormProvider) {
			this.shortFormProvider = shortFormProvider;
			dlQueryEngine = engine;
		}

		public void askQuery(String classExpression) {
			if (classExpression.length() == 0) {
				System.out.println("No class expression specified");
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("\\nQUERY:   ").append(classExpression).append("\\n\\n");
				Set<OWLClass> superClasses = dlQueryEngine.getSuperClasses(
						classExpression, false);
				printEntities("SuperClasses", superClasses, sb);
				Set<OWLClass> equivalentClasses = dlQueryEngine
						.getEquivalentClasses(classExpression);
				printEntities("EquivalentClasses", equivalentClasses, sb);
				Set<OWLClass> subClasses = dlQueryEngine.getSubClasses(classExpression,
						true);
				printEntities("SubClasses", subClasses, sb);
				Set<OWLNamedIndividual> individuals = dlQueryEngine.getInstances(
						classExpression, true);
				printEntities("Instances", individuals, sb);
				System.out.println(sb.toString());
			}
		}

		private void printEntities(String name, Set<? extends OWLEntity> entities,
				StringBuilder sb) {
			sb.append(name);
			int length = 50 - name.length();
			for (int i = 0; i < length; i++) {
				sb.append(".");
			}
			sb.append("\\n\\n");
			if (!entities.isEmpty()) {
				for (OWLEntity entity : entities) {
					sb.append("\\t").append(shortFormProvider.getShortForm(entity))
					.append("\\n");
				}
			} else {
				sb.append("\\t[NONE]\\n");
			}
			sb.append("\\n");
		}
	}
}
