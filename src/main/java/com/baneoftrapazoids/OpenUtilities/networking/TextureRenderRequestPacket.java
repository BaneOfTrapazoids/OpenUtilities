package com.baneoftrapazoids.OpenUtilities.networking;

import com.baneoftrapazoids.OpenUtilities.OpenUtilities;
import com.baneoftrapazoids.OpenUtilities.util.TextureHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class TextureRenderRequestPacket implements IMessage {

    ItemStack item;
    int requestId;

    public TextureRenderRequestPacket() {}

    public TextureRenderRequestPacket(ItemStack _item, int _requestId) {
        item = _item;
        requestId = _requestId;
    }
    @Override
    public void fromBytes(ByteBuf buf) {
        item = ByteBufUtils.readItemStack(buf);
        requestId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, item);
        buf.writeInt(requestId);
    }

    public static class Handler implements IMessageHandler<TextureRenderRequestPacket, TextureRenderResponsePacket> {

        @Override
        public TextureRenderResponsePacket onMessage(TextureRenderRequestPacket message, MessageContext ctx) {
            try {
                byte[] pixels = TextureHandler.renderTexture(message.item);
                ByteBuffer pixelsBuf = ByteBuffer.wrap(pixels);

                for(int i = 0; i < (int) Math.ceil(pixels.length / 32000.0) - 1; i++) {
                    byte[] chunk = new byte[32000];
                    pixelsBuf.get(chunk, 0, 32000);
                    OpenUtilities.network.sendToServer(new TextureRenderResponsePacket(chunk, 32000, message.requestId, i));
                }

                byte[] lastChunk = new byte[pixelsBuf.remaining()];
                pixelsBuf.get(lastChunk);

                return new TextureRenderResponsePacket(lastChunk, lastChunk.length, message.requestId, -1);
            }
            catch (Exception e) {
                System.out.println("Error" + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }
}
