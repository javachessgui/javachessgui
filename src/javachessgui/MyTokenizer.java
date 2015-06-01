package javachessgui;

public class MyTokenizer {
    
    String content;
    
    public String get_char()
    {
        if(content.length()==0)
        {
            return null;
        }
        String c=""+content.charAt(0);
        content=content.substring(1);
        return c;
    }
    
    public void flush()
    {
        String c;
        do
        {
            c=get_char();
            if(c==null)
            {
                return;
            }
        }while(c.equals(" "));
        content=c+content;
    }
    
    public String get_token()
    {
        String token=null;
        flush();
        String c;
        while((c=get_char())!=null)
        {
            if(c.equals(" "))
            {
                return token;
            }
            if(token==null)
            {
                token=c;
            }
            else
            {
                token+=c;
            }
        }
        return token;
    }
    
    public MyTokenizer(String what)
    {
        content=what;
    }
}
