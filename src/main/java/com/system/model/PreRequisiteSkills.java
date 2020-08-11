package com.system.model;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;



@RelationshipEntity
public class PreRequisiteSkills {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@StartNode
	private Skill skill1;

	@EndNode
	private Course course1;

	public PreRequisiteSkills(Skill skill1, Course course1) {
		super();
		this.skill1 = skill1;
		this.course1 = course1;
	}

	public Skill getSkill1() {
		return skill1;
	}

	public void setSkill1(Skill skill1) {
		this.skill1 = skill1;
	}

	public Course getCourse1() {
		return course1;
	}

	public void setCourse1(Course course1) {
		this.course1 = course1;
	}
	
	
	

}
