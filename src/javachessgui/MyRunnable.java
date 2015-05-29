package javachessgui;

public class MyRunnable implements Runnable {
    
    public String kind;
    
     public void run(){
         
         int i=0;
         
         while (!Thread.currentThread().isInterrupted()) {
             
            
               System.out.println(kind+" running "+i++);

               try {
               Thread.sleep(1000);
               } catch(InterruptedException ex) {
               Thread.currentThread().interrupt();
               }

               
         
         }
         
    }
}
