package com.bwie.websocketdemo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ServerScoket {

    public static void main(String[] args) throws Exception{

            //服务端socket对下岗
            ServerSocket serverSocket=null;
            //客户端socket对象
            Socket socket=null;

            OutputStream outputStream = null;
            InputStream inputStream = null;

            String string = "";


            try{
                //创建一个ServerSocket在端口4700监听客户请求
                serverSocket=new ServerSocket(8888);
                System.out.println("服务器已启动,等待客户连接...");
                //使用accept()阻塞等待客户请求，有客户
                socket=serverSocket.accept();//请求到来则产生一个Socket对象，并继续执行
                System.out.println("客户连接成功...");


                inputStream = socket.getInputStream();

                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                string = result.toString("UTF-8");

                System.out.println("收到客户的消息==="+string);

                outputStream = socket.getOutputStream();
                outputStream.write(("我是服务端，我已经收到："+string).getBytes());

            }catch(Exception e){
                e.printStackTrace();//出错，打印出错信息
            }finally {

                if (outputStream!=null){
                    outputStream.close();
                } if (inputStream!=null){
                    inputStream.close();
                }
            }



    }



}
