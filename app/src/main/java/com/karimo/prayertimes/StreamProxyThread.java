package com.karimo.prayertimes;

import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import java.io.*;
import java.net.*;

public class StreamProxyThread implements Runnable {
    //MediaPlayer buffer is not useful for streaming compressed remote data
    //hence, stuttering occurs when trying to load mp3 data for decompression while playing
    //the idea is, to create a local proxy server that
    //will make the request to the remote url and download byte data
    //the MediaPlayer can then open a socket to the localhost and read from the byte data
    private static final int SERVER_PORT=8888;
    private static final String TAG = "StreamProxyThread";
    private Thread thread;
    private boolean isRunning;
    private ServerSocket socket;
    private int port;
    public StreamProxyThread() {
        //create a listening socket
        try {
            socket = new ServerSocket(SERVER_PORT, 0, InetAddress.getByAddress(new byte[]{127,0,0,1}));
            socket.setSoTimeout(5000);
            port = socket.getLocalPort();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            Log.e(TAG, "IOException init server: ", e);
        }
    }
    public void start() {
        thread = new Thread(this);
        thread.start();
    }
    public void stop() {
        isRunning = false;
        thread.interrupt();
        try {
            thread.join(5000);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        Looper.prepare();
        isRunning = true;
        while(isRunning) {
            try {
                Socket client = socket.accept();
                if(client == null) {
                    continue;
                }
                Log.d(TAG, "client connected");


            }
            catch(SocketTimeoutException sce) {
                Log.e(TAG, "Socket timed out: ", sce);
            } catch (IOException e) {
                Log.e(TAG, "Error connecting to client: ", e);
            }
        }
    }

    private class StreamToMediaPlayerTask extends AsyncTask<String, Void, Integer> {
        String localPath;
        Socket client;
        int cbSkip;
        public StreamToMediaPlayerTask(Socket client) {
            this.client = client;
        }
        public String readTextStreamAvailable(InputStream inputStream) throws IOException {
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);

            // Do the first byte via a blocking read
            outputStream.write(inputStream.read());

            // Slurp the rest
            int available = inputStream.available();

            while (available > 0) {
                int cbToRead = Math.min(buffer.length, available);
                int cbRead = inputStream.read(buffer, 0, cbToRead);
                if (cbRead <= 0) {
                    throw new IOException("Unexpected end of stream");
                }
                outputStream.write(buffer, 0, cbRead);
                available -= cbRead;
            }
            return new String(outputStream.toByteArray());
        }

        public boolean processRequest() {
            //read HTTP headers
            String headers = "";
            try {
                headers = readTextStreamAvailable(client.getInputStream());
            } catch (IOException e) {
                Log.e(TAG, "Error reading HTTP request header from stream: ", e);
                return false;
            }
            //get the important bits from the headers
            String[] headerLines = headers.split("\n");
            String urlLine = headerLines[0];
            if(!urlLine.startsWith("GET ")) {
                Log.e(TAG, "Only GET is supported");
                return false;
            }
            urlLine = urlLine.substring(4);
            int charPos = urlLine.indexOf(' ');
            if(charPos != -1) {
                urlLine = urlLine.substring(1, charPos);
            }
            localPath = urlLine;

            //see if there is a "Range:" header
            for(int i = 0; i < headerLines.length; i++) {
                String headerLine = headerLines[i];
                if(headerLine.startsWith("Range: bytes=")) {
                    headerLine = headerLine.substring(13);
                    charPos = headerLine.indexOf('-');
                    if(charPos > 0) {
                        headerLine = headerLine.substring(0, charPos);
                    }
                    cbSkip = Integer.parseInt(headerLine);
                }
            }
            return true;
        }
        @Override
        protected Integer doInBackground(String... strings) {
            //in here is where we pass data onto MediaPlayer
            //get content length here
            File file = new File(localPath);
            long fileSize = 0;
            //create http header
            String headers = "HTTP/1.0 200 OK\r\n";
            headers += "Content-Type: audio/mpeg\r\n";
            headers += "Content-Length: " + fileSize  + "\r\n";
            headers += "Connection: close\r\n";
            headers += "\r\n";

            //begin with sending over http header
            int fc = 0;
            long cbToSend = fileSize - cbSkip;
            OutputStream output = null;
            byte[] buff = new byte[64 * 1024];
            try {
                output = new BufferedOutputStream(client.getOutputStream(), 32 * 1024);
                output.write(headers.getBytes());

                //loop as long as there is stuff to send
                while(isRunning && cbToSend > 0 && !client.isClosed()) {
                    //see if there is more to send
                    File temp = new File(localPath);
                    fc++;
                    int cbSentThisBatch = 0;
                    if(temp.exists()) {
                        FileInputStream input = new FileInputStream(temp);
                        input.skip(cbSkip);
                        int cbToSendThisBatch = input.available();
                        while(cbToSendThisBatch > 0) {
                            int cbToRead = Math.min(cbSentThisBatch, buff.length);
                            int cbRead = input.read(buff, 0, cbToRead);
                            if(cbRead == -1) {
                                break;
                            }
                            cbToSend -= cbRead;
                            cbToSend -= cbRead;
                            output.write(buff, 0, cbRead);
                            output.flush();
                            cbSkip += cbRead;
                            cbSentThisBatch += cbToRead;
                        }
                        input.close();
                    }
                    //if we did nothing this batch, block for a sec
                    if(cbSentThisBatch == 0) {
                        Log.d(TAG, "Blocking until more data appears");
                        Thread.sleep(1000);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //cleanup
            try {
                if(output != null) {
                    output.close();
                }
                client.close();
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
            return 1;
        }
    }
}
