package com.gieseckedevrient.android.hellosmartcard;

import android.util.Log;

import org.simalliance.openmobileapi.util.CommandApdu;

import java.io.IOException;

public class Telecom {
    private final String TAG = Telecom.class.getSimpleName();
    private SmartcardIO mSmartcardIO;
    public final static int EXT_RECORD_SIZE = 13;
    public final static int EF_EXT1 = 0x6F4A;
    public final static int EF_EXT2 = 0x6F4B;
    public final static int RECORD_USER = 1;
    public final static int RECORD_PASSWORD = 3;

    public Telecom(SmartcardIO smartcardIO) {
        mSmartcardIO = smartcardIO;
    }

    public byte[] readRecord(int record) throws IOException {
        CommandApdu c = new CommandApdu((byte)0x00, (byte)0xB2, (byte)record, (byte)0x04);
        return mSmartcardIO.runAPDU(c);
    }

    public void readRecords() throws IOException {
        int record = 1;
        byte data[];
        while ((data = readRecord(record++)) != null) {
            Log.d(TAG, SmartcardIO.hex(data));
        }
    }
    public static byte hi(int x) {
        return (byte) (x >> 8);
    }

    public static byte lo(int x) {
        return (byte) (x & 0xff);
    }

    public byte[] selectTelecom(int fid) throws IOException {
        CommandApdu c = new CommandApdu((byte)0x00, (byte)0xA4, (byte)0x08, (byte)0x04, new byte[] { 0x7f, 0x10, hi(fid), lo(fid) });
        return mSmartcardIO.runAPDU(c);
    }

    public byte[] readTelecomRecord(int fid, int record) throws IOException {
        byte result[] = null;
        if (selectTelecom(fid) != null) {
            Log.d(TAG, String.format("reading telecom %04X", fid));
            result = readRecord(record);
        }
        return result;
    }

    public void readTelecomRecords(int fid) throws IOException {
        if (selectTelecom(fid) != null) {
            Log.d(TAG, String.format("reading telecom %04X", fid));
            readRecords();
        }
    }
    public void updateRecord(int record, byte[] data) throws IOException {
        CommandApdu c = new CommandApdu((byte)0x00, (byte)0xdc, (byte)record, (byte)0x04, data);
        mSmartcardIO.runAPDU(c);
    }

    public void writeTelecom(int fid, int record, byte[] data) throws IOException {
        if (selectTelecom(fid) != null) {
            Log.d(TAG, String.format("write telecom %04X, record %d", fid, record));
            updateRecord(record, data);
        }
    }

    public String readData(int ext, int recordnr) throws IOException {
        String result = "";
        byte record[];
        while ((record = readTelecomRecord(ext, recordnr++)) != null) {
            for (int i = 1; i < EXT_RECORD_SIZE; i++) {
                int val = (record[i] & 0xFF);
                if (val == 0xFF) {
                    return result;
                }
                result += String.valueOf((char) val);
            }
        }
        return result;
    }

}
