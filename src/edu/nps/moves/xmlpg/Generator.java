package edu.nps.moves.xmlpg;

import java.io.*;
import java.util.*;

/**
 * Abstract superclass for all the concrete language generators, such as java, c++, etc.
 *
 * @author DMcG
 */

public abstract class Generator 
{
    /** Contains abstract descriptions of all the classes, key = name, value = object
    */
    protected HashMap classDescriptions;
    
    /** Directory in which to write the class code */
    public String  directory;
    
    protected Properties languageProperties;
    
    /**
     * Constructor
     */
    public Generator(HashMap pClassDescriptions, Properties pLanguageProperties)
    {
        classDescriptions = pClassDescriptions;
        languageProperties = pLanguageProperties;

        // Directory is set in the subclasses

        /*
        try
        {
            directory = (String)languageProperties.getProperty("directory");
        }
        catch(Exception e)
        {
            System.out.println("Missing language property, probably the directory in which the source code should be placed");
            System.out.println("add directory = aDir in the properties for the language");
            System.out.println(e);
        }
         
         */
    }
    
    /**
     * Overridden by the subclasses to generate the code specific to that language.
     */
    public abstract void writeClasses();
    
    /**
     * Create the directory in which to put the generated source code files
     */
    protected void createDirectory()
    {
        System.out.println("creating directory");
        
        boolean success = (new File(this.getDirectory())).mkdirs();
        
    }

    /**
     * Directory in which to write the class code
     * @return the directory
     */
    public String getDirectory()
    {
        return directory;
    }

    /**
     * Directory in which to write the class code
     * @param directory the directory to set
     */
    public void setDirectory(String directory)
    {
        this.directory = directory;
    }
    

}
