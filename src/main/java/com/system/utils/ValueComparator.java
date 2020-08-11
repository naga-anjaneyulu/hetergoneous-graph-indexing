package com.system.utils;

import java.util.Comparator;
import java.util.TreeMap;

public class ValueComparator implements Comparator<String>{
	 
	TreeMap<String, Integer> map = new TreeMap<String, Integer>();
	 
		public ValueComparator(TreeMap<String, Integer> skillTree){
			this.map.putAll(skillTree);
		}
	 
		@Override
		public int compare(String s1, String s2) {
			if(map.get(s1) >= map.get(s2)){
				return -1;
			}else{
				return 1;
			}	
		}

	}