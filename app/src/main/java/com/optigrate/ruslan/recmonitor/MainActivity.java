package com.optigrate.ruslan.recmonitor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import android.os.Handler;
import android.os.Message;


public class MainActivity extends AppCompatActivity {

    static final String TAG = "MainActivity";
    static final long INTERVAL = 1000;

    Thread UDPBroadcastThread;
    private DatagramSocket socket = null;
    private TimerManager tm = null;
    private Handler handler;
    private Boolean shouldRestartSocketListen = true;
    private CountDownTimer countDownTimer;
    private boolean timerHasStarted = false;
    private boolean flag = false;
    private int f1t = 0;
    private TextView status, tvDate, tvStatus;
    private ProgressBar progressBar;
    private ImageView iv;
    private Switch switch1;
    private ImageButton ibRefresh;

    private String imageURL;
    private String fileURL;
    private String smbURL;
    private String txtWeb;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = (TextView)findViewById(R.id.tvStatusResult);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        iv = (ImageView)findViewById(R.id.iv);
        switch1 = (Switch)findViewById(R.id.switch1);
        tvDate = (TextView)findViewById(R.id.tvDate);
        tvStatus = (TextView)findViewById(R.id.tvStatus);
        ibRefresh = (ImageButton) findViewById(R.id.ibRefresh);
        //btnCheck = (Button)findViewById(R.id.btnCheck);

        imageURL = getText(R.string.url_fringes).toString();
        smbURL = getText(R.string.url_smb).toString();
        fileURL = getText(R.string.url_file).toString();
        txtWeb = getText(R.string.url_txt).toString();

        tm = new TimerManager();

        status.setText(getText(R.string.main_msg_idle));
        iv.setVisibility(View.INVISIBLE);
        setDate();

        new DownloadSMBTask(status).execute(smbURL);

        //countDownTimer = new MyCountDownTimer((long) (60 * 1000), INTERVAL);
       // startListenForUDPBroadcast();

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    progressBar.setVisibility(View.INVISIBLE);
                    iv.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Checked");

                    new DownloadImageTask(iv).execute(imageURL);

                } else{
                    Log.d(TAG, "Unchecked");
                    //progressBar.setVisibility(View.VISIBLE);
                    iv.setVisibility(View.INVISIBLE);

                }
            }
        });


        ibRefresh.setOnClickListener(new CompoundButton.OnClickListener(){

            @Override
            public void onClick(View v){
                new DownloadSMBTask(status).execute(smbURL);
                setDate();
            }
        });

        Log.d(TAG, "call onCreate()");

    }



    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Input Stream Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            Log.d(TAG, "imageLoaded");
        }
    }

    private class DownloadSMBTask extends AsyncTask<String, Void, String> {
        TextView tv;

        public DownloadSMBTask(TextView tv) {
            this.tv = tv;
        }

        protected String doInBackground(String... urls) {
            String smbURL = urls[0];
            String date = "";
            String duration = "";

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new SmbFile(smbURL).getInputStream()));

                date =  br.readLine();
                duration = br.readLine();

                date = getRecTime(date,duration);

                br.close();
            } catch (SmbException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return date;
        }

        protected void onPostExecute(String result) {

           countDownTimer = new MyCountDownTimer((Long.valueOf(result)), INTERVAL);
           countDownTimer.start();
           Log.d("SMB", "File is read. Recording time count down: " + result);

        }
    }

    private void setDate(){
        tvDate.setText("Last check: " + getDate().toString());
    }

    private Date getDate(){
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //System.out.println(calendar.getTime());// print 'Mon Mar 28 06:00:00 ALMT 2016'
        return calendar.getTime();
    };

    private String getRecTime(String date, String duration){
        Date time = new Date();
        Date today = getDate();

        long recTime = 0;
        String myDate = date; //"2/13/2019 10:23:02 AM";
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa", Locale.US);
        try {
            recTime = 1000*Long.valueOf(duration);
            Log.d("DURATION", String.valueOf(recTime));
            time = dateFormat.parse(myDate);
            Log.d("STARTED", String.valueOf(time.getTime()));
            recTime = time.getTime() + recTime - today.getTime();
            Log.d("TODAY", String.valueOf(today.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return String.valueOf(recTime);
    }

/*
    private class GetMessageFromUDP extends AsyncTask<String, Void, String> {
        TextView tv;

        public GetMessageFromUDP(TextView tv) {
            this.tv = tv;
        }

        protected String doInBackground(String... urls) {
            String fromServer = "";
            String portNumber = urls[0];
            DatagramSocket clientSocket = null;

            try {
                clientSocket = new DatagramSocket(Integer.valueOf(portNumber));
                clientSocket.setBroadcast(true);

                Log.i(TAG, "port number: " + Integer.valueOf(portNumber));
                Log.i(TAG, "Listen on " + clientSocket.getLocalAddress() + " from " + clientSocket.getInetAddress() + " port " + clientSocket.getBroadcast());

                byte[] receiveData = new byte[512];

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

              //  while (true){
                    clientSocket.receive(receivePacket);

                    Log.i(TAG, "Packet received from: " + receivePacket.getAddress().getHostAddress());

                    String modifiedSentence = new String(receivePacket.getData());
                    fromServer = modifiedSentence;

                    //System.out.println("FROM SERVER:" + modifiedSentence);
                    Log.i(TAG, modifiedSentence);

              //  }



               clientSocket.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (SmbException e) {
                e.printStackTrace();
            } catch (java.net.SocketException e){
                e.printStackTrace();
            } catch (java.io.IOException e){
                e.printStackTrace();
            }

            return fromServer;
        }

        protected void onPostExecute(String result) {
            tv.setText(result);
            Log.d(TAG, "Message from UDP: " + result);
        }
    }
*/

/*
    class EventHandler extends Handler {
        EventHandler() {
        }

        public void handleMessage(Message msg) {
            hideShowFragment(msg.obj.toString());
        }
    }
*/

/*
    public void hideShowFragment(String text) {
        if (text != null) {
           // this.frtr = getFragmentManager().beginTransaction();
            if (text.equals("0")) {
               // this.frtr.hide(getFragmentManager().findFragmentById(C0048R.id.fragment1));
               // this.frtr.show(getFragmentManager().findFragmentById(C0048R.id.fragment2));
               // this.frtr.commit();
                return;
            }
            f1t = tm.stringParser(text);
            if (countDownTimer != null) {
                countDownTimer.cancel();
                timerHasStarted = false;
            }
           // this.frtr.hide(getFragmentManager().findFragmentById(C0048R.id.fragment2));
           // this.frtr.show(getFragmentManager().findFragmentById(C0048R.id.fragment1));
           // this.frtr.commit();
            countDownTimer = new MyCountDownTimer((long) (f1t * 1000), INTERVAL);
            if (timerHasStarted) {
                countDownTimer.cancel();
                timerHasStarted = false;
                return;
            }
            countDownTimer.start();
            timerHasStarted = true;
        }
    }
 */

/*
    private class Network implements Runnable {

        public void run() {
            Exception e;
            byte[] message = new byte[1024];
            try {
                String text;
                DatagramSocket s = new DatagramSocket(PORT);
                String text2 = null;
                while (shouldRestartSocketListen) {
                    try {
                        DatagramPacket p = new DatagramPacket(message, message.length);
                        Log.i("UDP", "Waiting for UDP broadcast");
                        Log.i("UDP", s.toString());
                       // s.receive(p);

                        text = new String(message, 0, p.getLength());
                        try {
                            s.receive(p);
                            Log.i("UDP", text);
                            // updatetrack(text);
                            text2 = text;
                            //Log.d("UDP", "up");
                            //        Thread.sleep(1000);
                        } catch (Exception e2) {
                            e = e2;
                        }
                    } catch (Exception e3) {
                        e = e3;
                        text = text2;
                    }
                }
                text = text2;

            } catch (Exception e4) {
                e = e4;
                Log.e("UDP", "no longer listening for UDP broadcasts cause of error " + e.getMessage());
            }
        }
    }

*/

    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public void onFinish() {
            //hideShowFragment("0");
            progressBar.setVisibility(View.VISIBLE);
            status.setText(getText(R.string.main_msg_idle));
            status.setTextColor(getColor(R.color.colorPrimary));
        }

        public void onTick(long millisUntilFinished) {
            status.setText((getText(R.string.main_msg_inProgress)) + "\n" + tm.getTime((int) (millisUntilFinished / INTERVAL)));
            status.setTextColor(getColor(R.color.colorRed));
            progressBar.setVisibility(View.INVISIBLE);

        }
    }


/*
    private void startListenForUDPBroadcast() {
        UDPBroadcastThread = new Thread(new Network());
        UDPBroadcastThread.start();
    }


/*
    private void updatetrack(String s) {
        Message msg = new Message();
        msg.obj = s;
        handler.sendMessage(msg);
    }
*/

/*
   public void stopListen() {
        shouldRestartSocketListen = false;
        socket.close();
    }
*/

/*
    public void onDestroy() {
        super.onDestroy();
        stopListen();
    }
  */
}
