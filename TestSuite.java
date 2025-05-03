import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        GameManagerTest.class,
        FrontEndTest.class,
        HexCubeTest.class
})
public class TestSuite {
}
