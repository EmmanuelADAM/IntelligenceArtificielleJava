package colordbscan;
	

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.animation.Timeline;

import javafx.animation.KeyFrame;
import javafx.util.Duration;


public class AppliDBScaneCouleurs extends Application {
	   /**taille d'une donnée*/
	   private int espace = 4;
	   /**taille d'un coté*/
	   private static int taille;
	   /**délai en ms entre chaque évolution*/
	   private static final  int tempo = 1000;
	   /**les couleurs utilisées*/
	   static Color[] colors = {Color.BLACK,
			   Color.rgb(150, 0, 0), Color.rgb(0, 150, 0), Color.rgb(0, 0, 150), 
			   Color.rgb(150, 150, 0), Color.rgb(0, 150, 150), Color.rgb(150, 0, 150), 
			   Color.rgb(50, 150, 0), Color.rgb(0, 50, 150), Color.rgb(150, 0, 50)};
	   /***/
	   static Timeline littleCycle;
	   
	   @Override
	   public void start(Stage primaryStage) {
		   AppliDBScaneCouleurs.taille = 700;
	      construireScene( primaryStage);
	   }
	   
	   private void construireScene(Stage primaryStage)
	   {	
	      int nbData = 3000;
//	      AlgoDBScan2D.createSamples(nbData, taille, taille);
//	      AlgoDBScan2D.createSamplesConcentricWithNoise(nbData, taille/2, 3, 0.1);
	      AlgoDBScan2D.createSamplesConcentric(nbData, taille/2, 4);
	      AlgoDBScan2D.initialize();
		  // definir la scene principale
		  Group root = new Group();
	      Scene scene = new Scene(root, taille, taille, Color.WHITE);
	      primaryStage.setTitle("DBScan...");
	      primaryStage.setScene(scene);
	      //habiller les acteurs
	      espace = 4;
	      dessinData( root);
	      //afficher le theatre
	      primaryStage.show();
	      //-----lancer le timer pour faire vivre la matrice
	      littleCycle = new Timeline(new KeyFrame(Duration.millis(tempo), 
	         event-> {
	        	 if(AlgoDBScan2D.isMoving)
	        	 {
	        		 AlgoDBScan2D.dbScan();
	        		 AlgoDBScan2D.updateColor();
	        	 }
	         }
	      ));
	      littleCycle.setCycleCount(Timeline.INDEFINITE);
	      littleCycle.play();
	   }
	   /** 
	    *creation des cellules et de leurs habits
	    *@param root racine
	    */
	   private void dessinData(Group root)
	   {
		   for(Data2D data:AlgoDBScan2D.dataSet)
		   {
			   Circle circle = new Circle(  data.values[0],  data.values[1], espace/2d);
			   circle.setFill(Color.RED);
			   data.circle = circle;
			   root.getChildren().add(circle);
			   circle = new Circle(  data.values[0],  data.values[1], taille*AlgoDBScan2D.eps);
			   circle.setFill(Color.WHITE);
			   data.circleCluster= circle;
			   root.getChildren().add(0, circle);
			   data.updateColor();
		   }
	   }
	   
	   

	   public static void main(String[] args) {
	      launch(args);
	   }
	}
