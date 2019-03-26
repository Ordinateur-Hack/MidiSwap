package Usb;

import java.nio.ByteBuffer;

public class RecvBuffer {
    private ByteBuffer buffer;
    private BufferChangeListener listener;
    public interface BufferChangeListener {
         void onUpdateByte(ByteBuffer byteBuffer);
    }

    public RecvBuffer(BufferChangeListener listener) {
        this.listener = listener;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
        if (listener != null) {
            listener.onUpdateByte(buffer);
        }
    }

}
