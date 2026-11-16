package main;

import javax.swing.*;
import java.io.PrintWriter;

public class Lab02 extends JPanel {
    public Lab02(PrintWriter out) {
        JButton run = new JButton("Выполнить");
        run.addActionListener(e -> main.Invoker.invokeAnnotated(new ExampleService(out), out));

        add(new JLabel("Вызов protected/private методов ExampleService, аннотированных @Repeat:"));
        add(run);
    }
}
