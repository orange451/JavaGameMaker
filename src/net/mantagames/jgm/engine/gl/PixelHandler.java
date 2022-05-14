package net.mantagames.jgm.engine.gl;

import java.nio.ByteBuffer;

public interface PixelHandler {
    public int getBytesPerPixel();
    public void handlePixel(ByteBuffer b, int pixel);
}