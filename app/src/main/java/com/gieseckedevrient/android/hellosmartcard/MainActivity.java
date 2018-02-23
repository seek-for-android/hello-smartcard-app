package com.gieseckedevrient.android.hellosmartcard;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import org.simalliance.openmobileapi.*;


public class MainActivity extends Activity implements SEService.CallBack {

	final String LOG_TAG = "HelloSmartcard";

	private SEService seService;

	private Button button;
	private Button joost;

	private static class MyOnClickListener implements OnClickListener{
		final String LOG_TAG = "HelloSmartcard";
		private Context mContext;
		private byte[] mAid;
		private byte[] mInstruction;
		private SEService mSeService;

		public MyOnClickListener(Context context, byte[] aid, byte[] instruction, SEService seService) {
			mContext = context;
			mAid = aid;
			mInstruction = instruction;
			mSeService = seService;
		}

		private void loge(String message) {
			Log.e(LOG_TAG, message);
			Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
		}

		private void logd(String message) {
			Log.d(LOG_TAG, message);
			Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
		}

		@Override
		public void onClick(View view) {
			try {
				Log.d(LOG_TAG, "Retrieve available readers...");
				Reader[] readers = mSeService.getReaders();
				if (readers.length < 1) {
					loge("No readers found");
				}
				Log.d(LOG_TAG, "Create Session from the first reader...");
				Session session = readers[0].openSession();
				Log.d(LOG_TAG, "Create logical channel within the session...");
				Channel channel = session.openLogicalChannel(mAid);

				Log.d(LOG_TAG, "Send HelloWorld APDU command");
				byte[] respApdu = channel.transmit(mInstruction);
				channel.close();
				if (respApdu == null) {
					loge("Error: respApdu == null");
				} else {
					int length = respApdu.length;
					int status = (respApdu[length - 2] & 0xff) << 8 | (respApdu[length - 1] & 0xff);
					if (status == 0x9000) {
						// Parse response APDU and show text but remove SW1 SW2 first
						byte[] helloStr = new byte[respApdu.length - 2];
						System.arraycopy(respApdu, 0, helloStr, 0, respApdu.length - 2);
						String hello = new String(helloStr);
						logd(hello);
					} else {
						loge("status: " + Integer.toString(status, 16));
					}
				}
			} catch (Exception e) {
				loge("Exception:" + e.getMessage());
			}
		}
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		try {
			Log.d(LOG_TAG, "creating SEService object");
			seService = new SEService(this, this);
		} catch (SecurityException e) {
			Log.e(LOG_TAG, "Binding not allowed, uses-permission org.simalliance.openmobileapi.SMARTCARD?");
		} catch (Exception e) {
			Log.e(LOG_TAG, "Exception: " + e.getMessage());
		}

		LinearLayout layout = new LinearLayout(this);
		layout.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, 
				LayoutParams.WRAP_CONTENT));

		button = new Button(this);
		button.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		button.setText("Click Me");
		button.setEnabled(false);
		button.setOnClickListener(new MyOnClickListener(
				MainActivity.this,
				new byte[] {
						(byte) 0xD2, 0x76, 0x00, 0x01, 0x18, 0x00, 0x02,
						(byte) 0xFF, 0x49, 0x50, 0x25, (byte) 0x89,
						(byte) 0xC0, 0x01, (byte) 0x9B, 0x01 },
				new byte[] {
						(byte) 0xA0, 0x10, 0x00, 0x00, 0x00 },
				seService
				));

		layout.addView(button);
		joost = new Button(this);
		joost.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		joost.setText("Joost");
		joost.setEnabled(false);
		joost.setOnClickListener(new MyOnClickListener(
				MainActivity.this,
				new byte[] {
						(byte)0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x01 },
				new byte[] {
						(byte)0x80, 0x00, 0x00, 0x00, 0x04 },
				seService
		));
		layout.addView(joost);
		setContentView(layout);

		
	}

	@Override
	protected void onDestroy() {
		if (seService != null && seService.isConnected()) {
			seService.shutdown();
		}
		super.onDestroy();
	}

	public void serviceConnected(SEService service) {
		Log.d(LOG_TAG, "serviceConnected()");
		button.setEnabled(true);
		joost.setEnabled(true);
	}
}
