package matlabmaster.fleetshare.utils;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponGroupType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.*;

public class FleetHelper {
    public static JSONObject serializeFleet(CampaignFleetAPI fleet) throws JSONException {
        JSONObject serializedFleet = new JSONObject();
        serializedFleet.put("id", fleet.getId());
        serializedFleet.put("locationX", fleet.getLocation().getX());
        serializedFleet.put("locationY", fleet.getLocation().getY());
        serializedFleet.put("location", Global.getSector().getCurrentLocation().getName());
        serializedFleet.put("factionId", fleet.getFaction().getId());
        serializedFleet.put("acceleration", fleet.getAcceleration());
        try {
            serializedFleet.put("currentAssignment", fleet.getCurrentAssignment().getActionText());
            serializedFleet.put("currentAssignmentTargetId", fleet.getCurrentAssignment().getTarget().getId());
        } catch (Exception e) {
            //do nothing no Assignment
            //occurs when players fleet
        }

        serializedFleet.put("moveDestinationX", fleet.getMoveDestination().getX());
        serializedFleet.put("moveDestinationY", fleet.getMoveDestination().getY());
        serializedFleet.put("isPlayerFleet", fleet.isPlayerFleet());
        serializedFleet.put("isTransponderOn", fleet.isTransponderOn());
        serializedFleet.put("maxBurnLevel", fleet.getFleetData().getBurnLevel());

        serializedFleet.put("abilities", serializeAbilities(fleet.getAbilities()));// no need for hash, apply

        JSONArray serializedCargo = CargoHelper.serializeCargo(fleet.getCargo().getStacksCopy());
        serializedFleet.put("cargo", serializedCargo);

        JSONArray serializedShips = serializeFleetShips(fleet.getFleetData());
        serializedFleet.put("ships", serializedShips);

        return serializedFleet;
    }


    /**
     * Unserializes a fleet from a JSONArray
     *
     * @param serializedFleet JSONArray containing serialized ship data
     * @param fleet           The fleet unSerialize (give it an empty fleet)
     * @throws JSONException If JSON parsing fails
     */
    public static void unSerializeFleet(JSONObject serializedFleet, CampaignFleetAPI fleet) throws JSONException {
        fleet.setTransponderOn(serializedFleet.getBoolean("isTransponderOn"));

        // Unserialize abilities
        JSONArray abilities = serializedFleet.getJSONArray("abilities");
        unSerializeAbilities(abilities, fleet);
        //fleet.getCargo().clear();
        CargoHelper.addCargoFromSerialized(serializedFleet.getJSONArray("cargo"), fleet.getCargo());
        //clearFleetMembers(fleet);
        JSONArray ships = serializedFleet.getJSONArray("ships");
        unSerializeFleetMember(ships, fleet);
    }

    public static JSONArray serializeAbilities(Map<String, AbilityPlugin> abilities) throws JSONException {
        JSONArray serializedAbilities = new JSONArray();
        for (AbilityPlugin ability : abilities.values()) {
            JSONObject serializedAbility = new JSONObject();
            if (!Objects.equals(ability.getId(), "transponder")) {
                serializedAbility.put("abilityId", ability.getId());
                serializedAbility.put("abilityActive", ability.isActive());//used for continous abilities ei, go dark, sustained burn, transponder
                serializedAbility.put("abilityInProgress", ability.isInProgress());//used for one time then cooldown ie, emergency burn, interdiction pulse ...
                serializedAbilities.put(serializedAbility);
            }
        }
        return serializedAbilities;
    }

    public static void unSerializeAbilities(JSONArray abilitiesArray, CampaignFleetAPI fleet) throws JSONException {
        for (int i = 0; i < abilitiesArray.length(); i++) {
            JSONObject abilityObject = abilitiesArray.getJSONObject(i);
            AbilityPlugin ability = fleet.getAbility(abilityObject.getString("abilityId"));
            if (ability == null) {
                fleet.addAbility(abilityObject.getString("abilityId"));
            } else {
                if (abilityObject.getBoolean("abilityActive") && !ability.isActive()) {
                    ability.activate();
                } else if (abilityObject.getBoolean("abilityInProgress") && !ability.isActiveOrInProgress()) {
                    ability.activate();
                } else if (ability.isActiveOrInProgress() && !abilityObject.getBoolean("abilityInProgress")) {
                    ability.deactivate();
                } else if (ability.isActive() && !abilityObject.getBoolean("abilityActive")) {
                    ability.deactivate();
                }
            }
        }
    }


    public static JSONArray serializeFleetShips(FleetDataAPI fleet) throws JSONException {
        JSONArray serializedFleet = new JSONArray();
        for (FleetMemberAPI ship : fleet.getMembersListCopy()) {
            JSONObject shipSerialized = new JSONObject();
            String hullId = ship.getHullSpec().getHullId();
            // Remove variant suffix if present (like "_default_D")
            if (hullId.contains("_default_")) {
                hullId = hullId.substring(0, hullId.indexOf("_default_"));
            }
            shipSerialized.put("hull", hullId);
            shipSerialized.put("combatReadiness", ship.getRepairTracker().getCR());
            shipSerialized.put("name", ship.getShipName());
            shipSerialized.put("isMothballed", ship.isMothballed());
            shipSerialized.put("fluxVents", ship.getVariant().getNumFluxVents());
            shipSerialized.put("fluxCapacitors", ship.getVariant().getNumFluxCapacitors());
            shipSerialized.put("isFlagShip", ship.isFlagship());
            ShipVariantAPI shipVariant = ship.getVariant();

            JSONArray hullModsArray = new JSONArray();
            Collection<String> hullMods = shipVariant.getHullMods();
            for (String hullMod : hullMods) {
                hullModsArray.put(hullMod);
            }
            shipSerialized.put("hullMods", hullMods);

            JSONArray sHullModsArray = new JSONArray();
            Set<String> sHullMods = shipVariant.getSMods();
            for (String sHullMod : sHullMods) {
                sHullModsArray.put(sHullMod);
            }
            shipSerialized.put("sHullMods", sHullMods);

            JSONArray fittedWingsArray = new JSONArray();
            List<String> fittedWings = shipVariant.getFittedWings();
            for (String fittedWing : fittedWings) {
                fittedWingsArray.put(fittedWing);
            }
            shipSerialized.put("fittedWings", fittedWingsArray);

            JSONArray fittedGunsArray = new JSONArray();
            for (String gunSlot : shipVariant.getFittedWeaponSlots()) {
                JSONObject gunSlotObject = new JSONObject();
                gunSlotObject.put("slotId", gunSlot);
                gunSlotObject.put("gunId", shipVariant.getWeaponId(gunSlot));
                fittedGunsArray.put(gunSlotObject);
            }
            shipSerialized.put("fittedGuns", fittedGunsArray);

            JSONArray weaponGroupsArray = new JSONArray();
            for (WeaponGroupSpec weaponGroup : shipVariant.getWeaponGroups()) {
                JSONObject weaponGroupObject = new JSONObject();
                weaponGroupObject.put("type", weaponGroup.getType());
                weaponGroupObject.put("autofire", weaponGroup.isAutofireOnByDefault());
                JSONArray slots = new JSONArray();
                for (String slotId : weaponGroup.getSlots()) {
                    slots.put(slotId);
                }
                weaponGroupObject.put("slots", slots);
                weaponGroupsArray.put(weaponGroupObject);
            }
            shipSerialized.put("weaponGroups", weaponGroupsArray);

            shipSerialized.put("captain", PersonsHelper.serializePerson(ship.getCaptain()));

            serializedFleet.put(shipSerialized);
        }
        return serializedFleet;
    }

    public static FleetMemberAPI unSerializeFleetMember(JSONObject shipObject) throws JSONException {
        int i;
        FleetMemberAPI ship = Global.getFactory().createFleetMember(FleetMemberType.SHIP, shipObject.getString("hull") + "_Hull");
        ship.getRepairTracker().setCR((float) shipObject.getDouble("combatReadiness"));
        try {
            ship.setShipName(shipObject.getString("name"));
        } catch (Exception e) {
            ship.setShipName("");
        }
        ship.getRepairTracker().setMothballed(shipObject.getBoolean("isMothballed"));
        ship.getVariant().setNumFluxVents(shipObject.getInt("fluxVents"));
        ship.getVariant().setNumFluxCapacitors(shipObject.getInt("fluxCapacitors"));


        ship.getVariant().setMayAutoAssignWeapons(false);//If not weapon groups sync will not work,

        JSONArray hullMods = shipObject.getJSONArray("hullMods");
        for (i = 0; i < hullMods.length(); i++) {
            ship.getVariant().addMod(hullMods.getString(i));
        }

        JSONArray sHullMods = shipObject.getJSONArray("sHullMods");
        for (i = 0; i < sHullMods.length(); i++) {
            ship.getVariant().addPermaMod(sHullMods.getString(i), true);
        }

        JSONArray fittedWings = shipObject.getJSONArray("fittedWings");
        for (i = 0; i < fittedWings.length(); i++) {
            ship.getVariant().setWingId(i, fittedWings.getString(i));
        }

        JSONArray fittedGuns = shipObject.getJSONArray("fittedGuns");
        for (i = 0; i < fittedGuns.length(); i++) {
            JSONObject fittedGun = fittedGuns.getJSONObject(i);
            ship.getVariant().addWeapon(fittedGun.getString("slotId"), fittedGun.getString("gunId"));
        }

        JSONArray weaponGroups = shipObject.getJSONArray("weaponGroups");
        for (i = 0; i < weaponGroups.length(); i++) {
            JSONObject weaponGroupObject = weaponGroups.getJSONObject(i);
            WeaponGroupSpec weaponGroup = new WeaponGroupSpec();
            weaponGroup.setType(WeaponGroupType.valueOf(weaponGroupObject.getString("type")));
            weaponGroup.setAutofireOnByDefault(weaponGroupObject.getBoolean("autofire"));
            for (int j = 0; j < weaponGroupObject.getJSONArray("slots").length(); j++) {
                weaponGroup.addSlot(weaponGroupObject.getJSONArray("slots").getString(j));
            }
            ship.getVariant().addWeaponGroup(weaponGroup);
        }
        ship.setCaptain(PersonsHelper.unSerializePerson(shipObject.getJSONObject("captain")));
        return ship;
    }

    /**
     * Unserializes multiple fleet members from a JSONArray and adds them to the fleet
     *
     * @param ships JSONArray containing serialized ship data
     * @param fleet The fleet to add the ships to
     * @throws JSONException If JSON parsing fails
     */
    public static void unSerializeFleetMember(JSONArray ships, CampaignFleetAPI fleet) throws JSONException {
        for (int i = 0; i < ships.length(); i++) {
            JSONObject shipObject = ships.getJSONObject(i);
            FleetMemberAPI member = unSerializeFleetMember(shipObject);
            fleet.getFleetData().addFleetMember(member);
            if(shipObject.getBoolean("isFlagShip")){
                fleet.setCommander(member.getCaptain());
                fleet.getFleetData().setFlagship(member);
            }
        }
    }

    public static void clearFleetMembers(CampaignFleetAPI fleet) {
        for (FleetMemberAPI ship : fleet.getFleetData().getMembersListWithFightersCopy()) {
            fleet.getFleetData().removeFleetMember(ship);
        }
    }

    public static CampaignFleetAPI spawnNewFleet(JSONObject message) throws JSONException {
        CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet("neutral", "spawned fleet", false);
        FleetHelper.unSerializeFleet(message, fleet);
        fleet.setLocation(Global.getSector().getPlayerFleet().getLocation().getX(), Global.getSector().getPlayerFleet().getLocation().getY());
        if(Objects.equals(Global.getSector().getPlayerFleet().getContainingLocation().getId(), "hyperspace")){
            Global.getSector().getHyperspace().addEntity(fleet);
        }else{
            Global.getSector().getPlayerFleet().getStarSystem().addEntity(fleet);
        }
        return fleet;
    }

}