package com.gieseckedevrient.android.hellosmartcard;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.simalliance.openmobileapi.SEService;
import nl.mansoft.openmobileapi.util.CommandApdu;
import nl.mansoft.openmobileapi.util.ResponseApdu;

import java.io.IOException;


public class MainActivity extends Activity implements SEService.CallBack {
	final String TAG = MainActivity.class.getSimpleName();
    public final static byte[] AID_HELLOAPPLET = {
            (byte) 0xD2, 0x76, 0x00, 0x01, 0x18, 0x00, 0x02,
            (byte) 0xFF, 0x49, 0x50, 0x25, (byte) 0x89,
            (byte) 0xC0, 0x01, (byte) 0x9B, 0x01
    };
    public final static byte[] AID_JOOSTAPPLET = { (byte) 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x01 };
    public final static byte[] AID_3GPP = { (byte) 0xA0, 0x00, 0x00, 0x00, (byte) 0x87 };
    public final static byte[] AID_ISOAPPLET = { (byte) 0xF2, (byte) 0x76, (byte) 0xA2, (byte) 0x88, (byte) 0xBC, (byte) 0xFB, (byte) 0xA6, (byte) 0x9D, (byte) 0x34, (byte) 0xF3, (byte) 0x10, (byte) 0x01 };

	private Button hello;
    private Button joost;
    private Button eduroam;
    private Button telecom;

    private SmartcardIO mSmartcardIO;

    void loge(String message) {
        Log.e(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void logd(String message) {
        Log.d(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    interface ResponseCallback {
        void responseCallback(byte response[]);
    }

    class ShowText implements  ResponseCallback {
        @Override
        public void responseCallback(byte response[]) {
            String hello = new String(response);
            logd(hello);
        }
    }

    private class MyOnClickListener implements OnClickListener {
        final String LOG_TAG = MyOnClickListener.class.getSimpleName();
        private byte[] mAid;
        private CommandApdu mCommandApdu[];
        private SmartcardIO mSmartcardIO;
        private ResponseCallback mResponseCallback;


        public MyOnClickListener(SmartcardIO smartcardIO, byte[] aid, CommandApdu commandApdu[], ResponseCallback responseCallback)  throws IOException {
            mSmartcardIO = smartcardIO;
            mAid = aid;
            mCommandApdu = commandApdu;
            mResponseCallback = responseCallback;
        }

        @Override
        public void onClick(View view) {
            try {
                mSmartcardIO.openChannel(mAid);
                int length = mCommandApdu.length;
                for (int i = 0; i < length; i++) {
                    CommandApdu commandApdu = mCommandApdu[i];
                    ResponseApdu response = mSmartcardIO.runAPDU(commandApdu);
                    if (response.isSuccess()) {
                        byte data[] = response.getData();
                        Log.d(LOG_TAG, "response: " + SmartcardIO.hex(data));
                        if (mResponseCallback != null) {
                            mResponseCallback.responseCallback(data);
                        }
                    } else {
                        Log.d(LOG_TAG, "response: null");
                    }
                }
            } catch (Exception e) {
               loge(e.getMessage());
            }
        }
    }

    private class EduroamOnClickListener implements OnClickListener {
        final String TAG = EduroamOnClickListener.class.getSimpleName();
        private SmartcardIO mSmartcardIO;

        public EduroamOnClickListener(SmartcardIO smartcardIO)  throws IOException {
            mSmartcardIO = smartcardIO;
        }

        public void showEduroam() throws Exception {
            mSmartcardIO.openChannel(AID_ISOAPPLET);
            Eduroam eduroam = new Eduroam(mSmartcardIO);
            ResponseApdu responseApdu = eduroam.readEduroam();
            if (responseApdu.isSuccess()) {
                byte data[] = responseApdu.getData();
                logd("user: " + Eduroam.readStringFromByteArray(data, Eduroam.OFFSET_USER));
                logd("password: " + Eduroam.readStringFromByteArray(data, Eduroam.OFFSET_PASSWORD));
            } else {
                loge("No credentials found on SIM card");
            }
            mSmartcardIO.closeChannel();
        }

        @Override
        public void onClick(View view) {
            try {
                showEduroam();
            } catch (Exception e) {
                loge(e.getMessage());
            }
        }
    }

    private class TelecomOnClickListener implements OnClickListener {
        final String TAG = EduroamOnClickListener.class.getSimpleName();
        private SmartcardIO mSmartcardIO;

        public TelecomOnClickListener(SmartcardIO smartcardIO)  throws IOException {
            mSmartcardIO = smartcardIO;
        }

        public void showTelecom() throws Exception {
            mSmartcardIO.openChannel(AID_3GPP);
            // select EXT1
            Telecom telecom = new Telecom(mSmartcardIO);
            logd("user: " + telecom.readData(Telecom.EF_EXT1, Telecom.RECORD_USER));
            logd("password: " + telecom.readData(Telecom.EF_EXT1, Telecom.RECORD_PASSWORD));
            mSmartcardIO.closeChannel();
        }

        @Override
        public void onClick(View view) {
            try {
                showTelecom();
            } catch (Exception e) {
                loge(e.getMessage());
            }
        }
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {

        try {
            super.onCreate(savedInstanceState);

            mSmartcardIO = new SmartcardIO();
            mSmartcardIO.setup(this, this);
            LinearLayout layout = new LinearLayout(this);
            layout.setLayoutParams(new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));

            hello = new Button(this);
            hello.setLayoutParams(new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));

            hello.setEnabled(false);
            hello.setText("Click Me");
            hello.setOnClickListener(new MyOnClickListener(
                    mSmartcardIO,
                    AID_HELLOAPPLET,
                    new CommandApdu[] { new CommandApdu((byte) 0x90, (byte) 0x10, (byte) 0x00, (byte) 0x00,(byte) 0x00 ) },
                    new ShowText()
                    ));
            layout.addView(hello);
            joost = new Button(this);
            joost.setLayoutParams(new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            joost.setEnabled(false);
            joost.setText("Joost");
            joost.setOnClickListener(new MyOnClickListener(
                    mSmartcardIO,
                    AID_JOOSTAPPLET,
                    new CommandApdu[] { new CommandApdu((byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00,(byte) 0x04 ) },
                    new ShowText()
                    ));
            layout.addView(joost);
            eduroam = new Button(this);
            eduroam.setLayoutParams(new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));

            eduroam.setEnabled(false);
            eduroam.setText("eduroam");
            eduroam.setOnClickListener(new EduroamOnClickListener(mSmartcardIO));
            layout.addView(eduroam);
            telecom = new Button(this);
            telecom.setLayoutParams(new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));

            telecom.setEnabled(false);
            telecom.setText("telecom");
            telecom.setOnClickListener(new TelecomOnClickListener(mSmartcardIO));
            layout.addView(telecom);
            setContentView(layout);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

    @Override
    protected void onDestroy() {
        mSmartcardIO.teardown();
        super.onDestroy();
    }

    @Override
    public void serviceConnected(SEService seService) {
        try {
            mSmartcardIO.setSession();
            hello.setEnabled(true);
            joost.setEnabled(true);
            eduroam.setEnabled(true);
            telecom.setEnabled(true);
        } catch (IOException e) {
            loge(e.getMessage());
        }
    }

}
