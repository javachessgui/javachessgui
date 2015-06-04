package javachessgui;

public class Encode32 {
    
       
    public String encode(String s)
    {
        
        byte[] bytes = s.getBytes();
        
        String binary="";
        
        for (byte b : bytes)
        {
           int val = b;
           
           for (int i = 0; i < 8; i++)
           {
              binary+=((val & 128) == 0 ? "0" : "1");
              val <<= 1;
           }
           
        }
        
        int mod=binary.length()%5;
        while(mod++<5){binary="0"+binary;}
        
        String out="";
        while(binary.length()>0)
        {
            String chunk=binary.substring(0,5);
            binary=binary.substring(5);
            
            int b = Integer.parseInt(chunk, 2);
            int letter=(int)'a';
            int number=(int)'0';
            b=b<26?b+letter:b-26+number;
            char c=(char)b;
            
            out+=c;
            
        }
        
        return out;
        
    }
    
    public Encode32()
    {
        
    }
            
    
}
