package com.system.model;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class Course {
	
	
	@Id
	@GeneratedValue private Long id;
	
	private String courseId;
	
	private String name;

	private Course() {
		
	};

	public Course(String courseId,String name) {
		this.courseId = courseId;
		this.name = name;
	}

	@Relationship(type = "preCourse", direction = Relationship.INCOMING)
	public Set<PreRequisiteCourses> preRequisites;

	
	@Relationship(type = "gainSkill", direction = Relationship.OUTGOING)
	public Set<SkillGained> gainSkills;

	
	 public void requiresCourse(PreRequisiteCourses pre) { 
		 if (preRequisites == null) {
			 preRequisites = new HashSet<>(); }
		 preRequisites.add(pre);
	 }
	 
	
	 public void teachesSkill(SkillGained lskills) { 
		 if (gainSkills == null) {
			 gainSkills = new HashSet<>(); }
		 gainSkills.add(lskills);
	 }

	 
	public String getCourseId() {
			return courseId;
		}

	public void setCourseId(String courseId) {
			this.courseId = courseId;
		}
	 
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<PreRequisiteCourses> getPreRequisites() {
		return preRequisites;
	}

	public void setPreRequisites(Set<PreRequisiteCourses> preRequisites) {
		this.preRequisites = preRequisites;
	}

	public Set<SkillGained> getGainedSkills() {
		return gainSkills;
	}

	public void setGainedSkills(Set<SkillGained> learnSkills) {
		this.gainSkills = learnSkills;
	}
	
	 

}
