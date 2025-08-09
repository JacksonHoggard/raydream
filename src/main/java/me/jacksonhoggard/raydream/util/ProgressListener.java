package me.jacksonhoggard.raydream.util;

import java.awt.image.BufferedImage;

public interface ProgressListener {

    void progressUpdated(int progress, BufferedImage image);

}