package com.coloredpanda;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

class firstClass extends Thread {
    private static JFrame frameDownload = new JFrame("Downloading");
    private static int index = 0;
    private static int counterPage = 0;
    private static int counterGreatPage = 0;
    private static JTextArea textArea = new JTextArea();
    private static ArrayList<String> urlsImgArray = new ArrayList<>();
    private static ArrayList<String> urlsPageArray = new ArrayList<>();
    private static ButtonGroup group;
    private static String dirNameString;
    private static String lastPath;
    private static Path path;
    private static JFrame frameTags;
    private static String workingDir = System.getProperty("user.dir");
    private static File dir = new File(workingDir);
    private static final int[] RGB_MASKS = {0xFF0000, 0xFF00, 0xFF};
    private static final ColorModel RGB_OPAQUE = new DirectColorModel(32, RGB_MASKS[0], RGB_MASKS[1], RGB_MASKS[2]);

    firstClass (JFrame jFrameTags, ButtonGroup groupB, String dirName, Path p) {
        dirNameString = dirName;
        path = p;
        frameTags = jFrameTags;
        group = groupB;
    }

    static void runThread() {
        frameDownload.setSize(300, 500);
        frameDownload.setResizable(false);
        frameDownload.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane areaScrollPane = new JScrollPane(textArea);
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        frameDownload.add(areaScrollPane);
        frameDownload.setLocation(frameTags.getLocationOnScreen());
        frameTags.setVisible(false);
        frameDownload.setVisible(true);
        Document doc = null;
        try {
            try {
                doc = Jsoup.connect(group.getSelection().getActionCommand()).get();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Don't worry, just run again");
                System.exit(-1);
                ex.printStackTrace();
            }
            Elements urlsPages = doc.select("section#paginator div.blockbody a[href]");
            Elements dirNameElements = doc.select("head title");

            for (Element urlsPage : urlsPages) {
                urlsPage = urlsPages.get(counterPage);
                String url = urlsPage.attr("abs:href");
                if (urlsPage.text().matches("[0-9]+")) {
                    urlsPageArray.add(url);
                    counterGreatPage++;
                }
                counterPage++;
            }
            for (Element dirNameElement : dirNameElements) {
                dirNameElement = dirNameElements.get(index);
                dirNameString = dirNameElement.text();
            }
            try {
                path = Paths.get(dirNameString);
            } catch (InvalidPathException e) {
                dirNameString = "Folder";
                path = Paths.get(dirNameString);
            }
            //if directory exists?
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    //fail to create directory
                    e.printStackTrace();
                }
            }
            String size = null;
            String gif = "gif";
            String png = "png";
            String jpg = "jpg";
            String twoBackSlashes = "\\";
            lastPath = dir + twoBackSlashes + path;


            for (int i = 0; i < counterGreatPage; i++) {
                int newIndex = 0;
                String mUrl = urlsPageArray.get(i);
                Document document = Jsoup.connect(mUrl).get();
                Elements urlsImages = document.select("section#imagelist a[href$=.jpg], a[href$=.png], a[href$=.gif]");
                for (Element urlImg : urlsImages) {
                    urlImg = urlsImages.get(newIndex);
                    urlsImgArray.add(urlImg.attr("abs:href"));
                    newIndex++;
                    index++;
                }
                size = String.valueOf(urlsImgArray.size());
            }
            download:
            for (int i = 0, imgName = 1; i < index; i++, imgName++) {
                String gifName = imgName + "." + gif;
                String pngName = imgName + "." + png;
                String jpgName = imgName + "." + jpg;
                URL url = null;
                try {
                    url = new URL(urlsImgArray.get(i));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                Image img = Toolkit.getDefaultToolkit().createImage(url);
                PixelGrabber pg = new PixelGrabber(img, 0, 0, -1, -1, true);
                try {
                    pg.grabPixels();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                DataBuffer buffer = new DataBufferInt((int[]) pg.getPixels(), pg.getWidth() * pg.getHeight());
                WritableRaster raster;
                try {
                    raster = Raster.createPackedRaster(buffer, pg.getWidth(), pg.getHeight(), pg.getWidth(), RGB_MASKS, null);
                } catch (IllegalArgumentException e) {
                    i++;
                    continue download;
                }
                BufferedImage imgBuf = new BufferedImage(RGB_OPAQUE, raster, false, null);
                assert url != null;
                if (Objects.equals(url.toString().substring(url.toString().length() - 4), "." + gif)) {
                    String pathGif = dir + "/" + path + "/" + gifName;
                    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                    FileOutputStream fos = new FileOutputStream(pathGif);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                }
                if (Objects.equals(url.toString().substring(url.toString().length() - 4), "." + png))
                    ImageIO.write(imgBuf, png, new File(lastPath, pngName));

                if (Objects.equals(url.toString().substring(url.toString().length() - 4), "." + jpg))
                    ImageIO.write(imgBuf, jpg, new File(lastPath, jpgName));
                textArea.append("Download image: " + imgName + "/" + size + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(null, "Download finished!");
        try {
            Desktop.getDesktop().open(new File(lastPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public void updateGUI() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                textArea.repaint();
                textArea.revalidate();
            }
        });
    }
}
