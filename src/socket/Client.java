/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socket;


import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Tecnovision
 */
public class Client {

    private static final Logger log = Logger.getLogger(Client.class.getName());
    Socket socket;
    PrintWriter out;

    public boolean sendMessage(String text) {
        boolean error = true;

        try {

            //1. creating a socket to connect to the server
            socket = new Socket("localhost", 23);

            //2. get Input and Output streams
            //out = new PrintWriter(socket.getOutputStream());
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "ISO-8859-1"));
            out.flush();

            //3. Sending message
            out.println(text);
            out.flush();

            
            //error = out.checkError();
            Thread.sleep(500);
            

            //4. closing socket
            socket.close();
            error = false;

        } catch (InterruptedException | IOException ex) {
            log.error(ex);
        }

        return error;
    }
}
