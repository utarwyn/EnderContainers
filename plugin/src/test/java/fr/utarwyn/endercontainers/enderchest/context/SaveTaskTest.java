package fr.utarwyn.endercontainers.enderchest.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SaveTaskTest {

    private SaveTask task;

    @Mock
    private PlayerContext context;

    @BeforeEach
    public void setUp() {
        this.task = new SaveTask(this.context);
    }

    @Test
    public void run() {
        this.task.run();
        verify(this.context).save();
    }

}
