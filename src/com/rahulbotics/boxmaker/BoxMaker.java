package com.rahulbotics.boxmaker;

import java.io.FileNotFoundException;

import com.lowagie.text.DocumentException;

import org.apache.commons.cli.*;

/**
 * Simple wrapper to let me run the renderer from the command line
 * @author rahulb
 */
public class BoxMaker {

    String filePath = null;

    double mmWidth = 0;
    double mmHeight = 0;
    double mmDepth = 0;

    double mmThickness = 0;
    double mmCutWidth = 0;
    double mmNotchLength = 0;

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
        if (mmNotchLength == 0) {
            /* Default notch length to 2.5x material thickness */
            mmNotchLength = mmThickness * 2.5;
        }

        if (internalDimensions) {
            /*
             * The specified dimensions are internal, add double the material
             * thickness to each dimension.
             */
            System.out.println("Converting from interior to exterior dimensions");
            mmWidth  += mmThickness * 2;
            mmHeight += mmThickness * 2;
            mmDepth  += mmThickness * 2;
        }
        
        if (inMetric) {
            /* Convert all units from inches into millimeters */
            mmWidth       *= Renderer.INCH_PER_MM;
            mmHeight      *= Renderer.INCH_PER_MM;
            mmDepth       *= Renderer.INCH_PER_MM;
            mmThickness   *= Renderer.INCH_PER_MM;
            mmCutWidth    *= Renderer.INCH_PER_MM;
            mmNotchLength *= Renderer.INCH_PER_MM;
        }
        
        // try to render it, don't do any error handling (file won't get created)
        try {
            Renderer.render(filePath,
                            mmWidth, mmHeight, mmDepth,
                            mmThickness, mmCutWidth, mmNotchLength,
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

        opts.addOption("W", "width", true, "Width of box");
        opts.addOption("H", "height", true, "Height of box");
        opts.addOption("D", "depth", true, "Depth of box");

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
                mmWidth =
                    Double.parseDouble(commandLine.getOptionValue("width"));
            } else {
                System.err.println("Must specify width");
                printOptionsHelp(opts);
            }

            if (commandLine.hasOption("height")) {
                mmHeight =
                    Double.parseDouble(commandLine.getOptionValue("height"));
            } else {
                System.err.println("Must specify height");
                printOptionsHelp(opts);
            }

            if (commandLine.hasOption("depth")) {
                mmDepth =
                    Double.parseDouble(commandLine.getOptionValue("depth"));
            } else {
                System.err.println("Must specify depth");
                printOptionsHelp(opts);
            }

            if (commandLine.hasOption("thickness")) {
                mmThickness =
                    Double.parseDouble(commandLine.getOptionValue("thickness"));
            } else {
                System.err.println("Must specify material thickness");
                printOptionsHelp(opts);
            }

            if (commandLine.hasOption("kerf")) {
                mmCutWidth =
                    Double.parseDouble(commandLine.getOptionValue("kerf"));
            }

            if (commandLine.hasOption("notchlength")) {
                mmNotchLength =
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
