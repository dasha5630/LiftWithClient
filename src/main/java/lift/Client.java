
package lift;

import main.java.lift.net.MessageReceiver;
import main.java.lift.events.MessageListener;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Класс пользователя лифта (может несколько пользователей), который нажимает нужный этаж 
 */

public class Client implements Runnable, Closeable, MessageListener {

    private Socket socket;

    private BufferedReader reader;

    private PrintWriter writer;

    private MessageReceiver messageResiver;

    private Client(String host, int port) throws UnknownHostException, IOException {

        socket = new Socket(host, port);
        OutputStream output = socket.getOutputStream();
        writer = new PrintWriter(output);
        Reader input = new InputStreamReader(System.in);
        reader = new BufferedReader(input);

        Set<Socket> sockets = Collections.singleton(socket);
        messageResiver = new MessageReceiver(sockets);
        messageResiver.addListener(this);
    }

    @Override
    public void onMessangeReceived(Socket socket,
            String message) {
        System.out.println("> " + message);
    }

    @Override
    public void run() {
        Thread recieverThread = new Thread(messageResiver);
        recieverThread.start();
        System.out.println("Enter the floor you'd like to go to ");
        String line;
        Integer fl;
        while (true) {

            if (Thread.interrupted() || !socket.isConnected()) {
                Thread.currentThread().interrupt();
                break;
            }
            try {
                
                line = reader.readLine();
                if ((Integer.parseInt(line) < 8) && (Integer.parseInt(line) > 0)){
                writer.print(line);
                writer.flush(); //отправляем текст
                }
                else{ System.out.println("1 < floor < 7");}
            } catch (IOException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        try {
            recieverThread.interrupt();
            close();
        } catch (IOException ignore) {
        }
    }

    @Override
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
            socket = null;
        }

        if (writer != null) {
            writer.close();
            writer = null;
        }
        if(messageResiver != null){
            messageResiver.close();
        }
        reader = null;
    }

    public static void main(String[] args) {
        try (Client client = new Client("localhost", 8080)) {
            client.run();
        } catch (ConnectException ex) {
            System.err.println("Server unreacheble!");

        } catch (IOException ex) {
            System.err.println("Error encountered");
            ex.printStackTrace(System.err);
        }

    }

}
