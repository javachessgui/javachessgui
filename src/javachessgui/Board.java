package javachessgui;

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

public class Board {
    
    ////////////////////////////////////////////////////////
    // static members
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
    
    private GraphicsContext gc;
    private GraphicsContext upper_gc;
    
    private static int padding;
    private static int piece_size;
    private static int margin;
    private static int board_size;
    private static int info_bar_size;
    private static int font_size;
    
    private Color board_color;
    private Color piece_color;
    ////////////////////////////////////////////////////////
	
    
    
    public static void init_class()
    {
        
        piece_size=45;
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
       
        //System.out.println("board class init complete");
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
                ", hm: "+String.valueOf(halfmove_clock)+
                ", fm: "+String.valueOf(fullmove_number)+
                ", flp: "+(flip?"y":"n")
                ,
                0,board_size+padding+font_size);
        
        gc.strokeRect(0, 0, board_size, board_size);
        
        fen_text.setText(report_fen());
        
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
        
        drawBoard();
        
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
                +ep_square_algeb;
        
        return(fen);
    }

    public void flip()
    {
        flip=!flip;
        drawBoard();
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
                    
                    if((drag_to_i>=0)&&(drag_to_j>=0)&&(drag_to_i<=7)&&(drag_to_j<=7))
                    {
                    
                        drag_to_x=gc_x(drag_to_i);
                        drag_to_y=gc_y(drag_to_j);

                        board[drag_from_i][drag_from_j]=' ';

                        board[drag_to_i][drag_to_j]=orig_piece;
                        
                        turn=-turn;

                    }
                    else
                    {
                        //board[drag_from_i][drag_from_j]=orig_piece;
                    }
                    
                    drawBoard();
                
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
    
    private void reset()
    {
        rep="rnbqkbnrpppppppp                                PPPPPPPPRNBQKBNR";
         
        rep_to_board(rep);
        
        castling_rights="KQkq";
        
        ep_square_algeb="-";
        
        halfmove_clock=0;
        fullmove_number=1;
        
        turn=TURN_WHITE;
        
        is_drag_going=false;
    }
    
    public void stop_engine_process()
    {
        engine_read_thread.interrupt();
        engine_write_thread.interrupt();
        
        uci_engine_process.destroy();
    }
    	
    public Board()
    {
        
        
        flip=false;
        
        canvas=new Canvas(board_size,board_size+info_bar_size);
        upper_canvas=new Canvas(board_size,board_size);
        
        canvas_group.getChildren().add(canvas);
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
                String fen=report_fen();
                runnable_engine_write_thread.command=
                        "position fen "+fen+"\ngo infinite\n";
            }
        });
        
        Button engine_stop_button=new Button();
        engine_stop_button.setText("Stop");
        engine_stop_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                runnable_engine_write_thread.command="stop\n";
            }
        });
        
        controls_box.getChildren().add(flip_button);
        controls_box.getChildren().add(set_fen_button);
        controls_box.getChildren().add(report_fen_button);
        controls_box.getChildren().add(stop_engine_process_button);
        controls_box.getChildren().add(engine_go_button);
        controls_box.getChildren().add(engine_stop_button);
        
        vertical_box.getChildren().add(canvas_group);
        
        vertical_box.getChildren().add(fen_text);
        
        vertical_box.getChildren().add(controls_box);
        
        engine_text.setMaxHeight(100);
        
        vertical_box.getChildren().add(engine_text);
        
        upper_canvas.setOnMouseDragged(mouseHandler);
        upper_canvas.setOnMouseClicked(mouseHandler);
        upper_canvas.setOnMouseReleased(mouseHandler);
        
        gc = canvas.getGraphicsContext2D();
        upper_gc = upper_canvas.getGraphicsContext2D();
        
        reset();
        
        board_color=Color.rgb(255, 220, 220);
        piece_color=Color.rgb(0, 0, 0);
        
        drawBoard();
        
        uci_engine_process_builder=new ProcessBuilder("c:\\unzip\\chessgui\\uciengine.exe");
        
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
