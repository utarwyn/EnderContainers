package fr.utarwyn.endercontainers.storage.backups;

import fr.utarwyn.endercontainers.TestHelper;
import fr.utarwyn.endercontainers.TestInitializationException;
import fr.utarwyn.endercontainers.backup.Backup;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(MockitoJUnitRunner.class)
public class BackupsFlatDataTest {

    private BackupsFlatData data;

    private Backup backup;

    private File dataFolder;

    private File backupFolder;

    private File fakeFile;

    private File file;

    @BeforeClass
    public static void setUpClass() throws TestInitializationException {
        TestHelper.setUpFiles();
    }

    private static FileTime getLastModifiedTime(File file) throws IOException {
        return Files.readAttributes(file.toPath(), BasicFileAttributes.class).lastModifiedTime();
    }

    private static void copy(File from, File folderTo) throws IOException {
        Files.copy(from.toPath(), new File(folderTo, from.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private static void emptyFolder(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                Arrays.stream(files).forEach(File::delete);
            }
        } else {
            folder.mkdirs();
        }
    }

    @Before
    public void setUp() throws TestInitializationException, IOException {
        this.data = new BackupsFlatData(TestHelper.getPlugin());
        this.backup = new Backup("test", new Timestamp(System.currentTimeMillis()), "Utarwyn");

        File testingFolder = TestHelper.getPlugin().getDataFolder();
        this.dataFolder = new File(testingFolder, "data");
        this.backupFolder = new File(testingFolder, "backups" + File.separator + "test");
        this.fakeFile = new File(testingFolder, "fakedatafile.yml");
        this.file = new File(testingFolder, "backups.yml");
    }

    @Test
    public void load() {
        assertThat(this.data.configuration).isNotNull();
        assertThat(this.data.configuration.isConfigurationSection("backups")).isTrue();

        assertThat(this.data.backups).isNotEmpty().hasAtLeastOneElementOfType(Backup.class);
        assertThat(this.data.backups.get(0).getName()).isEqualTo("Name 1");
        assertThat(this.data.backups.get(0).getDate()).isEqualTo(new Timestamp(1616609845000L));
    }

    @Test
    public void save() {
        this.data.save();

        try (Scanner reader = new Scanner(file)) {
            assertThat(reader.hasNextLine()).isTrue();
            assertThat(reader.nextLine()).isEqualTo("backups:");
            assertThat(reader.nextLine()).isEqualTo("  backup1:");
            assertThat(reader.hasNextLine()).isTrue();
        } catch (IOException e) {
            fail("backups file must be written on the disk");
        }
    }

    @Test
    public void saveNewBackup() throws IOException {
        FileTime beforeTime = getLastModifiedTime(this.file);
        assertThat(this.data.saveNewBackup(this.backup)).isTrue();

        try {
            Thread.sleep(200);
        } catch (Exception ignored) {
        }

        assertThat(beforeTime).isLessThan(getLastModifiedTime(this.file));
        assertThat(this.data.configuration.isConfigurationSection("backups.test")).isTrue();
        assertThat(this.data.configuration.get("backups.test.name")).isEqualTo("test");
        assertThat(this.data.configuration.get("backups.test.createdBy")).isEqualTo("Utarwyn");
    }

    @Test
    public void executeStorage() throws IOException {
        // Prepare test
        emptyFolder(this.backupFolder);
        emptyFolder(this.dataFolder);
        copy(this.fakeFile, this.dataFolder);

        // Execute storage
        this.data.executeStorage(this.backup);
        assertThat(this.backupFolder).exists();
        assertThat(this.backupFolder.listFiles()).isNotEmpty().hasSize(1);

        // Still one file in the folder
        this.data.executeStorage(this.backup);
        assertThat(this.backupFolder.listFiles()).isNotEmpty().hasSize(1);
    }

    @Test
    public void applyBackup() throws IOException {
        // Prepare test
        emptyFolder(this.backupFolder);
        emptyFolder(this.dataFolder);
        copy(this.fakeFile, this.backupFolder);

        // Apply the backup
        this.data.applyBackup(this.backup);
        assertThat(this.dataFolder).exists();
        assertThat(this.dataFolder.listFiles()).isNotNull().isNotEmpty().hasSize(1);

        // Still one file in the folder
        this.data.applyBackup(this.backup);
        assertThat(this.dataFolder.listFiles()).isNotNull().isNotEmpty().hasSize(1);
    }

    @Test
    public void removeBackup() throws IOException {
        // Prepare test
        emptyFolder(this.backupFolder);
        copy(this.fakeFile, this.backupFolder);

        // Remove and check if backup file saved
        FileTime beforeTime = getLastModifiedTime(this.file);
        assertThat(this.data.removeBackup(this.backup)).isTrue();
        assertThat(beforeTime).isLessThan(getLastModifiedTime(this.file));

        // Check configuration updated and folder removed
        assertThat(this.data.configuration.contains("backups.test")).isFalse();
        assertThat(this.backupFolder).doesNotExist();

        // Can remove twice
        assertThat(this.data.removeBackup(this.backup)).isTrue();
    }

}
