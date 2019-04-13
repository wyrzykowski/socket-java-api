import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.Executors;

/**
 * A server program which accepts requests from clients to capitalize strings. When
 * a client connects, a new thread is started to handle it. Receiving client data,
 * capitalizing it, and sending the response back is all done on the thread, allowing
 * much greater throughput because more clients can be handled concurrently.
 */
public class WeatherInfoServer {

    /**
     * Runs the server. When a client connects, the server spawns a new thread to do
     * the servicing and immediately returns to listening. The application limits the
     * number of threads via a thread pool (otherwise millions of clients could cause
     * the server to run out of resources by allocating too many threads).
     */
    public static void main(String[] args) throws Exception {
        try (var listener = new ServerSocket(8080)) { //create new server socket

            System.out.println("Server waiting for connections...");

            var pool = Executors.newFixedThreadPool(20);

            while (true) {
                pool.execute(new WeatherInformation(listener.accept())); //execute method WeatherInformation for every thread
            }
        }
    }

    private static class WeatherInformation implements Runnable {
        private Socket socket;

        WeatherInformation(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            System.out.println("Connected: " + socket);
            try {
                JSONObject json = new JSONObject();
                var in = new Scanner(socket.getInputStream());
                var out = new PrintWriter(socket.getOutputStream(), true);

                System.out.println(in.nextLine());
                while(true) {

                    long timeNow =System.currentTimeMillis(); //get actual server time

                    JSONParser parser = new JSONParser();
                    try
                    {
                        Object object = parser
                                .parse(new FileReader("sample.json"));

                        //convert Object to JSONObject
                        JSONObject jsonObject = (JSONObject)object;
                        String stringTime =  Long.toString(timeNow);

                        //Add actual time to Json string object
                        jsonObject.put("time",stringTime);
                        out.println(jsonObject);

                        //Reading the String
                        String temperature = (String) jsonObject.get("temperature");
                        String wind = (String) jsonObject.get("wind");
                        String humidity = (String) jsonObject.get("humidity");

                        //Reading the array
                        JSONArray moreInfo = (JSONArray)jsonObject.get("moreInfo");

                        //Printing all the values
                        System.out.println("temperature: " + temperature);
                        System.out.println("wind: " + wind);
                        System.out.println("humidity:"+humidity);
                        for(Object info : moreInfo)
                        {
                            System.out.println("\t"+info.toString());
                        }
                    }
                    catch(FileNotFoundException fe)
                    {
                        fe.printStackTrace();
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }

                        json.put("time:", timeNow);
//                        out.println(json);
                    Thread.sleep(2000);
                }

            } catch (Exception e) {
                System.out.println("Error:" + socket);
            } finally {
                try { socket.close(); } catch (IOException e) {}
                System.out.println("Closed: " + socket);
            }
        }


    }
}