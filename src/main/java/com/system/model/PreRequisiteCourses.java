package com.system.model;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;



@RelationshipEntity
public class PreRequisiteCourses {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@StartNode
	private Course course1;

	@EndNode
	private Course course2;
	
	private int weight;
	

	public PreRequisiteCourses(Course course1, Course course2, int weight) {
		super();
		this.course1 = course1;
		this.course2 = course2;
		this.weight = weight;
	}

	public Course getCourse1() {
		return course1;
	}

	public void setCourse1(Course course1) {
		this.course1 = course1;
	}

	public Course getCourse2() {
		return course2;
	}

	public void setCourse2(Course course2) {
		this.course2 = course2;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}


	
	

}
