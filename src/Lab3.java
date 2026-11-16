import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.*;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.*;

public class Lab3 extends JPanel {
    private static final String DICT_PATH = "dict.txt";

    private final PrintWriter out;
    private final JTextField dictPath = new JTextField(DICT_PATH);
    private final JTextField textPath = new JTextField();
    private final JTextArea text = new JTextArea(6, 40);

    public Lab3(PrintWriter out) {
        super(new BorderLayout(5, 5));
        this.out = out;

        JButton chooseDict = new JButton("Выбрать...");
        JButton chooseText = new JButton("Выбрать...");
        JButton translate = new JButton("Перевести");
        chooseDict.addActionListener(e -> chooseFile(dictPath::setText));
        chooseText.addActionListener(e -> chooseFile(this::readText));
        translate.addActionListener(e -> translate());
        textPath.setEditable(false);
        text.setFont(dictPath.getFont());

        JPanel labels = new JPanel(new GridLayout(0, 1, 0, 5));
        JPanel paths = new JPanel(new GridLayout(0, 1, 0, 5));
        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 5));
        labels.add(new JLabel("Файл словаря:")); paths.add(dictPath); buttons.add(chooseDict);
        labels.add(new JLabel("Файл текста:"));  paths.add(textPath); buttons.add(chooseText);

        JPanel files = new JPanel(new BorderLayout(5, 0));
        files.add(labels, BorderLayout.WEST);
        files.add(paths, BorderLayout.CENTER);
        files.add(buttons, BorderLayout.EAST);

        JScrollPane input = new JScrollPane(text);
        input.setBorder(BorderFactory.createTitledBorder("Текст для перевода (из файла или вручную)"));

        add(files, BorderLayout.NORTH);
        add(input, BorderLayout.CENTER);
        add(translate, BorderLayout.SOUTH);
    }

    // диалог выбора файла, выбранный путь передаётся дальше
    private void chooseFile(Consumer<String> action) {
        JFileChooser fc = new JFileChooser(".");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            action.accept(fc.getSelectedFile().getPath());
    }

    // текст для перевода из файла попадает в поле ввода
    private void readText(String file) {
        try {
            text.setText(String.join("\n", Dictionary.readLines(file)));
            textPath.setText(file);
        } catch (FileReadException e) {
            out.println("Ошибка чтения текста: " + e.getMessage());
        }
    }

    private void translate() {
        try {
            Dictionary dict = Dictionary.load(dictPath.getText());
            text.getText().lines().map(dict::translate).forEach(out::println);
        } catch (InvalidFileFormatException e) {
            out.println("Неверный формат словаря: " + e.getMessage());
        } catch (FileReadException e) {
            out.println("Ошибка чтения словаря: " + e.getMessage());
        }
    }
}

class InvalidFileFormatException extends Exception {
    InvalidFileFormatException(String message) { super(message); }
}

class FileReadException extends Exception {
    FileReadException(String message, Throwable cause) { super(message, cause); }
}

class Dictionary {
    // проверка на корректный ввод регекспом
    private static final Pattern TOKEN = Pattern.compile("[\\p{L}\\p{Nd}]+|\\s+|.");

    private final Map<String, String> map = new HashMap<>();
    private int maxWords = 1; // длина самой длинной фразы

    // чтение строк файла (словаря или текста для перевода)
    static List<String> readLines(String file) throws FileReadException {
        try {
            return Files.readAllLines(Path.of(file), StandardCharsets.UTF_8);
        } catch (IOException | InvalidPathException e) {
            String reason = switch (e) {
                case NoSuchFileException x -> "файл не найден";
                case AccessDeniedException x -> "нет доступа к файлу";
                case CharacterCodingException x -> "надо поменять кодировку на UTF-8";
                case InvalidPathException x -> "некорректный путь";
                default -> "ошибка чтения";
            };
            throw new FileReadException(reason + ": " + file, e);
        }
    }

    static Dictionary load(String file) throws InvalidFileFormatException, FileReadException {
        List<String> lines = readLines(file);

        Dictionary d = new Dictionary();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\|", -1);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank())
                throw new InvalidFileFormatException(
                        "строка " + (i + 1) + ": ожидается 'слово или выражение | перевод'");
            String key = normalize(parts[0]);
            d.map.put(key, parts[1].strip());
            d.maxWords = Math.max(d.maxWords, key.split(" ").length);
        }
        if (d.map.isEmpty()) throw new InvalidFileFormatException("словарь пуст");
        return d;
    }

    String translate(String text) {
        List<String> t = new ArrayList<>();
        Matcher m = TOKEN.matcher(text);
        while (m.find()) t.add(m.group());

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < t.size(); ) {
            if (!isWord(t.get(i))) { out.append(t.get(i++)); continue; }

            // наращиваем фразу слово за словом
            String best = t.get(i);
            int bestEnd = i;
            StringBuilder key = new StringBuilder(t.get(i).toLowerCase(Locale.ROOT));
            for (int j = i, n = 1; ; j += 2, n++) {
                String found = map.get(key.toString());
                if (found != null) { best = found; bestEnd = j; }
                boolean canExtend = n < maxWords && j + 2 < t.size()
                        && t.get(j + 1).isBlank() && isWord(t.get(j + 2));
                if (!canExtend) break;
                key.append(' ').append(t.get(j + 2).toLowerCase(Locale.ROOT));
            }
            out.append(best);
            i = bestEnd + 1;
        }
        return out.toString();
    }

    private static boolean isWord(String s) {
        return Character.isLetterOrDigit(s.codePointAt(0));
    }

    private static String normalize(String phrase) {
        return String.join(" ", phrase.strip().toLowerCase(Locale.ROOT).split("\\s+"));
    }
}
