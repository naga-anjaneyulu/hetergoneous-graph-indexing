package com.system.model;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;



@NodeEntity
public class Skill {
	

	@Id
	@GeneratedValue private Long id;
	private String name;

	private Skill() {
		
	};

	public Skill(String name) {
		this.name = name;
	}
	

	@Relationship(type = "preReq", direction = Relationship.INCOMING)
	public Set<PreRequisites> preRequisites;
	
	@Relationship(type = "simiSkill", direction = Relationship.OUTGOING)
	public Set<SimilarSkill> simiSkills;
	
	public void requires(PreRequisites pre) { 
		 if (preRequisites == null) {
			 preRequisites = new HashSet<>(); }
		 preRequisites.add(pre);
	 }
	
	 
	public void similarTo(SimilarSkill simSkill) { 
		 if (simiSkills == null) {
			 simiSkills = new HashSet<>(); }
		 
		 simiSkills.add(simSkill);
	 }

	 
	public Set<SimilarSkill> getSimiSkills() {
			return simiSkills;
		}

    public void setSimiSkills(Set<SimilarSkill> simiSkills) {
			this.simiSkills = simiSkills;
		} 
	 
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
  


}
