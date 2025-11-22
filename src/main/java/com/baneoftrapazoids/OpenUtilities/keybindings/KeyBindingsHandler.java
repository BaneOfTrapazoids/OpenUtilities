package com.baneoftrapazoids.OpenUtilities.keybindings;

import com.baneoftrapazoids.OpenUtilities.OpenUtilities;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class KeyBindingsHandler {

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;

        if (KeyBindings.saveTexture.isPressed()) {
            ItemStack item = player.getHeldItem();
            String name = item.getItem().getIconIndex(item).getIconName();
            System.out.println("FOUND AN ITEM TEXTURE: " + name);
            TextureMap map = ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationItemsTexture));
            TextureAtlasSprite texture = map.getAtlasSprite(name);
            ByteBuffer pixels = BufferUtils.createByteBuffer(Math.max(texture.getIconWidth() * texture.getIconHeight(), 1024));
            Framebuffer buffer = Minecraft.getMinecraft().c



            byte[] texData = new byte[pixels.remaining()];
            pixels.get(texData);
            BufferedImage img = new BufferedImage(texture.getIconWidth(), texture.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            System.out.println(texture);
            for(int i = 0; i < texData.length; i += 4) {
                int rgb = ((255 - texData[i+3]) << 24) | (texData[i] << 16) | (texData[i+1] << 8) | texData[i+2];
                System.out.println(i / (4 *texture.getIconWidth()) + ", " + ((i / 4) % texture.getIconWidth()) + ": " + new Color(rgb) + ", alpha: " + texData[i + 3]);
                img.setRGB(i / (4 * texture.getIconWidth()), (i / 4) % texture.getIconWidth(), rgb);
            }

            try {
                OpenUtilities.LOG.debug("TEXTURE LENGTH: " + texData.length);
                File output = new File(name);
                ImageIO.write(img, "png", output);
            }
            catch (IOException e) {
                OpenUtilities.LOG.error("what!", e);
            }

        }
    }
}
