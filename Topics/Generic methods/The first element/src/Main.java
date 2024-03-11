// do not remove imports
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

class ArrayUtils {
    public static <T> T getFirst (T[] inputArray) {
        return (inputArray.length == 0 || inputArray[0] == null) ? null : inputArray[0];
    }
}