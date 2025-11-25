package com.baneoftrapazoids.OpenUtilities.mixins.late;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.items.parts.ItemMultiPart;
import appeng.me.GridAccessException;
import appeng.parts.CableBusStorage;
import appeng.tile.misc.TileSecurity;
import com.baneoftrapazoids.OpenUtilities.OpenUtilities;
import com.baneoftrapazoids.OpenUtilities.networking.TextureRenderRequestPacket;
import com.baneoftrapazoids.OpenUtilities.networking.TextureRenderResponsePacket;
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
import java.time.Instant;
import java.util.*;

import static com.baneoftrapazoids.OpenUtilities.util.TextureHandler.sanitizeName;

@Mixin(UpgradeAE.class)
public abstract class OpenComputersMixins implements NetworkControl<TileSecurity> {
    @Callback(doc = "HELLO FROM OC MIXINS!")
    public Object[] getItemsWithTextures(Context ctx, Arguments args) throws GridAccessException {
        ArrayList<Object> res = new ArrayList<>();
        IItemList<IAEItemStack> items = this.tile().getProxy().getStorage().getItemInventory().getStorageList();
        if(items != null) {
            for (IAEItemStack itemStack : items) {
                System.out.println(itemStack.getDisplayName());

                try {
                    List<EntityPlayerMP> players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
                    int reqId = itemStack.hashCode();
                    if(!players.isEmpty()) {
                        OpenUtilities.network.sendTo(new TextureRenderRequestPacket(itemStack.getItemStack(), reqId), players.get(0));
                        byte[] pixels = null;
                        long start = System.nanoTime();
                        while(System.nanoTime() - start < 500_000_000) {
                            Pair<Integer, byte[]> pair = TextureRenderResponsePacket.renderedTextures.get(reqId);
                            if(pair != null && pair.second != null && pair.first == -1) {
                                pixels = pair.second;
                                break;
                            }
                            Thread.sleep(0, 20_000);
                        }
                        if(pixels != null) {
                            int imageDim = itemStack.getItemStack().getIconIndex().getIconWidth();
                            if(itemStack.getItem() instanceof ItemBlock || itemStack.getItem() instanceof ItemMultiPart) {
                                imageDim = 128;
                            }
                            BufferedImage img = new BufferedImage(imageDim, imageDim, BufferedImage.TYPE_INT_ARGB);
                            TextureHandler.readBytesToImg(pixels, imageDim, img);

                            try {
                                OpenUtilities.LOG.info("TEXTURE LENGTH: " + imageDim * imageDim);
                                File output = new File("savedTextures/" + sanitizeName(itemStack.getItemStack().getIconIndex().getIconName() + "_" + itemStack.getItemDamage()) + ".png");
                                ImageIO.write(img, "png", output);
                            }
                            catch (IOException e) {
                                OpenUtilities.LOG.error("what!", e);
                            }

                        } else {
                            res.add("Timeout on image render " + itemStack.getItemStack().getDisplayName());
                        }
                    } else {
                        res.add("No players were online...");
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    res.add("Ran into error:" + e);
                }
            }
        }
        else {
            res.add("Could not find items :(");
        }
        return ResultWrapper.result(JavaConverters.asScalaIteratorConverter(Arrays.stream(res.toArray()).iterator()).asScala().toSeq());
    }
}
