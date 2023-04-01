import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;

public class Server implements Runnable{


    private ArrayList<ConnectionHandler> connections;

    private boolean done = false;
    ServerSocket server;

    private ExecutorService pool;

    public Server(){
        connections = new ArrayList<ConnectionHandler>();
    }
    @Override
    public void run() {
        try {
            pool = Executors.newCachedThreadPool();
            server = new ServerSocket(9999);
            if(!done){
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
            }
        } catch (IOException e) {
             // TODO: handle
        }

    }

    public void shutdown(){
        if(!server.isClosed()){
            try {
                server.close();
                for(ConnectionHandler ch : connections){
                    ch.shutdown();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void broadcast(String message){
        for(ConnectionHandler ch : connections){
            if(message != null){
                ch.sendMessage(message);
            }
        }
    }

    class ConnectionHandler implements Runnable{

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        public ConnectionHandler(Socket client){
            this.client = client;
        }





        @Override
        public void run() {
            try{
                // cliente veri gonderirken output streamine yaziyoruz.
                out = new PrintWriter(client.getOutputStream(),true);

                // client'ın input stream'ini kullanicinin girdiği veri olarak dusunebiliriz.
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("please enter a nickname");
                nickname = in.readLine();
                System.out.println(nickname + "connected ");
                broadcast(nickname + "joined the chat ");

                String message;

                while ((message = in.readLine()) != null){
                    broadcast(nickname + ": " + message );
                }

            }catch(IOException e){
                shutdown();
            }
        }

        public void sendMessage(String message){
            out.println(message);
        }
        
        public void shutdown(){
            if(client.isClosed()){
                try {
                    in.close();
                    out.close();
                    client.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    public static void main(String args[]){
        Server server = new Server();
        server.run();
    }
}
