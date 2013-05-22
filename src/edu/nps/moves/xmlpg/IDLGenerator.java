package edu.nps.moves.xmlpg;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class IDLGenerator extends Generator {
	Properties types = new Properties();
	String xmlFileName;

	class IDLClass {
		GeneratedClass c_;
		List<String> dependencies_ = new LinkedList<String>();
		boolean printed_ = false;
		
		public IDLClass(GeneratedClass c){
			c_ = c;
			
			if(c_.getClassAttributes().size() == 0){
				ClassAttribute dummy = new ClassAttribute();
				dummy.setAttributeKind(ClassAttribute.ClassAttributeType.PRIMITIVE);
				dummy.setType("short");
				dummy.setName("dummy");
				c_.getClassAttributes().add(dummy);
			}
		}
		
		boolean printed = false;

		public String print() {
			if(printed == true)
				return "";
			printed = true;
			processDependencies(getAllAttributes());
			String toReturn = "";
			for(Iterator<String> it = dependencies_.iterator(); it.hasNext();){
				IDLClass dependency = (IDLClass) IDLGenerator.this.classDescriptions.get(it.next());
				toReturn += dependency.print();
			}
			toReturn += toString();
			return toReturn;
		}
		
		public String toString(){
			String toReturn = "";
			if (c_.getClassComments() != null)
				toReturn += "// " + c_.getClassComments() + "\n";
			toReturn += "struct " + c_.getName() + " {\n";
			toReturn += printAttributes();
			toReturn += "};\n";
			return toReturn;
		}
		
		public List<ClassAttribute> getAllAttributes(){
			List<ClassAttribute> toReturn = new LinkedList<ClassAttribute>();
			toReturn.addAll(getParentAttributes());
			toReturn.addAll(getAttributes());
			return toReturn;
		}
						
		public List<ClassAttribute> getAttributes(){
			return new LinkedList<ClassAttribute>(c_.getClassAttributes());
		}
		
		public List<ClassAttribute> getParentAttributes(){
			List<ClassAttribute> toReturn = new LinkedList<ClassAttribute>();
			if(!c_.getParentClass().equalsIgnoreCase("root")){
				IDLClass parent = (IDLClass)IDLGenerator.this.classDescriptions.get(c_.getParentClass());
				toReturn.addAll(parent.getParentAttributes());
				toReturn.addAll(parent.getAttributes());
			}
			return toReturn;
		}
		
		private void processDependencies(List<ClassAttribute> ivars){
			for (int idx = 0; idx < ivars.size(); idx++) {
				ClassAttribute anAttribute = ivars.get(idx);
				if (anAttribute.getAttributeKind() == ClassAttribute.ClassAttributeType.CLASSREF)
					dependencies_.add(anAttribute.getType());
				if (anAttribute.getAttributeKind() == ClassAttribute.ClassAttributeType.FIXED_LIST && anAttribute.getUnderlyingTypeIsPrimitive() == false) 
					dependencies_.add(anAttribute.getType());
				if (anAttribute.getAttributeKind() == ClassAttribute.ClassAttributeType.VARIABLE_LIST && anAttribute.getUnderlyingTypeIsPrimitive() == false)
					dependencies_.add(anAttribute.getType());
			}
		}
		
		private String printAttributes() {
			String toReturn = "";
			List ivars = getAllAttributes();
			for (int idx = 0; idx < ivars.size(); idx++) {
				ClassAttribute anAttribute = (ClassAttribute) ivars.get(idx);

				if (anAttribute.getComment() != null) 
					toReturn += "\t// " + anAttribute.getComment() + "\n";;
							
				if (anAttribute.getAttributeKind() == ClassAttribute.ClassAttributeType.PRIMITIVE) {
					String attributeType = types.getProperty(anAttribute.getType());
					toReturn += "\t"+attributeType + "  " + anAttribute.getName() + ";\n";
				}

				if (anAttribute.getAttributeKind() == ClassAttribute.ClassAttributeType.CLASSREF) {
					String attributeType = anAttribute.getType();
					toReturn += "\t"+attributeType + "  " + anAttribute.getName() + ";\n";
					//dependencies_.add(anAttribute.getType());;
				}

				if ((anAttribute.getAttributeKind() == ClassAttribute.ClassAttributeType.FIXED_LIST)) {
					String attributeType = anAttribute.getType();
					int listLength = anAttribute.getListLength();
					String listLengthString = (new Integer(listLength)).toString();
					if (anAttribute.getUnderlyingTypeIsPrimitive() == true) {
						toReturn += "\t"+types.getProperty(attributeType) + " " + anAttribute.getName() + "["+listLengthString+"];\n";
					} else if (anAttribute.listIsClass() == true) {
						toReturn += "\t"+attributeType + " " + anAttribute.getName() + "["+listLengthString+"];\n";
						//dependencies_.add(anAttribute.getType());
					}
				}
				
				// The attribute is a variable list of some kind.
				if ((anAttribute.getAttributeKind() == ClassAttribute.ClassAttributeType.VARIABLE_LIST)) {
					String attributeType = anAttribute.getType();
					int listLength = anAttribute.getListLength();

					if (anAttribute.getUnderlyingTypeIsPrimitive() == true) {
						toReturn += "\t sequence<"+types.getProperty(attributeType) + "> " + anAttribute.getName() + ";\n";
					} else if (anAttribute.listIsClass() == true) {
						toReturn += "\t sequence<"+attributeType + "> " + anAttribute.getName() + ";\n";
						//dependencies_.add(anAttribute.getType());
					}
				}
			} // End of loop through ivars
			return toReturn;
		}
		
	}
	
	
	
	public IDLGenerator(String xmlDescriptionFileName,
			HashMap pClassDescriptions, Properties pLanguageProperties) {
		super(pClassDescriptions, pLanguageProperties);

		xmlFileName = xmlDescriptionFileName;

		Properties systemProperties = System.getProperties();
		String directory = null;
		String clDirectory = systemProperties
				.getProperty("xmlpg.generatedSourceDir");
		String clModule = systemProperties.getProperty("xmlpg.module");

		// Directory to place generated source code
		if (clDirectory != null)
			pLanguageProperties.setProperty("directory", clDirectory);

		// Module for generated code
		if (clModule != null)
			pLanguageProperties.setProperty("module", clModule);

		System.out.println(pLanguageProperties);
		super.setDirectory(pLanguageProperties.getProperty("directory"));

		types.setProperty("unsigned short", "unsigned short");
		types.setProperty("unsigned byte", "unsigned short");
		types.setProperty("unsigned int", "unsigned long");
		types.setProperty("unsigned long", "unsigned long long");

		types.setProperty("byte", "octet");
		types.setProperty("short", "short");
		types.setProperty("int", "long");
		types.setProperty("long", "long long");

		types.setProperty("double", "double");
		types.setProperty("float", "float");

		for(Iterator<String>it = classDescriptions.keySet().iterator(); it.hasNext(); ){
			String key = it.next();
			classDescriptions.put(key, new IDLClass((GeneratedClass)classDescriptions.get(key)));
		}
	}
	
	
	@Override
	public void writeClasses() {
		System.out.println("Creating IDL source code.");
		// this.createDirectory();
		Iterator it = classDescriptions.values().iterator();

		// Create package structure, if any
		String fullPath = getDirectory()
				+ "/"
				+ xmlFileName.split("/")[xmlFileName.split("/").length - 1]
						.split("\\.")[0] + ".idl"; // FIXME

		try {
			File outputFile = new File(fullPath);
			outputFile.getParentFile().mkdirs();
			outputFile.createNewFile();
			PrintWriter pw = new PrintWriter(outputFile);

			pw.println("module DIS {");
			
			while (it.hasNext())
				pw.print(((IDLClass)it.next()).print());
						
			pw.println("};");

			pw.flush();
			pw.close();
			
		} catch (Exception e) {
			System.out.println("error creating source code " + e);
		}
	}
}
