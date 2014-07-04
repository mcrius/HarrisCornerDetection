/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package harriscornerdetection;

import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/**
 *
 * @author bzzzt
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private Label label;
    @FXML
    private Label threshLabel;
    @FXML
    private ImageView imageView;
    @FXML
    private ImageView outView;
    private File file;
    private static int[] orig;
    private Image image;
    @FXML
    private VBox vBox;
    @FXML
    private Slider slider;
    @FXML
    private Slider threshSlider;
    @FXML
    private TextField textField;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TabPane tabPane;
//    final DoubleProperty zoomProperty = new SimpleDoubleProperty(100);

    @FXML
    private void handleButtonAction(ActionEvent event) {
        FileChooser fc = new FileChooser();
        file = fc.showOpenDialog(null);
        if (file != null) {
            Platform.runLater(() -> {
                try {
                    image = new Image(new FileInputStream(file));
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
                imageView.setImage(image);
                textField.setText(file.getAbsolutePath());
                imageView.setFitHeight(image.getHeight());
                imageView.setFitWidth(image.getWidth());
                imageView.preserveRatioProperty().set(true);
                outView.setFitHeight(image.getHeight());
                outView.setFitWidth(image.getWidth());
                outView.preserveRatioProperty().set(true);
            });
        }
    }

    @FXML
    private void process(ActionEvent e) {
        double k = slider.getValue() / 1000;
        double thresh = threshSlider.getValue();
        try {
            Toolkit tk = Toolkit.getDefaultToolkit();
            java.awt.Image i = tk.createImage(file.getAbsolutePath());
//            i = i.getScaledInstance(256, 256, java.awt.Image.SCALE_SMOOTH);
            harris h = new harris();
//            image.
            int size = image.widthProperty().intValue() * image.heightProperty().intValue();
//            int size = i.getWidth(null) * i.getHeight(null);
            orig = new int[size];
//            int width = 256;
//            int height = 256;
            int width = imageView.getImage().widthProperty().intValue();
            int height = imageView.getImage().heightProperty().intValue();
//            java.awt.Image i = new ToolkitImage(new FileImageSource(file.getAbsolutePath()));

            PixelGrabber grabber = new PixelGrabber(i, 0, 0, width, height, orig, 0, width);
            grabber.grabPixels();
            Thread t = new Thread(() -> {
                h.init(orig, width, height, k, thresh);
                orig = h.process();
//                Component c = new JFXPanel();
                final java.awt.Image output = tk.createImage(new MemoryImageSource(width, height, orig, 0, width));
                WritableImage wi = new WritableImage(width, height);
                WritableImage newwi = SwingFXUtils.toFXImage(toBufferedImage(output), wi);
                Platform.runLater(() -> {
                    outView.setImage(newwi);
                    tabPane.getSelectionModel().selectLast();
                });
            });
            t.start();
        } catch (InterruptedException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        label.textProperty().bind(slider.valueProperty().divide(1000.0d).asString("%1.3f"));
        threshLabel.textProperty().bind(threshSlider.valueProperty().asString("%1.0f"));
    }

    public static BufferedImage toBufferedImage(java.awt.Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }
}
