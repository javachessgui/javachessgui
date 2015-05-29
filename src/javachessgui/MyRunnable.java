package javachessgui;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class MyRunnable implements Runnable {
    
    public String kind;
    public InputStream std_in;
    public OutputStream std_out;
    public String command;
    
     public void run(){
         
         int i=0;
         
         while (!Thread.currentThread().isInterrupted()) {
             
            
               //System.out.println(kind+" running "+i++);
               
               if(kind=="engine_read")
               {
                   
                   try {
                    int chunk=std_in.read();
                    System.out.print((char)chunk);
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
