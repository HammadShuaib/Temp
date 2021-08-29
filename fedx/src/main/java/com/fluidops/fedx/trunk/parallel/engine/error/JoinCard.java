package com.fluidops.fedx.trunk.parallel.engine.error;

import com.fluidops.fedx.trunk.graph.Vertex;
import com.fluidops.fedx.trunk.stream.engine.util.HypTriple;

import java.util.HashMap;

public class JoinCard {

	// formatting the result according to required format
	private Vertex vertex;
	private int join_number;
	private Double estimated_Card;
	private Double real_Card;

	public Vertex getVertex() {
		return vertex;
	}

	public void setVertex(Vertex vertex) {
		this.vertex = vertex;
	}

	public int getJoin_number() {
		return join_number;
	}

	public void setJoin_number(int join_number) {
		this.join_number = join_number;
	}

	public Double getEstimated_Card() {
		return estimated_Card;
	}

	public void setEstimated_Card(Double estimated_Card) {
		this.estimated_Card = estimated_Card;
	}

	public Double getReal_Card() {
		return real_Card;
	}

	public void setReal_Card(Double real_Card) {
	///	this.real_Card = real_Card;
	}

	private HashMap<HypTriple, TripleCard> tripleCards = new HashMap<>();

	public void addTripleWithCard(HypTriple triple, TripleCard card) {
	//	tripleCards.put(triple, card);
	}

	public HashMap<HypTriple, TripleCard> getTripleCards() {
		return tripleCards;
	}
}
