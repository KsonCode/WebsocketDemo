package com.bwie.websocketdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnStart;
    private TextView tvOutput;
    private WebSocket mSocket;
    private EditText messageEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStart = (Button) findViewById(R.id.start);
        tvOutput = (TextView) findViewById(R.id.output);
        messageEt = findViewById(R.id.et_message);
        btnStart.setOnClickListener(this);
    }

    /**
     * websocket建立连接
     */
    private void start() {
        OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                .readTimeout(3, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(3, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(3, TimeUnit.SECONDS)//设置连接超时时间
                .build();

        Request request = new Request.Builder().url("ws://47.93.35.224/sub/lixiaoming_20190826").build();
        EchoWebSocketListener socketListener = new EchoWebSocketListener();
        mOkHttpClient.newWebSocket(request, socketListener);
        mOkHttpClient.dispatcher().executorService().shutdown();
    }

    /**
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start://开始连接
                start();
                break;
        }
    }

    /**
     * 发送消息
     *
     * @param view 当前view
     */
    public void send(View view) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                OutputStream outputStream = null;
                InputStream inputStream = null;
                try {

                    Socket socket = new Socket("127.0.0.1", 8888);
                    System.out.println("客户端连接成功");
                    outputStream = socket.getOutputStream();
                    outputStream.write(messageEt.getText().toString().getBytes());

                    inputStream = socket.getInputStream();


                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) != -1) {
                        result.write(buffer, 0, length);
                    }
                    String string = result.toString("UTF-8");
                    System.out.println(string);



                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        if (inputStream!=null)
                        inputStream.close();
                        if (outputStream!=null)
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            }
        }).start();

    }


    private final class EchoWebSocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            mSocket = webSocket;
            String openid = "1";
            //连接成功后，发送登录信息
            String message = "{\"type\":\"login\",\"user_id\":\"" + openid + "\"}";
            mSocket.send(message);
            output("连接成功！");

        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            super.onMessage(webSocket, bytes);
            output("receive bytes:" + bytes.hex());
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
            output("receive text:" + text);
            //收到服务器端发送来的信息后，每隔10秒发送一次心跳包
            final String message = "{key:1}";
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mSocket.send(message);
                }
            }, 25000);

        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
            output("closed:" + reason);
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            super.onClosing(webSocket, code, reason);
            output("closing:" + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
            output("failure:" + t.getMessage());
        }
    }

    private void output(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvOutput.setText(tvOutput.getText().toString() + "\n\n" + text);
            }
        });
    }


}
