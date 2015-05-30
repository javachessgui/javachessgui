package javachessgui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import javafx.stage.*;

import javafx.stage.FileChooser;

import java.io.File;
import javafx.scene.control.TextArea;

public class Game {
    
        public VBox vertical_box=new VBox(2);
        FileChooser f=new FileChooser();
        
        private Stage s=new Stage();
        
        private Board b;
        
        private TextArea game_text = new TextArea ();
        
        final private int max_moves=5000;
        
        private Move[] moves=new Move[max_moves];
        
        private int move_ptr=0;
        
        public void reset()
        {
            move_ptr=0;
            
            update_game();
        }
        
        public void add_move(Move m)
        {
            if(move_ptr>=max_moves)
            {
                
            }
            else
            {
                Move copy=new Move();
                copy.copy(m);
                moves[move_ptr++]=copy;
            }
            
            update_game();
        }
        
        private void update_game()
        {
            String game_buffer="";
            
            if(move_ptr>0)
            {
                for(int i=0;i<move_ptr;i++)
                {
                    game_buffer+=moves[i].to_algeb()+" ";
                }
            }
            else
            {
                game_buffer="*";
            }
            
            game_text.setText(game_buffer);
            
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
            vertical_box.getChildren().add(game_text);
            
            reset();
            
        }
    
}
