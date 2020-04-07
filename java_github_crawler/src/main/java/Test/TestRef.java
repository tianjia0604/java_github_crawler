package Test;

import java.lang.reflect.Field;

public class TestRef {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        String str = "hello";
        Field field = String.class.getDeclaredField("value");
        field.setAccessible(true);
        //value就是str内部
        char[] value = (char[])field.get(str);
    }
}

