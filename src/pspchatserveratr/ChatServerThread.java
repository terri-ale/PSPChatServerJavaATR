package pspchatserveratr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatServerThread extends Thread{
    
    public boolean isActive = true;
    
    private boolean run = true;
    
    
    
    private int id;
    private String userName;
    
    
    
    private DataInputStream flujoE;
    private DataOutputStream flujoS;
    
    private final Socket servidor;
    
    private ChatServer server;

    public ChatServerThread(ChatServer server, Socket servidor) {
        this.server = server;
        this.servidor = servidor;
        try{
            flujoE = new DataInputStream(servidor.getInputStream());
            flujoS = new DataOutputStream(servidor.getOutputStream());
        }catch(Exception ex){
            System.out.println(ex.getLocalizedMessage());
        }
    }
    
    
    
    
    @Override
    public void run(){
        String text = "";
        while(run){
            
            try {
                text = flujoE.readUTF();
                

                System.out.println("TEXTO RECIBIDO "+text);
                
                if(userName == null){
                    // Nombre no establecido (primer mensaje es el nombre y contraseña)
                    String[] pieces = text.split(ChatServer.MESSAGE_DELIMITER);
                    
                    System.out.println("USERNAME ES NULL");
                    
                    boolean result = server.checkLogin(pieces[0], pieces[1]);
                    
                    if(!result){
                        
                        //Si el usuario es inválido, se manda mensaje de error y se destruye el hilo
                        this.send(ChatServer.RESPONSE_JOIN_FAILED);
                        server.deleteThread(this);
                    }else{
                        //Si el nombre de usuario es válido, se manda mensaje de éxito y se establece
                        
                        System.out.println("USERNAME NO RESERVADO");
                        this.send(ChatServer.RESPONSE_JOIN_SUCCESSFUL);
                        System.out.println("INTENTO ENVIAR");
                        userName = pieces[0];
                    }
                    
                    
                }else if(text.equals(ChatServer.DISCONNECT_CODE)){
                    // Se desconecta pero no se destruye
                    isActive = false;
                    run = false;
                    server.deleteThread(this);
                    

                }else if(text.equals(ChatServer.GET_USERS_CODE)){
                    // Quiere obtener la lista de usuarios online y desconectados
                    this.send(server.listUsers(userName));
                    
                }else{
                    // Es un mensaje normal y corriente (Broadcast o privado)
                    String[] pieces = text.split(ChatServer.MESSAGE_DELIMITER);
                    
                    if(pieces[0].equals(ChatServer.GLOBAL_MESSAGE_CODE)){
                        // Mensaje de broadcast
                        server.broadcast(text);
                        
                    }else if(pieces[0].equals(ChatServer.PRIVATE_MESSAGE_CODE)){
                        // Mensaje privado
                        server.sendPrivate(userName, pieces[2], text);
                    }
                    
                    
                }
                
                
                
                //flujoS.writeUTF(id + " -> " + text);
                //flujoS.flush();
            } catch (Exception ex) {
                System.out.println("chivato"+ex.toString());
                run = false;
                isActive = false;
                server.deleteThread(this);
            }
        }
    }
    
    
    public void send(String text){
        try {
                
            flujoS.writeUTF(text);
            flujoS.flush();

        } catch (IOException ex) {
            System.out.println("send: "+ex.getLocalizedMessage());
        }
    }
    
    
    public void setId(int id){
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    
    
    
}
