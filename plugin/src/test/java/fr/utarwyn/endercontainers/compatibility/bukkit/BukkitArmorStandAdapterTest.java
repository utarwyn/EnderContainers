package fr.utarwyn.endercontainers.compatibility.bukkit;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BukkitArmorStandAdapterTest {

    private static final String TEXT = "text";

    private BukkitArmorStandAdapter armorStandAdapter;

    private Location location;

    @Mock
    private ArmorStand armorStand;

    @Mock
    private Player observer;

    @Mock
    private World world;

    @BeforeAll
    public static void setUpClass() {
        TestHelper.setUpServer();
    }

    @BeforeEach
    public void setUp() {
        this.armorStandAdapter = new BukkitArmorStandAdapter();
        this.location = new Location(this.world, 0, 0, 0);
    }

    @Test
    public void spawnArmorStandFor() throws TestInitializationException {
        doReturn(this.armorStand).when(this.world).spawnEntity(any(Location.class), eq(EntityType.ARMOR_STAND));

        this.armorStandAdapter.spawnArmorStandFor(TestHelper.getPlugin(), this.observer, this.location, TEXT);

        verify(this.observer).showEntity(TestHelper.getPlugin(), this.armorStand);

        verify(this.armorStand).setCustomName(TEXT);
    }

    @Test
    public void destroyArmorStandFor() {
        List<Entity> entities = Arrays.asList(mock(Entity.class), mock(Entity.class));

        doReturn(1).when(entities.get(0)).getEntityId();
        doReturn(2).when(entities.get(1)).getEntityId();
        doReturn(this.world).when(this.observer).getWorld();
        doReturn(entities).when(this.world).getEntities();

        this.armorStandAdapter.destroyArmorStandFor(this.observer, 2);

        verify(entities.get(0), never()).remove();
        verify(entities.get(1)).remove();
    }

}
