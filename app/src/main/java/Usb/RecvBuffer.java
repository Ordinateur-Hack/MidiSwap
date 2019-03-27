package Usb;

import android.util.Log;

import java.nio.ByteBuffer;

public class RecvBuffer {
    private BufferChangeListener listener;

    public interface BufferChangeListener {
        void onUpdateByte(ByteBuffer byteBuffer);
    }

    private static final String TAG = RecvBuffer.class.getSimpleName();

    public RecvBuffer(BufferChangeListener listener) {
        this.listener = listener;
    }

    public void updateBuffer(ByteBuffer buffer) {
        if (listener == null) {
            Log.d(TAG, "No listener set for ReceiveBuffer, will not notify any listener");
            return;
        }
        listener.onUpdateByte(buffer);
    }

}
