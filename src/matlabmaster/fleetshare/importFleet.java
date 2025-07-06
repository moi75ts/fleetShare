package matlabmaster.fleetshare;

import com.fs.starfarer.api.Global;
import matlabmaster.fleetshare.utils.FleetHelper;
import org.json.JSONObject;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import java.util.Base64;

public class importFleet implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context){
        try {
            Console.showMessage("Importing fleet");
            byte[] decodedBytes = Base64.getDecoder().decode(args);
            String decodedFleet = new String(decodedBytes);
            FleetHelper.spawnNewFleet(new JSONObject(decodedFleet));
            return CommandResult.SUCCESS;
        }catch (Exception e){
            Global.getLogger(exportFleet.class).error("Error importing fleet", e);
            return CommandResult.ERROR;
        }
    }
}
