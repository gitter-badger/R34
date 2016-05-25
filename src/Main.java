
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;
import java.io.*;
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
import javax.imageio.ImageIO;
import javax.swing.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class Main {
    private static JFrame frameTags = new JFrame("Popular tags");
    private static JPanel panel = new JPanel();
    private static JProgressBar progressBar = new JProgressBar();
    private static ArrayList<JRadioButton> checkList = new ArrayList<>();
    private static ArrayList<String> tagURLs = new ArrayList<>();
    private static ButtonGroup group = new ButtonGroup();
    private static String mainUrl;
    private static String dirNameString;
    static File dir;
    private static Path path;

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
        nextButton.setHorizontalAlignment(SwingConstants.CENTER);
        checkList.get(0).setSelected(true);
        nextButton.setMaximumSize(new Dimension(277, 50));
        panel.add(nextButton);
        fourClass buttonThread = new fourClass(frameTags, group, dirNameString, path);
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Thread queryThread = new Thread() {
                    public void run() {
                        fourClass.runThread();
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

class fourClass extends Thread {
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

    fourClass(JFrame jFrameTags, ButtonGroup groupB, String dirName, Path p) {
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
        textArea.setMargin( new Insets(10,10,10,10) );
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
                String size = String.valueOf(urlsImgArray.size());
                for (int i2 = 0, imgName = 1; i2 < index; i2++, imgName++) {
                    String gifName = imgName + "." + gif;
                    String pngName = imgName + "." + png;
                    String jpgName = imgName + "." + jpg;
                    URL url = null;
                    try {
                        url = new URL(urlsImgArray.get(i2));
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
                    int width = pg.getWidth(), height = pg.getHeight();
                    DataBuffer buffer = new DataBufferInt((int[]) pg.getPixels(), pg.getWidth() * pg.getHeight());
                    WritableRaster raster = Raster.createPackedRaster(buffer, width, height, width, RGB_MASKS, null);
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