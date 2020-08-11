package com.system.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.types.Node;


import com.system.model.Course;
import com.system.model.Skill;


public class GraphRepository implements AutoCloseable{
	
	private final Driver driver;

    public GraphRepository( String uri, String user, String password )
    {
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }

    @Override
    public void close() throws Exception
    {
        driver.close();
    }
    
    
   public String generateCommunityQuery(List<Course> courses , List<Skill> skills ) {
    	
    	//String courses = "'WITH [\"+courses.get(0) +"\",\""+courses+"\", \"EXPLORATORY DATA ANALYSIS\",\"THE SOCIAL AND ORGANIZATIONAL INFORMATICS OF BIG DATA\",\"APPLIED MACHINE LEARNING\",\"DATA FLUENCY\"]
    	
    	String query = "CALL algo.louvain.stream('WITH [\""+courses.get(0).getName() + "\",\""+courses.get(1).getName()+"\", \""+courses.get(2).getName()+"\",\""+courses.get(3).getName()+"\",\""+courses.get(4).getName()+"\",\""+courses.get(5).getName()+"\",\""+courses.get(6).getName()+"\",\""+courses.get(7).getName()+"\",\""+courses.get(8).getName()+"\",\""+courses.get(9).getName()+"\"] as names MATCH (c:Course)-[:preCourse]->(c1:Course) WHERE c.name in names and c1.name in names WITH collect(c1) as community "+
    	"WITH [\""+ skills.get(0).getName()+"\",\""+skills.get(1).getName()+"\",\""+skills.get(2).getName()+"\",\""+skills.get(3).getName()+"\",\""+skills.get(4).getName()+"\",\""+skills.get(5).getName()+"\",\""+skills.get(6).getName()+"\",\""+skills.get(7).getName()+"\",\""+skills.get(8).getName()+"\",\""+skills.get(9).getName()+"\"] as skills ,community as community MATCH p=(c2:Course)-[:gainSkill]->(s:Skill) WHERE c2 in community  and s.name in skills with collect(c2) as courses,collect(s) as skills WITH courses as courses,skills as skills  MATCH (c:Course)-[r:gainSkill]->(s:Skill) where c in courses and s in skills  RETURN id(c) as id ',' MATCH (c1:Course)-[:preCourse]->(c) RETURN id(c1) as source,id(c) as target ',"+
        		"{graph:'cypher',includeIntermediateCommunities: true, weightProperty: 'weight'}) "+
        		" YIELD nodeId, community,communities RETURN DISTINCT algo.asNode(nodeId) as node, community LIMIT 20;";
    	
		return query;
    	
    	
    	
    }

    public HashMap<Integer, List<String>> getCourseCommunities(String query)
    {	
    	//System.out.println(query);
    	
    	HashMap<Integer,List<String>> commMap = new HashMap<Integer,List<String>>();
    	
        try ( Session session = driver.session() )
        {
        	List<Record> communities = session.writeTransaction( new TransactionWork<List<Record>>()
            {
                @Override
                public List<Record> execute( Transaction tx )
                {
                    Result result = tx.run(query);
                    return result.list();
                }
            } );
            for(Record com : communities) {
            	Node node =  com.get("node").asNode();
            	int community = com.get("community").asInt();
            	String label = ((List<String>) node.labels()).get(0);
            	Map<String, Object> mappedNode = node.asMap();
            	
            	if(commMap.containsKey(community)) {
            		List<String> courseList = commMap.get(community);
            		courseList.add((String) mappedNode.get("courseId"));
            		commMap.put(community, courseList);
            	}else {
            		
            		List<String> courseList = new ArrayList<String>();
            		courseList.add((String) mappedNode.get("courseId"));
            		commMap.put(community, courseList);
            		
            	}
            	
   
            	//System.out.println(mappedNode.get("name"));
            	//System.out.println(mappedNode.get("courseId"));
            	
            }
            
            
        }
    
        	return commMap;
}
    
    public static void main( String... args ) throws Exception
    {
        try ( GraphRepository graph = new GraphRepository( "bolt://localhost:7687", "neo4j", "idontknow.3" ) )
        {
        	graph.getCourseCommunities("sdfs");
        }
    }
    
    
    
    
    
}

