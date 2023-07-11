import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class StartScreen extends JFrame{
    public static JFrame startScreen;

    public static float[][] kernelMatrix;
    public static float factor;

    private String matrixDis;

    public static String originalPath;
    public static String processedPath;
    public static ImageIcon originalImage;
    public static ImageIcon processedImage;
    public static BufferedImage outputImg;

    private JFileChooser openFile;
    private JMenuBar MenuBar;
    private JMenu menu;
    private JMenu submenu;
    private JMenuItem img;
    private JMenuItem sample1;
    private JMenuItem sample2;
    private JMenuItem sample3;
    private ButtonGroup modes;
    private JPanel panel;
    private JLabel selectM;
    private JRadioButton seqB;
    private JRadioButton parB;
    private JRadioButton disB;
    private  JLabel kernelT;
    private JList kernelList;
    private JButton apply;
    public static JLabel timeL;
    private JLabel beforeT;
    private JLabel beforeI;
    private JLabel afterT;
    public static JLabel afterI;

    public StartScreen(){

        startScreen = this;
        this.setSize(800, 600);
        this.setResizable(false);
        this.setTitle("Kernel Image Processing");
        this.setDefaultCloseOperation(3);

        openFile = new JFileChooser();
        openFile.setDialogTitle("Open file");
        openFile.setPreferredSize(new Dimension(400,300));

        // Menu bar for selecting an image
        MenuBar = new JMenuBar();
        menu = new JMenu("Image");
        submenu = new JMenu("Samples");
        img = new JMenuItem("Select Image");
        img.addActionListener(e -> selectImageAction(e));

        sample1 = new JMenuItem("Sample 1");
        sample2 = new JMenuItem("Sample 2");
        sample3 = new JMenuItem("Sample 3");

        sample1.addActionListener(e -> selectSample1(e));
        sample2.addActionListener(e -> selectSample2(e));
        sample3.addActionListener(e -> selectSample3(e));

        submenu.add(sample1); submenu.add(sample2); submenu.add(sample3);
        menu.add(img); menu.add(submenu);
        MenuBar.add(menu);
        this.setJMenuBar(MenuBar);


        // Buttons for selecting the mode
        modes = new ButtonGroup();
        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        selectM = new JLabel("Select Mode: ");
        seqB = new JRadioButton("Sequential");
        parB = new JRadioButton("Parallel");
        disB = new JRadioButton("Distributed");
        seqB.setSelected(true);

        modes.add(seqB); modes.add(parB); modes.add(disB);
        panel.add(selectM); panel.add(seqB); panel.add(parB); panel.add(disB);


        kernelT = new JLabel("Select Filter:");
        String kernels[] = {"Sharpen", "Edge detection", "Gaussian blur", "Box blur", "Emboss"};
        kernelList = new JList<>(kernels);
        kernelList.setSelectedIndex(0);
        apply = new JButton("Apply");
        apply.addActionListener(e -> {
            try {
                applyAction(e);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        panel.add(kernelT);
        panel.add(kernelList);
        panel.add(apply);

        timeL = new JLabel("Total time: ");
        panel.add(timeL);

        beforeT = new JLabel("Selected image:");
        beforeI = new JLabel();
        beforeI.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        beforeI.setPreferredSize(new Dimension(250,250));

        afterT = new JLabel("Processed image:");
        afterI = new JLabel();
        afterI.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        afterI.setPreferredSize(new Dimension(250,250));

        panel.add(beforeT); panel.add(beforeI);
        panel.add(afterT); panel.add(afterI);
        this.add(panel);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public static void insertImage(String filePath, JLabel label) {
        ImageIcon icon = new ImageIcon(new ImageIcon(filePath).getImage().getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_SMOOTH));
        label.setIcon(icon);
    }

    private void selectImageAction(ActionEvent evt) {

        openFile.addChoosableFileFilter(new FileNameExtensionFilter("Images (.jpg, .jpeg, .png)", "jpg", "jpeg", "png"));

        int returnVal = openFile.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = openFile.getSelectedFile();
            originalPath = file.getAbsolutePath();
            insertImage(originalPath, beforeI);
        } else {
            System.out.println("File access cancelled.");
        }
    }

    private void selectSample1(ActionEvent e){
        originalPath = "src/Samples/sample1.jpg";
        insertImage(originalPath, beforeI);
    }

    private void selectSample2(ActionEvent e){
        originalPath = "src/Samples/sample2.jpg";
        insertImage(originalPath, beforeI);
    }
    private void selectSample3(ActionEvent e){
        originalPath = "src/Samples/sample3.jpg";
        insertImage(originalPath, beforeI);
    }

    private void applyAction(ActionEvent event) throws IOException {
        if (kernelList.getSelectedValue().equals("Sharpen")){
            kernelMatrix = Kernels.sharpen;
            matrixDis = "sharpen";
            factor = 1;
        } else if (kernelList.getSelectedValue().equals("Edge detection")){
            kernelMatrix = Kernels.edge_detection;
            matrixDis = "edge_detection";
            factor = 1;
        } else if (kernelList.getSelectedValue().equals("Gaussian blur")){
            kernelMatrix = Kernels.gaussian_blur;
            matrixDis = "gaussian_blur";
            factor = 1f/16f;
        } else if (kernelList.getSelectedValue().equals("Box blur")) {
            kernelMatrix = Kernels.box_blur;
            matrixDis = "box_blur";
            factor = 1f/9f;
        } else if (kernelList.getSelectedValue().equals("Emboss")) {
            kernelMatrix = Kernels.emboss;
            matrixDis = "emboss";
            factor = 1;
        }

        try {
            processImage(kernelMatrix, factor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processImage(float[][] kernelMatrix, float factor) throws IOException {
        if (seqB.isSelected()){
            Sequential.convolution(originalPath, kernelMatrix, factor);

            insertImage("src/Temp/temp.jpg", afterI);
        } else if (parB.isSelected()) {
            Parallel.convolution(originalPath,kernelMatrix,factor);

            insertImage("src/Temp/temp.jpg", afterI);
        } else if (disB.isSelected()) {
            //Distributed.startServer(originalPath, matrixDis, factor);
            Thread serverThread = new Thread(()->{
                Server server = new Server(originalPath, matrixDis, factor);
                server.run();
            });
            serverThread.start();

        }

        //System.gc();
    }
}
