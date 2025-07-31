package matlabmaster.fleetshare;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import matlabmaster.fleetshare.utils.CompressHelper;
import matlabmaster.fleetshare.utils.FleetHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.util.Base64;
import java.util.Set;

public class DebugConsoleIntel extends BaseIntelPlugin {
    // Button IDs for our custom buttons
    private static final String BUTTON_1 = "button1";
    private static final String BUTTON_2 = "button2";
    private static final String BUTTON_3 = "button3";
    private static final String BUTTON_4 = "button4";
    private static final String COPY_BUTTON = "copyButton";
    private static final String PASTE_BUTTON = "pasteButton";
    private static final String CLEAR_BUTTON = "clearButton";

    // Single text area variable
    private String mainText = ""; // Changed from inputText/outputText

    @Override
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate,
                                   Color tc, float initPad) {
        info.addPara("export / import fleets", tc, initPad);
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
        Color tc = Misc.getTextColor();
        float pad = 3f;
        info.addPara(getName(), c, 0f);
        addBulletPoints(info, mode, false, tc, pad);
    }

    @Override
    public String getIcon() {
        // Custom icon for the Intel tab
        return "graphics/icons/intel/fleetshare_icon.png";
    }

    // Using the horizontal button row version from your first file
    public void createLargeDescription(CustomPanelAPI panel, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color tc = Misc.getTextColor();
        float opad = 10f;
        float pad = 5f;

        TooltipMakerAPI tooltip = panel.createUIElement(width, height, true);

        tooltip.addSectionHeading("FleetShare", Alignment.MID, opad);

        // --- Create a Horizontal Button Row (Fleet Buttons) ---
        // Define button dimensions for fleet buttons
        float fleetButtonWidth = 130f;
        float buttonHeight = 20f;
        // Add a small gap between buttons
        float buttonGap = 5f;
        // Calculate the total width needed for the button row
        float totalFleetButtonRowWidth = (4 * fleetButtonWidth) + (3 * buttonGap); // 4 buttons, 3 gaps

        // Create a custom panel to hold the fleet buttons horizontally
        CustomPanelAPI fleetButtonRowPanel = panel.createCustomPanel(totalFleetButtonRowWidth, buttonHeight, null);

        // Create TooltipMakers for each fleet button within the row panel
        TooltipMakerAPI button1Tooltip = fleetButtonRowPanel.createUIElement(fleetButtonWidth, buttonHeight, false);
        button1Tooltip.addButton("Export fleet", BUTTON_1, fleetButtonWidth, buttonHeight, 0f);
        fleetButtonRowPanel.addUIElement(button1Tooltip).inTL(0, 0);

        TooltipMakerAPI button2Tooltip = fleetButtonRowPanel.createUIElement(fleetButtonWidth, buttonHeight, false);
        button2Tooltip.addButton("Import fleet", BUTTON_2, fleetButtonWidth, buttonHeight, 0f);
        fleetButtonRowPanel.addUIElement(button2Tooltip).inTL(fleetButtonWidth + buttonGap, 0);

        TooltipMakerAPI button3Tooltip = fleetButtonRowPanel.createUIElement(fleetButtonWidth, buttonHeight, false);
        button3Tooltip.addButton("Add to fleet", BUTTON_3, fleetButtonWidth, buttonHeight, 0f);
        fleetButtonRowPanel.addUIElement(button3Tooltip).inTL(2 * (fleetButtonWidth + buttonGap), 0);

        TooltipMakerAPI button4Tooltip = fleetButtonRowPanel.createUIElement(fleetButtonWidth, buttonHeight, false);
        button4Tooltip.addButton("Replace with fleet", BUTTON_4, fleetButtonWidth, buttonHeight, 0f);
        fleetButtonRowPanel.addUIElement(button4Tooltip).inTL(3 * (fleetButtonWidth + buttonGap), 0);

        // Add the custom fleet button row panel to the main tooltip, centered
        tooltip.addCustom(fleetButtonRowPanel, pad); // Centered alignment added
        // --- End of Horizontal Fleet Button Row ---

        // --- Single Text Area Section ---
        tooltip.addSectionHeading("Fleet Data", Alignment.MID, opad);

        tooltip.addPara("Data:", tc, 0f);

        // Create scrollable container for the single text area
        if (mainText.isEmpty()) {
            tooltip.addPara("<Empty - Use buttons or paste from clipboard>", h, 5f);
        } else {
            // Add scrollable text display
            addScrollableText(tooltip, mainText, width - 20f, 100f, pad);
        }

        // --- Create a Horizontal Button Row (Action Buttons) ---
        // Define button dimensions for action buttons
        float pasteButtonWidth = 150f;
        float clearButtonWidth = 80f;
        float copyButtonWidth = 120f;
        // Use the same gap
        // Calculate the total width needed for the action button row
        // Total width = sum of widths + (number of gaps * gap width)
        float totalActionButtonRowWidth = copyButtonWidth + pasteButtonWidth + clearButtonWidth + (3 * buttonGap);

        // Create a custom panel to hold the action buttons horizontally
        CustomPanelAPI actionButtonRowPanel = panel.createCustomPanel(totalActionButtonRowWidth, buttonHeight, null);

        // Create TooltipMakers for each action button within the row panel

        // Copy Button
        TooltipMakerAPI copyButtonTooltip = actionButtonRowPanel.createUIElement(copyButtonWidth, buttonHeight, false);
        copyButtonTooltip.addButton("Copy", COPY_BUTTON, copyButtonWidth, buttonHeight, 0f);
        actionButtonRowPanel.addUIElement(copyButtonTooltip).inTL(0, 0);
        // Paste Button
        TooltipMakerAPI pasteButtonTooltip = actionButtonRowPanel.createUIElement(pasteButtonWidth, buttonHeight, false);
        pasteButtonTooltip.addButton("Paste", PASTE_BUTTON, pasteButtonWidth, buttonHeight, 0f);
        actionButtonRowPanel.addUIElement(pasteButtonTooltip).inTL(copyButtonWidth + buttonGap, 0);

        // Clear Button
        TooltipMakerAPI clearButtonTooltip = actionButtonRowPanel.createUIElement(clearButtonWidth, buttonHeight, false);
        clearButtonTooltip.addButton("Clear", CLEAR_BUTTON, clearButtonWidth, buttonHeight, 0f);
        actionButtonRowPanel.addUIElement(clearButtonTooltip).inTL(pasteButtonWidth + copyButtonWidth + (buttonGap*2), 0);

        // Add the custom action button row panel to the main tooltip, centered
        tooltip.addCustom(actionButtonRowPanel, pad); // Centered alignment added
        // --- End of Horizontal Action Button Row ---

        panel.addUIElement(tooltip).inTL(0, 0);
    }

    // Helper method to create scrollable text display (Unchanged)
    private void addScrollableText(TooltipMakerAPI tooltip, String text, float width, float height, float pad) {
        // Since we can't create true scrollable areas, we'll wrap the text
        // and limit the display to a reasonable number of lines
        String wrappedText = wrapText(text, 80);
        tooltip.addPara(wrappedText, Misc.getTextColor(), pad);
        // If text is very long, add indicator
        if (text.length() > 500) {
            tooltip.addPara("... (text truncated - use clipboard for full content)",
                    Misc.getGrayColor(), 0f);
        }
    }

    // Helper method to wrap text (Unchanged)
    private String wrapText(String text, int maxWidth) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder wrapped = new StringBuilder();
        String[] lines = text.split("\n"); // Corrected newline escape
        for (String line : lines) {
            if (line.length() <= maxWidth) {
                wrapped.append(line).append("\n"); // Corrected newline escape
            } else {
                // Wrap long lines
                int start = 0;
                while (start < line.length()) {
                    int end = Math.min(start + maxWidth, line.length());
                    wrapped.append(line.substring(start, end)).append("\n"); // Corrected newline escape
                    start = end;
                }
            }
        }
        // Remove trailing newline and limit length for display
        String result = wrapped.toString();
        if (result.endsWith("\n")) { // Corrected newline escape
            result = result.substring(0, result.length() - 1);
        }
        // Limit display length to prevent UI issues
        if (result.length() > 1000) {
            result = result.substring(0, 1000) + "...";
        }
        return result.isEmpty() ? "<Empty>" : result;
    }

    @Override
    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
        // All actions now read from and modify mainText
        if (BUTTON_1.equals(buttonId)) {
            Global.getLogger(this.getClass()).info("Export fleet button pressed");
            String result = null;
            try {
                result = String.valueOf(matlabmaster.fleetshare.utils.FleetHelper.serializeFleet(Global.getSector().getPlayerFleet()));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            // Instead of setting outputText, set mainText
            try {
                mainText = CompressHelper.compress(result);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ui.updateUIForItem(this);
        }
        else if (BUTTON_2.equals(buttonId)) {
            // Import fleet data from the single text area (mainText)
            if (mainText != null && !mainText.isEmpty()) { // Check mainText
                Global.getLogger(this.getClass()).info("Import fleet button pressed with data: " + mainText.substring(0, Math.min(50, mainText.length())) + "...");
                try {
                    CampaignFleetAPI fleet = FleetHelper.spawnNewFleet(new JSONObject(CompressHelper.decompress(mainText)));
                    mainText = "Import successful! Processed data from clipboard area."; // Modify mainText
                } catch (JSONException e) {
                    mainText = "An error occurred, the string you provided is either invalid or contains modded content which are not installed here";
                } catch (IOException e) {
                    mainText = "An error occurred, did you copy the full string and made sure there are no extra spaces / characters?";
                }
            } else {
                Global.getSector().getCampaignUI().addMessage(
                        "No fleet data to import. Please paste/export data first.",
                        Misc.getNegativeHighlightColor()
                );
                // Optionally update mainText to show error
                mainText = "Import failed: No data in clipboard area."; // Modify mainText
            }
            ui.updateUIForItem(this);
        }
        else if (BUTTON_3.equals(buttonId)) {
            // Add fleet data from the single text area (mainText) to player's fleet
            if (mainText != null && !mainText.isEmpty()) { // Check mainText
                Global.getLogger(this.getClass()).info("Add to fleet button pressed with data: " + mainText.substring(0, Math.min(50, mainText.length())) + "...");
                try {
                    FleetHelper.unSerializeFleet(new JSONObject(CompressHelper.decompress(mainText)), Global.getSector().getPlayerFleet());
                    mainText = "Fleet data added to your current fleet!";
                } catch (JSONException e) {
                    mainText = "An error occurred, the string you provided is either invalid or contains modded content which are not installed here";
                } catch (IOException e) {
                    mainText = "An error occurred, did you copy the full string and made sure there are no extra spaces / characters?";
                }
            } else {
                Global.getSector().getCampaignUI().addMessage(
                        "No fleet data to add. Please paste/export data first.",
                        Misc.getNegativeHighlightColor()
                );
                // Optionally update mainText to show error
                mainText = "Add failed: No data in clipboard area."; // Modify mainText
            }
            ui.updateUIForItem(this);
        }
        else if (BUTTON_4.equals(buttonId)) {
            // Replace player's fleet with data from the single text area (mainText)
            if (mainText != null && !mainText.isEmpty()) { // Check mainText
                Global.getLogger(this.getClass()).info("Replace fleet button pressed with data: " + mainText.substring(0, Math.min(50, mainText.length())) + "...");
                try {
                    Global.getSector().getPlayerFleet().getFleetData().clear();
                    FleetHelper.unSerializeFleet(new JSONObject(CompressHelper.decompress(mainText)), Global.getSector().getPlayerFleet());
                    mainText = "Your fleet has been replaced with the clipboard data!"; // Modify mainText
                } catch (Exception e){
                    mainText = "an error has occurred " + e;
                }

            } else {
                Global.getSector().getCampaignUI().addMessage(
                        "No fleet data to replace with. Please paste/export data first.",
                        Misc.getNegativeHighlightColor()
                );
                // Optionally update mainText to show error
                mainText = "Replace failed: No data in clipboard area."; // Modify mainText
            }
            ui.updateUIForItem(this);
        }
        else if (PASTE_BUTTON.equals(buttonId)) {
            // Paste from clipboard into the single text area (mainText)
            try {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                    String clipboardText = (String) clipboard.getData(DataFlavor.stringFlavor);
                    mainText = clipboardText; // Update mainText
                    Global.getSector().getCampaignUI().addMessage(
                            "Text pasted from clipboard",
                            Misc.getPositiveHighlightColor()
                    );
                } else {
                    Global.getSector().getCampaignUI().addMessage(
                            "No text found in clipboard",
                            Misc.getNegativeHighlightColor()
                    );
                }
            } catch (Exception e) {
                Global.getSector().getCampaignUI().addMessage(
                        "Failed to paste from clipboard: " + e.getMessage(),
                        Misc.getNegativeHighlightColor()
                );
                Global.getLogger(this.getClass()).error("Error pasting from clipboard", e);
            }
            ui.updateUIForItem(this);
        }
        else if (CLEAR_BUTTON.equals(buttonId)) {
            mainText = ""; // Clear mainText
            Global.getSector().getCampaignUI().addMessage(
                    "Data area cleared", // Updated message
                    Misc.getPositiveHighlightColor()
            );
            ui.updateUIForItem(this);
        }
        else if (COPY_BUTTON.equals(buttonId)) {
            if (!mainText.isEmpty()) { // Check mainText
                try {
                    StringSelection stringSelection = new StringSelection(mainText); // Copy mainText
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);
                    Global.getSector().getCampaignUI().addMessage(
                            "Data copied to clipboard", // Updated message
                            Misc.getPositiveHighlightColor()
                    );
                } catch (Exception e) {
                    Global.getSector().getCampaignUI().addMessage(
                            "Failed to copy data to clipboard: " + e.getMessage(), // Updated message
                            Misc.getNegativeHighlightColor()
                    );
                    Global.getLogger(this.getClass()).error("Error copying to clipboard", e);
                }
            } else {
                Global.getSector().getCampaignUI().addMessage(
                        "No data to copy", // Updated message
                        Misc.getNegativeHighlightColor()
                );
            }
        }
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_IMPORTANT);
        return tags;
    }

    @Override
    public String getName() {
        return "FleetShare";
    }

    @Override
    public String getSortString() {
        return "FleetShare";
    }

    @Override
    public boolean hasSmallDescription() {
        return true;
    }

    @Override
    public boolean hasLargeDescription() {
        return true;
    }

    @Override
    public IntelSortTier getSortTier() {
        return IntelSortTier.TIER_3;
    }

    // Getters and setters for the single text field
    public String getMainText() { // New getter
        return mainText;
    }

    public void setMainText(String mainText) { // New setter
        this.mainText = mainText;
    }
}
