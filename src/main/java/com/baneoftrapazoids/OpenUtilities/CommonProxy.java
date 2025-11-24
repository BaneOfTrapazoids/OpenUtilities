package com.baneoftrapazoids.OpenUtilities;

import com.baneoftrapazoids.OpenUtilities.networking.TextureRenderRequestPacket;
import com.baneoftrapazoids.OpenUtilities.networking.TextureRenderResponsePacket;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        OpenUtilities.network = new SimpleNetworkWrapper(OpenUtilities.networkChannelName);
        OpenUtilities.network.registerMessage(TextureRenderRequestPacket.Handler.class, TextureRenderRequestPacket.class, 0, Side.CLIENT);
        OpenUtilities.network.registerMessage(TextureRenderResponsePacket.Handler.class, TextureRenderResponsePacket.class, 1, Side.SERVER);

        OpenUtilities.LOG.info(Config.greeting);
        OpenUtilities.LOG.info("I am MyMod at version " + Tags.VERSION);
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {}

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {}

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}
}
