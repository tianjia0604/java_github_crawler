package Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestGson {
    static class Test {
        private int aaa;
        private int bbb;
    }

    public static void main(String[] args) {
        Gson gson = new GsonBuilder().create();
        String jsonString = "{\"aaa\": 1,\"bbb\": 2}";
        //Test.class取出当前这个类的类对象
        //fromJson方法实现，依赖反射机制
        Test t = gson.fromJson(jsonString, Test.class);
        System.out.println(t.aaa);
        System.out.println(t.bbb);
    }
}
