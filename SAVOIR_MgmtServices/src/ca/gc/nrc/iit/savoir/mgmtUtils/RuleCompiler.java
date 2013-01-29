// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.mgmtUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.io.ResourceFactory;

/**
 * Utility class to compile a rulebase to its corresponding binary.
 * 
 * @author Aaron Moss
 */
public class RuleCompiler {

	/**
	 * Compiles the rules in the file specified by the given name to a binary.
	 * This binary is the serialized form of the collection of knowledge 
	 * packages stored in that file.
	 * 
	 * @param filename	The name of the file to compile. Will output to a file 
	 * 					of the same name, with ".bin" appended
	 * 
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 * @throws KnowledgeBuilderException 
	 */
	public static void compileRules(String filename) 
			throws FileNotFoundException, IOException, 
			KnowledgeBuilderException {
		String binname = filename + ".bin";
		writeKnowledgePackages(binname, compileRulefile(filename));
	}
	
	/**
	 * Loads knowledge base from serialized binary of knowledge packages.
	 * 
	 * @param filename	The filename to load from
	 * 
	 * @return the stored knowledge base
	 * 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws ClassCastException 
	 * @throws FileNotFoundException 
	 * @throws KnowledgeBuilderException 
	 */
	public static KnowledgeBase loadRules(String filename) 
			throws FileNotFoundException, ClassCastException, 
			IOException, ClassNotFoundException {
		return genKnowledgeBase(readKnowledgePackages(
				new FileInputStream(filename)));
	}
	
	/**
	 * Runs the Drools compiler to generate KnowledgePackages from a rulefile
	 * 
	 * @param filename		The name of the rulefile
	 * 
	 * @return The KnowledgePackages derived from that rulefile
	 * 
	 * @throws KnowledgeBuilderException on compile errors
	 */
	public static Collection<KnowledgePackage> compileRulefile(String filename) 
			throws KnowledgeBuilderException {
		//load rule file
		KnowledgeBuilder kbuilder = 
			KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newFileResource(filename), 
				ResourceType.DRL);
		
		//check for load errors
		if (kbuilder.hasErrors()) {
			//report and abort
			throw new KnowledgeBuilderException(kbuilder.getErrors());
		}
		
		//return packages
		return kbuilder.getKnowledgePackages();
	}
	
	/**
	 * Generates a new knowledge base from a collection of knowledge packages
	 * 
	 * @param kpkgs		The knowledge packages
	 * 
	 * @return a new knowledge base 
	 */
	public static KnowledgeBase genKnowledgeBase(
			Collection<KnowledgePackage> kpkgs) {
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kpkgs);
		return kbase;
	}
	
	/**
	 * Writes a collection of knowledge packages to file
	 * 
	 * @param filename	The name of the file to write
	 * @param kpkgs		The knowledge packages to write
	 * 
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	public static void writeKnowledgePackages(String filename, 
			Collection<KnowledgePackage> kpkgs) 
			throws FileNotFoundException, IOException {
		ObjectOutputStream oos = null;
		try {
			//get output stream
			oos = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(filename)));
			
			//write objects
			oos.writeObject(kpkgs);
		} finally {
			if (oos != null) oos.close();
		}
	}
	
	/**
	 * Reads a collection of knowledge packages from a file
	 * 
	 * @param filename		The name of the file to read
	 * 
	 * @return the knowlege packages stored in that file
	 * 
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 * @throws ClassCastException 
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public static Collection<KnowledgePackage> readKnowledgePackages(
			InputStream inputStream) 
			throws FileNotFoundException, IOException, ClassCastException, 
			ClassNotFoundException {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new BufferedInputStream(inputStream));
			return (Collection<KnowledgePackage>)ois.readObject();
		} finally {
			if (ois != null) ois.close();
		}
	}
	
	/**
	 * Runs the rule compiler.
	 * 
	 * @param args	A list of files to compile
	 */
	public static void main(String[] args) {
		for (String filename : args) {
			System.out.println("Compiling `" + filename + "'");
			try {
				compileRules(filename);
				System.out.println("\tSuccess");
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}
}
