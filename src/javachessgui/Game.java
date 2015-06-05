package javachessgui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import javafx.stage.*;

import javafx.stage.FileChooser;

import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.Set;

import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;   
import javafx.scene.Group;
import javafx.scene.Scene;

import javafx.scene.control.cell.ComboBoxListCell;

import javafx.scene.control.ListCell;
import javafx.util.Callback;
import javafx.scene.paint.Color;

class AnnotationFormatCell extends ListCell<String> {
    
    public static Color get_color(String item)
    {
        
        item=" "+item+" ";
        
        if(item.contains(" !! "))
        {
             return(Color.GREEN);
        }
        else if(item.contains(" ! "))
        {
             return(Color.DARKGREEN);
        }
        else if(item.contains(" ?? "))
        {
             return(Color.RED);
        }
        else if(item.contains(" ? "))
        {
             return(Color.DARKRED);
        }
        else if(item.contains(" !? "))
        {
             return(Color.DARKBLUE);
        }
        else if(item.contains(" ?! "))
        {
             return(Color.LIGHTBLUE);
        }
        else if(item.contains(" - "))
        {
             return(Color.BLACK);
        }
        
        return(Color.GRAY);
         
    }

     public AnnotationFormatCell() {    }
       
     @Override protected void updateItem(String item, boolean empty) {
         // calling super here is very important - don't skip this!
         super.updateItem(item, empty);
         
         setText(item);
         
         if(item==null)
         {
             return;
         }
         
         Color c=get_color(item);
         
         if(!c.equals(Color.GRAY))
         {
             setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
         }
         else
         {
             setStyle("-fx-font-size: 20px;");
         }
         
         setTextFill(c);
         
         
         }
     }


class BookMoveComparator implements Comparator<BookMove>
{
    public int compare(BookMove b1, BookMove b2)
    {
        return b2.notation-b1.notation;
    }
}

class BookMove
{
    
    
    
    public String san;
    public int notation;
    public Boolean is_analyzed;
    public int eval;
    public int count;
    
    public Hashtable report_hash()
    {
        Hashtable hash=new Hashtable();
        hash.put("notation", ""+notation);
        hash.put("is_analyzed", ""+is_analyzed);
        hash.put("eval", ""+eval);
        hash.put("count", ""+count);
        
        return hash;
    }
    
    public void set_from_hash(Hashtable hash)
    {
        if(hash.get("notation")!=null)
        {
            notation=Integer.parseInt(hash.get("notation").toString());
        }
        
        if(hash.get("is_analyzed")!=null)
        {
            is_analyzed=hash.get("is_analyzed").toString().equals("true");
        }
        
        if(hash.get("eval")!=null)
        {
            eval=Integer.parseInt(hash.get("eval").toString());
        }
        
        if(hash.get("count")!=null)
        {
            count=Integer.parseInt(hash.get("count").toString());
        }
        
    }
    
    public BookMove(String set_san)
    {
        san=set_san;
        notation=-1;
        is_analyzed=false;
        eval=0;
        count=0;
    }
}

public class Game {
    
           
        List<BookMove> book_list;
    
        private Hashtable pgn_header_hash=new Hashtable();
            
        public HBox clip_box=new HBox(2);
        public HBox book_box=new HBox(2);
        public HBox save_pgn_box=new HBox(2);
    
        private TextField pgn_name_text = new TextField();
    
        private String pgn;
    
        String initial_dir="";
    
        ListView<String> list = new ListView<String>();
        ListView blist = new ListView<String>();
        
        TextArea pgn_text=new TextArea();
    
        public VBox vertical_box=new VBox(2);
        FileChooser f=new FileChooser();
        
        private Stage s=new Stage();
        
        private Board b;
        
        private TextArea game_text = new TextArea ();
        
        final private int MAX_MOVES=250;
        
        String[] pgn_lines=new String[MAX_MOVES];
        
        private String[] moves=new String[MAX_MOVES];
        public String initial_position;
        private String[] positions=new String[MAX_MOVES];
        
        int move_indices[]=new int[MAX_MOVES];
        int start_fen_end_index=0;
        
        private int move_ptr=0;
        
        private int game_ptr=0;
        
        public void reset(String initial_fen)
        {
            
            sel_book_move=-1;
            
            move_ptr=0;
            
            game_ptr=0;
            
            initial_position=initial_fen;
            
            update_game();
            
        }
        
        private String fen_to_name(String fen)
        {
            return "book\\"+Encode32.encode(fen, true)+".txt";
        }
        
        private Hashtable get_pos(String fen)
        {
            
            Object pos_obj=book.get(fen);
            
            if(pos_obj==null)
            {
                
                String name=fen_to_name(fen);
                
                File f = new File(name);
                
                if(f.exists())
                {
                    
                    MyFile look_up=new MyFile(name);
                    Hashtable pos=look_up.to_hash();
                    
                    book.put(fen,pos);
                    
                    return pos;
                    
                }
                else
                {
                    
                    return new Hashtable();
                    
                }
                
            }
            else
            {
                
                return (Hashtable)pos_obj;
                
            }
            
        }
        
        private void store_pos(String fen,Hashtable hash)
        {
            String name=fen_to_name(fen);
            
            MyFile pos_file=new MyFile(name);
            
            pos_file.from_hash(hash);
        }
        
        public void add_move(String san,String fen_after)
        {
            
            sel_book_move=-1;
            
            if(game_ptr<move_ptr)
            {
                move_ptr=game_ptr;
            }
            
            if(move_ptr>=MAX_MOVES)
            {
                
            }
            else
            {
                
                String fen_before=initial_position;
                if(move_ptr>0)
                {
                    fen_before=positions[move_ptr-1];
                }
                positions[move_ptr]=fen_after;
                moves[move_ptr++]=san;
                game_ptr++;
                
                Hashtable pos=get_pos(fen_before);
                                
                if(pos.get(san)==null)
                {
                    BookMove new_book_move=new BookMove(san);
                    new_book_move.count=1;
                    pos.put(san,new_book_move.report_hash());
                }
                else
                {
                    BookMove old_book_move=new BookMove(san);
                    old_book_move.set_from_hash((Hashtable)pos.get(san));
                    old_book_move.count++;
                    pos.put(san,old_book_move.report_hash());
                }
                
                store_pos(fen_before,pos);
                
                //book_file.from_hash(book);
                
            }
            
            update_game();
            
        }
        
        public String to_begin()
        {
            
            sel_book_move=-1;
            
            game_ptr=0;
            update_game();
            return initial_position;
        }
        
        public String back()
        {
            
            sel_book_move=-1;
            
            if(game_ptr==0)
            {
                return initial_position;
            }
            else
            {
                game_ptr--;
                update_game();
                if(game_ptr==0)
                {
                    return initial_position;
                }
                return positions[game_ptr-1];
                
            }
            
        }
        
        public String forward()
        {
            
            sel_book_move=-1;
            
            if(game_ptr==move_ptr)
            {
                if(game_ptr==0)
                {
                    return initial_position;
                }
                return positions[game_ptr-1];
            }
            else
            {
                game_ptr++;
                update_game();
                return positions[game_ptr-1];
                
            }
            
        }
        
        public String to_end()
        {
            
            sel_book_move=-1;
            
            game_ptr=move_ptr;
            update_game();
            if(move_ptr==0)
            {
                return initial_position;
            }
            return positions[move_ptr-1];
        }
        
        public String delete_move()
        {
            
            sel_book_move=-1;
            
            if(game_ptr<move_ptr)
            {
                move_ptr=game_ptr;
            }
            
            if(move_ptr==0)
            {
                // nothing to delete
                update_game();
                return(initial_position);
            }
            else
            {
                if(move_ptr>0)
                {
                    move_ptr--;
                    game_ptr--;
                }
                
                update_game();
                
                if(move_ptr==0)
                {
                    return initial_position;
                }
                
                return(positions[move_ptr-1]);
            }
        }
        
        
        private int no_book_moves;
        
        public void update_book()
        {
            
            String fen=initial_position;
            if(game_ptr>0)
            {
                fen=positions[game_ptr-1];
            }
            
            Hashtable book_moves=get_pos(fen);
            
            no_book_moves=0;
            
            book_list=new ArrayList<BookMove>();
            
            Set<String> keys = book_moves.keySet();

            for(String key: keys)
            {

                Hashtable value=(Hashtable)(book_moves.get(key));

                BookMove book_move=new BookMove(key);

                book_move.set_from_hash(value);

                book_list.add(book_move);

            }

            // sort book list

            book_list.sort(new BookMoveComparator());

            no_book_moves=book_list.size();

            String[] temp_list=new String[200];
            int temp_ptr=0;

            for (BookMove temp : book_list) {

                String notation_as_string="N/A";
                if(temp.notation>=0)
                {
                    notation_as_string=notation_list[temp.notation];
                }
                
                String eval="_";
                if(temp.is_analyzed)
                {
                    eval=""+temp.eval;
                }
                String book_line=String.format("%-10s %-4s %5d %8s",temp.san,notation_as_string,temp.count,eval);
                temp_list[temp_ptr++]=book_line;
            }

            ObservableList<String> items =FXCollections.observableArrayList(
            Arrays.copyOfRange(temp_list, 0, no_book_moves)
            );

            blist.setItems(items);

            if(sel_book_move>=0)
            {
                //blist.getSelectionModel().select(sel_book_move);
                blist.getSelectionModel().select(null);
            }
            else
            {
                blist.getSelectionModel().select(null);
            }
            
        }
        
        public void update_game()
        {
            
            update_book();
            
            String[] game_buffer=new String[MAX_MOVES+1];
            
            game_buffer[0]="*";
            
            for(int i=0;i<move_ptr;i++)
            {
                game_buffer[i+1]=moves[i];
            }

            //game_text.setText(game_buffer);
            
            ObservableList<String> items =FXCollections.observableArrayList(
                Arrays.copyOfRange(game_buffer, 0, move_ptr+1)
            );
        
            list.setItems(items);
            
            list.getSelectionModel().select(game_ptr);
            list.scrollTo(game_ptr);
            
            pgn_text.setText(calc_pgn());
            
            if(game_ptr>0)
            {
                
                pgn_text.positionCaret(move_indices[game_ptr-1]);
                pgn_text.selectNextWord();

            }
            else
            {
                pgn_text.selectRange(0, start_fen_end_index);
            }
                        
        }
        
        public String calc_pgn()
        {
            Board dummy=new Board(false);
            
            dummy.set_from_fen(initial_position);
            
            int fullmove_number=dummy.fullmove_number;
            int turn=dummy.turn;
            
            pgn="[StartFen \""+initial_position+"\"]\n";
            start_fen_end_index=pgn.length()-1;
            pgn+="[Flip \""+b.flip+"\"]\n";
            
            // add hash headers
            
            Set<String> keys = pgn_header_hash.keySet();
            for(String key: keys)
            {
                String value=pgn_header_hash.get(key).toString();
                if((!key.equals("StartFen"))&&(!key.equals("Flip")))
                {
                    pgn+="["+key+" \""+value+"\"]\n";
                }
            }
            
            pgn+="\n";
            
            if(move_ptr>0)
            {
                pgn+=fullmove_number+". ";
                
                if(turn==Board.TURN_BLACK)
                {
                    pgn+="... ";
                }
                
                move_indices[0]=pgn.length();
                
                pgn+=moves[0]+" ";
            }
            
            for(int i=1;i<move_ptr;i++)
            {
                dummy.set_from_fen(positions[i-1]);
                turn=dummy.turn;
                if(dummy.turn==Board.TURN_WHITE)
                {
                    fullmove_number++;
                    pgn+=fullmove_number+". ";
                }
                move_indices[i]=pgn.length();
                pgn+=moves[i]+" ";
            }
            
            return pgn;
            
        }
        
        private void set_from_pgn_lines()
        {
            
            move_ptr=0;
            
            pgn_header_hash.clear();
            
            initial_position="rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
            
            int line_cnt=0;
            
            // read headers
            int empty_cnt=0;
            
            Boolean finished=false;
            
            do
            {
                String line=pgn_lines[line_cnt++];
                
                if(line_cnt<pgn_lines.length)
                {
                    if(line.length()<2)
                    {
                        finished=true;
                    }
                    else
                    {
                        if(line.charAt(0)!='[')
                        {
                            finished=true;
                        }
                        else
                        {
                            //System.out.println("header "+line);
                            Pattern get_flip = Pattern.compile("(Flip .true)");
                            Matcher flip_matcher = get_flip.matcher(line);
            
                            if(flip_matcher.find())
                            {
                                b.flip=true;
                            }
                            else
                            {
                                b.flip=false;
                            }
                                                        
                            // parse header fields
                            
                            Pattern get_header = Pattern.compile("\\[([^ ]+) \"([^\\\"]+)\\\"");
                            Matcher header_matcher = get_header.matcher(line);
                            
                            if(header_matcher.find())
                            {
                                String key=header_matcher.group(1);
                                String value=header_matcher.group(2);
                                //System.out.println("key "+key+" value "+value);
                                
                                pgn_header_hash.put(key,value);
                            }
                            
                        }
                    }
                }
                else
                {
                    finished=true;
                }
                
            }while(!finished);
            
            String body="";
            while(line_cnt<pgn_lines.length)
            {
                String line=pgn_lines[line_cnt++];
                if(line.length()<2)
                {
                    break;
                }
                body+=line+" ";
            }
            
            // remove all comments, carriage return, line feed
            body=body.replaceAll("\r|\n|\\{[^\\}]*\\}","");
            
            //System.out.println("body: "+body);
            
            MyTokenizer t=new MyTokenizer(body);
            
            String token;
            
            b.reset();
            
            while((token=t.get_token())!=null)
            {
                //System.out.println("token: "+token);
                
                if(b.is_san_move_legal(token))
                {
                    b.make_san_move(token, false);
                    String fen_after=b.report_fen();
                    add_move(token,fen_after);
                    
                    //System.out.println("san: "+token+" "+fen_after);
                }
                
            }
            
            game_ptr=move_ptr;
            
            b.drawBoard();
            
            update_game();
            
        }
        
        private EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {
 
            @Override
            public void handle(MouseEvent mouseEvent) {

                int x=(int)mouseEvent.getX();
                int y=(int)mouseEvent.getY();
                
                String type=mouseEvent.getEventType().toString();

                if(type.equals("MOUSE_CLICKED"))
                {
                    //System.out.println("Mouse clicked over pgn text at x="+x+" y="+y);
                    
                    //System.out.println("caret position: "+pgn_text.getCaretPosition());
                    
                    int click_index=pgn_text.getCaretPosition();
                            
                    for(int i=0;i<move_ptr;i++)
                    {
                        int move_index=move_indices[i];
                        if(click_index<move_index)
                        {
                            game_ptr=i;
                            
                            String pos=initial_position;
                            if(game_ptr>0){pos=positions[game_ptr-1];}
                                                        
                            b.set_from_fen_inner(pos,false);
                            b.drawBoard();
                            
                            update_game();
                            
                            return;
                        }
                    }
                    
                    if(move_ptr>0)
                    {
                        game_ptr=move_ptr;
                            
                        String pos=positions[game_ptr-1];

                        b.set_from_fen_inner(pos,false);
                        b.drawBoard();

                        update_game();
                    }
                }

            }
        
        };
        
        private int sel_book_move=-1;
        
        private Stage select_annotation_stage;
        int selected_notation;
        
        private EventHandler<MouseEvent> mouseHandlerBook = new EventHandler<MouseEvent>() {
 
            @Override
            public void handle(MouseEvent mouseEvent) {
                
                int x=(int)mouseEvent.getX();
                int y=(int)mouseEvent.getY();
                
                String type=mouseEvent.getEventType().toString();

                if(type.equals("MOUSE_CLICKED"))
                {
                    //System.out.println("Mouse clicked over pgn text at x="+x+" y="+y);
                    
                    //System.out.println("caret position: "+pgn_text.getCaretPosition());
                    
                    int j=blist.getSelectionModel().getSelectedIndex();
                    
                    sel_book_move=j;
                    
                    int size=book_list.size();
                    if((j>=0)&&(j<size))
                    {
                        String san=book_list.get(j).san;
                        if(x<50)
                        {
                            b.make_san_move(san,true);
                        }
                        else
                        {
                            String fen_before=initial_position;
                            if(move_ptr>0)
                            {
                                fen_before=positions[game_ptr-1];
                            }

                            Hashtable pos=get_pos(fen_before);
                            
                            if(pos.get(san)==null)
                            {
                                BookMove new_book_move=new BookMove(san);
                                new_book_move.count=1;
                                pos.put(san,new_book_move.report_hash());
                            }
                            else
                            {
                                BookMove old_book_move=new BookMove(san);
                                old_book_move.set_from_hash((Hashtable)pos.get(san));
                                
                                // obtain new notation
                                
                                Group select_engine_group=new Group();
        
                                ListView<String> list = new ListView<String>();

                                list.setStyle("-fx-font-family: monospace;");
                                
                                list.setMinWidth(280);
                                list.setMaxWidth(280);
                                list.setMinHeight(260);
                                list.setMaxHeight(260);
                                
                                
                                String[] notation_list={"!!  winning","!   strong","!?  promising","-   stable","?!  interesting","?   bad","??  losing"};
                                ObservableList<String> items =FXCollections.observableArrayList(
                                        notation_list
                                    );

                                list.setItems(items);
                                list.setCellFactory(new Callback<ListView<String>, ListCell<String>>()
                                {
                                    @Override public ListCell<String> call(ListView<String> list) {
                                        return new AnnotationFormatCell();
                                    }
                                });

                                select_engine_group.getChildren().add(list);

                                Scene select_annotation_scene=new Scene(select_engine_group);

                                select_annotation_stage=new Stage();

                                select_annotation_stage.initModality(Modality.APPLICATION_MODAL);
                                
                                select_annotation_stage.setTitle("Select");
                                select_annotation_stage.setScene(select_annotation_scene);

                                list.setOnMouseClicked(new EventHandler<Event>() {

                                        @Override
                                        public void handle(Event event) {

                                            selected_notation =  notation_list.length-1-list.getSelectionModel().getSelectedIndex();

                                            select_annotation_stage.close();
                                    }

                                });
                                
                                selected_notation=old_book_move.notation;

                                select_annotation_stage.showAndWait();
                                
                                // end obtain new notation
                                
                                old_book_move.notation=selected_notation;
                                pos.put(san,old_book_move.report_hash());
                                
                                store_pos(fen_before,pos);
                                
                            }

                            //book_file.from_hash(book);
                            
                            update_book();
                        }
                    }
                            
                }

            }
        
        };
        
        private void look_for_initial_dir()
        {
            if(initial_dir.equals(""))
            {
                MyFile config=new MyFile("config.txt");
                String result=config.get_field("initial_dir");
                if(result!=null)
                {
                    initial_dir=result;
                }
            }
        }
        
        private Clipboard clip=Clipboard.getSystemClipboard();
        
        private Hashtable book;
        
        private MyFile book_file;
        final private String[] notation_list={"??","?","?!","-","!?","!","!!"};
        
        public void record_eval(String fen,String san,int eval)
        {
            
            Hashtable pos=get_pos(fen);
                                
            if(pos.get(san)==null)
            {
                BookMove new_book_move=new BookMove(san);
                new_book_move.count=1;
                new_book_move.is_analyzed=true;
                new_book_move.eval=eval;
                pos.put(san,new_book_move.report_hash());
            }
            else
            {
                BookMove old_book_move=new BookMove(san);
                old_book_move.set_from_hash((Hashtable)pos.get(san));
                old_book_move.is_analyzed=true;
                old_book_move.eval=eval;
                pos.put(san,old_book_move.report_hash());
            }

            store_pos(fen,pos);
            
            update_book();
            
        }
        
        public Game(Stage set_s,Board set_b)
        {
            
            book_file=new MyFile("book.txt");
            
            book=new Hashtable();
            //book=book_file.to_hash();
            
            s=set_s;
            b=set_b;
            
            Button open_pgn_button=new Button();
            open_pgn_button.setText("Open PGN");
            
            open_pgn_button.setOnAction(new EventHandler<ActionEvent>() {
                    
                @Override public void handle(ActionEvent e) {
                    
                    look_for_initial_dir();
                    if(initial_dir!="")
                    {
                        File dir=new File(initial_dir);

                        f.setInitialDirectory(dir);
                    }
                                        
                    File file = f.showOpenDialog(s);
                    
                    if(file==null){return;}
                    
                    String path=file.getPath();
                    
                    pgn_name_text.setText(path);

                    initial_dir=path.substring(0,path.lastIndexOf(File.separator));

                    MyFile config=new MyFile("config.txt");
                    config.set_field("initial_dir",initial_dir);

                    MyFile my_file=new MyFile(path);

                    pgn_lines=my_file.read_lines();

                    set_from_pgn_lines();
                     
                    }
                
            });
            
            
            Button save_as_pgn_button=new Button();
            save_as_pgn_button.setText("Save as: ");
            
            save_as_pgn_button.setOnAction(new EventHandler<ActionEvent>() {
                    
                @Override public void handle(ActionEvent e) {
                    
                    String path=pgn_name_text.getText();
                    
                    if(path.length()>0)
                    {
                     
                        MyFile my_file=new MyFile(path);

                        calc_pgn();

                        my_file.content=pgn;

                        my_file.write_content();
                     
                    }
                }
                
            });
            
            Button save_to_pgn_button=new Button();
            save_to_pgn_button.setText("Save to PGN");
            
            save_to_pgn_button.setOnAction(new EventHandler<ActionEvent>() {
                    
                @Override public void handle(ActionEvent e) {
                    
                    look_for_initial_dir();
                    if(initial_dir!="")
                    {
                        File dir=new File(initial_dir);

                        f.setInitialDirectory(dir);
                    }
                                        
                     File file = f.showOpenDialog(s);
                     
                     if(file==null){return;}
                     
                     String path=file.getPath();
                     
                     initial_dir=path.substring(0,path.lastIndexOf(File.separator));
                     
                     MyFile my_file=new MyFile(path);
                     
                     calc_pgn();
                     
                     my_file.content=pgn;
                     
                     my_file.write_content();
                     
                    }
                
            });
            
            clip_box.getChildren().add(open_pgn_button);
            
            Button clip_to_fen_button=new Button();
            clip_to_fen_button.setText("Clip->Fen");
            clip_to_fen_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    String fen=clip.getString();
                    if(fen!=null)
                    {
                        b.set_from_fen(fen);
                        b.drawBoard();
                    }
                }
            });
            
            Button fen_to_clip_button=new Button();
            fen_to_clip_button.setText("Fen->Clip");
            fen_to_clip_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    ClipboardContent content = new ClipboardContent();
                    content.putString(b.report_fen());
                    clip.setContent(content);
                }
            });
            
            Button clip_to_pgn_button=new Button();
            clip_to_pgn_button.setText("Clip->PGN");
            clip_to_pgn_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    String pgn=clip.getString();
                    if(pgn!=null)
                    {
                        pgn_lines = pgn.split("\\r?\\n");
                        set_from_pgn_lines();
                    }
                }
            });
            
            Button pgn_to_clip_button=new Button();
            pgn_to_clip_button.setText("PGN->Clip");
            pgn_to_clip_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    ClipboardContent content = new ClipboardContent();
                    content.putString(calc_pgn());
                    clip.setContent(content);
                }
            });
            
            Button load_book_button=new Button();
            load_book_button.setText("Load Book");
            load_book_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    book=book_file.to_hash();
                    update_game();
                }
            });
            
            Button save_book_button=new Button();
            save_book_button.setText("Save Book");
            save_book_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    book_file.from_hash(book);
                }
            });
            
            clip_box.getChildren().add(clip_to_fen_button);
            clip_box.getChildren().add(fen_to_clip_button);
            clip_box.getChildren().add(clip_to_pgn_button);
            clip_box.getChildren().add(pgn_to_clip_button);
            clip_box.getChildren().add(load_book_button);
            clip_box.getChildren().add(save_book_button);
            
            vertical_box.getChildren().add(clip_box);
            
            list.setMaxWidth(120);
            
            book_box.getChildren().add(list);
                     
            blist.setMinWidth(400);
            blist.setStyle("-fx-font-family: monospace;");
            
            blist.setCellFactory(new Callback<ListView<String>, ListCell<String>>()
            {
                @Override public ListCell<String> call(ListView<String> list) {
                    return new AnnotationFormatCell();
                }
            });
                        
            book_box.getChildren().add(blist);
            
            vertical_box.getChildren().add(book_box);
            
            save_pgn_box.getChildren().add(save_as_pgn_button);
            pgn_name_text.setMaxWidth(300);
            save_pgn_box.getChildren().add(pgn_name_text);
            save_pgn_box.getChildren().add(save_to_pgn_button);
            
            vertical_box.getChildren().add(save_pgn_box);
            
            pgn_text.setWrapText(true);
            
            pgn_text.setStyle("-fx-display-caret: false;");
            
            pgn_text.setOnMouseClicked(mouseHandler);
            
            blist.setOnMouseClicked(mouseHandlerBook);
            
            vertical_box.getChildren().add(pgn_text);
            
            list.setOnMouseClicked(new EventHandler<Event>() {

                        @Override
                        public void handle(Event event) {
                            
                            int selected =  list.getSelectionModel().getSelectedIndex();

                            String pos=initial_position;
                            if(selected>0){pos=positions[selected-1];}
                            
                            game_ptr=selected;
                            
                            b.set_from_fen_inner(pos,false);
                            b.drawBoard();
                            
                            update_game();

                    }

                });
            
            
        }
    
}
