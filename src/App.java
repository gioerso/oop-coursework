import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.function.Function;

public class App extends JFrame {

    enum Task {
        LAB1("Задание 1. Стратегия", lab1::new),
        LAB2("Задание 2. Аннотации", main.Lab02::new),
        LAB3("Задание 3. Переводчик", Lab3::new),
        LAB4("Задание 4. Stream API", Lab4::new);

        final String title;
        final Function<PrintWriter, JPanel> input;

        Task(String title, Function<PrintWriter, JPanel> input) {
            this.title = title;
            this.input = input;
        }

        @Override public String toString() { return title; }
    }

    public App() {
        super("Курсовая работа");

        CardLayout cards = new CardLayout();
        JPanel pages = new JPanel(cards);
        for (Task t : Task.values()) pages.add(page(t), t.name());

        JList<Task> list = new JList<>(Task.values());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBorder(BorderFactory.createTitledBorder("Задания"));
        list.addListSelectionListener(e -> {
            if (list.getSelectedValue() != null) cards.show(pages, list.getSelectedValue().name());
        });
        list.setSelectedIndex(0);

        add(list, BorderLayout.WEST);
        add(pages, BorderLayout.CENTER);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
    }

    // страница задания: сверху ввод, ниже вывод в области, защищённой от редактирования
    private static JPanel page(Task task) {
        JTextArea output = new JTextArea();
        output.setEditable(false);
        output.setLineWrap(true);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JScrollPane scroll = new JScrollPane(output);
        scroll.setBorder(BorderFactory.createTitledBorder("Вывод"));

        JButton clear = new JButton("Очистить вывод");
        clear.addActionListener(e -> output.setText(""));

        JPanel page = new JPanel(new BorderLayout(5, 5));
        page.setBorder(BorderFactory.createTitledBorder(task.title));
        page.add(task.input.apply(writerTo(output)), BorderLayout.NORTH);
        page.add(scroll, BorderLayout.CENTER);
        page.add(clear, BorderLayout.SOUTH);
        return page;
    }

    // поток вывода в текстовую область вместо System.out
    private static PrintWriter writerTo(JTextArea area) {
        return new PrintWriter(new Writer() {
            @Override public void write(char[] buf, int off, int len) {
                area.append(new String(buf, off, len).replace("\r", ""));
                area.setCaretPosition(area.getDocument().getLength());
            }

            @Override public void flush() { }
            @Override public void close() { }
        });
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}
