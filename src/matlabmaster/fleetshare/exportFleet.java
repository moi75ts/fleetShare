package matlabmaster.fleetshare;

import com.fs.starfarer.api.Global;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Base64;

public class exportFleet implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context){
        try {
            String message = "Exporting player's fleet";
            Global.getLogger(exportFleet.class).info(message);
            String result = String.valueOf(matlabmaster.fleetshare.utils.FleetHelper.serializeFleet(Global.getSector().getPlayerFleet()));
            String base64 = Base64.getEncoder().encodeToString(result.getBytes());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(base64),null);
            Console.showMessage("Fleet code copied to clipboard");
            return CommandResult.SUCCESS;
        }catch (Exception e){
            Global.getLogger(exportFleet.class).error("Error exporting fleet", e);
            return CommandResult.ERROR;
        }
    }
}
