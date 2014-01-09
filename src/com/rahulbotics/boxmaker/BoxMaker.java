package com.rahulbotics.boxmaker;

import java.io.FileNotFoundException;

import com.lowagie.text.DocumentException;

import org.apache.commons.cli.*;

import com.rahulbotics.boxmaker.Box;

/**
 * Simple wrapper to let me run the renderer from the command line
 * @author rahulb
 */
public class BoxMaker {

    String filePath = null;

    double width = 0;
    double height = 0;
    double depth = 0;

    double thickness = 0;
    double kerf = 0;
    double notchLength = 0;

    boolean drawBoundingBox = false;
    boolean internalDimensions = false;
    boolean inMetric = false;

    /**
     * Run the rendered with lots of args
     * 
     * @param args
     */
    public static void main(String[] args) {
        BoxMaker bm = new BoxMaker();

        if (!bm.parseOptions(args)) {
            System.exit(1);
        }

        bm.build();

        System.exit(0);
    }

    /**
     * Construct the arguments for the actual rendering function
     */
    private void build() {
        if (notchLength == 0) {
            /* Default notch length to 2.5x material thickness */
            notchLength = thickness * 2.5;
        }

        if (internalDimensions) {
            /*
             * The specified dimensions are internal, add double the material
             * thickness to each dimension.
             */
            System.out.println("Converting from interior to exterior dimensions");
            width  += thickness * 2;
            height += thickness * 2;
            depth  += thickness * 2;
        }
        
        if (inMetric) {
            /* Convert all units from inches into millimeters */
            width       *= Renderer.INCH_PER_MM;
            height      *= Renderer.INCH_PER_MM;
            depth       *= Renderer.INCH_PER_MM;
            thickness   *= Renderer.INCH_PER_MM;
            kerf    *= Renderer.INCH_PER_MM;
            notchLength *= Renderer.INCH_PER_MM;
        }

        /* Construct the box */
        Box box = new Box(width, height, depth,
                          thickness, kerf, notchLength);
        
        // try to render it, don't do any error handling (file won't get created)
        try {
            Renderer.render(filePath, box,
                            drawBoundingBox, !inMetric);
        } catch (FileNotFoundException e) {
            System.out.println("ERROR!" + e.toString());
            System.exit(1);
        } catch (DocumentException e) {
            System.out.println("ERROR!" + e.toString());
            System.exit(1);
        }
    }

    /**
     * Generate the command line parsing options
     */
    private Options constructOptions() {
        Options opts = new Options();

        opts.addOption("h", "help", false, "Print options help");

        opts.addOption("i", "internal", false,
                       "Specified dimensions are internal");
        opts.addOption("m", "metric", false,
                       "Specified dimensions are in millimeters");
        opts.addOption("b", "boundingbox", false,
                       "Draw bounding box");

        opts.addOption("f", "file", true, "Output file");

        opts.addOption("W", "width", true, "width of box");
        opts.addOption("H", "height", true, "height of box");
        opts.addOption("D", "depth", true, "depth of box");

        opts.addOption("T", "thickness", true, "Material thickness");
        opts.addOption("k", "kerf", true, "Cut width");
        opts.addOption("n", "notchlength", true, "Notch length");
        
        return opts;
    }

    private void printOptionsHelp(Options opts) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Options: ", opts);
        System.exit(1);
    }

         
    /**
     * Parse the command line options
     */
    private boolean parseOptions(final String[] args)
    {
        final CommandLineParser parser = new GnuParser();
        final Options opts = constructOptions();
        CommandLine commandLine = null;

        try {
            commandLine = parser.parse(opts, args);

            if (commandLine.hasOption("help")) {
                printOptionsHelp(opts);
                return false;
            }

            if (commandLine.hasOption("internal")) {
                internalDimensions = true;
            }

            if (commandLine.hasOption("metric")) {
                inMetric = true;
            }

            if (commandLine.hasOption("boundingbox")) {
                drawBoundingBox = true;
            }

            if (commandLine.hasOption("file")) {
                filePath = commandLine.getOptionValue("file");
            } else {
                System.err.println("Must specify output file");
                printOptionsHelp(opts);
            }

            if (commandLine.hasOption("width")) {
                width =
                    Double.parseDouble(commandLine.getOptionValue("width"));
            } else {
                System.err.println("Must specify width");
                printOptionsHelp(opts);
            }

            if (commandLine.hasOption("height")) {
                height =
                    Double.parseDouble(commandLine.getOptionValue("height"));
            } else {
                System.err.println("Must specify height");
                printOptionsHelp(opts);
            }

            if (commandLine.hasOption("depth")) {
                depth =
                    Double.parseDouble(commandLine.getOptionValue("depth"));
            } else {
                System.err.println("Must specify depth");
                printOptionsHelp(opts);
            }

            if (commandLine.hasOption("thickness")) {
                thickness =
                    Double.parseDouble(commandLine.getOptionValue("thickness"));
            } else {
                System.err.println("Must specify material thickness");
                printOptionsHelp(opts);
            }

            if (commandLine.hasOption("kerf")) {
                kerf =
                    Double.parseDouble(commandLine.getOptionValue("kerf"));
            }

            if (commandLine.hasOption("notchlength")) {
                notchLength =
                    Double.parseDouble(commandLine.getOptionValue("notchlength"));
            }

        } catch (ParseException parseException) // checked exception
        {
            System.err.println(
                "Encountered exception while parsing using GnuParser:\n"
                + parseException.getMessage() );
            printOptionsHelp(opts);

            return false;
        }

        return true;
    }
    
}
