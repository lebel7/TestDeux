package com.proper.TestDeux;


import java.io.IOException;
import java.io.InputStream;
//import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

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
import android.content.DialogInterface;
import android.content.Intent;
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
public class ActDetails extends Activity {
    //private TextView txtBarcode;
    private LetterSpacingTextView txtBarcode;
    private ImageView mImageView;
    private Bitmap mBitmap = null;
    private long startTime;
    private long timeElapsed;
    private EditText txtScanBy;
    private Button btnSubmit;
    private ScanTest currentItem;
    private static final int MSG_BCODE_STARTING = 22;
    private static final int MSG_DONE = 11;
    private Handler codeImageHandler = null;
    protected ProgressDialog bcDialog;
    private boolean hasBcRan = false;
    private int bcRunCount = 0;
    private ScanTest recentlySavedScan = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_details);

        startTime = new Date().getTime();
        //txtBarcode = (TextView) findViewById(R.id.lblBarcode);
        txtBarcode = (LetterSpacingTextView) findViewById(R.id.lblBarcode);
        txtScanBy = (EditText) findViewById(R.id.etxtScanBy);
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

    private void ButtonClick(View v) {
        switch (v.getId()) {
            case R.id.btnSubmit:
                //prepare some values to be passed to the database
                String inputText = txtScanBy.getText().toString();
                if (inputText != null && !inputText.trim().equalsIgnoreCase("")) {
                    //QueryDb(inputText);
                    currentItem.setTestDoneBy(inputText);
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
        //String _uri = "http://192.168.0.100:8080/warehouse.support/api/v1/scans/postscan";
        String _uri = "http://192.168.10.248:9080/warehouse.support/api/v1/scans/postscan";	//***
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
        TextView lblScanBy = (TextView) findViewById(R.id.lblScanBy);

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
        lblScanBy.setText("Test Performed By:");
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
            //com.google.zxing.Writer c9 = new Code128Writer();
            com.google.zxing.oned.EAN13Writer ean = new com.google.zxing.oned.EAN13Writer();
            try {
                //BitMatrix bm = c9.encode(data,BarcodeFormat.CODE_128,380, 168);
                BitMatrix bm = ean.encode(data, BarcodeFormat.EAN_13, 360, 108);
                //mBitmap = Bitmap.createBitmap(380, 168, Config.ARGB_8888);
                mBitmap = Bitmap.createBitmap(360, 108, Config.ARGB_8888);

                for (int i = 0; i < 360; i++) {
                    for (int j = 0; j < 108; j++) {

                        mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
                    }
                }
            } catch (WriterException e) {
                e.printStackTrace();
            }
            if (mBitmap != null) {
                mImageView.setImageBitmap(mBitmap);
                //mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                //txtBarcode.setText(data);
                txtBarcode.setLetterSpacing(31);
                txtBarcode.setText(data);
            }

            codeImageHandler.obtainMessage(MSG_DONE).sendToTarget();
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
                                txtScanBy.setText("");
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