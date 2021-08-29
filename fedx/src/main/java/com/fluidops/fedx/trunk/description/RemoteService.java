/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */
package com.fluidops.fedx.trunk.description;

import java.util.HashMap;
import java.util.Map;
import com.hp.hpl.jena.graph.Triple;

public class RemoteService {

	private String url;
	private int totalObj;
	private int totalSub;
	private int totalTriple;
	private int totalProperties;
	Map<String, PPartition> capabilities = new HashMap<String, PPartition>();

	public RemoteService(String url) {
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$This is url in Remote Services: " + url);

		this.url = url;
	}

	@Override
	public String toString() {
		return url;
	}

	public boolean hasCapability(Triple t) {
//		if (!t.getPredicate().isURI())
//			return false;
//		PPartition c = findCapability(t.getPredicate().getURI());
//		return c != null;
return true;
	}

	public int getDistinctObject(Triple t) {
	/*	if (t.getPredicate().isVariable()) {
			return totalObj;
		}
		PPartition c = findCapability(t.getPredicate().getURI());
		if (c != null) {
			return c.getDistinctObject();

		}*/
		return 0;
	}

	public int getDistinctSubject(Triple t) {
	/*	if (t.getPredicate().isVariable()) {
			return totalSub;
		}
		PPartition c = findCapability(t.getPredicate().getURI());
		if (c != null) {

			return c.getDistinctSubject();
		}*/
		return 0;
	}

	/**
	 * @return Returns the total number of triples .
	 */
	public int getTripleCount() {
		return totalTriple;

	}

	/**
	 * @param tripleCount the total number of triples of this service.
	 */
	public void setTripleCount(int tripleCount) {
		this.totalTriple = tripleCount;
	}

	public void addCapability(PPartition c) {
	//	capabilities.put(c.getPredicate(), c);
		// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$This is
		// predicate in Remote Services: "+c.getPredicate());

	}

	/**
	 * 
	 * @param t
	 * @return the number of triples with the same predicate as in t.
	 */
	public int getTriples(Triple t) {
		// if predicate is a variable, return the total number of triples of this
		// service
		/*if (t.getPredicate().isVariable()) {
			return totalTriple;
		}
		PPartition c = findCapability(t.getPredicate().getURI());
		if (c != null) {
			return c.getTriples();
		}*/
		return 0;

	}

	public String getUrl() {
		return url;
	}

	private PPartition findCapability(String predicate) {
		PPartition c=null;
		//PPartition c = capabilities.get(predicate);
		return c;
	}

	public void setTotalSub(int sub) {
		this.totalSub = sub;
	}

	public void setTotalObj(int obj) {
		this.totalObj = obj;
	}

	public int getTotalProperties() {
		return totalProperties;
	}

	public void setTotalProperties(int totalProperties) {
		this.totalProperties = totalProperties;
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