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
		run();
	}
	
	void run() {
		
	}
    public static void main( String[] args )
    {
    	SourceFileAnalyzer s;
    	java.io.File file;
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
