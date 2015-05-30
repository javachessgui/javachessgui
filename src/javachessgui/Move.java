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
    
    public String ij_to_algeb(int i,int j)
    {
        String algeb="";
        algeb+=(char)(i+(int)'a');
        algeb+=(char)((7-j)+(int)'1');
        return algeb;
    }
    
    public String to_algeb()
    {
        String algeb="";
        
        algeb+=ij_to_algeb(i1,j1);
        algeb+=ij_to_algeb(i2,j2);
        
        return algeb;
    }
    
    public void copy(Move m)
    {
        
        i1=m.i1;
        j1=m.j1;
        i2=m.i2;
        j2=m.j2;
        orig_piece=m.orig_piece;
        
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
