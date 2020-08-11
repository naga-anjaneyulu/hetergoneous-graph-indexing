package com.system.repository;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;


import com.system.model.Course;
import com.system.model.PreRequisiteCourses;
import com.system.model.Skill;



public interface CourseRepository extends Neo4jRepository<Course, Long>{
	
Course findByName(@Param("name") String name);
	
	Course findByCourseId(@Param("courseId") String courseId);

	@Query("MATCH (c:Course)-[r:gainSkill]->(s:Skill)  where  s.name = {0} RETURN c")
	List<Course>  findCourses(String name);
	
	@Query("MATCH (n:Skill {name:{0}})  CALL algo.pageRank.stream(\' MATCH (c:Course)-[r:gainSkill]->(n) RETURN id(c) as id  \',"+
" \' MATCH (c1:Course)-[:preCourse]->(c2:Course) RETURN id(c1) as source, id(c2) as target \',"+
"{graph:'cypher',dampingFactor:0.15, weightProperty:'weight'}) YIELD nodeId,score RETURN algo.asNode(nodeId) ORDER by score DESC LIMIT 15")
	List<Course> rankedCoursesForSkill(String name);
	
	@Query("MATCH (c:Course)-[r:gainSkill]->(s:Skill)  where  s.name = {0} with c as collection " + 
			"CALL algo.pageRank.stream('Course', 'preCourse', {iterations:20, dampingFactor:0.15, sourceNodes: [collection] , weightProperty: 'weight'}) " + 
			"YIELD nodeId, score  RETURN algo.asNode(nodeId) AS page,score " + 
			"ORDER BY score DESC LIMIT 10")
	List<Course> rankedCoursesForSkills(String name);
	

	@Query("MATCH (course1:Course {name:{0}}),(course2:Course {name:{1}}),(course3:Course {name:{2}}),(course4:Course {name:{3}}),(course5:Course {name:{4}}),"
				+ "(course6:Course {name:{5}}),(course7:Course {name:{6}}),(course8:Course {name:{7}}),(course9:Course {name:{8}}),(course10:Course {name:{9}}) CALL algo.pageRank.stream('Course', 'preCourse'," + 
				" {iterations:20, dampingFactor:0.15, sourceNodes: [course1,course2,course3,course4,course5,course6,course7,course8,course9,course10], weightProperty: 'weight'})" + 
				" YIELD nodeId,score RETURN algo.asNode(nodeId) ORDER by score DESC")
	List<Course> rankCourses(String course1, String course2, String course3, String course4, String course5, String course6,
			String course7, String course8, String course9,String course10);
	
	
	@Query("MATCH (course1:Course {name:{0}}),(course2:Course {name:{1}}),(course3:Course {name:{2}}),(course4:Course {name:{3}}),(course5:Course {name:{4}}),"
			+ "(course6:Course {name:{5}}),(course7:Course {name:{6}}),(course8:Course {name:{7}}),(course9:Course {name:{8}}),(course10:Course {name:{9}}) ,"
			+ "(course11:Course {name:{10}}),(course12:Course {name:{11}}),(course13:Course {name:{12}}),(course14:Course {name:{13}}) ,(course15:Course {name:{14}}) ,"
			+ "(course16:Course {name:{15}}),(course17:Course {name:{16}}) ,(course18:Course {name:{17}}) ,(course19:Course {name:{18}}) ,(course20:Course {name:{19}}) CALL algo.pageRank.stream('Course', 'preCourse'," + 
			" {iterations:20, dampingFactor:0.15, sourceNodes: [course1,course2,course3,course4,course5,course6,course7,course8,course9,course10,course11,course12,course13,course14,course15,course16,course17,course18,course19,course20], weightProperty: 'weight'})" + 
			" YIELD nodeId,score RETURN algo.asNode(nodeId) ORDER by score DESC")
List<Course> rankCourses(String course1, String course2, String course3, String course4, String course5, String course6,
		String course7, String course8, String course9,String course10,String course11, String course12, String course13, String course14, String course15, String course16,
		String course17, String course18, String course19,String course20);

	@Query("MATCH (course1:Course {name:{0}}),(course2:Course {name:{1}}),(course3:Course {name:{2}}),(course4:Course {name:{3}}),(course5:Course {name:{4}})"
			+ " CALL algo.pageRank.stream('Course', 'preCourse'," + 
			" {iterations:20, dampingFactor:0.15, sourceNodes: [course1,course2,course3,course4,course5], weightProperty: 'weight'})" + 
			" YIELD nodeId,score RETURN algo.asNode(nodeId) ORDER by score DESC")
	List<Course> rankCourses1(String name, String name2, String name3, String name4, String name5);

	@Query("WITH {0} as course ,{1} as skill CALL algo.louvain.stream('WITH course as names MATCH (c:Course)-[:preCourse]->(c1:Course) WHERE c.name in names and c1.name in names WITH collect(c1) as community WITH skill as skills ,community as community MATCH p=(c2:Course)-[:gainSkill]->(s:Skill) WHERE c2 in community  and s.name in skills with nodes(p) as nodes MATCH (n) where n in nodes return id(n) as id', \r\n" + 
			"'MATCH (n1:Course)-[]->(n2:Course) return id(n1) as source,id(n2) as target ',\r\n" + 
			"{graph:\"cypher\",includeIntermediateCommunities: true, weightProperty: 'weight'}) \r\n" + 
			"YIELD nodeId, community,communities\r\n" + 
			"RETURN algo.asNode(nodeId), community,communities ")
	List<Course> detectCommunities(List<String> courses, String skills);

	@Query("MATCH (c:Course)-[:gainSkill]->(s:Skill) where c.courseId = {0} with collect(s) as skills return skills")
	List<Skill> getSkillGainedbyCourseId(String courseId);

	@Query("MATCH (c:Course)-[:preReq]->(c1:Vourse) where c.courseId = {0} with collect(c1) as preCourses return preCourses")
	List<PreRequisiteCourses> getPreRequisites(String courseId);

	@Query("MATCH (c:Course)-[:gainSkill]->(s1:Skill) where s1.name = {0} with collect(c) as courses return courses")
	List<Course> getCourses(String skill);


}
