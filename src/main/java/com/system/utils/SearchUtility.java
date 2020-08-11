package com.system.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.system.model.SimilarSkill;
import com.system.model.Skill;

@Component
public class SearchUtility {
	
	

	public List<String> courseSearch(List<String> jobSkills, IndexSearcher searcher,String field) throws IOException, ParseException {
		List<String> skillList = new ArrayList<String>();
		HashMap<String,Integer> courseSkillMap = new HashMap<String,Integer>();
		String queryString = "";
		String result = "";
		for(String skill : jobSkills) {
			queryString += skill;
		}
		
		
		Analyzer analyzer = new StandardAnalyzer(); 
		QueryParser parser = new QueryParser(field, analyzer); 
		Query query = parser.parse(QueryParserUtil.escape(queryString));
		System.out.println(query.toString());

		TopScoreDocCollector collector = TopScoreDocCollector.create(1000);
		searcher.search(query, collector);          
		ScoreDoc[] docs = collector.topDocs().scoreDocs;
		int count = 1;
		for (int i = 0; i < 50; i++) { 
			Document doc = searcher.doc(docs[i].doc);  
			result += "-" + doc.get("Skills");

		}   

		String[] skillArray = result.toString().split("-");
		for(int i =1;i < skillArray.length ;i++) {
			String skill = skillArray[i].toLowerCase().trim().replace(",","").replace("\n"," ");
			if(!courseSkillMap.containsKey(skill)) {
				//System.out.println(skill);
				courseSkillMap.put(skill,1);
				skillList.add(skill);
			}
				
				

		}
		
		List<String> newSkillList = getRelevantCourseSkills(skillList, 100);
		
			return skillList;
	}
	
	

	private List<String> getRelevantCourseSkills(List<String> skillList2, int count) {

		HashMap<String,Integer> skillMap = new HashMap<String,Integer>();
		List<String> skillList = new ArrayList<String>();
		for(String skill :skillList2 ) {
		
			if (skillMap.containsKey(skill)) {
				
				skillMap.put(skill, skillMap.get(skill) + 1);
			}else {
				skillMap.put(skill, 1);
			}
		}

		TreeMap<String,Integer> skillTree = new TreeMap<String,Integer>(skillMap);
		Comparator<String> comparator = new ValueComparator(skillTree);
		TreeMap<String, Integer> result = new TreeMap<String, Integer>(comparator);
		result.putAll(skillTree);
		int c = 0;
		for(Map.Entry<String,Integer> relevantSkill : result.entrySet()) {
			if(c < count) {
				//System.out.println(relevantSkill.getKey() +" -----  "+relevantSkill.getValue());
				c += 1;
				skillList.add(relevantSkill.getKey());
			}
		}
		//System.out.println("\n");
		
		return skillList;
	
	
	}



	public void quesSearch(String queryString, IndexSearcher searcher,String field) throws IOException, ParseException {
		Analyzer analyzer = new StandardAnalyzer(); 
		QueryParser parser = new QueryParser(field, analyzer); 
		//System.out.println(queryString);
		Query query = parser.parse(QueryParserUtil.escape(queryString));
		TopScoreDocCollector collector = TopScoreDocCollector.create(1000);
		searcher.search(query, collector);          
		ScoreDoc[] docs = collector.topDocs().scoreDocs;
		int count = 1;
		for (int i = 0; i < 6; i++) { 
			Document doc = searcher.doc(docs[i].doc);  
			//System.out.println(doc.get("Question"));

		}   


	}

	public String jobSearch(String queryString, IndexSearcher searcher,String field) throws IOException, ParseException {
		Analyzer analyzer = new StandardAnalyzer(); 
		QueryParser parser = new QueryParser(field, analyzer);    
		String skills = "";
		Query query = parser.parse(QueryParserUtil.escape(queryString));
		//System.out.println(query.toString());
		TopScoreDocCollector collector = TopScoreDocCollector.create(1000);
		searcher.search(query, collector);          
		ScoreDoc[] docs = collector.topDocs().scoreDocs;
		int count = 1;
		for (int i = 0; i < docs.length; i++) { 
			Document doc = searcher.doc(docs[i].doc); 
			skills += doc.get("Skills");
			//System.out.println(doc.get("Category") + "\n");

		}   


		return skills.replace("\n"," ");
	}


	/**
	 * This method is used to map the category to its skills.
	 * @param coursePath
	 * @return
	 * @throws IOException
	 */
	public HashMap<String, HashMap<String, Integer>> getJobSkillsRel(String jobCategoryPath) throws IOException {

		HashMap<String, HashMap<String, Integer>> jobSkills = new HashMap<String, HashMap<String, Integer>>();

		FileInputStream fis = null;
		fis = new FileInputStream(jobCategoryPath);
		Workbook workbook = new XSSFWorkbook(fis);	 
		Sheet sheet = workbook.getSheetAt(0);
		DataFormatter dataFormatter = new DataFormatter();
		for (Row row: sheet) {
			int count = 0;
			String skill = "";
			String category ="";
			for(Cell cell: row) {
				if (count == 0) {
					category = dataFormatter.formatCellValue(cell);
					count += 1;
				}else if (count == 1) {
					skill = dataFormatter.formatCellValue(cell);
					skill = skill.trim();
					count += 1;

				}else if(count == 2) {
					int skillCount = 0;
					String weight = dataFormatter.formatCellValue(cell);
					if(!weight.equals("count")) {
						skillCount = Integer.parseInt(weight.trim());

					}

					count += 1;
					if(jobSkills.containsKey(category)) {
						HashMap<String, Integer> skillMap = jobSkills.get(category);
						skillMap.put(skill, skillCount);
						jobSkills.put(category, skillMap);
					}else {
						HashMap<String, Integer> skillMap = new HashMap<String, Integer>();
						skillMap.put(skill, skillCount);
						jobSkills.put(category, skillMap);

					}


				}
			}
		}

		fis.close();

		/*
		 * for (Map.Entry<String, HashMap<String, Integer>> job : jobSkills.entrySet())
		 * { System.out.print(job.getKey()+"\n"); HashMap<String, Integer> skillMap =
		 * job.getValue(); for(Map.Entry<String, Integer> skill: skillMap.entrySet()) {
		 * System.out.print(skill.getKey()+"-------"+skill.getValue()+"\n"); }
		 * 
		 * 
		 * }
		 */
		return jobSkills;


	}

	public String findUsefulSkill(HashMap<String, HashMap<String, Integer>> jobSkills, String skills, String jobCat) {
		int maxCount = 0;
		String maxSkill = "";
		String[] skillArray = skills.toString().split("\\s");
		System.out.println(skillArray[1]);
		ArrayList<String> skillList = new ArrayList<>();
		String skill ="";
		for(int i =0;i < skillArray.length ;i++) {
			if( skillArray[i].trim().equals("|")) {
				System.out.println(skill);
				skillList.add(skill);
				skill = "";
			}
			else {
				skill += " "+skillArray[i].trim();
			}
		}

		HashMap<String, Integer> jobSkillMap = jobSkills.get(jobCat);
		for(String skill1 : skillList) {
			for (Map.Entry<String,Integer> skillMap : jobSkillMap.entrySet()) {
				String skill2 = skillMap.getKey();
				if(skill2.toLowerCase().trim().equals(skill1.toLowerCase().trim())) {
					int count = jobSkillMap.get(skill2);
					if(count >= maxCount) {
						maxCount = count;
						maxSkill = skill1;
					}
				}}	
		}
		return maxSkill;
	}

	/**
	 * This method is used to get top relevant skills for a given job description.
	 * @param skills
	 * @param count
	 * @return
	 */
	public List<String> getRelevantSkills(String skills,int count) {
		HashMap<String,Integer> skillMap = new HashMap<String,Integer>();
		String[] skillArray = skills.toString().split("\\s");
		ArrayList<String> skillList = new ArrayList<>();
		String skill ="";
		for(int i =0;i < skillArray.length ;i++) {
			if( skillArray[i].trim().equals("|")) {
				if(skillMap.containsKey(skill)) {
					skillMap.put(skill, skillMap.get(skill) + 1);
				}else {
					skillMap.put(skill,1);
				}
				skill = "";
			}
			else {
				skill += " "+skillArray[i].trim().toLowerCase().replace("(","").
						replace(")", "").replace("-","").replace(",","");
			}
		}

	    TreeMap<String,Integer> skillTree = new TreeMap<String,Integer>(skillMap);
		Comparator<String> comparator = new ValueComparator(skillTree);
		TreeMap<String, Integer> result = new TreeMap<String, Integer>(comparator);
		result.putAll(skillTree);
		int c = 0;
		for(Map.Entry<String,Integer> relevantSkill : result.entrySet()) {
			if(c < count) {
				//System.out.println(relevantSkill.getKey() +" -----  "+relevantSkill.getValue());
				c += 1;
				skillList.add(relevantSkill.getKey());
			}
		}
		System.out.println("\n");
		
		return skillList;
	}

	
	public void courseJobCatSearch(String queryString,String field,IndexSearcher searcher) throws ParseException, IOException {
		
		List<String> skillList = new ArrayList<String>();
		String result = "";
		Analyzer analyzer = new StandardAnalyzer(); 
		QueryParser parser = new QueryParser(field, analyzer); 
		Query query = parser.parse(QueryParserUtil.escape(queryString));
		System.out.println(query.toString());

		TopScoreDocCollector collector = TopScoreDocCollector.create(1000);
		searcher.search(query, collector);          
		ScoreDoc[] docs = collector.topDocs().scoreDocs;
		int count = 1;
		for (int i = 0; i < docs.length; i++) { 
			Document doc = searcher.doc(docs[i].doc);  
			result += "-" + doc.get("Skills");

		}   
		
		String[] skillArray = result.toString().split("-");
		for(int i =1;i < skillArray.length ;i++) {
				skillList.add(skillArray[i].toLowerCase().trim().replace(",","").replace("\n"," "));
				
				System.out.println(skillArray[i]);

		}
		
		List<String> newSkillList = getRelevantCourseSkills(skillList, 50);
	}

	public static void main(String[] args) throws IOException, ParseException {


		IndexReader jobReader = DirectoryReader.open(FSDirectory.open(Paths
				.get( "C:\\Users\\highw\\Desktop\\Fall_2019\\Research Work\\Index\\Job")));
		IndexSearcher jobSearcher = new IndexSearcher(jobReader);	

		IndexReader quesReader = DirectoryReader.open(FSDirectory.open(Paths
				.get("C:\\Users\\highw\\Desktop\\Fall_2019\\Research Work\\Index\\Questions")));
		IndexSearcher quesSearcher = new IndexSearcher(quesReader);


		/*
		 * String jobCat = "Java Developer"; Search search = new Search();
		 * System.out.println("Searching job index for Java Developer jobs");
		 * System.out.println("\n");
		 * 
		 * String skills = search.jobSearch(jobCat, jobSearcher, "Category");
		 * System.out.println(skills);
		 * 
		 * HashMap<String, HashMap<String, Integer>> jobSkills =
		 * search.getJobSkillsRel(); String skillQuery =
		 * search.findUsefulSkill(jobSkills,skills,jobCat);
		 * System.out.println("skillQuery  :  "+skillQuery);
		 * 
		 * 
		 * System.out.
		 * println("Searching the questions index based on the above skills :");
		 * System.out.println("\n"); System.out.println("Questions:");
		 * System.out.println("\n"); search.quesSearch(skillQuery , quesSearcher,
		 * "Skills");
		 */

	}



	public HashMap<String, Skill> jobSkillSearch(String queryString, IndexSearcher jobSearcher, String field, HashMap<String, Skill> jobskillMap, Skill courseSkill) throws ParseException, IOException {
		Analyzer analyzer = new StandardAnalyzer();
		jobSearcher.setSimilarity(new BM25Similarity());
		QueryParser parser = new QueryParser(field, analyzer);    
	
		Query query = parser.parse(QueryParserUtil.escape(queryString));
		//System.out.println(query.toString());
		TopScoreDocCollector collector = TopScoreDocCollector.create(1000);
		jobSearcher.search(query, collector);          
		ScoreDoc[] docs = collector.topDocs().scoreDocs;
		int count = 1;
		for (int i = 0; i < docs.length; i++) { 
			Document doc = jobSearcher.doc(docs[i].doc); 
			
			if(jobskillMap.containsKey(doc.get("Skills").trim())) {
				Skill jobSkill = jobskillMap.get(doc.get("Skills").trim());
				SimilarSkill simSkill = new SimilarSkill(jobSkill,courseSkill, docs[i].score);
				jobSkill.similarTo(simSkill);
				jobskillMap.put(jobSkill.getName(),jobSkill);
			}else {
				Skill jobSkill =new Skill(doc.get("Skills").trim());
				SimilarSkill simSkill = new SimilarSkill(jobSkill, courseSkill, docs[i].score);
				jobSkill.similarTo(simSkill);
				jobskillMap.put(jobSkill.getName(),jobSkill);
			}
		}   
		return jobskillMap;
	}

}
