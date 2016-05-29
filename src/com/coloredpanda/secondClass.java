package com.coloredpanda;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

class secondClass extends Thread {

    private int counterTags = 0;
    private static int counterProgress = 0;
    private static ArrayList<String> tagList = new ArrayList<>();
    private ArrayList<String> tagURLs;
    private ArrayList<JRadioButton> checkList;
    private JProgressBar progressBar;
    private ButtonGroup group;
    private JPanel panel;

    secondClass(ArrayList<String> tagU, ArrayList<JRadioButton> chkList, JPanel jPanel, JProgressBar progressB, ButtonGroup groupB) {
        tagURLs = tagU;
        checkList = chkList;
        panel = jPanel;
        progressBar = progressB;
        group = groupB;
    }

    @Override
    public synchronized void run() {
        Document doc = null;
        try {
            doc = Jsoup.connect("http://rule34.paheal.net/tags/popularity").get();

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Don't worry, just run again");
            System.exit(-1);
        }

        Elements urlsPages = doc.select("section#Tagsmain div.blockbody a[href]");
        Elements tags = doc.select("section#Tagsmain div.blockbody a");

        for (Element urlsPage : urlsPages) {
            urlsPage = urlsPages.get(counterTags);
            String url = urlsPage.attr("abs:href");
            tagURLs.add(url);
            counterTags++;
        }
        counterTags = 0;
        for (Element tag : tags) {
            counterTags++;
            tagList.add(tag.text());
        }

        progressBar.setMaximum(counterTags);

        for (String element : tagList) {
            JRadioButton radioButton = new JRadioButton(element);
            radioButton.setActionCommand(tagURLs.get(counterProgress));
            group.add(radioButton);
            checkList.add(radioButton);
            panel.add(radioButton);
            progressBar.setValue(counterProgress);
            counterProgress++;
        }
    }
}
