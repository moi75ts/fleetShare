package matlabmaster.fleetshare.commands;

import com.fs.starfarer.api.Global;
import matlabmaster.fleetshare.utils.CompressHelper;
import matlabmaster.fleetshare.utils.FleetHelper;
import org.json.JSONObject;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

public class addToMyFleet implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context){
        try {
            Console.showMessage("adding to fleet");
            FleetHelper.unSerializeFleet(new JSONObject(CompressHelper.decompress(args)), Global.getSector().getPlayerFleet());
            return CommandResult.SUCCESS;
        }catch (Exception e){
            Global.getLogger(exportFleet.class).error("Error importing fleet", e);
            return CommandResult.ERROR;
        }
    }
}
