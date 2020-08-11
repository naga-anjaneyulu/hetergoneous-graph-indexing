package com.system.repository;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import com.system.model.PreRequisites;
import com.system.model.Skill;


public interface SkillRepository extends Neo4jRepository<Skill, Long> {
	
	List<Skill> findByName(@Param("name") String name);

	List<Skill> findByNameLike(@Param("name") String name);
	
	@Query("MATCH (skill1:Skill {name:{0}}),(skill2:Skill {name:{1}}),(skill3:Skill {name:{2}}) CALL algo.pageRank.stream('Skill', 'preReq'," + 
			" {iterations:20, dampingFactor:0.15, sourceNodes: [skill1,skill2,skill3], weightProperty: 'weight'})" + 
			" YIELD nodeId,score RETURN algo.asNode(nodeId) ORDER by score DESC")
	List<Skill> rankSkills(String skill1,String skill2, String skill3);

	@Query("CALL algo.pageRank.stream('Skill', 'preReq'," + 
			" {iterations:20, dampingFactor:0.85, sourceNodes:[{s1},{s2},{s3}], weightProperty: 'weight'})" + 
			" YIELD nodeId,score RETURN algo.asNode(nodeId) ORDER by score DESC")
	List<Skill> rankSkills(@Param("s1")Skill s1,@Param("s2")Skill s2, @Param("s3")Skill s3);

	@Query("MATCH (n:Skill)  WHERE toLower(n.name) CONTAINS {skillName} RETURN n")
	List<Skill> findRelatedSkills(@Param("skillName")String skillName);
	
	
	@Query("MATCH (skill1:Skill {name:{0}}),(skill2:Skill {name:{1}}),(skill3:Skill {name:{2}}),(skill4:Skill {name:{3}}),(skill5:Skill {name:{4}}),"
			+ "(skill6:Skill {name:{5}}),(skill7:Skill {name:{6}}),(skill8:Skill {name:{7}}),(skill9:Skill {name:{8}}),(skill10:Skill {name:{9}}) CALL algo.pageRank.stream('Skill', 'preReq'," + 
			" {iterations:20, dampingFactor:0.15, sourceNodes: [skill1,skill2,skill3,skill4,skill5,skill6,skill7,skill8,skill9,skill10], weightProperty: 'weight'})" + 
			" YIELD nodeId,score RETURN algo.asNode(nodeId) ORDER by score DESC")
	List<Skill> rankSkills(String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8,
			String s9, String s10);
	
	@Query("MATCH (skill1:Skill {name:{0}}),(skill2:Skill {name:{1}}),(skill3:Skill {name:{2}}),(skill4:Skill {name:{3}}),(skill5:Skill {name:{4}}),"
			+ "(skill6:Skill {name:{5}}),(skill7:Skill {name:{6}}),(skill8:Skill {name:{7}}),(skill9:Skill {name:{8}}),(skill10:Skill {name:{9}}),(skill11:Skill {name:{10}}),"
			+ "(skill12:Skill {name:{11}}),(skill13:Skill {name:{12}}),(skill14:Skill {name:{13}}),(skill15:Skill {name:{14}}),(skill16:Skill {name:{15}}),"
			+ "(skill17:Skill {name:{16}}),(skill18:Skill {name:{17}}) ,(skill19:Skill {name:{18}}),(skill20:Skill {name:{19}}) CALL algo.pageRank.stream('Skill', 'preReq'," + 
			" {iterations:20, dampingFactor:0.15, sourceNodes: [skill1,skill2,skill3,skill4,skill5,skill6,skill7,skill8,skill9,skill10,skill11,skill12,skill13,skill14,skill15,skill16,skill17,skill18,skill19,skill20], weightProperty: 'weight'})" + 
			" YIELD nodeId,score RETURN algo.asNode(nodeId) ORDER by score DESC")
	List<Skill> rankSkills(String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8,
			String s9, String s10,String s11, String s12, String s13, String s14, String s15, String s16, String s17, String s18,
			String s19, String s20);
	
	@Query("MATCH {0} CALL algo.pageRank.stream('Skill', 'preReq'," + 
			" {iterations:20, dampingFactor:0.15, sourceNodes: {1}, weightProperty: 'weight'})" + 
			" YIELD nodeId,score RETURN algo.asNode(nodeId) ORDER by score DESC")
	List<Skill> rankSkills(String s1, String s2);

	@Query("MATCH (skill1:Skill {name:{0}}),(skill2:Skill {name:{1}}),(skill3:Skill {name:{2}}),(skill4:Skill {name:{3}}),(skill5:Skill {name:{4}}) CALL algo.pageRank.stream('Skill', 'preReq'," + 
			" {iterations:20, dampingFactor:0.15, sourceNodes: [skill1,skill2,skill3,skill4,skill5], weightProperty: 'weight'})" + 
			" YIELD nodeId,score RETURN algo.asNode(nodeId) ORDER by score DESC")
	List<Skill> rankSkills(String s1, String s2, String s3, String s4, String s5);

	@Query("MATCH p=()-[r:preReq]->() return p")
	List<PreRequisites> findAllSkillRelationships();

	@Query("MATCH (s:Skill)-[r:simiSkill]->(s1:Skill) where toLower(s.name) = {jobSkill}  return s1 order by r.weight desc")
	List<Skill> findBySimilarity(@Param("jobSkill")String jobSkill);

	@Query("MATCH (skill1:Skill {name:{0}}),(skill2:Skill {name:{1}}),(skill3:Skill {name:{2}}),(skill4:Skill {name:{3}}),(skill5:Skill {name:{4}}),"
			+ "(skill6:Skill {name:{5}}),(skill7:Skill {name:{6}}),(skill8:Skill {name:{7}}),(skill9:Skill {name:{8}}),(skill10:Skill {name:{9}}) CALL algo.pageRank.stream('Skill', 'simiSkill'," + 
			" {iterations:20, dampingFactor:0.15, sourceNodes: [skill1,skill2,skill3,skill4,skill5,skill6,skill7,skill8,skill9,skill10], weightProperty: 'weight'})" + 
			" YIELD nodeId,score RETURN algo.asNode(nodeId) ORDER by score DESC")
	List<Skill> rankJobSkills(String name, String name2, String name3, String name4, String name5, String name6,
			String name7, String name8, String name9, String name10);

	@Query("MATCH (s:Skill)-[:preReq]->(s1:Skill) where s.name = {0} with collect(s1) as skills return skills")
	List<Skill> getPreRequisites(String name);

	@Query("MATCH (s:Skill)-[:preReq]->(s1:Skill) where s1.name = {0} with collect(s) as skills return skills")
	List<Skill> getAdvancedSkills(String name);


}
