// https://introcs.cs.princeton.edu/java/97data/Complex.java.html

public class Complex {
    private double real;
    private double imaginary;

    public Complex(double x, double y) {
        //System.out.println("Constructor: " + x + " " + y);
        this.real = x;
        this.imaginary = y;

    }

    public Complex() {
        this.real = 0;
        this.imaginary = 0;
    }

    public double abs() {
        return Math.hypot(real, imaginary);
    }

    public Complex plus(Complex b) {
        Complex a = this;
        double real = a.real + b.real;
        double imag = a.imaginary + b.imaginary;
        return new Complex(real, imag);
    }

    public Complex minus(Complex b) {
        Complex a = this;
        double real = a.real - b.real;
        double imag = a.imaginary - b.imaginary;
        return new Complex(real, imag);
    }

    // return a new Complex object whose value is (this * b)
    public Complex times(Complex b) {
        Complex a = this;
        double real = a.real * b.real - a.imaginary * b.imaginary;
        double imag = a.real * b.imaginary + a.imaginary * b.real;
        return new Complex(real, imag);
    }

    // return a new object whose value is (this * alpha)
    public Complex scale(int i, int j) {
        Complex c = new Complex(this.real * i, this.imaginary * j);
        return c;
    }

    // return a string representation of the invoking Complex object
    public String toString() {
        if (imaginary == 0) return real + "";
        if (real == 0) return imaginary + "i";
        if (imaginary <  0) return real + " - " + (-imaginary) + "i";
        return real + " + " + imaginary + "i";
    }

    public double getReal() {
        return this.real;
    }

    public double getImaginary() {
        return this.imaginary;
    }

}
