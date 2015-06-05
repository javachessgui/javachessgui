package javachessgui;

public class Encode32 {
    
       
    public static String encode(String s,Boolean encode)
    {
        
        byte[] bytes = s.getBytes();
        
        int mask=(encode?5:8);
        
        String binary="";
        
        int letter=(int)'a';
        int number=(int)'0';
        
        for (byte b : bytes)
        {
           int val = b;
           
           if(!encode)
           {
               char x=(char)b;
               if((x>='a')&&(x<='z'))
               {
                   val=(int)x-letter;
               }
               else
               {
                   val=(int)x-number+26;
               }
           }
                     
           for (int i = 0; i < (encode?8:5); i++)
           {
              binary+=((val & (encode?128:16)) == 0 ? "0" : "1");
              val <<= 1;
           }
           
        }
        
        int mod=binary.length()%mask;
        
        if(encode)
        {
            while(mod++<mask){binary="0"+binary;}
        }
        else
        {
            binary=binary.substring(mod);
        }
        
        String out="";
               
        
        while(binary.length()>0)
        {
            
            String chunk=binary.substring(0,mask);
            binary=binary.substring(mask);
            
            int b = Integer.parseInt(chunk, 2);
            
            if(encode)
            {
                b=b<26?b+letter:b-26+number;
            }
            else
            {
                
            }
        
            char c=(char)b;
            
            out+=c;
            
        }
        
        return out;
        
    }
    
    public Encode32()
    {
        
    }
            
    
}
