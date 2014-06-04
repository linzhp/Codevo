package edu.ucsc.codevo.fixtures;

import java.util.Iterator;
import java.util.List;

import edu.ucsc.cs.netEvo.CodeEntity;
import edu.ucsc.cs.netEvo.SourceFileAnalyzer;

/**
 * Hello world!
 *
 */
public class App 
{
	int mode;
	
	App() {
		this("abc");
	}
	
	App(String s) {
	}
    public static void main( String[] args )
    {
    	SourceFileAnalyzer s;
    	com.google.common.base.Joiner joiner;
    	com.google.common.collect.Lists.asList("a", "b");
    	edu.ucsc.cs.netEvo.CodeEntity[] nodes;
    	s = new SourceFileAnalyzer(100);
    	List<CodeEntity> v = s.vertices;
        System.out.println( "Hello World!" );
    }
    
    class Component {
    	void process() {
    		
    	}
    }
}
