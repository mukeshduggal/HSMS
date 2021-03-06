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
 * RotatePDFPages.java
 * ---------------
 */
package org.jpedal.examples.viewer.gui.popups;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.print.attribute.standard.PageRanges;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import org.jpedal.examples.viewer.gui.GUI;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;


public class RotatePDFPages extends Save
{

	JLabel OutputLabel = new JLabel();
	final ButtonGroup buttonGroup1 = new ButtonGroup();
	ButtonGroup buttonGroup2 = new ButtonGroup();
	
	final JToggleButton jToggleButton3 = new JToggleButton();
	
	final JToggleButton jToggleButton2 = new JToggleButton();
	
	final JRadioButton printAll=new JRadioButton();
	final JRadioButton printCurrent=new JRadioButton();
	final JRadioButton printPages=new JRadioButton();
	
	final JTextField pagesBox=new JTextField();

	final String[] rotationItems = {Messages.getMessage("PdfViewerComboBox.Clockwise90"),Messages.getMessage("PdfViewerComboBox.CounterClockwise90")
,Messages.getMessage("PdfViewerComboBox.180Degrees")};
	
	final JLabel direction = new JLabel(Messages.getMessage("PdfViewerMessage.Direction"));
	final JComboBox directionBox = new JComboBox(rotationItems);
	public RotatePDFPages( final String root_dir, final int end_page, final int currentPage )
	{
		super(root_dir, end_page, currentPage);

		try
		{
			jbInit();
		}
		catch( final Exception e )
		{
			e.printStackTrace();
		}
	}

	///////////////////////////////////////////////////////////////////////
	/**
	 * get root dir
	 */
    public final int[] getRotatedPages()
	{
		
		int[] pagesToExport=null;
		
		if(printAll.isSelected()){
			pagesToExport=new int[end_page];
			for(int i=0;i<end_page;i++) {
                pagesToExport[i] = i + 1;
            }

		}else if( printCurrent.isSelected() ){
			pagesToExport=new int[1];
			pagesToExport[0]=currentPage;
			
		}else if( printPages.isSelected() ){
			
			try{
				final PageRanges pages=new PageRanges(pagesBox.getText());
				
				int count=0;
				int i = -1;
				while ((i = pages.next(i)) != -1) {
                    count++;
                }
				
				pagesToExport=new int[count];
				count=0;
				i = -1;
				while ((i = pages.next(i)) != -1){
					if(i > end_page){
                        if(GUI.showMessages) {
                            JOptionPane.showMessageDialog(this, Messages.getMessage("PdfViewerText.Page") +
                                    ' ' + i + ' ' + Messages.getMessage("PdfViewerError.OutOfBounds") + ' ' +
                                    Messages.getMessage("PdfViewerText.PageCount") + ' ' + end_page);
                        }
							
						return null;
					}
					pagesToExport[count]=i;
					count++;
				}
			}catch (final IllegalArgumentException  e) {
				LogWriter.writeLog( "Exception " + e + " in exporting pdfs" );
                if(GUI.showMessages) {
                    JOptionPane.showMessageDialog(this, Messages.getMessage("PdfViewerError.InvalidSyntax"));
                }
			}
		}
		
		return pagesToExport;

	}
	
	public int getDirection(){
		return directionBox.getSelectedIndex();
	}
	
	private void jbInit() throws Exception
	{
		
		direction.setFont( new java.awt.Font( "Dialog", Font.BOLD, 14 ) );
		direction.setDisplayedMnemonic( '0' );
		direction.setBounds( new Rectangle( 13, 13, 220, 26 ) );
		
		directionBox.setBounds( new Rectangle( 23, 40, 300, 23 ) );
		
		pageRangeLabel.setText(Messages.getMessage("PdfViewerPageRange.text"));
		pageRangeLabel.setBounds( new Rectangle( 13, 71, 199, 26 ) );
		
		printAll.setText(Messages.getMessage("PdfViewerRadioButton.All"));
		printAll.setBounds( new Rectangle( 23, 100, 75, 22 ) );
		
		printCurrent.setText(Messages.getMessage("PdfViewerRadioButton.CurrentPage"));
		printCurrent.setBounds( new Rectangle( 23, 120, 100, 22 ) );
		printCurrent.setSelected(true);
		
		printPages.setText(Messages.getMessage("PdfViewerRadioButton.Pages"));
		printPages.setBounds( new Rectangle( 23, 142, 70, 22 ) );
		
		pagesBox.setBounds( new Rectangle( 95, 142, 200, 22 ) );
		pagesBox.addKeyListener(new KeyListener(){
			@Override
            public void keyPressed(final KeyEvent arg0) {}

			@Override
            public void keyReleased(final KeyEvent arg0) {
				if(pagesBox.getText().isEmpty()) {
                    printCurrent.setSelected(true);
                } else {
                    printPages.setSelected(true);
                }
				
			}

			@Override
            public void keyTyped(final KeyEvent arg0) {}
		});

		final JTextArea pagesInfo=new JTextArea(Messages.getMessage("PdfViewerMessage.PageNumberOrRange")+ '\n' +
				Messages.getMessage("PdfViewerMessage.PageRangeExample"));
		pagesInfo.setBounds(new Rectangle(23,185,400,40));
		pagesInfo.setOpaque(false);
				
		optionsForFilesLabel.setBounds( new Rectangle( 13, 220, 199, 26 ) );
		
		this.add( printAll, null );
		this.add( printCurrent, null );
		
		this.add( printPages, null );
		this.add( pagesBox, null );
		this.add( pagesInfo, null );
		
		this.add( directionBox, null );
		this.add( direction, null );
		this.add( changeButton, null );
		this.add( pageRangeLabel, null );
		
		this.add( jToggleButton2, null );
		this.add( jToggleButton3, null );
		
		buttonGroup1.add( printAll );
		buttonGroup1.add( printCurrent );
		buttonGroup1.add( printPages );
	}
	
	@Override
    public final Dimension getPreferredSize()
	{
		return new Dimension( 400, 250 );
	}
	
}
