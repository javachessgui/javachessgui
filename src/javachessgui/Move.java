package javachessgui;

public class Move {
    
    public int i1,j1;
    public int i2,j2;
    
    char orig_piece;
    
    public Move()
    {
        i1=0;
        j1=0;
        i2=0;
        j2=0;
        orig_piece=' ';
    }
    
    public void from_algeb(String algeb)
    {
        
        if(algeb.length()<2)
        {
            // error
            return;
        }
        
        i1=algeb.charAt(0)-'a';
        j1='8'-algeb.charAt(1);
        
        if(algeb.length()<4)
        {
            // error
            return;
        }
        
        i2=algeb.charAt(2)-'a';
        j2='8'-algeb.charAt(3);
        
    }
}
