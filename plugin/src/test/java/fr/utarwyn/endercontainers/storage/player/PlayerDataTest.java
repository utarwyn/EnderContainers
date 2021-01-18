package fr.utarwyn.endercontainers.storage.player;

import com.google.common.collect.Sets;
import fr.utarwyn.endercontainers.enderchest.EnderChest;
import fr.utarwyn.endercontainers.enderchest.VanillaEnderChest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PlayerDataTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private PlayerData playerData;

    @Test
    public void saveContext() {
        EnderChest enderchest1 = mock(VanillaEnderChest.class);
        EnderChest enderchest2 = mock(EnderChest.class);
        EnderChest enderchest3 = mock(EnderChest.class);

        this.playerData.saveContext(Sets.newHashSet(enderchest1, enderchest2, enderchest3));

        // does not save vanilla enderchest with our system
        verify(this.playerData, never()).saveEnderchest(enderchest1);
        // save custom enderchests
        verify(this.playerData).saveEnderchest(enderchest2);
        verify(this.playerData).saveEnderchest(enderchest3);
    }

}
