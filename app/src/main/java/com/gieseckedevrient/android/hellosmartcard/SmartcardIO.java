package com.gieseckedevrient.android.hellosmartcard;

import android.content.Context;
import android.util.Log;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.Session;
import org.simalliance.openmobileapi.util.CommandApdu;
import org.simalliance.openmobileapi.util.ResponseApdu;

import java.io.IOException;

public class SmartcardIO implements SEService.CallBack{
    private final String TAG = SmartcardIO.class.getSimpleName();
    private Session session;
    private Channel cardChannel;
    private SEService mSeService;
    private SEService.CallBack mCallBack;

    public byte[] runAPDU(CommandApdu commandApdu) throws IOException {
        byte[] data = null;
        byte cmdApdu[] = commandApdu.toByteArray();
        ResponseApdu responseApdu = new ResponseApdu(cardChannel.transmit(cmdApdu));

        if (responseApdu.isSuccess()) {
            data = responseApdu.getData();
        } else {
            Log.e(TAG,"ERROR: status: " + String.format("%04X", responseApdu.getSwValue()));
        }
        return data;
    }

    public void setup(Context context, SEService.CallBack callBack) throws IOException {
        mCallBack = callBack;
        mSeService = new SEService(context, this);
    }

    public void teardown() {
        Reader[] readers = mSeService.getReaders();
        if (readers.length < 1) {
            Log.e(TAG, "No readers found");
        }
        closeChannel();
        readers[0].closeSessions();
        if (mSeService != null && mSeService.isConnected()) {
            mSeService.shutdown();
        }
    }

    public void closeChannel() {
        if (cardChannel != null && !cardChannel.isClosed()) {
            cardChannel.close();
        }
    }
    public void openChannel(byte aid[]) throws IOException {
        closeChannel();
        cardChannel = session.openLogicalChannel(aid);
    }

    public static String hex2(int hex) {
        return String.format("%02X", hex & 0xff);
    }

    public static String hex(byte[] barr) {
        String result;
        if (barr == null) {
            result = "null";
        } else {
            result = "";
            for (byte b : barr) {
                result += " " + hex2(b);
            }
        }
        return result;
    }

    @Override
    public void serviceConnected(SEService seService) {
        try {
            Log.d(TAG, "serviceConnected()");
            Log.d(TAG, "Retrieve available readers...");
            Reader[] readers = mSeService.getReaders();
            if (readers.length < 1) {
                Log.e(TAG, "No readers found");
            }
            Log.d(TAG, "Create Session from the first reader...");
            session = readers[0].openSession();
            Log.d(TAG, "Create logical channel within the session...");
            if (mCallBack != null) {
                mCallBack.serviceConnected(seService);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
