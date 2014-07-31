/*
01234567890123456789012345678901234567890123456789012345678901234567890123456789
 *	A wrapper for PSGr to draw boxes.  Initialize, then call drawBox!
 *
 *	2004.03.10 (rahulb)
 *		- created, gleaning code out of BoxMaker.java

*/

package com.rahulbotics.boxmaker;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

import com.rahulbotics.boxmaker.Box;
 
/**
 * Handles actually drawing of the notched box to a file.  This class passes everything around
 * in millimeters until it actually draws it at the low level.  It renders a files like this:
 * <pre>
 *               ----------
 *               |  w x d |
 *               ----------
 *               ----------
 *               |  w x h |
 *               |        |
 *               ----------
 *    ---------  ----------  ---------
 *    | d x h |  |  w x d |  | d x h |
 *    ---------  ----------  ---------
 *               ----------
 *               |  w x h |
 *               |        |
 *               ----------
 * </pre>
 *  
 * @author rahulb
 */
public class Renderer {

	// how many millimeters in one inch
	static final double MM_PER_INCH = 25.4f;
	// how many inches in one millimeter
    static final double INCH_PER_MM = 0.0393700787f;
    // the standard display DPI of the pdf (not the same as printing resolution to a pdf)
    static final double DPI = 72.0f;

    // the PDF document created
    private Document doc;
    // the writer underneath the PDF document, which we need to keep a reference to
    private PdfWriter docPdfWriter;
    // the path that we are writing the file to
    private String filePath; 

    /**
     * Public method to render and save a box.
     *  
     * @param filePath			the full absolute path to save the file to
     * @param box               the dox with all needed dimensions
     * @param drawBoundingBox	draw an outer edge with a dimension (for easier DXF import)
     * @param specifiedInInches the user specified the box in inches?
     *
     * @throws FileNotFoundException
     * @throws DocumentException
     */
    public static void render(String filePath, Box box,
                              boolean drawBoundingBox,
                              boolean specifiedInInches) 
        throws FileNotFoundException, DocumentException {

        Renderer myRenderer = new Renderer(filePath);
    	myRenderer.drawAllSides(box, drawBoundingBox, specifiedInInches);
    	myRenderer.closeDoc();
    }
    
	/**
	 * Create a new renderer (doesn't actually do anything)
	 * @param pathToSave	the full absolute path to save the file to
	 */
	private Renderer(String pathToSave){
    	filePath = pathToSave;
    }
    
    /**
     * Create the document to write to (needed before any rendering can happen).
     * @param widthMm	the width of the document in millimeters
     * @param heightMm	the height of the document in millimeters
     * @throws FileNotFoundException
     * @throws DocumentException
     */
    private void openDoc(double widthMm,double heightMm) throws FileNotFoundException, DocumentException{
		double docWidth = widthMm*DPI;
		double docHeight = heightMm*DPI;
		//System.out.println("doc = "+docWidth+" x "+docHeight);
    	doc = new Document(new Rectangle((float)docWidth, (float)docHeight));
		docPdfWriter = PdfWriter.getInstance(doc,new FileOutputStream(filePath));
		String appNameVersion = BoxMakerConstants.APP_NAME+" "+BoxMakerConstants.VERSION;
		doc.addAuthor(appNameVersion);
		doc.open();
		doc.add(new Paragraph(
                    "Produced by "+BoxMakerConstants.APP_NAME+" "+BoxMakerConstants.VERSION+"\n"+
                    "  on "+new Date()+"\n"+BoxMakerConstants.WEBSITE_URL )
                );
    }

    /**
     * Draw a bounding box around the whole thing.
     * 
     * @param margin	the offset to draw the box (in millimeters)
     * @param widthMM	the width of the box to draw (in millimeters)
     * @param heightMM	the height of the box to draw (in millimeters)
     * @throws DocumentException 
     */
    private void drawBoundingBox(double margin,double widthMM, double heightMM, boolean specifiedInInches) 
        throws DocumentException {
    	drawBoxByMm(margin, margin, widthMM, heightMM);
		if(specifiedInInches) {
            doc.add(new Paragraph("Bounding box (in): "+widthMM+" x "+heightMM));
		} else {
		    doc.add(new Paragraph("Bounding box (mm): "+widthMM * MM_PER_INCH+" x "+heightMM * MM_PER_INCH));
		}
	}

    /**
     * Close up the document (writing it to disk)
     */
    private void closeDoc(){
		doc.close();    	
    }
    
	/**
	 * Math utility function
	 * @param numd	a number
	 * @return		the closest odd number to the one passed in
	 */
	private static int closestOddTo(double numd){
		int num=(int) (numd+0.5);
		if(num % 2 == 0) return num-1;
		return num;
    }
	
    /**
     * Actually draw all the faces of the box
     * @param box               the box
     * @param drawBoundingBox 	draw an outer edge with a dimension (for easier DXF import)
     * @param specifiedInInches the user specified the box in inches?
     * @throws FileNotFoundException
     * @throws DocumentException
     */
    public void drawAllSides(Box box,
                             boolean drawBoundingBox,
                             boolean specifiedInInches) 
        throws FileNotFoundException, DocumentException{

        double margin = 0.5;
			
		//initialize the eps file
		double boxPiecesWidth = (box.depth * 2 + box.width);		// based on layout of pieces
		double boxPiecesHeight = (box.height * 2 + box.depth * 2); // based on layout of pieces
		openDoc((double) (boxPiecesWidth+margin*4),(double) (boxPiecesHeight+margin*5));
        if(specifiedInInches) {
            doc.add(new Paragraph("Width (in): "+box.width));
            doc.add(new Paragraph("Height (in): "+box.height));
            doc.add(new Paragraph("Depth (in): "+box.depth));
            doc.add(new Paragraph("Thickness (in): "+box.thickness));
            doc.add(new Paragraph("Notch Length (in): "+box.notchLength));
            doc.add(new Paragraph("Cut Width (in): "+box.kerf));        
        } else {
            doc.add(new Paragraph("Width (mm): "+box.width * MM_PER_INCH));
            doc.add(new Paragraph("Height (mm): "+box.height * MM_PER_INCH));
            doc.add(new Paragraph("Depth (mm): "+box.depth * MM_PER_INCH));
            doc.add(new Paragraph("Thickness (mm): "+box.thickness * MM_PER_INCH));
            doc.add(new Paragraph("Notch Length (mm): "+box.notchLength * MM_PER_INCH));
            doc.add(new Paragraph("Cut Width (mm): "+box.kerf * MM_PER_INCH));        
        }
		if(drawBoundingBox) drawBoundingBox(margin,boxPiecesWidth+margin*2,boxPiecesHeight+margin*3,specifiedInInches);

		//start the drawing phase
		double xOrig = 0;
		double yOrig = 0;
	
		// compensate for the cut width (in part) by increasing mwidth (eolson)
		// no, don't do that, because the cut widths cancel out. (eolson)
		//	    mwidth+=box.kerf/2; 

		//1. a W x H side (the back)
		xOrig = box.depth + margin*2;
		yOrig = margin;
		drawHorizontalLine(box, xOrig,yOrig,box.notchLengthW,box.numNotchesW,box.kerf/2,false,false, false);					//top
		drawHorizontalLine(box, xOrig,yOrig+box.height-box.thickness,box.notchLengthW,box.numNotchesW, box.kerf/2,true,false, false);	//bottom
		drawVerticalLine(box, xOrig,yOrig,box.notchLengthH,box.numNotchesH, box.kerf/2,false,false);					//left
		drawVerticalLine(box, xOrig+box.width-box.thickness,yOrig,box.notchLengthH,box.numNotchesH, -box.kerf/2,false,false);	//right
		
		//2. a D x H side (the left side)
		xOrig = margin;
		yOrig = box.height + margin*2;
		drawHorizontalLine(box, xOrig,yOrig,box.notchLengthD,box.numNotchesD, box.kerf/2,false,false, false);					//top
		drawHorizontalLine(box, xOrig,yOrig+box.height-box.thickness,box.notchLengthD,box.numNotchesD, box.kerf/2,true,false, false);	//bottom
		drawVerticalLine(box, xOrig,yOrig,box.notchLengthH,box.numNotchesH, box.kerf/2,false,false);					//left
		drawVerticalLine(box, xOrig+box.depth-box.thickness,yOrig,box.notchLengthH,box.numNotchesH, -box.kerf/2,false,false);	//right
		
		//3. a W x D side (the bottom)
		xOrig = box.depth + margin*2;
		yOrig = box.height + margin*2;
		drawHorizontalLine(box, xOrig,yOrig,box.notchLengthW,box.numNotchesW, -box.kerf/2,true,true, false);				//top
		drawHorizontalLine(box, xOrig,yOrig+box.depth-box.thickness,box.notchLengthW,box.numNotchesW, -box.kerf/2,false,true, false);	//bottom
		drawVerticalLine(box, xOrig,yOrig,box.notchLengthD,box.numNotchesD, -box.kerf/2,true,true);				//left
		drawVerticalLine(box, xOrig+box.width-box.thickness,yOrig,box.notchLengthD,box.numNotchesD, -box.kerf/2,false,true);	//right

		//4. a D x H side (the right side)
		xOrig = box.depth + box.width + margin*3;
		yOrig = box.height + margin*2;
		drawHorizontalLine(box, xOrig,yOrig,box.notchLengthD,box.numNotchesD, box.kerf/2,false,false, false);					//top
		drawHorizontalLine(box, xOrig,yOrig+box.height-box.thickness,box.notchLengthD,box.numNotchesD, box.kerf/2,true,false, false);	//bottom
		drawVerticalLine(box, xOrig,yOrig,box.notchLengthH,box.numNotchesH, box.kerf/2,false,false);					//left
		drawVerticalLine(box, xOrig+box.depth-box.thickness,yOrig,box.notchLengthH,box.numNotchesH, -box.kerf/2,false,false);	//right

		//5. a W x H side (the front)
		xOrig = box.depth + margin*2;
		yOrig = box.height + box.depth+ margin*3;
		drawHorizontalLine(box, xOrig,yOrig,box.notchLengthW,box.numNotchesW, box.kerf/2,false,false, false);					//top
		drawHorizontalLine(box, xOrig,yOrig+box.height-box.thickness,box.notchLengthW,box.numNotchesW, box.kerf/2,true,false, false);	//bottom
		drawVerticalLine(box, xOrig,yOrig,box.notchLengthH,box.numNotchesH, box.kerf/2,false,false);					//left
		drawVerticalLine(box, xOrig+box.width-box.thickness,yOrig,box.notchLengthH,box.numNotchesH, -box.kerf/2,false,false);	//right
		
		//6. a W x D side (the top)
		xOrig = box.depth + margin*2;
		yOrig = box.height*2 + box.depth + margin*4;
		drawHorizontalLine(box, xOrig,                     yOrig,                     box.notchLengthW, box.numNotchesW, -box.kerf/2, true,  true, true); //top
		drawHorizontalLine(box, xOrig,                     yOrig + box.depth - box.thickness, box.notchLengthW, box.numNotchesW, -box.kerf/2, false, true, true);	//bottom
		drawVerticalLine  (box, xOrig,                     yOrig,                     box.notchLengthD, box.numNotchesD, -box.kerf/2, true,  true); //left
		drawVerticalLine  (box, xOrig + box.width - box.thickness, yOrig,                     box.notchLengthD, box.numNotchesD, -box.kerf/2, false, true);	//right
    }

    private void drawNotchedLine(Box box,
                                 double x0, double y0,
                                 double notchWidth,
                                 int notchCount,
                                 double cutwidth,
                                 boolean flip, boolean smallside,
                                 boolean tabs){
    	double x = x0, y = y0;
    	System.out.println("Horizonal side: "+notchCount+" steps @ ( "+x0+" , "+y0+" )");
    	
        for (int step = 0; step < notchCount; step++)
        {
            double height = box.thickness;
            // XXX - Figure this bit out - if (tabs && step == 1) height *= 1.5;

            y = (((step % 2) == 0) ^ flip) ? y0 : y0 + height;
	
			if (step == 0) {		//start first edge in the right place
			    if (smallside) {
                    drawLineByMm(x + height, y,
                                 x + notchWidth + cutwidth, y);
                } else {
                    drawLineByMm(x, y,
                                 x + notchWidth + cutwidth, y);
                }
			} else if (step == (notchCount - 1)) {	//shorter last edge
			    drawLineByMm(x - cutwidth, y,
                             x + notchWidth - height, y);
			} else if (step % 2 == 0) {
			    drawLineByMm(x - cutwidth, y,
                             x + notchWidth + cutwidth, y);
		    } else {
			    drawLineByMm(x + cutwidth, y,
                             x + notchWidth - cutwidth, y);
		    }
			
			if (step < (notchCount - 1)) {
			    if (step % 2 == 0) {
					drawLineByMm(x + notchWidth + cutwidth, y0 + height,
                                 x + notchWidth + cutwidth, y0);
			    } else {
					drawLineByMm(x + notchWidth - cutwidth, y0 + height,
                                 x + notchWidth - cutwidth, y0);
			    }
			}
			
			x = x + notchWidth;
		}
    }



	/**
     * Draw one horizontal notched line
     * @param x0			x-coord of the starting point of the line (lower left corner) 
     * @param y0			y-coord of the starting point of the line (lower left corner)
     * @param notchWidth	the width of each notch to draw in millimeters
     * @param notchCount	the number of notches to draw along the edge
     * @param notchHeight	the height of the notches to draw (the material thickness)
     * @param cutwidth		the width of the laser beam to compensate for
     * @param flip			should the first line (at x0,y0) be out or in
     * @param smallside		should this stop short of the full height or not
     * @param tabs          should include tabs for opening
     */
    private void drawHorizontalLine(Box box,
                                    double x0, double y0,
                                    double notchWidth,
                                    int notchCount,
                                    double cutwidth,
                                    boolean flip, boolean smallside,
                                    boolean tabs){
    	double x = x0, y = y0;
    	System.out.println("Horizonal side: "+notchCount+" steps @ ( "+x0+" , "+y0+" )");
    	
        for (int step = 0; step < notchCount; step++)
        {
            double height = box.thickness;
            // XXX - Figure this bit out - if (tabs && step == 1) height *= 1.5;

            y = (((step % 2) == 0) ^ flip) ? y0 : y0 + height;
	
			if (step == 0) {		//start first edge in the right place
			    if (smallside) {
                    drawLineByMm(x + height, y,
                                 x + notchWidth + cutwidth, y);
                } else {
                    drawLineByMm(x, y,
                                 x + notchWidth + cutwidth, y);
                }
			} else if (step == (notchCount - 1)) {	//shorter last edge
			    drawLineByMm(x - cutwidth, y,
                             x + notchWidth - height, y);
			} else if (step % 2 == 0) {
			    drawLineByMm(x - cutwidth, y,
                             x + notchWidth + cutwidth, y);
		    } else {
			    drawLineByMm(x + cutwidth, y,
                             x + notchWidth - cutwidth, y);
		    }
			
			if (step < (notchCount - 1)) {
			    if (step % 2 == 0) {
					drawLineByMm(x + notchWidth + cutwidth, y0 + height,
                                 x + notchWidth + cutwidth, y0);
			    } else {
					drawLineByMm(x + notchWidth - cutwidth, y0 + height,
                                 x + notchWidth - cutwidth, y0);
			    }
			}
			
			x = x + notchWidth;
		}
    }

    /**
     * Draw one vertical notched line
     * @param x0			x-coord of the starting point of the line (lower left corner) 
     * @param y0			y-coord of the starting point of the line (lower left corner)
     * @param notchWidth	the width of each notch to draw in millimeters
     * @param notchCount	the number of notches to draw along the edge
     * @param notchHeight	the height of the notches to draw (the material thickness)
     * @param cutwidth		the width of the laser beam to compensate for
     * @param flip			should the first line (at x0,y0) be out or in
     * @param smallside		should this stop short of the full height or not
     */
    private void drawVerticalLine(Box box,
                                  double x0, double y0,
                                  double notchWidth, int notchCount,
                                  double cutwidth,
                                  boolean flip, boolean smallside){
		double x=x0,y=y0;
        System.out.println("Vertical side: "+notchCount+" steps @ ( "+x0+" , "+y0+" )");
	
		for (int step=0;step<notchCount;step++) {
			x=(((step%2)==0)^flip) ? x0 : x0+box.thickness;
	
			if (step==0) {
				if(smallside) drawLineByMm(x,y+box.thickness,x,y+notchWidth+cutwidth);
			    else drawLineByMm(x,y,x,y+notchWidth+cutwidth);
			} else if (step==(notchCount-1)) {
			    //g.moveTo(x,y+cutwidth); g.lineTo(x,y+notchWidth); g.stroke();
				if(smallside) drawLineByMm(x,y-cutwidth,x,y+notchWidth-box.thickness);
			    else drawLineByMm(x,y-cutwidth,x,y+notchWidth); 
			} else if (step%2==0) {
			    drawLineByMm(x,y-cutwidth,x,y+notchWidth+cutwidth);
			} else {
			    drawLineByMm(x,y+cutwidth,x,y+notchWidth-cutwidth);
			}
			
			if (step<(notchCount-1)) {
			    if (step%2==0) {
			    	drawLineByMm(x0+box.thickness,y+notchWidth+cutwidth,x0,y+notchWidth+cutwidth);
			    } else {
			    	drawLineByMm(x0+box.thickness,y+notchWidth-cutwidth,x0,y+notchWidth-cutwidth);
			    }
			}
			y=y+notchWidth;
		}
    }

    /**
     * Low-level function to draw lines
     * @param fromXmm	start x pos on age (in millimeters)
     * @param fromYmm	start y pos on age (in millimeters)
     * @param toXmm		end x pos on age (in millimeters)
     * @param toYmm		end y pos on age (in millimeters)
     */
    private void drawLineByMm(double fromXmm,double fromYmm,double toXmm,double toYmm){
    	PdfContentByte cb = docPdfWriter.getDirectContent();
		cb.setLineWidth(0f);
		float x0 = (float)(DPI*fromXmm);
		float y0 = (float)(DPI*fromYmm);
    	cb.moveTo(x0, y0);
    	float x1 = (float)(DPI*toXmm);
    	float y1 = (float)(DPI*toYmm);
    	cb.lineTo(x1, y1);
    	cb.stroke();
    	System.out.println(" Line  - ( "+x0+" , "+y0+" ) to ( "+x1+" , "+y1+" )");
    }

    /**
     * Draw a rectangle with based on the endpoints passed in
     * @param fromXmm
     * @param fromYmm
     * @param toXmm
     * @param toYmm
     */
    private void drawBoxByMm(double fromXmm,double fromYmm,double toXmm,double toYmm){
     	PdfContentByte cb = docPdfWriter.getDirectContent();
		cb.setLineWidth(0f);
		float x0 = (float)(DPI*fromXmm);
		float y0 = (float)(DPI*fromYmm);
    	float x1 = (float)(DPI*toXmm);
    	float y1 = (float)(DPI*toYmm);
    	cb.rectangle(x0, y0, x1, y1);
    	cb.stroke();
    	System.out.println(" Box  - ( "+x0+" , "+y0+" ) to ( "+x1+" , "+y1+" )");
    }    
}
