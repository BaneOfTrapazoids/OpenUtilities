package com.baneoftrapazoids.OpenUtilities.networking;

import com.baneoftrapazoids.OpenUtilities.util.Pair;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.Tuple;
import scala.Int;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class TextureRenderResponsePacket implements IMessage {

    private static final HashMap<Integer, byte[]> partialRenderedTextures = new HashMap<>();
    public static final HashMap<Integer, byte[]> renderedTextures = new HashMap<>();

    byte[] pixels;
    int length;
    int requestId;
    int chunk;

    public TextureRenderResponsePacket() {}

    public TextureRenderResponsePacket(byte[] _pixels, int _length, int _requestId, int _chunk) {
        pixels = _pixels;
        length = _length;
        requestId = _requestId;
        chunk = _chunk;

    }
    @Override
    public void fromBytes(ByteBuf buf) {
        length = buf.readInt();
        requestId = buf.readInt();
        chunk = buf.readInt();
        pixels = new byte[length];
        buf.readBytes(pixels);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(length);
        buf.writeInt(requestId);
        buf.writeInt(chunk);
        buf.writeBytes(pixels);
    }

    public static class Handler implements IMessageHandler<TextureRenderResponsePacket, IMessage> {

        @Override
        public IMessage onMessage(TextureRenderResponsePacket message, MessageContext ctx) {
            byte[] oldPixels = partialRenderedTextures.get(message.requestId);
            byte[] newPixels = message.pixels;
            if(oldPixels == null) {
                partialRenderedTextures.put(message.requestId, message.pixels);
            } else {
                newPixels = new byte[oldPixels.length + message.pixels.length];

                System.arraycopy(oldPixels, 0, newPixels, 0, oldPixels.length);
                System.arraycopy(message.pixels, 0, newPixels, oldPixels.length, message.pixels.length);
                System.out.println("NEW PIXELS contains " + Arrays.toString(newPixels));

                if(message.chunk != -1) {
                    partialRenderedTextures.put(message.requestId, newPixels);
                }
            }
            System.out.println("Received final packet on: " + message.requestId + " at time " + System.nanoTime());
            renderedTextures.put(message.requestId, newPixels);
            System.out.println(Thread.currentThread().toString() + Thread.currentThread().hashCode());

            return null;
        }
    }
}
