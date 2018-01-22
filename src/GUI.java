import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static javax.swing.JOptionPane.showMessageDialog;

public class GUI extends JFrame {
    private JPanel rootPanel;
    private JButton buttonTrain;
    private JPanel imagePanel;
    private JLabel imageLabel;
    private JLabel labelError;
    private JLabel labelAnswer;
    private JLabel labelEpoch;
    private JTextArea textAreaAnswer;
    private JButton buttonTest;
    private JLabel labelNetError;
    private NeuralNet neuralNet;

    public GUI() {
        setContentPane(rootPanel);
        pack();
        setTitle("Обучение методом SGD");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        buttonTest.setEnabled(false);

        buttonTrain.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                train();
            }
        });
        buttonTest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                test();
            }
        });
    }

    public void train() {
        try {
            buttonTest.setEnabled(false);
            Vector[] trainVectorSet = readTrainVectors("D://Train");
            neuralNet = new NeuralNet(trainVectorSet[0].getX().length, trainVectorSet[0].getDesireOutputs().length);
            neuralNet.setComplete(false);

            Runnable task1 = () -> {
                try {
                    neuralNet.train(trainVectorSet);
                } catch (InterruptedException e) {
                }
            };
            Thread thread1 = new Thread(task1);
            Runnable task2 = () -> {
                while (!neuralNet.isComplete()) {
                    labelEpoch.setText("Номер эпохи: " + neuralNet.getEpochNumber());
                    double[] err = neuralNet.getError();
                    StringBuilder s = new StringBuilder();
                    IntStream.range(0, err.length)
                            .forEach(i -> s.append(String.format("[%.5f] ", err[i])));

                    labelError.setText("Ошибка нейронов: " + s);
                    labelNetError.setText("Ошибка вектора: " + neuralNet.getErrorNet());
                }
                textAreaAnswer.append("Обучение завершено\n");
                buttonTest.setEnabled(true);
            };
            Thread thread2 = new Thread(task2);

            thread1.start();
            thread2.start();
        } catch (IOException e) {
            showMessageDialog(null, "Файл не найден");
        } catch (Exception e) {
            showMessageDialog(null, e.toString());
        }

        int f = 0;
    }

    public void test() {
        try {
            String path = "D://Test//";
            int totalCount = 0;
            int totalPassed = 0;
            for (int i = 0; i < 10; i++) {
                File[] files = new File(path + i).listFiles();
                int count = 0;
                int passed = 0;
                for (File file : files) {
                    double[] testVector = readVector(file.getPath());
                    double[] answer = neuralNet.test(testVector);
                    count++;
                    if (i == getMaxNeuronIdx(answer)) {
                        passed++;
                    }
                }
                double percent = (double) passed / count * 100.0;
                textAreaAnswer.append(String
                        .format("Процент распознавания класса образов №%d: %.2f%n", i, percent));
                totalCount += count;
                totalPassed += passed;
            }
            double percent = 100.0 - (double) totalPassed / totalCount * 100.0;
            textAreaAnswer.append(String
                    .format("Процент ошибки распознавания: %.2f%n", percent));
        } catch (IOException e) {
            showMessageDialog(null, "Файл не найден");
        } catch (Exception e) {
            showMessageDialog(null, e.toString());
        }
    }

    public int getMaxNeuronIdx(double[] answer) {
        int maxIdx = 0;
        for (int i = 1; i < answer.length; i++) {
            if (answer[maxIdx] < answer[i])
                maxIdx = i;
        }
        return maxIdx;
    }

    public double[] readVector(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        int[][] grayImage = imageToGrayScale(image);
        double[] imageVector = imageToVector(grayImage);
        return imageVector;
    }


    public Vector[] readTrainVectors(String rootDir) throws IOException {
        List<Vector> trainVectorSet = new ArrayList();

        for (int i = 0; i < 10; i++) {
            File[] files = new File(rootDir + "//" + i).listFiles();
            for (File file : files) {
                BufferedImage image = ImageIO.read(file);

                int[][] grayImage = imageToGrayScale(image);
                double[] imageVector = imageToVector(grayImage);

                double[] desireOutputs = new double[10];
                for (int k = 0; k < desireOutputs.length; k++) {
                    desireOutputs[k] = i == k ? 1 : 0;
                }

                trainVectorSet.add(new Vector(imageVector, desireOutputs));
                //imageLabel.setIcon(new ImageIcon(image));
            }
        }
        return (Vector[]) trainVectorSet.toArray(new Vector[trainVectorSet.size()]);
    }

    public int[][] imageToGrayScale(BufferedImage image) {
        int[][] resultImage = new int[image.getWidth()][image.getHeight()];
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color c = new Color(image.getRGB(x, y));
                resultImage[x][y] = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
            }
        }
        return resultImage;
    }

    public BufferedImage grayScaleToImage(int[][] grayImage) {
        int height = grayImage[0].length;
        int width = grayImage[1].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color c = new Color(grayImage[x][y], grayImage[x][y], grayImage[x][y], 0);
                image.setRGB(x, y, c.getRGB());
            }
        }
        return image;
    }


    public double[] imageToVector(int[][] image) {
        double[] resultVector = new double[image[0].length * image[1].length];
        int i = 0;
        for (int x = 0; x < image.length; x++) {
            for (int y = 0; y < image.length; y++) {
                resultVector[i++] = image[x][y];
            }
        }
        return resultVector;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(7, 5, new Insets(10, 10, 10, 10), -1, -1));
        rootPanel.setPreferredSize(new Dimension(500, 300));
        buttonTrain = new JButton();
        buttonTrain.setText("Train");
        rootPanel.add(buttonTrain, new com.intellij.uiDesigner.core.GridConstraints(6, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        rootPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        labelError = new JLabel();
        labelError.setText("Ошибки нейронов:");
        rootPanel.add(labelError, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelEpoch = new JLabel();
        labelEpoch.setText("Номер эпохи:");
        rootPanel.add(labelEpoch, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAnswer = new JLabel();
        labelAnswer.setText("Ответ:");
        rootPanel.add(labelAnswer, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonTest = new JButton();
        buttonTest.setText("Test");
        rootPanel.add(buttonTest, new com.intellij.uiDesigner.core.GridConstraints(4, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        rootPanel.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textAreaAnswer = new JTextArea();
        textAreaAnswer.setMargin(new Insets(0, 0, 0, 0));
        scrollPane1.setViewportView(textAreaAnswer);
        imagePanel = new JPanel();
        imagePanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(imagePanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(10, 10), new Dimension(10, 10), new Dimension(10, 10), 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        imagePanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        imageLabel = new JLabel();
        imageLabel.setText("");
        imagePanel.add(imageLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelNetError = new JLabel();
        labelNetError.setText("Ошибка вектора:");
        rootPanel.add(labelNetError, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }
}
