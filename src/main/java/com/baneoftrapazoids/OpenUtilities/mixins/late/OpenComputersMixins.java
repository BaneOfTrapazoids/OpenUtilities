package com.baneoftrapazoids.OpenUtilities.mixins.late;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.worlddata.WorldData;
import appeng.items.parts.ItemMultiPart;
import appeng.me.GridAccessException;
import appeng.parts.CableBusStorage;
import appeng.tile.misc.TileSecurity;
import com.baneoftrapazoids.OpenUtilities.OpenUtilities;
import com.baneoftrapazoids.OpenUtilities.networking.TextureRenderRequestPacket;
import com.baneoftrapazoids.OpenUtilities.networking.TextureRenderResponsePacket;
import com.baneoftrapazoids.OpenUtilities.util.Internet;
import com.baneoftrapazoids.OpenUtilities.util.Pair;
import com.baneoftrapazoids.OpenUtilities.util.TextureHandler;
import com.gtnewhorizon.gtnhlib.client.renderer.TessellatorManager;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.integration.appeng.NetworkControl;
import li.cil.oc.integration.appeng.UpgradeAE;
import li.cil.oc.util.ResultWrapper;
import li.cil.oc.util.ResultWrapper$;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import scala.collection.JavaConverters;
import scala.collection.immutable.Seq;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static com.baneoftrapazoids.OpenUtilities.util.TextureHandler.sanitizeName;

@Mixin(UpgradeAE.class)
public abstract class OpenComputersMixins implements NetworkControl<TileSecurity> {

    private byte[] getItemTextureRender(ItemStack itemStack, EntityPlayerMP renderer) throws InterruptedException, TimeoutException {
        List<EntityPlayerMP> players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        int reqId = itemStack.getUnlocalizedName().hashCode();
        if(players.contains(renderer)) {
            System.out.println("Sending off PACKET at time " + System.nanoTime());
            System.out.println(Thread.currentThread().toString() + Thread.currentThread().hashCode());
            OpenUtilities.network.sendTo(new TextureRenderRequestPacket(itemStack, reqId), renderer);

            long start = System.nanoTime();
            byte[] pixels;

            while(System.nanoTime() - start < 800_000_000) {
                pixels = TextureRenderResponsePacket.renderedTextures.get(reqId);
                if(pixels != null) {
                    break;
                }
            }

            pixels = TextureRenderResponsePacket.renderedTextures.get(reqId);

            if(pixels != null) {
                TextureRenderResponsePacket.renderedTextures.put(reqId, null);
                return pixels;
            } else {
                throw new TimeoutException("Timeout on image render " + itemStack.getDisplayName());
            }
        } else {
            throw new PlayerNotFoundException("Owner of ME system (" + renderer.getGameProfile().getName() + ") was not online to process render request...");
        }
    }

    @Callback(doc = "HELLO FROM OC MIXINS!")
    public Object[] getAllItemTextureRendersForServer(Context ctx, Arguments args) {
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

        if(items != null) {
            for (IAEItemStack itemStack : items) {
                try {
                    byte[] render = getItemTextureRender(itemStack.getItemStack(), owner);
                    Internet.executePost(url, render);
                } catch (PlayerNotFoundException | MalformedURLException e) {
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
}
