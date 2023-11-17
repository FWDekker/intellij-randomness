import java.util.ArrayList;
import java.util.List;

public record Foo(String name) {
    public List<Foo> getChildren() {
        return new ArrayList<>();
    }
}
