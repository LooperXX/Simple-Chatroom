/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/* ChatAnnotation *
*
*  My chatroom.
*  @author LooperXX
*/
/*https://www.jianshu.com/p/d79bf8174196 帮助理解了websocket的用法*/

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

@ServerEndpoint(value = "/chat/{nickname}")   /*@ServerEndpoint来进行声明接口：@ServerEndpoint(value="/websocket/{paraName}") ; 其中 “ { } ”用来表示带参数的连接*/
public class ChatAnnotation {
    private static final Set<ChatAnnotation> connections = new HashSet<ChatAnnotation>();
    private String nickname;
    private Session session;

    public ChatAnnotation() {
    }

    private static void broadcast(String msg) { /*广播*/
        for (ChatAnnotation client : connections) {
            try {
                synchronized (client) { /*用synchronized 给client对象加了锁--当一个线程访问client对象时，其他试图访问client对象的线程将会阻塞，直到该线程访问client对象结束*/
                    client.session.getBasicRemote().sendText(msg);
                }
            } catch (IOException e) {
                System.out.println("Error: Failed to send message to our server!");
                connections.remove(client);
                try {
                    client.session.close();
                } catch (IOException e1) {
                    System.out.println(e1.toString());
                }
            }
        }
    }

    @OnOpen
    public void start(@PathParam("nickname") String nickname, Session session) {  /*@PathParam("nickname") 用于获取{}中的参数nickname*/
        this.session = session;
        connections.add(this);
        this.nickname = nickname;
        String message = String.format("{\n" +
                "    \"type\":\"online\",\n" +   /*提示上线*/
                "    \"user\":\"%s\"\n" +
                "}", nickname);
        broadcast(message);/*广播信息*/
        userList();/*更新用户列表*/
    }

    @OnClose
    public void close() {
        connections.remove(this);
        String message = String.format("{\n" +
                "    \"type\":\"offline\",\n" + /*提示下线  */
                "    \"user\":\"%s\"\n" +
                "}", this.nickname);
        broadcast(message);/*广播信息*/
        userList();/*更新用户列表*/
    }

    @OnMessage
    public void process(String message) {
        JSONObject jo = JSON.parseObject(message);/*json处理信息*/
        String type = jo.getString("type");
        switch (type) {
            case "getUserList":
                userList();
                break;
            case "msg":
                sendMsgTo(jo);
                break;
            default:
                System.out.println("Error: Something wrong with message!");
        }
    }

    @OnError
    public void onError(Throwable t) throws Throwable {
        System.out.println("Error: " + t.toString());
    }

    private void userList() {  /*重载用户列表并广播*/
        String msg = "{\n" +
                "    \"type\":\"userList\",\n" +
                "    \"userList\":[";
        msg += "\"" + "Chat_bot" + "\",";

        for (ChatAnnotation client : connections) {
            msg += "\"" + client.nickname + "\",";
        }
        //TODO: emmm,那么去掉当前进程的用户名是前端弄还是后端弄呢 ---2018/1/20
        //OK 还是后端弄更合理 ---2018/1/21
        msg = msg.substring(0, msg.length() - 1); /*去除最后的逗号并处理成json*/
        msg += "]\n}";
        broadcast(msg);
    }

    private static String sendToBot(String msg) { /*连接图灵机器人*/
        try {
            String host = "www.tuling123.com";
            Socket socket = new Socket(host, 80);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintStream out = new PrintStream(socket.getOutputStream(), true, "UTF-8");/*自动flush*/
            out.println("GET /openapi/api?key=f9ead0aad301411392637cc46708c5cd&info=" + msg + " HTTP/1.1");
            out.println("Host: " + host);
            out.println("User-Agent: curl/7.38.0");
            out.println("Accept: */*");
            out.println("");
            String tmp = "";
            while (!(tmp = in.readLine()).equals("")) {
            }
            in.readLine();
            String response = in.readLine();/*提取图灵机器人的回话*/
            in.readLine();
            JSONObject jsonMsg = JSON.parseObject(response);
            return jsonMsg.getString("text");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendMsgTo(JSONObject jo) {
        if (jo.getBoolean("broadcast")) {
            broadcast(JSON.toJSONString(jo));
        } else {
            String from = jo.getString("from");/*发送方*/
            String to = jo.getString("to");/*接收方*/
            String msg;
            if (to.equals("Chat_bot")) {
                Date myDate = new Date();
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("ah:mm:ss"); /*与js中时间格式保持一致*/
                String time = dateTimeFormat.format(myDate);
                String send = jo.getString("content");
                String response = sendToBot(send);
                to = from;
                msg = "{\n" +
                        "    \"type\":\"msg\",\n" +
                        "    \"time\":\"" + time + "\",\n" +
                        "    \"content\":\"" + response + "\",\n" +
                        "    \"from\":\"Chat_bot\",\n" +
                        "    \"to\":\"" + from + "\",\n" +
                        "    \"broadcast\":false\n" +
                        "  }";

            } else {
                msg = JSON.toJSONString(jo);
            }
            for (ChatAnnotation client : connections) { /*遍历connections 找到to*/
                if (client.nickname.equals(to)) {
                    try {
                        client.session.getBasicRemote().sendText(msg);
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}