/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/support/
 *
 * (C) Copyright 1997-2015 IDRsolutions and Contributors.
 *
 * This file is part of JPedal/JPDF2HTML5
 *
 
 *
 * ---------------
 * FindTextInRectangle.java
 * ---------------
 */

package org.jpedal.examples.text;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.jpedal.PdfDecoderInt;
import org.jpedal.PdfDecoderServer;
import org.jpedal.exception.PdfException;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.grouping.SearchType;
import org.jpedal.objects.PdfPageData;

/**
 *
 * <h2><b>Sample Code Showing how JPedal Library can be used with
 * PDF to Find Text from a Specified Rectangle.</b></h2>
 * 
 * <p>XML file of the output.</p>
 *
 * <p><b>Debugging tip: Set verbose=true in LogWriter to see what is going on.</b></p>
 *
 * <p>It can run from jar directly using the command:
 *
 * <br><b>java -cp libraries_needed org/jpedal/examples/text/FindTextInRectangle inputValues</b></p>
 *
 * <p>Where inputValues are:</p>
 * <ul>
 * <li>-c (optional):	This optional flag prevents the results of the search being shown on the console.</li>
 * <li>Search Path:	    The filename (including the path if needed) or a directory containing files. If it contains spaces it must be enclosed by double quotes (ie "C:/Path with spaces/").</li>
 * <li>Target string:	This is the unicode text string to find.</li>
 * <li>XML file path (optional):	The path to where an optional XML file of the search results will be created, overwriting an existing file if it already exists.</li>
 * </ul>
 * <p><a href="http://www.idrsolutions.com/how-to-search-a-pdf-file-for-text/">See our Support Pages for more information on Text Searching.</a></p>
 */
public class FindTextInRectangle
{
    /**return value for testing*/
    ArrayList co_ords;

    /**page to search*/
    int page = -1;
    
    /**area to search*/
    int[] areaToScan;

    /**correct separator for OS */
    final String separator = System.getProperty("file.separator");

    /**the decoder object which decodes the pdf and returns a data object*/
    PdfDecoderInt decodePdf;

    /**word to find*/
    private final String textToFind;

    /** xml output file*/
    private File xmlOutputFile;
    private static String xmlOutputPath;

    /**Output control flags.  Default settings*/
    private static boolean enableXML;
    private static boolean enableSTDout = true;

    /**example method to open a file and extract the raw text*/
    public FindTextInRectangle(final String file_name, final String textToFind)
    {
        this.textToFind=textToFind;
        findText(file_name);
    }

    /**example method to open a file and extract the raw text*/
    public FindTextInRectangle(final String file_name, final String textToFind, final int[] areaToScan)
    {
        this.textToFind=textToFind;
        this.areaToScan=areaToScan;
        findText(file_name);
    }

    /**example method to open a file and extract the raw text*/
    public FindTextInRectangle(final String file_name, final String textToFind, final int page)
    {
        this.textToFind=textToFind;
        this.page = page;
        findText(file_name);
    }
    
    /**example method to open a file and extract the raw text*/
    public FindTextInRectangle(final String file_name, final String textToFind, final int[] areaToScan, final int page)
    {
        this.textToFind=textToFind;
        this.areaToScan=areaToScan;
        this.page = page;
        findText(file_name);
    }
    private void findText(String file_name)
    {
        createXMLFile(true);

        co_ords = new ArrayList();

        /**
         * if file name ends pdf, do the file otherwise
         * do every pdf file in the directory. We already know file or
         * directory exists so no need to check that, but we do need to
         * check its a directory
         */
        final File targetFile = new File(file_name);

        if (file_name.toLowerCase().endsWith(".pdf")) {
            decodeFile(file_name);
        }
        else if(targetFile.isDirectory()) {
            //get list of files and check directory
            final String[] files = targetFile.list();

            //make sure name ends with a deliminator for correct path later
            if (!file_name.endsWith(separator)) {
                file_name += separator;
            }

            //now work through all pdf files
            final long fileCount = files.length;

            for (int i = 0; i < fileCount; i++) {
                if(enableSTDout) {
                    System.out.println("File "+ i + " of " + fileCount + ' ' + files[i]);
                }

                if (files[i].toLowerCase().endsWith(".pdf")) {
                    if(enableSTDout) {
                        System.out.println(file_name + files[i]);
                    }
                    decodeFile(file_name + files[i]);
                }
            }
        }
        else {
            System.err.println(file_name + " is not a directory. Exiting program");
        }

        //close XML file
        createXMLFile(false);
    }

    /**
     * routine to decode a file
     */
    private void decodeFile(final String file_name)
    {
        /**debugging code to create a log
         LogWriter.setupLogFile(true,0,"","v",false);
         LogWriter.log_name =  "/mnt/shared/log.txt";
         /***/

        createFileXMLElement(file_name, true);

        //PdfDecoder returns a PdfException if there is a problem
        try {
            decodePdf = new PdfDecoderServer(false);
            decodePdf.setExtractionMode(PdfDecoderServer.TEXT); //extract just text
            PdfDecoderServer.init(true);
            //make sure widths in data CRITICAL if we want to split lines correctly!!

            /**
             * open the file (and read metadata including pages in  file)
             */
            if(enableSTDout) {
                System.out.println("Opening file: " + file_name);
            }
            decodePdf.openPdfFile(file_name);
        }
        catch (final PdfException e) {
            System.err.println("Ignoring " + file_name);
            System.err.println("Due to: " + e);
            createFileXMLElement(file_name, false);
            return;
        }

        /**
         * extract data from pdf (if allowed).
         */
        if ((decodePdf.isEncrypted()&&(!decodePdf.isPasswordSupplied())) && (!decodePdf.isExtractionAllowed())) {
            System.out.println("Encrypted settings");
            System.out.println(
                    "Please look at Viewer for code sample to handle such files");
            System.out.println("Or get support/consultancy");
        }
        else {
            //page range
            int start = 1, end = decodePdf.getPageCount();

            //Single page search requested
            if(page!=-1){
            	start = page;
            	end = page;
            }
            
            /**
             * extract data from pdf
             */
            try {
                for (int currentPage = start; currentPage <= end; currentPage++) { //read pages

                    if(enableSTDout) {
                        System.out.println("=========================");
                        System.out.println("Page "+currentPage);
                        System.out.println("=========================");
                    }

                    //decode the page
                    decodePdf.decodePage(currentPage);

                    /** create a grouping object to apply grouping to data*/
                    final PdfGroupingAlgorithms currentGrouping =decodePdf.getGroupingObject();
                    if(currentGrouping!=null){

                        final int x1;
                        final int y1;
                        final int x2;
                        final int y2;

                        /**use whole page size for  demo - get data from PageData object unless set*/
                        if(areaToScan==null){
                            final PdfPageData currentPageData = decodePdf.getPdfPageData();
                            x1 = currentPageData.getMediaBoxX(currentPage);
                            x2 = currentPageData.getMediaBoxWidth(currentPage)+x1;

                            y2 = currentPageData.getMediaBoxY(currentPage);
                            y1 = currentPageData.getMediaBoxHeight(currentPage)+y2;
                        }else{
                            x1=areaToScan[0];
                            y1=areaToScan[1];
                            x2=areaToScan[2];
                            y2=areaToScan[3];
                        }
                        //tell user
                        if(enableSTDout) {
                            System.out.println(
                                    "Scanning for text ("+textToFind+") rectangle ("
                                            + x1
                                            + ','
                                            + y1
                                            + ' '
                                            + x2
                                            + ','
                                            + y2
                                            + ')');
                        }

                        /**Co-ordinates are x1,y1 (top left hand corner), x2,y2(bottom right) */

                        /**co-ords for start of object are returned in float object.
                         * if not found co-ords=null
                         * if found co_ords[0]=x1, co_ords[1]=y
                         */
                        final float[] co_ords;

                        try{
                            co_ords =currentGrouping.findText(
                                    new String[]{textToFind},
                                    SearchType.MUTLI_LINE_RESULTS);

                            this.co_ords.add(co_ords);

                        } catch (final PdfException e) {
                            decodePdf.closePdfFile();
                            System.err.println("Ignoring " + file_name);
                            System.err.println("Due to: " + e);
                            createFileXMLElement(file_name, false);
                            return;
                        }

                        if (co_ords == null) {
                            if(enableSTDout) {
                                System.out.println("Text not found on page.");
                            }
                        }
                        else {
                            if(enableSTDout) {
                                System.out.println("Found " + (co_ords.length/5) + " on page.");
                            }
                            for(int i = 0; i <co_ords.length; i+=5) {
                                if(enableSTDout) {
                                    System.out.println("Text found at "+co_ords[i]+", "+co_ords[i+1]);
                                }
                                createFindXMLElement(co_ords[i],co_ords[i+1],currentPage);
                            }
                        }
                    }
                }

                //remove data once written out
                decodePdf.flushObjectValues(false);

            }
            catch (final Exception e) {
                decodePdf.closePdfFile();
                System.err.println("Exception: " + e.getMessage());
                createFileXMLElement(file_name, false);
                return;
            }

            /**
             * flush data structures - not strictly required but included
             * as example
             */
            decodePdf.flushObjectValues(true); //flush any text data read

            /**tell user*/
            if(enableSTDout) {
                System.out.println("File read...");
            }

        }

        //Close file xml element
        createFileXMLElement(file_name, false);

        /**close the pdf file*/
        decodePdf.closePdfFile();
    }

    /**
     * Get the coordinates returned for the entire page.
     * If no coords found then empty array is returned.
     * Coords for page are stored at index page-1
     * The origin of the coords is the bottom left hand corner (on unrotated page) organised in the following order.<br>
     * [0]=result x1 coord<br>
     * [1]=result y1 coord<br>
     * [2]=result x2 coord<br>
     * [3]=result y2 coord<br>
     * [4]=either -101 to show that the next text area is the remainder of this word on another line else any other value is ignored.<br>
     *
     * @return Vector containing all coords of text found on page
     */
    @SuppressWarnings("UnusedDeclaration")
    public ArrayList getCoords()
    {
        return co_ords;
    }

    /**
     * Return the coords for the page specified.
     * The origin of the coords is the bottom left hand corner (on unrotated page) organised in the following order.<br>
     * [0]=result x1 coord<br>
     * [1]=result y1 coord<br>
     * [2]=result x2 coord<br>
     * [3]=result y2 coord<br>
     * [4]=either -101 to show that the next text area is the remainder of this word on another line else any other value is ignored.<br>
     *
     * @param page :: Page number to check for results
     * @return float[] containing all coords for the page, or empty array is no results found
     */
    public float[] getCoords(final int page)
    {
        return (float[]) co_ords.get(page - 1);
    }

    /**
     * @param open True to create a new XML file.  False to close tags on the new file.
     */
    public void createXMLFile(final boolean open){
        if(enableXML) {
            if(open) {
                xmlOutputFile = new File(xmlOutputPath);

                if(xmlOutputFile.exists()) {
                    xmlOutputFile.delete();
                    try {
                        xmlOutputFile.createNewFile();
                    }
                    catch (final Exception e) {
                        enableXML = false;
                        System.err.println("Unable to create XML file: " + e + '\n');
                    }
                }

                if(enableXML) {
                    try {
                        final PrintWriter outputStream = new PrintWriter(new FileWriter(xmlOutputFile));
                        outputStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                        outputStream.println("<search>");
                        outputStream.println("<term>" + textToFind + "</term>");
                        outputStream.close();
                    }
                    catch (final Exception e) {
                        enableXML = false;
                        System.err.print("Failed to write to XML file: " + e + '\n');
                    }
                }
            }
            else {
                try {
                    final PrintWriter outputStream = new PrintWriter(new FileWriter(xmlOutputFile, true));
                    outputStream.println("</search>");
                    outputStream.close();
                }
                catch (final Exception e) {
                    System.err.print("Exception creating closing XML file: " + e + '\n');
                }
            }
        }
    }

    /**
     * @param filePath The contents of the files path element
     * @param open True if a the opening of file element is required, otherwise close a file element
     */
    public void createFileXMLElement(final String filePath, final boolean open)
    {
        if(enableXML) {
            try {
                final PrintWriter outputStream = new PrintWriter(new FileWriter(xmlOutputFile, true));
                if(open) {
                    outputStream.println("<file>");
                    outputStream.println("<path>" + filePath + "</path>");
                }
                else {
                    outputStream.println("</file>");
                }
                outputStream.close();
            }
            catch (final Exception e) {
                System.out.print("Creating new outputFile: " + e);
            }
        }
    }

    public void createFindXMLElement(final float x, final float y, final int pageNo)
    {
        if(enableXML) {
            try {
                final PrintWriter outputStream = new PrintWriter(new FileWriter(xmlOutputFile, true));
                outputStream.println("<found>");
                outputStream.println("<pageNo>" + pageNo + "</pageNo>");
                outputStream.println("<x>" + x + "</x>");
                outputStream.println("<y>" + y + "</y>");
                outputStream.println("</found>");
                outputStream.close();
            }
            catch (final Exception e) {
                System.out.print("Creating new outputFile: " + e);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////
    /**
     * main routine which checks for any files passed and runs the demo
     */
    public static void main(final String[] args) {

        String searchPath = null;
        String textToFind = null;
        int parameterCount = 0;

        //check user has passed us a filename, -c can be anywhere but other parameters have to be in order
        if(args.length >1 && args.length<=4) {
            for (final String arg : args) {
                if (arg.toLowerCase().equals("-c")) {
                    enableSTDout = false;
                } else {
                    switch (parameterCount) {
                        case 0:
                            searchPath = arg;
                            break;
                        case 1:
                            textToFind = arg;
                            break;
                        case 2:
                            xmlOutputPath = arg;
                            enableXML = true;
                            break;
                    }
                    parameterCount++;
                }
            }
        }

        if(parameterCount<2 || parameterCount>3) {
            System.out.println("Usage: FindTextInRectangle input string [xmlFile] [-c]");
            System.out.println("\t\tinput\tThe pdf file or directory you wish to search.");
            System.out.println("\t\tstring\tThe string to search for (Use quotes if it contains spaces).");
            System.out.println("\nOptional parameters:");
            System.out.println("\txmlOutput\tThe name you want to give the XML search output file");
            System.out.println("\t[-c]\t\tadd -c to suppress output to console");
            //@exit
            System.exit(1);
        }

        if(enableSTDout) {
            System.out.println("Search Target: " + searchPath + " Searching for: " + textToFind);
            if(enableXML) {
                System.out.println("XML File: " + xmlOutputPath);
            }
        }

        //check file exists
        final File pdf_file = new File(searchPath);

        //if file exists, open and get number of pages
        if (!pdf_file.exists()) {
            System.out.println("File " + searchPath + " not found");
            //@exit
            System.exit(1);
        }
        else {
            new FindTextInRectangle(searchPath,textToFind);
        }
    }
}
