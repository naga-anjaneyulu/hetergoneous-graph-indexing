package com.system.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.system.model.Course;
import com.system.model.Question;
import com.system.model.Skill;
import com.system.repository.CourseRepository;
import com.system.repository.GraphRepository;
import com.system.repository.QuestionRepoUtility;
import com.system.repository.QuestionRepository;
import com.system.repository.SkillRepository;
import com.system.utils.SearchUtility;

@Service
public class RecomService {

	private final static Logger log = LoggerFactory.getLogger(RecomService.class);

	@Autowired
	private GraphRepository graph = new GraphRepository( "bolt://localhost:7687", "neo4j", "idontknow.3" ) ;

	@Autowired
	private SkillRepository skillRepository;

	@Autowired
	private GraphRepository graphRepository;

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private QuestionRepository quesRepository;

	@Autowired
	private QuestionRepoUtility quesUitl;
	
	@Autowired
	private SearchUtility search;


	public List<Skill> getSkillsForJob(String jobCat) {
		log.info("Extracting relevant skills for the job category ");

		List<Skill> skillEntities = new ArrayList<Skill>(); 
		String skills = null;
		try {
			skills = search.jobSearch(jobCat,jobSearcher, "Skills");
		} catch (IOException | ParseException e1) {
			log.error("Could not retrieve skills for the corresponding job category");
			e1.printStackTrace();
		}
		List<String> jobSkills = search.getRelevantSkills(skills,10); 
		List<String> courseSkills = null;
		try {
			courseSkills = search.courseSearch(jobSkills, courseSearcher, "Description");
		} catch (IOException | ParseException e) {
			log.error("Could not retrieve skills from course index");
			e.printStackTrace();
		}
		for(String skill :courseSkills) {
			skill = skill.trim();
			List<Skill> skillList = this.skillRepository.findSimilarSkill(skill.
					toLowerCase().trim().replace("(","").replace(")","").replace("-",""));
			if(skillList.size() > 0) {
				skillEntities.add(skillList.get(0));
			} 
		}

		return skillEntities;

	}



	private List<Skill> getSkillRanking(List<Skill> skillEntities) {
		log.info("Ranking the skills ....");
		List<Skill> skillRanking = this.skillRepository.rankSkills(skillEntities.get(0).getName(),
				skillEntities.get(1).getName(),skillEntities.get(2).getName(),skillEntities.get(3).getName(),
				skillEntities.get(4).getName(),skillEntities.get(5).getName(),skillEntities.get(6).getName(),
				skillEntities.get(7).getName(),skillEntities.get(8).getName(),skillEntities.get(9).getName()); 

		//List<Skill> skillRanking = skillRepository.rankSkills(s1,s2,s3);
		//List<Skill> skillRanking3 = skillRepository.rankSkills(s1,s2,s3,s4,s5);
		int count = 0;
		System.out.println("\n\n Top 10 skills based on ranking : ");
		for(Skill skill : skillRanking) {
			if (count <10) {
				System.out.println(skill.getName());
				count += 1;}else {break; } 
		} 
		return skillRanking;
	}

	private List<Course> getCourseRanking(List<Skill> skillRanking) {
		log.info("Ranking the courses ....");
		List<Skill> top5Skills = skillRanking.stream().limit(5).collect(Collectors.toList()); 
		List<String> finalCourses = new ArrayList<String>();
		for(Skill skill: top5Skills) {
			int count1 = 0;
			List<Course> courses = this.courseRepository.rankedCoursesForSkills(skill.getName());
			for(int i =0; i< courses.size();i++) {
				if(!finalCourses.contains(courses.get(i).getName()) && count1 < 2) {
					finalCourses.add(courses.get(i).getName());
					count1 += 1; 
				} 
			}
		}

		List<Course> courseRanking = this.courseRepository.rankCourses(finalCourses.get(0),finalCourses.get(1),
				finalCourses.get(2),finalCourses.get(3),finalCourses.get(4),finalCourses.get(5),finalCourses.get(6),
				finalCourses.get(7),finalCourses.get(8));
		//List<Course> courseRanking2 = courseRepository.rankCourses1(finalCourses.get(0),finalCourses.get(1),
		//finalCourses.get(2),finalCourses.get(3),finalCourses.get(4));
		System.out.println("\n\nTop 10 courses based on ranking : ");
		int count =0;
		List<String> courses = new ArrayList<String>();
		for(Course course :courseRanking) {
			if (count <10) {
				courses.add(course.getName());
				System.out.println(course.getName());
				count += 1;}else {break; } 
		}
		return courseRanking;
	}


	public Question geneateFirstQuestion(String jobCategory) {
		log.info("Generating first question");
		try {
			this.jobReader =  DirectoryReader.open(FSDirectory.open(Paths 
					.get("C:\\Users\\highw\\Desktop\\Fall_2019\\Research Work\\Index\\Job")));
			this.courseReader =  DirectoryReader.open(FSDirectory.open(Paths.
					get("C:\\Users\\highw\\Desktop\\Fall_2019\\Research Work\\Index\\Course")));
			this.jobSearcher = new IndexSearcher(jobReader);
			this.courseSearcher = new IndexSearcher(courseReader);
		} catch (IOException e) {
			log.error("Unable to access job/course index at this time.");
			e.printStackTrace();
		}
		List<Skill> skillEntities = getSkillsForJob(jobCategory);
		List<Skill> skillRanking = getSkillRanking(skillEntities);
		List<Course> courseRanking = getCourseRanking(skillRanking);
		List<String> courses = new ArrayList<String>();
		courseRanking.stream().forEach((course)->{courses.add(course.getName());});
		// Courses Ranking Map
		HashMap<String,Integer> courseMap = new HashMap<String,Integer>();
		for(Course course : courseRanking) {
			courseMap.put(course.getCourseId(),courseRanking.indexOf(course));
		}
		// Skill Ranking Map
		HashMap<String,Integer> skillMap = new HashMap<String,Integer>();
		for(Skill skill : skillRanking) {
			skillMap.put(skill.getName(), skillRanking.indexOf(skill));
		}
		String query = this.graph.generateCommunityQuery(courseRanking, skillRanking);
		HashMap<Integer, List<String>> commMap = this.graph.getCourseCommunities(query);
		System.out.println("\n\nCourses Communities \n");

		for(Map.Entry<Integer,List<String>> map : commMap.entrySet()) {
			System.out.println("Community :"+ map.getKey());
			List<String> CommCourses = map.getValue();
			TreeMap<Integer,String> map1 = new TreeMap<Integer,String>();
			CommCourses.forEach( (course)->{map1.put( courses.indexOf(course),course) ;});
			for(Map.Entry<Integer,String> map2:map1.entrySet()) {
				System.out.println(map2.getKey()+"-----------"+map2.getValue());
			}


		}

		System.out.println("\n\n Question 1");
		this.quesUitl.getFirstQuestion(commMap,skillMap,courseMap);
		//List<Course> courseList = courseRepository.detectCommunities(courses,skills);	
		return null; 


	}








}
