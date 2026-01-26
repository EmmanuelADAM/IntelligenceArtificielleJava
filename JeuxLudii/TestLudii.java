import app.StartDesktopApp;
import utils.AIRegistry;

/**Launch Ludii Application, adding specific AI implementations*/
public class TestLudii {

    public static void main(final String[] args)
    {
        // AI playing randomly
        String label = "EAAI";
        if (!AIRegistry.registerAI(label, MyDummyAI::new, (game) -> {return true;}))
            System.err.println("WARNING! Failed to register "+label+" because one with that name already existed!");
        if (!AIRegistry.registerAI(label, UCTSoemers::new, (game) -> {return true;}))
            System.err.println("WARNING! Failed to register "+label+" because one with that name already existed!");
        // Run Ludii
        StartDesktopApp.main(new String[0]);
    }

}