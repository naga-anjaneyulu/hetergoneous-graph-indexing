package com.system.repository;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import com.system.model.Question;


public interface QuestionRepository  extends Neo4jRepository<Question, Long>{

	@Query("MATCH  (q:Question)-[:testSkill]->(s:Skill) where s.name = {0} and q.difficulty = {1} return q")
	List<Question> findQuestionbySkill(String name, String difficulty);

}
