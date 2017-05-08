import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

/*
 * WebWorker is a subclass of Thread that downloads the content for one url.
 */

public class WebWorker extends Thread {
   private final WebFrame frame;
   private final String urlString;
   private final int rowNum;
   private final String INTERRUPTED = "Interrupted";
  
   public WebWorker(String url,int rowNum, WebFrame frame) {
       urlString = url;
       this.frame = frame;
       this.rowNum = rowNum;
   }
  
   @Override
   public void run(){
       frame.increaseThreads();
       try {
           download();
       } catch (IOException ex) {
           Logger.getLogger(WebWorker.class.getName()).log(Level.SEVERE, null, ex);
       }
       frame.decreaseThreads();
   }
  
   private void download() throws IOException{
        InputStream input = null;
       StringBuilder contents;
       try {
           URL url = new URL(urlString);
           URLConnection connection = url.openConnection();
           connection.setConnectTimeout(5000);
           connection.connect();
           input = connection.getInputStream();
           
           BufferedReader reader = new BufferedReader(new InputStreamReader(input));
           char[] array = new char[1000];
           int len;
           contents = new StringBuilder(1000);
           long start = System.currentTimeMillis();
           while ((len = reader.read(array, 0, array.length)) > 0) {
               if(Thread.interrupted()){
                   frame.updateTable(rowNum, INTERRUPTED);
               }
               contents.append(array, 0, len);
               Thread.sleep(100);
           }
           long end = System.currentTimeMillis();
            SimpleDateFormat ft = new SimpleDateFormat ("hh:mm:ss");
            Date dat = new Date();
            String curTime = ft.format(dat);
           String status = curTime + "   " + (end-start) + "ms   " + contents.length() + " bytes";
           frame.updateTable(rowNum, status);
       }
       
       // Otherwise control jumps to a catch...
       catch(MalformedURLException e) {
           frame.updateTable(rowNum, "err");
       } catch(InterruptedException e) {
           frame.updateTable(rowNum, INTERRUPTED);
       } catch(IOException e) {
           frame.updateTable(rowNum, "err");
       }
       
       // "finally" clause, to close the input stream
       // in any case
       finally {
           try{
               if (input != null) input.close();
           }
           catch(IOException e) {}
       }
   }
}