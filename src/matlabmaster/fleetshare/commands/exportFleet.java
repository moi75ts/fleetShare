package matlabmaster.fleetshare.commands;

import com.fs.starfarer.api.Global;
import matlabmaster.fleetshare.utils.CompressHelper;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class exportFleet implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context){
        try {
            String message = "Exporting player's fleet";
            Global.getLogger(exportFleet.class).info(message);
            String result = String.valueOf(matlabmaster.fleetshare.utils.FleetHelper.serializeFleet(Global.getSector().getPlayerFleet()));
            result = CompressHelper.compress(result);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(result),null);
            Console.showMessage("Exported fleet amounting to " + Global.getSector().getPlayerFleet().getFleetPoints() + " fleet points");
            Console.showMessage("Exported fleet composed of " + Global.getSector().getPlayerFleet().getFleetData().getNumMembers() + " ships");
            Console.showMessage("Fleet code copied to clipboard");
            return CommandResult.SUCCESS;
        }catch (Exception e){
            Global.getLogger(exportFleet.class).error("Error exporting fleet", e);
            return CommandResult.ERROR;
        }
    }
}
