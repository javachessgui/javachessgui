package javachessgui;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Group;

public class MyModal {
    
    private Stage modal_stage;
    
    public void close()
    {
        modal_stage.close();
    }
    
    public void show_and_wait()
    {
        modal_stage.showAndWait();
    }
    
    public void setxy(int x,int y)
    {
        modal_stage.setX(x);
        modal_stage.setY(y);
    }
    
    public MyModal(Group modal_group,String title)
    {
        
        modal_stage=new Stage();
        
        Scene modal_scene=new Scene(modal_group);

        modal_stage.initModality(Modality.APPLICATION_MODAL);

        modal_stage.setTitle(title);
        modal_stage.setScene(modal_scene);
        
    }
    
}
