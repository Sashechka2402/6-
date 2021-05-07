package com.company;

import javax.swing.JComponent;
import java.awt.image.BufferedImage;
import java.awt.*;

// Этот класс позволяет нам отображать наши фракталы.

class JImageDisplay extends JComponent
{
    // управляет изображением, содержимое которого можно записать.

    private BufferedImage displayImage;

    // принимает целочисленные значения ширины и высоты, и инициализирует объект BufferedImage новым
    //изображением с этой шириной и высотой, и типом изображения TYPE_INT_RGB.

    public BufferedImage getImage() {
        return displayImage;
    }

    public JImageDisplay(int width, int height)
    {
        displayImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Dimension imageDimension = new Dimension(width, height);
        super.setPreferredSize(imageDimension);

    }

    // код для отрисовки

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.drawImage(displayImage, 0, 0, displayImage.getWidth(), displayImage.getHeight(), null);
    }

    //  устанавливает все пиксели изображения в черный цвет

    public void clearImage()
    {
        int[] blankArray = new int[getWidth() * getHeight()];
        displayImage.setRGB(0, 0, getWidth(), getHeight(), blankArray, 0, 1);
    }

    // устанавливает пиксель в определенный цвет

    public void drawPixel(int x, int y, int rgbColor)
    {
        displayImage.setRGB(x, y, rgbColor);
    }
}
