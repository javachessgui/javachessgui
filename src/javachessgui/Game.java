package javachessgui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import javafx.stage.*;

import javafx.stage.FileChooser;

import java.io.File;
import java.util.Arrays;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

public class Game {
    
        ListView<String> list = new ListView<String>();
    
        public VBox vertical_box=new VBox(2);
        FileChooser f=new FileChooser();
        
        private Stage s=new Stage();
        
        private Board b;
        
        private TextArea game_text = new TextArea ();
        
        final private int max_moves=250;
        
        private String[] moves=new String[max_moves];
        public String initial_position;
        private String[] positions=new String[max_moves];
        
        private int move_ptr=0;
        
        private int game_ptr=0;
        
        public void reset(String initial_fen)
        {
            
            move_ptr=0;
            
            initial_position=initial_fen;
            
            update_game();
            
        }
        
        public void add_move(String san,String fen_after)
        {
            
            if(game_ptr<move_ptr)
            {
                game_ptr=move_ptr;
            }
            
            if(move_ptr>=max_moves)
            {
                
            }
            else
            {
                
                positions[move_ptr]=fen_after;
                moves[move_ptr++]=san;
                game_ptr++;
                
            }
            
            update_game();
            
        }
        
        public String to_begin()
        {
            game_ptr=0;
            update_game();
            return initial_position;
        }
        
        public String back()
        {
            
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
            
            if(game_ptr<move_ptr)
            {
                move_ptr=game_ptr;
            }
            
            if(move_ptr==0)
            {
                // nothing to delete
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
        
        private void update_game()
        {
            
            String[] game_buffer=new String[max_moves+1];
            
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
            
            list.setOnMouseClicked(new EventHandler<Event>() {

                        @Override
                        public void handle(Event event) {
                            
                            int selected =  list.getSelectionModel().getSelectedIndex();

                            String pos=initial_position;
                            if(selected>0){pos=positions[selected-1];}
                            
                            game_ptr=selected;
                            
                            b.set_from_fen_inner(pos,false);
                            b.drawBoard();

                    }

                });
            
        }
        
        public Game(Stage set_s,Board set_b)
        {
            
            s=set_s;
            b=set_b;
            
            Button open_pgn_button=new Button();
            open_pgn_button.setText("Open PGN");
            
            open_pgn_button.setOnAction(new EventHandler<ActionEvent>() {
                    
                @Override public void handle(ActionEvent e) {
                     File file = f.showOpenDialog(s);
                     b.reset();
                    }
                
            });
            
            vertical_box.getChildren().add(open_pgn_button);
            
            list.setMaxWidth(120);
            
            vertical_box.getChildren().add(list);
            
            
        }
    
}
