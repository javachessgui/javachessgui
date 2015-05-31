package javachessgui;

import java.io.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import javafx.event.ActionEvent;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.*;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;

import java.io.IOException;

import javafx.scene.control.TextArea;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import javafx.application.Platform;

import javax.swing.JOptionPane; 

public class Board {
    
    public Game g=null;
    
    ////////////////////////////////////////////////////////
    // static members
    private static String uci_engine_path;
    
    private MyRunnable runnable_engine_read_thread;
    private MyRunnable runnable_engine_write_thread;
    
    private Thread engine_read_thread;
    private Thread engine_write_thread;
    
    private ProcessBuilder uci_engine_process_builder;
    private Process uci_engine_process;
    
    private InputStream engine_in;
    private OutputStream engine_out;
    
    final static int TURN_WHITE=1;
    final static int TURN_BLACK=-1;
    
    private static Hashtable translit_light;
    private static Hashtable translit_dark;
    
    private static InputStream stream = Javachessgui.class.getResourceAsStream("resources/fonts/MERIFONTNEW.TTF");
    private static Font chess_font;
    ////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////
    // board representation
    private String castling_rights;
    private String ep_square_algeb;
    
    private int halfmove_clock;
    private int fullmove_number;
    
    private int turn;
    
    private String rep;
    private char[][] board=new char[8][8];
    private char[][] fonts=new char[8][8];
    
    private Boolean flip;
    ////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////
    // drag and drop
    private Boolean is_drag_going;
    private char drag_piece;
    private char orig_drag_piece;
    private char orig_piece;
    private char orig_empty;
    private int drag_from_i;
    private int drag_from_j;
    private int drag_to_i;
    private int drag_to_j;
    private int drag_to_x;
    private int drag_to_y;
    private int drag_from_x;
    private int drag_from_y;
    private int drag_dx;
    private int drag_dy;
    ////////////////////////////////////////////////////////
	
    ////////////////////////////////////////////////////////
    // gc attributes
    private Group canvas_group=new Group();
    
    public VBox vertical_box=new VBox(2);
    private HBox controls_box=new HBox(2);
    
    private TextField fen_text = new TextField ();
    public TextArea engine_text = new TextArea ();
    
    public Canvas canvas;
    public Canvas upper_canvas;
    public Canvas engine_canvas;
    
    private GraphicsContext gc;
    private GraphicsContext upper_gc;
    private GraphicsContext engine_gc;
    
    private static int padding;
    private static int piece_size;
    private static int margin;
    private static int board_size;
    private static int info_bar_size;
    private static int font_size;
    
    private Color board_color;
    private Color piece_color;
    private Color engine_color;
    private int color_r;
    private int color_g;
    private int color_b;
    private Color score_color;
    private Font engine_font=new Font("Courier New",12);
    ////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////
    // uci out
    private int depth;
    private String pv;
    private String bestmove_algeb;
    Move bestmove;
    Move makemove=new Move();
    private int score_cp;
    private int score_mate;
    private String score_verbal;
    private int score_numerical;
    private Boolean engine_running;
    ////////////////////////////////////////////////////////
    
    
    ////////////////////////////////////////////////////////
    // move generation
    final static int move_table_size=20000;
    static MoveDescriptor move_table[]=new MoveDescriptor[move_table_size];
    static int move_table_ptr[][][]=new int[8][8][32];
    
    final static int STRAIGHT=16;
    final static int DIAGONAL=8;
    final static int SLIDING=STRAIGHT|DIAGONAL;
    final static int SINGLE=4;
    
    final static int QUEEN=STRAIGHT|DIAGONAL;
    final static int ROOK=STRAIGHT;
    final static int BISHOP=DIAGONAL;
    final static int KNIGHT=SINGLE;
    final static int KING=SINGLE|STRAIGHT|DIAGONAL;
    final static int PAWN=2;
    
    final static int PIECE_TYPE=30;
    final static int PIECE_COLOR=1;
    
    final static int WHITE=1;
    final static int BLACK=0;
    ////////////////////////////////////////////////////////
    
    private int piece_code_of(int piece)
    {
        
        if(piece=='p'){return BLACK|PAWN;}
        if(piece=='P'){return WHITE|PAWN;}
        if(piece=='n'){return BLACK|KNIGHT;}
        if(piece=='N'){return WHITE|KNIGHT;}
        if(piece=='b'){return BLACK|BISHOP;}
        if(piece=='B'){return WHITE|BISHOP;}
        if(piece=='r'){return BLACK|ROOK;}
        if(piece=='R'){return WHITE|ROOK;}
        if(piece=='Q'){return BLACK|QUEEN;}
        if(piece=='q'){return WHITE|QUEEN;}
        
        return 0;
    }
    
    private static Boolean square_ok(int i,int j)
    {
        if((i>=0)&&(i<=7)&&(j>=0)&&(j<=7))
        {
            return true;
        }
        return false;
    }

    public static void init_class()
    {
        
        ////////////////////////////////////////////
        // read config
        
        uci_engine_path="";
        
        File f = new File("config.txt");
        if(f.exists())
        {
            FileReader fr=null;
            try {
               fr=new FileReader("config.txt");
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
                Pattern get_pv = Pattern.compile("(engine=)(.*)");
                Matcher pv_matcher = get_pv.matcher(CurrentLine);

                if (pv_matcher.find( )) {
                   uci_engine_path=pv_matcher.group(2);
                   System.out.println("engine path: "+uci_engine_path);
                }
            }
            
            }while(CurrentLine!=null);
 
            
        }
        else
        {
            try {
               f.createNewFile();
               } catch(IOException ex) {
               
               }
            
            FileWriter fw=null;
            try {
               fw = new FileWriter(f.getAbsoluteFile());
               } catch(IOException ex) {
               
               }
            
            BufferedWriter bw = new BufferedWriter(fw);
                     
            try {
               bw.write("");
               } catch(IOException ex) {
               
               }
            
            try {
               bw.close();
               } catch(IOException ex) {
               
               }
            
        
        }
        
        //uci_engine_path="";    
        
        ////////////////////////////////////////////
        
        piece_size=52;
        padding=5;
        margin=10;
        font_size=15;
                
        board_size=(piece_size+padding)*8+2*margin;
        info_bar_size=font_size+2*padding+margin;
        
        chess_font=Font.loadFont(stream, piece_size);
        
        // light square conversion
		
        translit_light=new Hashtable();
		
        translit_light.put(' ',' ');
        translit_light.put('P','p');
        translit_light.put('N','n');
        translit_light.put('B','b');
        translit_light.put('R','r');
        translit_light.put('Q','q');
        translit_light.put('K','k');
        translit_light.put('p','o');
        translit_light.put('n','m');
        translit_light.put('b','v');
        translit_light.put('r','t');
        translit_light.put('q','w');
        translit_light.put('k','l');
		
        // dark square conversion
		
        translit_dark=new Hashtable();
		
        translit_dark.put(' ','+');
        translit_dark.put('P','P');
        translit_dark.put('N','N');
        translit_dark.put('B','B');
        translit_dark.put('R','R');
        translit_dark.put('Q','Q');
        translit_dark.put('K','K');
        translit_dark.put('p','O');
        translit_dark.put('n','M');
        translit_dark.put('b','V');
        translit_dark.put('r','T');
        translit_dark.put('q','W');
        translit_dark.put('k','L');
        
        // init move descriptors
        
        int move_table_curr_ptr=0;
        
        for(int i=0;i<8;i++)
        {
            for(int j=0;j<8;j++)
            {
                for(int p=0;p<32;p++)
                {
                    int piece_type=p&PIECE_TYPE;
                    
                    System.out.println("i "+i+" j "+j+" p "+p+" curr "+move_table_curr_ptr);
                    
                    if((piece_type==QUEEN)||(piece_type==ROOK)||(piece_type==BISHOP)||(piece_type==KNIGHT)||(piece_type==KING))
                    {
                        
                        Boolean is_single=(piece_type&SINGLE)!=0;
                        
                        move_table_ptr[i][j][p]=move_table_curr_ptr;
                        
                        for(int vi=-2;vi<=2;vi++)
                        {
                            for(int vj=-2;vj<=2;vj++)
                            {
                                if(
                                        
                                        ((Math.abs(vi)+Math.abs(vj))>0)
                                        
                                        &&
                                        
                                        (
                                        
                                            (
                                                ((vi*vj)!=0)
                                                &&
                                                ((Math.abs(vi)!=2)&&(Math.abs(vj)!=2))
                                                &&
                                                ((piece_type&DIAGONAL)!=0)
                                            )
                                        
                                            ||
                                        
                                            (
                                                ((vi*vj)==0)
                                                &&
                                                ((Math.abs(vi)!=2)&&(Math.abs(vj)!=2))
                                                &&
                                                ((piece_type&STRAIGHT)!=0)
                                            )
                                        
                                            ||
                                        
                                            (
                                                (Math.abs(vi*vj)==2)
                                                &&
                                                (piece_type==KNIGHT)
                                            )
                                            
                                        )
                                        
                                )
                                {
                                    
                                    int start_vector=move_table_curr_ptr;
                                    
                                    int ci=i;
                                    int cj=j;
                                    Boolean square_ok;
                                    do
                                    {
                                        
                                        ci+=vi;
                                        cj+=vj;
                                        square_ok=square_ok(ci,cj);
                                        if(square_ok)
                                        {
                                            MoveDescriptor md=new MoveDescriptor();
                                            md.to_i=ci;
                                            md.to_j=cj;
                                            
                                            move_table[move_table_curr_ptr++]=md;
                                        }
                                             
                                    }while(square_ok&&(!is_single));
                                    
                                    for(int ptr=start_vector;ptr<move_table_curr_ptr;ptr++)
                                    {
                                        move_table[ptr].next_vector=move_table_curr_ptr;
                                    }
                                    
                                    
                                }
                            }
                        }
                        
                        // piece finished
                        
                        move_table[move_table_curr_ptr]=new MoveDescriptor();
                        move_table[move_table_curr_ptr++].end_piece=true;
                        
                    }
                    
                    
                    if(piece_type==PAWN)
                    {
                        move_table[move_table_curr_ptr]=new MoveDescriptor();
                        move_table[move_table_curr_ptr++].end_piece=true;
                    }
                    
                }
            }
        }
       
        //System.out.println("board class init complete");
        
        
    }
    
    public void record_position()
    {
        g.add_position(report_fen());
    }
    
    private void reset_game()
    {
        if(g!=null)
        {
            g.reset();
            record_position();
        }
    }
    
    private void update_engine()
    {
        if((bestmove_algeb!="")&&(bestmove_algeb!=null))
        {
            
            color_r=0;
            color_g=0;
            
            int color_limit=500;
            int draw_limit=80;
            int min_color=127;
            
            color_b=min_color;
            
            if(score_numerical<-color_limit)
            {
                color_r=255;
                color_b=0;
            }
            else if(score_numerical<-draw_limit)
            {
                color_r=-score_numerical/color_limit*255;
                if(color_r<min_color){color_r=min_color;}
                color_b=0;
            }
            
            if(score_numerical>color_limit)
            {
                color_g=255;
                color_b=0;
            }
            else if(score_numerical>draw_limit)
            {
                color_g=score_numerical/color_limit*255;
                if(color_g<min_color){color_g=min_color;}
                color_b=0;
            }
            
            score_color=Color.rgb(color_r,color_g,color_b);
            
            bestmove.from_algeb(bestmove_algeb);
            
            int shift=(int)((piece_size)/2);

            int from_x=gc_x(bestmove.i1)+shift;
            int from_y=gc_y(bestmove.j1)+shift+padding;
            int to_x=gc_x(bestmove.i2)+shift;
            int to_y=gc_y(bestmove.j2)+shift+padding;
            
            Platform.runLater(new Runnable()
            {
               public void run()
               {
                   engine_text.setFont(engine_font);
            
                    engine_text.setText(
                            "depth "+depth+
                            "\npv "+pv+
                            "\nscore "+score_verbal+
                            "\nscore numerical "+score_numerical+
                            "\nbestmove "+bestmove_algeb
                    );
                    
                    engine_text.setStyle("-fx-text-fill: rgb("
                        +color_r+","+color_g+","+color_b
                        +");"
                    );

                    engine_gc.clearRect(0,0,board_size,board_size);

                    //System.out.println("r "+color_r+" g "+color_g+" b "+color_b);

                    engine_gc.setStroke(score_color);
                    engine_gc.setLineWidth(10);
                    engine_gc.strokeRect(0, 0, board_size, board_size);


                    engine_gc.setStroke(engine_color);
                    engine_gc.setLineWidth(3);
                    engine_gc.strokeLine(from_x, from_y, to_x, to_y);
                    engine_gc.setFill(score_color);
                    
                    engine_gc.fillOval(to_x-padding, to_y-padding, 2*padding, 2*padding);
               }
            });
            
        }
        else
        {
            Platform.runLater(new Runnable()
            {
               public void run()
               {
                engine_gc.clearRect(0,0,board_size,board_size);
                engine_text.setText("");
               }
            });
        }
    }
    
    public void consume_engine_out(String uci)
    {
        
        System.out.println("uci out "+uci);
        
        Pattern get_bestmove = Pattern.compile("(bestmove )(.*)");
        Matcher bestmove_matcher = get_bestmove.matcher(uci);
        
        if (bestmove_matcher.find( )) {
           engine_running=false;
           return;
        }
        
        Pattern get_pv = Pattern.compile("( pv )(.{4,})");
        Matcher pv_matcher = get_pv.matcher(uci);
        
        if (pv_matcher.find( )) {
           pv=pv_matcher.group(2);
           String[] pv_parts=pv.split(" ");
           
           bestmove_algeb=pv_parts[0];
        }
        
        Pattern get_depth = Pattern.compile("(depth )([^ ]+)");
        Matcher depth_matcher = get_depth.matcher(uci);
        
        if (depth_matcher.find( )) {
           depth=Integer.parseInt(depth_matcher.group(2));
        }
        
        Pattern get_score_cp = Pattern.compile("(score cp )([^ ]+)");
        Matcher score_cp_matcher = get_score_cp.matcher(uci);
        
        if (score_cp_matcher.find( )) {
           score_cp=Integer.parseInt(score_cp_matcher.group(2));
           score_verbal="cp "+score_cp;
           score_numerical=score_cp;
        }
        
        Pattern get_score_mate = Pattern.compile("(score mate )([^ ]+)");
        Matcher score_mate_matcher = get_score_mate.matcher(uci);
        
        if (score_mate_matcher.find( )) {
           score_mate=Integer.parseInt(score_mate_matcher.group(2));
           score_verbal="mate "+score_mate;
           score_numerical=
                   score_mate<0?
                   -10000-score_mate
                   :
                   10000-score_mate
                   ;
        }
        

        update_engine();
        
    }
    
    private Boolean is_dark_square(int i,int j)
    {
        return((i+j)%2==1);
    }
    
    private char[][] board_to_fonts(char[][] board)
    {
        
       for(int i=0;i<8;i++)
         {
             for(int j=0;j<8;j++)
             {
                 fonts[i][j]=
                         is_dark_square(i,j)?
                         (char)translit_dark.get(board[i][j])
                         :
                         (char)translit_light.get(board[i][j]);
             }
         }
        
         return fonts;
         
    }
    
    private char[][] rep_to_board(String rep)
    {
         
         for(int i=0;i<8;i++)
         {
             for(int j=0;j<8;j++)
             {
                 board[i][j]=rep.charAt(i+j*8);
             }
         }
         
         return board;
         
    }
    
    // convert board coordinates to screen coordinates
    private int gc_x(int i)
    {
        return(margin+(flip?(7-i):i)*(piece_size+padding));
    }
    
    private int gc_y(int j)
    {
        return(margin+((flip?(7-j):j)*(piece_size+padding)));
    }
    
    // convert screen coordinates to board coordinates
    private int gc_i(int x)
    {
        int i=(int)((x-margin)/(piece_size+padding));
        return(flip?(7-i):i);
    }
    
    private int gc_j(int y)
    {
        int j=(int)((y-margin)/(piece_size+padding));
        return(flip?(7-j):j);
    }
    
    private void put_piece_xy(GraphicsContext select_gc,int x,int y,char piece)
    {
        if(select_gc==gc)
        {
            select_gc.setFill(board_color);
            select_gc.fillRect(x, y, piece_size+padding, piece_size+padding );
        }
        select_gc.setFill(piece_color);
        select_gc.setFont(chess_font);
        select_gc.fillText(
                        Character.toString(piece),
                        x,y+piece_size+padding
        );
    }
    
    public void drawBoard() {
        
        board_to_fonts(board);
        
	gc.setFont(chess_font);
        
        gc.setFill(board_color);
        gc.fillRect(0, 0, board_size, board_size);
        
        gc.setFill(Color.rgb(200, 255, 200));
        gc.fillRect(0, board_size, board_size, info_bar_size );
        
        gc.setFill(piece_color);
         
        for(int i=0;i<8;i++)
        {
             for(int j=0;j<8;j++)
             {
                 
                 gc.fillText(Character.toString(fonts[i][j]), gc_x(i),gc_y(j)+piece_size+padding);
                 
             }
        }
        
        gc.setFont(Font.font("Courier New",font_size));
        
        gc.fillText(
                " t: "+(turn==1?"w":"b")+
                ", c: "+castling_rights+
                ", ep: "+ep_square_algeb+
                ", hm: "+halfmove_clock+
                ", fm: "+fullmove_number+
                ", flp: "+(flip?"y":"n")
                ,
                0,board_size+padding+font_size);
        
        gc.strokeRect(0, 0, board_size, board_size);
        
        fen_text.setText(report_fen());
        
        update_engine();
        
    }
    
    private String board_to_rep()
    {
        rep="";
        for(int i=0;i<8;i++)
        {
            for(int j=0;j<8;j++)
            {
               rep+=board[j][i];
            }
        }
        return rep;
    }
    
    private Boolean set_from_fen(String fen)
    {
        return set_from_fen_inner(fen,true);
    }
    
    private Boolean set_from_fen_inner(String fen,Boolean do_reset_game)
    {
        
        rep="";
        
        String[] fen_parts=fen.split(" ");
        
        fen=fen_parts[0];
        for(int i=0;i<fen.length();i++)
        {
            char current=fen.charAt(i);
            if(rep.length()<64)
            {
                if(current=='/')
                {

                }
                else
                {
                    if((current>='1')&&(current<='8'))
                    {
                       for(int j=0;j<Integer.parseInt(""+current);j++)
                       {
                           rep+=" ";
                       }
                    }
                    else
                    {
                        rep+=current;
                    }
                }
            }
            else
            {
                break;
            }
        }
        
        if(rep.length()<64)
        {
            board_to_rep();
            return false;
        }
        
        rep_to_board(rep);
        
        if(fen_parts.length>=2)
        {
            
            String turn_part=fen_parts[1];
            
            if(turn_part.charAt(0)=='w')
            {
                turn=TURN_WHITE;
            }
            else
            {
                turn=TURN_BLACK;
            }
            
        }
        
        if(fen_parts.length>=3)
        {
            String castling_rights_part=fen_parts[2];
            
            castling_rights=castling_rights_part;
        }
        
        
        if(fen_parts.length>=4)
        {
            String ep_square_algeb_part=fen_parts[3];

            ep_square_algeb=ep_square_algeb_part;
        }
        
        if(fen_parts.length>=5)
        {
            String halfmove_clock_part=fen_parts[4];

            halfmove_clock=Integer.parseInt(halfmove_clock_part);
        }
        
        if(fen_parts.length>=6)
        {
            String fullmove_number_part=fen_parts[5];

            fullmove_number=Integer.parseInt(fullmove_number_part);
        }
        
        drawBoard();
        
        if(do_reset_game)
        {
            reset_game();
        }
        
        return true;
    }
    
    private String report_fen()
    {
        
        String fen="";
        
        board_to_rep();
        
        for(int j=0;j<8;j++)
        {
            int empty_cnt=0;
            for(int i=0;i<8;i++)
            {
                int index=i+j*8;
                char current=rep.charAt(index);
                if(current==' ')
                {
                    empty_cnt++;
                }
                else
                {
                    if(empty_cnt>0)
                    {
                        fen+=empty_cnt;
                        empty_cnt=0;
                    }
                    fen+=current;
                }
            }
            if(empty_cnt>0)
            {
                fen+=empty_cnt;
            }
            if(j<7)
            {
                fen+="/";
            }
        }
        
        fen+=
                " "
                +(turn==TURN_WHITE?"w":"b")
                +" "
                +castling_rights
                +" "
                +ep_square_algeb
                +" "
                +halfmove_clock
                +" "
                +fullmove_number;
        
        return(fen);
    }

    public void flip()
    {
        flip=!flip;
        drawBoard();
    }
    
    private void make_move(Move m)
    {
        
        // make move
        m.orig_piece=board[m.i1][m.j1];
        board[m.i1][m.j1]=' ';
        
        char dest_piece=board[m.i2][m.j2];
        
        board[m.i2][m.j2]=m.orig_piece;
        
        // turn
        turn=-turn;
        
        // clear ep
        ep_square_algeb="-";
        
        // promotion
        if( ((m.orig_piece=='P')&&(m.j2==0)) || ((m.orig_piece=='p')&&(m.j2==7)) )
        {
            if(m.prom_piece!=' ')
            {
                if((m.prom_piece>='a')&&(m.prom_piece<='z'))
                {
                    if(m.orig_piece=='P')
                    {
                        m.prom_piece+='A'-'a';
                    }
                }
                else
                {
                    if(m.orig_piece=='p')
                    {
                        m.prom_piece+='a'-'A';
                    }
                }
            }
            else
            {
                Object[] options = {"Queen",
                    "Rook",
                    "Bishop",
                    "Knight"
                };
                
                int n=0;
                n = JOptionPane.showOptionDialog(null,
                    "Select:",
                    "Promote piece",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
                
                switch(n)
                {
                    case 0:m.prom_piece=m.orig_piece=='P'?'Q':'q';break;
                    case 1:m.prom_piece=m.orig_piece=='P'?'R':'r';break;
                    case 2:m.prom_piece=m.orig_piece=='P'?'B':'b';break;
                    case 3:m.prom_piece=m.orig_piece=='P'?'N':'n';break;
                    default:m.prom_piece=' ';
                }
                
            }
                        
            board[m.i2][m.j2]=m.prom_piece;
            
        }
        
        // halfmove clock
        Boolean is_capture=(dest_piece!=' ');
        
        Boolean is_pawn_move=((m.orig_piece=='p')||(m.orig_piece=='P'));
        
        if(is_capture||is_pawn_move)
        {
            halfmove_clock=0;
        }
        else
        {
            halfmove_clock++;
        }
        
        // fullmove number
        if(turn==TURN_WHITE)
        {
            fullmove_number++;
        }
        
        // pawn push by two
        if(
                ((m.orig_piece=='P')&&(m.j1==6)&&(m.j2==4))
                ||
                ((m.orig_piece=='p')&&(m.j1==1)&&(m.j2==3))
        )
        {
            ep_square_algeb=Move.ij_to_algeb(m.i1,m.j1+(m.j2-m.j1)/2);
        }
        
        // castling rights
        
        if(m.orig_piece=='k')
        {
            castling_rights=castling_rights.replace("k","");
            castling_rights=castling_rights.replace("q","");
            
        }
        
        if(m.orig_piece=='K')
        {
            castling_rights=castling_rights.replace("K","");
            castling_rights=castling_rights.replace("Q","");
            
        }
        
        if(board[0][0]==' ')
        {
            castling_rights=castling_rights.replace("q","");
        }
        
        if(board[0][7]==' ')
        {
            castling_rights=castling_rights.replace("Q","");
        }
        
        if(board[7][0]==' ')
        {
            castling_rights=castling_rights.replace("k","");
        }
        
        if(board[7][7]==' ')
        {
            castling_rights=castling_rights.replace("K","");
        }
        
        if(castling_rights.length()<=0)
        {
            castling_rights="-";
        }
        
        // castling
        if((m.j1==0)&&(m.i1==4)&&(m.j2==0)&&(m.i2==6))
        {
            board[7][0]=' ';
            board[5][0]='r';
        }
        
        if((m.j1==0)&&(m.i1==4)&&(m.j2==0)&&(m.i2==2))
        {
            board[0][0]=' ';
            board[3][0]='r';
        }
        
        if((m.j1==7)&&(m.i1==4)&&(m.j2==7)&&(m.i2==6))
        {
            board[7][7]=' ';
            board[5][7]='R';
        }
        
        if((m.j1==7)&&(m.i1==4)&&(m.j2==7)&&(m.i2==2))
        {
            board[0][7]=' ';
            board[3][7]='R';
        }
        
        // ep capture
        if((m.orig_piece=='p')||(m.orig_piece=='P')&&(dest_piece==' ')&&(m.i1!=m.i2))
        {
            board[m.i2][m.j1]=' ';
        }
        
    }
    
    private void make_move_show(Move m)
    {
        
        Boolean restart=false;
        
        if(engine_running)
        {
            restart=true;
            stop_engine();
        }
        
        if(m!=null)
        {
            make_move(m);

            g.add_move(m);
            record_position();
        }
        
        bestmove_algeb="";
        
        drawBoard();
        
        if(restart)
        {
            go_infinite();
        }
    }
    
    private int turn_of_piece(char piece)
    {
        if((piece>='a')&&(piece<='z'))
        {
            return TURN_BLACK;
        }
        return TURN_WHITE;
    }
    
    private EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {
 
        @Override
        public void handle(MouseEvent mouseEvent) {
            
            int x=(int)mouseEvent.getX();
            int y=(int)mouseEvent.getY();
            String type=mouseEvent.getEventType().toString();
            //System.out.println(type + " x " + x + " y " + y);
            //System.out.println("i "+gc_i(x)+" j "+gc_j(y));
            
            if(type=="MOUSE_RELEASED")
            {
                
                if(is_drag_going)
                {
                
                    upper_gc.clearRect(0,0,board_size,board_size);
                    is_drag_going=false;

                    drag_to_i=gc_i(x);
                    drag_to_j=gc_i(y);
                    
                    // same square
                    if((drag_to_i==drag_from_i)&&(drag_to_j==drag_from_j))
                    {
                        drawBoard();
                        return;
                    }
                    
                    // wrong turn
                    if(turn_of_piece(orig_piece)!=turn)
                    {
                        drawBoard();
                        return;
                    }
                    
                    if(
                            (drag_to_i>=0)&&(drag_to_j>=0)&&(drag_to_i<=7)&&(drag_to_j<=7)
                            
                    )
                    {
                    
                        drag_to_x=gc_x(drag_to_i);
                        drag_to_y=gc_y(drag_to_j);
                        
                        makemove.i1=drag_from_i;
                        makemove.j1=drag_from_j;
                        makemove.i2=drag_to_i;
                        makemove.j2=drag_to_j;
                        makemove.prom_piece=' ';
                        
                        make_move_show(makemove);

                    }
                    else
                    {
                        drawBoard();
                        return;
                    }
                
                }
                
            }
            
            if(type=="MOUSE_DRAGGED")
            {
                
                if(is_drag_going)
                {
                    
                    upper_gc.clearRect(0,0,board_size,board_size);
                    
                    put_piece_xy(upper_gc,x+drag_dx,y+drag_dy,drag_piece);
                    
                }
                else
                {
                    is_drag_going=true;
                    drag_from_i=gc_i(x);
                    drag_from_j=gc_j(y);
                    drag_from_x=gc_x(drag_from_i);
                    drag_from_y=gc_y(drag_from_j);
                    drag_dx=drag_from_x-x;
                    drag_dy=drag_from_y-y;
                    orig_drag_piece=fonts[drag_from_i][drag_from_j];
                    orig_piece=board[drag_from_i][drag_from_j];
                    drag_piece=(char)translit_light.get(orig_piece);
                    
                    orig_empty=is_dark_square(drag_from_i,drag_from_j)?'+':' ';
                    
                    put_piece_xy(gc,drag_from_x,drag_from_y,orig_empty);
                    
                }
            }

        }
        
    };
    
    public void reset()
    {
        stop_engine();
        
        rep="rnbqkbnrpppppppp                                PPPPPPPPRNBQKBNR";
         
        rep_to_board(rep);
        
        castling_rights="KQkq";
        
        ep_square_algeb="-";
        
        halfmove_clock=0;
        fullmove_number=1;
        
        turn=TURN_WHITE;
        
        is_drag_going=false;
        
        bestmove_algeb="";
        
        drawBoard();
        
        reset_game();
    }
    
    public void stop_engine_process()
    {
        engine_read_thread.interrupt();
        engine_write_thread.interrupt();
        
        uci_engine_process.destroy();
    }
    
    private void go_infinite()
    {
        String fen=report_fen();
        runnable_engine_write_thread.command=
                "position fen "+fen+"\ngo infinite\n";
        engine_running=true;
    }
    
    private void stop_engine()
    {
        
        if(engine_running)
        {
            runnable_engine_write_thread.command=
                                    "stop\n";

            System.out.println("waiting for engine to stop");

            while(engine_running){
                try {
                            Thread.sleep(100);
                            } catch(InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            }

            System.out.println("engine stopped ok");
        }
            
        }
        
    }
    	
    public Board()
    {
        
        pv="";
        bestmove_algeb="";
        bestmove=new Move();
        depth=0;
        score_mate=0;
        score_cp=0;
        score_verbal="";
        score_numerical=0;
        engine_running=false;
        
        flip=false;
        
        canvas=new Canvas(board_size,board_size+info_bar_size);
        upper_canvas=new Canvas(board_size,board_size);
        engine_canvas=new Canvas(board_size,board_size);
        
        canvas_group.getChildren().add(canvas);
        canvas_group.getChildren().add(engine_canvas);
        canvas_group.getChildren().add(upper_canvas);
        
        Button flip_button=new Button();
        flip_button.setText("Flip");
        flip_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                flip();
            }
        });
        
        Button set_fen_button=new Button();
        set_fen_button.setText("Set Fen");
        set_fen_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                set_from_fen(fen_text.getText());
            }
        });
        
        Button report_fen_button=new Button();
        report_fen_button.setText("Report Fen");
        report_fen_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                drawBoard();
            }
        });
        
        Button reset_button=new Button();
        reset_button.setText("Reset");
        reset_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                reset();
            }
        });
        
        Button delete_button=new Button();
        delete_button.setText("Delete");
        delete_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                set_from_fen_inner(g.delete_move(),false);
                make_move_show(null);
            }
        });
        
        controls_box.getChildren().add(flip_button);
        controls_box.getChildren().add(set_fen_button);
        controls_box.getChildren().add(report_fen_button);
        controls_box.getChildren().add(reset_button);
        controls_box.getChildren().add(delete_button);
        
        if(uci_engine_path!="")
        {
        
            Button stop_engine_process_button=new Button();
            stop_engine_process_button.setText("Stop Engine Process");
            stop_engine_process_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    stop_engine_process();
                }
            });

            Button engine_go_button=new Button();
            engine_go_button.setText("Go");
            engine_go_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    if(!engine_running)
                    {
                        go_infinite();
                    }
                }
            });

            Button engine_stop_button=new Button();
            engine_stop_button.setText("Stop");
            engine_stop_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    stop_engine();
                }
            });
            
            Button engine_make_button=new Button();
            engine_make_button.setText("Make");
            engine_make_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    if((bestmove_algeb!="")&&(bestmove_algeb!=null))
                    {
                        
                        bestmove.from_algeb(bestmove_algeb);
                        make_move_show(bestmove);
                        
                    }
                }
            });
            
            //controls_box.getChildren().add(stop_engine_process_button);
            controls_box.getChildren().add(engine_go_button);
            controls_box.getChildren().add(engine_stop_button);
            controls_box.getChildren().add(engine_make_button);
            
        }
        
        vertical_box.getChildren().add(canvas_group);
        
        vertical_box.getChildren().add(fen_text);
        
        vertical_box.getChildren().add(controls_box);
        
        engine_text.setMaxHeight(100);
        
        if(uci_engine_path!="")
        {
            vertical_box.getChildren().add(engine_text);
        }
        
        upper_canvas.setOnMouseDragged(mouseHandler);
        upper_canvas.setOnMouseClicked(mouseHandler);
        upper_canvas.setOnMouseReleased(mouseHandler);
        
        gc = canvas.getGraphicsContext2D();
        upper_gc = upper_canvas.getGraphicsContext2D();
        engine_gc = engine_canvas.getGraphicsContext2D();
        
        board_color=Color.rgb(255, 220, 220);
        piece_color=Color.rgb(0, 0, 0);
        engine_color=Color.rgb(0, 0, 255);
        
        reset();
        
        if(uci_engine_path!="")
        {
        
            uci_engine_process_builder=new ProcessBuilder(uci_engine_path);

            try {
                   uci_engine_process=uci_engine_process_builder.start();
                   } catch(IOException ex) {

                   }

            engine_in=uci_engine_process.getInputStream();
            engine_out=uci_engine_process.getOutputStream();

            runnable_engine_read_thread=new MyRunnable();
            runnable_engine_read_thread.kind="engine_read";
            runnable_engine_read_thread.std_in=engine_in;
            runnable_engine_read_thread.b=this;
            engine_read_thread=new Thread(runnable_engine_read_thread);

            runnable_engine_write_thread=new MyRunnable();
            runnable_engine_write_thread.kind="engine_write";
            runnable_engine_write_thread.std_out=engine_out;
            runnable_engine_write_thread.command="";
            engine_write_thread=new Thread(runnable_engine_write_thread);

            engine_read_thread.start();
            engine_write_thread.start();
            
        }
        
    }
    
}
