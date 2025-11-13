
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParkomatSimulator extends JFrame {
    private JLabel lcdLine1;
    private JLabel lcdLine2;
    private JTextField regField;
    private JLabel costLabel;
    private JLabel timeSelectedLabel;
    private JButton printButton;
    private JButton cancelButton;
    private JLabel clockLabel;
    private JRadioButton cashRadio;
    private JRadioButton cardRadio;

    private int minutesSelected = 30;
    private final int MIN_MINUTES = 30;
    private final int MAX_MINUTES = 8 * 60;
    private final int PRICE_PER_30MIN = 2;

    public ParkomatSimulator() {
        super("Symulator Parkometru");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(820, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        initUI();
        startClock();
    }

    private void initUI() {
        Color navy = new Color(10, 25, 50);
        Color panelBg = new Color(19, 34, 64);
        Color accent = new Color(0, 150, 199);
        getContentPane().setBackground(navy);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JPanel lcd = new JPanel(new GridLayout(2, 1));
        lcd.setBackground(new Color(5, 10, 30));
        lcd.setBorder(new LineBorder(Color.DARK_GRAY, 4));
        lcdLine1 = createLcdLabel("Witaj! Wpisz numer rejestracyjny", 20);
        lcdLine2 = createLcdLabel("", 20);
        lcd.add(lcdLine1);
        lcd.add(lcdLine2);
        topPanel.add(lcd, BorderLayout.CENTER);

        clockLabel = new JLabel();
        clockLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        clockLabel.setForeground(Color.WHITE);
        clockLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        topPanel.add(clockLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel regPanel = new JPanel(new BorderLayout(5, 5));
        regPanel.setBackground(panelBg);
        regPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(accent), "Numer rejestracyjny"));
        regField = new JTextField();
        regField.setFont(new Font("Monospaced", Font.BOLD, 28));
        regField.setHorizontalAlignment(SwingConstants.CENTER);
        regField.setEditable(false);
        regField.setBackground(new Color(230, 240, 250));
        regPanel.add(regField, BorderLayout.CENTER);

        JButton confirmRegButton = new JButton("\u2713 Zatwierdź");
        confirmRegButton.setBackground(accent);
        confirmRegButton.setForeground(Color.white);
        confirmRegButton.setFont(confirmRegButton.getFont().deriveFont(14f));
        confirmRegButton.addActionListener(e -> onConfirmReg());
        regPanel.add(confirmRegButton, BorderLayout.EAST);

        center.add(regPanel, BorderLayout.NORTH);

        JPanel keysPanel = new JPanel(new BorderLayout(6,6));
        keysPanel.setOpaque(false);
        keysPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(accent), "Klawiatura"));

        JPanel lettersPanel = new JPanel(new GridLayout(3, 9, 6, 6));
        lettersPanel.setOpaque(false);
        String letters = "ABCDEFGHIJKL MNOPQRSTU VWXYZ";
        letters = letters.replace(" ", "");
        int colCount = 9;
        int idx = 0;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < colCount; c++) {
                if (idx < letters.length()) {
                    String s = String.valueOf(letters.charAt(idx++));
                    JButton b = makeStyledKey(s);
                    lettersPanel.add(b);
                } else {
                    lettersPanel.add(Box.createGlue());
                }
            }
        }

        JPanel digitsPanel = new JPanel(new GridLayout(1, 10, 6, 6));
        digitsPanel.setOpaque(false);
        for (int d = 1; d <= 9; d++) {
            JButton b = makeStyledKey(String.valueOf(d));
            digitsPanel.add(b);
        }
        JButton zero = makeStyledKey("0");
        digitsPanel.add(zero);

        JPanel actionsPanel = new JPanel(new GridLayout(1,4,6,6));
        actionsPanel.setOpaque(false);
        JButton backspace = new JButton("\u2190");
        backspace.setFont(backspace.getFont().deriveFont(18f));
        backspace.addActionListener(e -> {
            String t = regField.getText();
            if (!t.isEmpty()) regField.setText(t.substring(0, t.length() - 1));
            updateLcdMessage();
        });
        JButton space = new JButton("SPC");
        space.addActionListener(e -> {
            String t = regField.getText();
            if (!t.endsWith(" ")) regField.setText(t + " ");
            updateLcdMessage();
        });
        JButton clear = new JButton("WYCZYŚĆ");
        clear.addActionListener(e -> {
            regField.setText("");
            updateLcdMessage();
        });
        JButton ok = new JButton("OK");
        ok.addActionListener(e -> onConfirmReg());
        actionsPanel.add(backspace);
        actionsPanel.add(space);
        actionsPanel.add(clear);
        actionsPanel.add(ok);

        keysPanel.add(lettersPanel, BorderLayout.CENTER);
        keysPanel.add(digitsPanel, BorderLayout.SOUTH);
        keysPanel.add(actionsPanel, BorderLayout.NORTH);

        center.add(keysPanel, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(10, 10));
        right.setOpaque(false);
        right.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        JPanel timePanel = new JPanel(new GridLayout(8, 1, 8, 8));
        timePanel.setBackground(panelBg);
        timePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(accent), "Wybór czasu"));

        JButton plus30 = new JButton("+30 min");
        plus30.setBackground(new Color(70, 130, 180));
        plus30.setForeground(Color.white);
        plus30.addActionListener(e -> { changeMinutes(30); });
        JButton minus30 = new JButton("-30 min");
        minus30.setBackground(new Color(70, 130, 180));
        minus30.setForeground(Color.white);
        minus30.addActionListener(e -> { changeMinutes(-30); });

        timeSelectedLabel = new JLabel(formatMinutes(minutesSelected));
        timeSelectedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeSelectedLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        timeSelectedLabel.setForeground(Color.WHITE);

        costLabel = new JLabel("Koszt: " + calculateCostZl(minutesSelected) + " zł");
        costLabel.setHorizontalAlignment(SwingConstants.CENTER);
        costLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        costLabel.setForeground(Color.WHITE);

        JPanel paymentPanel = new JPanel(new GridLayout(1,2,6,6));
        paymentPanel.setBackground(panelBg);
        paymentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(accent), "Metoda płatności"));
        cashRadio = new JRadioButton("Gotówka");
        cardRadio = new JRadioButton("Karta");
        cashRadio.setSelected(true);
        cashRadio.setOpaque(false);
        cardRadio.setOpaque(false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(cashRadio);
        bg.add(cardRadio);
        cashRadio.setForeground(Color.WHITE);
        cardRadio.setForeground(Color.WHITE);
        paymentPanel.add(cashRadio);
        paymentPanel.add(cardRadio);

        timePanel.add(plus30);
        timePanel.add(minus30);
        timePanel.add(new JLabel(""));
        timePanel.add(timeSelectedLabel);
        timePanel.add(costLabel);
        timePanel.add(new JLabel(""));
        timePanel.add(paymentPanel);

        right.add(timePanel, BorderLayout.NORTH);

        JPanel confirmPanel = new JPanel(new GridLayout(3, 1, 8, 8));
        confirmPanel.setOpaque(false);
        printButton = new JButton("Drukuj bilet");
        printButton.setBackground(accent);
        printButton.setForeground(Color.white);
        printButton.setFont(printButton.getFont().deriveFont(16f));
        printButton.addActionListener(e -> onPrint());
        printButton.setEnabled(false);
        cancelButton = new JButton("ANULUJ");
        cancelButton.setBackground(new Color(200, 50, 50));
        cancelButton.setForeground(Color.white);
        cancelButton.addActionListener(e -> onCancel());
        confirmPanel.add(printButton);
        confirmPanel.add(cancelButton);
        right.add(confirmPanel, BorderLayout.SOUTH);

        add(right, BorderLayout.EAST);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        JLabel help = new JLabel("Wpisz numer rejestracyjny klawiaturą, zatwierdź, wybierz czas, metodę płatności i drukuj bilet.");
        help.setForeground(Color.WHITE);
        bottom.add(help, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        styleButtons(this.getContentPane());
        updateLcdMessage();
        updateTimeAndCostLabels();
    }

    private JLabel createLcdLabel(String text, int fontSize) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(150, 230, 200));
        l.setFont(new Font("Monospaced", Font.BOLD, fontSize));
        l.setOpaque(true);
        l.setBackground(new Color(5, 10, 30));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private JButton makeStyledKey(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setBackground(new Color(240, 245, 250));
        b.setFocusPainted(false);
        b.addActionListener(e -> {
            if (regField.getText().length() < 12) {
                regField.setText(regField.getText() + text);
                updateLcdMessage();
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        });
        return b;
    }

    private void styleButtons(Container root) {
        for (Component comp : root.getComponents()) {
            if (comp instanceof JButton) {
                JButton b = (JButton) comp;
                b.setBackground(new Color(60, 120, 170));
                b.setForeground(Color.white);
                b.setFocusPainted(false);
            } else if (comp instanceof Container) {
                styleButtons((Container) comp);
            }
        }
    }

    private void updateLcdMessage() {
        String t = regField.getText();
        if (t.isEmpty()) {
            lcdLine1.setText("Wpisz numer rejestracyjny");
            lcdLine2.setText("");
        } else {
            lcdLine1.setText("Numer rejestracyjny:");
            lcdLine2.setText(t);
        }
    }

    private void changeMinutes(int delta) {
        minutesSelected += delta;
        if (minutesSelected < MIN_MINUTES) minutesSelected = MIN_MINUTES;
        if (minutesSelected > MAX_MINUTES) minutesSelected = MAX_MINUTES;
        updateTimeAndCostLabels();
    }

    private void updateTimeAndCostLabels() {
        timeSelectedLabel.setText(formatMinutes(minutesSelected));
        costLabel.setText("Koszt: " + calculateCostZl(minutesSelected) + " zł");
    }

    private String formatMinutes(int mins) {
        int h = mins / 60;
        int m = mins % 60;
        if (h > 0) return String.format("%dh %02dm", h, m);
        else return String.format("%dm", m);
    }

    private int calculateCostZl(int mins) {
        int blocks = (mins + 29) / 30;
        return blocks * PRICE_PER_30MIN;
    }

    private void onConfirmReg() {
        String reg = regField.getText().trim().toUpperCase();
        if (validateRegistration(reg)) {
            lcdLine1.setText("Rejestracja OK");
            lcdLine2.setText(reg);
            printButton.setEnabled(true);
            JOptionPane.showMessageDialog(this,
                    "Numer " + reg + " zaakceptowany.\nWybierz czas parkowania, metodę płatności i naciśnij 'Drukuj bilet'.",
                    "OK", JOptionPane.INFORMATION_MESSAGE);
        } else {
            lcdLine1.setText("Błąd formatu!");
            lcdLine2.setText("Popraw format rejestracji");
            printButton.setEnabled(false);
            JOptionPane.showMessageDialog(this,
                    "Niepoprawny format numeru rejestracyjnego.\nAkceptowane: 1-3 litery, opcjonalna spacja, 1-5 cyfr, opcjonalnie 0-2 litery.\nPrzykłady: PO12345, KR 1234A, WX12345",
                    "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateRegistration(String reg) {
        if (reg.isEmpty()) return false;
        reg = reg.replaceAll("\\s+", " ");
        String pattern = "^[A-Z]{1,3}\\s?[0-9]{1,5}[A-Z]{0,2}$";
        return reg.matches(pattern);
    }

    private void onPrint() {
        String reg = regField.getText().trim().toUpperCase();
        if (!validateRegistration(reg)) {
            JOptionPane.showMessageDialog(this, "Najpierw zatwierdź poprawny numer rejestracyjny.", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int cost = calculateCostZl(minutesSelected);
        String payment = cashRadio.isSelected() ? "Gotówka" : "Karta";
        String message = String.format("Kwota do zapłaty: %d zł\nMetoda: %s\nPotwierdzić płatność?", cost, payment);
        int result = JOptionPane.showConfirmDialog(this, message, "Potwierdź płatność", JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION) return;
        String ticket = generateTicketText(reg, minutesSelected, cost, payment);
        JTextArea ta = new JTextArea(ticket);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(420, 260));
        int save = JOptionPane.showConfirmDialog(this, sp, "Podgląd biletu - zapisać do pliku?", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (save == JOptionPane.YES_OPTION) {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("bilet_" + reg.replaceAll("\\s+","") + ".txt"));
            int rv = fc.showSaveDialog(this);
            if (rv == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                try (FileWriter fw = new FileWriter(f)) {
                    fw.write(ticket);
                    fw.flush();
                    JOptionPane.showMessageDialog(this, "Bilet zapisany: " + f.getAbsolutePath(), "Zapisano", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Błąd zapisu: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Bilet wydrukowany (symulacja).", "Gotowe", JOptionPane.INFORMATION_MESSAGE);
        }
        regField.setText("");
        printButton.setEnabled(false);
        updateLcdMessage();
    }

    private void onCancel() {
        int r = JOptionPane.showConfirmDialog(this, "Czy na pewno anulować transakcję?", "Anuluj", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            regField.setText("");
            minutesSelected = MIN_MINUTES;
            updateTimeAndCostLabels();
            printButton.setEnabled(false);
            updateLcdMessage();
        }
    }

    private String generateTicketText(String reg, int minutes, int cost, String payment) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(dtf);
        StringBuilder sb = new StringBuilder();
        sb.append("=================================\n");
        sb.append("         B I L E T  P A R K I N G\n");
        sb.append("=================================\n");
        sb.append(String.format("Data i godzina: %s\n", now));
        sb.append(String.format("Rejestracja:    %s\n", reg));
        sb.append(String.format("Czas parkowania: %s\n", formatMinutes(minutes)));
        sb.append(String.format("Kwota:           %d zł\n", cost));
        sb.append(String.format("Metoda płatności: %s\n", payment));
        sb.append("---------------------------------\n");
        sb.append("Dziękujemy za skorzystanie z parkometru\n");
        sb.append("=================================\n");
        return sb.toString();
    }

    private void startClock() {
        javax.swing.Timer t = new javax.swing.Timer(1000, e -> {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");
            clockLabel.setText(dtf.format(LocalDateTime.now()));
        });
        t.start();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> {
            ParkomatSimulator f = new ParkomatSimulator();
            f.setVisible(true);
        });
    }
}
