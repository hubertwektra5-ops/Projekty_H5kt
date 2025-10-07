import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SimpleTextEditor extends JFrame {

    private JTextArea textArea;
    private JButton openButton;
    private JButton saveButton;
    private JButton clearButton;

    public SimpleTextEditor() {
        setTitle("Edytor tekstu");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();
        layoutComponents();
        registerEvents();
    }

    private void initComponents() {
        textArea = new JTextArea();
        openButton = new JButton("Otwórz");
        saveButton = new JButton("Zapisz");
        clearButton = new JButton("Wyczyść");
    }

    private void layoutComponents() {
        JPanel topPanel = new JPanel();
        topPanel.add(openButton);
        topPanel.add(saveButton);
        topPanel.add(clearButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }

    private void registerEvents() {
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SimpleTextEditor().setVisible(true);
        });
    }
}
