import javax.swing.*;
import java.io.PrintWriter;

public class lab1 extends JPanel {

    static final class Point {
        final double x, y;

        Point(double x, double y) {
            this.x = x; this.y = y;
        }

        double distanceTo(Point other) {
            return Math.hypot(other.x - x, other.y - y);
        }

        @Override public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    interface MovementStrategy {
        String title();
        double speed();

        default double estimateTimeSeconds(Point from, Point to) {
            return from.distanceTo(to) / speed();
        }

        default void move(Point from, Point to, PrintWriter out) {
            double distance = from.distanceTo(to);
            double timeSec = estimateTimeSeconds(from, to);
            out.printf("%s: %s -> %s | dist %.2f | time ~%.1f с%n",
                    title(), from, to, distance, timeSec);
        }
    }

    enum ModeStrategy implements MovementStrategy {
        WALK ("wolking", 1.4),
        HORSE ("on horse", 1.4),
        FLY ("flying", 1.4);

        final String label;
        final double speed;

        ModeStrategy(String title, double speed) {
            this.label = title;
            this.speed = speed;
        }

        @Override public String title() { return label; }
        @Override public double speed() { return speed; }
    }

    static final class Hero {
        private final PrintWriter out;
        private MovementStrategy strategy;
        private Point position;

        public Hero(Point start, MovementStrategy strategy, PrintWriter out) {
            this.position = start;
            this.strategy = strategy;
            this.out = out;
        }

        public void setStrategy(MovementStrategy strategy) {
            this.strategy = strategy;
            out.println("Now for the strategy: " + strategy.title());
        }

        public void move(Point to) {
            strategy.move(position, to, out);
            position = to;
        }

        public Point getPosition() { return position; }
        public String getStrategyName() { return strategy.title(); }
    }

    private final PrintWriter out;
    private final Hero hero;
    private final JTextField xField = new JTextField(6), yField = new JTextField(6);

    public lab1(PrintWriter out) {
        this.out = out;
        this.hero = new Hero(new Point(0, 0), ModeStrategy.WALK, out);

        JComboBox<ModeStrategy> strategy = new JComboBox<>(ModeStrategy.values());
        JButton move = new JButton("Переместить");
        strategy.addActionListener(e -> hero.setStrategy((ModeStrategy) strategy.getSelectedItem()));
        move.addActionListener(e -> moveHero());

        add(new JLabel("Стратегия:")); add(strategy);
        add(new JLabel("x:"));         add(xField);
        add(new JLabel("y:"));         add(yField);
        add(move);

        out.println("Lab1");
        out.println("After selecting a strategy, enter the target coordinates: x y (e.g., 10 5).");
        printState();
    }

    private void moveHero() {
        try {
            double x = Double.parseDouble(xField.getText().trim());
            double y = Double.parseDouble(yField.getText().trim());
            hero.move(new Point(x, y));
        } catch (NumberFormatException e) {
            out.println("Invalid coordinate format. Try again.");
        }
        printState();
    }

    private void printState() {
        out.printf("%nHero point: %s | Strategy: %s%n",
                hero.getPosition(), hero.getStrategyName());
    }
}
