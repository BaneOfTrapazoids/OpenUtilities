package com.baneoftrapazoids.OpenUtilities.mixins.late;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.worlddata.WorldData;
import appeng.me.GridAccessException;
import appeng.tile.misc.TileSecurity;
import com.baneoftrapazoids.OpenUtilities.OpenUtilities;
import com.baneoftrapazoids.OpenUtilities.networking.TextureRenderRequestPacket;
import com.baneoftrapazoids.OpenUtilities.util.Internet;
import com.baneoftrapazoids.OpenUtilities.util.TextureHandler;
import com.google.gson.JsonObject;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.integration.appeng.NetworkControl;
import li.cil.oc.integration.appeng.UpgradeAE;
import li.cil.oc.util.ResultWrapper;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import scala.collection.JavaConverters;

import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Mixin(UpgradeAE.class)
public abstract class OpenComputersMixins implements NetworkControl<TileSecurity> {

    @Callback(doc = "function() - string: error codes")
    public Object[] sendAllItemTextureRendersForWebServer(Context ctx, Arguments args) {
        ArrayList<Object> res = new ArrayList<>();
        int playerId;
        IItemList<IAEItemStack> items;

        try {
            // For some reason, AE2 uses player IDs instead of UUIDs??
            // Also, it says not to use WorldData.instance(), but it does, and I don't see a better alternative...
            playerId = this.tile().getProxy().getSecurity().getOwner();
            items = this.tile().getProxy().getStorage().getItemInventory().getStorageList();
        } catch (GridAccessException e) {
            res.add(e.toString());
            return ResultWrapper.result(JavaConverters.asScalaIteratorConverter(Arrays.stream(res.toArray()).iterator()).asScala().toSeq());
        }

        String url = args.checkString(0);
        EntityPlayerMP owner = (EntityPlayerMP) WorldData.instance().playerData().getPlayerFromID(playerId);
        List<EntityPlayerMP> players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;

        if(items != null && players.contains(owner)) {
            for (IAEItemStack itemStack : items) {
                try {
                    int reqId = itemStack.getUnlocalizedName().hashCode();
                    if(players.contains(owner)) {
                        OpenUtilities.network.sendTo(new TextureRenderRequestPacket(itemStack.getItemStack(), reqId, url), owner);
                    } else {
                        throw new PlayerNotFoundException("Owner of ME system (" + owner.getGameProfile().getName() + ") was not online to process render request...");
                    }
                } catch (PlayerNotFoundException e) {
                    res.add(e.toString());
                    break;
                } catch (Exception e) {
                    res.add(e.toString());
                }
            }
        }
        else {
            res.add("Could not find items :(");
        }
        return ResultWrapper.result(JavaConverters.asScalaIteratorConverter(Arrays.stream(res.toArray()).iterator()).asScala().toSeq());
    }

    @Callback(doc = "function() - bwuh?")
    public Object[] sendItemStacksToWebServer(Context ctx, Arguments args) {
        ArrayList<Object> res = new ArrayList<>();
        IItemList<IAEItemStack> items;
        try {
            items = this.tile().getProxy().getStorage().getItemInventory().getStorageList();
        } catch (GridAccessException e) {
            res.add(e.toString());
            return ResultWrapper.result(JavaConverters.asScalaIteratorConverter(Arrays.stream(res.toArray()).iterator()).asScala().toSeq());
        }

        JsonObject storageJson = new JsonObject();
        for(IAEItemStack itemStack: items) {
            JsonObject itemJson = new JsonObject();
            String internal_name = TextureHandler.sanitizedName(itemStack.getItemStack());
            itemJson.addProperty("internal_name", internal_name);
            itemJson.addProperty("display_name", itemStack.getDisplayName());
            itemJson.addProperty("count", itemStack.getStackSize());
            itemJson.addProperty("craftable", itemStack.isCraftable());
            storageJson.addProperty(internal_name, itemJson.toString());
        }

        try {
            Internet.executePost(args.checkString(0), storageJson.toString().getBytes(StandardCharsets.UTF_8), new HashMap<>(), Internet.JSON);
        } catch (IOException | InterruptedException e) {
            res.add(e.toString());
        }

        return ResultWrapper.result(JavaConverters.asScalaIteratorConverter(Arrays.stream(res.toArray()).iterator()).asScala().toSeq());
    }
}
