package com.baneoftrapazoids.OpenUtilities.keybindings;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class KeyBindings {
    public static final KeyBinding saveTexture;

    static {
        saveTexture = new KeyBinding("key.saveTexture", Keyboard.KEY_F9, "Open Utilities");
    }
}
