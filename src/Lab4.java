import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Lab4 extends JPanel {

    public static OptionalDouble average(List<Integer> ints) {
        return ints.stream()
                .mapToInt(Integer::intValue)
                .average();
    }

    public static List<String> upperWithPrefix(List<String> src) {
        return src.stream()
                .map(s -> "_new_" + s.toUpperCase(Locale.ROOT))
                .toList();
    }

    public static List<Integer> squaresOfUniques(List<Integer> ints) {
        return ints.stream()
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() == 1)
                .map(e -> e.getKey() * e.getKey())
                .toList();
    }

    public static <T> T lastOrThrow(Collection<T> coll) {
        return coll.stream()
                .reduce((prev, next) -> next)
                .orElseThrow(() -> new NoSuchElementException("Collection is empty"));
    }

    public static int sumEven(int[] arr) {
        return Arrays.stream(arr)
                .filter(n -> n % 2 == 0)
                .sum();
    }

    public static Map<Character, String> toMapByFirstChar(List<String> src) {
        return src.stream()
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toMap(
                        s -> s.charAt(0),
                        s -> s.substring(1),
                        (old, last) -> last,
                        LinkedHashMap::new));
    }

    private final PrintWriter out;
    private final JPanel names = new JPanel(new GridLayout(0, 1, 0, 5));
    private final JPanel inputs = new JPanel(new GridLayout(0, 1, 0, 5));
    private final JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 5));

    public Lab4(PrintWriter out) {
        super(new BorderLayout(5, 5));
        this.out = out;

        row("average", "среднее значение целых чисел", "1 2 5",
                s -> average(ints(s)).orElse(Double.NaN));
        row("upperWithPrefix", "верхний регистр и префикс _new_", "ab cde",
                s -> upperWithPrefix(words(s)));
        row("squaresOfUniques", "квадраты чисел, встречающихся один раз", "1 2 2 3 4 4",
                s -> squaresOfUniques(ints(s)));
        row("lastOrThrow", "последний элемент коллекции", "20 10 50",
                s -> lastOrThrow(words(s)));
        row("sumEven", "сумма чётных чисел массива", "1 2 3 4 5",
                s -> sumEven(words(s).stream().mapToInt(Integer::parseInt).toArray()));
        row("toMapByFirstChar", "Map: первый символ - остаток строки", "aaw x aBB",
                s -> toMapByFirstChar(words(s)));

        add(new JLabel("Входные данные методов: элементы через пробел или запятую"), BorderLayout.NORTH);
        add(names, BorderLayout.WEST);
        add(inputs, BorderLayout.CENTER);
        add(buttons, BorderLayout.EAST);
    }

    // строка метода: название, поле входных данных, кнопка выполнения
    private void row(String name, String hint, String sample, Function<String, Object> method) {
        JTextField input = new JTextField(sample);
        JButton run = new JButton("Выполнить");
        run.addActionListener(e -> {
            try {
                out.printf("%s(%s) -> %s%n", name, input.getText().strip(), method.apply(input.getText()));
            } catch (NumberFormatException ex) {
                out.println(name + ": нужны целые числа");
            } catch (NoSuchElementException ex) {
                out.println(name + ": ошибка " + ex.getMessage());
            }
        });
        names.add(new JLabel(name + " — " + hint));
        inputs.add(input);
        buttons.add(run);
    }

    // разбор входных данных на элементы
    private static List<String> words(String s) {
        return Arrays.stream(s.split("[\\s,]+"))
                .filter(w -> !w.isEmpty())
                .toList();
    }

    private static List<Integer> ints(String s) {
        return words(s).stream()
                .map(Integer::valueOf)
                .toList();
    }
}
