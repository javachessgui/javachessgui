package javachessgui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MyFile {
   
    final static int MAX_LINES=10000;
    
    String path;
    
    public String[] lines=new String[MAX_LINES];
    
    public String content="";
    
    public void write_content() {

        try
        {
            Files.write(Paths.get(path), content.getBytes());
        }
        catch(IOException ex)
        {

        }
        
    }
    
    public String[] read_lines()
    {
        
        int cnt=0;
        
        File f = new File(path);
        
        if(f.exists())
        {
            FileReader fr=null;
            try {
               fr=new FileReader(path);
               } catch(IOException ex) {
               
               }
            
            BufferedReader br=new BufferedReader(fr);
            
            String CurrentLine=null;
            
            do
            {
            try {
               CurrentLine = br.readLine();
               } catch(IOException ex) {
               
               }
            
            if(CurrentLine!=null)
            {
                if(cnt<MAX_LINES)
                {
                    lines[cnt++]=CurrentLine;
                    content+=CurrentLine+"\n";
                }
            }
            
            }while(CurrentLine!=null);
            
            if(cnt==0){return null;}
            
            return Arrays.copyOfRange(lines, 0, cnt);
            
        }
        
        return null;
        
    }
    
    
    public MyFile(String set_path)
    {
        path=set_path;
    }
    
}
