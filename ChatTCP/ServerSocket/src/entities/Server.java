package entities;

import threads.*;
import events.ClientConnectedEvent;
import events.ClientDisconnectedEvent;
import events.MessageReceivedEvent;
import events.ServerEventsListener;
import java.io.DataOutputStream;
import java.io.IOException;
//import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server implements ServerEventsListener{
    private ServerSocket serverSocket;
    private Map<Integer, Client> clients;
    private int clientId;
    ConnectionsHandler connectionsHandler;
    DisconnectionsHandler disconnectionsHandler;
    
    public Server(int port) throws IOException{
        serverSocket = new ServerSocket(port);
        clients = new HashMap<>();
        clientId = 0;
        System.out.println("Servidor iniciado en el puerto " + port);
    }
    
    public void start() throws IOException{
        connectionsHandler = new ConnectionsHandler(serverSocket);
        connectionsHandler.addEventsListener(this);
        connectionsHandler.start();
        
        disconnectionsHandler = new DisconnectionsHandler(clients);
        disconnectionsHandler.addEventsListener(this);
        disconnectionsHandler.start();
    }
    
    private void sendBroadcast(String data) throws IOException{
        DataOutputStream out;
        int originID = Integer.parseInt(getData("<origin>", data));
        System.out.println(originID);
        String originNickname = clients.get(originID).getNickname();
        
        String message = getData("<message>", data);
        
        for (Client client: clients.values()) {
            Socket clientSocket = client.getSocket();
            out = new DataOutputStream(clientSocket.getOutputStream());
            out.writeUTF(originNickname+": "+message);
        }
        
        //for (int i = 1; i <= this.clientId; i++) {
        //    Socket clientSocket = clients.get(i).getSocket();
        //    out = new DataOutputStream(clientSocket.getOutputStream());
        //    out.writeUTF(originNickname+": "+message);
        //}
    }
    
    private synchronized void addClient(Client client){
        clients.put(clientId, client);
        client.setId(clientId);
    }
 
    @Override
    public void onUserConnected(ClientConnectedEvent evt) {
        try {
            Socket socket = evt.getSocket();
            Client client = new Client(socket);
            clientId++;
            addClient(client);
            //clients.put(clientId, client);
            //client.setId(clientId);
            
            ClientHandler clientHandler = new ClientHandler(socket.getInputStream());
            clientHandler.addEventsListener(this);
            clientHandler.start();
            
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("<id>"+clientId+";");
            //sendID(socket.getOutputStream());
            
            System.out.println("El usuario ha sido registrado con el ID = "+clientId);
        } catch(IOException ex) {
            System.out.println(ex);
        }
    }
    
    private String getData(String type, String dataMessage){
        int i = dataMessage.indexOf(type) + type.length();
        int f = dataMessage.indexOf(";", i);
        
        return dataMessage.substring(i, f);
    }
    private void setNicknameToClient(String data) {
        int id = Integer.parseInt(getData("<id>", data));
        String nickname = getData("<nickname>", data);
        
        Client client = clients.get(id);
        client.setNickname(nickname);
    }
    
    @Override
    public void onReceivedMessage(MessageReceivedEvent evt) {
        String message = evt.getMessage();
        if (message.contains("<nickname>")){
            setNicknameToClient(message);
        } else {
            try {
                sendBroadcast(message);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private synchronized void delClient(int id){
        clients.remove(id);
    }
    
    @Override
    public void onClientDisconnected(ClientDisconnectedEvent evt) {
        //clients.remove(evt.getId());
        delClient(evt.getId());
    }
}
