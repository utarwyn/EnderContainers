package fr.utarwyn.endercontainers.inventory.menu;

import fr.utarwyn.endercontainers.configuration.Files;
import fr.utarwyn.endercontainers.configuration.ui.EnderChestItem;
import fr.utarwyn.endercontainers.configuration.ui.EnderChestItemVariantCondition;
import fr.utarwyn.endercontainers.enderchest.EnderChest;

/**
 * Process default and variants enderchest items
 * from configuration based for a specific enderchest.
 *
 * @author Utarwyn
 * @since 2.3.0
 */
public class EnderChestItemSelector {

    /**
     * Selects an enderchest item based for an enderchest.
     *
     * @param enderChest enderchest instance
     * @return item based on enderchest
     */
    public EnderChestItem fromEnderchest(EnderChest enderChest) {
        return Files.getConfiguration().getEnderchestItemVariants().stream()
                .filter(variant -> this.checkIfVariantConditionIsValid(variant.getCondition(), enderChest))
                .findFirst()
                .map(EnderChestItem.class::cast)
                .orElse(Files.getConfiguration().getEnderchestItem());
    }

    /**
     * Checks if an enderchest item variant is valid for an enderchest.
     *
     * @param condition  condition of the variant to check
     * @param enderChest enderchest instance
     * @return true if the variant can be applied for the enderchest
     */
    private boolean checkIfVariantConditionIsValid(EnderChestItemVariantCondition condition, EnderChest enderChest) {
        switch (condition.getKey()) {
            case FILLING:
                return condition.isValidUsingOperator(enderChest.getFillPercentage());
            case INACCESSIBLE:
                return !enderChest.isAccessible();
            default:
            case NUMBER:
                return condition.isValidUsingOperator(enderChest.getNum());
        }
    }

}
