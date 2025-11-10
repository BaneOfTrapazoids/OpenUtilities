package com.baneoftrapazoids.OpenUtilities;

import com.baneoftrapazoids.OpenUtilities.keybindings.KeyBindings;
import com.baneoftrapazoids.OpenUtilities.keybindings.KeyBindingsHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        FMLCommonHandler.instance().bus().register(new KeyBindingsHandler());
        KeyBindings.init();
    }
}
