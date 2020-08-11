package com.system.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;


import com.system.model.Course;
import com.system.model.PreRequisites;
import com.system.model.Question;
import com.system.model.Skill;
import com.system.repository.CourseRepository;
import com.system.repository.QuestionRepository;
import com.system.repository.SkillRepository;
import com.system.utils.GraphUtility;
import com.system.utils.SearchUtility;



@SpringBootApplication
@EnableNeo4jRepositories
public class CourseRecommendationApplication {
	
	private final static Logger log = LoggerFactory.getLogger(CourseRecommendationApplication.class);
	private String skillPath = "C:\\Users\\highw\\Desktop\\Fall_2019\\Research Work\\Final_Data"
			+ "\\final_course_skill.xlsx";
	private String coursePath = "C:\\Users\\highw\\Desktop\\Fall_2019\\Research Work\\Final_Data"
			+ "\\Zhu_cleaned_data\\courses_relationship_data.xlsx";
	private String jobCatPath = "C:\\Users\\highw\\Desktop\\Fall_2019\\Research Work\\Final_Data"
			+ "\\Zhu_cleaned_data\\jobCategory.xlsx";
	private String quesPath = "C:\\Users\\highw\\Desktop\\Fall_2019\\Research Work\\Final_Data\\"
			+ "Q&A\\Questions\\question.xlsx";
	private HashMap<String, HashMap<String, Integer>> skillGraph = 	
			new HashMap<String, HashMap<String, Integer>>();
	private HashMap<String, Course> graph = new HashMap<String, Course>();


	public static void main(String[] args) {
		SpringApplication.run(CourseRecommendationApplication.class, args);
	}
	
	/**
	 * This bean is used to build  a heterogeneous graph consisting of skill and course nodes.
	 * 
	 * @param skillRepository
	 * @param courseRepository
	 * @return
	 */
	@Bean CommandLineRunner builGraph(SkillRepository skillRepository,CourseRepository 
									courseRepository,QuestionRepository quesRepository) {
		return args -> {

			if(skillRepository.count() == 0) {
				skillRepository.deleteAll(); 
				courseRepository.deleteAll(); 
				quesRepository.deleteAll();
				GraphUtility graphObj = new GraphUtility(); 
				HashMap<String, Course> courses = graphObj.getCourseSkills(this.skillPath); 
				HashMap<String, Course> courseRels = graphObj.getCourseRels(this.coursePath, courses); 
				graphObj.buildGraph(courseRels);
				HashMap<String, Skill> skillGraph = graphObj.getSkills(); HashMap<String,
				Course> courseGraph = graphObj.getCourses();

				for (Map.Entry<String, Course> entry : courseGraph.entrySet()) {
					courseRepository.save(entry.getValue()); }
				List<Skill> skillList = (List<Skill>) skillRepository.findAll();
				
				HashMap<String,Skill> skillGraph2 = new HashMap<String,Skill>();
				for(Skill s: skillList) {
					skillGraph2.put(s.getName(),s);
				}
				HashMap<String,Question> quesGraph = graphObj.generateQuestions(this.quesPath,skillGraph2);
				for (Map.Entry<String, Question> entry : quesGraph.entrySet()) {
					quesRepository.save(entry.getValue()); }
				
				log.info("Wohooo , your heterogenous  graph is ready !");
			}
		}; 
	}
	
	
	@Bean
	CommandLineRunner jobSkillBuilder(SkillRepository skillRepository) {
		return args -> {
			IndexReader courseReader =  DirectoryReader.open(FSDirectory.open(Paths.
					get("C:\\Users\\highw\\Desktop\\Fall_2019\\Research Work\\Index\\CourseSkill")));
			IndexSearcher courseSearcher = new IndexSearcher(courseReader); 
			String dirPath = "C:\\Users\\highw\\Desktop\\Fall_2019\\Research Work\\Final_Data\\Jobs\\FinalJobs";
			File dir = new File(dirPath);
			File[] directoryListing = dir.listFiles();
			HashMap<String,Integer> skillMap = new HashMap<String,Integer>();
			int count = 0;
			for (File csvFile : directoryListing) {
				BufferedReader br = null;
				String line = "";
				String cvsSplitBy = ",";
				if (csvFile != null) {
					br = new BufferedReader(new FileReader(csvFile));
					line = br.readLine();
					while (line != null  ) {
						if (count > 0 && line.length() >= 2) {
							String[] jobRecord = line.split(cvsSplitBy);
							if(jobRecord.length == 9 ){		
								String[] jobSkillList = jobRecord[5].toString().split("\\|");
								for(int i = 0; i < jobSkillList.length -1 ; i++) {
									String skill = jobSkillList[i].trim().replace("'","").replace("\n","").replace(",","").replace("-","");
									if(!skillMap.containsKey(skill) && skill != "") {
										skillMap.put(skill,1);
									}
								}
							}}
						line = br.readLine();
						count += 1;
					}}}
			GraphUtility graphObj = new GraphUtility(); 
			HashMap<String,Integer> cleanedMap = graphObj.cleanSkillData(skillMap);
		    HashMap<String,Skill> skillGraph = graphObj.buildJobSkillGraph(skillMap,cleanedMap,courseSearcher);
		    System.out.println(skillMap.size());
		    System.out.println(skillGraph.size());
			for(Map.Entry<String,Skill> map : skillGraph.entrySet()) {
			skillRepository.save(map.getValue());
			}
		};	
	}
	
	
	
	
	
	
}
