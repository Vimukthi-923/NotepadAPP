import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class Notepad extends JFrame {

    private JTextArea textArea;
    private JFileChooser fileChooser;
    private File currentFile;
    private boolean isModified = false;

    public Notepad() {
        setTitle("Simple Notepad - Untitled");
        setSize(900, 600);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        initComponents();
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }

    private void initComponents() {
        // Text area + scroll pane
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { setModified(true); }
            public void removeUpdate(DocumentEvent e) { setModified(true); }
            public void changedUpdate(DocumentEvent e) { setModified(true); }
        });

        JScrollPane scrollPane = new JScrollPane(
                textArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        fileChooser = new JFileChooser();

        setJMenuBar(createMenuBar());
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // --- File menu
        JMenu fileMenu = new JMenu("File");

        JMenuItem openItem = new JMenuItem("Open...");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { openFileAction(); }
        });

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { saveFileAction(); }
        });

        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { saveAsAction(); }
        });

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { exitApplication(); }
        });

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // --- Edit menu
        JMenu editMenu = new JMenu("Edit");

        JMenuItem cutItem = new JMenuItem("Cut");
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        cutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { textArea.cut(); }
        });

        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        copyItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { textArea.copy(); }
        });

        JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        pasteItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { textArea.paste(); }
        });

        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);

        // --- Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO: replace name & ID with your own info
                JOptionPane.showMessageDialog(Notepad.this,
                        "Simple Notepad\nCreated by: Vimukthi Ranaweera\nID: 16478",
                        "About",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private void setModified(boolean modified) {
        if (this.isModified != modified) {
            this.isModified = modified;
            updateTitle();
        }
    }

    private void updateTitle() {
        String name = (currentFile == null) ? "Untitled" : currentFile.getName();
        setTitle((isModified ? "*" : "") + "Simple Notepad - " + name);
    }

    private void openFileAction() {
        if (!confirmSaveIfNeeded()) return;
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            openFile(f);
        }
    }

    private void saveFileAction() {
        if (currentFile == null) {
            saveAsAction();
        } else {
            saveToFile(currentFile);
        }
    }

    private void saveAsAction() {
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            // if user didn't give extension, recommend .txt (not forced)
            saveToFile(f);
        }
    }

    private boolean confirmSaveIfNeeded() {
        if (!isModified) return true;
        int result = JOptionPane.showConfirmDialog(
                this,
                "Current file has unsaved changes. Save now?",
                "Save",
                JOptionPane.YES_NO_CANCEL_OPTION
        );
        if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
            return false;
        } else if (result == JOptionPane.YES_OPTION) {
            saveFileAction();
            return !isModified; // true if saved successfully
        } else {
            return true;
        }
    }

    private void exitApplication() {
        if (confirmSaveIfNeeded()) {
            dispose();
            System.exit(0);
        }
    }

    private void openFile(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            textArea.setText(sb.toString());
            currentFile = file;
            setModified(false);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to open file:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveToFile(File file) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(textArea.getText());
            currentFile = file;
            setModified(false);
            JOptionPane.showMessageDialog(this, "Saved: " + file.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save file:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Start GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Notepad notepad = new Notepad();
                notepad.setVisible(true);
            }
        });
    }
}
