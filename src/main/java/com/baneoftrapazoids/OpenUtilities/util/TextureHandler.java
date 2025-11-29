package com.baneoftrapazoids.OpenUtilities.util;

import appeng.items.parts.ItemMultiPart;
import codechicken.nei.guihook.GuiContainerManager;
import com.baneoftrapazoids.OpenUtilities.OpenUtilities;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.*;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TextureHandler {
    public static byte[] renderTexture(ItemStack item) throws LWJGLException {
        IIcon icon = item.getItem().getIconIndex(item);
        int imageDim = Math.max(icon.getIconWidth(), 64);

//        if(item.getItem() instanceof ItemBlock || item.getItem() instanceof ItemMultiPart) {
//            imageDim = 64;
//        }
        //imageDim = Math.max(imageDim, 32);

        ByteBuffer rawPixels = BufferUtils.createByteBuffer(4 * imageDim * imageDim);

        Framebuffer buffer = new Framebuffer(imageDim, imageDim, true);
        buffer.bindFramebuffer(true);
        OpenGlHelper.func_153171_g(GL30.GL_READ_FRAMEBUFFER, buffer.framebufferObject);

        setupRenderState();
        ItemStack drawItem = item.copy();
        drawItem.stackSize = 1;
        GuiContainerManager.drawItem(0, 0, drawItem);

        GL11.glReadPixels(0, 0, imageDim, imageDim, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, rawPixels);

        // OpenGL draws things upside down, so we flip it again
        byte[] result = new byte[4 * imageDim * imageDim];
        byte[] src = new byte[4 * imageDim * imageDim];
        rawPixels.get(src);
        for(int i = 0; i < imageDim; i++) {
            byte[] row = Arrays.copyOfRange(src, i * 4*imageDim, (i+1)*4*imageDim);
            System.arraycopy(row, 0, result, (imageDim-i-1) * 4 * imageDim, row.length);
        }
        return result;
    }

    public static void saveTexture(ItemStack item, String path) throws LWJGLException {
        byte[] pixelsBytes = renderTexture(item);

        IIcon icon = item.getItem().getIconIndex(item);
        int imageDim = icon.getIconWidth();

        BufferedImage img = new BufferedImage(imageDim, imageDim, BufferedImage.TYPE_INT_ARGB);
        readBytesToImg(pixelsBytes, imageDim, img);

        try {
            OpenUtilities.LOG.info("TEXTURE LENGTH: " + imageDim * imageDim);
            File output = new File(path + sanitizedName(item) + ".png");
            ImageIO.write(img, "png", output);
        }
        catch (IOException e) {
            OpenUtilities.LOG.error("what!", e);
        }
    }

    public static BufferedImage transformImg(BufferedImage img) {
        BufferedImage dst = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        AffineTransform trans = new AffineTransform();
        trans.concatenate(AffineTransform.getScaleInstance(1, -1));
        trans.concatenate(AffineTransform.getTranslateInstance(0, -img.getHeight()));
        new AffineTransformOp(trans, AffineTransformOp.TYPE_BILINEAR).filter(img, dst);
        return dst;
    }

    public static void readBytesToImg(byte[] pixelsBytes, int imageDim, BufferedImage img) {
        int[] pixels = new int[imageDim * imageDim];
        ByteBuffer buffer = BufferUtils.createByteBuffer(pixelsBytes.length).put(pixelsBytes);
        buffer.position(0);
        buffer.asIntBuffer().get(pixels);
        img.setRGB(0, 0, imageDim, imageDim, pixels, 0, imageDim);
    }

    // Deal with differently named items later
    public static String sanitizedName(ItemStack item) {
        String name = item.getUnlocalizedName() + "_" + item.getItemDamage();
        return name.replace(':', '_').replace('.', '_');
    }

    private static void setupRenderState() {
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0, 1.0, 1.0, 0.0, -100.0, 100.0);
        double scaleFactor = 1 / 16.0;
        GL11.glScaled(scaleFactor, scaleFactor, scaleFactor);
        // We need to end with the model-view matrix selected. It's what the rendering code expects.
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    }
}
