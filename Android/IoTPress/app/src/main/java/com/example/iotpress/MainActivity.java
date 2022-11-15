package com.example.iotpress;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.somsakelect.android.mqtt.MqttAndroidClient;

import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private MqttAndroidClient mqttAndroidClient;
    private final String PRESS_TOPIC = "PRESS_221113/press";
    private TextView leftSensorView, rightSensorView;
    private TextView sensorTimeView;
    private TextView resultView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 안드로이드12 호환성
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent.getActivity(this.getApplicationContext(), 0 , intent, PendingIntent.FLAG_IMMUTABLE);

        // View 설정
        leftSensorView = (TextView) findViewById(R.id.leftSensorView);
        rightSensorView = (TextView) findViewById(R.id.rightSensorView);
        sensorTimeView = (TextView) findViewById(R.id.sensorTimeView);
        resultView = (TextView) findViewById(R.id.resultView);

        // 시간 format 구성
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");

        try{

        mqttAndroidClient = new MqttAndroidClient(this,  "tcp://test.mosquitto.org:1883", MqttClient.generateClientId());
        IMqttToken token = mqttAndroidClient.connect();

        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());    //연결에 성공한경우
                Log.e("Telechips", "onSuccess");
                try {
                    mqttAndroidClient.subscribe(PRESS_TOPIC, 0 );   //연결에 성공하면 토픽으로 subscribe함
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.e("Telechips", "onFailure " + exception.toString());
            }
        });


        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.e("Telechips", "connectionLost ");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (topic.equals(PRESS_TOPIC)){     //topic 별로 분기처리하여 작업을 수행할수도있음
                    String msg = new String(message.getPayload());
                    Log.e("Telechips", "arrive message : " + msg);

                    String values[] = msg.split(" ");

                    int leftValue = Integer.parseInt(values[0]);
                    int rightValue = Integer.parseInt(values[1]);

                    if(leftValue > (rightValue + 300) || (leftValue + 300) < rightValue)
                    {
                        resultView.setText("BAD");
                    }
                    else
                    {
                        resultView.setText("GOOD");
                    }

                    if(leftValue > rightValue)
                    {
                        leftSensorView.setTextColor(Color.RED);
                        rightSensorView.setTextColor(Color.BLACK);
                    }
                    else if(leftValue < rightValue)
                    {
                        leftSensorView.setTextColor(Color.BLACK);
                        rightSensorView.setTextColor(Color.RED);
                    }
                    else
                    {
                        leftSensorView.setTextColor(Color.BLACK);
                        rightSensorView.setTextColor(Color.BLACK);
                    }


                    long _mNow = System.currentTimeMillis();
                    Date _mDate = new Date(_mNow);
                    leftSensorView.setText(values[0]);
                    rightSensorView.setText(values[1]);
                    sensorTimeView.setText(mFormat.format(_mDate));
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
    private DisconnectedBufferOptions getDisconnectedBufferOptions() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(true);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        return disconnectedBufferOptions;
    }

    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setWill("aaa", "I am going offline".getBytes(), 1, true);
        return mqttConnectOptions;
    }
}