package com.system.model;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity
public class SimilarSkill {

	@Id
	@GeneratedValue
	private Long id;
	
	@StartNode
	private Skill skill1;

	@EndNode
	private Skill skill2;
	
	private double weight;

	public SimilarSkill( Skill skill1, Skill skill2, double weight2) {
		super();
		this.skill1 = skill1;
		this.skill2 = skill2;
		this.weight = weight2;
	}

	public Skill getSkill1() {
		return skill1;
	}

	public void setSkill1(Skill skill1) {
		this.skill1 = skill1;
	}

	public Skill getSkill2() {
		return skill2;
	}

	public void setSkill2(Skill skill2) {
		this.skill2 = skill2;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	
	
	

	
	
}
