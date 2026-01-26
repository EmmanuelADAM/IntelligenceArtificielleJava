package src.chemin.gui;

import java.awt.BorderLayout;
import javax.swing.*; 

import src.chemin.environment.Entrepot;


/** window used to display the environment
 * @author emmanuel adam*/
public class EnvironmentGui extends JFrame{

    /** environment to display*/
	private final Entrepot ent;
    
    /**jPanel for displaying results*/
	private PanelMethodChoice panelResult;
    
    
    public EnvironmentGui(Entrepot _ent ) {
        ent = _ent;
    	setBounds(10, 10, ent.getWidth()*6+10, ent.getHeight()*5+100);     
    	setTitle("A* resolution for shortest way");
    	buildGui();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    
    /** add a PanelEnvironment in the center of the window*/
    private void buildGui()
    {
    	
    	getContentPane().setLayout(new BorderLayout());
    	getContentPane().add(BorderLayout.CENTER, new PanelEnvironment( ent.getWidth()*2, ent.getHeight()*2, ent));    		
        panelResult = new PanelMethodChoice(ent);
        getContentPane().add(BorderLayout.SOUTH, panelResult);
    }

	/**
	 * @param _fieldClosedNodes the fieldClosedNodes to set
	 */
	public void setFieldClosedNodes(String _fieldClosedNodes) {
		panelResult.setFieldClosedNodes(_fieldClosedNodes);
	}

	/**
	 * @param _fieldFreeNodes the fieldFreeNodes to set
	 */
	public void setFieldFreeNodes(String _fieldFreeNodes) {
		panelResult.setFieldFreeNodes(_fieldFreeNodes);
	}

	/**
	 * @param _fieldDistance the fieldDistance to set
	 */
	public void setFieldDistance(String _fieldDistance) {
		panelResult.setFieldDistance(_fieldDistance);
	}

	/**
	 * @param _fieldDistanceWithNuisance the fieldDistance with nuisance to set
	 */
	public void setFieldDistanceWithNuisance(String _fieldDistanceWithNuisance) {
		panelResult.setFieldDistanceWithNuisance(_fieldDistanceWithNuisance);
	}

	/**
	 * @param _fieldComputeTime the  Compute Time to set
	 */
	public void setFieldComputeTime(String _fieldComputeTime) {
		panelResult.setFieldComputeTime( _fieldComputeTime) ;
	}

}
