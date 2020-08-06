package fr.utarwyn.endercontainers.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * All tests about {@link SemanticVersion} class.
 *
 * @author Utarwyn
 * @since 2.2.1
 */
public class SemanticVersionTest {

    @Test
    public void construct() {
        SemanticVersion v1 = new SemanticVersion("2.5.6");
        assertThat(v1.getMajor()).isEqualTo(2);
        assertThat(v1.getMinor()).isEqualTo(5);
        assertThat(v1.getPatch()).isEqualTo(6);
        assertThat(v1.isDevelopment()).isFalse();

        SemanticVersion vDev = new SemanticVersion("4.65.17-dev");
        assertThat(vDev.getMajor()).isEqualTo(4);
        assertThat(vDev.getMinor()).isEqualTo(65);
        assertThat(vDev.getPatch()).isEqualTo(17);
        assertThat(vDev.isDevelopment()).isTrue();
    }

    @Test
    public void malformed() {
        assertThatIllegalArgumentException().isThrownBy(() -> new SemanticVersion(""));
        assertThatIllegalArgumentException().isThrownBy(() -> new SemanticVersion("2.2"));
        assertThatIllegalArgumentException().isThrownBy(() -> new SemanticVersion("a.6.7"));
        assertThatIllegalArgumentException().isThrownBy(() -> new SemanticVersion("3..8"));
    }

    @Test
    public void compareMajor() {
        SemanticVersion v1 = new SemanticVersion("1.0.0");
        SemanticVersion v2 = new SemanticVersion("2.0.0");
        assertThat(v2.compareTo(v1)).isEqualTo(1);
    }

    @Test
    public void compareMinor() {
        SemanticVersion v1 = new SemanticVersion("1.0.0");
        SemanticVersion v2 = new SemanticVersion("1.1.0");
        assertThat(v2.compareTo(v1)).isEqualTo(1);
    }

    @Test
    public void comparePatch() {
        SemanticVersion v1 = new SemanticVersion("1.0.0");
        SemanticVersion v2 = new SemanticVersion("1.0.1");
        assertThat(v2.compareTo(v1)).isEqualTo(1);
    }

    @Test
    public void compareDevBuild() {
        SemanticVersion vStable = new SemanticVersion("1.1.0");
        SemanticVersion vDev = new SemanticVersion("1.1.0-dev");
        assertThat(vStable.compareTo(vDev)).isEqualTo(1);

        vStable = new SemanticVersion("1.1.1");
        vDev = new SemanticVersion("1.1.0-dev");
        assertThat(vStable.compareTo(vDev)).isEqualTo(1);
    }

}
