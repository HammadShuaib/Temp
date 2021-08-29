
package com.fluidops.fedx.trunk.description;

import java.io.File;

import com.fluidops.fedx.trunk.parallel.engine.opt.CostModel;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class Statistics {

	private Dataset dataset = null;
	private String uri = null;

	private ServiceRegistry serviceRegistry = new ServiceRegistry();

	public Statistics(File confDir) {
/*		File dir = confDir;
		File[] confs;
		if (dir.isDirectory())
			confs = dir.listFiles();
		else {
			confs = new File[1];
			confs[0] = confDir;
		}

		Model model = ModelFactory.createDefaultModel();
		if (confs.length > 0) {
			for (int i = 0; i < confs.length; i++) {
			//	System.out.println("Loading " + confs[i]);
				model.add(FileManager.get().loadModel(confs[i].toString()));
			}
			dataset = DatasetFactory.create(model);
		}
		findServices();
		dataset.close();
		model = null;
	*/}

	public Statistics(String uri) {
		// dataset = DatasetFactory.create(url);
	//	this.uri = uri;
	//	findServices();
		// dataset.close();
	}

	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	private void findServices() {
//		System.out.println("Processing service descriptions...");
/*
		String q = "PREFIX void:<http://rdfs.org/ns/void#> " + "SELECT * " + "{" + "  ?dataset  a void:Dataset. "
				+ "  ?dataset void:sparqlEndpoint ?url. " + "OPTIONAL {?dataset void:triples ?triples. "
				+ "?dataset void:entities ?entities. " + "?dataset void:properties ?properties. "
				+ "?dataset void:distinctSubjects ?subs. " + "?dataset void:distinctObjects ?objs}" + "}";

		try {
			Query query = QueryFactory.create(q);
			QueryExecution qexec = null;
			if (dataset != null)
				qexec = QueryExecutionFactory.create(query, dataset);
			if (uri != null)
				qexec = QueryExecutionFactory.sparqlService(uri, query);

			for (ResultSet rs = qexec.execSelect(); rs.hasNext();) {
				QuerySolution qs = rs.nextSolution();
				String url;
				RDFNode urlNode = qs.getResource("url");
				if (urlNode != null) {
					url = urlNode.toString();
				} else {
					throw new Exception("Error: Services must have an URL!");
				}

				int tripleCount = 0;
				RDFNode triplesNode = qs.getLiteral("triples");
				if (triplesNode != null && triplesNode.isLiteral()) {
					tripleCount = ((Literal) triplesNode).getInt();
				}

				int properties = 0;
				RDFNode propertiesNode = qs.getLiteral("properties");
				if (propertiesNode != null && propertiesNode.isLiteral()) {
					properties = ((Literal) propertiesNode).getInt();
				}

				int subs = 0;
				RDFNode subsNode = qs.getLiteral("subs");
				if (subsNode != null && subsNode.isLiteral()) {
					subs = ((Literal) subsNode).getInt();
				}

				int objs = 0;
				RDFNode objsNode = qs.getLiteral("objs");
				if (objsNode != null && objsNode.isLiteral()) {
					objs = ((Literal) objsNode).getInt();
				}

				CostModel.TotalTriples += tripleCount;
				CostModel.TotalProperties += properties;
				CostModel.TotalSubjects += subs;
				CostModel.TotalObjects += objs;

				RemoteService s = new RemoteService(url);
				s.setTripleCount(tripleCount);
				s.setTotalProperties(properties);
				s.setTotalObj(objs);
				s.setTotalSub(subs);

				findCapability(s);

				serviceRegistry.add(s);

			}
		} catch (Exception e) {
		//	System.err.println(e);
		}

		if (serviceRegistry.getAvailableServices().size() == 0) {
		}
*/
	//	System.out.println("Service descriptions loaded");
	}

	private void findCapability(RemoteService s) {
/*
	//	System.out.println("Building descriptions for " + s);

		String q = "PREFIX void:<http://rdfs.org/ns/void#> " + "SELECT * WHERE { "
				+ "?dataset void:propertyPartition ?partition. " + "?dataset void:sparqlEndpoint " + "<" + s.getUrl()
				+ ">. " + "?partition void:property ?property. " + "?partition void:triples ?triples. "
				+ "OPTIONAL {?partition  void:distinctSubjects ?subCount. "
				+ "?partition void:distinctObjects ?objCount.}" + "}";

		try {

			Query query = QueryFactory.create(q);
			// log.debug(query);

			QueryExecution qexec = null;
			if (dataset != null)
				qexec = QueryExecutionFactory.create(query, dataset);
			if (uri != null)
				qexec = QueryExecutionFactory.sparqlService(uri, query);
			int capabilitycount = 0;
			int counter = 0;
			for (ResultSet rs = qexec.execSelect(); rs.hasNext();) {
				QuerySolution qs = rs.nextSolution();
				counter++;
		//		if (counter % 100000 == 0)
		//			System.out.println(counter);
				String predicate;
				Resource predicateRes = qs.getResource("property");
				if (predicateRes != null) {
					predicate = predicateRes.getURI();
				} else {
					throw new Exception("Capability must include a predicate!");
				}

				PPartition cap = new PPartition(predicate);

				RDFNode pCount;
				int count = 0;
				pCount = qs.get("triples");
				if (pCount != null && pCount.isLiteral()) {
					try {
						count = ((Literal) pCount).getInt();
					} catch (Exception e) {
						throw new Exception("Could not read triples. (No number?)");
					}
				}

				cap.setTriples(count);

				RDFNode sCount;
				int scount = 0;
				sCount = qs.get("subCount");
				if (sCount != null && sCount.isLiteral()) {
					try {
						scount = ((Literal) sCount).getInt();
					} catch (Exception e) {
						throw new Exception("Could not read triples. (No number?)");
					}
				}

				CostModel.TotalEntities += scount;

				cap.setDistinctSubject(scount);

				RDFNode oCount;
				int ocount = 0;
				oCount = qs.get("objCount");
				if (oCount != null && oCount.isLiteral()) {
					try {
						ocount = ((Literal) oCount).getInt();
					} catch (Exception e) {
			//			throw new Exception("Could not read triples. (No number?)");
					}
				}

				CostModel.TotalEntities += ocount;
				cap.setDistinctObject(ocount);

				s.addCapability(cap);
				capabilitycount++;

			}
			if (capabilitycount == 0) {
				// log.warn("No capabilities found for service with url: "
				// + s.getUrl());
			}
		} catch (Exception e) {
			// log.error("Exception in findCapability(): " + e);
		}
*/
	}
}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */