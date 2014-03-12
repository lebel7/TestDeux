package com.proper.TestDeux;


import java.io.IOException;
import java.io.InputStream;
//import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.app.DownloadManager;
import android.content.*;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import com.proper.TestDeux.data.Product;
import com.proper.TestDeux.data.ScanTest;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
//import com.google.zxing.oned.Code128Writer;


import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

//import com.proper.appdeux.data.Product;
//import com.proper.appdeux.data.ScanTest;


/**
 * Created by Lebel on 13/02/14.
 */
public class ActDetails extends Activity implements IWifiMangerStatesMonitor {
    //private TextView txtBarcode;
    private LetterSpacingTextView txtBarcode;
    private ImageView mImageView;
    private Bitmap mBitmap = null;
    private long startTime;
    private long timeElapsed;
    private EditText txtOriginatedBin;
    private Button btnSubmit;
    private ScanTest currentItem;
    private static final int MSG_BCODE_STARTING = 22;
    private static final int MSG_DONE = 11;
    private Handler codeImageHandler = null;
    protected ProgressDialog bcDialog;
    private boolean hasBcRan = false;
    private int bcRunCount = 0;
    private ScanTest recentlySavedScan = null;
    private int size = 0;
    private  int currentChannel = 0;
    private String endPointLocation = "";
    private List<ScanResult> results;
    private WifiManager wifi;
    private WifiReceiver wifiRec;
    private int[] channelsFrequency = {0,2412,2417,2422,2427,2432,2437,2442,2447,2452,2457,2462,2467,2472,2484};
    private final String ENDPOINT_THE2S = "20:4e:7f:87:8f:a0";
    private final String ENDPOINT_AMAZONDISPATCH = "20:4e:7f:87:61:60";
    private final String ENDPOINT_THE4S = "20:4e:7f:87:58:00";
    private final String ENDPOINT_BACKSTOCK8 = "20:4e:7f:87:4d:e0";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_details);

        wifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiRec = new WifiReceiver();
        registerReceiver(wifiRec, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        startTime = new Date().getTime();
        //txtBarcode = (TextView) findViewById(R.id.lblBarcode);
        txtBarcode = (LetterSpacingTextView) findViewById(R.id.lblBarcode);
        txtOriginatedBin = (EditText) findViewById(R.id.etxtScanBy);
        mImageView = (ImageView) findViewById(R.id.imgBarcode);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ButtonClick(v);
            }
        });

        codeImageHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_BCODE_STARTING:
                        bcDialog = new ProgressDialog(ActDetails.this);
                        CharSequence message = "Working hard...contacting webservice...";
                        CharSequence title = "Please Wait";
                        bcDialog.setCancelable(true);
                        bcDialog.setCanceledOnTouchOutside(false);
                        bcDialog.setMessage(message);
                        bcDialog.setTitle(title);
                        bcDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        bcDialog.show();
                        break;
                    case MSG_DONE:
                        if (bcDialog != null && bcDialog.isShowing() == true) {
                            bcDialog.dismiss();
                        }
                        break;
                }
            }

        };
        populateUiControls(savedInstanceState);
    }

    @Override
    protected void onResume() {
        registerReceiver(wifiRec, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(wifiRec);
        super.onPause();
    }

    private void ButtonClick(View v) {
        switch (v.getId()) {
            case R.id.btnSubmit:
                //prepare some values to be passed to the database
                int currentChannel = 0;
                String inputText = txtOriginatedBin.getText().toString();
                String endPointLocation = "";
                if (inputText != null && !inputText.trim().equalsIgnoreCase("")) {

                    //Initiate a wifi scan - from a broadcast receiver
                    wifi.startScan();
                    WifiInfo info = wifi.getConnectionInfo();

                    //Broadcast Receiver
                    /*registerReceiver(new BroadcastReceiver()
                    {
                        @Override
                        public void onReceive(Context c, Intent intent)
                        {
                            results = wifi.getScanResults();
                            size = results.size();
                        }
                    }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

                    try {
                        if (results.isEmpty()) {
                            Log.e("Unreported Error Occured:","ScanResults did not yield any result"); // for testing purposes
                        }

                        for (ScanResult res : results) {
                            //checking that the currently connected BSSID is one of ours
                            if (res.BSSID.equalsIgnoreCase(ENDPOINT_THE2S)) {
                                //check that we have a frequency connected
                                if (!(Arrays.binarySearch(channelsFrequency, res.frequency) == -1)) {
                                    currentChannel = Arrays.binarySearch(channelsFrequency, res.frequency);
                                    endPointLocation = "the2s";
                                }
                            }else if (res.BSSID.equalsIgnoreCase(ENDPOINT_AMAZONDISPATCH)) {
                                //check that we have a frequency connected
                                if (!(Arrays.binarySearch(channelsFrequency, res.frequency) == -1)) {
                                    currentChannel = Arrays.binarySearch(channelsFrequency, res.frequency);
                                    endPointLocation = "AmazonDispatch";
                                }
                            }else if (res.BSSID.equalsIgnoreCase(ENDPOINT_BACKSTOCK8)) {
                                //check that we have a frequency connected
                                if (!(Arrays.binarySearch(channelsFrequency, res.frequency) == -1)) {
                                    currentChannel = Arrays.binarySearch(channelsFrequency, res.frequency);
                                    endPointLocation = "BackStock8";
                                }
                            }else if (res.BSSID.equalsIgnoreCase(ENDPOINT_THE4S)) {
                                //check that we have a frequency connected
                                if (!(Arrays.binarySearch(channelsFrequency, res.frequency) == -1)) {
                                    currentChannel = Arrays.binarySearch(channelsFrequency, res.frequency);
                                    endPointLocation = "the4s";
                                }
                            } else {
                                //Then it's not one of ours
                                currentChannel = res.frequency; // store frequency on channel for review and testing purposes only
                                endPointLocation = "Undetermined";
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        //log
                    }*/

                    currentItem.setBssId(info.getBSSID());
                    currentItem.setChannel(currentChannel);        //  ******************************** CHECK THIS VALUE   *************************
                    currentItem.setOriginatedBin(inputText);
                    currentItem.setEndPointLocation(endPointLocation);
                    wserverPost qryTask = new wserverPost();
                    qryTask.execute(currentItem);
                }
                break;
            default:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.alert_exploded)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // ZO SOMETHING!
                            }
                        });
                builder.show();
        }
    }

    private ScanTest QueryDb() {
        ScanTest scanPersisted = null;
        //String _uri = "http://192.168.10.2:8080/com.lebel.restsample/api/v1/scans/postscan"; //***
        String _uri = "http://192.168.10.2:8080/warehouse.support/api/v1/scans/postscan";
        //String _uri = "http://192.168.10.248:9080/warehouse.support/api/v1/scans/postscan";	//***
        try {
            URL serviceUrl = new URL(_uri);
            HttpURLConnection conn = (HttpURLConnection) serviceUrl.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            String input = mapper.writeValueAsString(currentItem);

            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }
            InputStream stream = conn.getInputStream();
            //InputStreamReader isReader = new InputStreamReader(stream);

            //put output stream into a pojo
            scanPersisted = mapper.readValue(stream, ScanTest.class);

            //put output stream into a string
            //BufferedReader br = new BufferedReader(isReader );
            //final StringBuilder sb  = new StringBuilder();
            //String result = "";
            //while ((result = br.readLine()) != null) {
            //	sb.append(result);
            //}
            //br.close();
            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return scanPersisted;
    }

    private void populateUiControls(Bundle form) {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            //Yell, Blue murder !
            return;
        }
        Product prod = (Product) extras.getSerializable("PRODUCT_EXTRA");
        String barcode = extras.getString("SCANDATA_EXTRA");
        long prevTaskElapsedTime = extras.getLong("TIME_EXTRA") != 0 ? extras.getLong("TIME_EXTRA") : 0;
        //Populate views
        TextView txtArtist = (TextView) findViewById(R.id.txtv_Artist);
        TextView txtTitle = (TextView) findViewById(R.id.txtv_Title);

        TextView lblShortDesc = (TextView) findViewById(R.id.lblShortDesc);
        TextView lblISBN = (TextView) findViewById(R.id.lblISBN);
        TextView lblFormat = (TextView) findViewById(R.id.lblFormat);
        TextView lblBinNo = (TextView) findViewById(R.id.lblBinNumber);
        TextView lblOutOfStock = (TextView) findViewById(R.id.lblOutOfStock);
        TextView lblOnHand = (TextView) findViewById(R.id.lblOnHand);
        TextView lblPrice = (TextView) findViewById(R.id.lblPrice);
        TextView lblTime = (TextView) findViewById(R.id.lblTime);
        TextView lblOriginatedBin = (TextView) findViewById(R.id.lblOriginatedBin);

        TextView txtShortDesc = (TextView) findViewById(R.id.txtvShortDesc);
        TextView txtISBN = (TextView) findViewById(R.id.txtvISBN);
        TextView txtFormat = (TextView) findViewById(R.id.txtvFormat);
        TextView txtBinNo = (TextView) findViewById(R.id.txtvBinNumber);
        TextView txtOutOfStock = (TextView) findViewById(R.id.txtvOutOfStock);
        TextView txtOnHand = (TextView) findViewById(R.id.txtvOnHand);
        TextView txtPrice = (TextView) findViewById(R.id.txtvPrice);
        TextView txtTime = (TextView) findViewById(R.id.txtvTime);

        txtArtist.setText(prod.getArtist()) ; txtTitle.setText(prod.getTitle());
        lblShortDesc.setText("Short Description:") ; lblISBN.setText("EAN:");
        lblFormat.setText("Format:") ; lblBinNo.setText("Bin Number:");
        lblOutOfStock.setText("Out of Stock:") ; lblOnHand.setText("Stock On Hand:");
        lblPrice.setText("Price:") ; lblTime.setText("Time Elapsed:");
        lblOriginatedBin.setText("Originated Bin:");
        txtShortDesc.setText(prod.getShortDescription()) ; txtISBN.setText(prod.getBarcode());
        txtFormat.setText(prod.getFormat()) ; txtBinNo.setText(prod.getBinNo());
        txtOutOfStock.setText(String.format("%s", prod.getOutOfStock()));
        txtOnHand.setText(String.format("%s", prod.getOnHand()));
        txtPrice.setText(String.format("£    %s", prod.getDealerPrice()));
        txtTime.setText(String.format("%s", prevTaskElapsedTime));

        //if barcode !=null => draw barcode else use the image in resources as default
        if (barcode != null && !barcode.equalsIgnoreCase("")) {
            if (hasBcRan == false && bcRunCount == 0) {
                final String bcode = barcode;
                Runnable bcRunnable = new Runnable() {

                    @Override
                    public void run() {
                        //generateBarCode(bcode);
                        mImageView.post(new Runnable() {

                            @Override
                            public void run() {
                                generateBarCode(bcode);
                            }

                        });
                    }

                };
                Thread bcThread = new Thread(bcRunnable);
                bcThread.setName("bcThread");
                bcThread.run();
            }
        }
        else {
            mImageView.setImageResource(R.drawable.barcode_ean13);
        }
        hasBcRan = false;
        bcRunCount = 0;

        long endTime = new Date().getTime();
        timeElapsed = endTime - startTime;
        long totalTimeElapsed = prevTaskElapsedTime + timeElapsed;

        //Build new ScanTest
        currentItem = new ScanTest();
        currentItem.setProductId(prod.getProductId());
        currentItem.setQueryTime(totalTimeElapsed);
    }

    public void generateBarCode(String data) {
        if (hasBcRan == false) {
            codeImageHandler.obtainMessage(MSG_BCODE_STARTING).sendToTarget();
            if (bcRunCount == 0) { bcRunCount ++; }

            switch (data.length()) {
                case 12:    //UPC-A
                    com.google.zxing.oned.UPCAWriter upc = new com.google.zxing.oned.UPCAWriter();
                    try {
                        BitMatrix bm = upc.encode(data, BarcodeFormat.UPC_A, 360, 108);
                        mBitmap = Bitmap.createBitmap(360, 108, Config.ARGB_8888);
                        for (int i = 0; i < 360; i++) {
                            for (int j = 0; j < 108; j++) {

                                mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
                            }
                        }
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    break;
                case 8:     //EAN-8
                    com.google.zxing.oned.EAN8Writer ean8 = new com.google.zxing.oned.EAN8Writer();
                    try {
                        BitMatrix bm = ean8.encode(data, BarcodeFormat.EAN_8, 360, 108);
                        mBitmap = Bitmap.createBitmap(360, 108, Config.ARGB_8888);
                        for (int i = 0; i < 360; i++) {
                            for (int j = 0; j < 108; j++) {

                                mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
                            }
                        }
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    break;
                case 14:    //UPC-14
                    //BitMatrix bm = c9.encode(data,BarcodeFormat.CODE_128,380, 168);
                    com.google.zxing.oned.ITFWriter itf = new com.google.zxing.oned.ITFWriter();
                    try {
                        //BitMatrix bm = c9.encode(data,BarcodeFormat.CODE_128,380, 168);
                        BitMatrix bm = itf.encode(data, BarcodeFormat.ITF, 360, 108);
                        mBitmap = Bitmap.createBitmap(360, 108, Config.ARGB_8888);
                        for (int i = 0; i < 360; i++) {
                            for (int j = 0; j < 108; j++) {

                                mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
                            }
                        }
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    break;
                case 13:    //EAN-13
                    com.google.zxing.oned.EAN13Writer ean13 = new com.google.zxing.oned.EAN13Writer();
                    try {
                        BitMatrix bm = ean13.encode(data, BarcodeFormat.EAN_13, 360, 108);
                        mBitmap = Bitmap.createBitmap(360, 108, Config.ARGB_8888);
                        for (int i = 0; i < 360; i++) {
                            for (int j = 0; j < 108; j++) {

                                mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
                            }
                        }
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    break;
                default:    //Error - throw dead kittens
                    WriterException ex = new WriterException();
                    ex.printStackTrace();
                    throw new RuntimeException(ex.getMessage());
            }

            if (mBitmap != null) {
                mImageView.setImageBitmap(mBitmap);
                //mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                //txtBarcode.setText(data);
                txtBarcode.setLetterSpacing(31);
                txtBarcode.setText(data);
            }else { //defaults to the local resources
                mImageView.setImageResource(R.drawable.barcode_ean13);
            }

            codeImageHandler.obtainMessage(MSG_DONE).sendToTarget();
        }
    }

    @Override
    public void SignalLevelChanged(WifiStatus status) {
        //handle WIFI Signal Strength Changed here
    }

    class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            results = new ArrayList<ScanResult>();
            results = wifi.getScanResults();
            size = results.size();
            if (!results.isEmpty()){
                for (ScanResult res : results) {
                    //checking that the currently connected BSSID is one of ours
                    if (res.BSSID.equalsIgnoreCase(ENDPOINT_THE2S)) {
                        //check that we have a frequency connected
                        if (!(Arrays.binarySearch(channelsFrequency, res.frequency) == -1)) {
                            currentChannel = Arrays.binarySearch(channelsFrequency, res.frequency);
                            endPointLocation = "the2s";
                        }
                    }else if (res.BSSID.equalsIgnoreCase(ENDPOINT_AMAZONDISPATCH)) {
                        //check that we have a frequency connected
                        if (!(Arrays.binarySearch(channelsFrequency, res.frequency) == -1)) {
                            currentChannel = Arrays.binarySearch(channelsFrequency, res.frequency);
                            endPointLocation = "AmazonDispatch";
                        }
                    }else if (res.BSSID.equalsIgnoreCase(ENDPOINT_BACKSTOCK8)) {
                        //check that we have a frequency connected
                        if (!(Arrays.binarySearch(channelsFrequency, res.frequency) == -1)) {
                            currentChannel = Arrays.binarySearch(channelsFrequency, res.frequency);
                            endPointLocation = "BackStock8";
                        }
                    }else if (res.BSSID.equalsIgnoreCase(ENDPOINT_THE4S)) {
                        //check that we have a frequency connected
                        if (!(Arrays.binarySearch(channelsFrequency, res.frequency) == -1)) {
                            currentChannel = Arrays.binarySearch(channelsFrequency, res.frequency);
                            endPointLocation = "the4s";
                        }
                    } else {
                        //Then it's not one of ours
                        currentChannel = res.frequency; // store frequency on channel for review and testing purposes only
                        endPointLocation = "Undetermined";
                    }
                }
            }
        }
    }

    private class wserverPost extends AsyncTask<ScanTest, Integer, ScanTest> {
        protected ProgressDialog mDialog;

        @Override
        protected ScanTest doInBackground(ScanTest... params) {
            Thread.currentThread().setName("QueryDb-wsPost");
            ScanTest ret = QueryDb();
            return ret;
        }

        @Override
        protected void onPostExecute(ScanTest result) {
            if (mDialog != null && mDialog.isShowing() == true) {mDialog.dismiss();}
            if (result != null && result.getProductId() > 0) {
                recentlySavedScan = result; //Persists our newly created scan into a private variable
                String mMsg = String.format("Successfully created TransactionID: %s for ProductID: %s",
                        result.getTransactionId(), result.getProductId());
                AlertDialog.Builder builder = new AlertDialog.Builder(ActDetails.this);
                builder.setMessage(mMsg)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                txtOriginatedBin.setText("");
                                if (btnSubmit.isEnabled()) btnSubmit.setEnabled(false);
                            }
                        });
                builder.show();
            }
        }

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(ActDetails.this);
            CharSequence message = "Working hard...contacting webservice...";
            CharSequence title = "Please Wait";
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setMessage(message);
            mDialog.setTitle(title);
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.show();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent resultIntent = new Intent();
        setResult(0, resultIntent);
        finish();
    }
}