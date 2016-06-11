package com.coloredpanda;
        import java.awt.*;
        import java.awt.event.ActionEvent;
        import java.awt.event.ActionListener;
        import java.util.ArrayList;
        import javax.swing.*;
class Main {
    private static JFrame frameTags = new JFrame("Popular tags");
    private static JFrame frameDownload = new JFrame("Downloading");
    private static JPanel panel = new JPanel();
    private static JProgressBar progressBar = new JProgressBar();
    private static ArrayList<JRadioButton> checkList = new ArrayList<>();
    private static ArrayList<String> tagURLs = new ArrayList<>();
    private static ButtonGroup group = new ButtonGroup();

    private static void createGUI() {
        frameTags.setSize(300, 500);
        frameTags.setLocation(650, 200);
        frameTags.setResizable(false);
        frameTags.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(progressBar);
        progressBar.setMinimum(0);
        progressBar.setStringPainted(true);
        frameTags.add(panel);
        frameTags.setVisible(true);
    }

    private static void changeGUI() {
        panel.remove(progressBar);
        panel.setAutoscrolls(true);
        JScrollPane scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        frameTags.add(scroll);
        frameTags.revalidate();
        frameTags.repaint();
    }

    private static void startThread() throws InterruptedException {
        secondClass tagsThread = new secondClass(tagURLs, checkList, panel, progressBar, group);
        tagsThread.start();
        do {
            progressBar.repaint();
            tagsThread.join(350);
        } while (tagsThread.isAlive());
    }


    private static void addButton() throws InterruptedException {
        JButton nextButton = new JButton("Download");
        checkList.get(0).setSelected(true);
        nextButton.setMaximumSize(new Dimension(277, 50));
        panel.add(nextButton);
        firstClass buttonThread = new firstClass (frameTags, group, panel, frameDownload);
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Thread queryThread = new Thread() {
                    public void run() {
                        firstClass.runThread();
                    }
                };
                queryThread.start();
            }
        });
    }

    public static void main(String[] args) throws Exception {
        createGUI();
        startThread();
        changeGUI();
        addButton();
    }
}
