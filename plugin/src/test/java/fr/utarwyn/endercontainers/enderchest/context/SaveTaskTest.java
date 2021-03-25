package fr.utarwyn.endercontainers.enderchest.context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SaveTaskTest {

    private SaveTask task;

    @Mock
    private PlayerContext context;

    @Before
    public void setUp() {
        this.task = new SaveTask(this.context);
    }

    @Test
    public void run() {
        this.task.run();
        verify(this.context).save();
    }

}
