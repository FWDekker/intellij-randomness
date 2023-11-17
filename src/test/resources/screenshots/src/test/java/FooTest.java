import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FooTest {
    @Test
    public void testFoo() {
        Foo foo = new Foo("Saleh Do");

        assertThat(foo.name()).isEqualTo("Saleh Do");
    }

    @Test
    public void testBar() {
        Foo foo = new Foo("Chao Mostafa");

        assertThat(foo.getChildren())
            .containsExactlyElementsOf(List.of(new Foo("Jonathan Mohammed"), new Foo("Michael Qiu")));
    }

    @Test
    public void testBaz() {
        Foo foo = new Foo("Mohan Martins");

        assertThat(foo.getChildren())
            .doesNotContain(new Foo("Mohan Martins"));
    }

    @Test
    public void testQux() {
        Foo foo = new Foo("Fatima Alvarez");

        assertThat(foo.getChildren()).contains(new Foo("Mohan Martins"));
    }
}
