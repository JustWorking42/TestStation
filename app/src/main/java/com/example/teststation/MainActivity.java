package com.example.teststation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teststation.test.simulation.TestSimulation;
import com.example.teststation.test.simulation.TestSimulationCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements TestSimulationCallback {
    public static final String NAME_CYCLIC = "Cyclic";
    public static final String NAME_LINEAR_SWEEP = "Linear sweep";
    public static final String NAME_SINUSOID = "Sinusoid";
    public static final String NAME_CONSTANT_VOLTAGE = "Constant voltage";
    public static final String NAME_CHRONOAMPEROMETRY = "Chronoamperometry";

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;
    static final int REQUEST_ENABLE_BLUETOOTH = 0;
    private static final int REQUEST_CODE_PERMISSION = 0;

    SendReceive sendReceive;
    private BluetoothSocket socket;
    private TestSimulation simulation;


    Button send;
    Button listen;
    TextView status;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listen = findViewById(R.id.listen);
        status = findViewById(R.id.status);
        send = findViewById(R.id.send);
        setListen();
        checkBluetoothEnable();

        simulation = new TestSimulation();
    }

    private void checkPermission() {
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_PERMISSION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    checkPermission();
                }
                return;
        }
    }




     Handler handler = new Handler(new Handler.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what)
            {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    status.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuffer = (byte[]) msg.obj;
                    String tempMsg = new String(readBuffer, 0, msg.arg1);
//                    msgBox.setText(tempMsg);
                    receiveData(tempMsg);
                    break;
            }
            return true;
        }
    });

    private void checkBluetoothEnable() {
        BluetoothAdapter bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {

        }
        else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    private void receiveData(String rData) {
        String[] inputParams = rData.split("\n");
        String currentTestName = inputParams[0];
        Toast.makeText(MainActivity.this, currentTestName, Toast.LENGTH_SHORT).show();
        if (inputParams[0].equals("stop")) {
            if (simulation != null) {
                simulation.stopSimulation();
            }
        }
        else if (currentTestName.equals(NAME_CYCLIC)) {
            simulation.startSimulation(this, this, 0);
        }
        else if (currentTestName.equals(NAME_LINEAR_SWEEP)) {
            simulation.startSimulation(this, this, 1);
        }
        else if (currentTestName.equals(NAME_SINUSOID)) {
            simulation.startSimulation(this, this, 2);
        }
        else if (currentTestName.equals(NAME_CONSTANT_VOLTAGE)) {
            simulation.startSimulation(this, this, 3);
        }
        else if (currentTestName.equals(NAME_CHRONOAMPEROMETRY)) {
            simulation.startSimulation(this, this, 4);
        }
        else {
            simulation.startSimulation(this, this, 5);
        }
    }

    private void setListen(){
        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HostThread hostThread = new HostThread();
                hostThread.start();
                status.setText("Click");
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData();
            }
        });
    }


    public void sendData(){
        String data = "Some DATA";
        sendReceive.write(data.getBytes());
    }

    @Override
    public void getTestData(String testData) {
        System.out.println(testData);
        sendReceive.write(testData.getBytes());
    }


    public class HostThread extends Thread {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        private  final UUID MY_UUID = UUID.fromString("9f2c4ce3-0801-42d1-ba41-1a6bfe1ccb70");
        private final String APP_NAME = "TEst";
        private final BluetoothServerSocket mmServerSocket;

        public HostThread(){
// используем вспомогательную переменную, которую в дальнейшем
// свяжем с mmServerSocket,
            BluetoothServerSocket tmp=null;
            try{
// MY_UUID это UUID нашего приложения, это же значение
// используется в клиентском приложении
                tmp= bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch(IOException ignored){}
            mmServerSocket= tmp;

        }


        public void run(){

// ждем пока не произойдет ошибка или не
// будет возвращен сокет
            while(true){
                try{
                    socket= mmServerSocket.accept();
                Message message = Message.obtain();
                message.what = STATE_CONNECTING;
                handler.sendMessage(message);
                } catch(IOException e){
                    e.printStackTrace();
//                Message message = Message.obtain();
//                message.what = STATE_CONNECTION_FAILED;
//                mainActivity.handler.sendMessage(message);
                    break;
                }

// если соединение было подтверждено
                if(socket!=null){


                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);
// управляем соединением (в отдельном потоке)
                     sendReceive = new SendReceive(socket);


                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sendReceive.start();

                    break;
                }
            }

        }

        /** отмена ожидания сокета */
        public void cancel(){
            try{
                mmServerSocket.close();
            } catch(IOException e){}
        }
    }




    private  class SendReceive extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        SendReceive(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempInput = null;
            OutputStream tempOutput = null;

            try {
                tempInput = bluetoothSocket.getInputStream();
                tempOutput = bluetoothSocket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = tempInput;
            outputStream = tempOutput;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while(true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}





