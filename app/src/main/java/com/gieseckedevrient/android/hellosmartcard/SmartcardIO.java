package com.gieseckedevrient.android.hellosmartcard;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.Session;
import nl.mansoft.openmobileapi.util.CommandApdu;
import nl.mansoft.openmobileapi.util.ResponseApdu;

import java.io.IOException;

public class SmartcardIO {
    private final String TAG = SmartcardIO.class.getSimpleName();
    private Session session;
    private Channel cardChannel;
    private SEService mSeService;
    private Context mContext;

    private void loge(String message) {
        Log.e(TAG, message);
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    private void logd(String message) {
        Log.d(TAG, message);
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    public ResponseApdu runAPDU(CommandApdu commandApdu) throws IOException {
        byte cmdApdu[] = commandApdu.toByteArray();
        ResponseApdu responseApdu = new ResponseApdu(cardChannel.transmit(cmdApdu));

        if (!responseApdu.isSuccess()) {
            Log.e(TAG,"ERROR: status: " + String.format("%04X", responseApdu.getSwValue()));
        }
        return responseApdu;
    }

    public void setup(Context context, SEService.CallBack callBack) throws IOException {
        mContext = context;
        mSeService = new SEService(context, callBack);
    }

    public void teardown() {
        Reader[] readers = mSeService.getReaders();
        closeChannel();
        if (readers.length < 1) {
            Log.e(TAG, "No readers found");
        } else {
            readers[0].closeSessions();
        }
        if (mSeService != null && mSeService.isConnected()) {
            mSeService.shutdown();
        }
    }

    public void closeChannel() {
        if (cardChannel != null && !cardChannel.isClosed()) {
            cardChannel.close();
        }
    }
    public void openChannel(byte aid[]) throws Exception {
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

    public void setSession() throws IOException {
        Log.d(TAG, "serviceConnected()");
        Log.d(TAG, "Retrieve available readers...");
        Reader[] readers = mSeService.getReaders();
        if (readers.length < 1) {
            loge("No readers found");
        } else {
            Log.d(TAG, "Create Session from the first reader...");
            session = readers[0].openSession();
        }
    }
}
