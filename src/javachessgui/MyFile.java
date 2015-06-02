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
    
    public int num_lines=0;
    
    public String calc_content()
    {
        content="";
        
        for(int i=0;i<num_lines;i++)
        {
            content+=lines[i]+"\n";
        }
        
        return content;
    }
    
    public String set(String key,String value)
    {
        for(int i=0;i<num_lines;i++)
        {
            
            String line=lines[i];
        
            Pattern get_key = Pattern.compile("^"+key+"=(.*)");
            Matcher get_key_matcher = get_key.matcher(line);

                if (get_key_matcher.find( )) {
                   if(value==null)
                   {
                       return(get_key_matcher.group(1));
                   }
                   lines[i]=key+"="+value;
                   return value;
                }
        }
        
        if(value==null)
        {
            return null;
        }
        
        if(num_lines<MAX_LINES)
        {
            lines[num_lines++]=key+"="+value;
            return value;
        }
        
        return null;
    }
    
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
        
        num_lines=0;
        
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
            
            num_lines=cnt;
            
            return Arrays.copyOfRange(lines, 0, cnt);
            
        }
        
        return null;
        
    }
    
    public String set_field(String key,String value)
    {
        read_lines();
        String result=set(key,value);
        if(result!=null)
        {
            calc_content();
            write_content();
            return value;
        }
        return null;
    }
    
    public String get_field(String key)
    {
        read_lines();
        String result=set(key,null);
        if(result!=null)
        {
            return result;
        }
        return null;
    }
    
    
    public MyFile(String set_path)
    {
        path=set_path;
    }
    
}
