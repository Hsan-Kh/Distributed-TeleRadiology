package client;

import common.ProcessedChunk;
import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class DoctorClient extends JFrame implements MessageListener {

    // CONFIGURATION (MUST MATCH YOUR IMAGE!)

    // If your Pituitary1.jpg is 512x512, change these numbers!
    private static final int IMAGE_WIDTH = 640;
    private static final int IMAGE_HEIGHT = 383;
    private static final int TOTAL_CHUNKS = 40; // Must match HospitalServer

    private BufferedImage displayImage;
    private JPanel panel;

    public DoctorClient() {
        // GUI Setup
        this.setTitle("Tele-Radiology Dashboard (Doctor)");
        this.setSize(IMAGE_WIDTH + 50, IMAGE_HEIGHT + 100);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize blank image
        displayImage = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);

        // Fill with dark gray so we can see updates appear
        Graphics2D g2d = displayImage.createGraphics();
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        g2d.dispose();

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw image centered
                g.drawImage(displayImage, 10, 10, null);
            }
        };
        this.add(panel);

        setupJMS();
        this.setVisible(true);
        System.out.println("Doctor Client Waiting for images...");
    }

    private void setupJMS() {
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            // Security fix to allow Object transfer
            ((ActiveMQConnectionFactory) factory).setTrustAllPackages(true);

            Connection connection = factory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic("TeleRadiology");
            MessageConsumer consumer = session.createConsumer(topic);
            consumer.setMessageListener(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof ObjectMessage) {
                ProcessedChunk chunk = (ProcessedChunk) ((ObjectMessage) message).getObject();

                // 1. Calculate the height of this specific strip
                // Logic: Area = Width * Height -> Height = Area / Width
                int chunkHeight = chunk.processedPixels.length / IMAGE_WIDTH;

                // 2. Calculate Y Offset
                // Use the standard height for calculation to find the starting Y
                int standardChunkHeight = IMAGE_HEIGHT / TOTAL_CHUNKS;
                int yOffset = chunk.id * standardChunkHeight;

                System.out.println("Received Chunk " + chunk.id + " (Height: " + chunkHeight + "px)");

                // 3. Paint pixels into the main image
                // setRGB(startX, startY, w, h, pixelArray, offset, scansize)
                displayImage.setRGB(0, yOffset, IMAGE_WIDTH, chunkHeight, chunk.processedPixels, 0, IMAGE_WIDTH);

                // 4. Refresh GUI
                panel.repaint();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new DoctorClient();
    }

}

