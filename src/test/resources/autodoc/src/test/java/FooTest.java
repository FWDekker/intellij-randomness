import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class FooTest {
    @Test
    public void testFoo() {
        Foo foo = new Foo("Name");

        assertThat(foo.name()).isEqualTo("Name");
    }

    @Test
    public void testBar() {
        Foo foo = new Foo("Name");

        assertThat(foo.getChildren()).hasSize(5);
    }
}
