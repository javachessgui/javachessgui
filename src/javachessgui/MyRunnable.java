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
             
            
               //System.out.println(kind+" running "+i++);
               
               if(kind=="engine_read")
               {
                   
                   try {
                    char chunk=(char)std_in.read();
                    
                    if(chunk=='\n')
                    {
                        //total_buffer=buffer+"\n"+total_buffer;
                        b.engine_text.setText(buffer);
                        buffer="";
                    }
                    else
                    {
                        buffer+=chunk;
                    }
                    
                    } catch(IOException ex) {
                        System.out.println("engine read IO exception");
                    }
                   
               }
               else if(kind=="engine_write")
               {
                   if((command!="")&&(command!=null))
                   {
                       
                       System.out.print("Command "+command);
                       
                       try {
                        
                        std_out.write(command.getBytes());
                        std_out.flush();
                        
                        } catch(IOException ex) {
                       
                        }
                       
                       command="";
                       
                       try {
                        Thread.sleep(100);
                        } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        }
                       
                   }
               }

               

               
         
         }
         
    }
}
