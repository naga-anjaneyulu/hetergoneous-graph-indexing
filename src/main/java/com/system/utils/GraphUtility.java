package com.system.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.system.model.Course;
import com.system.model.PreRequisiteCourses;
import com.system.model.PreRequisites;
import com.system.model.Question;
import com.system.model.SimilarSkill;
import com.system.model.Skill;
import com.system.model.SkillGained;
import com.system.repository.SkillRepository;

@Component
public class GraphUtility {

	public HashMap<String,Skill> skills = new HashMap<String,Skill>() ;
	public HashMap<String,Course> courses = new HashMap<String,Course>(); 
	
	@Autowired
	private SkillRepository skillRepository; 

	public GraphUtility(HashMap<String, Skill> skills, HashMap<String, Course> courses) {
		super();
		this.skills = skills;
		this.courses = courses;
	}

	public GraphUtility() {
		/**
		 * empty constructor
		 */
	}

	public HashMap<String, Skill> getSkills() {
		return skills;
	}

	public void setSkills(HashMap<String, Skill> skills) {
		this.skills = skills;
	}

	public HashMap<String, Course> getCourses() {
		return courses;
	}

	public void setCourses(HashMap<String, Course> courses) {
		this.courses = courses;
	}

	/**
	 * This method is used to build the heterogenous graph
	 * @param courseRels
	 * @return
	 */
	public void buildGraph(HashMap<String, Course> courseRels) {

		HashMap<String,Skill> skillsUtilityMap = new HashMap<String,Skill>();
		HashMap<Skill,HashMap<Skill,Integer>> skillGraph = new HashMap<Skill,HashMap<Skill,Integer>>();
		for(Map.Entry<String, Course> course :courseRels.entrySet()) {
			Course cour = course.getValue();
			HashMap<String,Skill> skillMap = new HashMap<String,Skill>();
			Set<PreRequisiteCourses> preRequisites = cour.getPreRequisites();
			Set<SkillGained> skillsGained = cour.getGainedSkills();			
			for(SkillGained currGained : skillsGained) {
				skillMap.put(currGained.getSkill1().getName().trim().toLowerCase(),currGained.getSkill1());
			}
			if(preRequisites != null) {
				for(PreRequisiteCourses preReq : preRequisites) {
					Course prev = preReq.getCourse1();
					Set<SkillGained> preSkillsGained = prev.getGainedSkills();

					for(SkillGained preGained : preSkillsGained) {
						if(!skillMap.containsKey(preGained.getSkill1().getName().trim().toLowerCase())) {
							for(Map.Entry<String,Skill> entry : skillMap.entrySet()) {
								if(skillGraph.containsKey(entry.getValue())) {
									HashMap<Skill,Integer> edges = skillGraph.get(entry.getValue());
									if(edges.containsKey(preGained.getSkill1())){
										edges.put(preGained.getSkill1(),edges.get(preGained.getSkill1()) + preReq.getWeight());
										skillGraph.put(entry.getValue(), edges);
									}else {
										edges.put(preGained.getSkill1(),preReq.getWeight());
										skillGraph.put(entry.getValue(), edges);
									}
								}else {
									HashMap<Skill,Integer> miniGraph = new HashMap<Skill,Integer>();
									for(SkillGained preGained2 : preSkillsGained) {
										miniGraph.put(preGained2.getSkill1(), preReq.getWeight());
									}
									skillGraph.put(entry.getValue(), miniGraph);
								}
							}
						}
					}
				}
			}
		}
		for(Map.Entry<Skill,HashMap<Skill,Integer>> entryMap : skillGraph.entrySet()) {
			Skill curr;
			HashMap<Skill,Integer> edges = entryMap.getValue();
			for(Map.Entry<Skill,Integer> edge : edges.entrySet()) {
				Skill prev;
				if(skillsUtilityMap.containsKey(entryMap.getKey().getName().trim().toLowerCase())){
					curr = skillsUtilityMap.get(entryMap.getKey().getName().trim().toLowerCase());		
				}else {
					curr = entryMap.getKey();
				}
				if(skillsUtilityMap.containsKey(edge.getKey().getName().trim().toLowerCase())) {
					prev = skillsUtilityMap.get(edge.getKey().getName().trim().toLowerCase());
				}else {
					prev = edge.getKey();
				}
				PreRequisites preReq = new PreRequisites(prev, curr,edge.getValue());
				curr.requires(preReq);
				skillsUtilityMap.put(curr.getName().trim().toLowerCase(), curr);
			}
		}
		this.setCourses(courseRels);
		this.setSkills(skillsUtilityMap);
		HashMap<String,Course> finalCourses = new HashMap<String,Course>();
		for(Map.Entry<String, Course> entry : this.courses.entrySet()) {
			Course course = entry.getValue();
			Set<SkillGained> skillsGained = course.getGainedSkills();						
			Set<SkillGained> newSkillsGained = new HashSet<SkillGained>();			
			if(skillsGained != null ) {
				for(SkillGained skill : skillsGained) {

					if(this.skills.containsKey(skill.getSkill1().getName().trim().toLowerCase())) {
						skill.setSkill1(this.skills.get(skill.getSkill1().getName().trim().toLowerCase()));
						newSkillsGained.add(skill);
					}}}
			course.setGainedSkills(newSkillsGained);
			finalCourses.put(course.getName().trim().toLowerCase(), course);
		}
		this.setCourses(finalCourses);





	}

	/**
	 * This method is used to build map the course and its skills.
	 * @param skillPath
	 * @return
	 * @throws IOException
	 */
	public  HashMap<String, Course> getCourseSkills(String skillPath) throws IOException {
		HashMap<String,Course> courses = new HashMap<String,Course>();
		HashMap<String,Skill> skills = new HashMap<String,Skill>();
		FileInputStream fis = null;
		fis = new FileInputStream(skillPath);
		Workbook workbook = new XSSFWorkbook(fis);
		Sheet sheet = workbook.getSheetAt(0);
		DataFormatter dataFormatter = new DataFormatter();
		for (Row row: sheet) {
			int count = 0;
			String courseName = "";
			String courseId = "";
			for(Cell cell: row) {
				if (count == 0) {
					courseId = dataFormatter.formatCellValue(cell);
					count += 1;

				}else if(count == 1){
					courseName = dataFormatter.formatCellValue(cell);
					count+=1;

				}else if (count == 2) {
					String skill = dataFormatter.formatCellValue(cell);
					count += 1;
					Course course;
					if(courses.containsKey(courseId.trim().toLowerCase())) {
						course = courses.get(courseId.trim().toLowerCase());
					}else {
						course = new Course(courseId.trim().toLowerCase(),courseName.trim().toLowerCase());
					}
					Skill skill1;
					if(skills.containsKey(skill.trim().toLowerCase())) {
						skill1 = skills.get(skill.trim().toLowerCase());
					}else {
						skill1 = new Skill(skill.trim().toLowerCase());
						skills.put(skill.trim().toLowerCase(),skill1);
					}
					SkillGained lskill = new SkillGained(course,skill1);
					course.teachesSkill(lskill);
					courses.put(courseId.trim().toLowerCase(), course);

				}
			}
		}
		fis.close();
		return courses;

	}

	/**
	 * This method is used to map the relationships between courses.
	 * @param coursePath
	 * @param courses 
	 * @return
	 * @throws IOException
	 */
	public HashMap<String, Course> getCourseRels(String coursePath, HashMap<String, Course> courses) throws IOException {

		HashMap<String, HashMap<String, Integer>> courseRels = new HashMap<String, HashMap<String, Integer>>();
		FileInputStream fis = null;
		fis = new FileInputStream(coursePath);
		Workbook workbook = new XSSFWorkbook(fis);	 
		Sheet sheet = workbook.getSheetAt(0);
		DataFormatter dataFormatter = new DataFormatter();
		for (Row row: sheet) {
			int count = 0;
			String prevCourse = "";
			String currCourse ="";
			for(Cell cell: row) {
				if (count == 0) {
					prevCourse = dataFormatter.formatCellValue(cell);
					count += 1;
				}else if (count == 1) {
					currCourse = dataFormatter.formatCellValue(cell);
					count += 1;
				}else if(count == 2) {
					int skillWeight = 0;
					String weight = dataFormatter.formatCellValue(cell);
					if(!weight.equals("count")) {
						skillWeight = Integer.parseInt(weight.trim());
					}
					count += 1;
					if(courseRels.containsKey(currCourse.trim().toLowerCase())) {
						HashMap<String, Integer> courseRel = courseRels.get(currCourse.trim().toLowerCase());
						courseRel.put(prevCourse.trim().toLowerCase(), skillWeight);
						courseRels.put(currCourse.trim().toLowerCase(), courseRel);
					}else {
						HashMap<String, Integer> courseRel = new HashMap<String, Integer>();
						courseRel.put(prevCourse.trim().toLowerCase(), skillWeight);
						courseRels.put(currCourse.trim().toLowerCase(), courseRel);
					}
				}
			}
		}

		for(Map.Entry<String,HashMap<String,Integer>> rel : courseRels.entrySet()) {
			Course curr = courses.get(rel.getKey());
			if ( curr != null) {
				for(Map.Entry<String,Integer> edges : rel.getValue().entrySet()) {
					Course prev = courses.get(edges.getKey());
					if(prev != null) {
							PreRequisiteCourses preReq = new PreRequisiteCourses(prev, curr, edges.getValue());
							curr.requiresCourse(preReq);

					}}
				courses.put(rel.getKey(), curr);
			}}
		fis.close();
		return courses;


	}

	public HashMap<String, Question> generateQuestions(String quesPath, HashMap<String, Skill> skillGraph) throws IOException {
		
		HashMap<String, Question> quesGraph = new HashMap<String,Question>();
		int ques_count = 1;
		FileInputStream fis = null;
		fis = new FileInputStream(quesPath);
		Workbook workbook = new XSSFWorkbook(fis);	 
		Sheet sheet = workbook.getSheetAt(0);
		DataFormatter dataFormatter = new DataFormatter();
		for (Row row: sheet) {
			int count = 0;
			String question = "";
			String difficulty ="";
			String[] skills = null;
			for(Cell cell: row) {
				if (count == 0) {
					question = dataFormatter.formatCellValue(cell);
					count += 1;
				}else if (count == 1) {
					difficulty = dataFormatter.formatCellValue(cell);
					count += 1;
				}else if(count == 2) {
					skills = dataFormatter.formatCellValue(cell).split(",");
					count += 1;
				}else if(count ==3) {
					String answer = dataFormatter.formatCellValue(cell);
					Question ques = new Question("ques_"+ques_count,question.toLowerCase(),difficulty.trim().toLowerCase(),answer.toLowerCase());
					ques_count += 1;
					for(int i = 0; i < skills.length;i++) {
					String skill = skills[i];
					Skill s = skillGraph.get(skill.trim().toLowerCase());
					if( s!= null) {
						ques.testSkill(s);}
					}
						quesGraph.put(question,ques);
					}
				}
			}
		
		
		
		
		
		return quesGraph;
	}

	public HashMap<String, Skill> buildJobSkillGraph(HashMap<String, Integer> skillMap, HashMap<String, Integer> cleanedMap, 
			IndexSearcher courseSearcher) {
		HashMap<String, Skill> skillGraph = new HashMap<String,Skill>();
		
		for(Map.Entry<String,Integer> map:skillMap.entrySet()) {
			String queryString = "";
			String[] skillArray = map.getKey().split("\\s");
			int max_value = 1000;
			int max_i = -1;
			for(int i =0 ; i<skillArray.length;i++) {
				
				if(cleanedMap.containsKey(skillArray[i].replace(")","").replace("(","").replace(",","").replace("-","").trim().toLowerCase())) {
					
					if (cleanedMap.get(skillArray[i].replace(")","").replace("(","").replace(",","").replace("-","").trim().toLowerCase()) < max_value) {
						max_value = cleanedMap.get(skillArray[i].replace(")","").replace("(","").replace(",","").replace("-","").trim().toLowerCase());
						max_i = i;
					}
				}
			}
			
			List<Query> queryList = new ArrayList<Query>();
			for(int i = 0; i < skillArray.length;i++) {
                 				
				if ( i != 0) {
					queryString += "";
				}
				if(i == max_i) {
				
					queryString += skillArray[i].replace(")","").replace("(","").replace(",","").replace("-","").toLowerCase()+"  ";
				}else {
					
					queryString += skillArray[i].replace(")","").replace("(","").replace(",","").replace("-","").toLowerCase()+"  ";
				}
				
			}
		
			System.out.println(queryString);
			Skill skill = new Skill(map.getKey().replace(")","").replace("(","").replace(",","").replace("-","").toLowerCase());
			List<String> skillList2 = new ArrayList<String>();
			courseSearcher.setSimilarity(new ClassicSimilarity());
			Analyzer analyzer = new StandardAnalyzer(); 
			QueryParser parser = new QueryParser("Skills", analyzer); 
			Query query = null;
			try {
				query = parser.parse(QueryParserUtil.escape(queryString));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			
			TopScoreDocCollector collector = TopScoreDocCollector.create(1000);
			try {
				courseSearcher.search(query, collector);
			} catch (IOException e) {
				e.printStackTrace();
			}          
			ScoreDoc[] docs = collector.topDocs().scoreDocs;
			int count = 0;
			if (docs.length > 0) {
			for (int i = 0; i < docs.length; i++) { 
				if(count < 5) {
					Document doc;
					try {
						doc = courseSearcher.doc(docs[i].doc);
						String cskill =  doc.get("Skills"); 
						
						double weight = (double)docs[i].score ;
						System.out.println(cskill.trim()+"--------"+weight);
						List<Skill> skillList = (List<Skill>) skillRepository.findRelatedSkills(cskill.trim().toLowerCase());
						for(Skill s : skillList) {
							if(!skillList2.contains(s.getName())){
								SimilarSkill simSkill = new SimilarSkill(skill,s,weight);
								skill.similarTo(simSkill);
								skillList2.add(s.getName().toLowerCase());
							}
						}
					} catch (IOException e) {
						
						e.printStackTrace();
					}  
					count += 1;
				 }
			  }   
			}
			if( skill.getSimiSkills() != null ) {
				skillGraph.put(skill.getName().toLowerCase(),skill);
			}
		}
		return skillGraph;
	}

  public HashMap<String,Integer> cleanSkillData(HashMap<String,Integer> skillMap){
	  HashMap<String,Integer> cleanedMap = new HashMap<String,Integer>();
	  for( Map.Entry<String,Integer> mapEntry : skillMap.entrySet()) {
		   String[] skillArray = mapEntry.getKey().split("\\s");
		   for(int i =0 ; i < skillArray.length; i++) {
			   if(cleanedMap.containsKey(skillArray[i].replace(")","").replace("(","").replace(",","").replace("-","").trim().toLowerCase())) {
				   cleanedMap.put(skillArray[i].replace(")","").replace("(","").replace(",","").replace("-","").trim().toLowerCase(), cleanedMap.get(skillArray[i].replace(")","").replace("(","").replace(",","").replace("-","").trim().toLowerCase()) + 1);
				   
			   }else {
				   cleanedMap.put(skillArray[i].replace(")","").replace("(","").replace(",","").replace("-","").trim().toLowerCase(),1);
			   }
		   }
		  
	  }
	  return cleanedMap;
	  
  }


}
