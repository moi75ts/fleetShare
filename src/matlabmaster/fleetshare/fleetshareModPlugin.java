package matlabmaster.fleetshare;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class fleetshareModPlugin extends BaseModPlugin {
    private static final Logger LOGGER = LogManager.getLogger("FleetShare");
    @Override
    public void onGameLoad(boolean newGame) {
        // Check if our intel already exists
        boolean intelExists = false;
        List<?> intelList = Global.getSector().getIntelManager().getIntel();

        for (Object intel : intelList) {
            if (intel instanceof DebugConsoleIntel) {
                intelExists = true;
                break;
            }
        }

        // If it exists, remove it first
        if (intelExists) {
            intelList.removeIf(intel -> intel instanceof DebugConsoleIntel);
        }

        // Add the new instance
        Global.getSector().getIntelManager().addIntel(new DebugConsoleIntel());

        try {
            Global.getSettings().loadTexture("graphics/icons/intel/fleetshare_icon.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}

