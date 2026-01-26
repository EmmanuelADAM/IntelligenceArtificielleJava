package colorkmeans;
	

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.animation.Timeline;

import java.util.ArrayList;

import javafx.animation.KeyFrame;
import javafx.util.Duration;


public class AppliKMeansCouleurs extends Application {
	   /**taille d'une cellule en pixel*/
	   int espace = 20;
	   /**taille d'un coté*/
	   static int taille;
	   /**délai en ms entre chaque évolution*/
	   private static final  int tempo = 2000;
	   /**les couleurs utilisées*/
	   private static final Color[] colors = {Color.BLACK,
			   Color.rgb(150, 0, 0), Color.rgb(0, 150, 0), Color.rgb(0, 0, 150), 
			   Color.rgb(150, 150, 0), Color.rgb(0, 150, 150), Color.rgb(150, 0, 150), 
			   Color.rgb(50, 150, 0), Color.rgb(0, 50, 150), Color.rgb(150, 0, 50)};
	   static Timeline littleCycle;

	   
	   @Override
	   public void start(Stage primaryStage) {
		   AppliKMeansCouleurs.taille = 700;
	      construireScene( primaryStage);
	   }
	   
	   void construireScene(Stage primaryStage) 
	   {	
	      int nbData = 500;
	      int nbCLusters = 6;
	      AlgoKMeans2D.createSamples(nbData, taille, taille);
//	      AlgoKMeans2D.createSamplesConcentric(nbData, taille/2, 3);
	      AlgoKMeans2D.initialize(nbCLusters);
	      //definir la scene principale
	      Group root = new Group();
	      Scene scene = new Scene(root, taille, taille, Color.WHITE);
	      primaryStage.setTitle("KMeans...");
	      primaryStage.setScene(scene);
	      //habiller les acteurs
	      espace = 4;
	      dessinData( root);
	      //afficher le theatre
	      primaryStage.show();
	      //-----lancer le timer pour faire vivre la matrice
	      AppliKMeansCouleurs.littleCycle = new Timeline(new KeyFrame(Duration.millis(tempo), 
	         event-> {
	        	 if(AlgoKMeans2D.isMoving)
	        	 {
	        		 AlgoKMeans2D.kMeanCluster();
	        	 }
	         }
	      ));
	      AppliKMeansCouleurs.littleCycle.setCycleCount(Timeline.INDEFINITE);
	      AppliKMeansCouleurs.littleCycle.play();
	   }
	   /** 
	    *creation des cellules et de leurs habits
	    *@param root racine
	    */
	   void dessinData(Group root)
	   {
		   int i = 0;
		   int nbColors = colors.length;
		   ArrayList<Circle> circles = new ArrayList<>();
		   for(Cluster2D cluster:AlgoKMeans2D.clusters)
		   {
			   Circle clusterCircle = new Circle( 
					   cluster.centroid.values[0],  cluster.centroid.values[1], espace*10);
			   clusterCircle.setFill(colors[i%nbColors]);
			   clusterCircle.setOpacity(0.2);
			   circles.add(clusterCircle);
			   cluster.circle = clusterCircle;
			   i++;
		   }
		   for(Data2D data:AlgoKMeans2D.dataSet)
		   {
			   Circle circle = new Circle(  data.values[0],  data.values[1], espace/2.);
			   circle.setFill(Color.BLUE);
			   circles.add(circle);
			   data.circle = circle;
			   data.updateColor();
		   }
		   root.getChildren().addAll(circles);
	   }
	   

	   public static void main(String[] args) {
	      launch(args);
	   }
	}
