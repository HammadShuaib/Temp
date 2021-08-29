package com.fluidops.fedx.trunk.description;

import com.fluidops.fedx.trunk.parallel.engine.opt.CostModel;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

@Deprecated
public class AggregatedStatsReader {
	private PredicateRegistry pr = new PredicateRegistry();
	private Model m;

	public AggregatedStatsReader(String path) {
		m = FileManager.get().loadModel(path);
		process();
	}

	private void process() {
		/*String q = "PREFIX void:<http://rdfs.org/ns/void#>  " + "SELECT * WHERE { " + "?root void:triples ?triples. "
				+ "?root void:properties ?properties. " + "?root void:entities ?entities " + "}";
		Query query = QueryFactory.create(q);

		QueryExecution qe = QueryExecutionFactory.create(query, m);
		ResultSet results = qe.execSelect();
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			String triples = qs.get("triples").asLiteral().getString();
			CostModel.TotalTriples = Integer.getInteger(triples);
			String properties = qs.get("properties").asLiteral().getString();
			CostModel.TotalProperties = Integer.getInteger(properties);
			String entities = qs.get("entities").asLiteral().getString();
			CostModel.TotalEntities = Integer.getInteger(entities);
		}

		q = "PREFIX void:<http://rdfs.org/ns/void#>  " + "SELECT * WHERE { "
				+ "?property void:distinctObject ?objects. " + "?property void:distinctSubject ?subjects. "
				+ "?property void:triples ?triples" + "}";

		qe = QueryExecutionFactory.create(q, m);
		results = qe.execSelect();
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			String property = qs.get("property").asResource().getURI();
			PPartition cap = new PPartition(property);
			int subjects = qs.get("subjects").asLiteral().getInt();
			int objects = qs.get("objects").asLiteral().getInt();
			cap.setDistinctObject(objects);
			cap.setDistinctSubject(subjects);
			String qtr = "PREFIX void:<http://rdfs.org/ns/void#>  " + "SELECT * WHERE { " + "<" + property
					+ "> void:in ?service " + "}";
			QueryExecution qelocal = QueryExecutionFactory.create(qtr, m);
			ResultSet services = qelocal.execSelect();
			while (services.hasNext()) {
				QuerySolution service = services.next();
				String url = service.get("service").asResource().getURI();
				cap.addService(new RemoteService(url));
			}
			pr.add(cap);
		}*/
	}

}
