package com.baneoftrapazoids.OpenUtilities.keybindings;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.awt.image.BufferedImage;
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
            TextureAtlasSprite texture = ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationItemsTexture)).getAtlasSprite(name);
            int[][] sprite = texture.getFrameTextureData(0);


            System.out.println(Arrays.deepToString(sprite));
        }
    }
}
