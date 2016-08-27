package fr.utarwyn.endercontainers.dependencies;

import fr.utarwyn.endercontainers.EnderContainers;
import fr.utarwyn.endercontainers.utils.Config;
import fr.utarwyn.endercontainers.utils.ConfigClass;
import fr.utarwyn.endercontainers.utils.CoreUtils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CitizensIntegration implements Listener{

    private Set<PlayerChatEditMode> playersChatEditMode = new HashSet<>();
    private Set<NPCLink> NPCLinks = new HashSet<>();

    private String separator = "§7 ================§8 [§6EnderContainers§8] §7================ ";
    private String cfgFile   = "citizens_links.yml";

    public void load(){
        EnderContainers.getConfigClass().loadConfigFile(cfgFile);

        reloadLinks();
    }
    public void onCommand(Player player, String subcommand, String[] args){
        switch(subcommand){
            case "link":
                NPC selectedNPC = CitizensAPI.getDefaultNPCSelector().getSelected(player);

                if(selectedNPC == null || !selectedNPC.isSpawned()){
                    CoreUtils.errorMessage(player, "You should select a NPC with the command §6/npc select§c to continue.");
                    return;
                }

                if(getEditModeFor(player) != null){
                    CoreUtils.errorMessage(player, "You are already creating a link configuration.");
                    return;
                }

                player.sendMessage(" ");
                player.sendMessage(this.separator);
                player.sendMessage(" ");
                player.sendMessage(" §r §eYou want to link NPC §a'" + selectedNPC.getName() + "' §ewith the plugin.");
                player.sendMessage(" §r §bStep 1: §eWhat type of inventory this NPC will allow to open?");
                player.sendMessage(" §r §7Please answer by typing §6main§7 or §6enderchest§7 in the chat below.");
                player.sendMessage(" §r §9You can type §2cancel§9 to stop this configuration.");
                player.sendMessage(" ");

                startEditModeFor(player);
                break;

            case "info":
                selectedNPC = CitizensAPI.getDefaultNPCSelector().getSelected(player);

                if(selectedNPC == null || !selectedNPC.isSpawned()){
                    CoreUtils.errorMessage(player, "You should select a NPC with the command §6/npc select§c to continue.");
                    return;
                }

                NPCLink link = getNPCLinkById(selectedNPC.getId());
                if(link == null){
                    CoreUtils.errorMessage(player, "NPC §6" + selectedNPC.getName() + "§c is not linked with EnderContainers.");
                    return;
                }

                player.sendMessage(" ");
                player.sendMessage(this.separator);
                player.sendMessage(" ");
                player.sendMessage(" §r §aNPC: §e" + selectedNPC.getName() + " (" + link.getNPCId() + ")");
                player.sendMessage(" §r §aType: §e" + link.getType().toUpperCase());
                player.sendMessage(" §r §aDelay: §e" + link.getDelay() + "s");
                if(link.getEnderchestNumber() > -1)
                    player.sendMessage(" §r §aChest number: §e" + link.getEnderchestNumber());
                player.sendMessage(" ");
                break;

            case "unlink":
                selectedNPC = CitizensAPI.getDefaultNPCSelector().getSelected(player);

                if(selectedNPC == null || !selectedNPC.isSpawned()){
                    CoreUtils.errorMessage(player, "You should select a NPC with the command §6/npc select§c to continue.");
                    return;
                }

                link = getNPCLinkById(selectedNPC.getId());
                if(link == null){
                    CoreUtils.errorMessage(player, "NPC §6" + selectedNPC.getName() + "§c is not linked with EnderContainers.");
                    return;
                }

                removeNPCLink(link.getNPCId());
                player.sendMessage(Config.prefix + "§aYou have successfully unlinked the NPC §e'" + selectedNPC.getName() + "'§a with EnderContainers.");

                break;

            default:
                CoreUtils.errorMessage(player, EnderContainers.__("error_command_usage") + ": /endc npc <link|info|unlink>");
                break;
        }
    }
    private void onNPCClicked(final Player player, NPC npc){
        NPCLink link = getNPCLinkById(npc.getId());
        if(link == null) return;

        switch(link.getType()){
            case "main":
                if(link.getDelay() == 0)
                    EnderContainers.getEnderchestsManager().openPlayerMainMenu(player, null);
                else{
                    player.sendMessage(Config.prefix + "Opening chest in §6" + link.getDelay() + "§7 seconds...");
                    Bukkit.getScheduler().scheduleSyncDelayedTask(EnderContainers.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            EnderContainers.getEnderchestsManager().openPlayerMainMenu(player, null);
                        }
                    }, link.getDelay() * 20);
                }
                break;
            case "enderchest":
                final int n = link.getEnderchestNumber();
                if(n <= -1 || n > (Config.maxEnderchests - 1)) return;

                if(link.getDelay() == 0)
                    EnderContainers.getEnderchestsManager().openPlayerEnderChest(n, player, null);
                else{
                    player.sendMessage(Config.prefix + "Opening chest in §6" + link.getDelay() + "§7 seconds...");
                    Bukkit.getScheduler().scheduleSyncDelayedTask(EnderContainers.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            EnderContainers.getEnderchestsManager().openPlayerEnderChest(n, player, null);
                        }
                    }, link.getDelay() * 20);
                }
                break;
        }
    }

    // EDIT MODE
    private void startEditModeFor(Player player){
        playersChatEditMode.add(new PlayerChatEditMode(player));
    }
    private PlayerChatEditMode getEditModeFor(Player player){
        for(PlayerChatEditMode mode : playersChatEditMode){
            if(mode.getPlayer().getUniqueId().equals(player.getUniqueId()))
                return mode;
        }

        return null;
    }
    private void stopEditModeFor(Player player){
        PlayerChatEditMode mode = getEditModeFor(player);
        if(mode != null) playersChatEditMode.remove(mode);
    }

    // NPC MANAGEMENT
    private void saveNPCLink(Integer npcId, String linkType, Integer delay, Integer enderchestNumber){
        ConfigClass cc = EnderContainers.getConfigClass();
        if(cc.isConfigurationSection(cfgFile, String.valueOf(npcId))) cc.removePath(cfgFile, String.valueOf(npcId));

        cc.setAutoSaving = false;
        cc.set(cfgFile, npcId + ".type", linkType);
        cc.set(cfgFile, npcId + ".delay", delay);
        if(enderchestNumber != null) cc.set(cfgFile, npcId + ".chestNumber", enderchestNumber);
        cc.saveConfig(cfgFile);
        cc.setAutoSaving = true;

        reloadLinks();
    }
    private void removeNPCLink(Integer npcId){
        NPCLink link = getNPCLinkById(npcId);
        if(link == null) return;

        EnderContainers.getConfigClass().removePath(cfgFile, String.valueOf(npcId));
        EnderContainers.getConfigClass().saveConfig(cfgFile);

        NPCLinks.remove(link);
    }
    private NPCLink getNPCLinkById(Integer npcId){
        for(NPCLink npcLink : NPCLinks){
            if(npcLink.getNPCId() == npcId) return npcLink;
        }
        return null;
    }
    private void reloadLinks(){
        ConfigClass cc = EnderContainers.getConfigClass();

        NPCLinks.clear();

        for(String linkId : cc.getKeys(cfgFile, false)){
            Integer npcId  = Integer.parseInt(linkId);
            String type    = cc.getString(cfgFile, linkId + ".type");
            Integer delay  = cc.getInt(cfgFile, linkId + ".delay");

            Integer enderchestNumber = cc.contains(cfgFile, linkId + ".chestNumber") ? cc.getInt(cfgFile, linkId + ".chestNumber") : -1;

            NPCLinks.add(new NPCLink(npcId, type, delay, enderchestNumber));
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e){
        Player player = e.getPlayer();

        PlayerChatEditMode mode = getEditModeFor(player);
        if(mode == null) return;

        e.setCancelled(true);

        if(e.getMessage().equals("cancel")){
            stopEditModeFor(player);
            player.sendMessage(Config.prefix + "You have stopped the link configuration.");
            return;
        }

        switch(mode.getCurrentStep()){
            case 1:
                if(!e.getMessage().equals("main") && !e.getMessage().equals("enderchest")){
                    player.sendMessage(Config.prefix + "§cPlease type §6main§c or §6enderchest§c.");
                    return;
                }

                mode.setCurrentAnswer(e.getMessage());
                mode.nextStep();


                player.sendMessage(" ");
                player.sendMessage(this.separator);
                player.sendMessage(" ");
                player.sendMessage(" §r §eYou have chosen the type §a" + e.getMessage() + "§e.");
                if(e.getMessage().equals("main")){
                    player.sendMessage(" §r §bStep 2: §eDelay before opening the inventory when a player interacts with a NPC? (in seconds)");
                    player.sendMessage(" §r §7Please answer by typing in the chat below. §6(or 'no', if you don't want to have a delay)");
                }else{
                    player.sendMessage(" §r §bStep 2: §eWhat enderchest number this NPC will open ?");
                    player.sendMessage(" §r §7Please answer by typing in the chat below. §6(a number between 0 and " + (Config.maxEnderchests - 1) + ")");
                    mode.setCurrentStep(3);
                }

                player.sendMessage(" ");
                break;

            case 2:
                if(!e.getMessage().equals("no") && !StringUtils.isNumeric(e.getMessage())){
                    player.sendMessage(Config.prefix + "§cPlease type the delay (seconds) or §6no§c if you don't want to have a delay.");
                    return;
                }

                NPC selectedNPC = CitizensAPI.getDefaultNPCSelector().getSelected(player);

                mode.setCurrentAnswer(e.getMessage());
                stopEditModeFor(player);

                String delayAnswer = mode.getAnswerForStep(2);
                int delay          = StringUtils.isNumeric(delayAnswer) ? Integer.parseInt(delayAnswer) : 0;

                saveNPCLink(selectedNPC.getId(), mode.getAnswerForStep(1), delay, -1);

                player.sendMessage(Config.prefix + "§eNPC §a'" + selectedNPC.getName() + "'§e linked with EnderContainers. Enjoy!");
                break;

            case 3:
                if(!StringUtils.isNumeric(e.getMessage()) || Integer.parseInt(e.getMessage()) < 0 || Integer.parseInt(e.getMessage()) > Config.maxEnderchests - 1){
                    player.sendMessage(Config.prefix + "§cPlease type an enderchest number between §60 and " + (Config.maxEnderchests - 1) + "§c.");
                    return;
                }

                mode.setCurrentAnswer(e.getMessage());
                mode.nextStep();


                player.sendMessage(" ");
                player.sendMessage(this.separator);
                player.sendMessage(" ");
                player.sendMessage(" §r §eYou have chosen the number §a" + e.getMessage() + "§e.");
                player.sendMessage(" §r §bStep 3: §eDelay before opening the inventory when a player interacts with a NPC? (in seconds)");
                player.sendMessage(" §r §7Please answer by typing in the chat below. §6(or 'no', if you don't want to have a delay)");

                player.sendMessage(" ");
                break;

            case 4:
                if(!e.getMessage().equals("no") && !StringUtils.isNumeric(e.getMessage())){
                    player.sendMessage(Config.prefix + "§cPlease type the delay (seconds) or §6no§c if you don't want to have a delay.");
                    return;
                }

                selectedNPC = CitizensAPI.getDefaultNPCSelector().getSelected(player);

                mode.setCurrentAnswer(e.getMessage());
                stopEditModeFor(player);

                delayAnswer              = mode.getAnswerForStep(4);
                String chestNumberAnswer = mode.getAnswerForStep(3);

                delay           = StringUtils.isNumeric(delayAnswer) ? Integer.parseInt(delayAnswer) : 0;
                int chestNumber = StringUtils.isNumeric(chestNumberAnswer) ? Integer.parseInt(chestNumberAnswer) : -1;

                saveNPCLink(selectedNPC.getId(), mode.getAnswerForStep(1), delay, chestNumber);

                player.sendMessage(Config.prefix + "§eNPC §a'" + selectedNPC.getName() + "'§e linked with EnderContainers. Enjoy!");
                break;
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        if(getEditModeFor(player) != null) stopEditModeFor(player);
    }
    @EventHandler
    public void onNPCRightClicked(NPCRightClickEvent e){
        onNPCClicked(e.getClicker(), e.getNPC());
        e.setCancelled(true);
    }
    @EventHandler
    public void onNPCLeftClicked(NPCLeftClickEvent e){
        onNPCClicked(e.getClicker(), e.getNPC());
        e.setCancelled(true);
    }
    @EventHandler
    public void onNPCRemoved(NPCRemoveEvent e){
        NPC npc = e.getNPC();

        if(getNPCLinkById(npc.getId()) != null)
            removeNPCLink(npc.getId());
    }


    private class PlayerChatEditMode{
        private Player player;
        private Map<Integer, String> answers = new HashMap<>();
        private Integer currentStep = 1;

        PlayerChatEditMode(Player player){
            this.player = player;
        }

        public void setCurrentStep(Integer step){
            this.currentStep = step;
        }
        public String getAnswerForStep(Integer step){
            return this.answers.get(step);
        }

        public Player getPlayer(){
            return this.player;
        }
        public Integer getCurrentStep(){
            return this.currentStep;
        }
        public void setCurrentAnswer(String answer){
            this.answers.put(this.getCurrentStep(), answer);
        }
        public void nextStep(){
            this.setCurrentStep(this.getCurrentStep() + 1);
        }
    }
    private class NPCLink{
        private Integer npcId;
        private String type;
        private Integer enderchestNumber = -1;
        private Integer delay;

        NPCLink(Integer npcId, String type, Integer delay, Integer enderchestNumber){
            this.npcId = npcId;
            this.type = type;
            this.delay = delay;
            this.enderchestNumber = enderchestNumber;
        }
        NPCLink(Integer npcId, String type, Integer delay){
            this(npcId, type, delay, 0);
        }

        public Integer getNPCId(){
            return this.npcId;
        }
        public String getType(){
            return this.type;
        }
        public Integer getDelay(){
            return this.delay;
        }
        public Integer getEnderchestNumber(){
            return this.enderchestNumber;
        }
    }
}
