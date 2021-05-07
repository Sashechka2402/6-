package com.company;

import java.awt.*;
import javax.swing.*;
import java.awt.geom.Rectangle2D;
import java.awt.event.*;
import javax.swing.filechooser.*;
import java.awt.image.*;

// Этот класс позволяет исследовать различные части фрактала с помощью
// создания и отображения графического интерфейса Swing и обработки событий, вызванных различными
// взаимодействия с пользователем.

public class FractalExplorer
{
    private JButton saveButton;

    private JButton resetButton;

    private JComboBox myComboBox;

    private int rowsRemaining;

    // Целочисленный размер отображения - это ширина и высота отображения в пикселях.

    private int displaySize;

    // Ссылка JImageDisplay для обновления отображения с помощью различных методов по мере вычисления фрактала.

    private JImageDisplay display;

    // Объект FractalGenerator, использующий ссылку базового класса для отображения других типов фракталов в будущем.

    private FractalGenerator fractal;

    // указывает  диапазон комплексной плоскости, которая выводится на экран.
    private Rectangle2D.Double range;

    // конструктор, который принимает значение
    //размера отображения в качестве аргумента, затем сохраняет это значение в
    //соответствующем поле, а также инициализирует объекты диапазона и
    //фрактального генератора

    public FractalExplorer(int size)
    {
        displaySize = size;
        fractal = new Mandelbrot();
        range = new Rectangle2D.Double();
        fractal.getInitialRange(range);
        display = new JImageDisplay(displaySize, displaySize);

    }

    // инициализирует графический интерфейс Swing

    public void createAndShowGUI()
    {
        display.setLayout(new BorderLayout());
        JFrame myframe = new JFrame("Fractal Explorer");
        myframe.add(display, BorderLayout.CENTER);

        // Параметры кнопки
        resetButton = new JButton("Reset");

        ButtonHandler resetHandler = new ButtonHandler();
        resetButton.addActionListener(resetHandler);

        MouseHandler click = new MouseHandler();
        display.addMouseListener(click);

        // Операция закрытия окна по умолчанию
        myframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        myComboBox = new JComboBox();

        FractalGenerator mandelbrotFractal = new Mandelbrot();
        myComboBox.addItem(mandelbrotFractal);
        FractalGenerator tricornFractal = new Tricorn();
        myComboBox.addItem(tricornFractal);
        FractalGenerator burningShipFractal = new BurningShip();
        myComboBox.addItem(burningShipFractal);

        ButtonHandler fractalChooser = new ButtonHandler();
        myComboBox.addActionListener(fractalChooser);

        JPanel myPanel = new JPanel();
        JLabel myLabel = new JLabel("Fractal:");
        myPanel.add(myLabel);
        myPanel.add(myComboBox);
        myframe.add(myPanel, BorderLayout.NORTH);

        saveButton = new JButton("Save");
        JPanel myBottomPanel = new JPanel();
        myBottomPanel.add(saveButton);
        myBottomPanel.add(resetButton);
        myframe.add(myBottomPanel, BorderLayout.SOUTH);

        ButtonHandler saveHandler = new ButtonHandler();
        saveButton.addActionListener(saveHandler);

        // Данные операции правильно разметят содержимое окна, сделают его видимым

        myframe.pack();
        myframe.setVisible(true);
        myframe.setResizable(false);
    }

    // вывод на экран фрактала

    private void drawFractal()
    {
        enableUI(false);

        rowsRemaining = displaySize;

        for (int x=0; x<displaySize; x++){
            FractalWorker drawRow = new FractalWorker(x);
            drawRow.execute();
        }

    }

    // включает или отключает кнопки с выпадающим списком в пользовательском
    //интерфейсе на основе указанного параметра
    private void enableUI(boolean val) {
        myComboBox.setEnabled(val);
        resetButton.setEnabled(val);
        saveButton.setEnabled(val);
    }

    private class ButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (e.getSource() instanceof JComboBox) {
                JComboBox mySource = (JComboBox) e.getSource();
                fractal = (FractalGenerator) mySource.getSelectedItem();
                fractal.getInitialRange(range);
                drawFractal();

            }

            else if (command.equals("Reset")) {
                fractal.getInitialRange(range);
                drawFractal();
            }

            else if (command.equals("Save")) {

                JFileChooser myFileChooser = new JFileChooser();
                FileFilter extensionFilter =
                        new FileNameExtensionFilter("PNG Images", "png");
                myFileChooser.setFileFilter(extensionFilter);

                myFileChooser.setAcceptAllFileFilterUsed(false);

                int userSelection = myFileChooser.showSaveDialog(display);

                if (userSelection == JFileChooser.APPROVE_OPTION) {

                    java.io.File file = myFileChooser.getSelectedFile();
                    String file_name = file.toString();

                    try {
                        BufferedImage displayImage = display.getImage();
                        javax.imageio.ImageIO.write(displayImage, "png", file);
                    }

                    catch (Exception exception) {
                        JOptionPane.showMessageDialog(display,
                                exception.getMessage(), "Cannot Save Image",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
                else return;
            }
        }
    }

    // класс для обработки событий java.awt.event.MouseListener с дисплея

    private class MouseHandler extends MouseAdapter
    {
        //  При получении события о щелчке мышью, класс должен
        //отобразить пиксельные кооринаты щелчка в область фрактала, а затем вызвать
        //метод генератора recenterAndZoomRange() с координатами, по которым
        //щелкнули, и масштабом 0.5. Таким образом, нажимая на какое-либо место на
        //фрактальном отображении, вы увеличиваете его!

        @Override
        public void mouseClicked(MouseEvent e)
        {
            if (rowsRemaining != 0) {
                return;
            }
            int x = e.getX();
            double xCoord = fractal.getCoord(range.x,
                    range.x + range.width, displaySize, x);

            int y = e.getY();
            double yCoord = fractal.getCoord(range.y,
                    range.y + range.height, displaySize, y);

            fractal.recenterAndZoomRange(range, xCoord, yCoord, 0.5);
            drawFractal();
        }
    }

    private class FractalWorker extends SwingWorker<Object, Object>
    {

        int yCoordinate;

        int[] computedRGBValues;

        private FractalWorker(int row) {
            yCoordinate = row;
        }

        // метод, выполняющий фоновые операции
        protected Object doInBackground() {

            computedRGBValues = new int[displaySize];

            for (int i = 0; i < computedRGBValues.length; i++) {

                double xCoord = fractal.getCoord(range.x,
                        range.x + range.width, displaySize, i);
                double yCoord = fractal.getCoord(range.y,
                        range.y + range.height, displaySize, yCoordinate);

                int iteration = fractal.numIterations(xCoord, yCoord);

                if (iteration == -1){
                    computedRGBValues[i] = 0;
                }

                else {
                    float hue = 0.7f + (float) iteration / 200f;
                    int rgbColor = Color.HSBtoRGB(hue, 1f, 1f);

                    computedRGBValues[i] = rgbColor;
                }
            }
            return null;

        }
        // вызывается при завершении фоновой задачи
        protected void done() {

            for (int i = 0; i < computedRGBValues.length; i++) {
                display.drawPixel(i, yCoordinate, computedRGBValues[i]);
            }

            display.repaint(0, 0, yCoordinate, displaySize, 1);
            rowsRemaining--;
            if (rowsRemaining == 0) {
                enableUI(true);
            }
        }
    }

    public static void main(String[] args)
    {
        FractalExplorer displayExplorer = new FractalExplorer(800);
        displayExplorer.createAndShowGUI();
        displayExplorer.drawFractal();
    }
}