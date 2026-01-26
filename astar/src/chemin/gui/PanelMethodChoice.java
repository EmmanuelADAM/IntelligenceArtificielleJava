package src.chemin.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import src.chemin.TypeDistance;
import src.chemin.actions.TypeNeighborhood;
import src.chemin.environment.Entrepot;

/**
 * JPanel pour le choix de la methode de calcul et la presentation des resultats
 * @author emmanueladam
 */
public class PanelMethodChoice extends JPanel implements ActionListener, ChangeListener {
	/** entrepot (environment) linked to this gui*/
	private final Entrepot ent;
	
	/**textfied for closed nodes*/
	private JTextField fieldClosedNodes;
	/**textfied for free nodes*/
	private JTextField fieldFreeNodes;
	/**textfied for total nodes*/
	private JTextField fieldTotalNodes;
	/**textfied for distance */
	private JTextField fieldDistance;
	/**textfied for compute time */
	private JTextField fieldComputeTime;
	/**textfied for distance */
	private JTextField fieldDistanceWithNuisance;

	/**choice of the method to compute the best path*/
	private JComboBox<TypeDistance> methodBuildPathChoice;
	/**choice of the method to compute the neighbouring of a node*/
	private JComboBox<TypeNeighborhood> methodBuildingNeighbouringChoice;

	/**slider to adjust the repulsion near a container*/
	private JSlider sliderAvoidContainer;
	
	/**checkbox to set the landscape unified or composed of 3 different 'things'*/
	private JCheckBox unifiedLandscape;
	
	PanelMethodChoice(Entrepot _ent)
	{ent = _ent;
	buildGui();
	}

	/**create the panel*/
	private void buildGui()
	{
		setLayout(new GridLayout(0, 4));
		
		methodBuildPathChoice = new JComboBox<>(TypeDistance.values());
		methodBuildPathChoice.setActionCommand("MethodeCalculChemin");
		methodBuildPathChoice.setToolTipText("methode of best path computing");
		methodBuildPathChoice.addActionListener(this);
		add(methodBuildPathChoice);
		
		methodBuildingNeighbouringChoice = new JComboBox<>( TypeNeighborhood.values());
		methodBuildingNeighbouringChoice.setActionCommand("MethodeCalculVoisinage");
		methodBuildingNeighbouringChoice.setToolTipText("accessible neighbouring");
		methodBuildingNeighbouringChoice.addActionListener(this);
		add(methodBuildingNeighbouringChoice); 		
		
		JButton go = new JButton("Compute the best path");
		go.setActionCommand("CALCUL");
		go.addActionListener(this);
		add(go);
		
		JPanel jpanel = new JPanel(new FlowLayout());		
		jpanel.add(new JLabel("aprrox time in ms = "));
		fieldComputeTime  = new JTextField("00000000");
		fieldComputeTime.setEditable(false);
		jpanel.add(fieldComputeTime);
		add(jpanel);
		
		jpanel = new JPanel(new FlowLayout());		
		jpanel.add(new JLabel("closed nodes"));
		fieldClosedNodes = new JTextField("00000000");
		fieldClosedNodes.setEditable(false);
		jpanel.add(fieldClosedNodes);
		add(jpanel);
		
		jpanel = new JPanel(new FlowLayout());		
		jpanel.add(new JLabel("free nodes remaining"));
		fieldFreeNodes = new JTextField("00000000");
		fieldFreeNodes.setEditable(false);
		jpanel.add(fieldFreeNodes);
		add(jpanel);
		
		jpanel = new JPanel(new FlowLayout());		
		jpanel.add(new JLabel("total of visited nodes"));
		fieldTotalNodes = new JTextField("00000000");
		fieldTotalNodes.setEditable(false);
		jpanel.add(fieldTotalNodes);
		add(jpanel);
		
		jpanel = new JPanel(new FlowLayout());		
		jpanel.add(new JLabel("distance = "));
		fieldDistance  = new JTextField("00000000");
		fieldDistance.setEditable(false);
		jpanel.add(fieldDistance);
		add(jpanel);
		
		unifiedLandscape  = new JCheckBox();
		unifiedLandscape.addActionListener(this);
		unifiedLandscape.setSelected(false);
		unifiedLandscape.setToolTipText("Terrain composed of 3 different parts");
		unifiedLandscape.setText("Composed terrain");
		unifiedLandscape.setActionCommand("SetTerrainCompose");
		add(unifiedLandscape);

		add(new JLabel("coefficient of obstacles avoiding:"));

		sliderAvoidContainer = new JSlider(JSlider.HORIZONTAL, 0, 99, 0);
		sliderAvoidContainer.addChangeListener(this);
		sliderAvoidContainer.setMajorTickSpacing(10);
		sliderAvoidContainer.setMinorTickSpacing(2);
		sliderAvoidContainer.setPaintTicks(true);
		sliderAvoidContainer.setPaintLabels(true);
		sliderAvoidContainer.setPreferredSize(new Dimension(600, 50));
		sliderAvoidContainer.setToolTipText("coefficient d'evitement d'obstacles");
		add(sliderAvoidContainer);
		
		jpanel = new JPanel(new FlowLayout());		
		jpanel.add(new JLabel("distance pondered with nuisance = "));
		fieldDistanceWithNuisance  = new JTextField("00000000");
		fieldDistanceWithNuisance.setEditable(false);
		jpanel.add(fieldDistanceWithNuisance);
		add(jpanel);
}

	/**action when  event on the choice of a method and on the compute button*/
	public void actionPerformed(ActionEvent evt) {
		String typeEvt = evt.getActionCommand();
//		System.err.println(typeEvt);
		switch (typeEvt) {
			case "MethodeCalculChemin" -> {
				TypeDistance methode = (TypeDistance) methodBuildPathChoice.getSelectedItem();
				ent.setMethod(methode);
			}
			case "MethodeCalculVoisinage" -> {
				TypeNeighborhood methodeVoisinage = (TypeNeighborhood) methodBuildingNeighbouringChoice.getSelectedItem();
				ent.setMethodNeighbouring(methodeVoisinage);
			}
			case "CALCUL" -> {
				ent.removeVisit();
				ent.recompute();
			}
			case "EviterVoisinage" -> {
				ent.removeVisit();
				ent.diffusePoison();
				ent.recompute();
			}
			case "SetTerrainCompose" -> {
				ent.removeVisit();
				if (unifiedLandscape.isSelected())
					ent.colorLandscape();
				else
					ent.unifyLandscape();
				int valeur = sliderAvoidContainer.getValue();
				valeur = (valeur <= 0 ? 0 : 100 - valeur);
				ent.setCoefPoisonNeightbourg(valeur);
				if (valeur > 0) ent.diffusePoison();
				else
					ent.restoreArea();
				ent.recompute();
			}
		}
		

	}

	/**
	 * @param _fieldClosedNodes the fieldClosedNodes to set
	 */
	void setFieldClosedNodes(String _fieldClosedNodes) {
		fieldClosedNodes.setText(_fieldClosedNodes);
		int closedNodes = Integer.parseInt(_fieldClosedNodes);
		int freeNodes = Integer.parseInt(fieldFreeNodes.getText());
		fieldTotalNodes.setText(""+ (closedNodes + freeNodes));
	}

	/**
	 * @param _fieldFreeNodes the fieldFreeNodes to set
	 */
	void setFieldFreeNodes(String _fieldFreeNodes) {
		fieldFreeNodes.setText(_fieldFreeNodes);
		int closedNodes = Integer.parseInt(fieldClosedNodes.getText());
		int freeNodes = Integer.parseInt(_fieldFreeNodes);
		fieldTotalNodes.setText(""+ (closedNodes + freeNodes));
	}

	/**
	 * @param _fieldDistance the fieldDistance to set
	 */
	void setFieldDistance(String _fieldDistance) {
		fieldDistance.setText(_fieldDistance);
	}

	
	/**
	 * @param _fieldDistanceWithNuisance the fieldDistance with nuisance to set
	 */
	void setFieldDistanceWithNuisance(String _fieldDistanceWithNuisance) {
		fieldDistanceWithNuisance.setText(_fieldDistanceWithNuisance);
	}
	

	/**
	 * @param _fieldComputeTime the  Compute Time to set
	 */
	void setFieldComputeTime(String _fieldComputeTime) {
		fieldComputeTime.setText(_fieldComputeTime);
	}


	/**change of the slider --> change of neighbouring nuisance*/
	public void stateChanged(ChangeEvent evt) {
		JSlider source = (JSlider)evt.getSource(); 
			{
				if (source == sliderAvoidContainer) 
				{
					ent.removeVisit();
					ent.restoreArea();
					int valeur = sliderAvoidContainer.getValue();
					valeur = (valeur<=0?0:100-valeur);
					ent.setCoefPoisonNeightbourg(valeur);
					if(valeur>0) ent.diffusePoison();
//					System.err.println("valeur="+valeur);
					ent.recompute();
				}

			}
	}
	
	
}
