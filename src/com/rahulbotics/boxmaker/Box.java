/*
 * Class to represent a box
 *
 */
package com.rahulbotics.boxmaker;

public class Box {

    double width, height, depth;

    double thickness;
    double kerf;

    int numNotchesW, numNotchesH, numNotchesD;
    double notchLengthW, notchLengthH, notchLengthD;

    double notchLength; // XXX

    /* Initialize a box */
    public Box(double _w, double _h, double _d,
               double _t, double _k, double _n) {
        width = _w;
        height = _h;
        depth = _d;
        thickness = _t;
        kerf = _k;
        notchLength = _n;

        /* Enlarge the box to compensate for cut width */
        width += kerf;
        height += kerf;
        depth += kerf;

        /* Calculate the number of notches to make and actual notch lengths */
        numNotchesW = closestOddTo(width / notchLength);
        numNotchesH = closestOddTo(height / notchLength);
        numNotchesD = closestOddTo(depth / notchLength);

        notchLengthW = width / numNotchesW;
        notchLengthH = height / numNotchesH;
        notchLengthD = depth / numNotchesD;

        /* Recalculate the dimensions to correct for precision errors ??? */
        width  = numNotchesW * notchLengthW;
        height = numNotchesH * notchLengthH;
        depth  = numNotchesD * notchLengthD;
    }

    private static int closestOddTo(double numd){
		int num = (int)(numd + 0.5);
		if (num % 2 == 0) return (num - 1);
		return num;
    }

}
