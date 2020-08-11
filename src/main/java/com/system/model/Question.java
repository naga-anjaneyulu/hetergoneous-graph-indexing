package com.system.model;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;


@NodeEntity
public class Question {
	

	@Id
	@GeneratedValue 
	private Long id;
	
	private String name;
	
	private String question;
	
	private String difficulty;

	private String answer;
	
	
	@Relationship(type = "testSkill", direction = Relationship.OUTGOING)
	private Set<Skill> testSkills;
	

	public Question() {
		
	};

	public Question(String name,String question, String difficulty, String answer) {
		super();
		this.name = name;
		this.question = question;
		this.difficulty = difficulty;
		this.answer = answer;
	}

	public void testSkill(Skill skill) { 
		 if (testSkills == null) {
			 testSkills = new HashSet<>(); }
		 testSkills.add(skill);
	 }
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getDifficulty() {
		return difficulty;
	}
	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	

}
