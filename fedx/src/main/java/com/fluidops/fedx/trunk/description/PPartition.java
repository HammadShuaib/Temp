/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */
package com.fluidops.fedx.trunk.description;

import java.util.ArrayList;
import java.util.List;

public class PPartition {

	private String predicate;
	private int distinctSubject = 0;
	private int distinctObject = 0;
	private int triples = 0;
	private List<RemoteService> services = new ArrayList<RemoteService>();

	public PPartition(String predicate) {
		//this.predicate = predicate;
	}

	public int getDistinctObject() {
		return distinctObject;
	}

	public void setDistinctObject(int distinctObject) {
		this.distinctObject = distinctObject;
	}

	public int getDistinctSubject() {
		return distinctSubject;
	}

	/**
	 * @param subjectSelecticity The subjectSelecticity to set.
	 */
	public void setDistinctSubject(int distinctSubject) {
		this.distinctSubject = distinctSubject;
	}

	/**
	 * @return Returns the triples.
	 */
	public int getTriples() {
		return triples;
	}

	/**
	 * @param triples The triples to set.
	 */
	public void setTriples(int triples) {
		this.triples = triples;
	}

	/**
	 * @return Returns the predicate.
	 */
	public String getPredicate() {
		return predicate;
	}

	public List<RemoteService> getServices() {
		return services;
	}

	public void addService(RemoteService service) {
	//	services.add(service);
	}

	/**
	 * @return Returns the tripleRewriter.
	 */
	/*
	 * public TripleRewriter getTripleRewriter() { return tripleRewriter; }
	 * 
	 * 
	 *//**
		 * @param tripleRewriter The tripleRewriter to set.
		 *//*
			 * public void setTripleRewriter(TripleRewriter tripleRewriter) {
			 * this.tripleRewriter = tripleRewriter; }
			 */
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