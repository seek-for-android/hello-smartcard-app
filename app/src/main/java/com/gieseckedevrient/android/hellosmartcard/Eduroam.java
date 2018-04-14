package com.gieseckedevrient.android.hellosmartcard;

import android.util.Log;

import org.simalliance.openmobileapi.util.CommandApdu;

import java.io.IOException;

public class Eduroam {
    private final String TAG = Eduroam.class.getSimpleName();
    public final static int OFFSET_USER = 0x00;
    public final static int OFFSET_PASSWORD = 0x20;
    private SmartcardIO mSmartcardIO;

    public Eduroam(SmartcardIO smartcardIO) {
        mSmartcardIO = smartcardIO;
    }

    public byte[] selectEduroam() throws IOException {
        CommandApdu c = new CommandApdu((byte)0x00, (byte)0xA4, (byte)0x00, (byte)0x00, new byte[] { 0x10, 0x00 });
        return mSmartcardIO.runAPDU(c);
    }

    public byte[] readEduroam() throws IOException {
        byte result[] = null;
        if (selectEduroam() != null) {
            Log.d(TAG, "reading eduroam");
            CommandApdu c = new CommandApdu((byte)0x00, (byte)0xB0, (byte)0x00, (byte)0x00);
            result = mSmartcardIO.runAPDU(c);
        }
        return result;
    }

    public boolean updateEduroam(byte[] data) throws IOException {
        boolean result = false;
        if (selectEduroam() != null) {
            Log.d(TAG, "updating eduroam");
            CommandApdu c = new CommandApdu((byte)0x00, (byte)0xD6, (byte)0x00, (byte)0x00, data);
            result = mSmartcardIO.runAPDU(c) != null;
        }
        return result;
    }

    public static String readStringFromByteArray(byte[] barr, int offset) {
        String result = "";
        byte b;
        int i = offset;
        while ((b = barr[i++]) != (byte) 0xFF) {
            result += (char) b;
        }
        return result;
    }

}
