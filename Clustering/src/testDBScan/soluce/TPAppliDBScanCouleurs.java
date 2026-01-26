package testDBScan.soluce;


import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import static java.lang.System.out;

/**
 * graphic application to illustrate the clustering via DBScan method
 * @author emmanueladam
 * @since 21/10/19
 * */
public class TPAppliDBScanCouleurs extends Application {
	   /**taille d'une donnée*/
	   private int espace = 4;
	   /**taille d'un coté*/
	   private static int taille;
	   /**les couleurs utilisées*/
	   static Color[] colors = {Color.BLACK,
			   Color.rgb(150, 0, 0), Color.rgb(0, 150, 0), Color.rgb(0, 0, 150), 
			   Color.rgb(150, 150, 0), Color.rgb(0, 150, 150), Color.rgb(150, 0, 150), 
			   Color.rgb(50, 150, 0), Color.rgb(0, 50, 150), Color.rgb(150, 0, 50)};
	   /***/

	   @Override
	   public void start(Stage primaryStage) {
		   TPAppliDBScanCouleurs.taille = 700;
	      construireScene( primaryStage);
	   }
	   
	   private void construireScene(Stage primaryStage)
	   {	
		   initClusterize(3000, 4);
		  // definir la scene principale
		  Group root = new Group();
	      Scene scene = new Scene(root, taille, taille, Color.WHITE);
	      primaryStage.setTitle("DBScan...");
	      primaryStage.setScene(scene);
	      //create the actor and dress them
	      espace = 4;
	      dessinData( root);
	      //show !
	      primaryStage.show();
		  //launch the clustering
		   clusterize();
	   }


	   /**initialize the datasets
		* @param nbData nb of data
		* @param nbSets nb of groups */
	   void initClusterize(int nbData, int nbSets)
	   {
			   AlgoDBScan.createSamplesConcentric(nbData, taille/2, nbSets);
			   AlgoDBScan.initialize();
	   }

	   /**launch the clustering*/
	void clusterize()
	{
		long start = System.currentTimeMillis();
		AlgoDBScan.dbScan();
		long end = System.currentTimeMillis();
		out.println("after " + (end-start) + " ms ");
		AlgoDBScan.showClusters();
		AlgoDBScan.recolorize();
	}
	   /** 
	    *create the representation of data and link those representations to them
	    *@param root racine
	    */
	   private void dessinData(Group root)
	   {
		   for(Data data:AlgoDBScan.dataSet)
		   {
			   Circle circle = new Circle(  data.values[0],  data.values[1], espace/2d);
			   circle.setFill(Color.RED);
			   data.setC(circle);
			   root.getChildren().add(circle);
		   }
	   }
	   

	   /**main function that launch the window*/
	   public static void main(String[] args) {
	      launch(args);
	   }
	}
