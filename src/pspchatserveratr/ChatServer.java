package pspchatserveratr;

import java.util.List;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatServer {
    
    public static final String RESPONSE_JOIN_FAILED = "FAILED";
    public static final String RESPONSE_JOIN_SUCCESSFUL = "JOINED";
    
    public static final String DISCONNECT_CODE = "DISCONNECT";

    public static final String GET_USERS_CODE = "USERS";
    public static final String GLOBAL_MESSAGE_CODE = "GLOBAL";
    public static final String PRIVATE_MESSAGE_CODE = "PRIVATE";
    
    public static final String MESSAGE_DELIMITER = ":";
    
    
    
    private boolean run = true;
    
    private List<ChatServerThread> serverThreads = new ArrayList<>();
    
    private ServerSocket servicio;
    
    
    private Map<String, String> users = new TreeMap();
    

    public ChatServer(int port){
        try{
            servicio = new ServerSocket(port);
            
        }catch(IOException ex){
            System.out.println(ex.getLocalizedMessage());
        }
    }
    
    
    public boolean checkLogin(String userName, String password){
        if(userName.contains(":") || userName.contains(";") || password.contains(":") || password.contains(";")) return false;
        
        String pass = users.get(userName);
        if(pass == null){
            users.put(userName, password);
            return true;
        }else{
            return pass.equals(password);
        }
    }
    
    
    public void broadcast(String text){
        //for(ChatServerThread client : serverThreads){
        //    client.send(text);
        //}
        for (int i = 0; i < serverThreads.size(); i++) {
            //if(serverThreads.get(i).isActive)
                serverThreads.get(i).send(text);            
        }
    }
    
    
    public void broadcastAvoidSleep(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                
                while(true){
                    for (int i = 0; i < serverThreads.size(); i++){
                        serverThreads.get(i).send("HI");
                    }
                    System.out.println("VUELTA AVOID SLEEP");
                    
                    try{
                        Thread.sleep(30000);
                    }catch(Exception ex){ }
                }
                
            }
        }).start();
        
        
    }
    
    
    public void sendPrivate(String sender, String receiver, String message){
        for (int i = 0; i < serverThreads.size(); i++) {
            if(serverThreads.get(i).getUserName().equals(receiver) || serverThreads.get(i).getUserName().equals(sender)){
                serverThreads.get(i).send(message);
            }
        }
    }
    
    
    
    public String listUsers(String excluded){
        String list = GET_USERS_CODE + ":";
        for (int i = 0; i < serverThreads.size(); i++) {
            if(serverThreads.get(i).getUserName() != null && !serverThreads.get(i).getUserName().equals(excluded)){
                list += serverThreads.get(i).getUserName();
            
                if(i < serverThreads.size()-1 ) { list += ":"; }
            }
        }
        
        return list;
    }
    
    
    /*
    public boolean checkUserName(String userName){
        if(userName.contains(":") || userName.contains(";")){
            return false;
        }
        
        for (int i = 0; i < serverThreads.size(); i++) {
            if(serverThreads.get(i).getUserName() != null && serverThreads.get(i).getUserName().equalsIgnoreCase(userName)){
                return false;
            }
        }
        
        return true;
        
    }*/
    
    
    public void deleteThread(Thread thread){
        try{
            serverThreads.remove(thread);
        }catch(Exception ex){
            
        }
        
    }
    
    
    
    
    public void startService(){
        Thread mainThread = new Thread(){
            @Override
            public void run(){
                ChatServerThread serverThread;
                Socket servidor;
                while(run){
                    try {
                        servidor = servicio.accept();
                        serverThread = new ChatServerThread(ChatServer.this, servidor);
                        serverThreads.add(serverThread);
                        //serverThread.setId(serverThreads.indexOf(serverThread));
                        serverThread.start();
                    } catch (IOException ex) {
                        System.out.println(ex.getLocalizedMessage());
                    }
                }
            }
        };
        mainThread.start();
    }
    
    
    public static void main(String[] args){
        ChatServer chatServer = new ChatServer(5000);
        chatServer.startService();
        chatServer.broadcastAvoidSleep();
    }
    
}
