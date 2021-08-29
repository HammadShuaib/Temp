package com.fluidops.fedx.trunk.description;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import com.fluidops.fedx.trunk.parallel.engine.opt.CostModel;

@Deprecated
public class StatsAggregate {

	// services per predicate
	static Map<String, List<RemoteService>> predct_service = new HashMap<String, List<RemoteService>>();
	// the total number of occurrence per predicate
	static Map<String, Integer> predct_occurrence = new HashMap<String, Integer>();
	// the total number of distinct subjects per predicate
	static Map<String, Integer> predct_sub = new HashMap<String, Integer>();
	// the total number of distinct subjects per predicate
	static Map<String, Integer> predct_obj = new HashMap<String, Integer>();

	public static void main(String[] args) {

/*		Statistics config = new Statistics("http://152.78.65.127:8080/openrdf-sesame/repositories/voidstore");
		ServiceRegistry sr = config.getServiceRegistry();
		List<RemoteService> services = sr.getAvailableServices();
		for (RemoteService rs : services) {
			processService(rs);
		}

		Model m = ModelFactory.createDefaultModel();
		String ns = "http://rdfs.org/ns/void#";
		m.setNsPrefix("void", ns);
		Resource root = m.createResource();
		Property triplenum = m.createProperty(ns, "triples");
		m.add(root, triplenum, String.valueOf(CostModel.TotalTriples));
		m.add(root, m.createProperty(ns, "properties"), String.valueOf(predct_occurrence.size()));
		m.add(root, m.createProperty(ns, "entities"), String.valueOf(CostModel.TotalEntities));

		Property in = m.createProperty(ns, "in");
		Property disSub = m.createProperty(ns, "distinctSubjects");
		Property disObj = m.createProperty(ns, "distinctObjects");
		Set<String> predicates = predct_occurrence.keySet();
		for (String pre : predicates) {
			Resource prdct = m.createResource(pre);
			List<RemoteService> srvs = predct_service.get(pre);
			for (RemoteService rs : srvs) {
				m.add(prdct, in, m.createResource(rs.getUrl()));
			}
			m.add(prdct, triplenum, predct_occurrence.get(pre).toString());
			m.add(prdct, disSub, predct_sub.get(pre).toString());
			m.add(prdct, disObj, predct_occurrence.get(pre).toString());
		}

		RDFWriter writer = m.getWriter("TURTLE");

		try {
			FileOutputStream os = new FileOutputStream("aggre.ttl");
			writer.write(m, os, null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
*/
	}

	private static void processService(RemoteService sr) {
		List<PPartition> caps = new ArrayList<PPartition>();
		caps.addAll(sr.capabilities.values());
		for (PPartition cap : caps) {
			String pre = cap.getPredicate();
			// initialise
			if (predct_service.get(pre) == null) {
				predct_service.put(pre, new ArrayList<RemoteService>());
				predct_occurrence.put(pre, 0);
				predct_sub.put(pre, 0);
				predct_obj.put(pre, 0);
			}
			// aggregate
			predct_service.get(pre).add(sr);
			int triples = predct_occurrence.get(pre);
			triples += cap.getTriples();
			predct_occurrence.put(pre, triples);
			int subjects = predct_sub.get(pre);
			subjects += cap.getDistinctSubject();
			predct_sub.put(pre, subjects);
			int objects = predct_obj.get(pre);
			objects += cap.getDistinctObject();
			predct_obj.put(pre, objects);
		}
	}

}
