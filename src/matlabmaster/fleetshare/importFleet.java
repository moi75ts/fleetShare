package matlabmaster.fleetshare;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import matlabmaster.fleetshare.utils.CompressHelper;
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
            CampaignFleetAPI fleet = FleetHelper.spawnNewFleet(new JSONObject(CompressHelper.decompress(args)));
            Console.showMessage("Imported fleet amounting to " + fleet.getFleetPoints() + " fleet points");
            Console.showMessage("Imported fleet composed of " + fleet.getFleetData().getNumMembers() + " ships");
            return CommandResult.SUCCESS;
        }catch (Exception e){
            Global.getLogger(exportFleet.class).error("Error importing fleet", e);
            return CommandResult.ERROR;
        }
    }
}
