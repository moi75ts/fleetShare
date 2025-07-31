package matlabmaster.fleetshare;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.lazywizard.console.Console;

import java.io.IOException;

public class fleetshareModPlugin extends BaseModPlugin {
    private static final Logger LOGGER = LogManager.getLogger("FleetShare");
    @Override
    public void onGameLoad(boolean newGame) {
        Global.getSector().getIntelManager().addIntel(new DebugConsoleIntel());
        try {
            Global.getSettings().loadTexture("graphics/icons/intel/fleetshare_icon.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

