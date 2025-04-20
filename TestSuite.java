import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        GameMngrTest.class,
        FrontEndTest.class,
        HexCubeTest.class
})
public class TestSuite {
}
