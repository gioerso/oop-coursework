package main;

import java.io.PrintWriter;
import java.util.Arrays;

// Класс с public/protected/private методами

enum Mode { EASY, HARD }

class ExampleService {
    private final PrintWriter out; // вывод в текстовую область

    ExampleService(PrintWriter out) { this.out = out; }

    public void announce(String msg)       { out.println("announce: " + msg); }
    public int add(int a, int b)           { return a + b; }
    public String join(String a, String b) { return a + "-" + b; }

    // protected (будут вызваны, т.к. аннотированы)
    @Repeat(3)
    protected int compute(double x, int... ns) {
        return (int) Math.round(x) + Arrays.stream(ns).sum();
    }

    @Repeat(2)
    protected void shout(char c, int n) {
        out.println(String.valueOf(c).repeat(n));
    }

    // private (будут вызваны, т.к. аннотированы)
    @Repeat(2)
    private void secret(int a, String s) {
        out.println("secret: " + a + ", " + s);
    }

    @Repeat(4)
    private String cfg(Boolean flag, Mode mode) {
        return switch (mode) {
            case EASY -> "easy, flag=" + flag;
            case HARD -> "hard, flag=" + flag;
        };
    }
}
