package com.baneoftrapazoids.OpenUtilities.keybindings;

import codechicken.nei.guihook.GuiContainerManager;
import com.baneoftrapazoids.OpenUtilities.OpenUtilities;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class KeyBindingsHandler {

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;

        if (KeyBindings.saveTexture.isPressed()) {
            ItemStack item = player.getHeldItem();
            IIcon icon = item.getItem().getIconIndex(item);
            int imageDim = icon.getIconWidth();
            System.out.println("FOUND AN ITEM TEXTURE: " + icon.getIconName() + " of dimensions: " + icon.getIconWidth() + "x" + icon.getIconHeight());

            if(item.getItem() instanceof ItemBlock) {
                imageDim = 128;
            }

            ByteBuffer rawPixels = BufferUtils.createByteBuffer(Math.max((4 * imageDim * imageDim), 1024));
            Framebuffer buffer = new Framebuffer(imageDim, imageDim, true);
            buffer.bindFramebuffer(true);
            OpenGlHelper.func_153171_g(GL30.GL_READ_FRAMEBUFFER, buffer.framebufferObject);

            OpenUtilities.LOG.info("Built both buffers");

            setupRenderState();
            GuiContainerManager.drawItem(0, 0, item);

            OpenUtilities.LOG.info("Drew item");
            GL11.glReadPixels(0, 0, imageDim, imageDim, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, rawPixels);
            OpenUtilities.LOG.info("Read pixels to buffer");
            int[] pixels = new int[imageDim * imageDim];
            rawPixels.asIntBuffer().get(pixels);
            OpenUtilities.LOG.info("Converted to int[]");

            for(int pixel: pixels) {
                Color color = new Color(pixel, true);
                OpenUtilities.LOG.info(color + "a=" + color.getAlpha());
            }

            BufferedImage img = new BufferedImage(imageDim, imageDim, BufferedImage.TYPE_INT_ARGB);
            OpenUtilities.LOG.info("Made buffered image");
            img.setRGB(0, 0, imageDim, imageDim, pixels, 0, imageDim);
            OpenUtilities.LOG.info("Set RGB");

            img = transformImg(img);



            try {
                OpenUtilities.LOG.info("TEXTURE LENGTH: " + pixels.length);
                File output = new File(sanitizeName(icon.getIconName()) + ".png");
                ImageIO.write(img, "png", output);
            }
            catch (IOException e) {
                OpenUtilities.LOG.error("what!", e);
            }

        }
    }

    public BufferedImage transformImg(BufferedImage img) {
        BufferedImage dst = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        AffineTransform trans = new AffineTransform();
        trans.concatenate(AffineTransform.getScaleInstance(1, -1));
        trans.concatenate(AffineTransform.getTranslateInstance(0, -img.getHeight()));
        new AffineTransformOp(trans, AffineTransformOp.TYPE_BILINEAR).filter(img, dst);
        return dst;
    }

    public String sanitizeName(String name) {
        return name.replace(':', '_').replace('.', '_');
    }

    private void setupRenderState() {
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
