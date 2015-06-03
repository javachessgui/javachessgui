package javachessgui;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class MyRunnable implements Runnable {
    
    public String kind;
    public InputStream std_in;
    public OutputStream std_out;
    public String command;
    public Board b;
    
     public void run(){
         
         int i=0;
         
         String total_buffer="";
         String buffer="";
         
         while (!Thread.currentThread().isInterrupted()) {
             
               if(kind=="engine_read")
               {
                   
                   try
                   {
                       
                        char chunk=(char)std_in.read();

                        if(chunk=='\n')
                        {
                            //total_buffer=buffer+"\n"+total_buffer;
                            b.consume_engine_out(buffer);
                            buffer="";
                        }
                        else
                        {
                            buffer+=chunk;
                        }

                    }
                    catch(IOException ex)
                    {
                        
                        System.out.println("engine read IO exception");
                        
                    }
                   
               }

         }
         
    }
}
