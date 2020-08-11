package com.system.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


import com.system.model.Question;
import com.system.model.Skill;

@Repository
public class QuestionRepoUtility {
	
	@Autowired
	SkillRepository skillRepository;
	
	@Autowired
	GraphRepository graphRepository;
	
	@Autowired
	CourseRepository courseRepository;
	
	@Autowired
	QuestionRepository quesRepository;
	

	private List<Integer> commVisited = new ArrayList<Integer>();

	private List<String> coursesVisited = new ArrayList<String>();

	private List<String> skillsVisited = new ArrayList<String>();

	private List<String> coursesNVisited = new ArrayList<String>();
	
	private List<String> skillIgnoreList = new ArrayList<String>();
 
	private HashMap<String,Integer> abilityMap = new HashMap<String,Integer>();

	String courseId ="0";

	int quesCount = 1;

	private Integer pickCommunity(HashMap<Integer, List<String>> commMap) {
		int maxComm = -1;
		int maxSize = 0;
		int commSize =0;
		int commCount =0;
		
		for(Entry<Integer, List<String>> comm : commMap.entrySet()) {
			if(comm.getValue().size() >= maxSize && !this.commVisited.contains(comm.getKey())) {
				maxComm = comm.getKey();
				maxSize = comm.getValue().size();} }

		
		// When all communities have been visited atleast once.
		if(maxComm == -1) {
			maxSize = 0;
			for(Entry<Integer, List<String>> comm : commMap.entrySet()) {
				List<String> commCourses = comm.getValue();
				int count = 0;
				for(String course :this.coursesVisited) {
					if(commCourses.contains(course)) {
						count += 1;} }
				
				if(count != commSize) {
					commCount += 1;
				}
				
				if(count > maxSize) {
					maxComm = comm.getKey();
					maxSize = commCourses.size() - count ;} } }
		// When all courses in all communities have been visited or all courses have the same size .
		if(commCount == 1) {
			maxComm = (int)(Math.random()*((commMap.size()-1 - 0) + 1));}
		
		if(!this.commVisited.contains(maxComm)) {
			this.commVisited.add(maxComm); }

		return maxComm;
	}


	public HashMap<String, Integer> generateQuestions(HashMap<Integer, List<String>> commMap, HashMap<String, Integer> skillMap,
			HashMap<String, Integer> courseMap) {

		if(this.quesCount == 1) {
			for(Entry<Integer, List<String>> comm : commMap.entrySet()) {
				this.coursesNVisited.addAll(comm.getValue());}
		}
		
		while(this.quesCount < 6) {

			int community = pickCommunity(commMap);
			Skill skill = pickSkill(commMap,community,courseMap,skillMap);
			this.skillsVisited.add(skill.getName());
			System.out.println(skill.getName());
			Scanner myObj = new Scanner(System.in);  
			
			String quesDiff = getQuestDifficulty(skill);
			List<Question> questions = quesRepository.findQuestionbySkill(skill.getName(),quesDiff);
			
			if(questions.size() > 0) {
				System.out.println("\n\n Question "+ this.quesCount + " \n");
				System.out.println(questions.get((int)(Math.random()*((questions.size()-1 - 0) + 1))).getQuestion() + "\n");
				System.out.println("Answer :  \n 1.Yes, I know  \n 2.I don't know \n");
				String choice = myObj.nextLine();  
				updateSkillAbility(choice,skill);
				this.quesCount += 1;
				generateQuestions(commMap,skillMap,courseMap);
			}else {
				this.skillIgnoreList.add(skill.getName());
				generateQuestions(commMap,skillMap,courseMap);
			}
			
		}
		
		for(String skill : this.skillIgnoreList) {
			
			if(this.abilityMap.containsKey(skill)) {
				this.abilityMap.remove(skill);
			}
		}
		
		return this.abilityMap;
	}


	private void updateSkillAbility(String choice, Skill skill) {
		Scanner myObj = new Scanner(System.in);  
		if(choice.equals("1")) {
			this.abilityMap.put(skill.getName(),this.abilityMap.get(skill.getName()) + 1);
		}else if(choice.equals("2")) {
			this.abilityMap.put(skill.getName(),this.abilityMap.get(skill.getName()) - 1);
		}else {
			System.out.println("Invalid choice , please enter either 1 or 2 accordingly");
			String newChoice = myObj.nextLine();
			updateSkillAbility(newChoice,skill);
		}
	}


	private String getQuestDifficulty(Skill skill) {

		int level = 1;
		
		if(this.abilityMap.containsKey(skill.getName())) {
			level = this.abilityMap.get(skill.getName());
		}else {
			this.abilityMap.put(skill.getName(),level);
		}
		String difficulty = "medium";
		if(level < 1 && level >= 0 ) {
			difficulty = "easy";
		}else if ( level > 1 && level <= 4) {
			difficulty = "hard";
		}else if(level <= 0 ) {
			List<Skill> preReqs = this.skillRepository.getPreRequisites(skill.getName());
			if(preReqs.size() > 0 ) {
				skill = preReqs.get(0);
				this.abilityMap.put(skill.getName(),1);
				difficulty ="medium";
				}
		}else if(level > 3  ) {
			List<Skill> advancedSkills = this.skillRepository.getAdvancedSkills(skill.getName());
			if(advancedSkills.size() > 0 ) {
				skill = advancedSkills.get(0);
				this.abilityMap.put(skill.getName(),1);
				difficulty ="medium";
				}
		}else {
			difficulty = "medium";
		}

		return difficulty;
	}


	private Skill pickSkill(HashMap<Integer, List<String>> commMap, int community,
			HashMap<String, Integer> courseMap, HashMap<String, Integer> skillMap) {

		this.courseId = pickCourse(commMap,courseMap,community);
		if(!this.coursesVisited.contains(this.courseId)) {this.coursesVisited.add(this.courseId);}
		if(this.coursesNVisited.contains(this.courseId)) {this.coursesNVisited.remove(this.courseId);}

		List<Skill> skillList = this.courseRepository.getSkillGainedbyCourseId(courseId);
		int skillMin = 100;
		Skill skill = null;
		for(Skill s : skillList) {
			if(!this.skillsVisited.contains(s.getName()) && !this.skillIgnoreList.contains(s.getName())) {
				int rank = skillMap.getOrDefault(s.getName(), skillMin);
				if(rank < skillMin ) {skillMin = rank;skill = s;} }  }
		if(skill == null) {
			skillMin = 100;
			for(Skill s : skillList) {
				if(!this.skillIgnoreList.contains(s.getName())) {
					int rank = skillMap.getOrDefault(s.getName(), skillMin);
					if(rank < skillMin ) {skillMin = rank;skill = s; } } } }
		
		if(skill == null) {
			
			int index = (int)(Math.random()*((skillsVisited.size()-1 - 0) + 1));
			String s = this.skillsVisited.get(index);
			skill = skillRepository.findByName(s).get(0);
		}
		if(!this.skillsVisited.contains(skill.getName())) {this.skillsVisited.add(skill.getName());}

		return skill;
	}
	private String pickCourse(HashMap<Integer, List<String>> commMap,
			HashMap<String, Integer> courseMap, int community) {
		List<String> courseList = commMap.get(community);
		int courseMin = 100;  
		String courseId = "0";
		for(String course : courseList) {
			if(!this.coursesVisited.contains(course)) {
				int rank = courseMap.getOrDefault(course,courseMin);
				if(rank < courseMin ) {
					courseMin = rank;
					courseId = course;} } }
		if(courseId.equals("0")) {
			int index = (int)(Math.random()*((this.coursesVisited.size()-1 - 0) + 1));
			courseId = this.coursesVisited.get(index); }
		return courseId;
	}




	
	
}
