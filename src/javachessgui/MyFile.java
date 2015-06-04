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

import java.nio.file.Path;
import java.nio.file.InvalidPathException;
import java.util.Hashtable;
import java.util.Set;

public class MyFile {
    
    
    final static int MAX_LINES=10000;
    
    String path;
    
    public String[] lines=new String[MAX_LINES];
    
    public String content="";
    
    public int num_lines=0;
    
    private int level_of(String s)
    {
        int i=0;
        int level=0;
        while(i<s.length())
        {
            if(s.charAt(i)=='\t')
            {
                level++;
                i++;
            }
            else
            {
                return level;
            }
        }
        return level;
    }
    
    private int line_ptr;
    private Hashtable to_hash_recursive(Hashtable hash,int level)
    {
        
        while(line_ptr<num_lines)
        {
            
            String key=lines[line_ptr];
            
            if(level_of(key)==level)
            {
                
                key=key.substring(level);
                
                line_ptr++;
                
                String value=lines[line_ptr];
                
                if(level_of(value)==level)
                {
                    value=value.substring(level);
                    
                    hash.put(key,value);
                    line_ptr++;
                }
                else
                {
                    hash.put(key, to_hash_recursive(new Hashtable(),level+1));
                }
                
            }
            else
            {
                
                return hash;
                
            }
            
        }
        
        return hash;
        
    }
    
    public Hashtable to_hash()
    {
        Hashtable hash=new Hashtable();
        
        read_lines();
                        
        line_ptr=0;
        return to_hash_recursive(hash,0);
    }
    
    private void from_hash_recursive(Hashtable hash,String tab)
    {
        
        Set<String> keys = hash.keySet();
        for(String key: keys)
        {
            Object value=hash.get(key);
            
            if(value instanceof Hashtable)
            {
                lines[line_ptr++]=tab+key;
                from_hash_recursive((Hashtable)value,"\t"+tab);
            }
            else
            {
                lines[line_ptr++]=tab+key;
                lines[line_ptr++]=tab+value.toString();
            }
        }
        
    }
    
    public void from_hash(Hashtable hash)
    {
        
        line_ptr=0;
        
        from_hash_recursive(hash,"");
        
        num_lines=line_ptr;
        
        calc_content();
        
        write_content();
        
    }
    
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
    
    public Boolean write_content() {
        
        Path p;
            
        try
        {
            p=Paths.get(path);
        }
        catch(InvalidPathException ex)
        {
            System.out.println("invalid path "+path);

            return false;
        }

        try
        {
            
            Files.write(p, content.getBytes());
            return true;
            
        }
        catch(IOException ex)
        {
            
            System.out.println("IO error writing file "+path);
            return false;
            
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
    
    public Boolean add_line(String line)
    {
        read_lines();
        
        for(int i=0;i<num_lines;i++)
        {
            if(lines[i].equals(line))
            {
                return true;
            }
        }
        
        if(num_lines>=MAX_LINES)
        {
            return false;
        }
        
        lines[num_lines++]=line;
        
        calc_content();
        
        return write_content();
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
